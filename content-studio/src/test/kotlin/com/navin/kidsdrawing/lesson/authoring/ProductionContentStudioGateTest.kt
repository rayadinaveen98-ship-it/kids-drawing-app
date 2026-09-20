package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.CatalogColoringCapability
import com.navin.kidsdrawing.lesson.content.LessonCapabilityValidator
import com.navin.kidsdrawing.lesson.content.LessonCatalogSource
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.LessonStatus
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionContentStudioGateTest {
    @Test
    fun currentCatalogProducesStudioV1HealthAndEvidence() {
        val base = FileAssetCatalogSource(File("src/main/assets"))
        val packageNames = checkNotNull(base.list("lessons")).sorted()
        assertEquals(36, packageNames.size)

        var roundTrips = 0
        var toolPresetUsages = 0
        var playAsGroupUsages = 0
        var suggestedColorRoleUsages = 0
        var authoredSignalUsages = 0
        var enforcedSuggestedColorUsages = 0
        var audioDeclarations = 0

        packageNames.forEach { packageName ->
            val root = "lessons/$packageName"
            val imported = ContentStudioPackageImporter(base).import(root)
            assertTrue("Studio import failed for $root: $imported", imported is ContentStudioImportResult.Success)
            val draft = (imported as ContentStudioImportResult.Success).draft

            toolPresetUsages += draft.lesson.drawing.steps.count { it.childTurn.toolPreset != null }
            playAsGroupUsages += draft.lesson.drawing.steps.count { it.teacher.playAsGroup }
            suggestedColorRoleUsages += draft.lesson.coloring?.steps.orEmpty().count { it.suggestedColorRoles.isNotEmpty() }
            authoredSignalUsages += draft.lesson.drawing.steps.count {
                it.childTurn.completionPolicy == ChildCompletionPolicy.AUTHORED_SIGNAL
            }
            enforcedSuggestedColorUsages += draft.lesson.coloring?.steps.orEmpty().count { it.enforceSuggestedColors }
            audioDeclarations += draft.lesson.assets.audio.size

            val exported = ContentStudioCanonicalExporter.export(draft)
            assertTrue("Studio export failed for $root: $exported", exported is ContentStudioExportResult.Success)
            val staged = (exported as ContentStudioExportResult.Success).stagedPackage
            val reloaded = LessonPackageLoader(LessonPackageSource(staged::readText)).load(root)
            assertTrue("Production reload failed for $root: $reloaded", reloaded is LessonLoadResult.Success)
            val packageData = (reloaded as LessonLoadResult.Success).packageData
            assertTrue(
                "Capability gate failed for $root: ${LessonCapabilityValidator.validate(packageData)}",
                LessonCapabilityValidator.validate(packageData).isEmpty(),
            )
            assertEquals(draft.lesson, packageData.lesson)
            assertEquals(draft.strokeCatalog, packageData.strokeCatalog)
            assertEquals(draft.coloringRegionCatalog, packageData.coloringRegionCatalog)
            roundTrips++
        }

        assertEquals(36, roundTrips)
        assertEquals(0, authoredSignalUsages)
        assertEquals(0, enforcedSuggestedColorUsages)
        assertEquals(0, audioDeclarations)

        val representativeDraft = (
            ContentStudioPackageImporter(base).import("lessons/cute-cat") as ContentStudioImportResult.Success
        ).draft
        val representativeStaged = (
            ContentStudioCanonicalExporter.export(representativeDraft) as ContentStudioExportResult.Success
        ).stagedPackage
        val representativeValidation = ContentStudioProductionValidator(base).validate(representativeStaged)
        assertTrue(
            "Representative Studio production validation failed: $representativeValidation",
            representativeValidation is ContentStudioValidationResult.Ready,
        )
        val ready = representativeValidation as ContentStudioValidationResult.Ready
        val evidence = ready.evidence
        assertEquals("cute-cat", evidence.lessonId)
        assertEquals(1, evidence.revision)
        assertEquals(LessonStatus.RELEASE, evidence.lessonStatus)
        assertTrue(evidence.packageLoaderPassed)
        assertEquals(4, evidence.drawingStepCount)
        assertTrue((evidence.strokeCount ?: 0) > 0)
        assertTrue((evidence.guideCount ?: 0) > 0)
        assertEquals(0, evidence.coloringRegionCount)
        assertEquals(representativeDraft.lesson.supportedModes, evidence.supportedModes)
        assertEquals(true, evidence.helpReady)
        assertEquals(true, evidence.traceReady)
        assertEquals(CatalogColoringCapability.FREEHAND, evidence.coloringCapability)
        assertEquals(6, evidence.gateEvidence.size)
        assertTrue(evidence.gateEvidence.all { it.status == ContentStudioGateStatus.PASSED })
        assertEquals(6, evidence.qualityWarningCount)
        assertEquals(36, evidence.projectedIndexEntryCount)
        val projectedEntry = checkNotNull(evidence.projectedIndexEntry)
        assertEquals("cute-cat", projectedEntry.lessonId)
        assertEquals(1, projectedEntry.revision)
        assertEquals(evidence.supportedModes, projectedEntry.supportedModes)
        assertEquals(CatalogColoringCapability.FREEHAND, projectedEntry.capabilitySummary.coloring)

        val reportDir = File("build/reports/content-quality").apply { mkdirs() }
        val evidenceJson = ContentStudioEvidenceJson.render(ready)
        File(reportDir, "content-studio-v1-evidence.json").writeText(evidenceJson)

        val health = buildString {
            appendLine("Content Studio V1 health")
            appendLine("releasePackages=${packageNames.size}")
            appendLine("roundTripPassed=$roundTrips/${packageNames.size}")
            appendLine("representativePackage=cute-cat")
            appendLine("representativeValidation=READY")
            appendLine("evidenceSchema=${ContentStudioEvidenceJson.SCHEMA_VERSION}")
            appendLine("packageLoaderPassed=${evidence.packageLoaderPassed}")
            appendLine("validationGatesPassed=${evidence.gateEvidence.count { it.status == ContentStudioGateStatus.PASSED }}/${evidence.gateEvidence.size}")
            appendLine("evidenceQualityWarnings=${evidence.qualityWarningCount}")
            appendLine("projectedIndexEntries=${evidence.projectedIndexEntryCount}")
            appendLine("projectedEntry=${projectedEntry.lessonId}@${projectedEntry.revision}")
            appendLine("toolPresetReservedUsages=$toolPresetUsages")
            appendLine("playAsGroupReservedUsages=$playAsGroupUsages")
            appendLine("suggestedColorRolesReservedUsages=$suggestedColorRoleUsages")
            appendLine("authoredSignalReleaseBlockedUsages=$authoredSignalUsages")
            appendLine("enforcedSuggestedColorsReleaseBlockedUsages=$enforcedSuggestedColorUsages")
            appendLine("audioDeclarations=$audioDeclarations")
            appendLine("promotionRequiresReadyValidation=true")
            appendLine("promotionRequiresExplicitRevisionDecision=true")
            appendLine("promotionRequiresReleaseStatus=true")
            appendLine("promotionIncludesRegeneratedIndex=true")
            appendLine("invalidCandidateMutationPlan=false")
        }.trimEnd() + "\n"
        File(reportDir, "content-studio-v1-health.txt").writeText(health)
        println(health)
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
