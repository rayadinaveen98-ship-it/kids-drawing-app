package com.navin.kidsdrawing.drawing.quality

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColoringQualityWorkloadFactoryTest {
    @Test
    fun `c1 is deterministic and contains protected line art plus coloring operations`() {
        val first = ColoringQualityWorkloadFactory.c1()
        val second = ColoringQualityWorkloadFactory.c1()

        assertEquals(first, second)
        assertEquals(ColoringQualityWorkloadFactory.C1_OPERATION_COUNT, first.operations.size)
        assertTrue(first.operations.any { it is DocumentOperation.AddInkStroke })
        assertTrue(first.operations.any { it is DocumentOperation.AddColorStroke })
        assertTrue(first.operations.any { it is DocumentOperation.AddColorEraseMask })
        assertTrue(first.operations.any { it is DocumentOperation.AddColorRegionFill })
    }

    @Test
    fun `heavy and stress workloads preserve exact requested operation counts`() {
        val c2 = ColoringQualityWorkloadFactory.c2()
        val c3 = ColoringQualityWorkloadFactory.c3()

        assertEquals(ColoringQualityWorkloadFactory.C2_OPERATION_COUNT, c2.operations.size)
        assertEquals(ColoringQualityWorkloadFactory.C3_OPERATION_COUNT, c3.operations.size)
        assertTrue(c2.hasColoringOperations())
        assertTrue(c3.hasColoringOperations())
    }

    @Test
    fun `color fills are valid self contained polygons`() {
        val fills = ColoringQualityWorkloadFactory.c2().operations
            .filterIsInstance<DocumentOperation.AddColorRegionFill>()

        assertTrue(fills.isNotEmpty())
        assertTrue(fills.all { it.fill.points.size == 4 })
        assertTrue(fills.all { it.fill.points.distinct().size == 4 })
    }
}
