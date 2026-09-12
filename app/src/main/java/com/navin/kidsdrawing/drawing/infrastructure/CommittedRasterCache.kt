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
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import java.util.LinkedHashMap

/**
 * Flattened committed-artwork cache used by the production renderer.
 *
 * The editable/vector document remains authoritative. This cache only accelerates projection:
 * normal frames draw one bitmap, and recent Undo/Redo restores the nearest checkpoint then
 * replays a small tail instead of redrawing thousands of operations.
 */
internal class CommittedRasterCache(
    private val documentSize: DocumentSize,
    private val projector: OperationProjectionCache<RenderedRasterOperation> =
        OperationProjectionCache(::projectOperation),
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
    private val checkpoints = LinkedHashMap<Int, Bitmap>()
    private var documentId: String? = null
    private var operationIds: List<String> = emptyList()

    fun bitmap(): Bitmap = bitmap

    fun reconcile(documentId: String, operations: List<DocumentOperation>) {
        val ids = operations.map { it.operationId }
        if (this.documentId != documentId) {
            resetForDocument(documentId)
            rebuild(operations)
            operationIds = ids
            return
        }

        val commonPrefix = commonPrefixLength(operationIds, ids)
        val sameTimelinePrefix = commonPrefix == minOf(operationIds.size, ids.size)

        if (!sameTimelinePrefix) {
            // A branch edit or restored document invalidates checkpoints beyond the shared prefix.
            trimCheckpointsAfter(commonPrefix)
        }

        restoreNearestCheckpoint(commonPrefix)
        val projected = projector.projectOperations(operations)
        val start = currentCheckpointCursor(commonPrefix)
        for (index in start until projected.size) {
            apply(projected[index])
            val cursor = index + 1
            maybeCheckpoint(cursor, projected.size)
        }
        operationIds = ids
    }

    private fun rebuild(operations: List<DocumentOperation>) {
        clearBitmap()
        checkpoints.values.forEach(Bitmap::recycle)
        checkpoints.clear()
        val projected = projector.projectOperations(operations)
        projected.forEachIndexed { index, operation ->
            apply(operation)
            maybeCheckpoint(index + 1, projected.size)
        }
    }

    private fun restoreNearestCheckpoint(targetPrefix: Int) {
        val checkpointCursor = checkpoints.keys.filter { it <= targetPrefix }.maxOrNull()
        if (checkpointCursor == null) {
            clearBitmap()
            return
        }
        clearBitmap()
        canvas.drawBitmap(checkpoints.getValue(checkpointCursor), 0f, 0f, null)
    }

    private fun currentCheckpointCursor(targetPrefix: Int): Int =
        checkpoints.keys.filter { it <= targetPrefix }.maxOrNull() ?: 0

    private fun maybeCheckpoint(cursor: Int, totalSize: Int) {
        if (cursor % CHECKPOINT_INTERVAL != 0 && cursor != totalSize) return
        if (cursor < (totalSize - RECENT_HISTORY_WINDOW).coerceAtLeast(0)) return
        checkpoints.remove(cursor)?.recycle()
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
        clearBitmap()
        checkpoints.values.forEach(Bitmap::recycle)
        checkpoints.clear()
        projector.reset()
    }

    private fun commonPrefixLength(a: List<String>, b: List<String>): Int {
        val limit = minOf(a.size, b.size)
        var index = 0
        while (index < limit && a[index] == b[index]) index++
        return index
    }

    companion object {
        private const val CHECKPOINT_INTERVAL = 8
        private const val RECENT_HISTORY_WINDOW = 48
        private const val MAX_CHECKPOINTS = 8

        private fun projectOperation(operation: DocumentOperation): RenderedRasterOperation =
            when (operation) {
                is DocumentOperation.AddInkStroke -> RenderedRasterOperation.Ink(
                    InkStrokeRehydrator.rehydrate(operation.stroke),
                )
                is DocumentOperation.AddEraseMask -> RenderedRasterOperation.Erase(operation.mask)
                is DocumentOperation.ClearDocument -> RenderedRasterOperation.Clear
            }
    }
}

internal sealed interface RenderedRasterOperation {
    data class Ink(val stroke: androidx.ink.strokes.Stroke) : RenderedRasterOperation
    data class Erase(val mask: EraseMaskRecord) : RenderedRasterOperation
    data object Clear : RenderedRasterOperation
}
