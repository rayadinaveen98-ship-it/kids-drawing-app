package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonFinishReason
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class LessonSessionSnapshotCodecTest {
    private val codec = LessonSessionSnapshotCodec()

    @Test
    fun roundTripPreservesSemanticSnapshot() {
        val original = snapshot()
        val bytes = encode(original)

        val decoded = codec.decode(ByteArrayInputStream(bytes))

        assertEquals(original, decoded)
    }

    @Test
    fun nullableReadyFieldsRoundTrip() {
        val original = LessonSessionSnapshot(
            sessionId = "ready-session",
            lessonId = "cute-cat",
            lessonRevision = 1,
            childDocumentId = "ready-document",
            phase = LessonSnapshotPhase.READY,
            savedAtEpochMillis = 50L,
        )

        assertEquals(original, codec.decode(ByteArrayInputStream(encode(original))))
    }

    @Test
    fun encodeDoesNotCloseCallerOwnedOutputStream() {
        val output = CloseTrackingOutputStream()

        codec.encode(snapshot(), output)

        assertFalse(output.closed)
        output.write(0x7f)
    }

    @Test
    fun checksumCorruptionFailsClosed() {
        val bytes = encode(snapshot())
        bytes[bytes.lastIndex] = (bytes.last() xor 0x01)

        assertThrows(IOException::class.java) {
            codec.decode(ByteArrayInputStream(bytes))
        }
    }

    @Test
    fun truncatedEnvelopeFailsClosed() {
        val bytes = encode(snapshot()).copyOf(20)

        assertThrows(IOException::class.java) {
            codec.decode(ByteArrayInputStream(bytes))
        }
    }

    @Test
    fun trailingBytesAreRejected() {
        val bytes = encode(snapshot()) + byteArrayOf(7)

        assertThrows(IOException::class.java) {
            codec.decode(ByteArrayInputStream(bytes))
        }
    }

    private fun encode(snapshot: LessonSessionSnapshot): ByteArray {
        val output = ByteArrayOutputStream()
        codec.encode(snapshot, output)
        return output.toByteArray()
    }

    private fun snapshot() = LessonSessionSnapshot(
        sessionId = "lesson-session-1",
        lessonId = "cute-cat",
        lessonRevision = 1,
        childDocumentId = "child-document-1",
        mode = TeachingMode.TRACE_AND_LEARN,
        pace = TeachingPace.SLOW,
        phase = LessonSnapshotPhase.PAUSED,
        pausedResumePhase = LessonSnapshotPhase.HELP_ACTIVE,
        currentStepIndex = 2,
        currentStepId = "face",
        helpLevel = 4,
        overviewCompleted = true,
        finishReason = LessonFinishReason.SAVED_FOR_LATER,
        savedAtEpochMillis = 123_456L,
    )

    private class CloseTrackingOutputStream : ByteArrayOutputStream() {
        var closed = false
            private set

        override fun close() {
            closed = true
            super.close()
        }
    }

    private infix fun Byte.xor(other: Int): Byte = (toInt() xor other).toByte()
}
