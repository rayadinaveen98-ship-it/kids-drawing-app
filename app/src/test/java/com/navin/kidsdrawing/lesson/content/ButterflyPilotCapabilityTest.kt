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

class ButterflyPilotCapabilityTest {
    private val assetRoot = File("src/main/assets")
    private val source = FileAssetCatalogSource(assetRoot)

    @Test
    fun butterflyPilotHasExactlyOneIntentionalLessonPerAgeBandAndNoCapabilityDrift() {
        val snapshot = LessonCatalog(source).load()
        assertTrue("Catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())
        assertEquals(4, snapshot.entries.count { it.identity.lessonId.startsWith("butterfly-") })

        val expected = listOf(
            ExpectedLesson(
                id = "butterfly-big-shapes",
                ageBand = AgeBand.LITTLE_ARTISTS,
                difficulty = 1,
                minimumContentApi = 2,
                modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.TRACE_AND_LEARN),
                helpLevels = setOf(1, 2, 4),
                coloring = CatalogColoringCapability.GUIDED_PREPARED,
                coloringMode = ColoringMode.GUIDED,
            ),
            ExpectedLesson(
                id = "butterfly-pattern-play",
                ageBand = AgeBand.CREATIVE_EXPLORERS,
                difficulty = 2,
                minimumContentApi = 1,
                modes = listOf(
                    TeachingMode.DRAW_WITH_ME,
                    TeachingMode.WATCH_THEN_DRAW,
                    TeachingMode.TRACE_AND_LEARN,
                ),
                helpLevels = setOf(1, 2, 3, 4),
                coloring = CatalogColoringCapability.FREEHAND,
                coloringMode = ColoringMode.SELF,
            ),
            ExpectedLesson(
                id = "butterfly-symmetry-study",
                ageBand = AgeBand.GROWING_ARTISTS,
                difficulty = 3,
                minimumContentApi = 1,
                modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW),
                helpLevels = setOf(1, 2, 3),
                coloring = CatalogColoringCapability.NONE,
                coloringMode = null,
            ),
            ExpectedLesson(
                id = "butterfly-observation-study",
                ageBand = AgeBand.YOUNG_ARTISTS,
                difficulty = 4,
                minimumContentApi = 1,
                modes = listOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW),
                helpLevels = setOf(1, 2, 3),
                coloring = CatalogColoringCapability.NONE,
                coloringMode = null,
            ),
        )

        expected.forEach { expectation ->
            val entry = snapshot.byLessonId(expectation.id).single()
            assertEquals(setOf(expectation.ageBand), entry.ageBands)
            assertEquals(expectation.difficulty, entry.difficulty)
            assertEquals(expectation.modes.toSet(), entry.supportedModes)
            assertEquals(setOf("animals", "nature"), entry.categoryIds)
            assertEquals(setOf("journey.animal_artist"), entry.journeyIds)

            val packageData = assertNotNullAndReturn(snapshot.runtimePackage(entry.identity))
            assertEquals(LessonStatus.RELEASE, packageData.lesson.status)
            assertEquals(1, packageData.lesson.revision)
            assertEquals(expectation.minimumContentApi, packageData.lesson.minimumContentApi)
            assertEquals(expectation.modes, packageData.lesson.supportedModes)
            assertTrue(
                "Capability validation failed for ${expectation.id}: ${LessonCapabilityValidator.validate(packageData)}",
                LessonCapabilityValidator.validate(packageData).isEmpty(),
            )
            packageData.lesson.drawing.steps.forEach { step ->
                assertEquals(
                    "Unexpected Help ladder for ${expectation.id}/${step.id}",
                    expectation.helpLevels,
                    step.help.map { it.level }.toSet(),
                )
            }

            val hasTraceHelp = packageData.lesson.drawing.steps.any { step ->
                step.help.any { it.kind == HelpKind.TRACE_PATH }
            }
            assertEquals(TeachingMode.TRACE_AND_LEARN in expectation.modes, hasTraceHelp)
            assertEquals(expectation.coloringMode, packageData.lesson.coloring?.takeIf { it.enabled }?.defaultMode)
        }

        val projection = CatalogIndexV2Projector.project(snapshot)
        assertTrue("Projection failed: $projection", projection is CatalogIndexV2ProjectionResult.Success)
        val projected = (projection as CatalogIndexV2ProjectionResult.Success).index.entries
            .filter { it.lessonId.startsWith("butterfly-") }
            .associateBy { it.lessonId }
        assertEquals(expected.map { it.id }.toSet(), projected.keys)

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
    fun butterflyPilotUsesOnlyFrozenExistingTaxonomyAndNoCrossAgePrerequisites() {
        val snapshot = LessonCatalog(source).load()
        val butterflyEntries = snapshot.entries.filter { it.identity.lessonId.startsWith("butterfly-") }
        assertEquals(4, butterflyEntries.size)

        butterflyEntries.forEach { entry ->
            val packageData = assertNotNullAndReturn(snapshot.runtimePackage(entry.identity))
            assertTrue(packageData.lesson.metadata.prerequisiteLessonIds.isEmpty())
            assertTrue(packageData.lesson.assets.audio.isEmpty())
            assertTrue(packageData.lesson.drawing.steps.all { it.childTurn.toolPreset == null })
            assertTrue(packageData.lesson.drawing.steps.all { !it.teacher.playAsGroup })
            assertTrue(packageData.lesson.coloring?.steps.orEmpty().all { !it.enforceSuggestedColors })
        }

        val projection = CatalogIndexV2Projector.project(snapshot)
        assertTrue(projection is CatalogIndexV2ProjectionResult.Success)
        val entries = (projection as CatalogIndexV2ProjectionResult.Success).index.entries
            .filter { it.lessonId.startsWith("butterfly-") }
        assertTrue(entries.all { it.collectionIds.isEmpty() })
        assertTrue(entries.all { it.contentFamilyId == null })
        assertTrue(CatalogTaxonomyV2.registry.diagnostics.isEmpty())
    }

    private fun <T : Any> assertNotNullAndReturn(value: T?): T {
        assertNotNull(value)
        return checkNotNull(value)
    }

    private data class ExpectedLesson(
        val id: String,
        val ageBand: AgeBand,
        val difficulty: Int,
        val minimumContentApi: Int,
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
