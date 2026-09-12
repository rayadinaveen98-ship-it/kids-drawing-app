package com.navin.kidsdrawing.coloring.persistence

import com.navin.kidsdrawing.coloring.session.ColoringSessionSnapshot
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Atomic offline persistence for the semantic Coloring Session Engine state. */
class AtomicColoringSessionStore(
    private val rootDirectory: File,
    private val codec: ColoringSessionSnapshotCodec = ColoringSessionSnapshotCodec(),
) {
    enum class LoadSource {
        PRIMARY,
        BACKUP,
    }

    sealed interface LoadResult {
        data object Missing : LoadResult
        data class Loaded(
            val snapshot: ColoringSessionSnapshot,
            val source: LoadSource,
        ) : LoadResult
        data class Corrupt(
            val primaryFailure: String?,
            val backupFailure: String?,
        ) : LoadResult
    }

    private val ioMutex = Mutex()
    private val latestSavedAt = mutableMapOf<String, Long>()

    suspend fun save(snapshot: ColoringSessionSnapshot) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val remembered = latestSavedAt[snapshot.sessionId] ?: Long.MIN_VALUE
            if (snapshot.savedAtEpochMillis < remembered) return@withLock
            ensureRootDirectory()
            val files = filesFor(snapshot.sessionId)
            if (files.temp.exists() && !files.temp.delete()) {
                throw IOException("Unable to remove stale coloring-session temp file.")
            }

            try {
                FileOutputStream(files.temp).use { output ->
                    codec.encode(snapshot, output)
                    output.flush()
                    output.fd.sync()
                }
                if (files.target.exists()) {
                    if (files.backup.exists() && !files.backup.delete()) {
                        throw IOException("Unable to rotate coloring-session backup.")
                    }
                    if (!files.target.renameTo(files.backup)) {
                        throw IOException("Unable to preserve coloring-session backup.")
                    }
                }
                if (!files.temp.renameTo(files.target)) {
                    restoreBackupIfNeeded(files)
                    throw IOException("Unable to promote coloring-session temp file.")
                }
                latestSavedAt[snapshot.sessionId] = maxOf(remembered, snapshot.savedAtEpochMillis)
            } catch (failure: Throwable) {
                files.temp.delete()
                restoreBackupIfNeeded(files)
                throw failure
            }
        }
    }

    suspend fun load(sessionId: String): LoadResult = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            ensureRootDirectory()
            val files = filesFor(sessionId)
            val primary = decode(files.target)
            if (primary is DecodeResult.Success) {
                remember(primary.snapshot)
                return@withLock LoadResult.Loaded(primary.snapshot, LoadSource.PRIMARY)
            }
            val backup = decode(files.backup)
            if (backup is DecodeResult.Success) {
                remember(backup.snapshot)
                return@withLock LoadResult.Loaded(backup.snapshot, LoadSource.BACKUP)
            }
            if (primary is DecodeResult.Missing && backup is DecodeResult.Missing) {
                LoadResult.Missing
            } else {
                LoadResult.Corrupt(
                    primaryFailure = (primary as? DecodeResult.Failure)?.message,
                    backupFailure = (backup as? DecodeResult.Failure)?.message,
                )
            }
        }
    }

    suspend fun delete(sessionId: String) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val files = filesFor(sessionId)
            listOf(files.target, files.backup, files.temp).forEach { file ->
                if (file.exists() && !file.delete()) {
                    throw IOException("Unable to delete coloring session ${file.name}.")
                }
            }
            latestSavedAt.remove(sessionId)
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

    private fun remember(snapshot: ColoringSessionSnapshot) {
        latestSavedAt[snapshot.sessionId] = maxOf(
            latestSavedAt[snapshot.sessionId] ?: Long.MIN_VALUE,
            snapshot.savedAtEpochMillis,
        )
    }

    private fun ensureRootDirectory() {
        if (rootDirectory.isDirectory) return
        if (!rootDirectory.mkdirs() && !rootDirectory.isDirectory) {
            throw IOException("Unable to create coloring-session directory.")
        }
    }

    private fun restoreBackupIfNeeded(files: SessionFiles) {
        if (!files.target.exists() && files.backup.isFile) files.backup.renameTo(files.target)
    }

    private fun filesFor(sessionId: String): SessionFiles {
        require(sessionId.isNotBlank()) { "Coloring session ID cannot be blank." }
        val stem = sessionId.sha256Hex()
        return SessionFiles(
            target = File(rootDirectory, "$stem.kcs"),
            backup = File(rootDirectory, "$stem.bak"),
            temp = File(rootDirectory, "$stem.tmp"),
        )
    }

    private fun String.sha256Hex(): String = MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private sealed interface DecodeResult {
        data object Missing : DecodeResult
        data class Success(val snapshot: ColoringSessionSnapshot) : DecodeResult
        data class Failure(val message: String) : DecodeResult
    }

    private data class SessionFiles(
        val target: File,
        val backup: File,
        val temp: File,
    )
}
