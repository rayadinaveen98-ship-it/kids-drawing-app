package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Atomic, offline file store for semantic lesson-session snapshots. */
class AtomicLessonSessionStore(
    private val rootDirectory: File,
    private val faultInjector: (SaveStage) -> Unit = {},
    private val codec: LessonSessionSnapshotCodec = LessonSessionSnapshotCodec(),
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
            val snapshot: LessonSessionSnapshot,
            val source: LoadSource,
        ) : LoadResult

        data class Corrupt(
            val primaryFailure: String?,
            val backupFailure: String?,
        ) : LoadResult
    }

    private val ioMutex = Mutex()
    private val latestSavedAtBySession = mutableMapOf<String, Long>()

    suspend fun save(snapshot: LessonSessionSnapshot) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val latestSavedAt = latestSavedAtBySession[snapshot.sessionId] ?: Long.MIN_VALUE
            if (snapshot.savedAtEpochMillis < latestSavedAt) {
                return@withLock
            }

            ensureRootDirectory()
            val files = filesFor(snapshot.sessionId)
            if (files.temp.exists() && !files.temp.delete()) {
                throw IOException("Unable to remove stale temporary lesson session.")
            }

            try {
                FileOutputStream(files.temp).use { output ->
                    codec.encode(snapshot, output)
                    output.flush()
                    output.fd.sync()
                }
                faultInjector(SaveStage.TEMP_SYNCED)

                if (files.target.exists()) {
                    if (files.backup.exists() && !files.backup.delete()) {
                        throw IOException("Unable to rotate previous lesson-session backup.")
                    }
                    if (!files.target.renameTo(files.backup)) {
                        throw IOException("Unable to move current lesson session to backup.")
                    }
                }
                faultInjector(SaveStage.BACKUP_READY)

                if (!files.temp.renameTo(files.target)) {
                    restoreBackupIfPrimaryMissing(files)
                    throw IOException("Unable to promote temporary lesson session to primary.")
                }
                latestSavedAtBySession[snapshot.sessionId] = maxOf(
                    latestSavedAt,
                    snapshot.savedAtEpochMillis,
                )
                faultInjector(SaveStage.TARGET_REPLACED)
            } catch (failure: Throwable) {
                files.temp.delete()
                restoreBackupIfPrimaryMissing(files)
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

            val primaryExists = primary !is DecodeResult.Missing
            val backupExists = backup !is DecodeResult.Missing
            if (!primaryExists && !backupExists) {
                LoadResult.Missing
            } else {
                LoadResult.Corrupt(
                    primaryFailure = (primary as? DecodeResult.Failure)?.message,
                    backupFailure = (backup as? DecodeResult.Failure)?.message,
                )
            }
        }
    }

    suspend fun hasPersistedSession(sessionId: String): Boolean = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val files = filesFor(sessionId)
            files.target.isFile || files.backup.isFile
        }
    }

    suspend fun delete(sessionId: String) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val files = filesFor(sessionId)
            listOf(files.target, files.backup, files.temp).forEach { file ->
                if (file.exists() && !file.delete()) {
                    throw IOException("Unable to delete persisted lesson-session file ${file.name}.")
                }
            }
            latestSavedAtBySession.remove(sessionId)
        }
    }

    private fun remember(snapshot: LessonSessionSnapshot) {
        latestSavedAtBySession[snapshot.sessionId] = maxOf(
            latestSavedAtBySession[snapshot.sessionId] ?: Long.MIN_VALUE,
            snapshot.savedAtEpochMillis,
        )
    }

    private fun decode(file: File): DecodeResult {
        if (!file.isFile) return DecodeResult.Missing
        return try {
            val snapshot = FileInputStream(file).use(codec::decode)
            DecodeResult.Success(snapshot)
        } catch (failure: Throwable) {
            DecodeResult.Failure(failure.message ?: failure::class.simpleName.orEmpty())
        }
    }

    private fun restoreBackupIfPrimaryMissing(files: SessionFiles) {
        if (!files.target.exists() && files.backup.isFile) {
            files.backup.renameTo(files.target)
        }
    }

    private fun ensureRootDirectory() {
        if (rootDirectory.isDirectory) return
        if (!rootDirectory.mkdirs() && !rootDirectory.isDirectory) {
            throw IOException("Unable to create lesson-session directory.")
        }
    }

    private fun filesFor(sessionId: String): SessionFiles {
        require(sessionId.isNotBlank()) { "sessionId cannot be blank." }
        val stem = sessionId.sha256Hex()
        return SessionFiles(
            target = File(rootDirectory, "$stem.kls"),
            backup = File(rootDirectory, "$stem.bak"),
            temp = File(rootDirectory, "$stem.tmp"),
        )
    }

    private fun String.sha256Hex(): String = MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private sealed interface DecodeResult {
        data object Missing : DecodeResult
        data class Success(val snapshot: LessonSessionSnapshot) : DecodeResult
        data class Failure(val message: String) : DecodeResult
    }

    private data class SessionFiles(
        val target: File,
        val backup: File,
        val temp: File,
    )
}
