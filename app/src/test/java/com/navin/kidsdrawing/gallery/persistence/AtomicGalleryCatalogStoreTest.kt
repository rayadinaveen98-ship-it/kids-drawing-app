package com.navin.kidsdrawing.gallery.persistence

import com.navin.kidsdrawing.gallery.domain.GalleryArtworkRecord
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkSource
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.gallery.domain.GalleryPreviewStatus
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AtomicGalleryCatalogStoreTest {
    @Test
    fun metadataRoundTripsAcrossStoreRecreationNewestFirst() = withTempDirectory { root ->
        runBlocking {
            val firstStore = AtomicGalleryCatalogStore(root)
            firstStore.upsert(record("older", completedAt = 1_000L))
            firstStore.upsert(record("newer", completedAt = 2_000L))

            val recreated = AtomicGalleryCatalogStore(root)
            val loaded = recreated.load() as AtomicGalleryCatalogStore.LoadResult.Loaded

            assertEquals(listOf("newer", "older"), loaded.catalog.records.map { it.entryId })
            assertEquals(AtomicGalleryCatalogStore.LoadSource.PRIMARY, loaded.source)
        }
    }

    @Test
    fun equalTimestampsUseStableEntryIdTieBreak() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicGalleryCatalogStore(root)
            store.upsert(record("a", completedAt = 4_000L))
            store.upsert(record("z", completedAt = 4_000L))

            val loaded = store.load() as AtomicGalleryCatalogStore.LoadResult.Loaded
            assertEquals(listOf("z", "a"), loaded.catalog.records.map { it.entryId })
        }
    }

    @Test
    fun corruptNewestCatalogFallsBackToPreviousKnownGoodBackup() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicGalleryCatalogStore(root)
            store.upsert(record("first", completedAt = 1_000L))
            store.upsert(record("second", completedAt = 2_000L))

            File(root, "gallery.kgc").writeText("corrupt")
            val loaded = AtomicGalleryCatalogStore(root).load()

            assertTrue(loaded is AtomicGalleryCatalogStore.LoadResult.Loaded)
            loaded as AtomicGalleryCatalogStore.LoadResult.Loaded
            assertEquals(AtomicGalleryCatalogStore.LoadSource.BACKUP, loaded.source)
            assertEquals(listOf("first"), loaded.catalog.records.map { it.entryId })
        }
    }

    @Test
    fun previewStateRoundTripsWithoutArtworkPayload() = withTempDirectory { root ->
        runBlocking {
            val store = AtomicGalleryCatalogStore(root)
            val ready = record("preview", completedAt = 3_000L).copy(
                previewStatus = GalleryPreviewStatus.READY,
                previewReference = "preview.png",
            )
            store.upsert(ready)

            val loaded = store.load() as AtomicGalleryCatalogStore.LoadResult.Loaded
            assertEquals(ready, loaded.catalog.records.single())
        }
    }

    private fun record(id: String, completedAt: Long) = GalleryArtworkRecord(
        entryId = id,
        documentId = "document-$id",
        title = "Cute Cat",
        source = GalleryArtworkSource.LESSON,
        lessonId = "cute-cat",
        lessonRevision = 1,
        completionKind = GalleryCompletionKind.COLORED,
        completedAtEpochMillis = completedAt,
    )

    private fun withTempDirectory(block: (File) -> Unit) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-gallery-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
