package com.navin.kidsdrawing.drawing.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.infrastructure.InkDrawingSurfaceView

/**
 * Compose boundary for the low-latency View-backed drawing surface.
 *
 * Feature UI deliberately sees only product-owned records/metrics; all AndroidX Ink types remain in
 * drawing infrastructure.
 */
@Composable
fun DrawingSurface(
    modifier: Modifier = Modifier,
    onStrokeCommitted: (InkStrokeRecord) -> Unit = {},
    onMetricsChanged: (DrawingSurfaceMetrics) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            InkDrawingSurfaceView(context).apply {
                this.onStrokeCommitted = onStrokeCommitted
                this.onMetricsChanged = onMetricsChanged
            }
        },
        update = { surface ->
            surface.onStrokeCommitted = onStrokeCommitted
            surface.onMetricsChanged = onMetricsChanged
        },
    )
}
