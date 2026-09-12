package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.lab.LessonLabRecoveryOutcome
import com.navin.kidsdrawing.lesson.session.LessonSessionState

enum class ProductLessonStartupDecision {
    RESUME_RESTORED,
    START_FRESH,
    RESUME_NOT_FOUND,
}

/**
 * Pure product policy for deciding what the guided route does after runtime recovery.
 *
 * [startFreshRequested] is a one-time navigation intent set only when the child confirms Start
 * Drawing in preview. After the real session starts, saveable product state clears that intent so
 * Android recreation recovers the new session rather than wiping it and starting again.
 */
object ProductLessonStartupPolicy {
    fun decide(
        startFreshRequested: Boolean,
        recoveryOutcome: LessonLabRecoveryOutcome,
        recoveredState: LessonSessionState?,
    ): ProductLessonStartupDecision {
        if (startFreshRequested) return ProductLessonStartupDecision.START_FRESH

        val activeRestoredSession = recoveryOutcome == LessonLabRecoveryOutcome.RESTORED &&
            recoveredState != null &&
            recoveredState !is LessonSessionState.Finished &&
            recoveredState !is LessonSessionState.FatalContentError

        return if (activeRestoredSession) {
            ProductLessonStartupDecision.RESUME_RESTORED
        } else {
            ProductLessonStartupDecision.RESUME_NOT_FOUND
        }
    }
}
