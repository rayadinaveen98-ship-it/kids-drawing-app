package com.navin.kidsdrawing.lesson.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionCatalogIndexV2ProjectionTest {
    @Test
    fun currentReleaseCatalogProjectsToValidatedMetadataOnlyIndex() {
        val assetRoot = File("src/main/assets")
        val source = FileAssetCatalogSource(assetRoot)
        val legacySnapshot = LessonCatalog(source).load()

        assertTrue("Catalog diagnostics: ${legacySnapshot.diagnostics}", legacySnapshot.diagnostics.isEmpty())
        assertEquals("V2.2 must not expand lesson count.", 24, legacySnapshot.entries.size)

        val projection = CatalogIndexV2Projector.project(legacySnapshot)
        assertTrue("Projection failed: $projection", projection is CatalogIndexV2ProjectionResult.Success)
        val index = (projection as CatalogIndexV2ProjectionResult.Success).index
        assertEquals(24, index.entries.size)

        val rendered = CatalogIndexV2Projector.render(index)
        val generatedSource = LessonPackageSource { path ->
            if (path == CatalogIndexV2Loader.DEFAULT_INDEX_PATH) rendered else null
        }
        val loaded = CatalogIndexV2Loader(generatedSource).load()
        assertTrue("Generated index must validate: $loaded", loaded is CatalogIndexV2LoadResult.Success)
        val snapshot = (loaded as CatalogIndexV2LoadResult.Success).snapshot
        assertEquals(24, snapshot.entries.size)
        assertTrue(snapshot.entries.none { it.packageRef.isBlank() })

        val outputDir = File("build/reports/content-quality").apply { mkdirs() }
        val output = File(outputDir, "generated-lesson-index-v2.json")
        output.writeText(rendered)
        assertTrue(output.isFile && output.length() > 0L)

        val health = buildString {
            appendLine("Content Library V2 catalog-index projection")
            appendLine("releaseEntries=${snapshot.entries.size}")
            appendLine("ageBands=${snapshot.entries.flatMap { it.ageBands }.distinct().sortedBy { it.name }.joinToString()}")
            appendLine("categories=${snapshot.entries.flatMap { it.categoryIds }.distinct().sorted().size}")
            appendLine("skills=${snapshot.entries.flatMap { it.skillIds }.distinct().sorted().size}")
            appendLine("journeys=${snapshot.entries.flatMap { it.journeyIds }.distinct().sorted().size}")
            appendLine("collections=${snapshot.entries.flatMap { it.collectionIds }.distinct().sorted().size}")
            appendLine("contentFamilies=${snapshot.entries.mapNotNull { it.contentFamilyId }.distinct().sorted().size}")
            appendLine("helpReady=${snapshot.withHelp().size}")
            appendLine("traceReady=${snapshot.traceReady().size}")
            CatalogColoringCapability.entries.forEach { capability ->
                appendLine("coloring.${capability.name.lowercase()}=${snapshot.withColoring(capability).size}")
            }
        }.trimEnd() + "\n"
        File(outputDir, "catalog-index-v2-health.txt").writeText(health)
        println(health)
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
