package com.navin.kidsdrawing.product.quality

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductTimingLedgerTest {
    @Test
    fun `all required product timing surfaces are represented`() {
        assertEquals(
            setOf(
                ProductTimingMetric.PROFILE_RESOLUTION,
                ProductTimingMetric.HOME_LOAD,
                ProductTimingMetric.GALLERY_LIST,
                ProductTimingMetric.GALLERY_REOPEN,
                ProductTimingMetric.LESSON_RECOVERY,
                ProductTimingMetric.COLORING_RECOVERY,
                ProductTimingMetric.FREE_DRAW_RECOVERY,
            ),
            ProductTimingMetric.entries.toSet(),
        )
    }

    @Test
    fun `metrics are isolated and expose sample count median and tail percentiles`() {
        val ledger = ProductTimingLedger()
        repeat(5) { index ->
            ledger.record(ProductTimingMetric.HOME_LOAD, (index + 1L) * 1_000_000L)
        }
        ledger.record(ProductTimingMetric.GALLERY_LIST, 20_000_000L)

        val home = ledger.snapshot(ProductTimingMetric.HOME_LOAD)
        val gallery = ledger.snapshot(ProductTimingMetric.GALLERY_LIST)

        assertEquals(5L, home.sampleCount)
        assertEquals(3, home.medianMillis)
        assertEquals(5, home.p95Millis)
        assertEquals(5, home.p99Millis)
        assertEquals(1L, gallery.sampleCount)
        assertEquals(20, gallery.medianMillis)
    }

    @Test
    fun `reset all clears every local timing bucket`() {
        val ledger = ProductTimingLedger()
        ProductTimingMetric.entries.forEach { metric -> ledger.record(metric, 1_000_000L) }

        ledger.resetAll()

        val snapshots = ledger.snapshotAll()
        assertEquals(ProductTimingMetric.entries.size, snapshots.size)
        assertTrue(snapshots.values.all { it.sampleCount == 0L })
        assertTrue(snapshots.values.all { it.medianMillis == null })
        assertNull(ledger.snapshot(ProductTimingMetric.HOME_LOAD).p95Millis)
    }
}
