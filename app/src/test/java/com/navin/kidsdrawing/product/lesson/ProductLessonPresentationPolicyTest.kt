package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.AuthoredStroke
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.ChildTurn
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.HelpEntry
import com.navin.kidsdrawing.lesson.model.HelpKind
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
import com.navin.kidsdrawing.lesson.session.LessonActiveContext
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductLessonPresentationPolicyTest {
    @Test
    fun child_turn_exposes_only_valid_child_controls_and_face_skip() {
        val state = LessonSessionState.AwaitingChild(context(stepIndex = 2, stepId = "face"))

        val result = ProductLessonPresentationPolicy.from(state, packageData())

        assertEquals(CompanionSemanticState.WATCHING_CHILD, result.companionState)
        assertEquals("Face", result.stepLabel)
        assertTrue(result.showReplay)
        assertTrue(result.showHelp)
        assertTrue(result.showDone)
        assertTrue(result.showSkipStep)
        assertFalse(result.showRetry)
    }

    @Test
    fun non_skippable_child_step_hides_skip_instead_of_exercising_rejection() {
        val state = LessonSessionState.AwaitingChild(context(stepIndex = 0, stepId = "head"))
        val result = ProductLessonPresentationPolicy.from(state, packageData())
        assertFalse(result.showSkipStep)
    }

    @Test
    fun any_stroke_step_hides_manual_done_because_engine_completes_from_stroke_signal() {
        val packageData = packageData(
            headCompletionPolicy = ChildCompletionPolicy.ANY_STROKE,
        )
        val state = LessonSessionState.AwaitingChild(context(stepIndex = 0, stepId = "head"))

        val result = ProductLessonPresentationPolicy.from(state, packageData)

        assertFalse(result.showDone)
        assertTrue(result.showReplay)
        assertTrue(result.showHelp)
    }

    @Test
    fun help_state_is_semantic_and_keeps_child_in_control() {
        val state = LessonSessionState.HelpActive(
            context(stepIndex = 0, stepId = "head").copy(helpLevel = 4),
        )

        val result = ProductLessonPresentationPolicy.from(state, packageData())

        assertEquals(CompanionSemanticState.HELPING, result.companionState)
        assertTrue(result.showReduceHelp)
        assertTrue(result.showDismissHelp)
        assertTrue(result.showDone)
        assertTrue(result.instruction.contains("Tracing"))
        assertFalse(result.showHelp)
    }

    @Test
    fun teacher_demonstration_does_not_present_child_completion_controls() {
        val state = LessonSessionState.TeacherDemonstrating(
            context = context(stepIndex = 1, stepId = "ears"),
            requestId = "request-1",
        )

        val result = ProductLessonPresentationPolicy.from(state, packageData())

        assertEquals(CompanionSemanticState.DEMONSTRATING, result.companionState)
        assertTrue(result.showPause)
        assertFalse(result.showDone)
        assertFalse(result.showHelp)
        assertFalse(result.showReplay)
    }

    @Test
    fun post_drawing_choice_becomes_calm_artwork_celebration_without_fake_coloring() {
        val state = LessonSessionState.AwaitingPostDrawingChoice(
            context(stepIndex = 3, stepId = "body_tail"),
        )
        val result = ProductLessonPresentationPolicy.from(state, packageData())

        assertEquals(CompanionSemanticState.CELEBRATING_ARTWORK, result.companionState)
        assertEquals(1f, result.progress)
        assertTrue(result.showPostDrawingChoices)
        assertFalse(result.instruction.contains("color", ignoreCase = true))
    }

    private fun context(stepIndex: Int, stepId: String) = LessonActiveContext(
        mode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        currentStepIndex = stepIndex,
        currentStepId = stepId,
        helpLevel = 0,
        overviewCompleted = true,
    )

    private fun packageData(
        headCompletionPolicy: ChildCompletionPolicy = ChildCompletionPolicy.MANUAL_DONE,
    ): LessonRuntimePackage {
        val steps = listOf(
            step("head", allowSkip = false, completionPolicy = headCompletionPolicy),
            step("ears", allowSkip = false),
            step("face", allowSkip = true),
            step("body_tail", allowSkip = false),
        )
        return LessonRuntimePackage(
            packageRoot = "lessons/cute-cat",
            lesson = LessonSource(
                schemaVersion = "1.0",
                lessonId = "cute-cat",
                revision = 1,
                status = LessonStatus.RELEASE,
                minimumContentApi = 1,
                metadata = LessonMetadata(
                    titleKey = "lesson.cute_cat.title",
                    summaryKey = "lesson.cute_cat.summary",
                    ageBands = listOf(AgeBand.CREATIVE_EXPLORERS),
                    difficulty = 1,
                    estimatedMinutes = 8,
                    categoryIds = listOf("animals"),
                    skillIds = listOf("curves"),
                ),
                canvas = LessonCanvas(1000, 1000),
                supportedModes = TeachingMode.entries,
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
                strokes = emptyList<AuthoredStroke>(),
            ),
        )
    }

    private fun step(
        id: String,
        allowSkip: Boolean,
        completionPolicy: ChildCompletionPolicy = ChildCompletionPolicy.MANUAL_DONE,
    ) = DrawingStep(
        id = id,
        objectiveSkillIds = listOf("curves"),
        teacher = TeacherDemo(strokeRefs = listOf("stroke-$id")),
        childTurn = ChildTurn(
            completionPolicy = completionPolicy,
            allowReplay = true,
            allowSkip = allowSkip,
        ),
        help = listOf(
            HelpEntry(level = 1, kind = HelpKind.GENTLE_HINT),
            HelpEntry(level = 4, kind = HelpKind.TRACE_PATH),
        ),
    )
}
