package com.navin.kidsdrawing.product.adaptive

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Atomic single-profile local store for P5.7 adaptive advisory state. */
class AtomicLocalAdaptiveStateStore(
    private val rootDirectory: File,
    private val codec: LocalAdaptiveStateCodec = LocalAdaptiveStateCodec(),
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
        data class Loaded(val state: LocalAdaptiveState, val source: LoadSource) : LoadResult
        data class Incompatible(val formatVersion: Int) : LoadResult
        data class Corrupt(val primaryFailure: String?, val backupFailure: String?) : LoadResult
    }

    private val ioMutex = Mutex()

    suspend fun save(state: LocalAdaptiveState) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            ensureRootDirectory()
            val files = files()
            if (files.temp.exists() && !files.temp.delete()) {
                throw IOException("Unable to remove stale adaptive-state temporary file.")
            }
            try {
                FileOutputStream(files.temp).use { output ->
                    codec.encode(state, output)
                    output.flush()
                    output.fd.sync()
                }
                faultInjector(SaveStage.TEMP_SYNCED)

                if (files.target.exists()) {
                    if (files.backup.exists() && !files.backup.delete()) {
                        throw IOException("Unable to rotate adaptive-state backup.")
                    }
                    if (!files.target.renameTo(files.backup)) {
                        throw IOException("Unable to move adaptive state to backup.")
                    }
                }
                faultInjector(SaveStage.BACKUP_READY)

                if (!files.temp.renameTo(files.target)) {
                    restoreBackupIfPrimaryMissing(files)
                    throw IOException("Unable to promote adaptive-state temporary file.")
                }
                faultInjector(SaveStage.TARGET_REPLACED)
            } catch (failure: Throwable) {
                files.temp.delete()
                restoreBackupIfPrimaryMissing(files)
                throw failure
            }
        }
    }

    suspend fun load(): LoadResult = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            ensureRootDirectory()
            val files = files()
            val primary = decode(files.target)
            when (primary) {
                is DecodeResult.Success -> return@withLock LoadResult.Loaded(primary.state, LoadSource.PRIMARY)
                is DecodeResult.Incompatible -> return@withLock LoadResult.Incompatible(primary.formatVersion)
                DecodeResult.Missing,
                is DecodeResult.Failure,
                -> Unit
            }

            val backup = decode(files.backup)
            when (backup) {
                is DecodeResult.Success -> return@withLock LoadResult.Loaded(backup.state, LoadSource.BACKUP)
                is DecodeResult.Incompatible -> return@withLock LoadResult.Incompatible(backup.formatVersion)
                DecodeResult.Missing,
                is DecodeResult.Failure,
                -> Unit
            }

            if (primary is DecodeResult.Missing && backup is DecodeResult.Missing) {
                return@withLock LoadResult.Missing
            }
            LoadResult.Corrupt(
                primaryFailure = (primary as? DecodeResult.Failure)?.message,
                backupFailure = (backup as? DecodeResult.Failure)?.message,
            )
        }
    }

    /** Profile replacement/reset must call this rather than trying to infer across profiles. */
    suspend fun reset() = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val files = files()
            listOf(files.target, files.backup, files.temp).forEach { file ->
                if (file.exists() && !file.delete()) {
                    throw IOException("Unable to delete adaptive-state file ${file.name}.")
                }
            }
        }
    }

    private fun decode(file: File): DecodeResult {
        if (!file.isFile) return DecodeResult.Missing
        return try {
            DecodeResult.Success(FileInputStream(file).use(codec::decode))
        } catch (failure: UnsupportedAdaptiveStateFormatException) {
            DecodeResult.Incompatible(failure.formatVersion)
        } catch (failure: Throwable) {
            DecodeResult.Failure(failure.message ?: failure::class.simpleName.orEmpty())
        }
    }

    private fun ensureRootDirectory() {
        if (rootDirectory.isDirectory) return
        if (!rootDirectory.mkdirs() && !rootDirectory.isDirectory) {
            throw IOException("Unable to create adaptive-state directory.")
        }
    }

    private fun restoreBackupIfPrimaryMissing(files: StateFiles) {
        if (!files.target.exists() && files.backup.isFile) {
            files.backup.renameTo(files.target)
        }
    }

    private fun files(): StateFiles = StateFiles(
        target = File(rootDirectory, "adaptive_state.json"),
        backup = File(rootDirectory, "adaptive_state.bak"),
        temp = File(rootDirectory, "adaptive_state.tmp"),
    )

    private sealed interface DecodeResult {
        data object Missing : DecodeResult
        data class Success(val state: LocalAdaptiveState) : DecodeResult
        data class Incompatible(val formatVersion: Int) : DecodeResult
        data class Failure(val message: String) : DecodeResult
    }

    private data class StateFiles(
        val target: File,
        val backup: File,
        val temp: File,
    )
}
