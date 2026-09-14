package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.product.profile.AgeBand

enum class CompanionSemanticState {
    INTRODUCING_ACTIVITY,
    DEMONSTRATING,
    WATCHING_CHILD,
    HELPING,
    PAUSED,
    CELEBRATING_ARTWORK,
    GENTLE_ERROR,
    IDLE_PRESENT,
}

data class LessonWorkspacePresentation(
    val companionState: CompanionSemanticState,
    val eyebrow: String,
    val instruction: String,
    val secondaryCue: String?,
    val reflectionPrompt: String?,
    val stepLabel: String?,
    val progress: Float,
    val showPause: Boolean,
    val showResume: Boolean,
    val showReplay: Boolean,
    val showHelp: Boolean,
    val showReduceHelp: Boolean,
    val showDismissHelp: Boolean,
    val showDone: Boolean,
    val showSkipOverview: Boolean,
    val showSkipStep: Boolean,
    val showRetry: Boolean,
    val showPostDrawingChoices: Boolean,
    val isTerminal: Boolean,
)

/**
 * Pure, read-only presentation policy for the lesson companion.
 *
 * Lesson/session truth remains owned by [LessonSessionState] and validated lesson content. This
 * policy only converts that truth into age-appropriate words and control visibility. It never
 * mutates progress, Help level, artwork, persistence, or completion state.
 */
