package com.navin.kidsdrawing.product.home

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudioDiscoveryPolicyTest {
    @Test
    fun rankingUsesFrozenPriorityAndStableTieBreakRegardlessInputOrder() {
        val profile = profile(
            ageBand = AgeBand.CREATIVE_EXPLORER,
            interests = setOf(ChildInterest.ANIMALS),
            mode = TeachingMode.DRAW_WITH_ME,
        )
        val ageExactNoInterest = recommendation(
            id = "zebra",
            ageFit = LessonAgeFit.EXACT,
            reason = RecommendationReason.AGE_MATCH,
            mode = TeachingMode.DRAW_WITH_ME,
            difficulty = 2,
        )
        val fallbackInterest = recommendation(
            id = "animal-fallback",
            ageFit = LessonAgeFit.FALLBACK,
            reason = RecommendationReason.INTEREST_MATCH,
            mode = TeachingMode.DRAW_WITH_ME,
            difficulty = 2,
        )
        val exactInterestWrongMode = recommendation(
            id = "cat-watch",
            ageFit = LessonAgeFit.EXACT,
            reason = RecommendationReason.INTEREST_MATCH,
            mode = TeachingMode.WATCH_THEN_DRAW,
            difficulty = 2,
        )
        val exactInterestPreferredMode = recommendation(
            id = "cat-draw",
            ageFit = LessonAgeFit.EXACT,
            reason = RecommendationReason.INTEREST_MATCH,
            mode = TeachingMode.DRAW_WITH_ME,
            difficulty = 4,
        )
        val sameScoreA = recommendation(
            id = "alpha",
            ageFit = LessonAgeFit.EXACT,
            reason = RecommendationReason.INTEREST_MATCH,
            mode = TeachingMode.DRAW_WITH_ME,
            difficulty = 2,
        )
        val sameScoreB = recommendation(
            id = "beta",
            ageFit = LessonAgeFit.EXACT,
            reason = RecommendationReason.INTEREST_MATCH,
            mode = TeachingMode.DRAW_WITH_ME,
            difficulty = 2,
        )

        val first = StudioRecommendationPolicy.rank(
            profile,
            listOf(
                fallbackInterest,
                sameScoreB,
                exactInterestWrongMode,
                ageExactNoInterest,
                exactInterestPreferredMode,
                sameScoreA,
            ),
        )
        val reversed = StudioRecommendationPolicy.rank(profile, first.reversed())

        assertEquals(
            listOf("alpha", "beta", "cat-draw", "cat-watch", "zebra", "animal-fallback"),
            first.map { it.lessonId },
        )
        assertEquals(first.map { it.lessonId }, reversed.map { it.lessonId })
    }

    @Test
    fun categoriesAndJourneysComeOnlyFromLessonMetadata() {
        val basics = recommendation(
            id = "basic-cat",
            categoryIds = listOf("animals"),
            journeyIds = listOf("animal_artist"),
        )
        val advanced = recommendation(
            id = "advanced-cat",
            categoryIds = listOf("animals", "characters"),
            journeyIds = listOf("animal_artist"),
            prerequisites = listOf("basic-cat"),
        )
        val rocket = recommendation(
            id = "rocket",
            categoryIds = listOf("space"),
            journeyIds = emptyList(),
        )

        val categories = StudioRecommendationPolicy.categories(listOf(rocket, advanced, basics))
        val journeys = StudioRecommendationPolicy.journeys(
            recommendations = listOf(advanced, rocket, basics),
            activeLessonId = "advanced-cat",
        )

        assertEquals(listOf("animals", "characters", "space"), categories.map { it.categoryId })
        assertEquals(listOf("basic-cat", "advanced-cat"), categories.first().lessons.map { it.lessonId })
        assertEquals(listOf("animal_artist"), journeys.map { it.journeyId })
        assertEquals(listOf("basic-cat", "advanced-cat"), journeys.first().lessons.map { it.lessonId })
        assertEquals("Lesson 2 of 2 in progress", journeys.first().progressLabel)
    }

    @Test
    fun activeColoringAlwaysOutranksDrawingAndFreshWhileNewestDrawingWinsOtherwise() {
        val fresh = recommendation(id = "fresh")
        val oldDrawing = drawingResume(id = "old-drawing", savedAt = 100L)
        val newDrawing = drawingResume(id = "new-drawing", savedAt = 200L)
        val coloring = coloringResume(id = "coloring", savedAt = 50L)

        val withColoring = StudioPrimarySelectionPolicy.select(
            rankedRecommendations = listOf(fresh),
            drawingCandidates = listOf(newDrawing, oldDrawing),
            coloringCandidates = listOf(coloring),
        )
        assertEquals("coloring", withColoring.lessonId)
        assertEquals(coloring, withColoring.coloringResume)
        assertNull(withColoring.drawingResume)

        val drawingOnly = StudioPrimarySelectionPolicy.select(
            rankedRecommendations = listOf(fresh),
            drawingCandidates = listOf(oldDrawing, newDrawing),
            coloringCandidates = emptyList(),
        )
        assertEquals("new-drawing", drawingOnly.lessonId)
        assertEquals(newDrawing, drawingOnly.drawingResume)

        val freshOnly = StudioPrimarySelectionPolicy.select(
            rankedRecommendations = listOf(fresh),
            drawingCandidates = emptyList(),
            coloringCandidates = emptyList(),
        )
        assertEquals("fresh", freshOnly.lessonId)
    }

    private fun profile(
        ageBand: AgeBand,
        interests: Set<ChildInterest>,
        mode: TeachingMode,
    ) = ChildProfile(
        nickname = "Maya",
        ageBand = ageBand,
        teachingMode = mode,
        pace = TeachingPace.NORMAL,
        interests = interests,
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
    )

    private fun recommendation(
        id: String,
        ageFit: LessonAgeFit = LessonAgeFit.EXACT,
        reason: RecommendationReason = RecommendationReason.AGE_MATCH,
        mode: TeachingMode = TeachingMode.DRAW_WITH_ME,
        difficulty: Int = 2,
        categoryIds: List<String> = emptyList(),
        journeyIds: List<String> = emptyList(),
        prerequisites: List<String> = emptyList(),
    ) = LessonRecommendation(
        lessonId = id,
        lessonRevision = 1,
        title = id,
        summary = "summary",
        estimatedMinutes = 8,
        difficulty = difficulty,
        defaultMode = mode,
        defaultPace = TeachingPace.NORMAL,
        ageFit = ageFit,
        reason = reason,
        primarySkillIds = listOf("curves"),
        journeyIds = journeyIds,
        categoryIds = categoryIds,
        prerequisiteLessonIds = prerequisites,
    )

    private fun drawingResume(id: String, savedAt: Long) = ResumeLessonCandidate(
        sessionId = "$id-session",
        lessonId = id,
        lessonRevision = 1,
        childDocumentId = "$id-document",
        phase = LessonSnapshotPhase.AWAITING_CHILD,
        mode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        currentStepIndex = 0,
        currentStepId = "step",
        totalSteps = 3,
        savedAtEpochMillis = savedAt,
    )

    private fun coloringResume(id: String, savedAt: Long) = ColoringResumeCandidate(
        sessionId = "$id-coloring",
        lessonId = id,
        lessonRevision = 1,
        childDocumentId = "$id-document",
        savedAtEpochMillis = savedAt,
    )
}
