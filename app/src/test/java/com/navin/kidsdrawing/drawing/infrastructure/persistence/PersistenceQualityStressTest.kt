package com.navin.kidsdrawing.drawing.infrastructure.persistence

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.quality.StressFixtureFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistenceQualityStressTest {
    private val codec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec)

    @Test
    fun w2HeavyDocumentBinaryRoundTripPreservesOperationsMetadataAndSamples() {
        val original = StressFixtureFactory.w2Document()
        val encoded = ByteArrayOutputStream().use { output ->
            codec.encode(original, output)
            output.toByteArray()
        }
        val decoded = codec.decode(ByteArrayInputStream(encoded))

        assertEquals(original.documentId, decoded.documentId)
        assertEquals(original.operations.map { it.operationId }, decoded.operations.map { it.operationId })
        assertEquals(original.activeInkStrokes().map { it.strokeId }, decoded.activeInkStrokes().map { it.strokeId })
        assertEquals(
            StressFixtureFactory.W2_TOTAL_SAMPLES,
            decoded.activeInkStrokes().sumOf { it.points.size },
        )
        assertEquals(original.activeInkStrokes().map { it.colorArgb }, decoded.activeInkStrokes().map { it.colorArgb })
        assertEquals(original.activeInkStrokes().map { it.baseSize }, decoded.activeInkStrokes().map { it.baseSize })
        assertEquals(original.activeInkStrokes().map { it.tool }, decoded.activeInkStrokes().map { it.tool })
    }

    @Test
    fun twentyBinaryRoundTripsDoNotAccumulateGeometryOrMetadataDrift() {
        val original = StressFixtureFactory.representativePersistenceDocument()
        var current = original

        repeat(20) {
            val bytes = ByteArrayOutputStream().use { output ->
                codec.encode(current, output)
                output.toByteArray()
            }
            current = codec.decode(ByteArrayInputStream(bytes))
        }

        assertEquals(original, current)
    }

    @Test
    fun w3FiveThousandOperationEnvelopeRemainsDecodableAndOrdered() {
        val original = StressFixtureFactory.w3Document()
        val bytes = ByteArrayOutputStream().use { output ->
            codec.encode(original, output)
            output.toByteArray()
        }
        val decoded = codec.decode(ByteArrayInputStream(bytes))

        assertEquals(StressFixtureFactory.W3_OPERATION_COUNT, decoded.operations.size)
        assertEquals(original.operations.map { it.operationId }, decoded.operations.map { it.operationId })
        assertEquals(
            original.operations.filterIsInstance<DocumentOperation.AddInkStroke>().map { it.stroke.strokeId },
            decoded.operations.filterIsInstance<DocumentOperation.AddInkStroke>().map { it.stroke.strokeId },
        )
    }

    @Test
    fun hundredOverlappingSaveRequestsConvergeOnNewestSnapshotWithoutCorruption() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicDrawingDocumentStore(rootDirectory = root, documentCodec = codec)
            val base = StressFixtureFactory.document(
                documentId = "quality-concurrent-save",
                operationCount = 64,
                samplesPerStroke = 8,
            )

            coroutineScope {
                repeat(100) { version ->
                    launch {
                        store.save(base.copy(modifiedAtEpochMillis = 100_000L + version))
                    }
                }
            }

            val loaded = store.load(base.documentId)
            assertNotNull(loaded)
            assertEquals(100_099L, loaded!!.document.modifiedAtEpochMillis)
            assertEquals(base.operations.map { it.operationId }, loaded.document.operations.map { it.operationId })
        }
    }

    @Test
    fun corruptPrimaryAfterRepeatedSavesStillRecoversKnownGoodBackup() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicDrawingDocumentStore(rootDirectory = root, documentCodec = codec)
            val base = StressFixtureFactory.document(
                documentId = "quality-backup-stress",
                operationCount = 100,
                samplesPerStroke = 8,
            )
            repeat(10) { version ->
                store.save(base.copy(modifiedAtEpochMillis = 10_000L + version))
            }

            val primary = root.listFiles().orEmpty().single { it.extension == "kda" }
            primary.writeBytes(byteArrayOf(0x13, 0x37, 0x00, 0x01))

            val recovered = store.load(base.documentId)
            assertNotNull(recovered)
            assertEquals(AtomicDrawingDocumentStore.LoadSource.BACKUP, recovered!!.source)
            assertEquals(10_008L, recovered.document.modifiedAtEpochMillis)
            assertEquals(base.operations.map { it.operationId }, recovered.document.operations.map { it.operationId })
        }
    }

    @Test
    fun allStressStrokeTimestampsRemainMonotonicAfterRoundTrip() {
        val original = StressFixtureFactory.w2Document()
        val bytes = ByteArrayOutputStream().use { output ->
            codec.encode(original, output)
            output.toByteArray()
        }
        val decoded = codec.decode(ByteArrayInputStream(bytes))

        assertTrue(decoded.activeInkStrokes().all { stroke ->
            stroke.points.zipWithNext().all { (a, b) -> b.elapsedTimeMillis >= a.elapsedTimeMillis }
        })
    }

    private fun withTempDirectory(block: (File) -> Unit) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-quality-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
