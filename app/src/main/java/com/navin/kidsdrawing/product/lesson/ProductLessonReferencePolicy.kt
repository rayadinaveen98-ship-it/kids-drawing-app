package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.lesson.execution.LessonTeacherSequenceFactory
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSessionState

/**
 * Product-only presentation policy for authored teacher geometry.
 *
 * These strokes are always TEACHER_GENERATED overlay records. They never enter the child's
 * DrawingDocument, undo/redo stack, persistence store, Gallery document, or completion truth.
 */
object ProductLessonReferencePolicy {
    /** Complete authored lesson silhouette used by the lesson preview card. */
    fun allTeacherStrokes(packageData: LessonRuntimePackage?): List<InkStrokeRecord> =
        packageData?.lesson?.drawing?.steps.orEmpty().flatMap { step ->
            LessonTeacherSequenceFactory.create(checkNotNull(packageData), step)
                .strokes
                .map { it.stroke }
        }

    /**
     * Returns the faint construction context that may remain visible behind the active step.
     *
     * Draw With Me / Trace & Learn:
     * - while the teacher is actively drawing the current part, only earlier parts stay faint;
     * - during the child's turn, the just-demonstrated current part also stays faint.
     *
     * Watch Then Draw intentionally remains memory-based: only already-completed earlier parts stay
     * visible. The current target is not ghosted after the overview.
     */
    fun cumulativeReferenceStrokes(
        packageData: LessonRuntimePackage?,
        state: LessonSessionState?,
    ): List<InkStrokeRecord> {
        val lessonPackage = packageData ?: return emptyList()
        val contextual = state as? LessonSessionState.Contextual ?: return emptyList()
        if (state is LessonSessionState.OverviewDemonstrating) return emptyList()

        val context = contextual.context
        val includeCurrent = when (context.mode) {
            TeachingMode.WATCH_THEN_DRAW -> false
            TeachingMode.DRAW_WITH_ME,
            TeachingMode.TRACE_AND_LEARN,
            -> !currentTeacherDemonstrationIsLive(state)
        }
        val lastReferenceIndex = if (includeCurrent) {
            context.currentStepIndex
        } else {
            context.currentStepIndex - 1
        }
        if (lastReferenceIndex < 0) return emptyList()

        return lessonPackage.lesson.drawing.steps
            .take(lastReferenceIndex + 1)
            .flatMap { step ->
                LessonTeacherSequenceFactory.create(lessonPackage, step)
                    .strokes
                    .map { it.stroke }
            }
    }

    private fun currentTeacherDemonstrationIsLive(state: LessonSessionState): Boolean = when (state) {
        is LessonSessionState.PreparingStep,
        is LessonSessionState.TeacherDemonstrating,
        -> true

        is LessonSessionState.Paused -> when (state.previousStableState) {
            is LessonSessionState.PreparingStep,
            is LessonSessionState.TeacherDemonstrating,
            -> true
            else -> false
        }

        else -> false
    }
}
