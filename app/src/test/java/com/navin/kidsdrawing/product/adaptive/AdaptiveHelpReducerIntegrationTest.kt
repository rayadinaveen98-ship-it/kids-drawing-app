package com.navin.kidsdrawing.product.adaptive

import com.navin.kidsdrawing.lesson.model.HelpKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AdaptiveHelpReducerIntegrationTest {
    @Test
    fun accepted_authored_help_records_only_bounded_authored_context_and_choice() {
        val event = AdaptiveEvent.HelpRequested(
            eventKey = "help:session:body:level:2:r0",
            lessonId = "simple-body-and-pose",
            lessonRevision = 1,
            skillIds = listOf("body.pose"),
            categoryIds = listOf("characters"),
            helpKind = HelpKind.VISUAL_GUIDE,
            choice = AdaptiveHelpChoice.AUTHORED_HELP,
        )

        val state = LocalAdaptiveReducer.reduce(LocalAdaptiveState.empty(), event)

        assertEquals(1, state.helpRequestCounts["skill:body.pose"])
        assertEquals(1, state.helpRequestCounts["category:characters"])
        assertEquals(1, state.helpRequestCounts["kind:VISUAL_GUIDE"])
        assertEquals(1, state.helpRequestCounts["choice:AUTHORED_HELP"])
        assertFalse(state.helpRequestCounts.keys.any { key ->
            key.contains("stroke", ignoreCase = true) ||
                key.contains("score", ignoreCase = true) ||
                key.contains("accuracy", ignoreCase = true) ||
                key.contains("ability", ignoreCase = true)
        })
    }

    @Test
    fun replay_help_request_is_idempotent_and_has_no_fabricated_help_kind() {
        val event = AdaptiveEvent.HelpRequested(
            eventKey = "help:session:body:replay:req-7",
            lessonId = "simple-body-and-pose",
            lessonRevision = 1,
            skillIds = listOf("body.pose"),
            categoryIds = listOf("characters"),
            helpKind = null,
            choice = AdaptiveHelpChoice.REPLAY,
        )

        val once = LocalAdaptiveReducer.reduce(LocalAdaptiveState.empty(), event)
        val twice = LocalAdaptiveReducer.reduce(once, event)

        assertEquals(once, twice)
        assertEquals(1, once.helpRequestCounts["choice:REPLAY"])
        assertFalse(once.helpRequestCounts.keys.any { it.startsWith("kind:") })
    }
}
