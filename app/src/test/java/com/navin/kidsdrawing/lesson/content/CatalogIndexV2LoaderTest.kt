package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogIndexV2LoaderTest {
    @Test
    fun validIndexLoadsDeterministicallyWithoutTeachingPackages() {
        val index = source(
            listOf(
                entry("zebra-lines", category = "animals", skill = "line-control"),
                entry(
                    "apple-shapes",
                    category = "food",
                    skill = "basic-shapes",
                    collectionIds = listOf("starter-fun"),
                    contentFamilyId = "fruit-family",
                    helpAvailable = true,
                ),
            ),
        )

        val result = load(index)
        assertTrue(result is CatalogIndexV2LoadResult.Success)
        val snapshot = (result as CatalogIndexV2LoadResult.Success).snapshot

        assertEquals(listOf("apple-shapes", "zebra-lines"), snapshot.entries.map { it.identity.lessonId })
        assertEquals(listOf("apple-shapes"), snapshot.byCategory("food").map { it.identity.lessonId })
        assertEquals(listOf("apple-shapes"), snapshot.bySkill("basic-shapes").map { it.identity.lessonId })
        assertEquals(listOf("apple-shapes"), snapshot.byCollection("starter-fun").map { it.identity.lessonId })
        assertEquals(listOf("apple-shapes"), snapshot.byContentFamily("fruit-family").map { it.identity.lessonId })
        assertEquals(listOf("apple-shapes"), snapshot.withHelp().map { it.identity.lessonId })
    }

    @Test
    fun draftEntriesAreValidatedButNotExposedToRuntimeDiscovery() {
        val result = load(
            source(
                listOf(
                    entry("release-one"),
                    entry("draft-one", status = LessonStatus.DRAFT),
                ),
            ),
        ) as CatalogIndexV2LoadResult.Success

        assertEquals(listOf("release-one"), result.snapshot.entries.map { it.identity.lessonId })
    }

    @Test
    fun capabilitySummaryCannotAdvertiseTraceWithoutTraceMode() {
        val invalid = entry("trace-mismatch").copy(
            capabilitySummary = CatalogCapabilitySummarySource(
                teachingModes = listOf(TeachingMode.DRAW_WITH_ME),
                helpAvailable = false,
                traceReady = true,
                coloring = CatalogColoringCapability.NONE,
            ),
        )

        val result = load(source(listOf(invalid))) as CatalogIndexV2LoadResult.Failure
        assertTrue(result.diagnostics.any { it.code == CatalogIndexV2DiagnosticCode.INVALID_ENTRY })
    }

    @Test
    fun unsupportedVoiceCapabilityFailsClosed() {
        val invalid = entry("voice-claim").copy(
            capabilitySummary = entry("voice-claim").capabilitySummary.copy(
                voiceAudio = CatalogVoiceAudioCapability.READY,
            ),
        )

        val result = load(source(listOf(invalid))) as CatalogIndexV2LoadResult.Failure
        assertTrue(result.diagnostics.any { it.message.contains("voiceAudio READY") })
    }

    @Test
    fun missingPrerequisiteFailsClosed() {
        val result = load(
            source(
                listOf(
                    entry("second-lesson", prerequisiteLessonIds = listOf("missing-first")),
                ),
            ),
        ) as CatalogIndexV2LoadResult.Failure

        assertTrue(result.diagnostics.any { it.code == CatalogIndexV2DiagnosticCode.MISSING_PREREQUISITE })
    }

    @Test
    fun prerequisiteCycleFailsClosed() {
        val result = load(
            source(
                listOf(
                    entry("lesson-alpha", prerequisiteLessonIds = listOf("lesson-beta")),
                    entry("lesson-beta", prerequisiteLessonIds = listOf("lesson-alpha")),
                ),
            ),
        ) as CatalogIndexV2LoadResult.Failure

        assertTrue(result.diagnostics.any { it.code == CatalogIndexV2DiagnosticCode.PREREQUISITE_CYCLE })
    }

    @Test
    fun duplicateReleaseLessonIdentityFailsClosed() {
        val result = load(
            source(
                listOf(
                    entry("same-lesson"),
                    entry("same-lesson"),
                ),
            ),
        ) as CatalogIndexV2LoadResult.Failure

        assertTrue(result.diagnostics.any { it.code == CatalogIndexV2DiagnosticCode.DUPLICATE_IDENTITY })
    }

    @Test
    fun synthetic100EntryIndexIsStable() = assertSyntheticScale(100)

    @Test
    fun synthetic250EntryIndexIsStable() = assertSyntheticScale(250)

    @Test
    fun synthetic500EntryIndexIsStable() = assertSyntheticScale(500)

    private fun assertSyntheticScale(size: Int) {
        val entries = (0 until size).map { index ->
            entry(
                lessonId = "lesson-${index.toString().padStart(3, '0')}",
                category = "category-${index % 10}",
                skill = "skill-${index % 15}",
                collectionIds = listOf("collection-${index % 5}"),
                contentFamilyId = "family-${index % 20}",
            )
        }.reversed()

        val result = load(source(entries)) as CatalogIndexV2LoadResult.Success
        val snapshot = result.snapshot

        assertEquals(size, snapshot.entries.size)
        assertEquals("lesson-000", snapshot.entries.first().identity.lessonId)
        assertEquals("lesson-${(size - 1).toString().padStart(3, '0')}", snapshot.entries.last().identity.lessonId)
        assertEquals(size / 10, snapshot.byCategory("category-0").size)
        assertTrue(snapshot.entries.all { it.drawingStepCount == 3 })
    }

    private fun load(index: CatalogIndexV2Source): CatalogIndexV2LoadResult {
        val json = LessonPackageLoader.DEFAULT_JSON.encodeToString(index)
        val packageSource = LessonPackageSource { path ->
            if (path == CatalogIndexV2Loader.DEFAULT_INDEX_PATH) json else null
        }
        return CatalogIndexV2Loader(packageSource).load()
    }

    private fun source(entries: List<CatalogIndexV2EntrySource>) = CatalogIndexV2Source(
        schemaVersion = CatalogIndexV2Loader.SCHEMA_VERSION,
        contentApi = LessonPackageLoader.CURRENT_CONTENT_API,
        entries = entries,
    )

    private fun entry(
        lessonId: String,
        status: LessonStatus = LessonStatus.RELEASE,
        category: String = "drawing-basics",
        skill: String = "line-control",
        collectionIds: List<String> = emptyList(),
        contentFamilyId: String? = null,
        prerequisiteLessonIds: List<String> = emptyList(),
        helpAvailable: Boolean = false,
    ) = CatalogIndexV2EntrySource(
        lessonId = lessonId,
        revision = 1,
        status = status,
        minimumContentApi = 1,
        packageRef = "lessons/$lessonId",
        title = lessonId,
        titleKey = "lesson.title",
        summary = "Practice drawing $lessonId.",
        summaryKey = "lesson.summary",
        ageBands = listOf(AgeBand.CREATIVE_EXPLORERS),
        difficulty = 2,
        estimatedMinutes = 8,
        drawingStepCount = 3,
        categoryIds = listOf(category),
        skillIds = listOf(skill),
        journeyIds = emptyList(),
        collectionIds = collectionIds,
        prerequisiteLessonIds = prerequisiteLessonIds,
        tags = listOf("practice"),
        contentFamilyId = contentFamilyId,
        supportedModes = listOf(TeachingMode.DRAW_WITH_ME),
        capabilitySummary = CatalogCapabilitySummarySource(
            teachingModes = listOf(TeachingMode.DRAW_WITH_ME),
            helpAvailable = helpAvailable,
            traceReady = false,
            coloring = CatalogColoringCapability.NONE,
        ),
        thumbnailRef = "lessons/$lessonId/thumb.webp",
        previewRef = "lessons/$lessonId/preview.webp",
    )
}
