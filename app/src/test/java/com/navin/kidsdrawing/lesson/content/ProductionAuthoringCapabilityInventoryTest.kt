package com.navin.kidsdrawing.lesson.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Evidence report for Content Library V2 authoring design.
 *
 * Some schema fields pre-date a complete product/runtime behavior. This inventory deliberately does
 * not pretend those fields are supported capabilities. It records how release content actually uses
 * them so Content Studio semantics can be designed from evidence instead of assumptions.
 */
class ProductionAuthoringCapabilityInventoryTest {
    @Test
    fun releaseCatalogEmitsDeterministicAuthoringCapabilityInventory() {
        val snapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
        assertTrue("Catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())
        assertEquals(24, snapshot.entries.size)

        val toolPresets = mutableListOf<FieldUse>()
        val groupedTeacherDemos = mutableListOf<FieldUse>()
        val teacherNarration = mutableListOf<FieldUse>()
        val completionNarration = mutableListOf<FieldUse>()
        val helpNarration = mutableListOf<FieldUse>()
        val coloringNarration = mutableListOf<FieldUse>()
        val suggestedColorRoles = mutableListOf<FieldUse>()
        val enforcedSuggestedColors = mutableListOf<FieldUse>()
        val audioAssets = mutableListOf<FieldUse>()

        snapshot.entries.forEach { entry ->
            val packageData = checkNotNull(snapshot.runtimePackage(entry.identity))
            val lesson = packageData.lesson
            val lessonRef = "${entry.identity.lessonId}@${entry.identity.revision}"

            lesson.drawing.steps.forEach { step ->
                val stepRef = "$lessonRef/${step.id}"
                step.childTurn.toolPreset?.takeIf(String::isNotBlank)?.let { value ->
                    toolPresets += FieldUse(stepRef, value)
                }
                if (step.teacher.playAsGroup) {
                    groupedTeacherDemos += FieldUse(stepRef, step.teacher.strokeRefs.joinToString(","))
                }
                step.teacher.narrationKey?.takeIf(String::isNotBlank)?.let { value ->
                    teacherNarration += FieldUse(stepRef, value)
                }
                step.completionNarrationKey?.takeIf(String::isNotBlank)?.let { value ->
                    completionNarration += FieldUse(stepRef, value)
                }
                step.help.forEach { help ->
                    help.narrationKey?.takeIf(String::isNotBlank)?.let { value ->
                        helpNarration += FieldUse("$stepRef/help-${help.level}", value)
                    }
                }
            }

            lesson.coloring?.steps.orEmpty().forEach { step ->
                val stepRef = "$lessonRef/coloring-${step.id}"
                step.narrationKey?.takeIf(String::isNotBlank)?.let { value ->
                    coloringNarration += FieldUse(stepRef, value)
                }
                if (step.suggestedColorRoles.isNotEmpty()) {
                    suggestedColorRoles += FieldUse(stepRef, step.suggestedColorRoles.joinToString(","))
                }
                if (step.enforceSuggestedColors) {
                    enforcedSuggestedColors += FieldUse(stepRef, "true")
                }
            }

            lesson.assets.audio.toSortedMap().forEach { (key, path) ->
                audioAssets += FieldUse("$lessonRef/audio-$key", path)
            }
        }

        // The stricter capability gate already rejects this runtime-unsupported behavior. Keeping
        // the inventory assertion here makes accidental release use visible in two independent gates.
        assertTrue(
            "Release content must not enforce suggested colors until runtime support exists: $enforcedSuggestedColors",
            enforcedSuggestedColors.isEmpty(),
        )

        val report = buildString {
            appendLine("Content Library V2 authoring capability inventory")
            appendLine("lessons=${snapshot.entries.size}")
            appendSection("toolPreset", toolPresets)
            appendSection("playAsGroup", groupedTeacherDemos)
            appendSection("teacherNarrationKey", teacherNarration)
            appendSection("completionNarrationKey", completionNarration)
            appendSection("helpNarrationKey", helpNarration)
            appendSection("coloringNarrationKey", coloringNarration)
            appendSection("suggestedColorRoles", suggestedColorRoles)
            appendSection("enforceSuggestedColors", enforcedSuggestedColors)
            appendSection("audioAssets", audioAssets)
        }.trimEnd()

        val outputDir = File("build/reports/content-quality").apply { mkdirs() }
        val output = File(outputDir, "authoring-capability-inventory.txt")
        output.writeText(report + "\n")
        assertTrue(output.isFile && output.length() > 0L)
        println(report)
    }

    private fun StringBuilder.appendSection(name: String, uses: List<FieldUse>) {
        val ordered = uses.sortedWith(compareBy(FieldUse::owner, FieldUse::value))
        appendLine("[$name] count=${ordered.size} distinctValues=${ordered.map { it.value }.distinct().size}")
        ordered.forEach { use -> appendLine("${use.owner} = ${use.value}") }
    }

    private data class FieldUse(
        val owner: String,
        val value: String,
    )

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