object ProductLessonPresentationPolicy {
    fun from(
        state: LessonSessionState?,
        packageData: LessonRuntimePackage?,
        ageBand: AgeBand = AgeBand.CREATIVE_EXPLORER,
    ): LessonWorkspacePresentation {
        val totalSteps = packageData?.lesson?.drawing?.steps?.size?.coerceAtLeast(1) ?: 1
        val contextual = state as? LessonSessionState.Contextual
        val context = contextual?.context
        val step = context?.currentStepIndex?.let { index ->
            packageData?.lesson?.drawing?.steps?.getOrNull(index)
        }
        val stepLabel = step?.id?.toChildLabel()
        val progress = context?.currentStepIndex?.let { index ->
            (index.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
        } ?: if (state is LessonSessionState.Finished) 1f else 0f
        val manualDoneAllowed = step?.childTurn?.completionPolicy == ChildCompletionPolicy.MANUAL_DONE
        val openChoice = step.isOpenChoiceStep()
        val mode = context?.mode

        return when (state) {
            null,
            LessonSessionState.Ready,
            -> base(
                companionState = CompanionSemanticState.INTRODUCING_ACTIVITY,
                eyebrow = readyEyebrow(ageBand),
                instruction = readyInstruction(ageBand),
                secondaryCue = readySecondaryCue(ageBand),
                stepLabel = "Drawing",
                progress = 0f,
            )

            is LessonSessionState.OverviewDemonstrating -> base(
                companionState = CompanionSemanticState.DEMONSTRATING,
                eyebrow = overviewEyebrow(ageBand),
                instruction = overviewInstruction(ageBand),
                secondaryCue = overviewSecondaryCue(ageBand),
                stepLabel = stepLabel,
                progress = progress,
                showPause = true,
                showSkipOverview = true,
            )

            is LessonSessionState.PreparingStep,
            is LessonSessionState.TeacherDemonstrating,
            -> base(
                companionState = CompanionSemanticState.DEMONSTRATING,
                eyebrow = demonstrationEyebrow(ageBand),
                instruction = demonstrationInstruction(ageBand, mode),
                secondaryCue = demonstrationSecondaryCue(
                    ageBand = ageBand,
                    replay = (state as? LessonSessionState.TeacherDemonstrating)?.replay == true,
                ),
                stepLabel = stepLabel,
                progress = progress,
                showPause = true,
            )

            is LessonSessionState.AwaitingChild -> base(
                companionState = CompanionSemanticState.WATCHING_CHILD,
                eyebrow = childTurnEyebrow(ageBand),
                instruction = childTurnInstruction(ageBand, mode, openChoice),
                secondaryCue = childTurnSecondaryCue(
                    ageBand = ageBand,
                    mode = mode,
                    openChoice = openChoice,
                    replayAvailable = step?.childTurn?.allowReplay == true,
                ),
                stepLabel = stepLabel,
                progress = progress,
                showPause = true,
                showReplay = step?.childTurn?.allowReplay == true,
                showHelp = step?.help?.isNotEmpty() == true,
                showDone = manualDoneAllowed,
                showSkipStep = step?.childTurn?.allowSkip == true,
            )

            is LessonSessionState.HelpActive -> base(
                companionState = CompanionSemanticState.HELPING,
                eyebrow = helpEyebrow(ageBand),
                instruction = helpInstruction(ageBand, state.context.helpLevel),
                secondaryCue = helpSecondaryCue(ageBand),
                stepLabel = stepLabel,
                progress = progress,
                showPause = true,
                showReplay = step?.childTurn?.allowReplay == true,
                showHelp = step?.help?.any { it.level > state.context.helpLevel } == true,
                showReduceHelp = true,
                showDismissHelp = true,
                showDone = manualDoneAllowed,
                showSkipStep = step?.childTurn?.allowSkip == true,
            )

            is LessonSessionState.CompletingStep -> base(
                companionState = CompanionSemanticState.IDLE_PRESENT,
                eyebrow = completionTransitionEyebrow(ageBand),
                instruction = completionTransitionInstruction(ageBand),
                stepLabel = stepLabel,
                progress = progress,
            )

            is LessonSessionState.Paused -> base(
                companionState = CompanionSemanticState.PAUSED,
                eyebrow = pausedEyebrow(ageBand),
                instruction = pausedInstruction(ageBand),
                secondaryCue = pausedSecondaryCue(ageBand),
                stepLabel = stepLabel,
                progress = progress,
                showResume = true,
            )

            is LessonSessionState.DrawingComplete,
            is LessonSessionState.AwaitingPostDrawingChoice,
            -> base(
                companionState = CompanionSemanticState.CELEBRATING_ARTWORK,
                eyebrow = artworkCompleteEyebrow(ageBand),
                instruction = artworkCompleteInstruction(ageBand),
                secondaryCue = artworkCompleteSecondaryCue(ageBand),
                reflectionPrompt = reflectionPrompt(ageBand),
                stepLabel = "Drawing complete",
                progress = 1f,
                showPostDrawingChoices = true,
            )

            is LessonSessionState.HandingOffToColoring -> base(
                companionState = CompanionSemanticState.IDLE_PRESENT,
                eyebrow = handoffEyebrow(ageBand),
                instruction = handoffInstruction(ageBand),
                stepLabel = "Drawing complete",
                progress = 1f,
            )

            is LessonSessionState.RecoverableError -> base(
                companionState = CompanionSemanticState.GENTLE_ERROR,
                eyebrow = errorEyebrow(ageBand),
                instruction = recoverableErrorInstruction(ageBand),
                stepLabel = stepLabel,
                progress = progress,
                showRetry = true,
            )

            is LessonSessionState.FatalContentError -> base(
                companionState = CompanionSemanticState.GENTLE_ERROR,
                eyebrow = "DRAWING SAVED",
                instruction = fatalErrorInstruction(ageBand),
                stepLabel = "Drawing",
                progress = 0f,
                isTerminal = true,
            )

            is LessonSessionState.Finished -> base(
                companionState = CompanionSemanticState.IDLE_PRESENT,
                eyebrow = finishedEyebrow(ageBand),
                instruction = finishedInstruction(ageBand),
                stepLabel = "Finished for now",
                progress = 1f,
                isTerminal = true,
            )
        }
    }

    private fun base(
        companionState: CompanionSemanticState,
        eyebrow: String,
        instruction: String,
        secondaryCue: String? = null,
        reflectionPrompt: String? = null,
        stepLabel: String?,
        progress: Float,
        showPause: Boolean = false,
        showResume: Boolean = false,
        showReplay: Boolean = false,
        showHelp: Boolean = false,
        showReduceHelp: Boolean = false,
        showDismissHelp: Boolean = false,
        showDone: Boolean = false,
        showSkipOverview: Boolean = false,
        showSkipStep: Boolean = false,
        showRetry: Boolean = false,
        showPostDrawingChoices: Boolean = false,
        isTerminal: Boolean = false,
    ) = LessonWorkspacePresentation(
        companionState = companionState,
        eyebrow = eyebrow,
        instruction = instruction,
        secondaryCue = secondaryCue,
        reflectionPrompt = reflectionPrompt,
        stepLabel = stepLabel,
        progress = progress,
        showPause = showPause,
        showResume = showResume,
        showReplay = showReplay,
        showHelp = showHelp,
        showReduceHelp = showReduceHelp,
        showDismissHelp = showDismissHelp,
        showDone = showDone,
        showSkipOverview = showSkipOverview,
        showSkipStep = showSkipStep,
        showRetry = showRetry,
        showPostDrawingChoices = showPostDrawingChoices,
        isTerminal = isTerminal,
    )

    private fun DrawingStep?.isOpenChoiceStep(): Boolean =
        this != null &&
            childTurn.completionPolicy == ChildCompletionPolicy.MANUAL_DONE &&
            childTurn.expectedStrokeRefs.isEmpty()

    private fun readyEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "LET’S DRAW"
        AgeBand.CREATIVE_EXPLORER -> "READY TO DRAW"
        AgeBand.GROWING_ARTIST -> "READY TO BUILD"
        AgeBand.YOUNG_ARTIST -> "STUDIO READY"
    }

