package com.navin.kidsdrawing.product.gallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.gallery.domain.GalleryPreviewGenerationResult
import com.navin.kidsdrawing.gallery.domain.GalleryPreviewService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Small non-authoritative PNG derivative generated from persisted DrawingDocument operations. */
class AndroidGalleryPreviewService(context: Context) : GalleryPreviewService {
    private val root = File(context.applicationContext.filesDir, PREVIEW_DIRECTORY)

    override suspend fun generate(
        entryId: String,
        document: DrawingDocument,
    ): GalleryPreviewGenerationResult = withContext(Dispatchers.IO) {
        runCatching {
            ensureRoot()
            val reference = "preview-${safeStem(entryId)}.png"
            val target = File(root, reference)
            val temp = File(root, "$reference.tmp")
            if (temp.exists() && !temp.delete()) {
                throw IOException("Unable to replace Gallery preview temp file.")
            }

            val bitmap = render(document)
            try {
                FileOutputStream(temp).use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        "Gallery preview PNG compression failed."
                    }
                    output.flush()
                    output.fd.sync()
                }
            } finally {
                bitmap.recycle()
            }
            if (target.exists() && !target.delete()) {
                temp.delete()
                throw IOException("Unable to replace previous Gallery preview.")
            }
            if (!temp.renameTo(target)) {
                temp.delete()
                throw IOException("Unable to promote Gallery preview temp file.")
            }
            GalleryPreviewGenerationResult.Ready(reference)
        }.getOrElse { failure ->
            GalleryPreviewGenerationResult.Failed(failure.message)
        }
    }

    override fun exists(reference: String): Boolean = fileFor(reference)?.isFile == true

    override suspend fun delete(reference: String) = withContext(Dispatchers.IO) {
        val file = fileFor(reference) ?: return@withContext
        if (file.exists() && !file.delete()) {
            throw IOException("Unable to delete Gallery preview.")
        }
    }

    fun fileFor(reference: String): File? {
        if (reference.isBlank() || File(reference).name != reference) return null
        return File(root, reference)
    }

    private fun render(document: DrawingDocument): Bitmap {
        val finalBitmap = Bitmap.createBitmap(PREVIEW_SIZE, PREVIEW_SIZE, Bitmap.Config.ARGB_8888)
        val colorBitmap = Bitmap.createBitmap(PREVIEW_SIZE, PREVIEW_SIZE, Bitmap.Config.ARGB_8888)
        val lineBitmap = Bitmap.createBitmap(PREVIEW_SIZE, PREVIEW_SIZE, Bitmap.Config.ARGB_8888)
        val colorCanvas = Canvas(colorBitmap)
        val lineCanvas = Canvas(lineBitmap)
        val scale = min(
            (PREVIEW_SIZE - 2f * PADDING) / document.logicalSize.width,
            (PREVIEW_SIZE - 2f * PADDING) / document.logicalSize.height,
        )
        val contentWidth = document.logicalSize.width * scale
        val contentHeight = document.logicalSize.height * scale
        val offsetX = (PREVIEW_SIZE - contentWidth) / 2f
        val offsetY = (PREVIEW_SIZE - contentHeight) / 2f

        document.activeOperations().forEach { operation ->
            when (operation) {
                is DocumentOperation.AddInkStroke -> if (operation.stroke.authorRole == StrokeAuthorRole.CHILD) {
                    drawStroke(lineCanvas, operation.stroke, scale, offsetX, offsetY)
                }
                is DocumentOperation.AddEraseMask ->
                    drawErase(lineCanvas, operation.mask, scale, offsetX, offsetY)
                is DocumentOperation.AddColorStroke -> if (operation.stroke.authorRole == StrokeAuthorRole.CHILD) {
                    drawStroke(colorCanvas, operation.stroke, scale, offsetX, offsetY)
                }
                is DocumentOperation.AddColorEraseMask ->
                    drawErase(colorCanvas, operation.mask, scale, offsetX, offsetY)
                is DocumentOperation.ClearDocument -> {
                    colorCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    lineCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                }
            }
        }

        Canvas(finalBitmap).apply {
            drawColor(Color.WHITE)
            drawBitmap(colorBitmap, 0f, 0f, null)
            drawBitmap(lineBitmap, 0f, 0f, null)
        }
        colorBitmap.recycle()
        lineBitmap.recycle()
        return finalBitmap
    }

    private fun drawStroke(
        canvas: Canvas,
        stroke: InkStrokeRecord,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.colorArgb
            alpha = (stroke.opacity.coerceIn(0f, 1f) * 255f).toInt()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = stroke.baseSize * scale
        }
        val points = stroke.points
        if (points.size == 1) {
            canvas.drawCircle(
                offsetX + points.first().x * scale,
                offsetY + points.first().y * scale,
                paint.strokeWidth / 2f,
                paint.apply { style = Paint.Style.FILL },
            )
            return
        }
        val path = Path().apply {
            moveTo(offsetX + points.first().x * scale, offsetY + points.first().y * scale)
            points.drop(1).forEach { point ->
                lineTo(offsetX + point.x * scale, offsetY + point.y * scale)
            }
        }
        canvas.drawPath(path, paint)
    }

    private fun drawErase(
        canvas: Canvas,
        mask: EraseMaskRecord,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = mask.baseSize * scale
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        val points = mask.points
        if (points.size == 1) {
            canvas.drawCircle(
                offsetX + points.first().x * scale,
                offsetY + points.first().y * scale,
                paint.strokeWidth / 2f,
                paint.apply { style = Paint.Style.FILL },
            )
            return
        }
        val path = Path().apply {
            moveTo(offsetX + points.first().x * scale, offsetY + points.first().y * scale)
            points.drop(1).forEach { point ->
                lineTo(offsetX + point.x * scale, offsetY + point.y * scale)
            }
        }
        canvas.drawPath(path, paint)
    }

    private fun ensureRoot() {
        if (root.isDirectory) return
        if (!root.mkdirs() && !root.isDirectory) {
            throw IOException("Unable to create Gallery preview directory.")
        }
    }

    private fun safeStem(value: String): String = value
        .filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        .take(120)
        .ifBlank { "artwork" }

    companion object {
        const val PREVIEW_DIRECTORY = "gallery-previews"
        private const val PREVIEW_SIZE = 360
        private const val PADDING = 18f
    }
}
