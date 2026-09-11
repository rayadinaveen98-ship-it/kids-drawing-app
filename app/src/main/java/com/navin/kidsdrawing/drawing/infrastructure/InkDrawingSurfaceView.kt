package com.navin.kidsdrawing.drawing.infrastructure

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
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
import com.navin.kidsdrawing.drawing.domain.DocumentPoint
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DocumentViewportMapper
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.util.UUID

/**
 * AndroidX Ink is intentionally contained in this infrastructure adapter.
 *
 * The rest of the app receives product-owned [InkStrokeRecord] values in stable logical document
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

    var onStrokeCommitted: (InkStrokeRecord) -> Unit = {}
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        // A resize never mutates committed document-space geometry. If it happens mid-gesture,
        // cancel only the transient stroke so no mixed coordinate transforms can be committed.
        if (activeStrokeId != null) {
            inProgressStrokesView.cancelUnfinishedStrokes()
            pendingStrokes.clear()
            activeStrokeId = null
            activePointerId = null
            // The old predictor saw an incomplete stream. Reset it before accepting a new gesture.
            motionPredictor = MotionEventPredictor.newInstance(this)
        }

        viewportTransform = coordinateMapper.transformFor(w.toFloat(), h.toFloat())
        committedInkView.viewportTransform = viewportTransform
        committedInkView.invalidate()
        publishMetrics(metrics.copy(activeTool = null))
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
        val tool = pointerTool(toolType)

        // Inverted stylus is intentionally not treated as black ink. Eraser semantics belong to
        // the owned erase-operation slice, not this low-level pen/finger authoring milestone.
        if (!isDrawableTool(toolType)) return false

        val downPoint = coordinateMapper.viewportToDocumentOrNull(
            event.getX(pointerIndex),
            event.getY(pointerIndex),
            transform,
        ) ?: return false

        motionPredictor.record(event)
        requestUnbufferedDispatch(event)

        val brushSpec = brushFor(tool)
        val motionEventToDocument = motionEventToDocumentMatrix(transform)
        val inkStrokeId = inProgressStrokesView.startStroke(
            event = event,
            pointerId = pointerId,
            brush = brushSpec.brush,
            motionEventToWorldTransform = motionEventToDocument,
            strokeToWorldTransform = Matrix(),
        )

        val pending = PendingStroke(
            strokeId = UUID.randomUUID().toString(),
            tool = tool,
            brushPresetId = brushSpec.presetId,
            colorArgb = DEFAULT_COLOR_ARGB,
            baseSize = brushSpec.baseSize,
        )
        pending.points += pointFromCurrentEvent(event, pointerIndex, downPoint)
        pendingStrokes[inkStrokeId] = pending
        activeStrokeId = inkStrokeId
        activePointerId = pointerId
        publishMetrics(metrics.copy(activeTool = tool, lastPressure = event.getPressure(pointerIndex)))
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
            // A secondary pointer/palm left the screen. The primary drawing stroke remains valid.
            runCatching { motionPredictor.record(event) }
            return true
        }

        return if (isCanceledPointerUp(event)) {
            // Android 13+ marks rejected palm/accidental pointer-up events with FLAG_CANCELED.
            cancelStroke(event)
        } else {
            // Once multitouch has occurred, the primary drawing pointer can finish as POINTER_UP
            // rather than ACTION_UP. Finish the Ink stroke against that exact pointer id.
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
            committedInkView.addStroke(stroke)
            val pending = pendingStrokes.remove(inkId) ?: continue
            val record = pending.toRecord()
            val latency = pending.finishRequestedAtUptimeMillis?.let {
                (SystemClock.uptimeMillis() - it).coerceAtLeast(0L)
            }

            metrics = metrics.copy(
                committedStrokeCount = metrics.committedStrokeCount + 1,
                lastSampleCount = record.points.size,
                lastCommitLatencyMillis = latency,
                lastPressure = record.points.lastOrNull()?.pressure,
            )
            onStrokeCommitted(record)
        }

        // AndroidX Ink requires committed rendering + invalidation and wet-stroke removal in the
        // same UI run loop to prevent a gap/double-draw flicker during handoff.
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
                tiltRadians = event.getHistoricalAxisValue(
                    MotionEvent.AXIS_TILT,
                    pointerIndex,
                    historyIndex,
                ),
                orientationRadians = event.getHistoricalAxisValue(
                    MotionEvent.AXIS_ORIENTATION,
                    pointerIndex,
                    historyIndex,
                ),
            )
        }

        val current = coordinateMapper.viewportToDocument(
            event.getX(pointerIndex),
            event.getY(pointerIndex),
            transform,
        )
        pending.points += pointFromCurrentEvent(event, pointerIndex, current)
    }

    private fun pointFromCurrentEvent(
        event: MotionEvent,
        pointerIndex: Int,
        point: DocumentPoint,
    ): StrokePoint = StrokePoint(
        x = point.x,
        y = point.y,
        elapsedTimeMillis = event.eventTime - event.downTime,
        pressure = event.getPressure(pointerIndex),
        tiltRadians = event.getAxisValue(MotionEvent.AXIS_TILT, pointerIndex),
        orientationRadians = event.getAxisValue(MotionEvent.AXIS_ORIENTATION, pointerIndex),
    )

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

    private fun brushFor(tool: PointerTool): ResolvedBrush {
        val isStylus = tool == PointerTool.STYLUS
        val baseSize = if (isStylus) STYLUS_BASE_SIZE else FINGER_BASE_SIZE
        val family = if (isStylus) {
            StockBrushes.pressurePen(StockBrushes.PressurePenVersion.V1)
        } else {
            StockBrushes.marker(StockBrushes.MarkerVersion.V1)
        }
        val presetId = if (isStylus) "pencil.standard" else "marker.standard"
        return ResolvedBrush(
            presetId = presetId,
            baseSize = baseSize,
            brush = Brush.createWithColorIntArgb(
                family = family,
                colorIntArgb = DEFAULT_COLOR_ARGB,
                size = baseSize,
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
        val strokeId: String,
        val tool: PointerTool,
        val brushPresetId: String,
        val colorArgb: Int,
        val baseSize: Float,
        val points: MutableList<StrokePoint> = mutableListOf(),
        var finishRequestedAtUptimeMillis: Long? = null,
    ) {
        fun toRecord(): InkStrokeRecord = InkStrokeRecord(
            strokeId = strokeId,
            brushPresetId = brushPresetId,
            colorArgb = colorArgb,
            opacity = 1f,
            baseSize = baseSize,
            tool = tool,
            points = points.toList(),
        )
    }

    private class CommittedInkView(
        context: Context,
        private val documentSize: DocumentSize,
    ) : View(context) {
        private val strokes = mutableListOf<Stroke>()
        private val renderer = ViewStrokeRenderer(CanvasStrokeRenderer.create(), this)

        var viewportTransform: DocumentViewportMapper.Transform? = null

        fun addStroke(stroke: Stroke) {
            strokes += stroke
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val transform = viewportTransform ?: return
            renderer.drawWithStrokes(canvas) { scope ->
                val saveCount = canvas.save()
                canvas.translate(transform.offsetX, transform.offsetY)
                canvas.scale(transform.scale, transform.scale)
                canvas.clipRect(0f, 0f, documentSize.width, documentSize.height)
                strokes.forEach { scope.drawStroke(it) }
                canvas.restoreToCount(saveCount)
            }
        }
    }

    private companion object {
        private const val DEFAULT_COLOR_ARGB: Int = -0xDBDCDF // 0xFF242321
        private const val FINGER_BASE_SIZE = 14f
        private const val STYLUS_BASE_SIZE = 10f
        private const val BRUSH_EPSILON = 0.5f
    }
}
