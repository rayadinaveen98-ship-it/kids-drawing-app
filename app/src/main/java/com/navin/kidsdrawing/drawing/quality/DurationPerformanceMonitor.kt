package com.navin.kidsdrawing.drawing.quality

import kotlin.math.max

data class DurationPerformanceSnapshot(
    val sampleCount: Long = 0L,
    val medianMillis: Int? = null,
    val p95Millis: Int? = null,
    val p99Millis: Int? = null,
    val maxMillis: Double? = null,
)

/** Allocation-light percentile accumulator for timing paths such as touch dispatch. */
class DurationPerformanceMonitor {
    private val histogram = LongArray(MAX_BUCKET_MILLIS + 1)
    private var sampleCount = 0L
    private var maxDurationNanos = 0L

    @Synchronized
    fun record(durationNanos: Long) {
        val duration = durationNanos.coerceAtLeast(0L)
        val bucket = ceilMillis(duration).coerceAtMost(MAX_BUCKET_MILLIS)
        histogram[bucket]++
        sampleCount++
        maxDurationNanos = max(maxDurationNanos, duration)
    }

    @Synchronized
    fun snapshot(): DurationPerformanceSnapshot {
        if (sampleCount == 0L) return DurationPerformanceSnapshot()
        return DurationPerformanceSnapshot(
            sampleCount = sampleCount,
            medianMillis = percentileBucket(0.50),
            p95Millis = percentileBucket(0.95),
            p99Millis = percentileBucket(0.99),
            maxMillis = maxDurationNanos / NANOS_PER_MILLISECOND.toDouble(),
        )
    }

    @Synchronized
    fun reset() {
        histogram.fill(0L)
        sampleCount = 0L
        maxDurationNanos = 0L
    }

    private fun percentileBucket(percentile: Double): Int {
        val target = kotlin.math.ceil(sampleCount * percentile).toLong().coerceAtLeast(1L)
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
    }
}
