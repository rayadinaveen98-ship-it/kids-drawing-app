package com.navin.kidsdrawing.lesson.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentInspectionRepositoryTest {
    @Test
    fun productionCatalogProjectsIntoReadOnlyInspectionModel() {
        val inspection = ContentInspectionRepository(
            FileAssetCatalogSource(File("src/main/assets")),
        ).load()

        assertTrue(inspection.catalogDiagnostics.toString(), inspection.catalogDiagnostics.isEmpty())
        assertEquals(0, inspection.qualityReport.errorCount)
        assertEquals(9, inspection.lessons.size)
        inspection.lessons.forEach { lesson ->
            assertTrue("${lesson.entry.identity.lessonId} preview missing", !lesson.previewSvg.isNullOrBlank())
            assertTrue("${lesson.entry.identity.lessonId} thumbnail missing", !lesson.thumbnailSvg.isNullOrBlank())
            assertTrue("${lesson.entry.identity.lessonId} strings missing", lesson.defaultStrings.isNotEmpty())
            assertNotNull(lesson.qualitySummary)
        }
    }

    @Test
    fun inspectionExposesHelpAndPreparedColorGeometryFromValidatedRuntime() {
        val inspection = ContentInspectionRepository(
            FileAssetCatalogSource(File("src/main/assets")),
        ).load()

        val owl = inspection.lessons.single { it.entry.identity.lessonId == "friendly-owl" }
        val owlHelpLevels = owl.runtime.lesson.drawing.steps.flatMap { step -> step.help.map { it.level } }.toSet()
        assertTrue(1 in owlHelpLevels)
        assertTrue(5 in owlHelpLevels)

        val fish = inspection.lessons.single { it.entry.identity.lessonId == "little-fish" }
        assertTrue(checkNotNull(fish.runtime.coloringRegionCatalog).regions.isNotEmpty())
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
