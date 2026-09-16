package com.navin.kidsdrawing.coloring.persistence

import com.navin.kidsdrawing.coloring.session.ColoringSessionEngine
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.coloring.session.ColoringSessionTool
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AtomicColoringSessionStoreTest {
    @Test
    fun activeSnapshotRoundTripsThroughAtomicStore() = runBlocking {
        withTempDirectory { root ->
            val store = AtomicColoringSessionStore(root)
            val engine = ColoringSessionEngine.start(
                childDocumentId = "doc-color",
                lessonId = "cute-cat",
                lessonRevision = 1,
                mode = ColoringSessionMode.COLOR_MYSELF,
            )
            engine.selectColor(0xFF8E78C7.toInt())
            engine.selectTool(ColoringSessionTool.ERASER)
            engine.setBrushWidth(52f)
            val snapshot = engine.snapshot(9_000L)

            store.save(snapshot)
            val loaded = store.load(snapshot.sessionId)

            assertTrue(loaded is AtomicColoringSessionStore.LoadResult.Loaded)
            loaded as AtomicColoringSessionStore.LoadResult.Loaded
            assertEquals(snapshot, loaded.snapshot)
            assertEquals(AtomicColoringSessionStore.LoadSource.PRIMARY, loaded.source)
        }
    }

    @Test
    fun finishedSnapshotPersistsForHomeToIgnoreDeterministically() = runBlocking {
        withTempDirectory { root ->
            val store = AtomicColoringSessionStore(root)
            val engine = ColoringSessionEngine.start(
                childDocumentId = "doc-finished",
                lessonId = "cute-cat",
                lessonRevision = 1,
                mode = ColoringSessionMode.COLOR_WITH_ME,
            )
            engine.finish()
            val snapshot = engine.snapshot(10_000L)

            store.save(snapshot)
            val loaded = store.load(snapshot.sessionId) as AtomicColoringSessionStore.LoadResult.Loaded

            assertEquals(ColoringSessionPhase.FINISHED, loaded.snapshot.phase)
        }
    }

    @Test
    fun handoffRollbackDeleteRemovesPrimaryBackupAndResumeProjection() = runBlocking {
        withTempDirectory { root ->
            val store = AtomicColoringSessionStore(root)
            val engine = ColoringSessionEngine.start(
                childDocumentId = "doc-rollback",
                lessonId = "cute-cat",
                lessonRevision = 1,
                mode = ColoringSessionMode.COLOR_MYSELF,
            )
            val snapshot = engine.snapshot(11_000L)

            store.save(snapshot)
            assertTrue(store.load(snapshot.sessionId) is AtomicColoringSessionStore.LoadResult.Loaded)

            store.delete(snapshot.sessionId)

            assertEquals(
                AtomicColoringSessionStore.LoadResult.Missing,
                store.load(snapshot.sessionId),
            )
        }
    }

    @Test
    fun failureAfterBackupRotationPreservesLastKnownGoodColoringSession() {
        runBlocking {
            withTempDirectory { root ->
                val engine = ColoringSessionEngine.start(
                    childDocumentId = "doc-storage-pressure",
                    lessonId = "cute-cat",
                    lessonRevision = 1,
                    mode = ColoringSessionMode.COLOR_MYSELF,
                )
                engine.selectColor(0xFFE47C68.toInt())
                val knownGood = engine.snapshot(12_000L)
                AtomicColoringSessionStore(root).save(knownGood)

                engine.selectColor(0xFF6C9CB8.toInt())
                val newer = engine.snapshot(13_000L)
                val failingStore = AtomicColoringSessionStore(
                    rootDirectory = root,
                    faultInjector = { stage ->
                        if (stage == AtomicColoringSessionStore.SaveStage.BACKUP_READY) {
                            throw IOException("Injected storage-pressure failure.")
                        }
                    },
                )

                assertThrows(IOException::class.java) {
                    runBlocking { failingStore.save(newer) }
                }

                val recovered = AtomicColoringSessionStore(root).load(knownGood.sessionId)
                assertTrue(recovered is AtomicColoringSessionStore.LoadResult.Loaded)
                recovered as AtomicColoringSessionStore.LoadResult.Loaded
                assertEquals(knownGood, recovered.snapshot)
            }
        }
    }

    @Test
    fun missingSessionReturnsTypedMissing() = runBlocking {
        withTempDirectory { root ->
            val store = AtomicColoringSessionStore(root)
            assertEquals(
                AtomicColoringSessionStore.LoadResult.Missing,
                store.load("coloring-missing-document"),
            )
        }
    }

    private suspend fun withTempDirectory(block: suspend (File) -> Unit) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-coloring-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
