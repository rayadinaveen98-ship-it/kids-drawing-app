package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonCatalogTest {
    @Test
    fun threeValidPackagesCoexistAndEnumerationIsDeterministic() {
        val source = fixtureSource(
            specs = listOf(
                Spec("gamma-root", "gamma-lesson"),
                Spec("alpha-root", "alpha-lesson"),
                Spec("beta-root", "beta-lesson"),
            ),
            discoveryOrder = listOf("gamma-root", "beta-root", "alpha-root"),
        )

        val snapshot = LessonCatalog(source).load()

        assertEquals(listOf("alpha-lesson", "beta-lesson", "gamma-lesson"), snapshot.entries.map { it.identity.lessonId })
        assertTrue(snapshot.diagnostics.isEmpty())
        assertTrue(snapshot.firstReleaseLoadResult() is LessonLoadResult.Success)
    }

    @Test
    fun invalidPackageIsIsolatedFromValidPackages() {
        val source = fixtureSource(
            specs = listOf(Spec("alpha", "alpha-lesson"), Spec("beta", "beta-lesson"), Spec("gamma", "gamma-lesson")),
            discoveryOrder = listOf("broken", "gamma", "alpha", "beta"),
        )

        val snapshot = LessonCatalog(source).load()

        assertEquals(3, snapshot.entries.size)
        assertTrue(
            snapshot.diagnostics.any {
                it.code == LessonCatalogDiagnosticCode.PACKAGE_INVALID && it.packageRoot == "lessons/broken"
            },
        )
    }

    @Test
    fun duplicateReleaseLessonIdsAreAllRejectedRegardlessOfDiscoveryOrder() {
        fun load(order: List<String>): LessonCatalogSnapshot {
            val source = fixtureSource(
                specs = listOf(
                    Spec("first", "same-lesson"),
                    Spec("second", "same-lesson"),
                    Spec("safe", "safe-lesson"),
                ),
                discoveryOrder = order,
            )
            return LessonCatalog(source).load()
        }

        val first = load(listOf("first", "safe", "second"))
        val second = load(listOf("second", "first", "safe"))

        assertEquals(listOf("safe-lesson"), first.entries.map { it.identity.lessonId })
        assertEquals(first.entries, second.entries)
        assertTrue(first.diagnostics.any { it.code == LessonCatalogDiagnosticCode.DUPLICATE_LESSON_ID })
        assertEquals(first.diagnostics, second.diagnostics)
    }

    @Test
    fun metadataQueriesReturnStableSubsets() {
        val source = fixtureSource(
            specs = listOf(
                Spec(
                    root = "little",
                    lessonId = "little-lines",
                    ageBandsJson = "[\"little_artists\"]",
                    difficulty = 1,
                    categoriesJson = "[\"foundations\"]",
                    skillsJson = "[\"line_control\"]",
                    journeysJson = "[\"first_shapes_to_pictures\"]",
                    modesJson = "[\"draw_with_me\"]",
                ),
                Spec(
                    root = "watch",
                    lessonId = "watch-fish",
                    ageBandsJson = "[\"creative_explorers\"]",
                    difficulty = 2,
                    categoriesJson = "[\"animals\"]",
                    skillsJson = "[\"curves\"]",
                    journeysJson = "[\"animal_artist\"]",
                    modesJson = "[\"watch_then_draw\"]",
                ),
                Spec(
                    root = "older",
                    lessonId = "older-owl",
                    ageBandsJson = "[\"growing_artists\", \"young_artists\"]",
                    difficulty = 3,
                    categoriesJson = "[\"animals\"]",
                    skillsJson = "[\"proportion\"]",
                    journeysJson = "[\"animal_artist\"]",
                    modesJson = "[\"draw_with_me\"]",
                ),
            ),
        )

        val snapshot = LessonCatalog(source).load()

        assertEquals(listOf("little-lines"), snapshot.forAgeBand(AgeBand.LITTLE_ARTISTS).ids())
        assertEquals(listOf("older-owl", "watch-fish"), snapshot.byCategory("animals").ids())
        assertEquals(listOf("older-owl"), snapshot.bySkill("proportion").ids())
        assertEquals(listOf("older-owl", "watch-fish"), snapshot.byJourney("animal_artist").ids())
        assertEquals(listOf("watch-fish"), snapshot.supporting(TeachingMode.WATCH_THEN_DRAW).ids())
        assertEquals(listOf("older-owl"), snapshot.byDifficulty(3).ids())
        assertEquals(listOf("watch-fish"), snapshot.byLessonId("watch-fish").ids())
    }

    @Test
    fun missingDeclaredPreviewRejectsOnlyThatPackage() {
        val source = fixtureSource(
            specs = listOf(Spec("good", "good-lesson"), Spec("missing-preview", "bad-lesson")),
        )
        source.remove("lessons/missing-preview/preview.svg")

        val snapshot = LessonCatalog(source).load()

        assertEquals(listOf("good-lesson"), snapshot.entries.ids())
        assertTrue(
            snapshot.diagnostics.any {
                it.code == LessonCatalogDiagnosticCode.MISSING_DECLARED_ASSET &&
                    it.packageRoot == "lessons/missing-preview"
            },
        )
    }

    @Test
    fun missingAuthoredLocalizationKeyRejectsOnlyThatPackage() {
        val source = fixtureSource(
            specs = listOf(Spec("good", "good-lesson"), Spec("bad-strings", "bad-lesson")),
        )
        val path = "lessons/bad-strings/strings/en.json"
        val strings = Json.decodeFromString<Map<String, String>>(checkNotNull(source.readText(path))).toMutableMap()
        strings.remove("lesson.cute_cat.step.head")
        source.put(path, Json.encodeToString(strings))

        val snapshot = LessonCatalog(source).load()

        assertEquals(listOf("good-lesson"), snapshot.entries.ids())
        assertTrue(
            snapshot.diagnostics.any {
                it.code == LessonCatalogDiagnosticCode.MISSING_LOCALIZATION_KEY &&
                    it.packageRoot == "lessons/bad-strings"
            },
        )
    }

    @Test
    fun draftAndReviewPackagesAreNotExposedToChildCatalog() {
        val source = fixtureSource(
            specs = listOf(
                Spec("release", "release-lesson", status = "release"),
                Spec("draft", "draft-lesson", status = "draft"),
                Spec("review", "review-lesson", status = "review"),
            ),
        )

        val snapshot = LessonCatalog(source).load()

        assertEquals(listOf("release-lesson"), snapshot.entries.ids())
        assertTrue(snapshot.diagnostics.isEmpty())
    }

    @Test
    fun missingPrerequisiteIsTypedCatalogReferenceFailure() {
        val source = fixtureSource(
            specs = listOf(
                Spec("safe", "safe-lesson"),
                Spec("blocked", "blocked-lesson", prerequisitesJson = "[\"not-installed\"]"),
            ),
        )

        val snapshot = LessonCatalog(source).load()

        assertEquals(listOf("safe-lesson"), snapshot.entries.ids())
        assertTrue(snapshot.diagnostics.any { it.code == LessonCatalogDiagnosticCode.MISSING_CATALOG_REFERENCE })
    }

    private fun fixtureSource(
        specs: List<Spec>,
        discoveryOrder: List<String> = specs.map(Spec::root),
    ): MutableCatalogSource {
        val files = mutableMapOf<String, String>()
        specs.forEach { spec ->
            val root = "lessons/${spec.root}"
            files["$root/lesson.json"] = lessonJson(spec)
            files["$root/strokes.json"] = strokeJson()
            files["$root/thumbnail.svg"] = "<svg></svg>"
            files["$root/preview.svg"] = "<svg></svg>"
            files["$root/strings/en.json"] = stringsJson()
        }
        return MutableCatalogSource(files, discoveryOrder)
    }

    private fun lessonJson(spec: Spec): String = lessonTemplate()
        .replace("\"lessonId\": \"cute-cat\"", "\"lessonId\": \"${spec.lessonId}\"")
        .replace("\"status\": \"release\"", "\"status\": \"${spec.status}\"")
        .replace("\"ageBands\": [\"creative_explorers\", \"growing_artists\"]", "\"ageBands\": ${spec.ageBandsJson}")
        .replace("\"difficulty\": 1", "\"difficulty\": ${spec.difficulty}")
        .replace("\"categoryIds\": [\"animals\", \"pets\"]", "\"categoryIds\": ${spec.categoriesJson}")
        .replace("\"skillIds\": [\"curves\", \"shape_construction\", \"simple_details\"]", "\"skillIds\": ${spec.skillsJson}")
        .replace("\"journeyIds\": [\"animal_artist\"]", "\"journeyIds\": ${spec.journeysJson},\n    \"prerequisiteLessonIds\": ${spec.prerequisitesJson}")
        .replace("\"supportedModes\": [\"draw_with_me\", \"watch_then_draw\", \"trace_and_learn\"]", "\"supportedModes\": ${spec.modesJson}")

    private fun lessonTemplate(): String = File("src/main/assets/lessons/cute-cat/lesson.json").readText()

    private fun strokeJson(): String = File("src/main/assets/lessons/cute-cat/strokes.json").readText()

    private fun stringsJson(): String = File("src/main/assets/lessons/cute-cat/strings/en.json").readText()

    private fun List<LessonCatalogEntry>.ids(): List<String> = map { it.identity.lessonId }

    private data class Spec(
        val root: String,
        val lessonId: String,
        val status: String = "release",
        val ageBandsJson: String = "[\"creative_explorers\", \"growing_artists\"]",
        val difficulty: Int = 1,
        val categoriesJson: String = "[\"animals\", \"pets\"]",
        val skillsJson: String = "[\"curves\", \"shape_construction\", \"simple_details\"]",
        val journeysJson: String = "[\"animal_artist\"]",
        val prerequisitesJson: String = "[]",
        val modesJson: String = "[\"draw_with_me\", \"watch_then_draw\", \"trace_and_learn\"]",
    )

    private class MutableCatalogSource(
        private val files: MutableMap<String, String>,
        private val children: List<String>,
    ) : LessonCatalogSource {
        override fun readText(path: String): String? = files[path]

        override fun list(path: String): List<String>? = if (path == "lessons") children else emptyList()

        override fun exists(path: String): Boolean = path in files

        fun remove(path: String) {
            files.remove(path)
        }

        fun put(path: String, content: String) {
            files[path] = content
        }
    }
}
