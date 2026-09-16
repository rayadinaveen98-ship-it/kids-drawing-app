package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Loader
import com.navin.kidsdrawing.lesson.content.LessonCatalogSource
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentStudioPromotionPlanTest {
    private val base = FileAssetCatalogSource(File("src/main/assets"))

    @Test
    fun readyCandidateProducesOnePlanWithPackageIndexAndStaleDeletes() {
        val draft = (ContentStudioPackageImporter(base).import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val staged = (ContentStudioCanonicalExporter.export(draft) as ContentStudioExportResult.Success).stagedPackage
        val validation = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(validation is ContentStudioValidationResult.Ready)

        val existing = staged.manifest + "lessons/cute-cat/obsolete.svg"
        val planned = ContentStudioPromotionPlanner.plan(validation, existing)
        assertTrue(planned is ContentStudioPromotionPlanResult.Ready)
        val plan = (planned as ContentStudioPromotionPlanResult.Ready).plan

        assertEquals(listOf("lessons/cute-cat/obsolete.svg"), plan.deleteFiles)
        assertEquals(staged.files.keys.sorted(), plan.writeFiles.keys.filter { it != CatalogIndexV2Loader.DEFAULT_INDEX_PATH }.sorted())
        assertEquals(
            (validation as ContentStudioValidationResult.Ready).projectedIndexText,
            plan.writeFiles[CatalogIndexV2Loader.DEFAULT_INDEX_PATH],
        )
    }

    @Test
    fun blockedCandidateNeverInvokesPromotionTarget() {
        val draft = (ContentStudioPackageImporter(base).import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val english = checkNotNull(draft.stringsByLocale["en"]) - draft.lesson.metadata.titleKey
        val invalid = draft.copy(stringsByLocale = draft.stringsByLocale + ("en" to english))
        val staged = (ContentStudioCanonicalExporter.export(invalid) as ContentStudioExportResult.Success).stagedPackage
        val validation = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(validation is ContentStudioValidationResult.Blocked)

        var invoked = false
        val result = ContentStudioPromotionService.promote(
            validation = validation,
            existingPackageFiles = staged.manifest,
            target = ContentStudioPromotionTarget {
                invoked = true
                true
            },
        )
        assertTrue(result is ContentStudioPromotionResult.Rejected)
        assertFalse(invoked)
    }

    @Test
    fun targetFailureIsReportedWithoutClaimingCommit() {
        val draft = (ContentStudioPackageImporter(base).import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val staged = (ContentStudioCanonicalExporter.export(draft) as ContentStudioExportResult.Success).stagedPackage
        val validation = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(validation is ContentStudioValidationResult.Ready)

        val result = ContentStudioPromotionService.promote(
            validation = validation,
            existingPackageFiles = staged.manifest,
            target = ContentStudioPromotionTarget { false },
        )
        assertEquals(ContentStudioPromotionResult.TargetFailedWithoutCommit, result)
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
