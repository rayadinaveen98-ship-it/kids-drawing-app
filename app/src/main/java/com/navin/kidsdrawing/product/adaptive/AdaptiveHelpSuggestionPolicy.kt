package com.navin.kidsdrawing.product.adaptive

import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.HelpEntry
import com.navin.kidsdrawing.product.profile.AgeBand

enum class AdaptiveHelpAction {
    NEXT_AUTHORED_HELP,
    REPLAY,
}

data class AdaptiveHelpSuggestion(
    val action: AdaptiveHelpAction,
    val authoredHelp: HelpEntry?,
    val reasonCopy: String,
)

/**
 * Pure child-controlled Help policy.
 *
 * This policy is advisory only and is called only after the child explicitly taps Help. It can
 * choose only between the next authored Help entry and an already-authored Replay capability.
 */
object AdaptiveHelpSuggestionPolicy {
    fun suggest(
        childRequested: Boolean,
        ageBand: AgeBand,
        step: DrawingStep,
        currentHelpLevel: Int,
        state: LocalAdaptiveState?,
    ): AdaptiveHelpSuggestion? {
        if (!childRequested) return null

        val nextHelp = step.help
            .filter { it.level > currentHelpLevel }
            .minByOrNull { it.level }

        // Missing/corrupt/incompatible adaptive state preserves the original authored Help order.
        if (state == null) {
            return nextHelp?.let(::nextAuthoredHelp)
        }

        if (nextHelp == null) {
            return if (step.childTurn.allowReplay) replaySuggestion() else null
        }
        if (!step.childTurn.allowReplay || ageBand == AgeBand.LITTLE_ARTIST) {
            return nextAuthoredHelp(nextHelp)
        }

        val sameKindRequests = state.helpRequestCounts["kind:${nextHelp.kind.name}"] ?: 0
        val replayRequests = state.helpRequestCounts["choice:${AdaptiveHelpChoice.REPLAY.name}"] ?: 0
        val alternateReferenceIsUseful = sameKindRequests >= 2 && replayRequests * 2 < sameKindRequests

        return if (alternateReferenceIsUseful) replaySuggestion() else nextAuthoredHelp(nextHelp)
    }

    private fun nextAuthoredHelp(help: HelpEntry) = AdaptiveHelpSuggestion(
        action = AdaptiveHelpAction.NEXT_AUTHORED_HELP,
        authoredHelp = help,
        reasonCopy = "Here’s the next guide for this step.",
    )

    private fun replaySuggestion() = AdaptiveHelpSuggestion(
        action = AdaptiveHelpAction.REPLAY,
        authoredHelp = null,
        reasonCopy = "Let’s watch this step once more.",
    )
}
