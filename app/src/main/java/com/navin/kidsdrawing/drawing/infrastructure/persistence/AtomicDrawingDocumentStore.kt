package com.navin.kidsdrawing.drawing.infrastructure.persistence

import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AtomicDrawingDocumentStore(
    private val rootDirectory: File,
    private val faultInjector: (SaveStage) -> Unit = {},
    private val documentCodec: DrawingDocumentBinaryCodec = DrawingDocumentBinaryCodec(),
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

    data class LoadResult(
        val document: DrawingDocument,
        val source: LoadSource,
    )

    private val ioMutex = Mutex()
    private var latestSavedModifiedAtEpochMillis: Long = Long.MIN_VALUE

    suspend fun save(document: DrawingDocument) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            // A delayed debounce/lifecycle save must never overwrite a newer stable snapshot.
            if (document.modifiedAtEpochMillis < latestSavedModifiedAtEpochMillis) {
                return@withLock
            }

            ensureRootDirectory()
            val files = filesFor(document.documentId)
            if (files.temp.exists() && !files.temp.delete()) {
                throw IOException("Unable to remove stale temporary drawing document.")
            }

            try {
                FileOutputStream(files.temp).use { output ->
                    documentCodec.encode(document, output)
                    output.flush()
                    output.fd.sync()
                }
                faultInjector(SaveStage.TEMP_SYNCED)

                if (files.target.exists()) {
                    if (files.backup.exists() && !files.backup.delete()) {
                        throw IOException("Unable to rotate previous drawing-document backup.")
                    }
                    if (!files.target.renameTo(files.backup)) {
                        throw IOException("Unable to move current drawing document to backup.")
                    }
                }
                faultInjector(SaveStage.BACKUP_READY)

                if (!files.temp.renameTo(files.target)) {
                    restoreBackupIfPrimaryMissing(files)
                    throw IOException("Unable to promote temporary drawing document to primary.")
                }
                latestSavedModifiedAtEpochMillis = maxOf(
                    latestSavedModifiedAtEpochMillis,
                    document.modifiedAtEpochMillis,
                )
                faultInjector(SaveStage.TARGET_REPLACED)
            } catch (failure: Throwable) {
                files.temp.delete()
                restoreBackupIfPrimaryMissing(files)
                throw failure
            }
        }
    }

    suspend fun load(documentId: String): LoadResult? = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            ensureRootDirectory()
            val files = filesFor(documentId)
            val result = decodeOrNull(files.target)?.let { LoadResult(it, LoadSource.PRIMARY) }
                ?: decodeOrNull(files.backup)?.let { LoadResult(it, LoadSource.BACKUP) }
            if (result != null) {
                latestSavedModifiedAtEpochMillis = maxOf(
                    latestSavedModifiedAtEpochMillis,
                    result.document.modifiedAtEpochMillis,
                )
            }
            result
        }
    }

    suspend fun hasRecoverableDocument(documentId: String): Boolean = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val files = filesFor(documentId)
            files.target.exists() || files.backup.exists()
        }
    }

    private fun decodeOrNull(file: File): DrawingDocument? {
        if (!file.isFile) return null
        return runCatching {
            FileInputStream(file).use(documentCodec::decode)
        }.getOrNull()
    }

    private fun restoreBackupIfPrimaryMissing(files: DocumentFiles) {
        if (!files.target.exists() && files.backup.isFile) {
            files.backup.renameTo(files.target)
        }
    }

    private fun ensureRootDirectory() {
        if (rootDirectory.isDirectory) return
        if (!rootDirectory.mkdirs() && !rootDirectory.isDirectory) {
            throw IOException("Unable to create drawing-document directory.")
        }
    }

    private fun filesFor(documentId: String): DocumentFiles {
        require(documentId.isNotBlank()) { "documentId cannot be blank." }
        val stem = documentId.sha256Hex()
        return DocumentFiles(
            target = File(rootDirectory, "$stem.kda"),
            backup = File(rootDirectory, "$stem.bak"),
            temp = File(rootDirectory, "$stem.tmp"),
        )
    }

    private fun String.sha256Hex(): String = MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private data class DocumentFiles(
        val target: File,
        val backup: File,
        val temp: File,
    )
}
