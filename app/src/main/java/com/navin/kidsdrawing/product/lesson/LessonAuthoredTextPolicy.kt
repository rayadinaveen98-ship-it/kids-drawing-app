package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.session.LessonSessionState

/**
 * Pure mapping from accepted lesson/session truth to the lesson-specific text that is meaningful in
 * that state. It deliberately returns null when no authored message belongs on screen; callers keep
 * the existing generic age-aware guidance as the safe fallback.
 */
object LessonAuthoredTextPolicy {
    fun keyFor(
        state: LessonSessionState?,
        packageData: LessonRuntimePackage?,
    ): String? {
        val contextual = state as? LessonSessionState.Contextual ?: return null
        val context = contextual.context
        val step = packageData
            ?.lesson
            ?.drawing
            ?.steps
            ?.getOrNull(context.currentStepIndex)
            ?: return null
        if (step.id != context.currentStepId) return null

        return when (state) {
            is LessonSessionState.PreparingStep,
            is LessonSessionState.TeacherDemonstrating,
            -> step.teacher.narrationKey

            is LessonSessionState.HelpActive -> step.help
                .firstOrNull { it.level == context.helpLevel }
                ?.narrationKey

            is LessonSessionState.DrawingComplete,
            is LessonSessionState.AwaitingPostDrawingChoice,
            -> step.completionNarrationKey

            else -> null
        }?.takeIf(String::isNotBlank)
    }
}
