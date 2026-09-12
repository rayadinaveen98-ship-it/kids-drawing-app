package com.navin.kidsdrawing.drawing.infrastructure

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.ink.authoring.InProgressStrokeId
import androidx.ink.authoring.InProgressStrokesFinishedListener
import androidx.ink.authoring.InProgressStrokesView
import androidx.ink.brush.Brush
import androidx.ink.brush.StockBrushes
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.rendering.android.view.ViewStrokeRenderer
import androidx.ink.strokes.Stroke
import androidx.input.motionprediction.MotionEventPredictor
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentPoint
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DocumentViewportMapper
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.util.UUID

/**
 * AndroidX Ink is intentionally contained in this infrastructure adapter.
 *
 * The rest of the app receives product-owned stroke/mask records in stable logical document
 * coordinates. No persistence or database work occurs on this input/rendering hot path.
 */
class InkDrawingSurfaceView(
    context: Context,
    private val documentSize: DocumentSize = DocumentSize(1000f, 1000f),
) : FrameLayout(context) {
    private val coordinateMapper = DocumentViewportMapper(documentSize)
    private val committedInkView = CommittedInkView(context, documentSize)
    private val inProgressStrokesView = InProgressStrokesView(context)
    private var motionPredictor = MotionEventPredictor.newInstance(this)
    private val pendingStrokes = mutableMapOf<InProgressStrokeId, PendingStroke>()

    private var viewportTransform: DocumentViewportMapper.Transform? = null
    private var activeStrokeId: InProgressStrokeId? = null
    private var activePointerId: Int? = null
    private var metrics = DrawingSurfaceMetrics()

    var drawingToolSettings: DrawingToolSettings = DrawingToolSettings()
        set(value) {
            if (field == value) return
            if (activeStrokeId != null) cancelTransientInput()
            field = value
            publishMetrics(
                metrics.copy(
                    selectedDrawingTool = value.tool,
                    selectedColorArgb = value.colorArgb,
                    selectedWidth = value.width,
                    activeTool = null,
                ),
            )
        }

    var onStrokeCommitted: (InkStrokeRecord) -> Unit = {}
    var onEraseMaskCommitted: (EraseMaskRecord) -> Unit = {}
    var onMetricsChanged: (DrawingSurfaceMetrics) -> Unit = {}

    init {
        clipChildren = true
        clipToPadding = true
        addView(
            committedInkView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )
        addView(
            inProgressStrokesView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )

        inProgressStrokesView.addFinishedStrokesListener(
            object : InProgressStrokesFinishedListener {
                override fun onStrokesFinished(strokes: Map<InProgressStrokeId, Stroke>) {
                    handOffFinishedStrokes(strokes)
                }
            },
        )

        // Ink initialization is intentionally moved before the child's first stroke.
        post { inProgressStrokesView.eagerInit() }
    }

    /**
     * The child views are renderers only. This parent owns the complete gesture stream so input
     * cannot depend on a child view declining/accepting dispatch differently across Ink versions.
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        // A resize never mutates committed document-space geometry. If it happens mid-gesture,
        // cancel only the transient stroke so no mixed coordinate transforms can be committed.
        if (activeStrokeId != null) {
            cancelTransientInput()
        }

        viewportTransform = coordinateMapper.transformFor(w.toFloat(), h.toFloat())
        committedInkView.viewportTransform = viewportTransform
        committedInkView.invalidate()
        val transform = viewportTransform
        publishMetrics(
            metrics.copy(
                activeTool = null,
                viewportWidthPx = w,
                viewportHeightPx = h,
                documentToViewportScale = transform?.scale,
                documentOffsetXPx = transform?.offsetX,
                documentOffsetYPx = transform?.offsetY,
                selectedDrawingTool = drawingToolSettings.tool,
                selectedColorArgb = drawingToolSettings.colorArgb,
                selectedWidth = drawingToolSettings.width,
            ),
        )
    }

    /**
     * Reprojects the authoritative editable document into the committed renderer.
     *
     * This is used at stable editing boundaries such as Reload/Undo/Redo/Clear/erase/process
     * restore. Live pencil handoff remains incremental and does not rebuild prior geometry.
     */
    fun reconcileDocument(document: DrawingDocument) {
        require(document.logicalSize == documentSize) {
            "Surface document size ${document.logicalSize} does not match $documentSize."
        }
        cancelTransientInput()

        val activeOperations = document.activeOperations()
        committedInkView.replaceDocument(document)
        committedInkView.invalidate()

        val inkCount = activeOperations.count { it is DocumentOperation.AddInkStroke }
        val eraseCount = activeOperations.count { it is DocumentOperation.AddEraseMask }
        val lastPoints = when (val last = activeOperations.lastOrNull()) {
            is DocumentOperation.AddInkStroke -> last.stroke.points
            is DocumentOperation.AddEraseMask -> last.mask.points
            else -> emptyList()
        }
        publishMetrics(
            metrics.copy(
                committedStrokeCount = inkCount,
                committedEraseMaskCount = eraseCount,
                lastSampleCount = lastPoints.size,
                lastCommitLatencyMillis = null,
                lastPressure = lastPoints.lastOrNull()?.pressure,
                activeTool = null,
            ),
        )
    }

    /**
     * Internal Quality Lab hook that forces only the committed projection to participate in a
     * ViewRoot frame. It never mutates the authoritative document or transient Ink input.
     */
    fun invalidateCommittedProjectionForBenchmark() {
        committedInkView.invalidate()
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val transform = viewportTransform ?: return false

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> startStroke(event, transform)
            MotionEvent.ACTION_MOVE -> updateStroke(event)
            MotionEvent.ACTION_UP -> finishStroke(event)
            MotionEvent.ACTION_CANCEL -> cancelStroke(event)
            MotionEvent.ACTION_POINTER_DOWN -> handleAdditionalPointer(event)
            MotionEvent.ACTION_POINTER_UP -> handlePointerUp(event)
            else -> activeStrokeId != null
        }
    }

    private fun startStroke(
        event: MotionEvent,
        transform: DocumentViewportMapper.Transform,
    ): Boolean {
        if (activeStrokeId != null) return true

        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val toolType = event.getToolType(pointerIndex)
        val pointerTool = pointerTool(toolType)

        if (!isDrawableTool(toolType)) return false

        val downPoint = coordinateMapper.viewportToDocumentOrNull(
            event.getX(pointerIndex),
            event.getY(pointerIndex),
            transform,
        ) ?: return false

        motionPredictor.record(event)
        requestUnbufferedDispatch(event)

        val toolSettings = effectiveToolSettings(pointerTool)
        val brushSpec = brushFor(pointerTool, toolSettings)
        val motionEventToDocument = motionEventToDocumentMatrix(transform)
        val inkStrokeId = inProgressStrokesView.startStroke(
            event = event,
            pointerId = pointerId,
            brush = brushSpec.brush,
            motionEventToWorldTransform = motionEventToDocument,
            strokeToWorldTransform = Matrix(),
        )

        val pending = PendingStroke(
            recordId = UUID.randomUUID().toString(),
            pointerTool = pointerTool,
            drawingTool = toolSettings.tool,
            brushPresetId = brushSpec.presetId,
            colorArgb = toolSettings.colorArgb,
            baseSize = brushSpec.baseSize,
        )
        pending.points += pointFromCurrentEvent(event, pointerIndex, downPoint, pointerTool)
        pendingStrokes[inkStrokeId] = pending
        activeStrokeId = inkStrokeId
        activePointerId = pointerId
        publishMetrics(
            metrics.copy(
                activeTool = pointerTool,
                lastPressure = event.getPressure(pointerIndex),
                selectedDrawingTool = toolSettings.tool,
                selectedColorArgb = toolSettings.colorArgb,
                selectedWidth = toolSettings.width,
            ),
        )
        return true
    }

    private fun updateStroke(event: MotionEvent): Boolean {
        val strokeId = activeStrokeId ?: return false
        val pointerId = activePointerId ?: return false
        val pointerIndex = event.findPointerIndex(pointerId)
        if (pointerIndex < 0) return true

        motionPredictor.record(event)
        pendingStrokes[strokeId]?.let { appendMotionEventSamples(event, pointerIndex, it) }

        val prediction = motionPredictor.predict()
        try {
            inProgressStrokesView.addToStroke(
                event = event,
                pointerId = pointerId,
                strokeId = strokeId,
                prediction = prediction,
            )
        } finally {
            prediction?.recycle()
        }

        publishMetrics(metrics.copy(lastPressure = event.getPressure(pointerIndex)))
        return true
    }

    private fun finishStroke(event: MotionEvent): Boolean {
        val strokeId = activeStrokeId ?: return false
        val pointerId = activePointerId ?: return false
        val pointerIndex = event.findPointerIndex(pointerId)

        motionPredictor.record(event)
        if (pointerIndex >= 0) {
            pendingStrokes[strokeId]?.let { appendMotionEventSamples(event, pointerIndex, it) }
        }
        pendingStrokes[strokeId]?.finishRequestedAtUptimeMillis = SystemClock.uptimeMillis()
        inProgressStrokesView.finishStroke(event, pointerId, strokeId)

        activeStrokeId = null
        activePointerId = null
        publishMetrics(metrics.copy(activeTool = null))
        return true
    }

    private fun cancelStroke(event: MotionEvent): Boolean {
        val strokeId = activeStrokeId ?: return false
        val pointerId = activePointerId

        runCatching { motionPredictor.record(event) }
        if (pointerId != null) {
            inProgressStrokesView.cancelStroke(event, pointerId)
        } else {
            inProgressStrokesView.cancelStroke(strokeId, event)
        }
        pendingStrokes.remove(strokeId)
        activeStrokeId = null
        activePointerId = null
        publishMetrics(metrics.copy(activeTool = null))
        return true
    }

    private fun cancelTransientInput() {
        if (activeStrokeId != null || pendingStrokes.isNotEmpty()) {
            inProgressStrokesView.cancelUnfinishedStrokes()
        }
        pendingStrokes.clear()
        activeStrokeId = null
        activePointerId = null
        // A canceled stream must never seed prediction for the next child gesture.
        motionPredictor = MotionEventPredictor.newInstance(this)
    }

    private fun handleAdditionalPointer(event: MotionEvent): Boolean {
        if (activeStrokeId == null) return false

        // Keep the complete MotionEvent stream consistent for prediction, but never create a
        // second ink stroke. This is also the first-line palm/finger rejection policy while a
        // stylus stroke is active.
        runCatching { motionPredictor.record(event) }
        return true
    }

    private fun handlePointerUp(event: MotionEvent): Boolean {
        val currentStroke = activeStrokeId ?: return false
        val activePointer = activePointerId ?: return false
        val liftedPointer = event.getPointerId(event.actionIndex)

        if (liftedPointer != activePointer) {
            runCatching { motionPredictor.record(event) }
            return true
        }

        return if (isCanceledPointerUp(event)) {
            cancelStroke(event)
        } else {
            check(pendingStrokes.containsKey(currentStroke))
            finishStroke(event)
        }
    }

    private fun isCanceledPointerUp(event: MotionEvent): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (event.flags and MotionEvent.FLAG_CANCELED) != 0

    private fun handOffFinishedStrokes(strokes: Map<InProgressStrokeId, Stroke>) {
        if (strokes.isEmpty()) return

        for ((inkId, stroke) in strokes) {
            val pending = pendingStrokes.remove(inkId) ?: continue
            val latency = pending.finishRequestedAtUptimeMillis?.let {
                (SystemClock.uptimeMillis() - it).coerceAtLeast(0L)
            }

            when (pending.drawingTool) {
                DrawingTool.PENCIL -> {
                    val record = pending.toInkRecord()
                    committedInkView.addInkStroke(record.strokeId, stroke)
                    metrics = metrics.copy(
                        committedStrokeCount = metrics.committedStrokeCount + 1,
                        lastSampleCount = record.points.size,
                        lastCommitLatencyMillis = latency,
                        lastPressure = record.points.lastOrNull()?.pressure,
                    )
                    onStrokeCommitted(record)
                }

                DrawingTool.ERASER -> {
                    val mask = pending.toEraseMask()
                    committedInkView.addEraseMask(mask)
                    metrics = metrics.copy(
                        committedEraseMaskCount = metrics.committedEraseMaskCount + 1,
                        lastSampleCount = mask.points.size,
                        lastCommitLatencyMillis = latency,
                        lastPressure = mask.points.lastOrNull()?.pressure,
                    )
                    onEraseMaskCommitted(mask)
                }
            }
        }

        // Committed projection + wet-stroke removal occur in the same UI loop to avoid a gap.
        committedInkView.invalidate()
        inProgressStrokesView.removeFinishedStrokes(strokes.keys)
        publishMetrics(metrics.copy(activeTool = null))
    }

    private fun appendMotionEventSamples(
        event: MotionEvent,
        pointerIndex: Int,
        pending: PendingStroke,
    ) {
        val transform = viewportTransform ?: return
        for (historyIndex in 0 until event.historySize) {
            val point = coordinateMapper.viewportToDocument(
                event.getHistoricalX(pointerIndex, historyIndex),
                event.getHistoricalY(pointerIndex, historyIndex),
                transform,
            )
            pending.points += StrokePoint(
                x = point.x,
                y = point.y,
                elapsedTimeMillis = event.getHistoricalEventTime(historyIndex) - event.downTime,
                pressure = event.getHistoricalPressure(pointerIndex, historyIndex),
                tiltRadians = historicalStylusAxisOrNull(
                    event = event,
                    axis = MotionEvent.AXIS_TILT,
                    pointerIndex = pointerIndex,
                    historyIndex = historyIndex,
                    tool = pending.pointerTool,
                ),
                orientationRadians = historicalStylusAxisOrNull(
                    event = event,
                    axis = MotionEvent.AXIS_ORIENTATION,
                    pointerIndex = pointerIndex,
                    historyIndex = historyIndex,
                    tool = pending.pointerTool,
                ),
            )
        }

        val current = coordinateMapper.viewportToDocument(
            event.getX(pointerIndex),
            event.getY(pointerIndex),
            transform,
        )
        pending.points += pointFromCurrentEvent(event, pointerIndex, current, pending.pointerTool)
    }

    private fun pointFromCurrentEvent(
        event: MotionEvent,
        pointerIndex: Int,
        point: DocumentPoint,
        tool: PointerTool,
    ): StrokePoint = StrokePoint(
        x = point.x,
        y = point.y,
        elapsedTimeMillis = event.eventTime - event.downTime,
        pressure = event.getPressure(pointerIndex),
        tiltRadians = stylusAxisOrNull(event, MotionEvent.AXIS_TILT, pointerIndex, tool),
        orientationRadians = stylusAxisOrNull(
            event,
            MotionEvent.AXIS_ORIENTATION,
            pointerIndex,
            tool,
        ),
    )

    private fun stylusAxisOrNull(
        event: MotionEvent,
        axis: Int,
        pointerIndex: Int,
        tool: PointerTool,
    ): Float? {
        if (tool != PointerTool.STYLUS && tool != PointerTool.STYLUS_ERASER) return null
        val range = event.device?.getMotionRange(axis, event.source) ?: return null
        if (range.range <= 0f) return null
        return event.getAxisValue(axis, pointerIndex)
    }

    private fun historicalStylusAxisOrNull(
        event: MotionEvent,
        axis: Int,
        pointerIndex: Int,
        historyIndex: Int,
        tool: PointerTool,
    ): Float? {
        if (tool != PointerTool.STYLUS && tool != PointerTool.STYLUS_ERASER) return null
        val range = event.device?.getMotionRange(axis, event.source) ?: return null
        if (range.range <= 0f) return null
        return event.getHistoricalAxisValue(axis, pointerIndex, historyIndex)
    }

    private fun motionEventToDocumentMatrix(
        transform: DocumentViewportMapper.Transform,
    ): Matrix {
        val inverseScale = 1f / transform.scale
        return Matrix().apply {
            setValues(
                floatArrayOf(
                    inverseScale,
                    0f,
                    -transform.offsetX * inverseScale,
                    0f,
                    inverseScale,
                    -transform.offsetY * inverseScale,
                    0f,
                    0f,
                    1f,
                ),
            )
        }
    }

    private fun effectiveToolSettings(pointerTool: PointerTool): DrawingToolSettings {
        val selected = drawingToolSettings
        if (pointerTool != PointerTool.STYLUS_ERASER) return selected
        return selected.copy(
            tool = DrawingTool.ERASER,
            width = maxOf(selected.width, DrawingToolSettings.DEFAULT_ERASER_WIDTH),
        )
    }

    private fun brushFor(
        pointerTool: PointerTool,
        settings: DrawingToolSettings,
    ): ResolvedBrush {
        if (settings.tool == DrawingTool.ERASER) {
            return ResolvedBrush(
                presetId = "eraser.standard",
                baseSize = settings.width,
                brush = Brush.createWithColorIntArgb(
                    family = StockBrushes.marker(StockBrushes.MarkerVersion.V1),
                    colorIntArgb = ERASER_PREVIEW_COLOR_ARGB,
                    size = settings.width,
                    epsilon = BRUSH_EPSILON,
                ),
            )
        }

        val family = if (pointerTool == PointerTool.STYLUS) {
            StockBrushes.pressurePen(StockBrushes.PressurePenVersion.V1)
        } else {
            StockBrushes.marker(StockBrushes.MarkerVersion.V1)
        }
        return ResolvedBrush(
            presetId = "pencil.standard",
            baseSize = settings.width,
            brush = Brush.createWithColorIntArgb(
                family = family,
                colorIntArgb = settings.colorArgb,
                size = settings.width,
                epsilon = BRUSH_EPSILON,
            ),
        )
    }

    private fun pointerTool(toolType: Int): PointerTool = when (toolType) {
        MotionEvent.TOOL_TYPE_FINGER -> PointerTool.FINGER
        MotionEvent.TOOL_TYPE_STYLUS -> PointerTool.STYLUS
        MotionEvent.TOOL_TYPE_ERASER -> PointerTool.STYLUS_ERASER
        else -> PointerTool.UNKNOWN
    }

    private fun isDrawableTool(toolType: Int): Boolean = when (toolType) {
        MotionEvent.TOOL_TYPE_FINGER,
        MotionEvent.TOOL_TYPE_STYLUS,
        MotionEvent.TOOL_TYPE_ERASER,
        -> true
        else -> false
    }

    private fun publishMetrics(updated: DrawingSurfaceMetrics) {
        metrics = updated
        onMetricsChanged(updated)
    }

    private data class ResolvedBrush(
        val presetId: String,
        val baseSize: Float,
        val brush: Brush,
    )

    private data class PendingStroke(
        val recordId: String,
        val pointerTool: PointerTool,
        val drawingTool: DrawingTool,
        val brushPresetId: String,
        val colorArgb: Int,
        val baseSize: Float,
        val points: MutableList<StrokePoint> = mutableListOf(),
        var finishRequestedAtUptimeMillis: Long? = null,
    ) {
        fun toInkRecord(): InkStrokeRecord = InkStrokeRecord(
            strokeId = recordId,
            brushPresetId = brushPresetId,
            colorArgb = colorArgb,
            opacity = 1f,
            baseSize = baseSize,
            tool = pointerTool,
            points = points.toList(),
        )

        fun toEraseMask(): EraseMaskRecord = EraseMaskRecord(
            maskId = recordId,
            baseSize = baseSize,
            points = points.toList(),
        )
    }

    private class CommittedInkView(
        context: Context,
        private val documentSize: DocumentSize,
    ) : View(context) {
        private val rasterCache = CommittedRasterCache(documentSize)
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        var viewportTransform: DocumentViewportMapper.Transform? = null

        fun addInkStroke(strokeId: String, stroke: Stroke) {
            require(strokeId.isNotBlank())
            rasterCache.appendLiveInk(strokeId, stroke)
        }

        fun addEraseMask(mask: EraseMaskRecord) {
            rasterCache.appendLiveErase(mask)
        }

        fun replaceDocument(document: DrawingDocument) {
            rasterCache.reconcile(
                newDocumentId = document.documentId,
                operations = document.operations,
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val transform = viewportTransform ?: return
            val saveCount = canvas.save()
            canvas.translate(transform.offsetX, transform.offsetY)
            canvas.scale(transform.scale, transform.scale)
            canvas.clipRect(0f, 0f, documentSize.width, documentSize.height)
            canvas.drawBitmap(rasterCache.bitmap(), 0f, 0f, bitmapPaint)
            canvas.restoreToCount(saveCount)
        }
    }

    private companion object {
        private const val ERASER_PREVIEW_COLOR_ARGB: Int = -0x1 // 0xFFFFFFFF
        private const val BRUSH_EPSILON = 0.5f
    }
}
