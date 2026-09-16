package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogSelectedLessonLoaderTest {
    @Test
    fun metadataIndexLoadReadsNoTeachingPackage() {
        val reads = mutableListOf<String>()
        val index = index()
        val rendered = LessonPackageLoader.DEFAULT_JSON.encodeToString(index)
        val source = LessonPackageSource { path ->
            reads += path
            if (path == CatalogIndexV2Loader.DEFAULT_INDEX_PATH) rendered else null
        }

        val result = CatalogIndexV2Loader(
            source = source,
            taxonomyRegistry = registry(index),
        ).load()

        assertTrue(result is CatalogIndexV2LoadResult.Success)
        assertEquals(listOf(CatalogIndexV2Loader.DEFAULT_INDEX_PATH), reads)
    }

    @Test
    fun selectingOneLessonTouchesOnlyItsPackageRoot() {
        val reads = mutableListOf<String>()
        val index = index()
        val rendered = LessonPackageLoader.DEFAULT_JSON.encodeToString(index)
        val source = LessonPackageSource { path ->
            reads += path
            if (path == CatalogIndexV2Loader.DEFAULT_INDEX_PATH) rendered else null
        }
        val loader = CatalogSelectedLessonLoader(
            indexLoader = CatalogIndexV2Loader(source, taxonomyRegistry = registry(index)),
            packageLoader = LessonPackageLoader(source),
        )

        val result = loader.load(LessonCatalogIdentity("selected-lesson", 1))

        assertTrue(result is SelectedLessonLoadResult.Failure)
        assertEquals(
            listOf(
                CatalogIndexV2Loader.DEFAULT_INDEX_PATH,
                "lessons/selected-lesson/lesson.json",
            ),
            reads,
        )
        assertTrue(reads.none { it.startsWith("lessons/other-lesson/") })
    }

    private fun index() = CatalogIndexV2Source(
        schemaVersion = CatalogIndexV2Loader.SCHEMA_VERSION,
        contentApi = LessonPackageLoader.CURRENT_CONTENT_API,
        entries = listOf(
            entry("selected-lesson"),
            entry("other-lesson"),
        ),
    )

    private fun entry(lessonId: String) = CatalogIndexV2EntrySource(
        lessonId = lessonId,
        revision = 1,
        status = LessonStatus.RELEASE,
        minimumContentApi = 1,
        packageRef = "lessons/$lessonId",
        title = lessonId,
        titleKey = "lesson.title",
        summary = "Practice $lessonId.",
        summaryKey = "lesson.summary",
        ageBands = listOf(AgeBand.CREATIVE_EXPLORERS),
        difficulty = 2,
        estimatedMinutes = 8,
        drawingStepCount = 3,
        categoryIds = listOf("animals"),
        skillIds = listOf("line-control"),
        supportedModes = listOf(TeachingMode.DRAW_WITH_ME),
        capabilitySummary = CatalogCapabilitySummarySource(
            teachingModes = listOf(TeachingMode.DRAW_WITH_ME),
            helpAvailable = false,
            traceReady = false,
            coloring = CatalogColoringCapability.NONE,
        ),
        thumbnailRef = "lessons/$lessonId/thumb.webp",
        previewRef = "lessons/$lessonId/preview.webp",
    )

    private fun registry(index: CatalogIndexV2Source) = CatalogTaxonomyRegistry.fromIds(
        categories = index.entries.flatMap { it.categoryIds }.toSet(),
        skills = index.entries.flatMap { it.skillIds }.toSet(),
    )
}
