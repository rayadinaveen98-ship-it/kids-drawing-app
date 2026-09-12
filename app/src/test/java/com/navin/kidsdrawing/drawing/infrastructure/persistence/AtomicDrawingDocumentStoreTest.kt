package com.navin.kidsdrawing.drawing.infrastructure.persistence

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AtomicDrawingDocumentStoreTest {
    private val testCodec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec)

    @Test
    fun twentySaveLoadCyclesPreserveEditableOperationOrder() = withTempDirectory { root ->
        runBlocking {
            val store = store(root)
            var document = documentWithStrokeCount(4)

            repeat(20) {
                store.save(document)
                val loaded = store.load(document.documentId)
                assertNotNull(loaded)
                document = loaded!!.document
            }

            assertEquals(
                listOf("stroke-0", "stroke-1", "stroke-2", "stroke-3"),
                document.activeInkStrokes().map { it.strokeId },
            )
            assertEquals(listOf("op-0", "op-1", "op-2", "op-3"), document.operations.map { it.operationId })
        }
    }

    @Test
    fun delayedOlderAutosaveCannotReplaceNewerSavedDocument() = withTempDirectory { root ->
        runBlocking {
            val store = store(root)
            val newer = documentWithStrokeCount(2)
            val stale = documentWithStrokeCount(1)

            store.save(newer)
            store.save(stale)

            val loaded = store.load(newer.documentId)
            assertNotNull(loaded)
            assertEquals(
                listOf("stroke-0", "stroke-1"),
                loaded!!.document.activeInkStrokes().map { it.strokeId },
            )
            assertEquals(newer.modifiedAtEpochMillis, loaded.document.modifiedAtEpochMillis)
        }
    }

    @Test
    fun independentDocumentTimelinesDoNotSuppressEachOther() = withTempDirectory { root ->
        runBlocking {
            val store = store(root)
            val recentArtwork = documentWithStrokeCount(
                count = 2,
                documentId = "gallery-recent",
                modifiedAtEpochMillis = 50_000L,
            )
            val olderTimestampDifferentArtwork = documentWithStrokeCount(
                count = 1,
                documentId = "gallery-other",
                modifiedAtEpochMillis = 5_000L,
            )

            store.save(recentArtwork)
            store.save(olderTimestampDifferentArtwork)

            assertNotNull(store.load(recentArtwork.documentId))
            val second = store.load(olderTimestampDifferentArtwork.documentId)
            assertNotNull(second)
            assertEquals(5_000L, second!!.document.modifiedAtEpochMillis)
        }
    }

    @Test
    fun deleteRemovesOnlyRequestedDocumentIdentity() = withTempDirectory { root ->
        runBlocking {
            val store = store(root)
            val first = documentWithStrokeCount(1, documentId = "gallery-first")
            val second = documentWithStrokeCount(2, documentId = "gallery-second")
            store.save(first)
            store.save(second)

            store.delete(first.documentId)

            assertNull(store.load(first.documentId))
            assertNotNull(store.load(second.documentId))
        }
    }

    @Test
    fun failureAfterBackupRotationCannotDestroyLastKnownGoodDocument() = withTempDirectory { root ->
        val original = documentWithStrokeCount(1)
        val updated = documentWithStrokeCount(2)
        runBlocking { store(root).save(original) }

        val failingStore = store(root) { stage ->
            if (stage == AtomicDrawingDocumentStore.SaveStage.BACKUP_READY) {
                throw IOException("Injected failure after backup rotation.")
            }
        }

        assertThrows(IOException::class.java) {
            runBlocking { failingStore.save(updated) }
        }

        val recovered = runBlocking { store(root).load(original.documentId) }
        assertNotNull(recovered)
        assertEquals(listOf("stroke-0"), recovered!!.document.activeInkStrokes().map { it.strokeId })
    }

    @Test
    fun corruptPrimaryLoadsPreviousKnownGoodBackup() = withTempDirectory { root ->
        val store = store(root)
        val first = documentWithStrokeCount(1)
        val second = documentWithStrokeCount(2)

        runBlocking {
            store.save(first)
            store.save(second)
        }

        val primary = root.listFiles().orEmpty().single { it.extension == "kda" }
        primary.writeBytes(byteArrayOf(1, 2, 3, 4, 5))

        val recovered = runBlocking { store.load(first.documentId) }
        assertNotNull(recovered)
        assertEquals(AtomicDrawingDocumentStore.LoadSource.BACKUP, recovered!!.source)
        assertEquals(listOf("stroke-0"), recovered.document.activeInkStrokes().map { it.strokeId })
    }

    @Test
    fun corruptOnlyCopyReturnsRecoverableMissInsteadOfCrashing() = withTempDirectory { root ->
        val store = store(root)
        val document = documentWithStrokeCount(1)
        runBlocking { store.save(document) }

        val primary = root.listFiles().orEmpty().single { it.extension == "kda" }
        primary.writeText("not a drawing document")

        assertNull(runBlocking { store.load(document.documentId) })
    }

    private fun store(
        root: File,
        faultInjector: (AtomicDrawingDocumentStore.SaveStage) -> Unit = {},
    ) = AtomicDrawingDocumentStore(
        rootDirectory = root,
        documentCodec = testCodec,
        faultInjector = faultInjector,
    )

    private fun documentWithStrokeCount(
        count: Int,
        documentId: String = "document-atomic-test",
        modifiedAtEpochMillis: Long = 2_000L + count,
    ): DrawingDocument {
        val base = DrawingDocumentEngine.newDocument(
            documentId = documentId,
            nowEpochMillis = 1_000L,
        )
        return base.copy(
            modifiedAtEpochMillis = modifiedAtEpochMillis,
            operations = List(count) { index ->
                DocumentOperation.AddInkStroke(
                    operationId = "op-$index",
                    createdAtEpochMillis = 1_100L + index,
                    stroke = stroke(index),
                )
            },
        )
    }

    private fun stroke(index: Int) = InkStrokeRecord(
        strokeId = "stroke-$index",
        brushPresetId = "marker.standard",
        colorArgb = 0xFF242321.toInt(),
        opacity = 1f,
        baseSize = 14f,
        tool = PointerTool.FINGER,
        points = listOf(
            StrokePoint(index.toFloat(), 10f, 0L, 1f),
            StrokePoint(index + 1f, 11f, 16L, 1f),
        ),
    )

    private fun withTempDirectory(block: (File) -> Unit) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-store-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
