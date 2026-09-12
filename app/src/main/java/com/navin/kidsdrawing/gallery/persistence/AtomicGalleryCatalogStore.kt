package com.navin.kidsdrawing.gallery.persistence

import com.navin.kidsdrawing.gallery.domain.GalleryArtworkRecord
import com.navin.kidsdrawing.gallery.domain.GalleryCatalog
import com.navin.kidsdrawing.gallery.domain.newestFirst
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Atomic, checksummed local index over authoritative Gallery DrawingDocuments. */
class AtomicGalleryCatalogStore(
    private val rootDirectory: File,
    private val codec: GalleryCatalogCodec = GalleryCatalogCodec(),
    private val faultInjector: (SaveStage) -> Unit = {},
) {
    enum class SaveStage {
        TEMP_SYNCED,
        BACKUP_READY,
        TARGET_REPLACED,
    }

    enum class LoadSource {
        PRIMARY,
        BACKUP,
    }

    sealed interface LoadResult {
        data object Missing : LoadResult
        data class Loaded(
            val catalog: GalleryCatalog,
            val source: LoadSource,
        ) : LoadResult
        data class Corrupt(
            val primaryFailure: String?,
            val backupFailure: String?,
        ) : LoadResult
    }

    private sealed interface DecodeResult {
        data object Missing : DecodeResult
        data class Success(val catalog: GalleryCatalog) : DecodeResult
        data class Failure(val message: String) : DecodeResult
    }

    private val ioMutex = Mutex()

    suspend fun load(): LoadResult = withContext(Dispatchers.IO) {
        ioMutex.withLock { loadLocked() }
    }

    suspend fun upsert(record: GalleryArtworkRecord): GalleryCatalog = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val existing = editableCatalogLocked()
            val next = existing.copy(
                records = (existing.records.filterNot { it.entryId == record.entryId } + record).newestFirst(),
            )
            saveLocked(next)
            next
        }
    }

    suspend fun remove(entryId: String): GalleryCatalog = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val existing = editableCatalogLocked()
            val next = existing.copy(records = existing.records.filterNot { it.entryId == entryId }.newestFirst())
            if (next != existing) saveLocked(next)
            next
        }
    }

    suspend fun replace(catalog: GalleryCatalog) = withContext(Dispatchers.IO) {
        ioMutex.withLock { saveLocked(catalog.newestFirst()) }
    }

    private fun editableCatalogLocked(): GalleryCatalog = when (val loaded = loadLocked()) {
        LoadResult.Missing -> GalleryCatalog()
        is LoadResult.Loaded -> loaded.catalog
        is LoadResult.Corrupt -> throw IOException(
            "Gallery catalog is corrupt; refusing to overwrite recoverable artwork index. " +
                "primary=${loaded.primaryFailure}, backup=${loaded.backupFailure}",
        )
    }

    private fun loadLocked(): LoadResult {
        ensureRootDirectory()
        val files = files()
        val primary = decode(files.target)
        if (primary is DecodeResult.Success) {
            return LoadResult.Loaded(primary.catalog, LoadSource.PRIMARY)
        }
        val backup = decode(files.backup)
        if (backup is DecodeResult.Success) {
            return LoadResult.Loaded(backup.catalog, LoadSource.BACKUP)
        }
        if (primary is DecodeResult.Missing && backup is DecodeResult.Missing) {
            return LoadResult.Missing
        }
        return LoadResult.Corrupt(
            primaryFailure = (primary as? DecodeResult.Failure)?.message,
            backupFailure = (backup as? DecodeResult.Failure)?.message,
        )
    }

    private fun saveLocked(catalog: GalleryCatalog) {
        ensureRootDirectory()
        val files = files()
        if (files.temp.exists() && !files.temp.delete()) {
            throw IOException("Unable to remove stale Gallery catalog temp file.")
        }

        try {
            FileOutputStream(files.temp).use { output ->
                codec.encode(catalog.newestFirst(), output)
                output.flush()
                output.fd.sync()
            }
            faultInjector(SaveStage.TEMP_SYNCED)

            if (files.target.exists()) {
                if (decode(files.target) is DecodeResult.Success) {
                    if (files.backup.exists() && !files.backup.delete()) {
                        throw IOException("Unable to rotate Gallery catalog backup.")
                    }
                    if (!files.target.renameTo(files.backup)) {
                        throw IOException("Unable to preserve previous Gallery catalog.")
                    }
                } else if (!files.target.delete()) {
                    // A valid backup, when present, is deliberately retained. Never replace the
                    // last known-good catalog with a corrupt primary during recovery mutation.
                    throw IOException("Unable to remove corrupt Gallery catalog primary.")
                }
            }
            faultInjector(SaveStage.BACKUP_READY)

            if (!files.temp.renameTo(files.target)) {
                restoreBackupIfNeeded(files)
                throw IOException("Unable to promote Gallery catalog temp file.")
            }
            faultInjector(SaveStage.TARGET_REPLACED)
        } catch (failure: Throwable) {
            files.temp.delete()
            restoreBackupIfNeeded(files)
            throw failure
        }
    }

    private fun decode(file: File): DecodeResult {
        if (!file.isFile) return DecodeResult.Missing
        return try {
            DecodeResult.Success(FileInputStream(file).use(codec::decode))
        } catch (failure: Throwable) {
            DecodeResult.Failure(failure.message ?: failure::class.simpleName.orEmpty())
        }
    }

    private fun ensureRootDirectory() {
        if (rootDirectory.isDirectory) return
        if (!rootDirectory.mkdirs() && !rootDirectory.isDirectory) {
            throw IOException("Unable to create Gallery catalog directory.")
        }
    }

    private fun restoreBackupIfNeeded(files: CatalogFiles) {
        if (!files.target.exists() && files.backup.isFile) files.backup.renameTo(files.target)
    }

    private fun files(): CatalogFiles = CatalogFiles(
        target = File(rootDirectory, "gallery.kgc"),
        backup = File(rootDirectory, "gallery.bak"),
        temp = File(rootDirectory, "gallery.tmp"),
    )

    private data class CatalogFiles(
        val target: File,
        val backup: File,
        val temp: File,
    )
}
