package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.HelpKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LessonPackageLoaderTest {
    @Test
    fun bundledCuteCatPackageLoadsDeterministically() {
        val result = loader().load(ROOT)

        assertTrue(result is LessonLoadResult.Success)
        val packageData = (result as LessonLoadResult.Success).packageData
        assertEquals("cute-cat", packageData.lesson.lessonId)
        assertEquals(1, packageData.lesson.revision)
        assertEquals(4, packageData.lesson.drawing.steps.size)
        assertEquals(8, packageData.strokeCatalog.strokes.size)
        assertEquals(4, packageData.strokeCatalog.guides.size)
        assertEquals(3, packageData.lesson.supportedModes.size)
    }

    @Test
    fun unknownLessonPropertyFailsStrictJsonDecode() {
        val invalid = lessonJson().replaceFirst(
            "\"schemaVersion\": \"1.0\",",
            "\"schemaVersion\": \"1.0\",\n  \"unexpected\": true,",
        )

        val result = loader(lessonOverride = invalid).load(ROOT)

        assertFailureHas(result, LessonDiagnosticCode.INVALID_JSON)
    }

    @Test
    fun unsupportedContentApiFailsBeforeSessionCanStart() {
        val invalid = lessonJson().replaceFirst(
            "\"minimumContentApi\": 1",
            "\"minimumContentApi\": 2",
        )

        val result = loader(lessonOverride = invalid).load(ROOT)

        assertFailureHas(result, LessonDiagnosticCode.UNSUPPORTED_CONTENT_API)
    }

    @Test
    fun lessonIdSchemaLengthIsEnforcedAtRuntime() {
        val invalid = lessonJson().replaceFirst(
            "\"lessonId\": \"cute-cat\"",
            "\"lessonId\": \"a\"",
        )

        val result = loader(lessonOverride = invalid).load(ROOT)

        assertFailureHas(result, LessonDiagnosticCode.INVALID_ID)
    }

    @Test
    fun unsafeStrokeAssetPathIsRejected() {
        val invalid = lessonJson().replaceFirst(
            "\"strokeFile\": \"strokes.json\"",
            "\"strokeFile\": \"../strokes.json\"",
        )

        val result = loader(lessonOverride = invalid).load(ROOT)

        assertFailureHas(result, LessonDiagnosticCode.UNSAFE_ASSET_PATH)
    }

    @Test
    fun duplicateStepIdsAreRejected() {
        val invalid = lessonJson().replaceFirst(
            "\"id\": \"ears\"",
            "\"id\": \"head\"",
        )

        val result = loader(lessonOverride = invalid).load(ROOT)

        assertFailureHas(result, LessonDiagnosticCode.DUPLICATE_ID)
    }

    @Test
    fun missingTeacherStrokeReferenceIsRejected() {
        val invalid = lessonJson().replaceFirst(
            "\"strokeRefs\": [\"head-outline\"]",
            "\"strokeRefs\": [\"missing-stroke\"]",
        )

        val result = loader(lessonOverride = invalid).load(ROOT)

        assertFailureHas(result, LessonDiagnosticCode.MISSING_REFERENCE)
    }

    @Test
    fun traceModeRequiresAuthoredTraceSourceForEveryStep() {
        val success = loader().load(ROOT) as LessonLoadResult.Success
        val lesson = success.packageData.lesson
        val first = lesson.drawing.steps.first()
        val invalidFirst = first.copy(
            childTurn = first.childTurn.copy(expectedStrokeRefs = emptyList()),
            help = first.help.filterNot { it.kind == HelpKind.TRACE_PATH },
        )
        val invalidLesson = lesson.copy(
            drawing = lesson.drawing.copy(
                steps = listOf(invalidFirst) + lesson.drawing.steps.drop(1),
            ),
        )

        val diagnostics = LessonPackageValidator.validate(
            invalidLesson,
            success.packageData.strokeCatalog,
            LessonPackageLoader.CURRENT_CONTENT_API,
        )

        assertTrue(diagnostics.any { it.code == LessonDiagnosticCode.INVALID_TRACE_SUPPORT })
    }

    @Test
    fun schemaParityBoundsAreEnforcedForMetadataAndSteps() {
        val success = loader().load(ROOT) as LessonLoadResult.Success
        val lesson = success.packageData.lesson
        val first = lesson.drawing.steps.first()
        val invalidLesson = lesson.copy(
            metadata = lesson.metadata.copy(
                prerequisiteLessonIds = listOf("Bad Lesson ID"),
                tags = List(21) { "tag_$it" },
                ageBands = listOf(lesson.metadata.ageBands.first(), lesson.metadata.ageBands.first()),
            ),
            drawing = lesson.drawing.copy(
                steps = listOf(
                    first.copy(
                        teacher = first.teacher.copy(normalDurationMs = 99),
                        help = first.help + first.help.first().copy(level = 5),
                    ),
                ) + lesson.drawing.steps.drop(1),
            ),
        )

        val diagnostics = LessonPackageValidator.validate(
            invalidLesson,
            success.packageData.strokeCatalog,
            LessonPackageLoader.CURRENT_CONTENT_API,
        )

        assertTrue(diagnostics.any { it.path == "metadata.prerequisiteLessonIds[0]" && it.code == LessonDiagnosticCode.INVALID_ID })
        assertTrue(diagnostics.any { it.path == "metadata.tags" && it.code == LessonDiagnosticCode.INVALID_VALUE })
        assertTrue(diagnostics.any { it.path == "metadata.ageBands" && it.code == LessonDiagnosticCode.DUPLICATE_ID })
        assertTrue(diagnostics.any { it.path.endsWith("teacher.normalDurationMs") && it.code == LessonDiagnosticCode.INVALID_VALUE })
    }

    @Test
    fun nonMonotonicStrokeTimestampIsRejected() {
        val success = loader().load(ROOT) as LessonLoadResult.Success
        val catalog = success.packageData.strokeCatalog
        val firstStroke = catalog.strokes.first()
        val brokenPoints = firstStroke.points.toMutableList().also {
            it[1] = it[1].copy(timeMs = -1)
        }
        val invalidCatalog = catalog.copy(
            strokes = listOf(firstStroke.copy(points = brokenPoints)) + catalog.strokes.drop(1),
        )

        val diagnostics = LessonPackageValidator.validate(
            success.packageData.lesson,
            invalidCatalog,
            LessonPackageLoader.CURRENT_CONTENT_API,
        )

        assertTrue(diagnostics.any { it.code == LessonDiagnosticCode.INVALID_VALUE && it.path.contains("timeMs") })
    }

    private fun loader(
        lessonOverride: String? = null,
        strokeOverride: String? = null,
    ): LessonPackageLoader {
        val values = mapOf(
            "$ROOT/lesson.json" to (lessonOverride ?: lessonJson()),
            "$ROOT/strokes.json" to (strokeOverride ?: strokeJson()),
        )
        return LessonPackageLoader(LessonPackageSource { path -> values[path] })
    }

    private fun lessonJson(): String = File("src/main/assets/$ROOT/lesson.json").readText()

    private fun strokeJson(): String = File("src/main/assets/$ROOT/strokes.json").readText()

    private fun assertFailureHas(result: LessonLoadResult, code: LessonDiagnosticCode) {
        assertTrue("Expected failure but got $result", result is LessonLoadResult.Failure)
        val diagnostics = (result as LessonLoadResult.Failure).diagnostics
        assertTrue("Expected $code in $diagnostics", diagnostics.any { it.code == code })
    }

    private companion object {
        const val ROOT = "lessons/cute-cat"
    }
}
