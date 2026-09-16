package com.navin.kidsdrawing.product.quality

import com.navin.kidsdrawing.drawing.quality.DurationPerformanceMonitor
import com.navin.kidsdrawing.drawing.quality.DurationPerformanceSnapshot

enum class ProductTimingMetric(val childSafeLabel: String) {
    PROFILE_RESOLUTION("Profile resolution"),
    HOME_LOAD("Home load"),
    GALLERY_LIST("Gallery list"),
    GALLERY_REOPEN("Gallery reopen"),
    LESSON_RECOVERY("Lesson recovery/start"),
    COLORING_RECOVERY("Coloring recovery"),
    FREE_DRAW_RECOVERY("Free Draw recovery"),
}

/**
 * Local/internal product timing evidence for P6.5.
 *
 * This class intentionally contains no analytics transport, identifiers or persistence. A Quality
 * Lab run records monotonic elapsed durations into named buckets and can reset them between runs.
 */
class ProductTimingLedger {
    private val monitors = ProductTimingMetric.entries.associateWith { DurationPerformanceMonitor() }

    fun record(metric: ProductTimingMetric, durationNanos: Long) {
        monitors.getValue(metric).record(durationNanos)
    }

    fun snapshot(metric: ProductTimingMetric): DurationPerformanceSnapshot =
        monitors.getValue(metric).snapshot()

    fun snapshotAll(): Map<ProductTimingMetric, DurationPerformanceSnapshot> =
        ProductTimingMetric.entries.associateWith(::snapshot)

    fun reset(metric: ProductTimingMetric) {
        monitors.getValue(metric).reset()
    }

    fun resetAll() {
        monitors.values.forEach(DurationPerformanceMonitor::reset)
    }
}

/**
 * Process-local timing source shared by production operation boundaries and the internal Quality Lab.
 *
 * It deliberately uses [System.nanoTime] so elapsed measurement is monotonic, and it records failed
 * operations too because slow/failing recovery paths are still useful hardening evidence. Nothing is
 * persisted or uploaded.
 */
object ProductTimingEvidence {
    val ledger = ProductTimingLedger()

    suspend fun <T> measure(
        metric: ProductTimingMetric,
        block: suspend () -> T,
    ): T {
        val startedNanos = System.nanoTime()
        return try {
            block()
        } finally {
            ledger.record(metric, System.nanoTime() - startedNanos)
        }
    }

    fun <T> measureBlocking(
        metric: ProductTimingMetric,
        block: () -> T,
    ): T {
        val startedNanos = System.nanoTime()
        return try {
            block()
        } finally {
            ledger.record(metric, System.nanoTime() - startedNanos)
        }
    }
}
