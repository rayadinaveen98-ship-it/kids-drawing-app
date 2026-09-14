package com.navin.kidsdrawing.product.adaptive

import java.io.File
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    suspend fun loadForPolicy(): LocalAdaptiveState? = when (val result = store.load()) {
        is AtomicLocalAdaptiveStateStore.LoadResult.Loaded -> result.state
        AtomicLocalAdaptiveStateStore.LoadResult.Missing,
        is AtomicLocalAdaptiveStateStore.LoadResult.Corrupt,
        is AtomicLocalAdaptiveStateStore.LoadResult.Incompatible,
        -> null
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
