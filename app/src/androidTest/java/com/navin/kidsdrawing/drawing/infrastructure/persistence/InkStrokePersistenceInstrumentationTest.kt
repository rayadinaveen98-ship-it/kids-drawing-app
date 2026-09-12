package com.navin.kidsdrawing.drawing.infrastructure.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on Android so the production AndroidX Ink JNI-backed payload codec is exercised directly. */
@RunWith(AndroidJUnit4::class)
class InkStrokePersistenceInstrumentationTest {
    private val codec = DrawingDocumentBinaryCodec()

    @Test
    fun productionInkCodecRoundTripsStylusPressureTiltAndOrientation() {
        val stroke = InkStrokeRecord(
            strokeId = "android-real-ink-stylus",
            brushPresetId = "pencil.standard",
            colorArgb = 0xFF315C9A.toInt(),
            opacity = 0.85f,
            baseSize = 12f,
            tool = PointerTool.STYLUS,
            points = listOf(
                StrokePoint(120f, 180f, 0L, 0.35f, tiltRadians = 0.2f, orientationRadians = 0.5f),
                StrokePoint(180f, 220f, 16L, 0.65f, tiltRadians = 0.25f, orientationRadians = 0.55f),
                StrokePoint(250f, 300f, 32L, 0.9f, tiltRadians = 0.3f, orientationRadians = 0.6f),
            ),
        )
        val base = DrawingDocumentEngine.newDocument(
            documentId = "android-real-ink-document",
            nowEpochMillis = 1_000L,
        )
        val document = base.copy(
            modifiedAtEpochMillis = 1_100L,
            operations = listOf(
                DocumentOperation.AddInkStroke(
                    operationId = "android-real-ink-op",
                    createdAtEpochMillis = 1_050L,
                    stroke = stroke,
                ),
            ),
        )

        val bytes = ByteArrayOutputStream().use { output ->
            codec.encode(document, output)
            output.toByteArray()
        }
        val decoded = codec.decode(ByteArrayInputStream(bytes))
        val restored = decoded.activeInkStrokes().single()

        assertEquals(stroke.strokeId, restored.strokeId)
        assertEquals(stroke.tool, restored.tool)
        assertEquals(stroke.brushPresetId, restored.brushPresetId)
        assertEquals(stroke.colorArgb, restored.colorArgb)
        assertEquals(stroke.baseSize, restored.baseSize)
        assertEquals(stroke.points.size, restored.points.size)
        stroke.points.zip(restored.points).forEach { (expected, actual) ->
            assertEquals(expected.x, actual.x, 0.0001f)
            assertEquals(expected.y, actual.y, 0.0001f)
            assertEquals(expected.elapsedTimeMillis, actual.elapsedTimeMillis)
            assertEquals(expected.pressure, actual.pressure, 0.0001f)
            assertNotNull(actual.tiltRadians)
            assertNotNull(actual.orientationRadians)
            assertEquals(expected.tiltRadians!!, actual.tiltRadians!!, 0.0001f)
            assertEquals(expected.orientationRadians!!, actual.orientationRadians!!, 0.0001f)
        }
        assertTrue(restored.points.zipWithNext().all { (a, b) -> b.elapsedTimeMillis >= a.elapsedTimeMillis })
    }

    @Test
    fun productionInkCodecRoundTripsFingerStrokeWithoutInventingStylusAxes() {
        val stroke = InkStrokeRecord(
            strokeId = "android-real-ink-touch",
            brushPresetId = "marker.standard",
            colorArgb = 0xFF242321.toInt(),
            opacity = 1f,
            baseSize = 18f,
            tool = PointerTool.FINGER,
            points = listOf(
                StrokePoint(20f, 40f, 0L, 1f),
                StrokePoint(40f, 80f, 12L, 1f),
            ),
        )

        val payloadBytes = ByteArrayOutputStream().use { output ->
            InkStrokePayloadCodec.encode(stroke, output)
            output.toByteArray()
        }
        val restored = InkStrokePayloadCodec.decode(ByteArrayInputStream(payloadBytes))

        assertEquals(PointerTool.FINGER, restored.tool)
        assertEquals(stroke.points.size, restored.points.size)
        assertTrue(restored.points.all { it.tiltRadians == null && it.orientationRadians == null })
    }
}
