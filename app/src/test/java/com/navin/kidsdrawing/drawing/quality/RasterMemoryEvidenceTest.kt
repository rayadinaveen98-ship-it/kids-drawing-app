package com.navin.kidsdrawing.drawing.quality

import com.navin.kidsdrawing.drawing.domain.DocumentSize
import org.junit.Assert.assertEquals
import org.junit.Test

class RasterMemoryEvidenceTest {
    @Test
    fun `standard document exposes line and color raster byte estimates`() {
        val estimate = RasterMemoryEvidence.standardDocument()

        assertEquals(1_000, estimate.documentWidthPx)
        assertEquals(1_000, estimate.documentHeightPx)
        assertEquals(4_000_000L, estimate.baseArgbRasterBytes)
        assertEquals(8, estimate.lineCheckpointCount)
        assertEquals(36_000_000L, estimate.lineProjectionUpperBoundBytes)
        assertEquals(4_000_000L, estimate.colorProjectionBytes)
        assertEquals(40_000_000L, estimate.combinedProjectionUpperBoundBytes)
    }

    @Test
    fun `estimate scales with document dimensions rather than frame count`() {
        val small = RasterMemoryEvidence.estimate(DocumentSize(500f, 500f))
        val standard = RasterMemoryEvidence.standardDocument()

        assertEquals(1_000_000L, small.baseArgbRasterBytes)
        assertEquals(10_000_000L, small.combinedProjectionUpperBoundBytes)
        assertEquals(4L, standard.baseArgbRasterBytes / small.baseArgbRasterBytes)
        assertEquals(4L, standard.combinedProjectionUpperBoundBytes / small.combinedProjectionUpperBoundBytes)
    }
}
