package com.navin.kidsdrawing.product.adaptive

import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAdaptiveStateRepositoryTest {
    @Test
    fun missingStateFallsBackToNullPolicyInput() = withTempDirectory { root ->
        val repository = LocalAdaptiveStateRepository(root)
        assertNull(runBlocking { repository.loadForPolicy() })
    }

    @Test
    fun genuineEventCreatesLocalStateAndDuplicateDeliveryRemainsIdempotent() = withTempDirectory { root ->
        val repository = LocalAdaptiveStateRepository(root)
        val event = completion("complete:a")

        assertTrue(runBlocking { repository.record(event) })
        assertTrue(runBlocking { repository.record(event) })
        val state = runBlocking { repository.loadForPolicy() }

        requireNotNull(state)
        assertEquals(1L, state.revision)
        assertEquals(1, state.completedLessons.size)
        assertEquals(1, state.skillExposureCounts["line.curve"])
    }

    @Test
    fun corruptStateIsResetOnlyWhenNewGenuineEventArrives() = withTempDirectory { root ->
        val repository = LocalAdaptiveStateRepository(root)
        File(root, "adaptive_state.json").writeText("broken")

        assertNull(runBlocking { repository.loadForPolicy() })
        assertTrue(runBlocking { repository.record(completion("complete:new")) })

        val state = runBlocking { repository.loadForPolicy() }
        requireNotNull(state)
        assertEquals(listOf(AdaptiveLessonIdentity("cute-cat", 1)), state.completedLessons)
    }

    @Test
    fun incompatibleFutureStateIsNotOverwritten() = withTempDirectory { root ->
        File(root, "adaptive_state.json").writeText(
            """{"formatVersion":99,"revision":0,"completedLessons":[],"skillExposureCounts":{},"recentCompletions":[],"helpRequestCounts":{},"processedEventKeys":[]}""",
        )
        val repository = LocalAdaptiveStateRepository(root)

        assertFalse(runBlocking { repository.record(completion("complete:a")) })
        assertNull(runBlocking { repository.loadForPolicy() })
        assertTrue(File(root, "adaptive_state.json").readText().contains("\"formatVersion\":99"))
    }

    @Test
    fun profileReplacementResetRemovesProjection() = withTempDirectory { root ->
        val repository = LocalAdaptiveStateRepository(root)
        runBlocking {
            repository.record(completion("complete:a"))
            repository.resetForProfileReplacement()
        }

        assertNull(runBlocking { repository.loadForPolicy() })
    }

    private fun completion(key: String) = AdaptiveEvent.LessonCompleted(
        eventKey = key,
        lessonId = "cute-cat",
        lessonRevision = 1,
        skillIds = listOf("line.curve"),
        categoryIds = listOf("animals"),
        journeyIds = listOf("journey.animal_artist"),
        difficulty = 2,
    )

    private fun withTempDirectory(block: (File) -> Unit) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-adaptive-repository-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
