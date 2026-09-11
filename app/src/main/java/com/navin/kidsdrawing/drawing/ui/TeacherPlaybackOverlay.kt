package com.navin.kidsdrawing.drawing.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole

/**
 * Teacher demonstration renderer layered above the editable child DrawingSurface.
 *
 * This overlay consumes only product-owned stroke records. It never mutates the child document,
 * undo/redo history, persistence store, or AndroidX Ink dry-stroke renderer.
 */
@Composable
fun TeacherPlaybackOverlay(
    strokes: List<InkStrokeRecord>,
    modifier: Modifier = Modifier,
    documentSize: DocumentSize = DocumentSize(1000f, 1000f),
) {
    Canvas(modifier = modifier) {
        if (strokes.isEmpty()) return@Canvas
        val scale = minOf(size.width / documentSize.width, size.height / documentSize.height)
        val offsetX = (size.width - documentSize.width * scale) / 2f
        val offsetY = (size.height - documentSize.height * scale) / 2f

        strokes.forEach { stroke ->
            require(stroke.authorRole == StrokeAuthorRole.TEACHER_GENERATED) {
                "Teacher overlay accepts only teacher-generated strokes."
            }
            val points = stroke.points
            if (points.isEmpty()) return@forEach

            val color = Color(stroke.colorArgb).copy(
                alpha = (Color(stroke.colorArgb).alpha * stroke.opacity).coerceIn(0f, 1f),
            )
            if (points.size == 1) {
                drawCircle(
                    color = color,
                    radius = stroke.baseSize * scale / 2f,
                    center = Offset(
                        x = offsetX + points.first().x * scale,
                        y = offsetY + points.first().y * scale,
                    ),
                )
                return@forEach
            }

            val path = Path().apply {
                moveTo(
                    offsetX + points.first().x * scale,
                    offsetY + points.first().y * scale,
                )
                points.drop(1).forEach { point ->
                    lineTo(
                        offsetX + point.x * scale,
                        offsetY + point.y * scale,
                    )
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = stroke.baseSize * scale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}
