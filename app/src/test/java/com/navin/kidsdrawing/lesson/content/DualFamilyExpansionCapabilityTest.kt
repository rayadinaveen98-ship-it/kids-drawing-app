package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.ColoringMode
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DualFamilyExpansionCapabilityTest {
    private val assetRoot = File("src/main/assets")
    private val source = FileAssetCatalogSource(assetRoot)

    @Test
    fun v25AddsExactlyTwoIntentionalFourAgeFamiliesWithoutCapabilityDrift() {
        val snapshot = LessonCatalog(source).load()
        assertTrue("Catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())
        assertEquals("Content V2.5 release catalog must contain exactly 36 lessons.", 36, snapshot.entries.size)

        val expected = expectedLessons()
        val expectedIds = expected.map { it.id }.toSet()
        val actualExpansionEntries = snapshot.entries.filter { it.identity.lessonId in expectedIds }
        assertEquals(expectedIds, actualExpansionEntries.map { it.identity.lessonId }.toSet())
        assertEquals(8, actualExpansionEntries.size)

        expected.forEach { expectation ->
            val entry = snapshot.byLessonId(expectation.id).single()
            assertEquals(setOf(expectation.ageBand), entry.ageBands)
            assertEquals(expectation.difficulty, entry.difficulty)
            assertEquals(expectation.modes.toSet(), entry.supportedModes)
            assertEquals(expectation.categories, entry.categoryIds)
            assertEquals(expectation.skills, entry.skillIds)
            assertEquals(setOf(expectation.journeyId), entry.journeyIds)

            val packageData = assertNotNullAndReturn(snapshot.runtimePackage(entry.identity))
            val lesson = packageData.lesson
            assertEquals(LessonStatus.RELEASE, lesson.status)
            assertEquals(1, lesson.revision)
            assertEquals(expectation.minimumContentApi, lesson.minimumContentApi)
            assertEquals(expectation.modes, lesson.supportedModes)
            assertEquals(expectation.stepCount, lesson.drawing.steps.size)
            assertTrue("V2.5 lessons must not create cross-age prerequisites.", lesson.metadata.prerequisiteLessonIds.isEmpty())
            assertTrue("Runtime audio remains unsupported in V2.5.", lesson.assets.audio.isEmpty())
            assertTrue("Reserved toolPreset must remain unused in V2.5.", lesson.drawing.steps.all { it.childTurn.toolPreset == null })
            assertTrue("Reserved playAsGroup must remain unused in V2.5.", lesson.drawing.steps.all { !it.teacher.playAsGroup })
            assertTrue(
                "Suggested colors must never be enforced in V2.5.",
                lesson.coloring?.steps.orEmpty().all { !it.enforceSuggestedColors },
            )
            assertTrue(
                "Capability validation failed for ${expectation.id}: ${LessonCapabilityValidator.validate(packageData)}",
                LessonCapabilityValidator.validate(packageData).isEmpty(),
            )

            lesson.drawing.steps.forEach { step ->
                assertEquals(
                    "Unexpected Help ladder for ${expectation.id}/${step.id}",
                    expectation.helpLevels,
                    step.help.map { it.level }.toSet(),
                )
            }

            val traceExpected = TeachingMode.TRACE_AND_LEARN in expectation.modes
            val traceHelpByStep = lesson.drawing.steps.map { step -> step.help.any { it.kind == HelpKind.TRACE_PATH } }
            assertEquals(
                "Trace help readiness must match advertised mode for ${expectation.id}",
                List(lesson.drawing.steps.size) { traceExpected },
                traceHelpByStep,
            )
            assertEquals(expectation.coloringMode, lesson.coloring?.takeIf { it.enabled }?.defaultMode)

            when (expectation.coloring) {
                CatalogColoringCapability.GUIDED_PREPARED -> {
                    assertEquals(2, expectation.minimumContentApi)
                    assertTrue("Prepared coloring asset is required for ${expectation.id}", !lesson.assets.coloringRegions.isNullOrBlank())
                    assertTrue("Prepared coloring geometry must be loaded for ${expectation.id}", packageData.coloringRegionCatalog?.regions.orEmpty().isNotEmpty())
                    assertTrue("Prepared coloring steps need explicit regions for ${expectation.id}", lesson.coloring?.steps.orEmpty().all { it.regionIds.isNotEmpty() })
                }
                CatalogColoringCapability.FREEHAND -> {
                    val steps = lesson.coloring?.steps.orEmpty()
                    assertEquals("Freehand coloring must be one unambiguous regionless step for ${expectation.id}", 1, steps.size)
                    assertTrue(steps.single().regionIds.isEmpty())
                    assertTrue(lesson.assets.coloringRegions.isNullOrBlank())
                }
                CatalogColoringCapability.NONE -> {
                    assertTrue(lesson.coloring == null || !lesson.coloring.enabled)
                    assertTrue(lesson.assets.coloringRegions.isNullOrBlank())
                }
                CatalogColoringCapability.PREPARED -> error("V2.5 does not declare PREPARED-only lessons")
            }

            assertTrue("Preview is missing for ${expectation.id}", File(assetRoot, lesson.assets.preview).isFile)
            assertTrue("Thumbnail is missing for ${expectation.id}", File(assetRoot, lesson.assets.thumbnail).isFile)
            lesson.assets.strings.values.forEach { stringPath ->
                assertTrue("Strings file is missing for ${expectation.id}: $stringPath", File(assetRoot, stringPath).isFile)
            }
        }

        val projected = projectedExpansion(snapshot, expectedIds)
        expected.forEach { expectation ->
            val entry = checkNotNull(projected[expectation.id])
            assertEquals(expectation.modes, entry.supportedModes)
            assertEquals(expectation.modes, entry.capabilitySummary.teachingModes)
            assertEquals(true, entry.capabilitySummary.helpAvailable)
            assertEquals(TeachingMode.TRACE_AND_LEARN in expectation.modes, entry.capabilitySummary.traceReady)
            assertEquals(expectation.coloring, entry.capabilitySummary.coloring)
            assertEquals(CatalogVoiceAudioCapability.NOT_SUPPORTED, entry.capabilitySummary.voiceAudio)
        }
    }

    @Test
    fun v25FamiliesUseFrozenTaxonomyAndHaveMateriallyDistinctAuthoredAssetsPerAge() {
        val snapshot = LessonCatalog(source).load()
        assertTrue("Catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())
        val expected = expectedLessons()
        val expectedIds = expected.map { it.id }.toSet()
        val projected = projectedExpansion(snapshot, expectedIds)

        expected.forEach { expectation ->
            val entry = snapshot.byLessonId(expectation.id).single()
            val packageData = assertNotNullAndReturn(snapshot.runtimePackage(entry.identity))
            assertTrue(packageData.lesson.metadata.prerequisiteLessonIds.isEmpty())
            assertTrue(packageData.lesson.assets.audio.isEmpty())

            val projectedEntry = checkNotNull(projected[expectation.id])
            assertTrue(projectedEntry.collectionIds.isEmpty())
            assertTrue(projectedEntry.contentFamilyId == null)
        }
        assertTrue("Frozen taxonomy registry itself must remain valid.", CatalogTaxonomyV2.registry.diagnostics.isEmpty())

        listOf("sea-turtle-", "robot-").forEach { prefix ->
            val family = expected.filter { it.id.startsWith(prefix) }
            assertEquals(4, family.size)
            assertEquals(setOf(1, 2, 3, 4), family.map { it.difficulty }.toSet())
            assertEquals(
                setOf(AgeBand.LITTLE_ARTISTS, AgeBand.CREATIVE_EXPLORERS, AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS),
                family.map { it.ageBand }.toSet(),
            )
            assertEquals(listOf(3, 4, 4, 5), family.sortedBy { it.difficulty }.map { it.stepCount })

            val previews = family.map { expectation ->
                val lesson = assertNotNullAndReturn(snapshot.runtimePackage(snapshot.byLessonId(expectation.id).single().identity)).lesson
                File(assetRoot, lesson.assets.preview).readText()
            }
            val thumbnails = family.map { expectation ->
                val lesson = assertNotNullAndReturn(snapshot.runtimePackage(snapshot.byLessonId(expectation.id).single().identity)).lesson
                File(assetRoot, lesson.assets.thumbnail).readText()
            }
            assertEquals("Each age variant must own distinct preview geometry for $prefix", 4, previews.distinct().size)
            assertEquals("Each age variant must own distinct thumbnail geometry for $prefix", 4, thumbnails.distinct().size)
        }
    }

    private fun projectedExpansion(snapshot: LessonCatalogSnapshot, expectedIds: Set<String>): Map<String, CatalogIndexV2Entry> {
        val projection = CatalogIndexV2Projector.project(snapshot)
        assertTrue("Projection failed: $projection", projection is CatalogIndexV2ProjectionResult.Success)
        val projected = (projection as CatalogIndexV2ProjectionResult.Success).index.entries
            .filter { it.lessonId in expectedIds }
            .associateBy { it.lessonId }
        assertEquals(expectedIds, projected.keys)
        return projected
    }

    private fun expectedLessons(): List<ExpectedLesson> = listOf(
        ExpectedLesson(
            id = "sea-turtle-big-shell",
            ageBand = AgeBand.LITTLE_ARTISTS,
            difficulty = 1,
            minimumContentApi = 2,
            stepCount = 3,
            categories = setOf("animals", "nature"),
            skills = setOf("shape.ellipse", "shape.combine", "line.curve", "placement.relative"),
            journeyId = "journey.animal_artist",
            modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.TRACE_AND_LEARN),
            helpLevels = setOf(1, 2, 4),
            coloring = CatalogColoringCapability.GUIDED_PREPARED,
            coloringMode = ColoringMode.GUIDED,
        ),
        ExpectedLesson(
            id = "sea-turtle-pattern-swim",
            ageBand = AgeBand.CREATIVE_EXPLORERS,
            difficulty = 2,
            minimumContentApi = 1,
            stepCount = 4,
            categories = setOf("animals", "nature"),
            skills = setOf("shape.combine", "line.curve", "pattern", "creativity.variation", "placement.relative"),
            journeyId = "journey.animal_artist",
            modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW, TeachingMode.TRACE_AND_LEARN),
            helpLevels = setOf(1, 2, 3, 4),
            coloring = CatalogColoringCapability.FREEHAND,
            coloringMode = ColoringMode.SELF,
        ),
        ExpectedLesson(
            id = "sea-turtle-form-study",
            ageBand = AgeBand.GROWING_ARTISTS,
            difficulty = 3,
            minimumContentApi = 1,
            stepCount = 4,
            categories = setOf("animals", "nature"),
            skills = setOf("proportion.basic", "overlap.basic", "scale.relative", "contour.simple", "line.control"),
            journeyId = "journey.animal_artist",
            modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW),
            helpLevels = setOf(1, 2, 3),
            coloring = CatalogColoringCapability.NONE,
            coloringMode = null,
        ),
        ExpectedLesson(
            id = "sea-turtle-observation-glide",
            ageBand = AgeBand.YOUNG_ARTISTS,
            difficulty = 4,
            minimumContentApi = 1,
            stepCount = 5,
            categories = setOf("animals", "nature"),
            skills = setOf("observation", "contour.refinement", "proportion", "detail.texture", "overlap.basic"),
            journeyId = "journey.animal_artist",
            modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW),
            helpLevels = setOf(1, 2, 3),
            coloring = CatalogColoringCapability.NONE,
            coloringMode = null,
        ),
        ExpectedLesson(
            id = "robot-big-shapes",
            ageBand = AgeBand.LITTLE_ARTISTS,
            difficulty = 1,
            minimumContentApi = 2,
            stepCount = 3,
            categories = setOf("characters", "design"),
            skills = setOf("shape.rectangle", "shape.circle", "shape.combine", "placement.relative"),
            journeyId = "journey.character_creator",
            modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.TRACE_AND_LEARN),
            helpLevels = setOf(1, 2, 4),
            coloring = CatalogColoringCapability.GUIDED_PREPARED,
            coloringMode = ColoringMode.GUIDED,
        ),
        ExpectedLesson(
            id = "robot-design-lab",
            ageBand = AgeBand.CREATIVE_EXPLORERS,
            difficulty = 2,
            minimumContentApi = 1,
            stepCount = 4,
            categories = setOf("characters", "design"),
            skills = setOf("shape.combine", "design.silhouette", "design.variation", "creativity.variation", "expression.face"),
            journeyId = "journey.character_creator",
            modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW, TeachingMode.TRACE_AND_LEARN),
            helpLevels = setOf(1, 2, 3, 4),
            coloring = CatalogColoringCapability.FREEHAND,
            coloringMode = ColoringMode.SELF,
        ),
        ExpectedLesson(
            id = "robot-structure-study",
            ageBand = AgeBand.GROWING_ARTISTS,
            difficulty = 3,
            minimumContentApi = 1,
            stepCount = 4,
            categories = setOf("characters", "design"),
            skills = setOf("proportion.basic", "overlap.basic", "line.control", "detail.layering", "design.silhouette"),
            journeyId = "journey.character_creator",
            modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW),
            helpLevels = setOf(1, 2, 3),
            coloring = CatalogColoringCapability.NONE,
            coloringMode = null,
        ),
        ExpectedLesson(
            id = "robot-mechanical-observation",
            ageBand = AgeBand.YOUNG_ARTISTS,
            difficulty = 4,
            minimumContentApi = 1,
            stepCount = 5,
            categories = setOf("characters", "design"),
            skills = setOf("observation", "contour.refinement", "proportion", "detail.texture", "design.silhouette"),
            journeyId = "journey.character_creator",
            modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW),
            helpLevels = setOf(1, 2, 3),
            coloring = CatalogColoringCapability.NONE,
            coloringMode = null,
        ),
    )

    private fun <T : Any> assertNotNullAndReturn(value: T?): T {
        assertNotNull(value)
        return checkNotNull(value)
    }

    private data class ExpectedLesson(
        val id: String,
        val ageBand: AgeBand,
        val difficulty: Int,
        val minimumContentApi: Int,
        val stepCount: Int,
        val categories: Set<String>,
        val skills: Set<String>,
        val journeyId: String,
        val modes: List<TeachingMode>,
        val helpLevels: Set<Int>,
        val coloring: CatalogColoringCapability,
        val coloringMode: ColoringMode?,
    )

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
