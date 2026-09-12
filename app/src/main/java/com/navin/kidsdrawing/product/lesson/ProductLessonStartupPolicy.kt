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
 * The runtime remains the source of recovery/session truth. This policy only prevents navigation
 * intent from being inferred from a potentially stale Compose projection.
 */
object ProductLessonStartupPolicy {
    fun decide(
        resumeRequested: Boolean,
        recoveryOutcome: LessonLabRecoveryOutcome,
        recoveredState: LessonSessionState?,
    ): ProductLessonStartupDecision {
        if (!resumeRequested) return ProductLessonStartupDecision.START_FRESH

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
