package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionCatalogIndexV2ProjectionTest {
    @Test
    fun currentReleaseCatalogMatchesCommittedValidatedMetadataOnlyIndex() {
        val assetRoot = File("src/main/assets")
        val source = FileAssetCatalogSource(assetRoot)
        val legacySnapshot = LessonCatalog(source).load()

        assertTrue("Catalog diagnostics: ${legacySnapshot.diagnostics}", legacySnapshot.diagnostics.isEmpty())
        assertEquals("V2.4 Butterfly pilot must contain exactly 28 release lessons.", 28, legacySnapshot.entries.size)

        val projection = CatalogIndexV2Projector.project(legacySnapshot)
        assertTrue("Projection failed: $projection", projection is CatalogIndexV2ProjectionResult.Success)
        val index = (projection as CatalogIndexV2ProjectionResult.Success).index
        assertEquals(28, index.entries.size)

        val rendered = CatalogIndexV2Projector.render(index)
        val outputDir = File("build/reports/content-quality").apply { mkdirs() }
        val output = File(outputDir, "generated-lesson-index-v2.json")
        output.writeText(rendered)
        assertTrue(output.isFile && output.length() > 0L)

        val committedIndex = File(assetRoot, CatalogIndexV2Loader.DEFAULT_INDEX_PATH)
        assertTrue("Committed Catalog Index V2 is missing.", committedIndex.isFile)
        assertEquals(
            "Committed Catalog Index V2 drifted from the fully validated release packages. Regenerate it; do not hand-edit capability truth.",
            rendered,
            committedIndex.readText(),
        )

        val loaded = CatalogIndexV2Loader(source).load()
        assertTrue("Committed index must pass strict taxonomy/index validation: $loaded", loaded is CatalogIndexV2LoadResult.Success)
        val snapshot = (loaded as CatalogIndexV2LoadResult.Success).snapshot
        assertEquals(28, snapshot.entries.size)
        assertTrue(snapshot.entries.none { it.packageRef.isBlank() })
        assertTrue("Frozen taxonomy registry itself must be valid.", CatalogTaxonomyV2.registry.diagnostics.isEmpty())

        val ageBands = snapshot.entries.flatMap { it.ageBands }.distinct().sortedBy { it.name }
        val categoryIds = snapshot.entries.flatMap { it.categoryIds }.distinct().sorted()
        val skillIds = snapshot.entries.flatMap { it.skillIds }.distinct().sorted()
        val journeyIds = snapshot.entries.flatMap { it.journeyIds }.distinct().sorted()
        val collectionIds = snapshot.entries.flatMap { it.collectionIds }.distinct().sorted()
        val familyIds = snapshot.entries.mapNotNull { it.contentFamilyId }.distinct().sorted()

        val health = buildString {
            appendLine("Content Library V2 catalog-index projection")
            appendLine("releaseEntries=${snapshot.entries.size}")
            appendLine("ageBands=${ageBands.joinToString()}")
            appendLine("categories=${categoryIds.size}")
            appendLine("skills=${skillIds.size}")
            appendLine("journeys=${journeyIds.size}")
            appendLine("collections=${collectionIds.size}")
            appendLine("contentFamilies=${familyIds.size}")
            (1..5).forEach { difficulty ->
                appendLine("difficulty.$difficulty=${snapshot.byDifficulty(difficulty).size}")
            }
            TeachingMode.entries.forEach { mode ->
                appendLine("mode.${mode.name.lowercase()}=${snapshot.supporting(mode).size}")
            }
            appendLine("helpReady=${snapshot.withHelp().size}")
            appendLine("traceReady=${snapshot.traceReady().size}")
            CatalogColoringCapability.entries.forEach { capability ->
                appendLine("coloring.${capability.name.lowercase()}=${snapshot.withColoring(capability).size}")
            }
            journeyIds.forEach { journeyId ->
                appendLine("journey.$journeyId.size=${snapshot.byJourney(journeyId).size}")
            }
            collectionIds.forEach { collectionId ->
                appendLine("collection.$collectionId.size=${snapshot.byCollection(collectionId).size}")
            }
            familyIds.forEach { familyId ->
                appendLine("contentFamily.$familyId.size=${snapshot.byContentFamily(familyId).size}")
            }
            appendLine("taxonomy.registered.categories=${CatalogTaxonomyV2.registry.definitions(CatalogTaxonomyKind.CATEGORY).size}")
            appendLine("taxonomy.registered.skills=${CatalogTaxonomyV2.registry.definitions(CatalogTaxonomyKind.SKILL).size}")
            appendLine("taxonomy.registered.journeys=${CatalogTaxonomyV2.registry.definitions(CatalogTaxonomyKind.JOURNEY).size}")
            appendLine("taxonomy.registered.collections=${CatalogTaxonomyV2.registry.definitions(CatalogTaxonomyKind.COLLECTION).size}")
            appendLine("taxonomy.registered.contentFamilies=${CatalogTaxonomyV2.registry.definitions(CatalogTaxonomyKind.CONTENT_FAMILY).size}")
            appendLine("unknownTaxonomyReferences=0")
            appendLine("duplicateIdentities=0")
            appendLine("prerequisiteGraphFailures=0")
            appendLine("committedIndexDrift=0")
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
