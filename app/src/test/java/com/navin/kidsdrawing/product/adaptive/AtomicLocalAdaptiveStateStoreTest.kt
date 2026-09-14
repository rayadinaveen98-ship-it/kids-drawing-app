package com.navin.kidsdrawing.product.adaptive

import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AtomicLocalAdaptiveStateStoreTest {
    @Test
    fun missingStateIsTypedAndDoesNotFabricateHistory() = withTempDirectory { root ->
        val result = runBlocking { AtomicLocalAdaptiveStateStore(root).load() }
        assertTrue(result is AtomicLocalAdaptiveStateStore.LoadResult.Missing)
    }

    @Test
    fun saveLoadPreservesState() = withTempDirectory { root ->
        val store = AtomicLocalAdaptiveStateStore(root)
        val state = LocalAdaptiveReducer.reduce(
            LocalAdaptiveState.empty(),
            AdaptiveEvent.LessonCompleted(
                eventKey = "complete:a",
                lessonId = "cute-cat",
                lessonRevision = 1,
                skillIds = listOf("line.curve"),
                categoryIds = listOf("animals"),
                journeyIds = listOf("journey.animal_artist"),
                difficulty = 2,
            ),
        )

        runBlocking { store.save(state) }
        val loaded = runBlocking { store.load() }

        assertTrue(loaded is AtomicLocalAdaptiveStateStore.LoadResult.Loaded)
        loaded as AtomicLocalAdaptiveStateStore.LoadResult.Loaded
        assertEquals(AtomicLocalAdaptiveStateStore.LoadSource.PRIMARY, loaded.source)
        assertEquals(state, loaded.state)
    }

    @Test
    fun corruptPrimaryFallsBackToPreviousGoodBackup() = withTempDirectory { root ->
        val store = AtomicLocalAdaptiveStateStore(root)
        val first = LocalAdaptiveState(revision = 1, processedEventKeys = listOf("event:first"))
        val second = LocalAdaptiveState(revision = 2, processedEventKeys = listOf("event:first", "event:second"))
        runBlocking {
            store.save(first)
            store.save(second)
        }

        File(root, "adaptive_state.json").writeText("not-json")
        val loaded = runBlocking { store.load() }

        assertTrue(loaded is AtomicLocalAdaptiveStateStore.LoadResult.Loaded)
        loaded as AtomicLocalAdaptiveStateStore.LoadResult.Loaded
        assertEquals(AtomicLocalAdaptiveStateStore.LoadSource.BACKUP, loaded.source)
        assertEquals(first, loaded.state)
    }

    @Test
    fun incompatiblePrimaryNeverFallsBackToOlderBackup() = withTempDirectory { root ->
        val store = AtomicLocalAdaptiveStateStore(root)
        val first = LocalAdaptiveState(revision = 1, processedEventKeys = listOf("event:first"))
        val second = LocalAdaptiveState(revision = 2, processedEventKeys = listOf("event:first", "event:second"))
        runBlocking {
            store.save(first)
            store.save(second)
        }
        File(root, "adaptive_state.json").writeText(
            """{"formatVersion":44,"revision":3,"completedLessons":[],"skillExposureCounts":{},"recentCompletions":[],"helpRequestCounts":{},"processedEventKeys":[]}""",
        )

        val loaded = runBlocking { store.load() }

        assertTrue(loaded is AtomicLocalAdaptiveStateStore.LoadResult.Incompatible)
        loaded as AtomicLocalAdaptiveStateStore.LoadResult.Incompatible
        assertEquals(44, loaded.formatVersion)
    }

    @Test
    fun corruptOnlyCopyReturnsTypedCorruptOutcome() = withTempDirectory { root ->
        val store = AtomicLocalAdaptiveStateStore(root)
        runBlocking { store.save(LocalAdaptiveState.empty()) }
        File(root, "adaptive_state.json").writeText("broken")

        val result = runBlocking { store.load() }

        assertTrue(result is AtomicLocalAdaptiveStateStore.LoadResult.Corrupt)
    }

    @Test
    fun unsupportedFutureFormatReturnsIncompatibleWithoutGuessingMigration() = withTempDirectory { root ->
        File(root, "adaptive_state.json").writeText(
            """{"formatVersion":44,"revision":0,"completedLessons":[],"skillExposureCounts":{},"recentCompletions":[],"helpRequestCounts":{},"processedEventKeys":[]}""",
        )

        val result = runBlocking { AtomicLocalAdaptiveStateStore(root).load() }

        assertTrue(result is AtomicLocalAdaptiveStateStore.LoadResult.Incompatible)
        result as AtomicLocalAdaptiveStateStore.LoadResult.Incompatible
        assertEquals(44, result.formatVersion)
    }

    @Test
    fun resetRemovesAllProfileAdaptiveState() = withTempDirectory { root ->
        val store = AtomicLocalAdaptiveStateStore(root)
        runBlocking {
            store.save(LocalAdaptiveState(revision = 1, processedEventKeys = listOf("event:a")))
            store.save(LocalAdaptiveState(revision = 2, processedEventKeys = listOf("event:a", "event:b")))
            store.reset()
        }

        assertTrue(runBlocking { store.load() } is AtomicLocalAdaptiveStateStore.LoadResult.Missing)
    }

    @Test
    fun failureAfterBackupRotationRestoresLastKnownGoodState() = withTempDirectory { root ->
        val first = LocalAdaptiveState(revision = 1, processedEventKeys = listOf("event:first"))
        val second = LocalAdaptiveState(revision = 2, processedEventKeys = listOf("event:first", "event:second"))
        runBlocking { AtomicLocalAdaptiveStateStore(root).save(first) }
        val failing = AtomicLocalAdaptiveStateStore(
            rootDirectory = root,
            faultInjector = { stage ->
                if (stage == AtomicLocalAdaptiveStateStore.SaveStage.BACKUP_READY) {
                    throw IOException("Injected adaptive-store failure.")
                }
            },
        )

        assertThrows(IOException::class.java) {
            runBlocking { failing.save(second) }
        }

        val recovered = runBlocking { AtomicLocalAdaptiveStateStore(root).load() }
        assertTrue(recovered is AtomicLocalAdaptiveStateStore.LoadResult.Loaded)
        recovered as AtomicLocalAdaptiveStateStore.LoadResult.Loaded
        assertEquals(first, recovered.state)
    }

    private fun withTempDirectory(block: (File) -> Unit) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-adaptive-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
