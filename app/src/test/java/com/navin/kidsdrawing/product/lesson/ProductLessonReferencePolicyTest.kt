package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.AuthoredPoint
import com.navin.kidsdrawing.lesson.model.AuthoredStroke
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.ChildTurn
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.LessonAssets
import com.navin.kidsdrawing.lesson.model.LessonCanvas
import com.navin.kidsdrawing.lesson.model.LessonDrawing
import com.navin.kidsdrawing.lesson.model.LessonMetadata
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.LessonSource
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.StrokeCatalogSource
import com.navin.kidsdrawing.lesson.model.TeacherDemo
import com.navin.kidsdrawing.lesson.model.TeachingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductLessonReferencePolicyTest {
    @Test
    fun previewGeometryComesFromSelectedLessonPackageInsteadOfCuteCatSingleton() {
        val rocket = packageData("simple-rocket")

        val strokes = ProductLessonReferencePolicy.allTeacherStrokes(rocket)

        assertEquals(2, strokes.size)
        assertTrue(strokes.all { it.strokeId.contains("simple-rocket") })
        assertTrue(strokes.none { it.strokeId.contains("cute-cat") })
    }

    @Test
    fun missingPackageProducesNoInventedPreviewGeometry() {
        assertTrue(ProductLessonReferencePolicy.allTeacherStrokes(null).isEmpty())
    }

    private fun packageData(lessonId: String): LessonRuntimePackage {
        val steps = listOf(step("body", "stroke-body"), step("window", "stroke-window"))
        return LessonRuntimePackage(
            packageRoot = "lessons/$lessonId",
            lesson = LessonSource(
                schemaVersion = "1.0",
                lessonId = lessonId,
                revision = 1,
                status = LessonStatus.RELEASE,
                minimumContentApi = 1,
                metadata = LessonMetadata(
                    titleKey = "lesson.$lessonId.title",
                    summaryKey = "lesson.$lessonId.summary",
                    ageBands = listOf(AgeBand.CREATIVE_EXPLORERS),
                    difficulty = 2,
                    estimatedMinutes = 7,
                    categoryIds = listOf("space"),
                    skillIds = listOf("shapes"),
                ),
                canvas = LessonCanvas(1000, 1000),
                supportedModes = listOf(TeachingMode.WATCH_THEN_DRAW),
                assets = LessonAssets(
                    strokeFile = "strokes.json",
                    thumbnail = "thumbnail.svg",
                    preview = "preview.svg",
                    strings = mapOf("en" to "strings/en.json"),
                ),
                drawing = LessonDrawing(steps),
            ),
            strokeCatalog = StrokeCatalogSource(
                schemaVersion = "1.0",
                strokes = listOf(
                    stroke("stroke-body", 100f, 100f, 700f, 700f),
                    stroke("stroke-window", 350f, 300f, 650f, 300f),
                ),
            ),
        )
    }

    private fun step(id: String, strokeId: String) = DrawingStep(
        id = id,
        objectiveSkillIds = listOf("shapes"),
        teacher = TeacherDemo(strokeRefs = listOf(strokeId)),
        childTurn = ChildTurn(
            completionPolicy = ChildCompletionPolicy.MANUAL_DONE,
            allowReplay = true,
            allowSkip = false,
        ),
    )

    private fun stroke(id: String, x1: Float, y1: Float, x2: Float, y2: Float) = AuthoredStroke(
        id = id,
        points = listOf(
            AuthoredPoint(x = x1, y = y1, timeMs = 0L),
            AuthoredPoint(x = x2, y = y2, timeMs = 500L),
        ),
    )
}
