package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Imports accepted normal lesson packages into an internal Studio draft.
 *
 * V1 is intentionally text-asset-only. Current release content declares no audio assets; a future
 * binary asset source must be contracted before Studio can promise lossless audio round-tripping.
 */
class ContentStudioPackageImporter(
    private val source: LessonPackageSource,
    private val loader: LessonPackageLoader = LessonPackageLoader(source),
    private val json: Json = LessonPackageLoader.DEFAULT_JSON,
) {
    fun import(packageRoot: String): ContentStudioImportResult {
        val loaded = loader.load(packageRoot)
        if (loaded is LessonLoadResult.Failure) {
            return ContentStudioImportResult.Failure(
                loaded.diagnostics.map { diagnostic ->
                    ContentStudioDiagnostic(
                        code = ContentStudioDiagnosticCode.PRODUCTION_PACKAGE_INVALID,
                        path = diagnostic.path,
                        message = diagnostic.message,
                    )
                },
            )
        }
        val packageData = (loaded as LessonLoadResult.Success).packageData
        val lesson = packageData.lesson

        if (lesson.assets.audio.isNotEmpty()) {
            return ContentStudioImportResult.Failure(
                listOf(
                    ContentStudioDiagnostic(
                        code = ContentStudioDiagnosticCode.UNSUPPORTED_BINARY_AUDIO,
                        path = "assets.audio",
                        message = "Content Studio V1 cannot losslessly import binary audio assets; voice audio remains an unsupported working capability.",
                    ),
                ),
            )
        }

        val diagnostics = mutableListOf<ContentStudioDiagnostic>()
        val stringsByLocale = linkedMapOf<String, Map<String, String>>()
        lesson.assets.strings.toSortedMap().forEach { (locale, relativePath) ->
            val path = studioSafeJoin(packageData.packageRoot, relativePath)
            val text = path?.let(source::readText)
            if (path == null || text == null) {
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.MISSING_DRAFT_ASSET,
                    "assets.strings.$locale",
                    "Declared strings asset is unavailable for '$locale'.",
                )
            } else {
                val decoded = try {
                    json.decodeFromString<Map<String, String>>(text)
                } catch (_: SerializationException) {
                    null
                } catch (_: IllegalArgumentException) {
                    null
                }
                if (decoded == null) {
                    diagnostics += ContentStudioDiagnostic(
                        ContentStudioDiagnosticCode.INVALID_LOCALIZATION,
                        "assets.strings.$locale",
                        "Declared strings asset is not a valid string map.",
                    )
                } else {
                    stringsByLocale[locale] = decoded.toSortedMap()
                }
            }
        }

        val thumbnailSvg = readRequiredTextAsset(
            packageData.packageRoot,
            lesson.assets.thumbnail,
            "assets.thumbnail",
            diagnostics,
        )
        val previewSvg = readRequiredTextAsset(
            packageData.packageRoot,
            lesson.assets.preview,
            "assets.preview",
            diagnostics,
        )

        if (diagnostics.isNotEmpty()) {
            return ContentStudioImportResult.Failure(diagnostics.sortedBy { "${it.code}:${it.path}:${it.message}" })
        }

        return ContentStudioImportResult.Success(
            ContentStudioDraft(
                packageRoot = packageData.packageRoot,
                lesson = lesson,
                strokeCatalog = packageData.strokeCatalog,
                stringsByLocale = stringsByLocale,
                thumbnailSvg = checkNotNull(thumbnailSvg),
                previewSvg = checkNotNull(previewSvg),
                coloringRegionCatalog = packageData.coloringRegionCatalog,
            ),
        )
    }

    private fun readRequiredTextAsset(
        packageRoot: String,
        relativePath: String,
        diagnosticPath: String,
        diagnostics: MutableList<ContentStudioDiagnostic>,
    ): String? {
        val path = studioSafeJoin(packageRoot, relativePath)
        val text = path?.let(source::readText)
        if (path == null || text.isNullOrBlank()) {
            diagnostics += ContentStudioDiagnostic(
                ContentStudioDiagnosticCode.MISSING_DRAFT_ASSET,
                diagnosticPath,
                "Required discovery asset is missing or empty.",
            )
            return null
        }
        return text
    }
}

/** Deterministic serializer from an internal Studio draft to the existing runtime package format. */
object ContentStudioCanonicalExporter {
    private val renderJson = Json {
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = true
    }