    private fun readyInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "We’ll draw one small part at a time. Take your time."
        AgeBand.CREATIVE_EXPLORER -> "We’ll build the drawing one part at a time. You can make choices as we go."
        AgeBand.GROWING_ARTIST -> "We’ll build the drawing step by step. Watch the structure, then make your own marks."
        AgeBand.YOUNG_ARTIST -> "We’ll work through the drawing in clear stages. Observe first, then draw with intention."
    }

    private fun readySecondaryCue(ageBand: AgeBand): String? = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> null
        AgeBand.CREATIVE_EXPLORER -> "There’s no race. Use the speed that feels comfortable."
        AgeBand.GROWING_ARTIST -> "Use Replay or Help whenever another look would be useful."
        AgeBand.YOUNG_ARTIST -> "Use Replay or Help as studio tools whenever you want another reference."
    }

    private fun overviewEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "WATCH THE WHOLE DRAWING"
        else -> "FULL DRAWING PREVIEW"
    }

    private fun overviewInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "Watch the whole drawing once. Then you can try it."
        AgeBand.CREATIVE_EXPLORER -> "Watch how the whole drawing comes together. Then you’ll draw it in parts."
        AgeBand.GROWING_ARTIST -> "Watch the full sequence once and notice how the larger parts connect before your turn."
        AgeBand.YOUNG_ARTIST -> "Observe the full construction once—overall structure first, details later. Then you’ll work through it in stages."
    }

    private fun overviewSecondaryCue(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "Tap I’m ready if you want to start sooner."
        else -> "You can choose I’m ready when you have seen enough."
    }

    private fun demonstrationEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "MY TURN — WATCH"
        AgeBand.CREATIVE_EXPLORER -> "TEACHER TURN"
        AgeBand.GROWING_ARTIST -> "WATCH THIS STEP"
        AgeBand.YOUNG_ARTIST -> "OBSERVE THIS STEP"
    }

    private fun demonstrationInstruction(ageBand: AgeBand, mode: TeachingMode?): String = when (mode) {
        TeachingMode.TRACE_AND_LEARN -> when (ageBand) {
            AgeBand.LITTLE_ARTIST -> "Watch where the line starts and how it moves."
            AgeBand.CREATIVE_EXPLORER -> "Watch the path and direction of the line before you follow the guide."
            AgeBand.GROWING_ARTIST -> "Notice the start point, direction, and motion before you practice the guided path."
            AgeBand.YOUNG_ARTIST -> "Study the stroke path and direction before practicing it with the guide."
        }
        else -> when (ageBand) {
            AgeBand.LITTLE_ARTIST -> "Watch this part. Look at where the line starts."
            AgeBand.CREATIVE_EXPLORER -> "Watch how this part is built, then it will be your turn."
            AgeBand.GROWING_ARTIST -> "Notice the main shape and placement before your turn."
            AgeBand.YOUNG_ARTIST -> "Observe the structure and placement of this step before drawing it."
        }
    }

    private fun demonstrationSecondaryCue(ageBand: AgeBand, replay: Boolean): String? = when {
        replay && ageBand == AgeBand.LITTLE_ARTIST -> "This is another look. Watch any bit you want to see again."
        replay -> "Replay is another reference pass—focus on the part you wanted to see again."
        ageBand == AgeBand.YOUNG_ARTIST -> "Focus on the construction, not on memorizing every line."
        else -> null
    }

    private fun childTurnEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "YOUR TURN"
        AgeBand.CREATIVE_EXPLORER -> "YOUR TURN"
        AgeBand.GROWING_ARTIST -> "YOUR TURN — DRAW"
        AgeBand.YOUNG_ARTIST -> "YOUR TURN — APPLY IT"
    }

    private fun childTurnInstruction(
        ageBand: AgeBand,
        mode: TeachingMode?,
        openChoice: Boolean,
    ): String {
        if (openChoice) {
            return when (ageBand) {
                AgeBand.LITTLE_ARTIST -> "Now add this part your way. Pick the details you want."
                AgeBand.CREATIVE_EXPLORER -> "Choose, change, or invent the details that make this part yours."
                AgeBand.GROWING_ARTIST -> "Use the idea as a starting point, then design the details your way."
                AgeBand.YOUNG_ARTIST -> "Treat the example as a reference. Choose or invent the details that fit your design."
            }
        }

        if (mode == TeachingMode.TRACE_AND_LEARN) {
            return when (ageBand) {
                AgeBand.LITTLE_ARTIST -> "Follow the guide slowly. Your hand can take its time."
                AgeBand.CREATIVE_EXPLORER -> "Use the guide as a path, and let your line still be your own."
                AgeBand.GROWING_ARTIST -> "Follow the guide to practice the motion. Exact matching is not required."
                AgeBand.YOUNG_ARTIST -> "Use the guide to practice the stroke path. Focus on control, not perfect matching."
            }
        }

        return when (ageBand) {
            AgeBand.LITTLE_ARTIST -> "Draw this part now. Your line can look like yours."
            AgeBand.CREATIVE_EXPLORER -> "Use what you noticed and draw this part in your own way."
            AgeBand.GROWING_ARTIST -> "Use the shape and placement you observed. Your line does not need to match exactly."
            AgeBand.YOUNG_ARTIST -> "Apply the structure you observed. Matching the demonstration exactly is not the goal."
        }
    }

    private fun childTurnSecondaryCue(
        ageBand: AgeBand,
        mode: TeachingMode?,
        openChoice: Boolean,
        replayAvailable: Boolean,
    ): String? = when {
        openChoice && ageBand == AgeBand.LITTLE_ARTIST -> "There isn’t one right version."
        openChoice -> "There is no single correct version of this step."
        mode == TeachingMode.WATCH_THEN_DRAW && replayAvailable && ageBand == AgeBand.LITTLE_ARTIST ->
            "Try what you remember. Replay is there if you want another look."
        mode == TeachingMode.WATCH_THEN_DRAW && replayAvailable ->
            "Try from observation or memory first; Replay is available whenever you want another reference."
        mode == TeachingMode.TRACE_AND_LEARN && ageBand != AgeBand.LITTLE_ARTIST ->
            "The guide is practice support. It is there to help you learn the motion."
        else -> null
    }

    private fun helpEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "LET’S DO IT TOGETHER"
        AgeBand.CREATIVE_EXPLORER -> "A LITTLE HELP"
        AgeBand.GROWING_ARTIST -> "GUIDE ON"
        AgeBand.YOUNG_ARTIST -> "REFERENCE SUPPORT"
    }

    private fun helpInstruction(ageBand: AgeBand, level: Int): String = when {
        level >= 4 -> when (ageBand) {
            AgeBand.LITTLE_ARTIST -> "A strong guide is here. Follow it slowly, one bit at a time."
            AgeBand.CREATIVE_EXPLORER -> "Use the strong guide or tracing path as practice. Go at your own speed."
            AgeBand.GROWING_ARTIST -> "Use the guide or trace path to practice the motion. Taking direct support is part of learning."
            AgeBand.YOUNG_ARTIST -> "Use the direct guide or trace path as a practice tool. Focus on the motion rather than perfect matching."
        }
        level >= 2 -> when (ageBand) {
            AgeBand.LITTLE_ARTIST -> "Here’s a clearer path. Look where it starts, then try."
            AgeBand.CREATIVE_EXPLORER -> "Use the guide as a map for the start, direction, and shape."
            AgeBand.GROWING_ARTIST -> "Use the visual guide to check direction and placement, then make the stroke yourself."
            AgeBand.YOUNG_ARTIST -> "Use the guide to inspect direction and placement, then return to your own stroke."
        }
        else -> when (ageBand) {
            AgeBand.LITTLE_ARTIST -> "Here’s one small hint. Look, then try again."
            AgeBand.CREATIVE_EXPLORER -> "Here’s a small cue about where to look. Try the shape when you’re ready."
            AgeBand.GROWING_ARTIST -> "Use this small cue to re-check the shape or starting point, then continue."
            AgeBand.YOUNG_ARTIST -> "Use the cue as a quick reference, then continue when you have what you need."
        }
    }

    private fun helpSecondaryCue(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "You can ask for more help, use less, or hide it when you’re ready."
        AgeBand.CREATIVE_EXPLORER -> "You control the support: ask for more, use less, or hide it."
        AgeBand.GROWING_ARTIST -> "Adjust the support whenever you want—more, less, or hidden."
        AgeBand.YOUNG_ARTIST -> "Support is under your control; increase it, reduce it, or hide it as needed."
    }

    private fun completionTransitionEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "PART ADDED"
        else -> "STEP COMPLETE"
    }

    private fun completionTransitionInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "That part is in. I’ll get the next part ready."
        AgeBand.CREATIVE_EXPLORER -> "That part is in. Let’s get the next one ready."
        AgeBand.GROWING_ARTIST -> "That step is recorded. The next construction step is coming up."
        AgeBand.YOUNG_ARTIST -> "That step is recorded. Preparing the next stage."
    }

    private fun pausedEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "PAUSED"
        AgeBand.CREATIVE_EXPLORER -> "PAUSED"
        AgeBand.GROWING_ARTIST -> "STUDIO PAUSED"
        AgeBand.YOUNG_ARTIST -> "SESSION PAUSED"
    }

    private fun pausedInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "Your drawing is safe. Come back when you’re ready."
        AgeBand.CREATIVE_EXPLORER -> "Your drawing is safe. Take your time and resume when you want."
        AgeBand.GROWING_ARTIST -> "Your drawing is safe. Resume whenever you’re ready to continue the step."
        AgeBand.YOUNG_ARTIST -> "Your work is safe. Resume when you’re ready to continue."
    }

    private fun pausedSecondaryCue(ageBand: AgeBand): String? = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> null
        else -> "There is no time limit."
    }

    private fun artworkCompleteEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "YOU MADE A DRAWING"
        AgeBand.CREATIVE_EXPLORER -> "YOUR DRAWING IS READY"
        AgeBand.GROWING_ARTIST -> "DRAWING COMPLETE"
        AgeBand.YOUNG_ARTIST -> "STUDY COMPLETE"
    }

    private fun artworkCompleteInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "You made it one part at a time. It’s your drawing."
        AgeBand.CREATIVE_EXPLORER -> "You built this drawing step by step and made your own choices along the way."
        AgeBand.GROWING_ARTIST -> "You worked through the construction and made the final drawing your own."
        AgeBand.YOUNG_ARTIST -> "You completed the study and made your own decisions through the process."
    }

    private fun artworkCompleteSecondaryCue(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "You can color it, or finish for now."
        AgeBand.CREATIVE_EXPLORER -> "Choose whether to color now or save the drawing for later."
        AgeBand.GROWING_ARTIST -> "You can continue into color or keep the drawing as it is."
        AgeBand.YOUNG_ARTIST -> "Continue into color if it serves the piece, or keep the drawing as a finished study."
    }

    private fun reflectionPrompt(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "Notice one part you enjoyed making."
        AgeBand.CREATIVE_EXPLORER -> "Which detail feels most like your choice?"
        AgeBand.GROWING_ARTIST -> "Which choice changed the drawing most for you?"
        AgeBand.YOUNG_ARTIST -> "If you made another version, what would you refine, simplify, or change?"
    }

    private fun handoffEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.YOUNG_ARTIST -> "DRAWING SAVED"
        else -> "DRAWING SAFE"
    }

    private fun handoffInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "Your drawing is safe while colors get ready."
        AgeBand.CREATIVE_EXPLORER -> "Your drawing is safe while the coloring step gets ready."
        AgeBand.GROWING_ARTIST -> "Your finished drawing is saved while the coloring workspace gets ready."
        AgeBand.YOUNG_ARTIST -> "The drawing is preserved while the color workspace opens."
    }

    private fun errorEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "LET’S TRY THAT PART AGAIN"
        else -> "LET’S TRY THAT AGAIN"
    }

    private fun recoverableErrorInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "That part didn’t play. Your drawing is safe, so we can try again."
        AgeBand.CREATIVE_EXPLORER -> "That part didn’t play correctly, but your drawing is safe. Try it again when you’re ready."
        AgeBand.GROWING_ARTIST -> "The demonstration hit a problem, but your drawing is safe. Retry the step when you’re ready."
        AgeBand.YOUNG_ARTIST -> "The demonstration could not complete, but your work is safe. Retry when you’re ready."
    }

    private fun fatalErrorInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "This lesson needs a refresh. Your drawing stays safe."
        AgeBand.CREATIVE_EXPLORER -> "This lesson needs a refresh, but your artwork stays safe."
        AgeBand.GROWING_ARTIST -> "This lesson content needs a refresh. Your artwork remains safe."
        AgeBand.YOUNG_ARTIST -> "This lesson cannot continue until its content is refreshed. Your work remains safe."
    }

    private fun finishedEyebrow(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "SAVED FOR YOU"
        AgeBand.CREATIVE_EXPLORER -> "SAVED FOR YOU"
        AgeBand.GROWING_ARTIST -> "WORK SAVED"
        AgeBand.YOUNG_ARTIST -> "SESSION SAVED"
    }

    private fun finishedInstruction(ageBand: AgeBand): String = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> "Your drawing is safe in your studio."
        AgeBand.CREATIVE_EXPLORER -> "Your drawing is safe in your studio whenever you want to return."
        AgeBand.GROWING_ARTIST -> "Your drawing is saved and ready whenever you want to continue."
        AgeBand.YOUNG_ARTIST -> "Your work is saved and can be revisited later."
    }

    private fun String.toChildLabel(): String = split('_', '-')
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }
}
