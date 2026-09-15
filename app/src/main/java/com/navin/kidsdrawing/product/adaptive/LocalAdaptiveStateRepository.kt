package com.navin.kidsdrawing.product.adaptive

import java.io.File
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Read-only adaptive-state outcome for parent/reporting projections. */
sealed interface LocalAdaptiveReadResult {
    data object Missing : LocalAdaptiveReadResult
    data class Loaded(val state: LocalAdaptiveState) : LocalAdaptiveReadResult
    data class Corrupt(
        val primaryFailure: String?,
        val backupFailure: String?,
    ) : LocalAdaptiveReadResult
    data class Incompatible(val formatVersion: Int) : LocalAdaptiveReadResult
    data class Unavailable(val message: String?) : LocalAdaptiveReadResult
}

/**
 * Small owned façade around persisted adaptive state.
 *
 * Missing/corrupt/incompatible state never becomes a crash or fabricated history. Corrupt state is
 * reset only when a new genuine product event is recorded; incompatible future state is left alone.
 */
class LocalAdaptiveStateRepository(
    rootDirectory: File,
    private val store: AtomicLocalAdaptiveStateStore = AtomicLocalAdaptiveStateStore(rootDirectory),
) {
    private val mutationMutex = Mutex()

    /**
     * Existing teaching-policy path. Keep its accepted behavior stable for P6.3.
     */
    suspend fun loadForPolicy(): LocalAdaptiveState? = when (val result = store.load()) {
        is AtomicLocalAdaptiveStateStore.LoadResult.Loaded -> result.state
        AtomicLocalAdaptiveStateStore.LoadResult.Missing,
        is AtomicLocalAdaptiveStateStore.LoadResult.Corrupt,
        is AtomicLocalAdaptiveStateStore.LoadResult.Incompatible,
        -> null
    }

    /**
     * Parent/reporting read path. It preserves the exact store outcome and never mutates state.
     */
    suspend fun loadReadOnly(): LocalAdaptiveReadResult = try {
        when (val result = store.load()) {
            is AtomicLocalAdaptiveStateStore.LoadResult.Loaded -> LocalAdaptiveReadResult.Loaded(result.state)
            AtomicLocalAdaptiveStateStore.LoadResult.Missing -> LocalAdaptiveReadResult.Missing
            is AtomicLocalAdaptiveStateStore.LoadResult.Corrupt -> LocalAdaptiveReadResult.Corrupt(
                primaryFailure = result.primaryFailure,
                backupFailure = result.backupFailure,
            )
            is AtomicLocalAdaptiveStateStore.LoadResult.Incompatible ->
                LocalAdaptiveReadResult.Incompatible(result.formatVersion)
        }
    } catch (failure: Throwable) {
        LocalAdaptiveReadResult.Unavailable(failure.message)
    }

    suspend fun record(event: AdaptiveEvent): Boolean = mutationMutex.withLock {
        val base = when (val result = store.load()) {
            is AtomicLocalAdaptiveStateStore.LoadResult.Loaded -> result.state
            AtomicLocalAdaptiveStateStore.LoadResult.Missing -> LocalAdaptiveState.empty()
            is AtomicLocalAdaptiveStateStore.LoadResult.Corrupt -> {
                store.reset()
                LocalAdaptiveState.empty()
            }
            is AtomicLocalAdaptiveStateStore.LoadResult.Incompatible -> return@withLock false
        }
        val next = LocalAdaptiveReducer.reduce(base, event)
        if (next != base) store.save(next)
        true
    }

    suspend fun resetForProfileReplacement() = mutationMutex.withLock {
        store.reset()
    }

    companion object {
        const val DIRECTORY_NAME = "adaptive-teaching"
    }
}
