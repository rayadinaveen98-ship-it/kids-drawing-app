package com.navin.kidsdrawing.product.quality

import com.navin.kidsdrawing.drawing.quality.DurationPerformanceMonitor
import com.navin.kidsdrawing.drawing.quality.DurationPerformanceSnapshot

enum class ProductTimingMetric(val childSafeLabel: String) {
    PROFILE_RESOLUTION("Profile resolution"),
    HOME_LOAD("Home load"),
    GALLERY_LIST("Gallery list"),
    GALLERY_REOPEN("Gallery reopen"),
    LESSON_RECOVERY("Lesson recovery"),
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
