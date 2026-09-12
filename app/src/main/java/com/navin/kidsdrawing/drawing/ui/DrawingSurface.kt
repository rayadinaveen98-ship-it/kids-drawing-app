package com.navin.kidsdrawing.drawing.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.infrastructure.InkDrawingSurfaceView

/**
 * Owned command boundary for explicit document→renderer reconciliation.
 *
 * Feature UI can request a reload/undo/redo projection using [DrawingDocument] only. AndroidX Ink
 * types never cross this boundary.
 */
class DrawingSurfaceController {
    private var attachedSurface: InkDrawingSurfaceView? = null
    private var pendingDocument: DrawingDocument? = null

    fun reconcileDocument(document: DrawingDocument) {
        pendingDocument = document
        attachedSurface?.reconcileDocument(document)
    }

    /** Internal Quality Lab hook. Never mutates document state. */
    fun invalidateCommittedProjectionForBenchmark() {
        attachedSurface?.invalidateCommittedProjectionForBenchmark()
    }

    internal fun attach(surface: InkDrawingSurfaceView) {
        if (attachedSurface === surface) return
        attachedSurface = surface
        pendingDocument?.let(surface::reconcileDocument)
    }
}

/**
 * Compose boundary for the low-latency View-backed drawing surface.
 *
 * Feature UI deliberately sees only product-owned records/settings/metrics; all AndroidX Ink types
 * remain in drawing infrastructure.
 */
@Composable
fun DrawingSurface(
    modifier: Modifier = Modifier,
    controller: DrawingSurfaceController? = null,
    toolSettings: DrawingToolSettings = DrawingToolSettings(),
    onStrokeCommitted: (InkStrokeRecord) -> Unit = {},
    onEraseMaskCommitted: (EraseMaskRecord) -> Unit = {},
    onMetricsChanged: (DrawingSurfaceMetrics) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            InkDrawingSurfaceView(context).apply {
                drawingToolSettings = toolSettings
                this.onStrokeCommitted = onStrokeCommitted
                this.onEraseMaskCommitted = onEraseMaskCommitted
                this.onMetricsChanged = onMetricsChanged
                controller?.attach(this)
            }
        },
        update = { surface ->
            surface.drawingToolSettings = toolSettings
            surface.onStrokeCommitted = onStrokeCommitted
            surface.onEraseMaskCommitted = onEraseMaskCommitted
            surface.onMetricsChanged = onMetricsChanged
        },
    )
}
