package com.navin.kidsdrawing.drawing.quality

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DurationPerformanceMonitorTest {
    @Test
    fun reportsMedianP95P99AndReset() {
        val monitor = DurationPerformanceMonitor()
        for (millis in 1..100) monitor.record(millis * 1_000_000L)

        val before = monitor.snapshot()
        assertEquals(100L, before.sampleCount)
        assertEquals(50, before.medianMillis)
        assertEquals(95, before.p95Millis)
        assertEquals(99, before.p99Millis)
        assertEquals(100.0, before.maxMillis!!, 0.0001)

        monitor.reset()
        val after = monitor.snapshot()
        assertEquals(0L, after.sampleCount)
        assertNull(after.medianMillis)
        assertNull(after.p95Millis)
    }
}
