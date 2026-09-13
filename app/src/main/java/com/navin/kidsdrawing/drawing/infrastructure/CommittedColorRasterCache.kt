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
import com.navin.kidsdrawing.drawing.domain.ColorRegionFillRecord
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import java.util.LinkedHashSet

/**
 * Coloring-only committed projection.
 *
 * The editable DrawingDocument remains authoritative. This cache accepts coloring strokes,
 * coloring erase masks, prepared-region fills and global ClearDocument. Drawing/line-art
 * operations never render into this bitmap, so coloring cannot remove protected line-art pixels.
 */
internal class CommittedColorRasterCache(
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
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val provisionalVisualIds = LinkedHashSet<String>()
    private var documentId: String? = null
    private var operationIds: List<String> = emptyList()

    fun bitmap(): Bitmap = bitmap

    fun appendLiveColorInk(recordId: String, stroke: Stroke) {
        require(recordId.isNotBlank())
        renderer.draw(canvas, stroke, identity)
        provisionalVisualIds += recordId
    }

    fun appendLiveColorErase(mask: EraseMaskRecord) {
        drawEraseMask(mask)
        provisionalVisualIds += mask.maskId
    }

    fun reconcile(newDocumentId: String, operations: List<DocumentOperation>) {
        val colorOperations = operations.filter(::belongsToColorProjection)
        val ids = colorOperations.map { it.operationId }

        if (documentId != newDocumentId) {
            documentId = newDocumentId
            operationIds = emptyList()
            provisionalVisualIds.clear()
            rebuild(colorOperations)
            operationIds = ids
            return
        }

        if (ids == operationIds && provisionalVisualIds.isEmpty()) return
        if (acceptMatchingProvisionalExtension(colorOperations, ids)) return

        rebuild(colorOperations)
        operationIds = ids
        provisionalVisualIds.clear()
    }

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
        tail.forEach { operation ->
            val visualId = when (operation) {
                is DocumentOperation.AddColorStroke -> operation.stroke.strokeId
                is DocumentOperation.AddColorEraseMask -> operation.mask.maskId
                // Prepared fill has no transient Ink visual; force authoritative rebuild.
                is DocumentOperation.AddColorRegionFill -> return false
                is DocumentOperation.ClearDocument -> return false
                is DocumentOperation.AddInkStroke,
                is DocumentOperation.AddEraseMask,
                -> return false
            }
            tailVisualIds += visualId
        }
        if (tailVisualIds != provisionalVisualIds.toList()) return false

        operationIds = ids
        provisionalVisualIds.clear()
        return true
    }

    private fun rebuild(operations: List<DocumentOperation>) {
        clearBitmap()
        operations.forEach { operation ->
            when (operation) {
                is DocumentOperation.AddColorStroke -> {
                    renderer.draw(canvas, InkStrokeRehydrator.rehydrate(operation.stroke), identity)
                }

                is DocumentOperation.AddColorEraseMask -> drawEraseMask(operation.mask)
                is DocumentOperation.AddColorRegionFill -> drawRegionFill(operation.fill)
                is DocumentOperation.ClearDocument -> clearBitmap()
                is DocumentOperation.AddInkStroke,
                is DocumentOperation.AddEraseMask,
                -> Unit
            }
        }
    }

    private fun drawRegionFill(fill: ColorRegionFillRecord) {
        val points = fill.points
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { point -> lineTo(point.x, point.y) }
            close()
        }
        fillPaint.color = fill.colorArgb
        fillPaint.xfermode = null
        canvas.drawPath(path, fillPaint)
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

    internal fun estimatedRasterBytes(): Long =
        bitmapWidth.toLong() * bitmapHeight.toLong() * BYTES_PER_ARGB_8888_PIXEL

    private fun belongsToColorProjection(operation: DocumentOperation): Boolean = when (operation) {
        is DocumentOperation.AddColorStroke,
        is DocumentOperation.AddColorEraseMask,
        is DocumentOperation.AddColorRegionFill,
        is DocumentOperation.ClearDocument,
        -> true

        is DocumentOperation.AddInkStroke,
        is DocumentOperation.AddEraseMask,
        -> false
    }

    private companion object {
        const val BYTES_PER_ARGB_8888_PIXEL = 4L
    }
}
