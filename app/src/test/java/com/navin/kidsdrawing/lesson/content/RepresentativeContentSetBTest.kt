package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.ColoringMode
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepresentativeContentSetBTest {
    @Test
    fun productionCatalogContainsNineReleaseLessonsWithoutDiagnostics() {
        val snapshot = productionCatalog()
        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(9, snapshot.entries.size)
        assertEquals(
            setOf(
                "cute-cat", "design-your-spaceship", "easy-flower", "fox-portrait", "friendly-owl",
                "hot-air-balloon", "little-fish", "simple-rocket", "smiling-sun",
            ),
            snapshot.entries.map { it.identity.lessonId }.toSet(),
        )
    }

    @Test
    fun threeNewPackagesDirectLoadThroughStrictProductionLoader() {
        val loader = LessonPackageLoader(FileAssetCatalogSource(File("src/main/assets")))
        listOf("hot-air-balloon", "fox-portrait", "design-your-spaceship").forEach { id ->
            val result = loader.load("lessons/$id")
            assertTrue("$id failed to load: $result", result is LessonLoadResult.Success)
        }
    }

    @Test
    fun hotAirBalloonProvesRicherPreparedGuidedColoring() {
        val runtime = productionCatalog().runtime("hot-air-balloon")
        val lesson = runtime.lesson
        val regions = checkNotNull(runtime.coloringRegionCatalog)
        assertEquals(2, lesson.minimumContentApi)
        assertEquals(ColoringMode.GUIDED, lesson.coloring?.defaultMode)
        assertEquals(
            listOf("color_center", "color_sides", "color_basket"),
            lesson.coloring?.steps?.map { it.id },
        )
        assertEquals(
            setOf("balloon-left-region", "balloon-center-region", "balloon-right-region", "balloon-basket-region"),
            regions.regions.map { it.id }.toSet(),
        )
        assertEquals(2, lesson.coloring?.steps?.single { it.id == "color_sides" }?.regionIds?.size)
        assertTrue(AgeBand.LITTLE_ARTISTS in lesson.metadata.ageBands)
        assertTrue(TeachingMode.DRAW_WITH_ME in lesson.supportedModes)
        assertTrue(TeachingMode.WATCH_THEN_DRAW in lesson.supportedModes)
    }

    @Test
    fun foxPortraitIsCredibleOlderChildProportionContentWithoutTrace() {
        val lesson = productionCatalog().runtime("fox-portrait").lesson
        assertEquals(setOf(AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(4, lesson.metadata.difficulty)
        assertTrue("proportion" in lesson.metadata.skillIds)
        assertTrue("detail.texture" in lesson.metadata.skillIds)
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(TeachingMode.DRAW_WITH_ME in lesson.supportedModes)
        assertTrue(TeachingMode.WATCH_THEN_DRAW in lesson.supportedModes)
        assertFalse(lesson.coloring?.enabled == true)
    }

    @Test
    fun spaceshipCreativeStepHasNoReplicaRequirement() {
        val lesson = productionCatalog().runtime("design-your-spaceship").lesson
        val creative = lesson.drawing.steps.single { it.id == "make_it_yours" }
        assertTrue(AgeBand.YOUNG_ARTISTS in lesson.metadata.ageBands)
        assertTrue(creative.teacher.strokeRefs.isNotEmpty())
        assertTrue(creative.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(creative.childTurn.allowSkip)
        assertEquals("manual_done", creative.childTurn.completionPolicy.name.lowercase())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
    }

    @Test
    fun representativeSetCoversAllAgesDifficultyOneToFourAndThreePopulatedJourneys() {
        val snapshot = productionCatalog()
        AgeBand.values().forEach { age -> assertTrue("No lessons for $age", snapshot.forAgeBand(age).isNotEmpty()) }
        val difficulties = snapshot.entries.map { it.difficulty }.toSet()
        assertTrue(1 in difficulties)
        assertTrue(2 in difficulties)
        assertTrue(3 in difficulties)
        assertTrue(4 in difficulties)

        val lessons = snapshot.entries.map { entry -> checkNotNull(snapshot.runtimePackage(entry.identity)).lesson }
        val journeyCounts = lessons.flatMap { it.metadata.journeyIds }.groupingBy { it }.eachCount()
        assertTrue((journeyCounts["journey.first_shapes_to_pictures"] ?: 0) >= 2)
        assertTrue((journeyCounts["journey.animal_artist"] ?: 0) >= 2)
        assertTrue((journeyCounts["journey.space_artist"] ?: 0) >= 2)
        assertTrue(snapshot.forAgeBand(AgeBand.YOUNG_ARTISTS).size >= 2)
    }

    @Test
    fun everyReleasePackageHasCompleteDefaultEnglishKeys() {
        val snapshot = productionCatalog()
        snapshot.entries.forEach { entry ->
            val runtime = checkNotNull(snapshot.runtimePackage(entry.identity))
            val lesson = runtime.lesson
            val stringPath = lesson.assets.strings["en"]
            assertNotNull("${lesson.lessonId} has no English strings asset", stringPath)
            val stringsFile = File("src/main/assets/${runtime.packageRoot}/${checkNotNull(stringPath)}")
            assertTrue("Missing ${stringsFile.path}", stringsFile.isFile)
            val keys = Json.parseToJsonElement(stringsFile.readText()).jsonObject.keys
            referencedStringKeys(lesson).forEach { key ->
                assertTrue("${lesson.lessonId} missing default-locale key $key", key in keys)
            }
        }
    }

    private fun referencedStringKeys(lesson: com.navin.kidsdrawing.lesson.model.LessonSource): Set<String> = buildSet {
        add(lesson.metadata.titleKey)
        add(lesson.metadata.summaryKey)
        lesson.drawing.steps.forEach { step ->
            step.teacher.narrationKey?.let(::add)
            step.completionNarrationKey?.let(::add)
            step.help.forEach { it.narrationKey?.let(::add) }
        }
        lesson.coloring?.steps.orEmpty().forEach { it.narrationKey?.let(::add) }
    }

    private fun productionCatalog(): LessonCatalogSnapshot =
        LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()

    private fun LessonCatalogSnapshot.runtime(id: String) =
        checkNotNull(runtimePackage(checkNotNull(byLessonId(id).singleOrNull()).identity))

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
