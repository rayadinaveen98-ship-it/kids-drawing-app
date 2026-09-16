package com.navin.kidsdrawing.drawing.quality

import com.navin.kidsdrawing.drawing.domain.DocumentSize

data class RasterMemoryEstimate(
    val documentWidthPx: Int,
    val documentHeightPx: Int,
    val baseArgbRasterBytes: Long,
    val lineCheckpointCount: Int,
    val lineProjectionUpperBoundBytes: Long,
    val colorProjectionBytes: Long,
    val combinedProjectionUpperBoundBytes: Long,
)

/**
 * Deterministic raster-memory evidence for P6.5.
 *
 * The line projection currently retains one live ARGB bitmap plus at most eight checkpoint
 * bitmaps. The color projection retains one live ARGB bitmap and no per-frame/history bitmap
 * snapshots. This estimate intentionally excludes renderer/library overhead and vector document
 * objects; physical memory evidence still comes from the Quality Lab.
 */
object RasterMemoryEvidence {
    const val BYTES_PER_ARGB_8888_PIXEL = 4L
    const val LINE_CHECKPOINT_UPPER_BOUND = 8

    fun estimate(documentSize: DocumentSize): RasterMemoryEstimate {
        val width = documentSize.width.toInt().coerceAtLeast(1)
        val height = documentSize.height.toInt().coerceAtLeast(1)
        val base = width.toLong() * height.toLong() * BYTES_PER_ARGB_8888_PIXEL
        val lineUpper = base * (1L + LINE_CHECKPOINT_UPPER_BOUND)
        val color = base
        return RasterMemoryEstimate(
            documentWidthPx = width,
            documentHeightPx = height,
            baseArgbRasterBytes = base,
            lineCheckpointCount = LINE_CHECKPOINT_UPPER_BOUND,
            lineProjectionUpperBoundBytes = lineUpper,
            colorProjectionBytes = color,
            combinedProjectionUpperBoundBytes = lineUpper + color,
        )
    }

    fun standardDocument(): RasterMemoryEstimate = estimate(DocumentSize(1000f, 1000f))
}