    fun export(draft: ContentStudioDraft): ContentStudioExportResult {
        val diagnostics = validateDraftAssets(draft)
        if (diagnostics.isNotEmpty()) return ContentStudioExportResult.Failure(diagnostics)

        val lesson = draft.lesson.copy(
            assets = draft.lesson.assets.copy(
                strings = draft.lesson.assets.strings.toSortedMap(),
                audio = draft.lesson.assets.audio.toSortedMap(),
            ),
        )
        val files = sortedMapOf<String, String>()
        files["${draft.packageRoot}/lesson.json"] = renderJson.encodeToString(lesson) + "\n"
        files[checkNotNull(studioSafeJoin(draft.packageRoot, lesson.assets.strokeFile))] =
            renderJson.encodeToString(draft.strokeCatalog) + "\n"
        lesson.assets.strings.toSortedMap().forEach { (locale, relativePath) ->
            val strings = checkNotNull(draft.stringsByLocale[locale]).toSortedMap()
            files[checkNotNull(studioSafeJoin(draft.packageRoot, relativePath))] =
                renderJson.encodeToString(strings) + "\n"
        }
        files[checkNotNull(studioSafeJoin(draft.packageRoot, lesson.assets.thumbnail))] =
            draft.thumbnailSvg.withTrailingNewline()
        files[checkNotNull(studioSafeJoin(draft.packageRoot, lesson.assets.preview))] =
            draft.previewSvg.withTrailingNewline()
        lesson.assets.coloringRegions?.let { relativePath ->
            files[checkNotNull(studioSafeJoin(draft.packageRoot, relativePath))] =
                renderJson.encodeToString(checkNotNull(draft.coloringRegionCatalog)) + "\n"
        }
        return ContentStudioExportResult.Success(
            ContentStudioStagedPackage(
                packageRoot = draft.packageRoot,
                files = files.toMap(),
            ),
        )
    }

    private fun validateDraftAssets(draft: ContentStudioDraft): List<ContentStudioDiagnostic> = buildList {
        val expectedRoot = "lessons/${draft.lesson.lessonId}"
        if (draft.packageRoot != expectedRoot || studioSafeJoin("stage", draft.packageRoot) == null) {
            add(
                ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.INVALID_PACKAGE_ROOT,
                    "packageRoot",
                    "Studio package root must be exactly '$expectedRoot'.",
                ),
            )
        }

        if (draft.lesson.assets.audio.isNotEmpty()) {
            add(
                ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.UNSUPPORTED_BINARY_AUDIO,
                    "assets.audio",
                    "Content Studio V1 does not export binary audio; file presence cannot be treated as working voice narration.",
                ),
            )
        }

        val declaredLocales = draft.lesson.assets.strings.keys.toSortedSet()
        val suppliedLocales = draft.stringsByLocale.keys.toSortedSet()
        if (declaredLocales != suppliedLocales) {
            add(
                ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.LOCALIZATION_MISMATCH,
                    "assets.strings",
                    "Declared locales $declaredLocales must exactly match supplied Studio string maps $suppliedLocales.",
                ),
            )
        }
        draft.stringsByLocale.toSortedMap().forEach { (locale, strings) ->
            if (strings.keys.any(String::isBlank) || strings.values.any(String::isBlank)) {
                add(
                    ContentStudioDiagnostic(
                        ContentStudioDiagnosticCode.INVALID_LOCALIZATION,
                        "strings.$locale",
                        "String keys and values must be non-blank.",
                    ),
                )
            }
        }

        if (draft.thumbnailSvg.isBlank()) {
            add(
                ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.MISSING_DRAFT_ASSET,
                    "assets.thumbnail",
                    "Thumbnail SVG must not be blank.",
                ),
            )
        }
        if (draft.previewSvg.isBlank()) {
            add(
                ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.MISSING_DRAFT_ASSET,
                    "assets.preview",
                    "Preview SVG must not be blank.",
                ),
            )
        }

        val regionPath = draft.lesson.assets.coloringRegions
        if ((regionPath == null) != (draft.coloringRegionCatalog == null)) {
            add(
                ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.MISSING_DRAFT_ASSET,
                    "assets.coloringRegions",
                    "Coloring-region declaration and authored region catalog must either both exist or both be absent.",
                ),
            )
        }

        val paths = buildList {
            add("assets.strokeFile" to draft.lesson.assets.strokeFile)
            add("assets.thumbnail" to draft.lesson.assets.thumbnail)
            add("assets.preview" to draft.lesson.assets.preview)
            draft.lesson.assets.strings.forEach { (locale, path) -> add("assets.strings.$locale" to path) }
            regionPath?.let { add("assets.coloringRegions" to it) }
        }
        paths.forEach { (path, relativePath) ->
            if (studioSafeJoin(draft.packageRoot, relativePath) == null) {
                add(
                    ContentStudioDiagnostic(
                        ContentStudioDiagnosticCode.INVALID_ASSET_PATH,
                        path,
                        "Asset path must be a safe relative path contained inside the lesson package.",
                    ),
                )
            }
        }
    }.sortedBy { "${it.code}:${it.path}:${it.message}" }
}

internal fun studioSafeJoin(root: String, relative: String): String? {
    if (root.isBlank() || relative.isBlank()) return null
    if (root.startsWith('/') || root.startsWith('\\') || relative.startsWith('/') || relative.startsWith('\\')) return null
    val rootSegments = root.replace('\\', '/').split('/')
    val relativeSegments = relative.replace('\\', '/').split('/')
    if ((rootSegments + relativeSegments).any { it.isBlank() || it == "." || it == ".." }) return null
    return (rootSegments + relativeSegments).joinToString("/")
}

private fun String.withTrailingNewline(): String = if (endsWith("\n")) this else "$this\n"
