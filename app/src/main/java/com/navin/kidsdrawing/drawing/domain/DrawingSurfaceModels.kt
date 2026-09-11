package com.navin.kidsdrawing.drawing.domain

/** Logical drawing document dimensions. Persisted stroke geometry always uses this space. */
data class DocumentSize(
    val width: Float,
    val height: Float,
) {
    init {
        require(width.isFinite() && width > 0f) { "Document width must be finite and positive." }
        require(height.isFinite() && height > 0f) { "Document height must be finite and positive." }
    }
}

data class DocumentPoint(
    val x: Float,
    val y: Float,
)

enum class PointerTool {
    FINGER,
    STYLUS,
    STYLUS_ERASER,
    UNKNOWN,
}

/**
 * Product-owned input sample in document coordinates.
 *
 * No AndroidX Ink type is allowed in this model. Optional stylus axes remain nullable when the
 * device/tool does not report them.
 */
data class StrokePoint(
    val x: Float,
    val y: Float,
    val elapsedTimeMillis: Long,
    val pressure: Float,
    val tiltRadians: Float? = null,
    val orientationRadians: Float? = null,
)

/**
 * Product/domain representation emitted by DrawingSurface when a stroke commits successfully.
 * The Ink adapter may retain richer renderer data internally, but callers receive only this model.
 */
data class InkStrokeRecord(
    val strokeId: String,
    val brushPresetId: String,
    val colorArgb: Int,
    val opacity: Float,
    val baseSize: Float,
    val tool: PointerTool,
    val points: List<StrokePoint>,
    val authorRole: StrokeAuthorRole = StrokeAuthorRole.CHILD,
) {
    init {
        require(strokeId.isNotBlank()) { "strokeId cannot be blank." }
        require(brushPresetId.isNotBlank()) { "brushPresetId cannot be blank." }
        require(opacity in 0f..1f) { "opacity must be between 0 and 1." }
        require(baseSize.isFinite() && baseSize > 0f) { "baseSize must be finite and positive." }
        require(points.isNotEmpty()) { "A committed stroke must contain at least one input point." }
    }
}

/** Lightweight internal instrumentation surfaced by Art Lab only. */
data class DrawingSurfaceMetrics(
    val committedStrokeCount: Int = 0,
    val activeTool: PointerTool? = null,
    val lastSampleCount: Int = 0,
    val lastCommitLatencyMillis: Long? = null,
    val lastPressure: Float? = null,
)
