package com.navin.kidsdrawing.coloring.regions

import com.navin.kidsdrawing.drawing.domain.ColorRegionFillRecord
import com.navin.kidsdrawing.drawing.domain.ColorRegionPoint
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.lesson.model.AuthoredColorRegion
import com.navin.kidsdrawing.lesson.model.AuthoredRegionPoint
import com.navin.kidsdrawing.lesson.model.ColoringRegionCatalogSource
import com.navin.kidsdrawing.lesson.model.ColoringStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreparedColoringRegionEngineTest {
    private val body = region("body", 100f, 100f, 400f, 400f)
    private val tail = region("tail", 500f, 100f, 700f, 300f)
    private val fin = region("fin", 500f, 400f, 700f, 600f)
    private val engine = PreparedColoringRegionEngine(
        ColoringRegionCatalogSource(
            schemaVersion = "1.0",
            regions = listOf(body, tail, fin),
        ),
    )

    @Test
    fun hitTestingReturnsInsideAndBoundaryRegionsButRejectsOutsideOrDisallowedTargets() {
        assertEquals("body", engine.regionContaining(250f, 250f)?.id)
        assertEquals("body", engine.regionContaining(100f, 250f)?.id)
        assertNull(engine.regionContaining(50f, 50f))
        assertNull(engine.regionContaining(250f, 250f, allowedRegionIds = setOf("tail")))
        assertEquals("tail", engine.regionContaining(600f, 200f, allowedRegionIds = setOf("tail"))?.id)
    }

    @Test
    fun guidedProgressRequiresEveryRegionInAMultiRegionStep() {
        val steps = listOf(
            ColoringStep(id = "color_body", regionIds = listOf("body")),
            ColoringStep(id = "color_tail_fin", regionIds = listOf("tail", "fin")),
        )

        val initial = engine.guidedProgress(steps, document())
        assertEquals(0, initial.currentStepIndex)
        assertFalse(initial.isComplete)

        val afterBody = engine.guidedProgress(steps, document(fillOperation("body", 1L)))
        assertEquals(1, afterBody.currentStepIndex)
        assertEquals(1, afterBody.completedStepCount)
        assertFalse(afterBody.isComplete)

        val afterTailOnly = engine.guidedProgress(
            steps,
            document(fillOperation("body", 1L), fillOperation("tail", 2L)),
        )
        assertEquals(1, afterTailOnly.currentStepIndex)
        assertFalse(afterTailOnly.isComplete)

        val complete = engine.guidedProgress(
            steps,
            document(
                fillOperation("body", 1L),
                fillOperation("tail", 2L),
                fillOperation("fin", 3L),
            ),
        )
        assertTrue(complete.isComplete)
        assertNull(complete.currentStepIndex)
        assertEquals(2, complete.completedStepCount)
        assertEquals(setOf("body", "tail", "fin"), complete.filledRegionIds)
    }

    private fun region(id: String, left: Float, top: Float, right: Float, bottom: Float) =
        AuthoredColorRegion(
            id = id,
            points = listOf(
                AuthoredRegionPoint(left, top),
                AuthoredRegionPoint(right, top),
                AuthoredRegionPoint(right, bottom),
                AuthoredRegionPoint(left, bottom),
            ),
        )

    private fun fillOperation(regionId: String, time: Long) = DocumentOperation.AddColorRegionFill(
        operationId = "fill-$regionId-$time",
        createdAtEpochMillis = time,
        fill = ColorRegionFillRecord(
            regionId = regionId,
            colorArgb = 0xFFE47C68.toInt(),
            points = listOf(
                ColorRegionPoint(100f, 100f),
                ColorRegionPoint(200f, 100f),
                ColorRegionPoint(200f, 200f),
                ColorRegionPoint(100f, 200f),
            ),
        ),
    )

    private fun document(vararg operations: DocumentOperation) = DrawingDocument(
        documentId = "prepared-coloring-test",
        logicalSize = DocumentSize(1000f, 1000f),
        createdAtEpochMillis = 0L,
        modifiedAtEpochMillis = operations.maxOfOrNull { it.createdAtEpochMillis } ?: 0L,
        operations = operations.toList(),
    )
}
