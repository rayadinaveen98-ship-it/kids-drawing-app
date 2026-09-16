package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.AgeBand as LessonAgeBand
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.product.adaptive.AdaptiveFreshRecommendationPolicy
import com.navin.kidsdrawing.product.adaptive.AdaptiveLessonIdentity
import com.navin.kidsdrawing.product.adaptive.AdaptiveRecommendationReason
import com.navin.kidsdrawing.product.adaptive.LocalAdaptiveState
import com.navin.kidsdrawing.product.home.ColoringResumeCandidate
import com.navin.kidsdrawing.product.home.LessonAgeFit
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.home.ResumeLessonCandidate
import com.navin.kidsdrawing.product.home.StudioPrimarySelectionPolicy
import com.navin.kidsdrawing.product.home.StudioRecommendationPolicy
import com.navin.kidsdrawing.product.profile.AgeBand as ProductAgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * P5.8 pre-freeze integration gate over the real bundled release catalog.
 *
 * Focused engine/content tests remain authoritative for their detailed behavior. This gate proves
 * the release catalog, Home projection, local adaptive policy and primary-resume hierarchy still
 * compose correctly across the complete Phase-5 product before the final binary identity is cut.
 */
class P5_8IntegratedReleaseGateTest {
    @Test
    fun productionCatalogHasCompleteCrossAgeTeachingSurface() {
        val snapshot = releaseCatalog()

        assertTrue("Release catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())
        assertEquals(24, snapshot.entries.size)

        LessonAgeBand.entries.forEach { ageBand ->
            assertTrue("No release lessons for $ageBand", snapshot.forAgeBand(ageBand).isNotEmpty())
        }
        TeachingMode.entries.forEach { mode ->
            assertTrue("No release lesson supports $mode", snapshot.supporting(mode).isNotEmpty())
        }

        val releaseIds = snapshot.entries.map { it.identity.lessonId }.toSet()
        snapshot.entries.forEach { entry ->
            val lesson = checkNotNull(snapshot.runtimePackage(entry.identity)).lesson
            assertTrue("${lesson.lessonId} has no drawing steps", lesson.drawing.steps.isNotEmpty())
            assertTrue("${lesson.lessonId} has no supported teaching mode", lesson.supportedModes.isNotEmpty())
            assertFalse(
                "${lesson.lessonId} cannot require itself",
                lesson.lessonId in lesson.metadata.prerequisiteLessonIds,
            )
            assertTrue(
                "${lesson.lessonId} has missing prerequisite(s): ${lesson.metadata.prerequisiteLessonIds}",
                lesson.metadata.prerequisiteLessonIds.all(releaseIds::contains),
            )
        }
    }

    @Test
    fun realCatalogFreshRecommendationsAreDeterministicAgeFitAndPrerequisiteSafe() {
        val snapshot = releaseCatalog()
        ProductAgeBand.entries.forEach { ageBand ->
            val profile = profile(ageBand)
            val baseline = StudioRecommendationPolicy.rank(profile, project(snapshot, profile))
            val first = AdaptiveFreshRecommendationPolicy.rank(profile, baseline, LocalAdaptiveState.empty())
            val second = AdaptiveFreshRecommendationPolicy.rank(profile, baseline, LocalAdaptiveState.empty())

            assertEquals("Baseline must expose all release lessons for $ageBand", 24, baseline.size)
            assertEquals("Adaptive result changed for identical $ageBand input", first, second)
            assertTrue("No fresh recommendation for $ageBand", first.isNotEmpty())
            assertEquals("Fresh primary must use exact-age content when it exists", LessonAgeFit.EXACT, first.first().recommendation.ageFit)
            assertTrue(
                "Empty progression state exposed a prerequisite-locked fresh lesson for $ageBand",
                first.all { it.recommendation.prerequisiteLessonIds.isEmpty() },
            )

            first.forEach { decision ->
                val copy = decision.reasonCopy.lowercase()
                listOf("weak", "failed", "score", "rank", "grade", "talent", "ability").forEach { forbidden ->
                    assertFalse("Adaptive copy contains '$forbidden': $copy", copy.contains(forbidden))
                }
            }

            val categories = StudioRecommendationPolicy.categories(baseline)
            val journeys = StudioRecommendationPolicy.journeys(baseline)
            assertTrue("Category discovery disappeared for $ageBand", categories.isNotEmpty())
            assertTrue("Journey discovery disappeared for $ageBand", journeys.isNotEmpty())
            assertEquals(
                "Adaptive fresh ordering must not mutate the baseline browse catalog",
                24,
                baseline.distinctBy { it.lessonId to it.lessonRevision }.size,
            )
        }
    }

    @Test
    fun realPrerequisiteProgressionUnlocksSafelyAndPreservesJourneyReason() {
        val snapshot = releaseCatalog()
        val lockedEntry = snapshot.entries.firstOrNull { entry ->
            checkNotNull(snapshot.runtimePackage(entry.identity)).lesson.metadata.prerequisiteLessonIds.isNotEmpty()
        }
        assertNotNull("Release catalog needs prerequisite progression for the P5.8 gate", lockedEntry)
        lockedEntry!!

        val lockedLesson = checkNotNull(snapshot.runtimePackage(lockedEntry.identity)).lesson
        val ageBand = lockedLesson.metadata.ageBands.first().toProductAgeBand()
        val profile = profile(ageBand)
        val baseline = StudioRecommendationPolicy.rank(profile, project(snapshot, profile))
        val lockedRecommendation = baseline.single { it.lessonId == lockedLesson.lessonId }

        val before = AdaptiveFreshRecommendationPolicy.rank(profile, baseline, LocalAdaptiveState.empty())
        assertTrue(
            "Prerequisite-locked ${lockedLesson.lessonId} was exposed before completion",
            before.none { it.recommendation.lessonId == lockedLesson.lessonId },
        )

        val completed = lockedRecommendation.prerequisiteLessonIds.map { prerequisiteId ->
            val prerequisite = baseline.single { it.lessonId == prerequisiteId }
            AdaptiveLessonIdentity(prerequisite.lessonId, prerequisite.lessonRevision)
        }
        val state = LocalAdaptiveState(completedLessons = completed)
        val after = AdaptiveFreshRecommendationPolicy.rank(profile, baseline, state)
        val unlocked = after.firstOrNull { it.recommendation.lessonId == lockedLesson.lessonId }

        assertNotNull("${lockedLesson.lessonId} did not unlock after its prerequisites completed", unlocked)
        assertTrue(
            "Adaptive policy returned a lesson with unmet prerequisites",
            after.all { decision ->
                decision.recommendation.prerequisiteLessonIds.all { prerequisiteId ->
                    completed.any { it.lessonId == prerequisiteId }
                }
            },
        )
        if (lockedRecommendation.journeyIds.isNotEmpty()) {
            assertTrue(
                "Unlocked journey lesson should explain continuation",
                unlocked!!.reasons.contains(AdaptiveRecommendationReason.CONTINUE_JOURNEY),
            )
        }
    }

    @Test
    fun realFreshPrimaryStillYieldsToDrawingAndColoringResume() {
        val snapshot = releaseCatalog()
        val profile = profile(ProductAgeBand.GROWING_ARTIST)
        val baseline = StudioRecommendationPolicy.rank(profile, project(snapshot, profile))
        val fresh = AdaptiveFreshRecommendationPolicy.rank(profile, baseline, LocalAdaptiveState.empty())
            .first()
            .recommendation

        val drawing = ResumeLessonCandidate(
            sessionId = "p5-8-drawing-session",
            lessonId = "drawing-resume",
            lessonRevision = 1,
            childDocumentId = "p5-8-drawing-doc",
            phase = LessonSnapshotPhase.AWAITING_CHILD,
            mode = TeachingMode.DRAW_WITH_ME,
            pace = TeachingPace.NORMAL,
            currentStepIndex = 1,
            currentStepId = "step",
            totalSteps = 3,
            savedAtEpochMillis = 20L,
        )
        val coloring = ColoringResumeCandidate(
            sessionId = "p5-8-color-session",
            lessonId = "color-resume",
            lessonRevision = 1,
            childDocumentId = "p5-8-color-doc",
            savedAtEpochMillis = 10L,
        )

        val freshOnly = StudioPrimarySelectionPolicy.select(listOf(fresh), emptyList(), emptyList())
        val drawingOnly = StudioPrimarySelectionPolicy.select(listOf(fresh), listOf(drawing), emptyList())
        val withBoth = StudioPrimarySelectionPolicy.select(listOf(fresh), listOf(drawing), listOf(coloring))

        assertEquals(fresh.lessonId, freshOnly.lessonId)
        assertEquals("drawing-resume", drawingOnly.lessonId)
        assertEquals("color-resume", withBoth.lessonId)
        assertNotNull(drawingOnly.drawingResume)
        assertNotNull(withBoth.coloringResume)
    }

    /**
     * P5.8 is immutable historical release evidence. Later content expansion must not redefine the
     * Phase-5 browse/recommendation cohort, so project today's catalog onto the exact 24 accepted
     * release lesson IDs before exercising the frozen product integration assertions above.
     */
    private fun releaseCatalog(): LessonCatalogSnapshot {
        val current = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
        val phase5Entries = current.entries.filter { it.identity.lessonId in PHASE5_RELEASE_LESSON_IDS }
        check(phase5Entries.map { it.identity.lessonId }.toSet() == PHASE5_RELEASE_LESSON_IDS) {
            "Frozen Phase-5 release cohort is incomplete in current production catalog."
        }
        return LessonCatalogSnapshot(
            entries = phase5Entries,
            diagnostics = current.diagnostics,
            runtimePackages = phase5Entries.associate { entry ->
                entry.identity to checkNotNull(current.runtimePackage(entry.identity))
            },
        )
    }

    private fun project(
        snapshot: LessonCatalogSnapshot,
        profile: ChildProfile,
    ): List<LessonRecommendation> = snapshot.entries.map { entry ->
        val packageData = checkNotNull(snapshot.runtimePackage(entry.identity))
        StudioRecommendationPolicy.recommend(
            profile = profile,
            lesson = packageData.lesson,
            title = entry.title,
            summary = entry.summary,
        )
    }

    private fun profile(ageBand: ProductAgeBand) = ChildProfile(
        nickname = "Maya",
        ageBand = ageBand,
        teachingMode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        interests = setOf(ChildInterest.ANIMALS),
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
    )

    private fun LessonAgeBand.toProductAgeBand(): ProductAgeBand = when (this) {
        LessonAgeBand.LITTLE_ARTISTS -> ProductAgeBand.LITTLE_ARTIST
        LessonAgeBand.CREATIVE_EXPLORERS -> ProductAgeBand.CREATIVE_EXPLORER
        LessonAgeBand.GROWING_ARTISTS -> ProductAgeBand.GROWING_ARTIST
        LessonAgeBand.YOUNG_ARTISTS -> ProductAgeBand.YOUNG_ARTIST
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }

    private companion object {
        val PHASE5_RELEASE_LESSON_IDS = setOf(
            "create-your-character",
            "cute-cat",
            "design-your-spaceship",
            "easy-flower",
            "elephant-from-shapes",
            "face-and-expressions",
            "fox-portrait",
            "friendly-alien",
            "friendly-owl",
            "happy-lines",
            "hot-air-balloon",
            "ice-cream-shop",
            "little-fish",
            "one-point-room",
            "planet-with-rings",
            "rainbow-weather",
            "sailboat-scene",
            "shape-friends",
            "simple-body-and-pose",
            "simple-car",
            "simple-rocket",
            "smiling-sun",
            "snail-garden",
            "tree-through-seasons",
        )
    }
}
