package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class AtomicLessonSessionStoreTest {
    @Test
    fun saveLoadPreservesSnapshotAndPrimarySource() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicLessonSessionStore(root)
            val snapshot = snapshot(savedAt = 100L, stepIndex = 1, stepId = "ears")

            store.save(snapshot)
            val loaded = store.load(snapshot.sessionId)

            assertTrue(loaded is AtomicLessonSessionStore.LoadResult.Loaded)
            loaded as AtomicLessonSessionStore.LoadResult.Loaded
            assertEquals(AtomicLessonSessionStore.LoadSource.PRIMARY, loaded.source)
            assertEquals(snapshot, loaded.snapshot)
        }
    }

    @Test
    fun delayedOlderAutosaveCannotReplaceNewerSession() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicLessonSessionStore(root)
            val newer = snapshot(savedAt = 200L, stepIndex = 2, stepId = "face")
            val stale = snapshot(savedAt = 100L, stepIndex = 1, stepId = "ears")

            store.save(newer)
            store.save(stale)

            val loaded = store.load(newer.sessionId) as AtomicLessonSessionStore.LoadResult.Loaded
            assertEquals(newer, loaded.snapshot)
        }
    }

    @Test
    fun staleProtectionIsScopedPerSession() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicLessonSessionStore(root)
            val first = snapshot(sessionId = "session-a", savedAt = 500L)
            val second = snapshot(sessionId = "session-b", savedAt = 100L)

            store.save(first)
            store.save(second)

            assertTrue(store.load("session-a") is AtomicLessonSessionStore.LoadResult.Loaded)
            assertTrue(store.load("session-b") is AtomicLessonSessionStore.LoadResult.Loaded)
        }
    }

    @Test
    fun failureAfterBackupRotationPreservesLastKnownGoodSession() = withTempDirectory { root ->
        val first = snapshot(savedAt = 100L, stepIndex = 0, stepId = "head")
        val second = snapshot(savedAt = 200L, stepIndex = 1, stepId = "ears")
        runBlocking { AtomicLessonSessionStore(root).save(first) }

        val failing = AtomicLessonSessionStore(
            rootDirectory = root,
            faultInjector = { stage ->
                if (stage == AtomicLessonSessionStore.SaveStage.BACKUP_READY) {
                    throw IOException("Injected failure after session backup rotation.")
                }
            },
        )

        assertThrows(IOException::class.java) {
            runBlocking { failing.save(second) }
        }

        val recovered = runBlocking { AtomicLessonSessionStore(root).load(first.sessionId) }
        assertTrue(recovered is AtomicLessonSessionStore.LoadResult.Loaded)
        recovered as AtomicLessonSessionStore.LoadResult.Loaded
        assertEquals(first, recovered.snapshot)
    }

    @Test
    fun corruptPrimaryFallsBackToPreviousBackup() = withTempDirectory { root ->
        val store = AtomicLessonSessionStore(root)
        val first = snapshot(savedAt = 100L, stepIndex = 0, stepId = "head")
        val second = snapshot(savedAt = 200L, stepIndex = 1, stepId = "ears")
        runBlocking {
            store.save(first)
            store.save(second)
        }

        root.listFiles().orEmpty().single { it.extension == "kls" }.writeBytes(byteArrayOf(1, 2, 3))

        val recovered = runBlocking { store.load(first.sessionId) }
        assertTrue(recovered is AtomicLessonSessionStore.LoadResult.Loaded)
        recovered as AtomicLessonSessionStore.LoadResult.Loaded
        assertEquals(AtomicLessonSessionStore.LoadSource.BACKUP, recovered.source)
        assertEquals(first, recovered.snapshot)
    }

    @Test
    fun corruptOnlyCopyReturnsTypedCorruptOutcome() = withTempDirectory { root ->
        val store = AtomicLessonSessionStore(root)
        val snapshot = snapshot(savedAt = 100L)
        runBlocking { store.save(snapshot) }
        root.listFiles().orEmpty().single { it.extension == "kls" }.writeText("not a session")

        val result = runBlocking { store.load(snapshot.sessionId) }
        assertTrue(result is AtomicLessonSessionStore.LoadResult.Corrupt)
        result as AtomicLessonSessionStore.LoadResult.Corrupt
        assertTrue(!result.primaryFailure.isNullOrBlank())
    }

    @Test
    fun missingSessionIsDifferentFromCorruption() = withTempDirectory { root ->
        val result = runBlocking { AtomicLessonSessionStore(root).load("missing") }
        assertTrue(result is AtomicLessonSessionStore.LoadResult.Missing)
    }

    @Test
    fun deleteRemovesAllPersistedCopies() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicLessonSessionStore(root)
            val first = snapshot(savedAt = 100L)
            store.save(first)
            store.save(first.copy(savedAtEpochMillis = 200L, currentStepIndex = 1, currentStepId = "ears"))
            assertTrue(store.hasPersistedSession(first.sessionId))

            store.delete(first.sessionId)

            assertFalse(store.hasPersistedSession(first.sessionId))
            assertTrue(store.load(first.sessionId) is AtomicLessonSessionStore.LoadResult.Missing)
        }
    }

    private fun snapshot(
        sessionId: String = "lesson-session-store-test",
        savedAt: Long,
        stepIndex: Int = 0,
        stepId: String = "head",
    ) = LessonSessionSnapshot(
        sessionId = sessionId,
        lessonId = "cute-cat",
        lessonRevision = 1,
        childDocumentId = "child-document-store-test",
        mode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        phase = LessonSnapshotPhase.AWAITING_CHILD,
        currentStepIndex = stepIndex,
        currentStepId = stepId,
        helpLevel = 0,
        overviewCompleted = true,
        savedAtEpochMillis = savedAt,
    )

    private fun withTempDirectory(block: (File) -> Unit) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-lesson-session-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
