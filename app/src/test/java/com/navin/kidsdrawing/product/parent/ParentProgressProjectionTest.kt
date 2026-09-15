package com.navin.kidsdrawing.product.parent

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkCardModel
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkRecord
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkSource
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.gallery.domain.GalleryListResult
import com.navin.kidsdrawing.gallery.domain.GalleryPreviewStatus
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.adaptive.AdaptiveLessonIdentity
import com.navin.kidsdrawing.product.adaptive.LocalAdaptiveReadResult
import com.navin.kidsdrawing.product.adaptive.LocalAdaptiveState
import com.navin.kidsdrawing.product.home.ColoringResumeCandidate
import com.navin.kidsdrawing.product.home.LessonAgeFit
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.home.RecommendationReason
import com.navin.kidsdrawing.product.home.ResumeLessonCandidate
import com.navin.kidsdrawing.product.home.StudioHomeModel
import com.navin.kidsdrawing.product.home.StudioJourney
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentProgressProjectionTest {
    @Test
    fun completionTruthProjectsRecentOrderCategoriesSkillsAndJourneyNextStep() {
        val cat = lesson(
            id = "cute-cat",
            title = "Cute Cat",
            categories = listOf("animals"),
            skills = listOf("line.curve"),
            journeys = listOf("animal-artist"),
        )
        val fox = lesson(
            id = "friendly-fox",
            title = "Friendly Fox",
            categories = listOf("animals"),
            skills = listOf("shape.triangle"),
            journeys = listOf("animal-artist"),
            prerequisites = listOf("cute-cat"),
        )
        val home = StudioHomeModel(
            recommendation = fox,
            resumeCandidate = null,
            recommendations = listOf(cat, fox),
            journeys = listOf(
                StudioJourney("animal-artist", "Animal Artist", listOf(cat, fox)),
            ),
        )
        val state = LocalAdaptiveState(
            completedLessons = listOf(AdaptiveLessonIdentity("cute-cat", 1)),
            recentCompletions = listOf(AdaptiveLessonIdentity("cute-cat", 1)),
            skillExposureCounts = mapOf("line.curve" to 1),
        )

        val model = ParentProgressProjection.project(
            home = home,
            adaptiveRead = LocalAdaptiveReadResult.Loaded(state),
            galleryResult = GalleryListResult.Empty,
        )

        assertEquals(1, model.completedLessonCount)
        assertEquals(listOf("Cute Cat"), model.recentCompletions.map { it.title })
        assertEquals(listOf("Animals"), model.exploredCategories)
        assertEquals(listOf("Line Curve"), model.practicedSkills)
        assertEquals(1, model.journeys.single().completedLessons)
        assertEquals("Friendly Fox", model.journeys.single().nextAvailableLessonTitle)
    }

    @Test
    fun helpRequestCountersNeverBecomeParentProgressSignals() {
        val home = StudioHomeModel(
            recommendation = lesson("cute-cat", "Cute Cat"),
            resumeCandidate = null,
            recommendations = listOf(lesson("cute-cat", "Cute Cat")),
        )
        val state = LocalAdaptiveState(
            helpRequestCounts = mapOf(
                "skill:line.curve" to 9,
                "choice:AUTHORED_HELP" to 9,
            ),
        )

        val model = ParentProgressProjection.project(
            home,
            LocalAdaptiveReadResult.Loaded(state),
            GalleryListResult.Empty,
        )

        assertEquals(0, model.completedLessonCount)
        assertTrue(model.practicedSkills.isEmpty())
        assertFalse(model.hasLearningHistory)
    }

    @Test
    fun galleryActivityKeepsRealTimestampAndSourceSeparateFromLessonCompletionTruth() {
        val record = GalleryArtworkRecord(
            entryId = "gallery-1",
            documentId = "document-1",
            title = "My Free Drawing",
            source = GalleryArtworkSource.FREE_DRAW,
            completionKind = GalleryCompletionKind.DRAWING_ONLY,
            completedAtEpochMillis = 123456789L,
            previewStatus = GalleryPreviewStatus.MISSING,
        )
        val model = ParentProgressProjection.project(
            home = StudioHomeModel(recommendation = null, resumeCandidate = null),
            adaptiveRead = LocalAdaptiveReadResult.Missing,
            galleryResult = GalleryListResult.Ready(
                listOf(GalleryArtworkCardModel(record, usablePreviewReference = null)),
            ),
        )

        assertEquals(0, model.completedLessonCount)
        assertEquals(1, model.savedArtworkCount)
        assertEquals(123456789L, model.recentArtwork.single().completedAtEpochMillis)
        assertEquals("Free Draw artwork", model.recentArtwork.single().sourceLabel)
        assertTrue(model.hasLearningHistory)
    }

    @Test
    fun recentCompletionOrderingIsPreservedWithoutInventedClockData() {
        val cat = lesson("cute-cat", "Cute Cat")
        val fox = lesson("friendly-fox", "Friendly Fox")
        val state = LocalAdaptiveState(
            completedLessons = listOf(
                AdaptiveLessonIdentity("cute-cat", 1),
                AdaptiveLessonIdentity("friendly-fox", 1),
            ),
            recentCompletions = listOf(
                AdaptiveLessonIdentity("cute-cat", 1),
                AdaptiveLessonIdentity("friendly-fox", 1),
            ),
        )
        val model = ParentProgressProjection.project(
            StudioHomeModel(
                recommendation = null,
                resumeCandidate = null,
                recommendations = listOf(cat, fox),
            ),
            LocalAdaptiveReadResult.Loaded(state),
            GalleryListResult.Empty,
        )

        assertEquals(listOf("Friendly Fox", "Cute Cat"), model.recentCompletions.map { it.title })
    }

    @Test
    fun corruptAndIncompatibleAdaptiveStateStayUnavailableInsteadOfBecomingFakeZeroHistory() {
        val home = StudioHomeModel(
            recommendation = lesson("cute-cat", "Cute Cat"),
            resumeCandidate = null,
            recommendations = listOf(lesson("cute-cat", "Cute Cat")),
        )
        val corrupt = ParentProgressProjection.project(
            home,
            LocalAdaptiveReadResult.Corrupt("broken", null),
            GalleryListResult.Empty,
        )
        val incompatible = ParentProgressProjection.project(
            home,
            LocalAdaptiveReadResult.Incompatible(99),
            GalleryListResult.Empty,
        )

        assertTrue(corrupt.notices.any { it.contains("could not be read") })
        assertTrue(incompatible.notices.any { it.contains("newer app format") })
    }

    @Test
    fun activeResumeStateIsShownAsInProgressNotCompleted() {
        val cat = lesson("cute-cat", "Cute Cat")
        val drawing = ResumeLessonCandidate(
            sessionId = "session",
            lessonId = "cute-cat",
            lessonRevision = 1,
            childDocumentId = "doc",
            phase = LessonSnapshotPhase.AWAITING_CHILD,
            mode = TeachingMode.DRAW_WITH_ME,
            pace = TeachingPace.NORMAL,
            currentStepIndex = 0,
            currentStepId = "step-1",
            totalSteps = 3,
            savedAtEpochMillis = 500L,
        )
        val model = ParentProgressProjection.project(
            StudioHomeModel(
                recommendation = cat,
                resumeCandidate = drawing,
                recommendations = listOf(cat),
            ),
            LocalAdaptiveReadResult.Missing,
            GalleryListResult.Empty,
        )

        assertEquals(0, model.completedLessonCount)
        assertEquals("Cute Cat", model.inProgress.single().title)
        assertEquals("Drawing lesson in progress", model.inProgress.single().kindLabel)
    }

    @Test
    fun coloringResumeWinsOnlyAsSeparateInProgressFact() {
        val cat = lesson("cute-cat", "Cute Cat")
        val coloring = ColoringResumeCandidate(
            sessionId = "color-session",
            lessonId = "cute-cat",
            lessonRevision = 1,
            childDocumentId = "doc",
            savedAtEpochMillis = 700L,
        )
        val model = ParentProgressProjection.project(
            StudioHomeModel(
                recommendation = cat,
                resumeCandidate = null,
                coloringResumeCandidate = coloring,
                recommendations = listOf(cat),
            ),
            LocalAdaptiveReadResult.Missing,
            GalleryListResult.Empty,
        )

        assertEquals("Coloring in progress", model.inProgress.single().kindLabel)
        assertEquals(700L, model.inProgress.single().savedAtEpochMillis)
        assertNull(model.journeys.firstOrNull()?.nextAvailableLessonTitle)
    }

    private fun lesson(
        id: String,
        title: String,
        categories: List<String> = emptyList(),
        skills: List<String> = emptyList(),
        journeys: List<String> = emptyList(),
        prerequisites: List<String> = emptyList(),
    ) = LessonRecommendation(
        lessonId = id,
        lessonRevision = 1,
        title = title,
        summary = "Summary",
        estimatedMinutes = 5,
        difficulty = 1,
        defaultMode = TeachingMode.DRAW_WITH_ME,
        defaultPace = TeachingPace.NORMAL,
        ageFit = LessonAgeFit.EXACT,
        reason = RecommendationReason.AGE_MATCH,
        primarySkillIds = skills,
        journeyIds = journeys,
        categoryIds = categories,
        prerequisiteLessonIds = prerequisites,
    )
}
