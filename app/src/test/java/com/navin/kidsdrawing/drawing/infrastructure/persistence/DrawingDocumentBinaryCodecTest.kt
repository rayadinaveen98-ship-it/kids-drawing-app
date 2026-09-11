package com.navin.kidsdrawing.drawing.infrastructure.persistence

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingDocumentBinaryCodecTest {
    private val codec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec)

    @Test
    fun mixedOperationDocumentRoundTripsWithOwnedMetadata() {
        val original = sampleDocument()
        val decoded = roundTrip(original)
        assertDocumentEquivalent(original, decoded)
    }

    @Test
    fun twentyEncodeDecodeCyclesDoNotDriftOperationSemantics() {
        val original = sampleDocument()
        var current = original
        repeat(20) { current = roundTrip(current) }
        assertDocumentEquivalent(original, current)
    }

    @Test
    fun optionalMetadataDefaultsRemainAbsent() {
        val original = sampleDocument().copy(metadata = DrawingDocumentMetadata())
        val decoded = roundTrip(original)
        assertNull(decoded.metadata.lessonId)
        assertNull(decoded.metadata.lessonRevision)
    }

    @Test
    fun checksumDetectsCorruption() {
        val bytes = encode(sampleDocument())
        val bodyIndex = (bytes.size / 2).coerceAtLeast(12)
        bytes[bodyIndex] = (bytes[bodyIndex].toInt() xor 0x5A).toByte()

        assertThrows(IllegalArgumentException::class.java) {
            codec.decode(ByteArrayInputStream(bytes))
        }
    }

    @Test
    fun unsupportedEnvelopeVersionIsRejected() {
        val bytes = encode(sampleDocument())
        bytes[7] = 2

        assertThrows(IllegalArgumentException::class.java) {
            codec.decode(ByteArrayInputStream(bytes))
        }
    }

    private fun roundTrip(document: DrawingDocument): DrawingDocument =
        codec.decode(ByteArrayInputStream(encode(document)))

    private fun encode(document: DrawingDocument): ByteArray = ByteArrayOutputStream().use { output ->
        codec.encode(document, output)
        output.toByteArray()
    }

    private fun sampleDocument(): DrawingDocument {
        val stylusStroke = InkStrokeRecord(
            strokeId = "stroke-stylus",
            brushPresetId = "pencil.standard",
            colorArgb = 0xFF332211.toInt(),
            opacity = 0.8f,
            baseSize = 9.5f,
            tool = PointerTool.STYLUS,
            authorRole = StrokeAuthorRole.CHILD,
            points = listOf(
                point(10.25f, 20.5f, 0L, 0.30f, 0.2f, 1.2f),
                point(11.75f, 23.0f, 16L, 0.55f, 0.25f, 1.25f),
                point(15.0f, 26.25f, 32L, 0.80f, 0.3f, 1.3f),
            ),
        )
        val fingerStroke = InkStrokeRecord(
            strokeId = "stroke-finger",
            brushPresetId = "marker.standard",
            colorArgb = 0xFF225544.toInt(),
            opacity = 1f,
            baseSize = 14f,
            tool = PointerTool.FINGER,
            authorRole = StrokeAuthorRole.CHILD,
            points = listOf(
                point(100f, 200f, 0L, 1f),
                point(110f, 205f, 20L, 1f),
            ),
        )
        val erase = EraseMaskRecord(
            maskId = "erase-1",
            baseSize = 24f,
            points = listOf(
                point(40f, 50f, 0L, 1f),
                point(42f, 54f, 12L, 1f),
            ),
        )

        return DrawingDocument(
            documentId = "artwork-123",
            logicalSize = DocumentSize(1000f, 1000f),
            createdAtEpochMillis = 1_000L,
            modifiedAtEpochMillis = 2_000L,
            metadata = DrawingDocumentMetadata(
                lessonId = "cute-cat",
                lessonRevision = 3,
            ),
            operations = listOf(
                DocumentOperation.AddInkStroke("op-1", 1_100L, stylusStroke),
                DocumentOperation.AddEraseMask("op-2", 1_200L, erase),
                DocumentOperation.ClearDocument("op-3", 1_300L),
                DocumentOperation.AddInkStroke("op-4", 1_400L, fingerStroke),
            ),
        )
    }

    private fun point(
        x: Float,
        y: Float,
        time: Long,
        pressure: Float,
        tilt: Float? = null,
        orientation: Float? = null,
    ) = StrokePoint(
        x = x,
        y = y,
        elapsedTimeMillis = time,
        pressure = pressure,
        tiltRadians = tilt,
        orientationRadians = orientation,
    )

    private fun assertDocumentEquivalent(expected: DrawingDocument, actual: DrawingDocument) {
        assertEquals(expected.documentSchemaVersion, actual.documentSchemaVersion)
        assertEquals(expected.documentId, actual.documentId)
        assertClose(expected.logicalSize.width, actual.logicalSize.width)
        assertClose(expected.logicalSize.height, actual.logicalSize.height)
        assertEquals(expected.createdAtEpochMillis, actual.createdAtEpochMillis)
        assertEquals(expected.modifiedAtEpochMillis, actual.modifiedAtEpochMillis)
        assertEquals(expected.backgroundRole, actual.backgroundRole)
        assertEquals(expected.metadata, actual.metadata)
        assertEquals(expected.operations.size, actual.operations.size)

        expected.operations.zip(actual.operations).forEach { (left, right) ->
            assertEquals(left::class, right::class)
            assertEquals(left.operationId, right.operationId)
            assertEquals(left.createdAtEpochMillis, right.createdAtEpochMillis)
            when {
                left is DocumentOperation.AddInkStroke && right is DocumentOperation.AddInkStroke ->
                    assertStrokeEquivalent(left.stroke, right.stroke)
                left is DocumentOperation.AddEraseMask && right is DocumentOperation.AddEraseMask -> {
                    assertEquals(left.mask.maskId, right.mask.maskId)
                    assertClose(left.mask.baseSize, right.mask.baseSize)
                    assertPointsEquivalent(left.mask.points, right.mask.points)
                }
                left is DocumentOperation.ClearDocument && right is DocumentOperation.ClearDocument -> Unit
                else -> throw AssertionError("Operation type mismatch.")
            }
        }
    }

    private fun assertStrokeEquivalent(expected: InkStrokeRecord, actual: InkStrokeRecord) {
        assertEquals(expected.strokeId, actual.strokeId)
        assertEquals(expected.brushPresetId, actual.brushPresetId)
        assertEquals(expected.colorArgb, actual.colorArgb)
        assertClose(expected.opacity, actual.opacity)
        assertClose(expected.baseSize, actual.baseSize)
        assertEquals(expected.tool, actual.tool)
        assertEquals(expected.authorRole, actual.authorRole)
        assertPointsEquivalent(expected.points, actual.points)
    }

    private fun assertPointsEquivalent(expected: List<StrokePoint>, actual: List<StrokePoint>) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (left, right) ->
            assertClose(left.x, right.x)
            assertClose(left.y, right.y)
            assertEquals(left.elapsedTimeMillis, right.elapsedTimeMillis)
            assertClose(left.pressure, right.pressure)
            assertOptionalClose(left.tiltRadians, right.tiltRadians)
            assertOptionalClose(left.orientationRadians, right.orientationRadians)
        }
    }

    private fun assertOptionalClose(expected: Float?, actual: Float?) {
        if (expected == null || actual == null) {
            assertEquals(expected, actual)
        } else {
            assertClose(expected, actual)
        }
    }

    private fun assertClose(expected: Float, actual: Float) {
        assertTrue("Expected $expected, got $actual", kotlin.math.abs(expected - actual) < 0.0001f)
    }
}
