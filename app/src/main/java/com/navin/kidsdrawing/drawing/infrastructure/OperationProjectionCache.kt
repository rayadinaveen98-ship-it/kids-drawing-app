package com.navin.kidsdrawing.drawing.infrastructure

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument

/**
 * Reuses expensive renderer projections for immutable document operations.
 *
 * Document operation IDs are identity keys: once committed, an operation is immutable. Clear
 * operations affect visibility but have no renderer payload of their own. The cache is scoped to
 * one document ID so restoring a different document cannot accidentally reuse renderer objects.
 */
internal class OperationProjectionCache<T : Any>(
    private val projector: (DocumentOperation) -> T,
) {
    private var documentId: String? = null
    private val cache = LinkedHashMap<String, T>()

    fun project(document: DrawingDocument): List<T> {
        if (documentId != document.documentId) {
            documentId = document.documentId
            cache.clear()
        }

        val liveOperationIds = document.operations
            .asSequence()
            .filterNot { it is DocumentOperation.ClearDocument }
            .map { it.operationId }
            .toSet()
        cache.keys.retainAll(liveOperationIds)

        val visible = ArrayList<T>(document.operations.size)
        document.operations.forEach { operation ->
            when (operation) {
                is DocumentOperation.ClearDocument -> visible.clear()
                else -> visible += cache.getOrPut(operation.operationId) {
                    projector(operation)
                }
            }
        }
        return visible
    }

    fun reset() {
        documentId = null
        cache.clear()
    }

    internal val cachedOperationCount: Int
        get() = cache.size
}
