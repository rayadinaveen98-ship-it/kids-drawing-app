package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.AgeBand as ContentAgeBand
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
import com.navin.kidsdrawing.lesson.session.LessonFinishReason
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.product.profile.AgeBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductLessonPresentationPolicyTest {
    @Test
    fun child_turn_exposes_only_valid_child_controls_and_face_skip() {
        val state = LessonSessionState.AwaitingChild(context(stepIndex = 2, stepId = "face"))

        val result = ProductLessonPresentationPolicy.from(
            state = state,
            packageData = packageData(faceExpectedStrokeRefs = listOf("expected-face")),
            ageBand = AgeBand.CREATIVE_EXPLORER,
        )

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
        val result = ProductLessonPresentationPolicy.from(
            state,
            packageData(headExpectedStrokeRefs = listOf("expected-head")),
            AgeBand.CREATIVE_EXPLORER,
        )
        assertFalse(result.showSkipStep)
    }

    @Test
    fun any_stroke_step_hides_manual_done_because_engine_completes_from_stroke_signal() {
        val packageData = packageData(
            headCompletionPolicy = ChildCompletionPolicy.ANY_STROKE,
            headExpectedStrokeRefs = listOf("expected-head"),
        )
        val state = LessonSessionState.AwaitingChild(context(stepIndex = 0, stepId = "head"))

        val result = ProductLessonPresentationPolicy.from(state, packageData, AgeBand.CREATIVE_EXPLORER)

        assertFalse(result.showDone)
        assertTrue(result.showReplay)
        assertTrue(result.showHelp)
    }

    @Test
    fun help_state_is_semantic_and_keeps_child_in_control() {
        val state = LessonSessionState.HelpActive(
            context(stepIndex = 0, stepId = "head").copy(helpLevel = 4),
        )

        val result = ProductLessonPresentationPolicy.from(
            state,
            packageData(headExpectedStrokeRefs = listOf("expected-head")),
            AgeBand.CREATIVE_EXPLORER,
        )

        assertEquals(CompanionSemanticState.HELPING, result.companionState)
        assertTrue(result.showReduceHelp)
        assertTrue(result.showDismissHelp)
        assertTrue(result.showDone)
        assertTrue(result.instruction.contains("guide", ignoreCase = true) || result.instruction.contains("tracing", ignoreCase = true))
        assertFalse(result.showHelp)
    }

    @Test
    fun teacher_demonstration_does_not_present_child_completion_controls() {
        val state = LessonSessionState.TeacherDemonstrating(
            context = context(stepIndex = 1, stepId = "ears"),
            requestId = "request-1",
        )

        val result = ProductLessonPresentationPolicy.from(state, packageData(), AgeBand.GROWING_ARTIST)

        assertEquals(CompanionSemanticState.DEMONSTRATING, result.companionState)
        assertTrue(result.showPause)
        assertFalse(result.showDone)
        assertFalse(result.showHelp)
        assertFalse(result.showReplay)
    }

    @Test
    fun post_drawing_choice_becomes_calm_artwork_celebration_without_scoring() {
        val state = LessonSessionState.AwaitingPostDrawingChoice(
            context(stepIndex = 3, stepId = "body_tail"),
        )
        val result = ProductLessonPresentationPolicy.from(state, packageData(), AgeBand.YOUNG_ARTIST)

        assertEquals(CompanionSemanticState.CELEBRATING_ARTWORK, result.companionState)
        assertEquals(1f, result.progress)
        assertTrue(result.showPostDrawingChoices)
        assertNotNull(result.reflectionPrompt)
        assertContainsNoScoringLanguage(result.instruction)
        assertContainsNoScoringLanguage(result.reflectionPrompt.orEmpty())
    }

    @Test
    fun little_and_young_child_turns_use_materially_different_age_appropriate_tone_without_changing_controls() {
        val state = LessonSessionState.AwaitingChild(
            context(stepIndex = 0, stepId = "head", mode = TeachingMode.DRAW_WITH_ME),
        )
        val data = packageData(headExpectedStrokeRefs = listOf("expected-head"))

        val little = ProductLessonPresentationPolicy.from(state, data, AgeBand.LITTLE_ARTIST)
        val young = ProductLessonPresentationPolicy.from(state, data, AgeBand.YOUNG_ARTIST)

        assertNotEquals(little.eyebrow, young.eyebrow)
        assertNotEquals(little.instruction, young.instruction)
        assertTrue(little.instruction.length < young.instruction.length)
        assertEquals(little.showReplay, young.showReplay)
        assertEquals(little.showHelp, young.showHelp)
        assertEquals(little.showDone, young.showDone)
        assertEquals(little.showSkipStep, young.showSkipStep)
        assertTrue(young.instruction.contains("structure", ignoreCase = true))
    }

    @Test
    fun open_choice_manual_step_uses_authorship_language_without_copy_or_match_language() {
        val state = LessonSessionState.AwaitingChild(
            context(stepIndex = 2, stepId = "face", mode = TeachingMode.DRAW_WITH_ME),
        )
        val data = packageData(faceExpectedStrokeRefs = emptyList())

        AgeBand.entries.forEach { ageBand ->
            val result = ProductLessonPresentationPolicy.from(state, data, ageBand)
            val combined = listOfNotNull(result.instruction, result.secondaryCue).joinToString(" ").lowercase()
            assertTrue(
                combined.contains("choice") ||
                    combined.contains("choose") ||
                    combined.contains("invent") ||
                    combined.contains("details") ||
                    combined.contains("your way"),
            )
            assertFalse(combined.contains("copy"))
            assertFalse(combined.contains("match"))
            assertTrue(result.showDone)
        }
    }

    @Test
    fun required_stroke_manual_step_does_not_use_open_choice_presentation() {
        val state = LessonSessionState.AwaitingChild(
            context(stepIndex = 0, stepId = "head", mode = TeachingMode.DRAW_WITH_ME),
        )
        val data = packageData(headExpectedStrokeRefs = listOf("expected-head"))

        val result = ProductLessonPresentationPolicy.from(state, data, AgeBand.YOUNG_ARTIST)

        assertTrue(result.instruction.contains("structure", ignoreCase = true))
        assertFalse(result.instruction.contains("invent", ignoreCase = true))
        assertFalse(result.instruction.contains("choose", ignoreCase = true))
    }

    @Test
    fun trace_child_turn_normalizes_guide_as_practice_not_score() {
        val state = LessonSessionState.AwaitingChild(
            context(stepIndex = 0, stepId = "head", mode = TeachingMode.TRACE_AND_LEARN),
        )
        val data = packageData(headExpectedStrokeRefs = listOf("expected-head"))

        AgeBand.entries.forEach { ageBand ->
            val result = ProductLessonPresentationPolicy.from(state, data, ageBand)
            assertTrue(result.instruction.contains("guide", ignoreCase = true))
            assertContainsNoFailureLanguage(result.instruction)
            assertContainsNoScoringLanguage(result.instruction)
            result.secondaryCue?.let(::assertContainsNoScoringLanguage)
        }
    }

    @Test
    fun high_help_is_non_punitive_and_age_aware_for_every_age_band() {
        val state = LessonSessionState.HelpActive(
            context(stepIndex = 0, stepId = "head").copy(helpLevel = 4),
        )
        val data = packageData(headExpectedStrokeRefs = listOf("expected-head"))
        val instructions = AgeBand.entries.associateWith { ageBand ->
            ProductLessonPresentationPolicy.from(state, data, ageBand).instruction
        }

        assertEquals(4, instructions.values.toSet().size)
        instructions.values.forEach { instruction ->
            assertContainsNoFailureLanguage(instruction)
            assertContainsNoScoringLanguage(instruction)
            assertTrue(
                instruction.contains("guide", ignoreCase = true) ||
                    instruction.contains("trace", ignoreCase = true) ||
                    instruction.contains("tracing", ignoreCase = true),
            )
        }
    }

    @Test
    fun watch_then_draw_overview_is_distinct_from_per_step_demonstration() {
        val watchContext = context(
            stepIndex = 0,
            stepId = "head",
            mode = TeachingMode.WATCH_THEN_DRAW,
            overviewCompleted = false,
        )
        val overview = ProductLessonPresentationPolicy.from(
            LessonSessionState.OverviewDemonstrating(watchContext),
            packageData(headExpectedStrokeRefs = listOf("expected-head")),
            AgeBand.GROWING_ARTIST,
        )
        val stepDemo = ProductLessonPresentationPolicy.from(
            LessonSessionState.TeacherDemonstrating(
                context = watchContext.copy(overviewCompleted = true),
                requestId = "step-demo",
            ),
            packageData(headExpectedStrokeRefs = listOf("expected-head")),
            AgeBand.GROWING_ARTIST,
        )

        assertNotEquals(overview.eyebrow, stepDemo.eyebrow)
        assertNotEquals(overview.instruction, stepDemo.instruction)
        assertTrue(overview.showSkipOverview)
        assertFalse(stepDemo.showSkipOverview)
    }

    @Test
    fun completion_reflection_is_optional_presentation_only_and_age_specific() {
        val state = LessonSessionState.DrawingComplete(context(stepIndex = 3, stepId = "body_tail"))
        val data = packageData()

        val prompts = AgeBand.entries.map { ageBand ->
            ProductLessonPresentationPolicy.from(state, data, ageBand).reflectionPrompt
        }

        assertTrue(prompts.all { !it.isNullOrBlank() })
        assertEquals(4, prompts.toSet().size)
        prompts.filterNotNull().forEach(::assertContainsNoScoringLanguage)
    }

    @Test
    fun identical_inputs_produce_identical_presentation() {
        val state = LessonSessionState.AwaitingChild(
            context(stepIndex = 0, stepId = "head", mode = TeachingMode.WATCH_THEN_DRAW),
        )
        val data = packageData(headExpectedStrokeRefs = listOf("expected-head"))

        val first = ProductLessonPresentationPolicy.from(state, data, AgeBand.YOUNG_ARTIST)
        val second = ProductLessonPresentationPolicy.from(state, data, AgeBand.YOUNG_ARTIST)

        assertEquals(first, second)
    }

    @Test
    fun every_session_state_maps_deterministically_without_crash() {
        val baseContext = context(stepIndex = 0, stepId = "head")
        val states: List<LessonSessionState?> = listOf(
            null,
            LessonSessionState.Ready,
            LessonSessionState.OverviewDemonstrating(baseContext.copy(overviewCompleted = false)),
            LessonSessionState.PreparingStep(baseContext),
            LessonSessionState.TeacherDemonstrating(baseContext, requestId = "demo"),
            LessonSessionState.AwaitingChild(baseContext),
            LessonSessionState.HelpActive(baseContext.copy(helpLevel = 1)),
            LessonSessionState.CompletingStep(baseContext),
            LessonSessionState.Paused(LessonSessionState.AwaitingChild(baseContext)),
            LessonSessionState.DrawingComplete(baseContext),
            LessonSessionState.AwaitingPostDrawingChoice(baseContext),
            LessonSessionState.HandingOffToColoring(baseContext),
            LessonSessionState.Finished(LessonFinishReason.COMPLETED, baseContext),
            LessonSessionState.RecoverableError(
                context = baseContext,
                recoveryPhase = LessonSnapshotPhase.TEACHER_DEMONSTRATING,
                code = "demo_problem",
            ),
            LessonSessionState.FatalContentError(
                code = "content_problem",
                message = "Fixture content problem",
            ),
        )
        val data = packageData(headExpectedStrokeRefs = listOf("expected-head"))

        states.forEach { state ->
            val first = ProductLessonPresentationPolicy.from(state, data, AgeBand.CREATIVE_EXPLORER)
            val second = ProductLessonPresentationPolicy.from(state, data, AgeBand.CREATIVE_EXPLORER)
            assertEquals(first, second)
            assertTrue(first.eyebrow.isNotBlank())
            assertTrue(first.instruction.isNotBlank())
        }
    }

    private fun context(
        stepIndex: Int,
        stepId: String,
        mode: TeachingMode = TeachingMode.DRAW_WITH_ME,
        overviewCompleted: Boolean = true,
    ) = LessonActiveContext(
        mode = mode,
        pace = TeachingPace.NORMAL,
        currentStepIndex = stepIndex,
        currentStepId = stepId,
        helpLevel = 0,
        overviewCompleted = overviewCompleted,
    )

    private fun packageData(
        headCompletionPolicy: ChildCompletionPolicy = ChildCompletionPolicy.MANUAL_DONE,
        headExpectedStrokeRefs: List<String> = listOf("expected-head"),
        faceExpectedStrokeRefs: List<String> = listOf("expected-face"),
    ): LessonRuntimePackage {
        val steps = listOf(
            step(
                id = "head",
                allowSkip = false,
                completionPolicy = headCompletionPolicy,
                expectedStrokeRefs = headExpectedStrokeRefs,
            ),
            step(
                id = "ears",
                allowSkip = false,
                expectedStrokeRefs = listOf("expected-ears"),
            ),
            step(
                id = "face",
                allowSkip = true,
                expectedStrokeRefs = faceExpectedStrokeRefs,
            ),
            step(
                id = "body_tail",
                allowSkip = false,
                expectedStrokeRefs = listOf("expected-body-tail"),
            ),
        )
        return LessonRuntimePackage(
            packageRoot = "lessons/presentation-fixture",
            lesson = LessonSource(
                schemaVersion = "1.0",
                lessonId = "presentation-fixture",
                revision = 1,
                status = LessonStatus.RELEASE,
                minimumContentApi = 1,
                metadata = LessonMetadata(
                    titleKey = "lesson.fixture.title",
                    summaryKey = "lesson.fixture.summary",
                    ageBands = listOf(ContentAgeBand.CREATIVE_EXPLORERS),
                    difficulty = 2,
                    estimatedMinutes = 8,
                    categoryIds = listOf("objects"),
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
        expectedStrokeRefs: List<String>,
    ) = DrawingStep(
        id = id,
        objectiveSkillIds = listOf("curves"),
        teacher = TeacherDemo(strokeRefs = listOf("stroke-$id")),
        childTurn = ChildTurn(
            completionPolicy = completionPolicy,
            allowReplay = true,
            allowSkip = allowSkip,
            expectedStrokeRefs = expectedStrokeRefs,
        ),
        help = listOf(
            HelpEntry(level = 1, kind = HelpKind.GENTLE_HINT),
            HelpEntry(level = 4, kind = HelpKind.TRACE_PATH),
        ),
    )

    private fun assertContainsNoFailureLanguage(value: String) {
        val text = value.lowercase()
        listOf("wrong", "failed", "failure", "bad at", "mistake").forEach { forbidden ->
            assertFalse("Unexpected punitive language '$forbidden' in: $value", text.contains(forbidden))
        }
    }

    private fun assertContainsNoScoringLanguage(value: String) {
        val text = value.lowercase()
        listOf("score", "points", "stars", "xp", "rank", "grade", "accuracy").forEach { forbidden ->
            assertFalse("Unexpected scoring language '$forbidden' in: $value", text.contains(forbidden))
        }
    }
}
