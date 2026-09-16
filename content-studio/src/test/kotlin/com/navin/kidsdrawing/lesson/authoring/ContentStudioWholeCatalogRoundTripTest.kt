package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.LessonCapabilityValidator
import com.navin.kidsdrawing.lesson.content.LessonCatalogSource
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentStudioWholeCatalogRoundTripTest {
    @Test
    fun everyCurrentLessonPackageSurvivesStudioImportCanonicalExportAndProductionReload() {
        val base = FileAssetCatalogSource(File("src/main/assets"))
        val packageNames = checkNotNull(base.list("lessons")).sorted()
        assertEquals(24, packageNames.size)

        packageNames.forEach { packageName ->
            val root = "lessons/$packageName"
            val imported = ContentStudioPackageImporter(base).import(root)
            assertTrue("Studio import failed for $root: $imported", imported is ContentStudioImportResult.Success)
            val original = (imported as ContentStudioImportResult.Success).draft

            val exported = ContentStudioCanonicalExporter.export(original)
            assertTrue("Studio export failed for $root: $exported", exported is ContentStudioExportResult.Success)
            val staged = (exported as ContentStudioExportResult.Success).stagedPackage

            val stagedSource = LessonPackageSource(staged::readText)
            val productionReload = LessonPackageLoader(stagedSource).load(root)
            assertTrue("Production reload failed for $root: $productionReload", productionReload is LessonLoadResult.Success)
            val reloadedPackage = (productionReload as LessonLoadResult.Success).packageData
            assertTrue(
                "Capability validation failed for $root: ${LessonCapabilityValidator.validate(reloadedPackage)}",
                LessonCapabilityValidator.validate(reloadedPackage).isEmpty(),
            )

            val reimported = ContentStudioPackageImporter(stagedSource).import(root)
            assertTrue("Studio re-import failed for $root: $reimported", reimported is ContentStudioImportResult.Success)
            val roundTrip = (reimported as ContentStudioImportResult.Success).draft

            assertEquals("Lesson semantics changed for $root", original.lesson, roundTrip.lesson)
            assertEquals("Stroke geometry changed for $root", original.strokeCatalog, roundTrip.strokeCatalog)
            assertEquals(
                "Coloring geometry changed for $root",
                original.coloringRegionCatalog,
                roundTrip.coloringRegionCatalog,
            )
            assertEquals("Strings changed for $root", original.stringsByLocale, roundTrip.stringsByLocale)
        }
    }

    @Test
    fun readyEvidenceIsByteStableForSameValidatedCandidate() {
        val base = FileAssetCatalogSource(File("src/main/assets"))
        val draft = (ContentStudioPackageImporter(base).import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val staged = (ContentStudioCanonicalExporter.export(draft) as ContentStudioExportResult.Success).stagedPackage
        val validation = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(validation is ContentStudioValidationResult.Ready)

        assertEquals(
            ContentStudioEvidenceJson.render(validation),
            ContentStudioEvidenceJson.render(validation),
        )
        val json = ContentStudioEvidenceJson.render(validation)
        assertTrue(json.contains("\"validationState\": \"ready\""))
        assertTrue(json.contains("\"lessonId\": \"cute-cat\""))
        assertTrue(json.contains("\"CHILD_TOOL_PRESET\""))
        assertTrue(json.contains("\"RESERVED_PRESERVE_ONLY\""))
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
