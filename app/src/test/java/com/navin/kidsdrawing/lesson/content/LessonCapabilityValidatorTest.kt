package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.ColoringMode
import com.navin.kidsdrawing.lesson.model.HelpEntry
import com.navin.kidsdrawing.lesson.model.HelpKind
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonCapabilityValidatorTest {
    @Test
    fun bundledCuteCatIsCapabilityComplete() {
        assertTrue(LessonCapabilityValidator.validate(cuteCat()).isEmpty())
    }

    @Test
    fun authoredSignalCompletionIsRejectedUntilRuntimeImplementsIt() {
        val packageData = cuteCat()
        val first = packageData.lesson.drawing.steps.first()
        val lesson = packageData.lesson.copy(
            drawing = packageData.lesson.drawing.copy(
                steps = listOf(
                    first.copy(
                        childTurn = first.childTurn.copy(
                            completionPolicy = ChildCompletionPolicy.AUTHORED_SIGNAL,
                        ),
                    ),
                ) + packageData.lesson.drawing.steps.drop(1),
            ),
        )

        val diagnostics = LessonCapabilityValidator.validate(lesson, packageData.strokeCatalog)

        assertDiagnostic(
            diagnostics,
            LessonCapabilityDiagnosticCode.UNSUPPORTED_RUNTIME_CAPABILITY,
            "drawing.steps[0].childTurn.completionPolicy",
        )
    }

    @Test
    fun gentleHintWithoutAuthoredTextIsRejected() {
        val packageData = cuteCat()
        val first = packageData.lesson.drawing.steps.first()
        val brokenHelp = first.help.first().copy(
            kind = HelpKind.GENTLE_HINT,
            narrationKey = null,
            guideRefs = emptyList(),
        )
        val lesson = withFirstStepHelp(packageData, listOf(brokenHelp))

        val diagnostics = LessonCapabilityValidator.validate(lesson, packageData.strokeCatalog)

        assertDiagnostic(
            diagnostics,
            LessonCapabilityDiagnosticCode.INCOMPLETE_HELP_OUTPUT,
            "drawing.steps[0].help[0]",
        )
    }

    @Test
    fun visualGuideWithoutGeometryIsRejected() {
        assertVisualHelpWithoutGeometryRejected(HelpKind.VISUAL_GUIDE)
    }

    @Test
    fun directionAnchorsWithoutGeometryAreRejected() {
        assertVisualHelpWithoutGeometryRejected(HelpKind.DIRECTION_ANCHORS)
    }

    @Test
    fun tracePathHelpWithoutGeometryIsRejected() {
        assertVisualHelpWithoutGeometryRejected(HelpKind.TRACE_PATH)
    }

    @Test
    fun assistedSuccessWithoutAnyAuthoredOutputIsRejected() {
        val packageData = cuteCat()
        val lesson = withFirstStepHelp(
            packageData,
            listOf(
                HelpEntry(
                    level = 1,
                    kind = HelpKind.ASSISTED_SUCCESS,
                    narrationKey = null,
                    guideRefs = emptyList(),
                ),
            ),
        )

        val diagnostics = LessonCapabilityValidator.validate(lesson, packageData.strokeCatalog)

        assertDiagnostic(
            diagnostics,
            LessonCapabilityDiagnosticCode.INCOMPLETE_HELP_OUTPUT,
            "drawing.steps[0].help[0]",
        )
    }

    @Test
    fun enforcedSuggestedColorsAreRejectedUntilRuntimeSupportsThem() {
        val packageData = cuteCat()
        val coloring = requireNotNull(packageData.lesson.coloring)
        val lesson = packageData.lesson.copy(
            coloring = coloring.copy(
                steps = listOf(
                    coloring.steps.single().copy(
                        suggestedColorRoles = listOf("warm"),
                        enforceSuggestedColors = true,
                    ),
                ),
            ),
        )

        val diagnostics = LessonCapabilityValidator.validate(lesson, packageData.strokeCatalog)

        assertDiagnostic(
            diagnostics,
            LessonCapabilityDiagnosticCode.UNSUPPORTED_RUNTIME_CAPABILITY,
            "coloring.steps[0].enforceSuggestedColors",
        )
    }

    @Test
    fun multipleRegionlessGuidedColoringStepsAreRejectedAsAmbiguous() {
        val packageData = cuteCat()
        val coloring = requireNotNull(packageData.lesson.coloring)
        val first = coloring.steps.single()
        val lesson = packageData.lesson.copy(
            coloring = coloring.copy(
                defaultMode = ColoringMode.GUIDED,
                steps = listOf(
                    first.copy(id = "guided_one", regionIds = emptyList()),
                    first.copy(id = "guided_two", regionIds = emptyList()),
                ),
            ),
        )

        val diagnostics = LessonCapabilityValidator.validate(lesson, packageData.strokeCatalog)

        assertDiagnostic(
            diagnostics,
            LessonCapabilityDiagnosticCode.AMBIGUOUS_GUIDED_COLORING,
            "coloring.steps",
        )
    }

    @Test
    fun intentionalOpenAuthorshipTraceStepMayRemainGeometryFree() {
        val packageData = cuteCat()
        val first = packageData.lesson.drawing.steps.first()
        val openStep = first.copy(
            childTurn = first.childTurn.copy(
                completionPolicy = ChildCompletionPolicy.MANUAL_DONE,
                allowSkip = true,
                expectedStrokeRefs = emptyList(),
            ),
            help = first.help.filterNot { it.kind == HelpKind.TRACE_PATH },
        )
        val lesson = packageData.lesson.copy(
            drawing = packageData.lesson.drawing.copy(
                steps = listOf(openStep) + packageData.lesson.drawing.steps.drop(1),
            ),
        )

        val diagnostics = LessonCapabilityValidator.validate(lesson, packageData.strokeCatalog)

        assertTrue(
            diagnostics.none {
                it.code == LessonCapabilityDiagnosticCode.INCOMPLETE_TRACE_SUPPORT &&
                    it.path == "drawing.steps[0]"
            },
        )
    }

    private fun assertVisualHelpWithoutGeometryRejected(kind: HelpKind) {
        val packageData = cuteCat()
        val lesson = withFirstStepHelp(
            packageData,
            listOf(
                HelpEntry(
                    level = 1,
                    kind = kind,
                    narrationKey = "lesson.test.help",
                    guideRefs = emptyList(),
                ),
            ),
        )

        val diagnostics = LessonCapabilityValidator.validate(lesson, packageData.strokeCatalog)

        assertDiagnostic(
            diagnostics,
            LessonCapabilityDiagnosticCode.INCOMPLETE_HELP_OUTPUT,
            "drawing.steps[0].help[0].guideRefs",
        )
    }

    private fun withFirstStepHelp(
        packageData: com.navin.kidsdrawing.lesson.model.LessonRuntimePackage,
        help: List<HelpEntry>,
    ): com.navin.kidsdrawing.lesson.model.LessonSource {
        val first = packageData.lesson.drawing.steps.first()
        return packageData.lesson.copy(
            drawing = packageData.lesson.drawing.copy(
                steps = listOf(first.copy(help = help)) + packageData.lesson.drawing.steps.drop(1),
            ),
        )
    }

    private fun cuteCat(): com.navin.kidsdrawing.lesson.model.LessonRuntimePackage {
        val root = "lessons/cute-cat"
        val assets = File("src/main/assets")
        val source = LessonPackageSource { path -> File(assets, path).takeIf(File::isFile)?.readText() }
        val result = LessonPackageLoader(source).load(root)
        assertTrue("Expected bundled Cute Cat to load, got $result", result is LessonLoadResult.Success)
        return (result as LessonLoadResult.Success).packageData
    }

    private fun assertDiagnostic(
        diagnostics: List<LessonCapabilityDiagnostic>,
        code: LessonCapabilityDiagnosticCode,
        path: String,
    ) {
        val matching = diagnostics.filter { it.code == code && it.path == path }
        assertEquals("Expected one $code at $path, got $diagnostics", 1, matching.size)
    }
}
