package com.navin.kidsdrawing.drawing.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
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

    internal fun attach(surface: InkDrawingSurfaceView) {
        attachedSurface = surface
        pendingDocument?.let(surface::reconcileDocument)
    }
}

/**
 * Compose boundary for the low-latency View-backed drawing surface.
 *
 * Feature UI deliberately sees only product-owned records/metrics; all AndroidX Ink types remain in
 * drawing infrastructure.
 */
@Composable
fun DrawingSurface(
    modifier: Modifier = Modifier,
    controller: DrawingSurfaceController? = null,
    onStrokeCommitted: (InkStrokeRecord) -> Unit = {},
    onMetricsChanged: (DrawingSurfaceMetrics) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            InkDrawingSurfaceView(context).apply {
                this.onStrokeCommitted = onStrokeCommitted
                this.onMetricsChanged = onMetricsChanged
                controller?.attach(this)
            }
        },
        update = { surface ->
            surface.onStrokeCommitted = onStrokeCommitted
            surface.onMetricsChanged = onMetricsChanged
            controller?.attach(surface)
        },
    )
}
