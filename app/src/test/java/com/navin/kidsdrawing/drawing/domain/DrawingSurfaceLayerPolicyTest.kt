package com.navin.kidsdrawing.drawing.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingSurfaceLayerPolicyTest {
    @Test
    fun coloringCompositesCommittedAndWetColorBelowProtectedLineArt() {
        val layers = DrawingSurfaceContentRole.COLORING.renderLayersBackToFront()

        assertEquals(
            listOf(
                DrawingSurfaceRenderLayer.COMMITTED_COLOR,
                DrawingSurfaceRenderLayer.IN_PROGRESS_CHILD_INPUT,
                DrawingSurfaceRenderLayer.PROTECTED_LINE_ART_OVERLAY,
            ),
            layers,
        )
        assertTrue(
            layers.indexOf(DrawingSurfaceRenderLayer.COMMITTED_COLOR) <
                layers.indexOf(DrawingSurfaceRenderLayer.PROTECTED_LINE_ART_OVERLAY),
        )
        assertTrue(
            layers.indexOf(DrawingSurfaceRenderLayer.IN_PROGRESS_CHILD_INPUT) <
                layers.indexOf(DrawingSurfaceRenderLayer.PROTECTED_LINE_ART_OVERLAY),
        )
    }

    @Test
    fun lineArtEditorKeepsCommittedColorBelowProtectedDrawing() {
        assertEquals(
            listOf(
                DrawingSurfaceRenderLayer.COMMITTED_COLOR,
                DrawingSurfaceRenderLayer.COMMITTED_LINE_ART,
                DrawingSurfaceRenderLayer.IN_PROGRESS_CHILD_INPUT,
            ),
            DrawingSurfaceContentRole.LINE_ART.renderLayersBackToFront(),
        )
    }
}
