package com.navin.kidsdrawing.product.adaptive

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAdaptiveStateCodecTest {
    private val codec = LocalAdaptiveStateCodec()

    @Test
    fun roundTripPreservesBoundedProjection() {
        val state = LocalAdaptiveState(
            revision = 3L,
            completedLessons = listOf(AdaptiveLessonIdentity("cute-cat", 1)),
            skillExposureCounts = mapOf("line.curve" to 1),
            recentCompletions = listOf(AdaptiveLessonIdentity("cute-cat", 1)),
            helpRequestCounts = mapOf("kind:VISUAL_GUIDE" to 2),
            processedEventKeys = listOf("complete:a", "help:b"),
        )
        val bytes = ByteArrayOutputStream().also { codec.encode(state, it) }.toByteArray()

        val decoded = codec.decode(ByteArrayInputStream(bytes))

        assertEquals(state, decoded)
    }

    @Test
    fun unsupportedFutureFormatIsRejectedExplicitly() {
        val json = """{"formatVersion":99,"revision":0,"completedLessons":[],"skillExposureCounts":{},"recentCompletions":[],"helpRequestCounts":{},"processedEventKeys":[]}"""

        val failure = assertThrows(UnsupportedAdaptiveStateFormatException::class.java) {
            codec.decode(ByteArrayInputStream(json.toByteArray()))
        }

        assertEquals(99, failure.formatVersion)
    }

    @Test
    fun invalidSemanticBoundsFailClosed() {
        val json = """{"formatVersion":1,"revision":0,"completedLessons":[],"skillExposureCounts":{"line.curve":1000},"recentCompletions":[],"helpRequestCounts":{},"processedEventKeys":[]}"""

        assertThrows(IOException::class.java) {
            codec.decode(ByteArrayInputStream(json.toByteArray()))
        }
    }

    @Test
    fun persistedShapeContainsNoForbiddenArtworkOrJudgmentFields() {
        val state = LocalAdaptiveState(
            revision = 1,
            completedLessons = listOf(AdaptiveLessonIdentity("simple-rocket", 1)),
            skillExposureCounts = mapOf("shape.combine" to 1),
            helpRequestCounts = mapOf("kind:GENTLE_HINT" to 1),
            processedEventKeys = listOf("complete:one"),
        )
        val text = ByteArrayOutputStream().also { codec.encode(state, it) }
            .toString(Charsets.UTF_8.name())

        listOf(
            "strokePoints",
            "coordinates",
            "artwork",
            "imagePayload",
            "abilityLabel",
            "score",
            "grade",
            "rank",
            "deviceId",
            "advertisingId",
            "cloudToken",
        ).forEach { forbidden ->
            assertFalse("Persisted adaptive state must not contain $forbidden", text.contains(forbidden, ignoreCase = true))
        }
        assertTrue(text.contains("completedLessons"))
        assertTrue(text.contains("skillExposureCounts"))
    }
}
