package com.navin.kidsdrawing.lesson.execution

import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSequence
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage

/** Builds one non-destructive teacher sequence containing every drawing step in authored order. */
object LessonOverviewSequenceFactory {
    fun create(lessonPackage: LessonRuntimePackage): TeacherStrokeSequence {
        val sources = mutableListOf<TeacherStrokeSource>()
        var cursorMs = 0L

        lessonPackage.lesson.drawing.steps.forEachIndexed { index, step ->
            val stepSequence = LessonTeacherSequenceFactory.create(lessonPackage, step)
            stepSequence.strokes.forEach { source ->
                sources += source.copy(startTimeMillis = cursorMs + source.startTimeMillis)
            }
            cursorMs += stepSequence.sourceDurationMillis
            if (index != lessonPackage.lesson.drawing.steps.lastIndex) {
                cursorMs += STEP_GAP_MS
            }
        }

        require(sources.isNotEmpty()) { "Lesson overview requires at least one teacher stroke." }
        return TeacherStrokeSequence(
            sequenceId = "lesson-${lessonPackage.lesson.lessonId}-r${lessonPackage.lesson.revision}-overview",
            strokes = sources,
        )
    }

    private const val STEP_GAP_MS = 280L
}
