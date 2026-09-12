package com.navin.kidsdrawing.product.home

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.AgeBand as LessonAgeBand
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.ChildTurn
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.LessonAssets
import com.navin.kidsdrawing.lesson.model.LessonCanvas
import com.navin.kidsdrawing.lesson.model.LessonDrawing
import com.navin.kidsdrawing.lesson.model.LessonMetadata
import com.navin.kidsdrawing.lesson.model.LessonSource
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeacherDemo
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudioRecommendationPolicyTest {
    @Test
    fun animals_interest_wins_reason_and_profile_mode_pace_are_preserved() {
        val profile = profile(
            ageBand = AgeBand.CREATIVE_EXPLORER,
            interests = setOf(ChildInterest.ANIMALS),
            mode = TeachingMode.WATCH_THEN_DRAW,
            pace = TeachingPace.SLOW,
        )

        val result = StudioRecommendationPolicy.recommend(
            profile = profile,
            lesson = lesson(),
            title = "Cute Cat",
            summary = "Draw a friendly cat.",
        )

        assertEquals(RecommendationReason.INTEREST_MATCH, result.reason)
        assertEquals(LessonAgeFit.EXACT, result.ageFit)
        assertEquals(TeachingMode.WATCH_THEN_DRAW, result.defaultMode)
        assertEquals(TeachingPace.SLOW, result.defaultPace)
    }

    @Test
    fun unsupported_preferred_mode_falls_back_to_first_authored_mode() {
        val result = StudioRecommendationPolicy.recommend(
            profile = profile(mode = TeachingMode.TRACE_AND_LEARN),
            lesson = lesson(supportedModes = listOf(TeachingMode.DRAW_WITH_ME)),
            title = "Cute Cat",
            summary = "Draw a friendly cat.",
        )

        assertEquals(TeachingMode.DRAW_WITH_ME, result.defaultMode)
    }

    @Test
    fun outside_authored_age_without_interest_match_becomes_starter_pick() {
        val result = StudioRecommendationPolicy.recommend(
            profile = profile(
                ageBand = AgeBand.YOUNG_ARTIST,
                interests = setOf(ChildInterest.SPACE),
            ),
            lesson = lesson(),
            title = "Cute Cat",
            summary = "Draw a friendly cat.",
        )

        assertEquals(LessonAgeFit.FALLBACK, result.ageFit)
        assertEquals(RecommendationReason.STARTER_PICK, result.reason)
    }

    @Test
    fun age_presentation_reduces_simultaneous_detail_for_younger_children() {
        val little = StudioRecommendationPolicy.presentationFor(AgeBand.LITTLE_ARTIST)
        val young = StudioRecommendationPolicy.presentationFor(AgeBand.YOUNG_ARTIST)

        assertFalse(little.showDifficulty)
        assertFalse(little.showSkills)
        assertFalse(little.twoColumnSecondaryCards)
        assertTrue(young.showDifficulty)
        assertTrue(young.showSkills)
        assertTrue(young.twoColumnSecondaryCards)
    }

    @Test
    fun resume_candidate_uses_semantic_snapshot_and_rejects_terminal_phases() {
        val authored = lesson()
        val active = snapshot(phase = LessonSnapshotPhase.AWAITING_CHILD, currentStepIndex = 1, currentStepId = "ears")
        val ready = snapshot(phase = LessonSnapshotPhase.READY, mode = null, pace = null)
        val finished = snapshot(phase = LessonSnapshotPhase.FINISHED)

        val candidate = StudioRecommendationPolicy.resumeCandidate(active, authored)

        assertEquals("lesson-lab-cute-cat-session", candidate?.sessionId)
        assertEquals(1, candidate?.currentStepIndex)
        assertEquals(4, candidate?.totalSteps)
        assertEquals("Step 2 of 4 · continue where you left off", candidate?.progressLabel)
        assertNull(StudioRecommendationPolicy.resumeCandidate(ready, authored))
        assertNull(StudioRecommendationPolicy.resumeCandidate(finished, authored))
    }

    private fun profile(
        ageBand: AgeBand = AgeBand.CREATIVE_EXPLORER,
        interests: Set<ChildInterest> = setOf(ChildInterest.ANIMALS),
        mode: TeachingMode = TeachingMode.DRAW_WITH_ME,
        pace: TeachingPace = TeachingPace.NORMAL,
    ) = ChildProfile(
        nickname = "Maya",
        ageBand = ageBand,
        teachingMode = mode,
        pace = pace,
        interests = interests,
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
    )

    private fun lesson(
        supportedModes: List<TeachingMode> = TeachingMode.entries,
    ): LessonSource = LessonSource(
        schemaVersion = "1.0",
        lessonId = "cute-cat",
        revision = 1,
        status = LessonStatus.RELEASE,
        minimumContentApi = 1,
        metadata = LessonMetadata(
            titleKey = "lesson.cute_cat.title",
            summaryKey = "lesson.cute_cat.summary",
            ageBands = listOf(LessonAgeBand.CREATIVE_EXPLORERS, LessonAgeBand.GROWING_ARTISTS),
            difficulty = 1,
            estimatedMinutes = 8,
            categoryIds = listOf("animals", "pets"),
            skillIds = listOf("curves", "shape_construction", "simple_details"),
            journeyIds = listOf("animal_artist"),
            tags = listOf("cat", "beginner"),
        ),
        canvas = LessonCanvas(width = 1000, height = 1000),
        supportedModes = supportedModes,
        assets = LessonAssets(
            strokeFile = "strokes.json",
            thumbnail = "thumbnail.svg",
            preview = "preview.svg",
            strings = mapOf("en" to "strings/en.json"),
        ),
        drawing = LessonDrawing(
            steps = listOf("head", "ears", "face", "body_tail").map { id ->
                DrawingStep(
                    id = id,
                    objectiveSkillIds = listOf("curves"),
                    teacher = TeacherDemo(strokeRefs = listOf("stroke")),
                    childTurn = ChildTurn(
                        completionPolicy = ChildCompletionPolicy.MANUAL_DONE,
                        allowReplay = true,
                        allowSkip = false,
                    ),
                )
            },
        ),
    )

    private fun snapshot(
        phase: LessonSnapshotPhase,
        mode: TeachingMode? = TeachingMode.DRAW_WITH_ME,
        pace: TeachingPace? = TeachingPace.NORMAL,
        currentStepIndex: Int? = 0,
        currentStepId: String? = "head",
    ) = LessonSessionSnapshot(
        sessionId = "lesson-lab-cute-cat-session",
        lessonId = "cute-cat",
        lessonRevision = 1,
        childDocumentId = "lesson-lab-cute-cat-document",
        mode = mode,
        pace = pace,
        phase = phase,
        currentStepIndex = currentStepIndex,
        currentStepId = currentStepId,
        savedAtEpochMillis = 1234L,
    )
}
