package com.navin.kidsdrawing.drawing.infrastructure

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import java.util.LinkedHashMap
import java.util.LinkedHashSet

/**
 * Protected line-art committed projection used by the production renderer.
 *
 * The editable/vector document remains authoritative. Coloring operations are intentionally
 * filtered out before they reach this cache, so coloring and coloring erases cannot mutate the
 * protected drawing projection. The existing checkpoint/recent-window optimization remains intact.
 */
internal class CommittedRasterCache(
    private val documentSize: DocumentSize,
) {
    private val renderer = CanvasStrokeRenderer.create()
    private val identity = Matrix()
    private val bitmapWidth = documentSize.width.toInt().coerceAtLeast(1)
    private val bitmapHeight = documentSize.height.toInt().coerceAtLeast(1)
    private val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)
    private val erasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val projectedOperationCache = LinkedHashMap<String, RenderedRasterOperation>()
    private val checkpoints = LinkedHashMap<Int, Bitmap>()
    private val provisionalVisualIds = LinkedHashSet<String>()
    private var documentId: String? = null
    private var operationIds: List<String> = emptyList()

    fun bitmap(): Bitmap = bitmap

    /**
     * Immediate wet→dry handoff for a just-finished live line-art stroke.
     *
     * The pixels are provisional until the document engine publishes the matching operation.
     * Reconcile can confirm that exact append without drawing it a second time.
     */
    fun appendLiveInk(recordId: String, stroke: Stroke) {
        renderer.draw(canvas, stroke, identity)
        provisionalVisualIds += recordId
    }

    /** Immediate line-art erase; authoritative erase ordering is confirmed on reconcile. */
    fun appendLiveErase(mask: EraseMaskRecord) {
        drawEraseMask(mask)
        provisionalVisualIds += mask.maskId
    }

    fun reconcile(newDocumentId: String, operations: List<DocumentOperation>) {
        val lineOperations = operations.filter(::belongsToLineProjection)
        val ids = lineOperations.map { it.operationId }
        if (documentId != newDocumentId) {
            resetForDocument(newDocumentId)
            rebuild(lineOperations)
            operationIds = ids
            return
        }

        if (ids == operationIds && provisionalVisualIds.isEmpty()) return
        if (acceptMatchingProvisionalExtension(lineOperations, ids)) return

        val commonPrefix = commonPrefixLength(operationIds, ids)
        if (commonPrefix < minOf(operationIds.size, ids.size)) {
            trimCheckpointsAfter(commonPrefix)
        }

        restoreNearestCheckpoint(commonPrefix)
        val start = currentCheckpointCursor(commonPrefix)
        for (index in start until lineOperations.size) {
            val operation = lineOperations[index]
            apply(project(operation))
            maybeCheckpoint(index + 1, lineOperations.size)
            dropProjectionIfOutsideRecentWindow(index, lineOperations.size, operation.operationId)
        }
        operationIds = ids
        provisionalVisualIds.clear()
        retainRecentProjectedOperations(lineOperations)
    }

    /**
     * Confirms a live visual append when the authoritative line-art timeline extends by the exact
     * same stroke/mask record IDs in the exact same order. No bitmap redraw is needed.
     */
    private fun acceptMatchingProvisionalExtension(
        operations: List<DocumentOperation>,
        ids: List<String>,
    ): Boolean {
        if (provisionalVisualIds.isEmpty()) return false
        if (operationIds.size > ids.size) return false
        if (ids.subList(0, operationIds.size) != operationIds) return false

        val tail = operations.subList(operationIds.size, operations.size)
        if (tail.size != provisionalVisualIds.size) return false

        val tailVisualIds = ArrayList<String>(tail.size)
        for (operation in tail) {
            val visualId = when (operation) {
                is DocumentOperation.AddInkStroke -> operation.stroke.strokeId
                is DocumentOperation.AddEraseMask -> operation.mask.maskId
                is DocumentOperation.ClearDocument -> return false
                is DocumentOperation.AddColorStroke,
                is DocumentOperation.AddColorEraseMask,
                -> return false
            }
            tailVisualIds += visualId
        }
        if (tailVisualIds != provisionalVisualIds.toList()) return false

        operationIds = ids
        provisionalVisualIds.clear()
        maybeCheckpoint(operations.size, operations.size)
        retainRecentProjectedOperations(operations)
        return true
    }

    private fun rebuild(operations: List<DocumentOperation>) {
        clearBitmap()
        recycleCheckpoints()
        provisionalVisualIds.clear()
        operations.forEachIndexed { index, operation ->
            apply(project(operation))
            maybeCheckpoint(index + 1, operations.size)
            dropProjectionIfOutsideRecentWindow(index, operations.size, operation.operationId)
        }
        retainRecentProjectedOperations(operations)
    }

    private fun project(operation: DocumentOperation): RenderedRasterOperation =
        projectedOperationCache.getOrPut(operation.operationId) {
            when (operation) {
                is DocumentOperation.AddInkStroke -> RenderedRasterOperation.Ink(
                    InkStrokeRehydrator.rehydrate(operation.stroke),
                )

                is DocumentOperation.AddEraseMask -> RenderedRasterOperation.Erase(operation.mask)
                is DocumentOperation.ClearDocument -> RenderedRasterOperation.Clear
                is DocumentOperation.AddColorStroke,
                is DocumentOperation.AddColorEraseMask,
                -> error("Coloring operation cannot enter protected line-art projection.")
            }
        }

    private fun dropProjectionIfOutsideRecentWindow(
        index: Int,
        totalSize: Int,
        operationId: String,
    ) {
        val recentStart = (totalSize - PROJECTED_OPERATION_WINDOW).coerceAtLeast(0)
        if (index < recentStart) {
            projectedOperationCache.remove(operationId)
        }
    }

    private fun retainRecentProjectedOperations(operations: List<DocumentOperation>) {
        val recentStart = (operations.size - PROJECTED_OPERATION_WINDOW).coerceAtLeast(0)
        val liveRecent = operations
            .subList(recentStart, operations.size)
            .mapTo(HashSet(PROJECTED_OPERATION_WINDOW)) { it.operationId }
        projectedOperationCache.keys.retainAll(liveRecent)
    }

    private fun restoreNearestCheckpoint(targetPrefix: Int) {
        val checkpointCursor = checkpoints.keys.filter { it <= targetPrefix }.maxOrNull()
        clearBitmap()
        if (checkpointCursor != null) {
            canvas.drawBitmap(checkpoints.getValue(checkpointCursor), 0f, 0f, null)
        }
    }

    private fun currentCheckpointCursor(targetPrefix: Int): Int =
        checkpoints.keys.filter { it <= targetPrefix }.maxOrNull() ?: 0

    private fun maybeCheckpoint(cursor: Int, totalSize: Int) {
        if (cursor == 0 || cursor % CHECKPOINT_INTERVAL != 0) return
        if (cursor < (totalSize - RECENT_HISTORY_WINDOW).coerceAtLeast(0)) return
        if (checkpoints.containsKey(cursor)) return
        checkpoints[cursor] = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        trimCheckpointCount()
    }

    private fun trimCheckpointsAfter(cursor: Int) {
        val toRemove = checkpoints.keys.filter { it > cursor }
        toRemove.forEach { key -> checkpoints.remove(key)?.recycle() }
    }

    private fun trimCheckpointCount() {
        while (checkpoints.size > MAX_CHECKPOINTS) {
            val oldest = checkpoints.entries.firstOrNull() ?: break
            checkpoints.remove(oldest.key)?.recycle()
        }
    }

    private fun apply(operation: RenderedRasterOperation) {
        when (operation) {
            is RenderedRasterOperation.Ink -> renderer.draw(canvas, operation.stroke, identity)
            is RenderedRasterOperation.Erase -> drawEraseMask(operation.mask)
            RenderedRasterOperation.Clear -> clearBitmap()
        }
    }

    private fun drawEraseMask(mask: EraseMaskRecord) {
        erasePaint.strokeWidth = mask.baseSize
        val points = mask.points
        if (points.size == 1) {
            canvas.drawCircle(points.first().x, points.first().y, mask.baseSize / 2f, erasePaint)
            return
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { point -> lineTo(point.x, point.y) }
        }
        canvas.drawPath(path, erasePaint)
    }

    private fun clearBitmap() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    private fun resetForDocument(newDocumentId: String) {
        documentId = newDocumentId
        operationIds = emptyList()
        provisionalVisualIds.clear()
        clearBitmap()
        recycleCheckpoints()
        projectedOperationCache.clear()
    }

    private fun recycleCheckpoints() {
        checkpoints.values.forEach(Bitmap::recycle)
        checkpoints.clear()
    }

    private fun commonPrefixLength(a: List<String>, b: List<String>): Int {
        val limit = minOf(a.size, b.size)
        var index = 0
        while (index < limit && a[index] == b[index]) index++
        return index
    }

    internal fun estimatedRasterBytes(): Long =
        (1L + checkpoints.size) * bitmapWidth * bitmapHeight * BYTES_PER_ARGB_8888_PIXEL

    internal fun checkpointCount(): Int = checkpoints.size

    internal fun projectedOperationCount(): Int = projectedOperationCache.size

    internal fun provisionalVisualCount(): Int = provisionalVisualIds.size

    private fun belongsToLineProjection(operation: DocumentOperation): Boolean = when (operation) {
        is DocumentOperation.AddInkStroke,
        is DocumentOperation.AddEraseMask,
        is DocumentOperation.ClearDocument,
        -> true

        is DocumentOperation.AddColorStroke,
        is DocumentOperation.AddColorEraseMask,
        -> false
    }

    private companion object {
        const val CHECKPOINT_INTERVAL = 8
        const val RECENT_HISTORY_WINDOW = 48
        const val MAX_CHECKPOINTS = 8
        const val PROJECTED_OPERATION_WINDOW = RECENT_HISTORY_WINDOW + CHECKPOINT_INTERVAL
        const val BYTES_PER_ARGB_8888_PIXEL = 4L
    }
}

internal sealed interface RenderedRasterOperation {
    data class Ink(val stroke: Stroke) : RenderedRasterOperation
    data class Erase(val mask: EraseMaskRecord) : RenderedRasterOperation
    data object Clear : RenderedRasterOperation
}
