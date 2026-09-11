package com.navin.kidsdrawing.drawing.quality

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FramePerformanceMonitorTest {
    @Test
    fun emptySnapshotHasNoFabricatedPercentiles() {
        val snapshot = FramePerformanceMonitor().snapshot()

        assertEquals(0L, snapshot.frameCount)
        assertEquals(0L, snapshot.jankFrameCount)
        assertNull(snapshot.p95UiMillis)
        assertNull(snapshot.p99UiMillis)
        assertEquals(0.0, snapshot.jankRatePercent, 0.0)
    }

    @Test
    fun histogramReportsExpectedP95P99AndJankRate() {
        val monitor = FramePerformanceMonitor()
        for (millis in 1..100) {
            monitor.record(
                frameDurationUiNanos = millis * 1_000_000L,
                isJank = millis % 10 == 0,
            )
        }

        val snapshot = monitor.snapshot()
        assertEquals(100L, snapshot.frameCount)
        assertEquals(10L, snapshot.jankFrameCount)
        assertEquals(95, snapshot.p95UiMillis)
        assertEquals(99, snapshot.p99UiMillis)
        assertEquals(100.0, snapshot.maxUiMillis!!, 0.0001)
        assertEquals(10.0, snapshot.jankRatePercent, 0.0001)
    }

    @Test
    fun subMillisecondDurationsRoundUpAndResetClearsEvidence() {
        val monitor = FramePerformanceMonitor()
        monitor.record(1L, isJank = false)
        monitor.record(1_000_001L, isJank = true)

        val before = monitor.snapshot()
        assertEquals(2L, before.frameCount)
        assertEquals(2, before.p95UiMillis)

        monitor.reset()
        val after = monitor.snapshot()
        assertEquals(0L, after.frameCount)
        assertNull(after.p95UiMillis)
    }
}
