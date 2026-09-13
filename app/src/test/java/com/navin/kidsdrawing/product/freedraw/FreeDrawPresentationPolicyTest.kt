package com.navin.kidsdrawing.product.freedraw

import com.navin.kidsdrawing.drawing.domain.DrawingBrushPreset
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.product.profile.AgeBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeDrawPresentationPolicyTest {
    @Test
    fun ageBandsChangeDensityWithoutChangingPaletteCapability() {
        val little = freeDrawPresentationPolicyFor(AgeBand.LITTLE_ARTIST)
        val young = freeDrawPresentationPolicyFor(AgeBand.YOUNG_ARTIST)

        assertEquals(CHILD_SAFE_PALETTE, little.paletteArgb)
        assertEquals(CHILD_SAFE_PALETTE, young.paletteArgb)
        assertTrue(little.minimumControlHeightDp > young.minimumControlHeightDp)
        assertTrue(little.toolColumns < young.toolColumns)
        assertTrue(little.paletteColumns < young.paletteColumns)
        assertTrue(little.showToolDescriptions)
        assertFalse(young.showToolDescriptions)
    }

    @Test
    fun eachDrawingPresetAndEraserExposeThreeBoundedSizeChoices() {
        DrawingBrushPreset.entries.forEach { preset ->
            val choices = freeDrawSizeChoices(DrawingTool.PENCIL, preset)
            assertEquals(3, choices.size)
            assertTrue(choices.zipWithNext().all { (a, b) -> a.width < b.width })
        }
        assertEquals(3, freeDrawSizeChoices(DrawingTool.ERASER, DrawingBrushPreset.PENCIL).size)
    }
}
