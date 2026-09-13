package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Local package source with deterministic discovery/existence capabilities for the bundled catalog. */
interface LessonCatalogSource : LessonPackageSource {
    fun list(path: String): List<String>?
    fun exists(path: String): Boolean
}

data class LessonCatalogIdentity(
    val lessonId: String,
    val revision: Int,
)

data class LessonCatalogEntry(
    val identity: LessonCatalogIdentity,
    val packageRoot: String,
    val title: String,
    val summary: String,
    val ageBands: Set<AgeBand>,
    val difficulty: Int,
    val estimatedMinutes: Int,
    val categoryIds: Set<String>,
    val skillIds: Set<String>,
    val journeyIds: Set<String>,
    val supportedModes: Set<TeachingMode>,
    val thumbnailPath: String,
    val previewPath: String,
)

enum class LessonCatalogDiagnosticCode {
    ROOT_UNAVAILABLE,
    PACKAGE_INVALID,
    DUPLICATE_LESSON_ID,
    MISSING_DECLARED_ASSET,
    INVALID_LOCALIZATION,
    MISSING_LOCALIZATION_KEY,
    MISSING_CATALOG_REFERENCE,
}

data class LessonCatalogDiagnostic(
    val code: LessonCatalogDiagnosticCode,
    val packageRoot: String?,
    val message: String,
    val lessonDiagnostics: List<LessonDiagnostic> = emptyList(),
)

class LessonCatalogSnapshot internal constructor(
    entries: List<LessonCatalogEntry>,
    val diagnostics: List<LessonCatalogDiagnostic>,
    private val runtimePackages: Map<LessonCatalogIdentity, LessonRuntimePackage>,
) {
    val entries: List<LessonCatalogEntry> = entries.sortedWith(ENTRY_ORDER)

    fun byLessonId(lessonId: String): List<LessonCatalogEntry> =
        entries.filter { it.identity.lessonId == lessonId }

    fun forAgeBand(ageBand: AgeBand): List<LessonCatalogEntry> =
        entries.filter { ageBand in it.ageBands }

    fun byCategory(categoryId: String): List<LessonCatalogEntry> =
        entries.filter { categoryId in it.categoryIds }

    fun bySkill(skillId: String): List<LessonCatalogEntry> =
        entries.filter { skillId in it.skillIds }

    fun byDifficulty(difficulty: Int): List<LessonCatalogEntry> =
        entries.filter { it.difficulty == difficulty }

    fun byJourney(journeyId: String): List<LessonCatalogEntry> =
        entries.filter { journeyId in it.journeyIds }

    fun supporting(mode: TeachingMode): List<LessonCatalogEntry> =
        entries.filter { mode in it.supportedModes }

    internal fun runtimePackage(identity: LessonCatalogIdentity): LessonRuntimePackage? =
        runtimePackages[identity]

    internal fun firstReleaseLoadResult(): LessonLoadResult {
        val entry = entries.firstOrNull()
            ?: return LessonLoadResult.Failure(
                listOf(
                    LessonDiagnostic(
                        code = LessonDiagnosticCode.INVALID_VALUE,
                        path = "catalog",
                        message = diagnostics.firstOrNull()?.message ?: "No release lesson is available.",
                    ),
                ),
            )
        return LessonLoadResult.Success(checkNotNull(runtimePackages[entry.identity]))
    }

    private companion object {
        val ENTRY_ORDER = compareBy<LessonCatalogEntry>(
            { it.identity.lessonId },
            { it.identity.revision },
            { it.packageRoot },
        )
    }
}

/**
 * Deterministic, offline catalog layered over [LessonPackageLoader].
 *
 * Package decoding/lesson validation remains owned by LessonPackageLoader. The catalog adds
 * discovery, declared-asset/default-localization integrity, duplicate isolation and metadata queries.
 */
