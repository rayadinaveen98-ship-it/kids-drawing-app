package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.lab.LessonLabRecoveryOutcome
import com.navin.kidsdrawing.lesson.session.LessonFinishReason
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductLessonStartupPolicyTest {
    @Test
    fun preview_start_replaces_old_session_exactly_once() {
        val result = ProductLessonStartupPolicy.decide(
            startFreshRequested = true,
            recoveryOutcome = LessonLabRecoveryOutcome.RESTORED,
            recoveredState = LessonSessionState.Ready,
        )

        assertEquals(ProductLessonStartupDecision.START_FRESH, result)
    }

    @Test
    fun recreation_resumes_restored_active_session_after_fresh_intent_is_cleared() {
        val result = ProductLessonStartupPolicy.decide(
            startFreshRequested = false,
            recoveryOutcome = LessonLabRecoveryOutcome.RESTORED,
            recoveredState = LessonSessionState.Ready,
        )

        assertEquals(ProductLessonStartupDecision.RESUME_RESTORED, result)
    }

    @Test
    fun continue_never_silently_creates_new_session_when_recovery_is_missing() {
        val result = ProductLessonStartupPolicy.decide(
            startFreshRequested = false,
            recoveryOutcome = LessonLabRecoveryOutcome.NONE,
            recoveredState = null,
        )

        assertEquals(ProductLessonStartupDecision.RESUME_NOT_FOUND, result)
    }

    @Test
    fun continue_does_not_treat_terminal_state_as_resumable() {
        val result = ProductLessonStartupPolicy.decide(
            startFreshRequested = false,
            recoveryOutcome = LessonLabRecoveryOutcome.RESTORED,
            recoveredState = LessonSessionState.Finished(
                reason = LessonFinishReason.FINISHED_FOR_NOW,
            ),
        )

        assertEquals(ProductLessonStartupDecision.RESUME_NOT_FOUND, result)
    }
}
