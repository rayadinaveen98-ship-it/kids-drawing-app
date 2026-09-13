package com.navin.kidsdrawing.lesson.lab

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.persistence.AtomicLessonSessionStore
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonRuntimeIdentityIntegrationTest {
    @Test
    fun customIdentityOwnsDocumentStartPersistenceAndRecovery() = runBlocking {
        val root = Files.createTempDirectory("lesson-runtime-identity-").toFile()
        try {
            val source = LessonPackageSource { path ->
                File("src/main/assets/$path").takeIf(File::isFile)?.readText()
            }
            val result = LessonPackageLoader(source).load(LessonLabRuntimeCore.LESSON_ROOT)
            val identity = LessonRuntimeIdentity(
                sessionId = "custom-catalog-session",
                documentId = "custom-catalog-document",
            )
            val documents = File(root, "documents")
            val sessions = File(root, "sessions")
            val first = LessonLabRuntimeCore(
                documentRoot = documents,
                sessionRoot = sessions,
                lessonPackageResult = result,
                runtimeIdentity = identity,
            )

            assertEquals(identity.documentId, first.documentEngine.state.value.document.documentId)
            assertEquals("cute-cat", first.documentEngine.state.value.document.metadata.lessonId)
            assertEquals(1, first.documentEngine.state.value.document.metadata.lessonRevision)
            assertTrue(first.start(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL) is LessonCommandResult.Accepted)
            first.onBackground()

            val stored = AtomicLessonSessionStore(sessions).load(identity.sessionId)
            assertTrue(stored is AtomicLessonSessionStore.LoadResult.Loaded)
            val snapshot = (stored as AtomicLessonSessionStore.LoadResult.Loaded).snapshot
            assertEquals(identity.sessionId, snapshot.sessionId)
            assertEquals(identity.documentId, snapshot.childDocumentId)

            val recreated = LessonLabRuntimeCore(
                documentRoot = documents,
                sessionRoot = sessions,
                lessonPackageResult = result,
                runtimeIdentity = identity,
            )
            assertEquals(LessonLabRecoveryOutcome.RESTORED, recreated.recover())
            assertEquals(identity.documentId, recreated.documentEngine.state.value.document.documentId)
        } finally {
            root.deleteRecursively()
        }
    }
}
