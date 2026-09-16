package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.LessonCatalogSource
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentStudioPackageCodecTest {
    private val baseSource = FileAssetCatalogSource(File("src/main/assets"))
    private val importer = ContentStudioPackageImporter(baseSource)

    @Test
    fun minimalValidDraftExportsDeterministicallyAndReloadsThroughProductionLoader() {
        val sourceDraft = (importer.import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val sourceStep = sourceDraft.lesson.drawing.steps.first()
        val minimalStep = sourceStep.copy(
            help = emptyList(),
            completionNarrationKey = null,
        )
        val requiredStrokeIds = (
            minimalStep.teacher.strokeRefs + minimalStep.childTurn.expectedStrokeRefs
        ).toSet()
        val minimalStrokeCatalog = sourceDraft.strokeCatalog.copy(
            strokes = sourceDraft.strokeCatalog.strokes.filter { it.id in requiredStrokeIds },
            guides = emptyList(),
        )
        val requiredStringKeys = buildSet {
            add(sourceDraft.lesson.metadata.titleKey)
            add(sourceDraft.lesson.metadata.summaryKey)
            minimalStep.teacher.narrationKey?.let(::add)
        }
        val english = checkNotNull(sourceDraft.stringsByLocale["en"])
            .filterKeys(requiredStringKeys::contains)
        val minimalLesson = sourceDraft.lesson.copy(
            supportedModes = listOf(TeachingMode.DRAW_WITH_ME),
            assets = sourceDraft.lesson.assets.copy(coloringRegions = null),
            drawing = sourceDraft.lesson.drawing.copy(steps = listOf(minimalStep)),
            coloring = null,
        )
        val minimalDraft = sourceDraft.copy(
            lesson = minimalLesson,
            strokeCatalog = minimalStrokeCatalog,
            stringsByLocale = mapOf("en" to english),
            coloringRegionCatalog = null,
        )

        val first = ContentStudioCanonicalExporter.export(minimalDraft)
        val second = ContentStudioCanonicalExporter.export(minimalDraft)
        assertTrue(first is ContentStudioExportResult.Success)
        assertTrue(second is ContentStudioExportResult.Success)
        val firstStaged = (first as ContentStudioExportResult.Success).stagedPackage
        val secondStaged = (second as ContentStudioExportResult.Success).stagedPackage
        assertEquals(firstStaged.files, secondStaged.files)

        val reloaded = LessonPackageLoader(LessonPackageSource(firstStaged::readText)).load("lessons/cute-cat")
        assertTrue("Minimal normal package failed production reload: $reloaded", reloaded is LessonLoadResult.Success)
        val packageData = (reloaded as LessonLoadResult.Success).packageData
        assertEquals(minimalLesson, packageData.lesson)
        assertEquals(minimalStrokeCatalog, packageData.strokeCatalog)
    }

    @Test
    fun acceptedCuteCatRoundTripsDeterministicallyThroughNormalPackageFormat() {
        val imported = importer.import("lessons/cute-cat") as ContentStudioImportResult.Success

        val first = ContentStudioCanonicalExporter.export(imported.draft) as ContentStudioExportResult.Success
        val second = ContentStudioCanonicalExporter.export(imported.draft) as ContentStudioExportResult.Success
        assertEquals(first.stagedPackage.files, second.stagedPackage.files)

        val validation = ContentStudioProductionValidator(baseSource).validate(first.stagedPackage)
        assertTrue("Expected READY but got $validation", validation is ContentStudioValidationResult.Ready)

        val reimported = ContentStudioPackageImporter(
            LessonPackageSource(first.stagedPackage::readText),
        ).import("lessons/cute-cat") as ContentStudioImportResult.Success

        assertEquals(imported.draft.lesson, reimported.draft.lesson)
        assertEquals(imported.draft.strokeCatalog, reimported.draft.strokeCatalog)
        assertEquals(imported.draft.coloringRegionCatalog, reimported.draft.coloringRegionCatalog)
        assertEquals(imported.draft.stringsByLocale, reimported.draft.stringsByLocale)
        assertEquals(imported.draft.thumbnailSvg.ensureTrailingNewline(), reimported.draft.thumbnailSvg)
        assertEquals(imported.draft.previewSvg.ensureTrailingNewline(), reimported.draft.previewSvg)
    }

    @Test
    fun legacyReservedFieldsArePreservedButNeverPromotedToWorkingCapabilities() {
        val draft = (importer.import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val usage = ContentStudioCapabilityStatusPolicy.reservedFieldUsage(draft.lesson)

        assertTrue(usage.any { it.field == ContentStudioReservedField.CHILD_TOOL_PRESET })
        assertTrue(usage.any { it.field == ContentStudioReservedField.TEACHER_PLAY_AS_GROUP })
        assertTrue(usage.all { it.disposition == ContentStudioAuthoringDisposition.RESERVED_PRESERVE_ONLY })

        val staged = (ContentStudioCanonicalExporter.export(draft) as ContentStudioExportResult.Success).stagedPackage
        val reimported = ContentStudioPackageImporter(LessonPackageSource(staged::readText))
            .import("lessons/cute-cat") as ContentStudioImportResult.Success
        assertEquals(
            draft.lesson.drawing.steps.map { it.childTurn.toolPreset },
            reimported.draft.lesson.drawing.steps.map { it.childTurn.toolPreset },
        )
        assertEquals(
            draft.lesson.drawing.steps.map { it.teacher.playAsGroup },
            reimported.draft.lesson.drawing.steps.map { it.teacher.playAsGroup },
        )
    }

    @Test
    fun missingRequiredStringKeyIsBlockedByProductionCatalogValidation() {
        val draft = (importer.import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val english = checkNotNull(draft.stringsByLocale["en"]).toMutableMap().apply {
            remove(draft.lesson.metadata.titleKey)
        }
        val invalidDraft = draft.copy(stringsByLocale = draft.stringsByLocale + ("en" to english))
        val staged = (ContentStudioCanonicalExporter.export(invalidDraft) as ContentStudioExportResult.Success).stagedPackage

        val result = ContentStudioProductionValidator(baseSource).validate(staged)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        assertTrue(blocked.diagnostics.any {
            it.code == ContentStudioDiagnosticCode.CATALOG_INVALID &&
                it.message.contains("missing authored key", ignoreCase = true)
        })
        val catalogGate = blocked.evidence.gateEvidence.single {
            it.gate == ContentStudioValidationGate.CATALOG_INTEGRITY
        }
        assertTrue(catalogGate.status == ContentStudioGateStatus.BLOCKED)
        assertTrue(catalogGate.diagnostics.any { it.sourceCode == "MISSING_LOCALIZATION_KEY" })
    }

    @Test
    fun authoredSignalIsReleaseBlockedByAcceptedCapabilityValidator() {
        val draft = (importer.import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val firstStep = draft.lesson.drawing.steps.first()
        val invalidLesson = draft.lesson.copy(
            drawing = draft.lesson.drawing.copy(
                steps = listOf(
                    firstStep.copy(
                        childTurn = firstStep.childTurn.copy(
                            completionPolicy = ChildCompletionPolicy.AUTHORED_SIGNAL,
                        ),
                    ),
                ) + draft.lesson.drawing.steps.drop(1),
            ),
        )
        val staged = (ContentStudioCanonicalExporter.export(draft.copy(lesson = invalidLesson)) as ContentStudioExportResult.Success)
            .stagedPackage

        val result = ContentStudioProductionValidator(baseSource).validate(staged)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        assertTrue(blocked.diagnostics.any {
            it.code == ContentStudioDiagnosticCode.CAPABILITY_INVALID &&
                it.message.contains("authored_signal")
        })
    }

    @Test
    fun enforcedSuggestedColorsAreReleaseBlocked() {
        val draft = (importer.import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val coloring = checkNotNull(draft.lesson.coloring)
        val invalidLesson = draft.lesson.copy(
            coloring = coloring.copy(
                steps = coloring.steps.mapIndexed { index, step ->
                    if (index == 0) step.copy(enforceSuggestedColors = true) else step
                },
            ),
        )
        val staged = (ContentStudioCanonicalExporter.export(draft.copy(lesson = invalidLesson)) as ContentStudioExportResult.Success)
            .stagedPackage

        val result = ContentStudioProductionValidator(baseSource).validate(staged)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        assertTrue(blocked.diagnostics.any {
            it.code == ContentStudioDiagnosticCode.CAPABILITY_INVALID &&
                it.message.contains("Enforced suggested colors")
        })
    }

    @Test
    fun stagedReplacementCannotBorrowDeletedAssetFromOldReleasePackage() {
        val draft = (importer.import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val staged = (ContentStudioCanonicalExporter.export(draft) as ContentStudioExportResult.Success).stagedPackage
        val previewPath = "${staged.packageRoot}/${draft.lesson.assets.preview}"
        val withoutPreview = staged.copy(files = staged.files - previewPath)

        val result = ContentStudioProductionValidator(baseSource).validate(withoutPreview)
        assertTrue(result is ContentStudioValidationResult.Blocked)
        val blocked = result as ContentStudioValidationResult.Blocked
        assertTrue(blocked.diagnostics.any {
            it.code == ContentStudioDiagnosticCode.CATALOG_INVALID &&
                it.message.contains("Preview is missing")
        })
    }

    @Test
    fun audioDeclarationsFailClosedUntilBinaryRoundTripIsContracted() {
        val draft = (importer.import("lessons/cute-cat") as ContentStudioImportResult.Success).draft
        val lesson = draft.lesson.copy(
            assets = draft.lesson.assets.copy(audio = mapOf("en" to "audio/en.ogg")),
        )

        val result = ContentStudioCanonicalExporter.export(draft.copy(lesson = lesson))
        assertTrue(result is ContentStudioExportResult.Failure)
        val failure = result as ContentStudioExportResult.Failure
        assertTrue(failure.diagnostics.any { it.code == ContentStudioDiagnosticCode.UNSUPPORTED_BINARY_AUDIO })
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }

    private fun String.ensureTrailingNewline(): String = if (endsWith("\n")) this else "$this\n"
}
