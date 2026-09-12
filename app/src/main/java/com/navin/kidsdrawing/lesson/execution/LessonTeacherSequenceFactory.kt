package com.navin.kidsdrawing.lesson.execution

import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSequence
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSource
import com.navin.kidsdrawing.lesson.model.AuthoredStroke
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import kotlin.math.roundToLong

/**
 * Converts validated portable lesson geometry into the frozen Drawing Engine teacher-playback
 * contract. The returned records are overlays only: every stroke is TEACHER_GENERATED and this
 * factory has no reference to a child DrawingDocument or document mutation API.
 */
object LessonTeacherSequenceFactory {
    fun create(
        lessonPackage: LessonRuntimePackage,
        step: DrawingStep,
    ): TeacherStrokeSequence {
        val catalog = lessonPackage.strokeCatalog.strokes.associateBy(AuthoredStroke::id)
        val authored = step.teacher.strokeRefs.map { ref ->
            requireNotNull(catalog[ref]) {
                "Validated lesson step ${step.id} references missing teacher stroke $ref."
            }
        }
        require(authored.isNotEmpty()) { "Teacher step ${step.id} must contain at least one stroke." }

        val normalizedDurations = authored.map(::authoredDuration)
        val unscaledDuration = normalizedDurations.sum() +
            INTER_STROKE_GAP_MS * (authored.size - 1).coerceAtLeast(0)
        val requestedDuration = step.teacher.normalDurationMs?.toLong()
        val targetDuration = requestedDuration?.coerceAtLeast(MIN_SEQUENCE_DURATION_MS)
            ?: unscaledDuration.coerceAtLeast(MIN_SEQUENCE_DURATION_MS)
        val scale = if (unscaledDuration <= 0L) 1.0 else targetDuration.toDouble() / unscaledDuration.toDouble()

        var cursorMs = 0L
        val sources = authored.mapIndexed { index, stroke ->
            val firstAuthoredTime = stroke.points.first().timeMs
            val scaledPoints = stroke.points.map { point ->
                StrokePoint(
                    x = point.x,
                    y = point.y,
                    elapsedTimeMillis = ((point.timeMs - firstAuthoredTime).coerceAtLeast(0L) * scale)
                        .roundToLong(),
                    pressure = point.pressure.coerceIn(0f, 1f),
                )
            }
            val record = InkStrokeRecord(
                strokeId = "teacher-${lessonPackage.lesson.lessonId}-${step.id}-${stroke.id}",
                brushPresetId = TEACHER_BRUSH_PRESET,
                colorArgb = DrawingToolSettings.DEFAULT_PENCIL_COLOR_ARGB,
                opacity = TEACHER_OPACITY,
                baseSize = TEACHER_BASE_SIZE,
                tool = PointerTool.STYLUS,
                points = scaledPoints,
                authorRole = StrokeAuthorRole.TEACHER_GENERATED,
            )
            val source = TeacherStrokeSource(
                stroke = record,
                startTimeMillis = (cursorMs * scale).roundToLong(),
            )
            cursorMs += normalizedDurations[index]
            if (index != authored.lastIndex) cursorMs += INTER_STROKE_GAP_MS
            source
        }

        return TeacherStrokeSequence(
            sequenceId = "lesson-${lessonPackage.lesson.lessonId}-r${lessonPackage.lesson.revision}-${step.id}",
            strokes = sources,
        )
    }

    private fun authoredDuration(stroke: AuthoredStroke): Long {
        val first = stroke.points.first().timeMs
        val last = stroke.points.last().timeMs
        return (last - first).coerceAtLeast(0L)
    }

    private const val TEACHER_BRUSH_PRESET = "pencil.standard"
    private const val TEACHER_OPACITY = 0.72f
    private const val TEACHER_BASE_SIZE = 8f
    private const val INTER_STROKE_GAP_MS = 120L
    private const val MIN_SEQUENCE_DURATION_MS = 100L
}
