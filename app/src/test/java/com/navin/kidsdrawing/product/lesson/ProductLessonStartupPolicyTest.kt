package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.lab.LessonLabRecoveryOutcome
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductLessonStartupPolicyTest {
    @Test
    fun fresh_start_always_starts_new_session_even_if_old_session_was_restored() {
        val result = ProductLessonStartupPolicy.decide(
            resumeRequested = false,
            recoveryOutcome = LessonLabRecoveryOutcome.RESTORED,
            recoveredState = LessonSessionState.Ready,
        )

        assertEquals(ProductLessonStartupDecision.START_FRESH, result)
    }

    @Test
    fun continue_resumes_only_a_restored_active_session() {
        val result = ProductLessonStartupPolicy.decide(
            resumeRequested = true,
            recoveryOutcome = LessonLabRecoveryOutcome.RESTORED,
            recoveredState = LessonSessionState.Ready,
        )

        assertEquals(ProductLessonStartupDecision.RESUME_RESTORED, result)
    }

    @Test
    fun continue_never_silently_creates_new_session_when_recovery_is_missing() {
        val result = ProductLessonStartupPolicy.decide(
            resumeRequested = true,
            recoveryOutcome = LessonLabRecoveryOutcome.NONE,
            recoveredState = null,
        )

        assertEquals(ProductLessonStartupDecision.RESUME_NOT_FOUND, result)
    }

    @Test
    fun continue_does_not_treat_terminal_state_as_resumable() {
        val result = ProductLessonStartupPolicy.decide(
            resumeRequested = true,
            recoveryOutcome = LessonLabRecoveryOutcome.RESTORED,
            recoveredState = LessonSessionState.Finished(
                reason = com.navin.kidsdrawing.lesson.session.LessonFinishReason.FINISHED_FOR_NOW,
            ),
        )

        assertEquals(ProductLessonStartupDecision.RESUME_NOT_FOUND, result)
    }
}
