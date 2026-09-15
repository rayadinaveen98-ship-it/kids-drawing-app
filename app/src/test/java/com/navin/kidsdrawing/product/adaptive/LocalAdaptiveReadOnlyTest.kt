package com.navin.kidsdrawing.product.adaptive

import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAdaptiveReadOnlyTest {
    @Test
    fun missingStateIsReportedAsMissing() = withTempDirectory { root ->
        val repository = LocalAdaptiveStateRepository(root)
        assertTrue(runBlocking { repository.loadReadOnly() } is LocalAdaptiveReadResult.Missing)
    }

    @Test
    fun loadedStateIsReturnedWithoutMutation() = withTempDirectory { root ->
        val repository = LocalAdaptiveStateRepository(root)
        runBlocking {
            repository.record(
                AdaptiveEvent.LessonCompleted(
                    eventKey = "complete:cat",
                    lessonId = "cute-cat",
                    lessonRevision = 1,
                    skillIds = listOf("line.curve"),
                    categoryIds = listOf("animals"),
                    journeyIds = listOf("animal-artist"),
                    difficulty = 1,
                ),
            )
        }

        val result = runBlocking { repository.loadReadOnly() }
        require(result is LocalAdaptiveReadResult.Loaded)
        assertEquals(listOf(AdaptiveLessonIdentity("cute-cat", 1)), result.state.completedLessons)
    }

    @Test
    fun corruptStateIsReportedAndNotResetByRead() = withTempDirectory { root ->
        val primary = File(root, "adaptive_state.json")
        primary.writeText("broken")
        val repository = LocalAdaptiveStateRepository(root)

        val result = runBlocking { repository.loadReadOnly() }

        assertTrue(result is LocalAdaptiveReadResult.Corrupt)
        assertEquals("broken", primary.readText())
    }

    @Test
    fun incompatibleStateIsReportedAndLeftUntouched() = withTempDirectory { root ->
        val primary = File(root, "adaptive_state.json")
        val text = """{"formatVersion":99,"revision":0,"completedLessons":[],"skillExposureCounts":{},"recentCompletions":[],"helpRequestCounts":{},"processedEventKeys":[]}"""
        primary.writeText(text)
        val repository = LocalAdaptiveStateRepository(root)

        val result = runBlocking { repository.loadReadOnly() }

        require(result is LocalAdaptiveReadResult.Incompatible)
        assertEquals(99, result.formatVersion)
        assertEquals(text, primary.readText())
    }

    private fun withTempDirectory(block: (File) -> Unit) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-adaptive-read-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
