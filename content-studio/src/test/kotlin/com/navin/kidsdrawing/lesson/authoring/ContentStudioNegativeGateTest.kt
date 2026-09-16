package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.LessonCatalogSource
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentStudioNegativeGateTest {
    private val base = FileAssetCatalogSource(File("src/main/assets"))
    private val cuteCat = (ContentStudioPackageImporter(base)
        .import("lessons/cute-cat") as ContentStudioImportResult.Success).draft

    @Test
    fun unknownTaxonomyReferenceIsBlockedByV2IndexValidation() {
        val invalidLesson = cuteCat.lesson.copy(
            metadata = cuteCat.lesson.metadata.copy(
                categoryIds = cuteCat.lesson.metadata.categoryIds + "unknown.studio.category",
            ),
        )
        val staged = (ContentStudioCanonicalExporter.export(cuteCat.copy(lesson = invalidLesson))
            as ContentStudioExportResult.Success).stagedPackage

        val result = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        assertTrue(blocked.diagnostics.any {
            it.code == ContentStudioDiagnosticCode.INDEX_INVALID &&
                it.message.contains("taxonomy", ignoreCase = true)
        })
        val indexGate = blocked.evidence.gateEvidence.single {
            it.gate == ContentStudioValidationGate.INDEX_VALIDATION
        }
        assertTrue(indexGate.status == ContentStudioGateStatus.BLOCKED)
        assertTrue(indexGate.diagnostics.any { it.sourceCode == "MISSING_TAXONOMY_REFERENCE" })
    }

    @Test
    fun gentleHintWithoutAuthoredTextIsBlockedByCapabilityGate() {
        val firstStep = cuteCat.lesson.drawing.steps.first()
        val hintIndex = firstStep.help.indexOfFirst { it.narrationKey != null }
        assertTrue(hintIndex >= 0)
        val brokenHelp = firstStep.help.mapIndexed { index, help ->
            if (index == hintIndex) help.copy(narrationKey = null, guideRefs = emptyList()) else help
        }
        val invalidLesson = cuteCat.lesson.copy(
            drawing = cuteCat.lesson.drawing.copy(
                steps = listOf(firstStep.copy(help = brokenHelp)) + cuteCat.lesson.drawing.steps.drop(1),
            ),
        )
        val staged = (ContentStudioCanonicalExporter.export(cuteCat.copy(lesson = invalidLesson))
            as ContentStudioExportResult.Success).stagedPackage

        val result = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        assertTrue(blocked.diagnostics.any {
            it.code == ContentStudioDiagnosticCode.CAPABILITY_INVALID &&
                it.message.contains("requires an authored narration", ignoreCase = true)
        })
    }

    @Test
    fun traceModeWithStructuredStepMissingTraceGeometryIsBlocked() {
        val firstStep = cuteCat.lesson.drawing.steps.first()
        val withoutTraceHelp = firstStep.help.filterNot { it.kind.name == "TRACE_PATH" }
        val invalidLesson = cuteCat.lesson.copy(
            drawing = cuteCat.lesson.drawing.copy(
                steps = listOf(
                    firstStep.copy(
                        childTurn = firstStep.childTurn.copy(
                            allowSkip = false,
                            expectedStrokeRefs = emptyList(),
                        ),
                        help = withoutTraceHelp,
                    ),
                ) + cuteCat.lesson.drawing.steps.drop(1),
            ),
        )
        val staged = (ContentStudioCanonicalExporter.export(cuteCat.copy(lesson = invalidLesson))
            as ContentStudioExportResult.Success).stagedPackage

        val result = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        assertTrue(blocked.diagnostics.any {
            (it.code == ContentStudioDiagnosticCode.PRODUCTION_PACKAGE_INVALID ||
                it.code == ContentStudioDiagnosticCode.CAPABILITY_INVALID) &&
                it.message.contains("Trace", ignoreCase = true)
        })
    }

    @Test
    fun preparedColoringDeclarationWithoutRegionGeometryCannotReachStaging() {
        val invalidLesson = cuteCat.lesson.copy(
            assets = cuteCat.lesson.assets.copy(coloringRegions = "coloring-regions.json"),
        )
        val result = ContentStudioCanonicalExporter.export(
            cuteCat.copy(lesson = invalidLesson, coloringRegionCatalog = null),
        )

        assertTrue(result is ContentStudioExportResult.Failure)
        val failure = result as ContentStudioExportResult.Failure
        assertTrue(failure.diagnostics.any {
            it.code == ContentStudioDiagnosticCode.MISSING_DRAFT_ASSET &&
                it.path == "assets.coloringRegions"
        })
    }

    @Test
    fun invalidLessonIdIsRejectedByProductionPackageGate() {
        val invalidId = "Cute Cat"
        val invalidDraft = cuteCat.copy(
            packageRoot = "lessons/$invalidId",
            lesson = cuteCat.lesson.copy(lessonId = invalidId),
        )
        val exported = ContentStudioCanonicalExporter.export(invalidDraft)
        assertTrue(exported is ContentStudioExportResult.Success)
        val staged = (exported as ContentStudioExportResult.Success).stagedPackage

        val result = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        val loaderGate = blocked.evidence.gateEvidence.single {
            it.gate == ContentStudioValidationGate.PACKAGE_LOADER
        }
        assertTrue(loaderGate.status == ContentStudioGateStatus.BLOCKED)
        assertTrue(loaderGate.diagnostics.any { it.sourceCode == "INVALID_ID" })
    }

    @Test
    fun missingAuthoredStrokeReferenceIsRejectedByProductionPackageGate() {
        val firstStep = cuteCat.lesson.drawing.steps.first()
        val invalidLesson = cuteCat.lesson.copy(
            drawing = cuteCat.lesson.drawing.copy(
                steps = listOf(
                    firstStep.copy(
                        teacher = firstStep.teacher.copy(strokeRefs = listOf("missing-studio-stroke")),
                    ),
                ) + cuteCat.lesson.drawing.steps.drop(1),
            ),
        )
        val staged = (ContentStudioCanonicalExporter.export(cuteCat.copy(lesson = invalidLesson))
            as ContentStudioExportResult.Success).stagedPackage

        val result = ContentStudioProductionValidator(base).validate(staged)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        val loaderGate = blocked.evidence.gateEvidence.single {
            it.gate == ContentStudioValidationGate.PACKAGE_LOADER
        }
        assertTrue(loaderGate.status == ContentStudioGateStatus.BLOCKED)
        assertTrue(loaderGate.diagnostics.any { it.sourceCode == "MISSING_REFERENCE" })
    }

    @Test
    fun unsafeAssetPathIsRejectedBeforeStaging() {
        val invalidLesson = cuteCat.lesson.copy(
            assets = cuteCat.lesson.assets.copy(preview = "../outside-preview.svg"),
        )
        val result = ContentStudioCanonicalExporter.export(cuteCat.copy(lesson = invalidLesson))

        assertTrue(result is ContentStudioExportResult.Failure)
        val failure = result as ContentStudioExportResult.Failure
        assertTrue(failure.diagnostics.any {
            it.code == ContentStudioDiagnosticCode.INVALID_ASSET_PATH && it.path == "assets.preview"
        })
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
