package com.navin.kidsdrawing.product.adaptive

import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.ChildTurn
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.HelpEntry
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeacherDemo
import com.navin.kidsdrawing.product.profile.AgeBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdaptiveHelpSuggestionPolicyTest {
    @Test
    fun no_suggestion_exists_until_child_explicitly_requests_help() {
        val suggestion = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = false,
            ageBand = AgeBand.GROWING_ARTIST,
            step = step(),
            currentHelpLevel = 0,
            state = LocalAdaptiveState.empty(),
        )

        assertNull(suggestion)
    }

    @Test
    fun missing_adaptive_state_preserves_original_next_authored_help_order() {
        val suggestion = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = AgeBand.YOUNG_ARTIST,
            step = step(allowReplay = true),
            currentHelpLevel = 0,
            state = null,
        )

        assertEquals(AdaptiveHelpAction.NEXT_AUTHORED_HELP, suggestion?.action)
        assertEquals(1, suggestion?.authoredHelp?.level)
        assertEquals(HelpKind.GENTLE_HINT, suggestion?.authoredHelp?.kind)
    }

    @Test
    fun little_artist_keeps_gentle_authored_help_even_after_repeated_requests() {
        val suggestion = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = AgeBand.LITTLE_ARTIST,
            step = step(allowReplay = true),
            currentHelpLevel = 0,
            state = LocalAdaptiveState(
                helpRequestCounts = mapOf("kind:GENTLE_HINT" to 5),
            ),
        )

        assertEquals(AdaptiveHelpAction.NEXT_AUTHORED_HELP, suggestion?.action)
    }

    @Test
    fun repeated_same_authored_help_can_offer_authored_replay_for_older_child() {
        val suggestion = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = AgeBand.GROWING_ARTIST,
            step = step(allowReplay = true),
            currentHelpLevel = 0,
            state = LocalAdaptiveState(
                helpRequestCounts = mapOf("kind:GENTLE_HINT" to 2),
            ),
        )

        assertEquals(AdaptiveHelpAction.REPLAY, suggestion?.action)
        assertNull(suggestion?.authoredHelp)
    }

    @Test
    fun recorded_replay_prevents_replay_loop_and_returns_to_authored_help() {
        val suggestion = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = AgeBand.YOUNG_ARTIST,
            step = step(allowReplay = true),
            currentHelpLevel = 0,
            state = LocalAdaptiveState(
                helpRequestCounts = mapOf(
                    "kind:GENTLE_HINT" to 2,
                    "choice:REPLAY" to 1,
                ),
            ),
        )

        assertEquals(AdaptiveHelpAction.NEXT_AUTHORED_HELP, suggestion?.action)
    }

    @Test
    fun replay_is_never_invented_when_step_does_not_author_it() {
        val suggestion = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = AgeBand.YOUNG_ARTIST,
            step = step(allowReplay = false),
            currentHelpLevel = 0,
            state = LocalAdaptiveState(
                helpRequestCounts = mapOf("kind:GENTLE_HINT" to 10),
            ),
        )

        assertEquals(AdaptiveHelpAction.NEXT_AUTHORED_HELP, suggestion?.action)
        assertEquals(HelpKind.GENTLE_HINT, suggestion?.authoredHelp?.kind)
    }

    @Test
    fun trace_is_never_invented_when_current_step_does_not_author_trace() {
        val suggestion = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = AgeBand.CREATIVE_EXPLORER,
            step = step(allowReplay = false),
            currentHelpLevel = 1,
            state = LocalAdaptiveState.empty(),
        )

        assertEquals(AdaptiveHelpAction.NEXT_AUTHORED_HELP, suggestion?.action)
        assertEquals(HelpKind.VISUAL_GUIDE, suggestion?.authoredHelp?.kind)
    }

    @Test
    fun identical_inputs_produce_identical_suggestion() {
        val state = LocalAdaptiveState(
            helpRequestCounts = mapOf("kind:GENTLE_HINT" to 2),
        )
        val first = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = AgeBand.GROWING_ARTIST,
            step = step(),
            currentHelpLevel = 0,
            state = state,
        )
        val second = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = AgeBand.GROWING_ARTIST,
            step = step(),
            currentHelpLevel = 0,
            state = state,
        )

        assertEquals(first, second)
    }

    private fun step(allowReplay: Boolean = true) = DrawingStep(
        id = "body",
        objectiveSkillIds = listOf("shape.combine"),
        teacher = TeacherDemo(strokeRefs = listOf("body-stroke")),
        childTurn = ChildTurn(
            completionPolicy = ChildCompletionPolicy.MANUAL_DONE,
            allowReplay = allowReplay,
            allowSkip = false,
        ),
        help = listOf(
            HelpEntry(level = 1, kind = HelpKind.GENTLE_HINT),
            HelpEntry(level = 2, kind = HelpKind.VISUAL_GUIDE, guideRefs = listOf("body-guide")),
        ),
    )
}
