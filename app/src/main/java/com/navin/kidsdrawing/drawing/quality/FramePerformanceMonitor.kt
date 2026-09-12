package com.navin.kidsdrawing.drawing.quality

import kotlin.math.max

data class FramePerformanceSnapshot(
    val frameCount: Long = 0L,
    val jankFrameCount: Long = 0L,
    val over16_7FrameCount: Long = 0L,
    val p95UiMillis: Int? = null,
    val p99UiMillis: Int? = null,
    val maxUiMillis: Double? = null,
    val jankRatePercent: Double = 0.0,
    val over16_7RatePercent: Double = 0.0,
)

/**
 * Allocation-light accumulator for Art Lab JankStats callbacks.
 *
 * Durations are recorded into 1 ms histogram buckets. We deliberately avoid retaining FrameData
 * instances (JankStats reuses them) and avoid sorting frame lists on the UI thread. Hardware gate
 * evidence reads a snapshot at stable intervals rather than doing expensive work per frame.
 *
 * `jankRatePercent` is JankStats' native-refresh classification. `over16_7RatePercent` is a
 * separate 60 Hz-equivalent rate used by the Phase 1 Class-M/L contract so 90/120 Hz devices are
 * not incorrectly failed solely because their native refresh deadline is stricter than 16.7 ms.
 */
class FramePerformanceMonitor {
    private val histogram = LongArray(MAX_BUCKET_MILLIS + 1)
    private var frameCount = 0L
    private var jankFrameCount = 0L
    private var over16_7FrameCount = 0L
    private var maxDurationNanos = 0L

    @Synchronized
    fun record(frameDurationUiNanos: Long, isJank: Boolean) {
        val duration = frameDurationUiNanos.coerceAtLeast(0L)
        val bucketMillis = ceilMillis(duration).coerceAtMost(MAX_BUCKET_MILLIS)
        histogram[bucketMillis]++
        frameCount++
        if (isJank) jankFrameCount++
        if (duration > SIXTY_HZ_FRAME_NANOS) over16_7FrameCount++
        maxDurationNanos = max(maxDurationNanos, duration)
    }

    @Synchronized
    fun snapshot(): FramePerformanceSnapshot {
        if (frameCount == 0L) return FramePerformanceSnapshot()
        return FramePerformanceSnapshot(
            frameCount = frameCount,
            jankFrameCount = jankFrameCount,
            over16_7FrameCount = over16_7FrameCount,
            p95UiMillis = percentileBucket(0.95),
            p99UiMillis = percentileBucket(0.99),
            maxUiMillis = maxDurationNanos / NANOS_PER_MILLISECOND.toDouble(),
            jankRatePercent = jankFrameCount * 100.0 / frameCount,
            over16_7RatePercent = over16_7FrameCount * 100.0 / frameCount,
        )
    }

    @Synchronized
    fun reset() {
        histogram.fill(0L)
        frameCount = 0L
        jankFrameCount = 0L
        over16_7FrameCount = 0L
        maxDurationNanos = 0L
    }

    private fun percentileBucket(percentile: Double): Int {
        val target = kotlin.math.ceil(frameCount * percentile).toLong().coerceAtLeast(1L)
        var cumulative = 0L
        histogram.forEachIndexed { millis, count ->
            cumulative += count
            if (cumulative >= target) return millis
        }
        return MAX_BUCKET_MILLIS
    }

    private fun ceilMillis(nanos: Long): Int =
        ((nanos + NANOS_PER_MILLISECOND - 1L) / NANOS_PER_MILLISECOND)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()

    private companion object {
        const val NANOS_PER_MILLISECOND = 1_000_000L
        const val MAX_BUCKET_MILLIS = 500
        const val SIXTY_HZ_FRAME_NANOS = 16_700_000L
    }
}
