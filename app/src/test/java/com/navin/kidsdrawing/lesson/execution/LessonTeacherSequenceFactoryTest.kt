package com.navin.kidsdrawing.lesson.execution

import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs

class LessonTeacherSequenceFactoryTest {
    @Test
    fun headSequencePreservesGeometryButScalesTimingToAuthoredDuration() {
        val packageData = packageData()
        val step = packageData.lesson.drawing.steps.first { it.id == "head" }
        val authored = packageData.strokeCatalog.strokes.first { it.id == "head-outline" }

        val sequence = LessonTeacherSequenceFactory.create(packageData, step)

        assertEquals(1, sequence.strokes.size)
        assertEquals(step.teacher.normalDurationMs!!.toLong(), sequence.sourceDurationMillis)
        val generated = sequence.strokes.single().stroke
        assertEquals(StrokeAuthorRole.TEACHER_GENERATED, generated.authorRole)
        assertEquals("pencil.standard", generated.brushPresetId)
        assertEquals(authored.points.size, generated.points.size)
        authored.points.zip(generated.points).forEach { (source, mapped) ->
            assertEquals(source.x, mapped.x)
            assertEquals(source.y, mapped.y)
            assertEquals(source.pressure, mapped.pressure)
        }
        assertEquals(0L, generated.points.first().elapsedTimeMillis)
        assertEquals(step.teacher.normalDurationMs!!.toLong(), generated.points.last().elapsedTimeMillis)
    }

    @Test
    fun multiStrokeStepIsOneOrderedSequenceWithTargetDuration() {
        val packageData = packageData()
        val step = packageData.lesson.drawing.steps.first { it.id == "ears" }

        val sequence = LessonTeacherSequenceFactory.create(packageData, step)

        assertEquals(2, sequence.strokes.size)
        assertEquals(step.teacher.normalDurationMs!!.toLong(), sequence.sourceDurationMillis)
        assertTrue(sequence.strokes[1].startTimeMillis > sequence.strokes[0].endTimeMillis)
        assertTrue(sequence.strokes.all { it.stroke.authorRole == StrokeAuthorRole.TEACHER_GENERATED })
        assertTrue(sequence.strokes.all { source ->
            source.stroke.points.zipWithNext().all { (a, b) -> b.elapsedTimeMillis >= a.elapsedTimeMillis }
        })
        assertTrue(abs(sequence.sourceDurationMillis - step.teacher.normalDurationMs!!.toLong()) <= 1L)
    }

    @Test
    fun teacherSequenceIsRejectedByRealChildDocumentEngine() = runBlocking {
        val packageData = packageData()
        val childDocument = DrawingDocument(
            documentId = "child-doc",
            logicalSize = DocumentSize(1000f, 1000f),
            createdAtEpochMillis = 10L,
            modifiedAtEpochMillis = 10L,
            metadata = DrawingDocumentMetadata(
                lessonId = packageData.lesson.lessonId,
                lessonRevision = packageData.lesson.revision,
            ),
        )
        val childEngine = DrawingDocumentEngine(
            initialDocument = childDocument,
            clockMillis = { 20L },
            idFactory = { "should-not-be-used" },
        )
        val sequence = LessonTeacherSequenceFactory.create(
            packageData,
            packageData.lesson.drawing.steps.first(),
        )
        val teacherStroke = sequence.strokes.single().stroke

        var rejected = false
        try {
            childEngine.commitChildStroke(teacherStroke)
        } catch (_: IllegalArgumentException) {
            rejected = true
        }

        assertTrue("Teacher-generated stroke must be rejected by child history.", rejected)
        assertEquals(childDocument, childEngine.state.value.document)
        assertTrue(childEngine.state.value.document.operations.isEmpty())
        assertEquals(StrokeAuthorRole.TEACHER_GENERATED, teacherStroke.authorRole)
    }

    private fun packageData(): LessonRuntimePackage {
        val loader = LessonPackageLoader(
            LessonPackageSource { path ->
                File("src/main/assets/$path").takeIf(File::isFile)?.readText()
            },
        )
        val result = loader.load(ROOT)
        assertTrue("Expected bundled lesson to load, got $result", result is LessonLoadResult.Success)
        return (result as LessonLoadResult.Success).packageData
    }

    private companion object {
        const val ROOT = "lessons/cute-cat"
    }
}
