package com.navin.kidsdrawing.product.home

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2LoadResult
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Loader
import com.navin.kidsdrawing.lesson.content.LessonCatalog
import com.navin.kidsdrawing.lesson.content.LessonCatalogSource
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogIndexV2StudioProjectionTest {
    @Test
    fun metadataOnlyProjectionMatchesLegacyFullPackageRecommendationsForAllProfiles() {
        val source = FileAssetCatalogSource(File("src/main/assets"))
        val legacy = LessonCatalog(source).load()
        assertTrue("Legacy catalog diagnostics: ${legacy.diagnostics}", legacy.diagnostics.isEmpty())

        val indexResult = CatalogIndexV2Loader(source).load()
        assertTrue("V2 index failed: $indexResult", indexResult is CatalogIndexV2LoadResult.Success)
        val index = (indexResult as CatalogIndexV2LoadResult.Success).snapshot
        assertEquals(legacy.entries.map { it.identity }, index.entries.map { it.identity })

        val interestSets = listOf(emptySet()) + ChildInterest.entries.map { setOf(it) }
        AgeBand.entries.forEach { ageBand ->
            TeachingMode.entries.forEach { preferredMode ->
                interestSets.forEach { interests ->
                    val profile = ChildProfile(
                        nickname = "Parity",
                        ageBand = ageBand,
                        teachingMode = preferredMode,
                        pace = TeachingPace.NORMAL,
                        interests = interests,
                        handedness = Handedness.RIGHT,
                        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
                    )

                    index.entries.forEach { indexEntry ->
                        val legacyEntry = legacy.entries.first { it.identity == indexEntry.identity }
                        val packageData = checkNotNull(legacy.runtimePackage(indexEntry.identity))
                        val expected = StudioRecommendationPolicy.recommend(
                            profile = profile,
                            lesson = packageData.lesson,
                            title = legacyEntry.title,
                            summary = legacyEntry.summary,
                        )
                        val actual = indexEntry.toStudioRecommendation(profile)

                        assertEquals(
                            "Discovery parity failed for ${indexEntry.identity} / $ageBand / $preferredMode / $interests",
                            expected,
                            actual,
                        )
                    }
                }
            }
        }
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