class LessonCatalog(
    private val source: LessonCatalogSource,
    private val catalogRoot: String = DEFAULT_CATALOG_ROOT,
    private val loader: LessonPackageLoader = LessonPackageLoader(source),
    private val json: Json = LessonPackageLoader.DEFAULT_JSON,
) {
    fun load(): LessonCatalogSnapshot {
        val children = source.list(catalogRoot)
            ?: return LessonCatalogSnapshot(
                entries = emptyList(),
                diagnostics = listOf(
                    LessonCatalogDiagnostic(
                        code = LessonCatalogDiagnosticCode.ROOT_UNAVAILABLE,
                        packageRoot = catalogRoot,
                        message = "Bundled lesson catalog root is unavailable: $catalogRoot",
                    ),
                ),
                runtimePackages = emptyMap(),
            )

        val diagnostics = mutableListOf<LessonCatalogDiagnostic>()
        val candidates = mutableListOf<Candidate>()

        children
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .sorted()
            .forEach { child ->
                val packageRoot = "$catalogRoot/$child"
                when (val result = loader.load(packageRoot)) {
                    is LessonLoadResult.Failure -> diagnostics += LessonCatalogDiagnostic(
                        code = LessonCatalogDiagnosticCode.PACKAGE_INVALID,
                        packageRoot = packageRoot,
                        message = "Lesson package failed validation.",
                        lessonDiagnostics = result.diagnostics,
                    )

                    is LessonLoadResult.Success -> {
                        val packageData = result.packageData
                        if (packageData.lesson.status != LessonStatus.RELEASE) return@forEach

                        val integrity = validateDeclaredAssets(packageData)
                        if (integrity.isNotEmpty()) {
                            diagnostics += integrity
                        } else {
                            val strings = readStrings(
                                packageRoot = packageData.packageRoot,
                                relativePath = checkNotNull(packageData.lesson.assets.strings[DEFAULT_LOCALE]),
                            ) ?: return@forEach
                            candidates += Candidate(
                                entry = packageData.toCatalogEntry(strings),
                                packageData = packageData,
                            )
                        }
                    }
                }
            }

        val duplicateIds = candidates
            .groupBy { it.entry.identity.lessonId }
            .filterValues { it.size > 1 }
            .keys
            .sorted()
        duplicateIds.forEach { lessonId ->
            val roots = candidates
                .filter { it.entry.identity.lessonId == lessonId }
                .map { it.entry.packageRoot }
                .sorted()
            diagnostics += LessonCatalogDiagnostic(
                code = LessonCatalogDiagnosticCode.DUPLICATE_LESSON_ID,
                packageRoot = null,
                message = "Duplicate release lesson ID '$lessonId' in ${roots.joinToString()}.",
            )
        }

        var accepted = candidates.filterNot { it.entry.identity.lessonId in duplicateIds }
        val availableIds = accepted.map { it.entry.identity.lessonId }.toSet()
        val missingReferenceRoots = mutableSetOf<String>()
        accepted.forEach { candidate ->
            val missing = candidate.packageData.lesson.metadata.prerequisiteLessonIds
                .filterNot { it in availableIds }
                .distinct()
                .sorted()
            if (missing.isNotEmpty()) {
                missingReferenceRoots += candidate.entry.packageRoot
                diagnostics += LessonCatalogDiagnostic(
                    code = LessonCatalogDiagnosticCode.MISSING_CATALOG_REFERENCE,
                    packageRoot = candidate.entry.packageRoot,
                    message = "Missing prerequisite lesson reference(s): ${missing.joinToString()}.",
                )
            }
        }
        accepted = accepted.filterNot { it.entry.packageRoot in missingReferenceRoots }

        val entries = accepted.map(Candidate::entry)
        val packages = accepted.associate { it.entry.identity to it.packageData }
        return LessonCatalogSnapshot(
            entries = entries,
            diagnostics = diagnostics.sortedWith(
                compareBy<LessonCatalogDiagnostic>(
                    { it.code.name },
                    { it.packageRoot ?: "" },
                    { it.message },
                ),
            ),
            runtimePackages = packages,
        )
    }

    private fun validateDeclaredAssets(packageData: LessonRuntimePackage): List<LessonCatalogDiagnostic> {
        val lesson = packageData.lesson
        val root = packageData.packageRoot
        val diagnostics = mutableListOf<LessonCatalogDiagnostic>()

        fun requireAsset(relativePath: String, label: String) {
            val path = join(root, relativePath)
            if (!source.exists(path)) {
                diagnostics += LessonCatalogDiagnostic(
                    code = LessonCatalogDiagnosticCode.MISSING_DECLARED_ASSET,
                    packageRoot = root,
                    message = "$label is missing: $path",
                )
            }
        }

        requireAsset(lesson.assets.thumbnail, "Thumbnail")
        requireAsset(lesson.assets.preview, "Preview")
        lesson.assets.audio.values.forEach { requireAsset(it, "Audio asset") }
        lesson.assets.coloringRegions?.let { requireAsset(it, "Coloring regions") }

        val defaultStringsPath = lesson.assets.strings[DEFAULT_LOCALE]
        if (defaultStringsPath == null) {
            diagnostics += LessonCatalogDiagnostic(
                code = LessonCatalogDiagnosticCode.INVALID_LOCALIZATION,
                packageRoot = root,
                message = "Release lesson must declare default '$DEFAULT_LOCALE' strings.",
            )
        }

        lesson.assets.strings.toSortedMap().forEach { (locale, relativePath) ->
            val path = join(root, relativePath)
            if (!source.exists(path)) {
                diagnostics += LessonCatalogDiagnostic(
                    code = LessonCatalogDiagnosticCode.MISSING_DECLARED_ASSET,
                    packageRoot = root,
                    message = "Strings file for '$locale' is missing: $path",
                )
                return@forEach
            }
            if (readStrings(root, relativePath) == null) {
                diagnostics += LessonCatalogDiagnostic(
                    code = LessonCatalogDiagnosticCode.INVALID_LOCALIZATION,
                    packageRoot = root,
                    message = "Strings file for '$locale' is not a valid string map: $path",
                )
            }
        }

        if (defaultStringsPath != null) {
            val defaultStrings = readStrings(root, defaultStringsPath)
            if (defaultStrings != null) {
                requiredStringKeys(packageData).forEach { key ->
                    if (defaultStrings[key].isNullOrBlank()) {
                        diagnostics += LessonCatalogDiagnostic(
                            code = LessonCatalogDiagnosticCode.MISSING_LOCALIZATION_KEY,
                            packageRoot = root,
                            message = "Default strings are missing authored key '$key'.",
                        )
                    }
                }
            }
        }
        return diagnostics
    }

    private fun requiredStringKeys(packageData: LessonRuntimePackage): Set<String> = buildSet {
        val lesson = packageData.lesson
        add(lesson.metadata.titleKey)
        add(lesson.metadata.summaryKey)
        lesson.drawing.steps.forEach { step ->
            step.teacher.narrationKey?.let(::add)
            step.completionNarrationKey?.let(::add)
            step.help.forEach { help -> help.narrationKey?.let(::add) }
        }
        lesson.coloring?.steps?.forEach { step -> step.narrationKey?.let(::add) }
    }

    private fun readStrings(packageRoot: String, relativePath: String): Map<String, String>? {
        val text = source.readText(join(packageRoot, relativePath)) ?: return null
        return try {
            json.decodeFromString<Map<String, String>>(text)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun LessonRuntimePackage.toCatalogEntry(strings: Map<String, String>): LessonCatalogEntry {
        val lesson = lesson
        return LessonCatalogEntry(
            identity = LessonCatalogIdentity(lesson.lessonId, lesson.revision),
            packageRoot = packageRoot,
            title = strings[lesson.metadata.titleKey] ?: lesson.lessonId.toDisplayTitle(),
            summary = strings[lesson.metadata.summaryKey] ?: "A calm step-by-step drawing lesson.",
            ageBands = lesson.metadata.ageBands.toSet(),
            difficulty = lesson.metadata.difficulty,
            estimatedMinutes = lesson.metadata.estimatedMinutes,
            categoryIds = lesson.metadata.categoryIds.toSet(),
            skillIds = lesson.metadata.skillIds.toSet(),
            journeyIds = lesson.metadata.journeyIds.toSet(),
            supportedModes = lesson.supportedModes.toSet(),
            thumbnailPath = join(packageRoot, lesson.assets.thumbnail),
            previewPath = join(packageRoot, lesson.assets.preview),
        )
    }

    private fun join(root: String, relative: String): String =
        "${root.trimEnd('/')}/${relative.trimStart('/')}"

    private fun String.toDisplayTitle(): String = split('-', '_')
        .filter(String::isNotBlank)
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }

    private data class Candidate(
        val entry: LessonCatalogEntry,
        val packageData: LessonRuntimePackage,
    )

    companion object {
        const val DEFAULT_CATALOG_ROOT = "lessons"
        const val DEFAULT_LOCALE = "en"
    }
}
