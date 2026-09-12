package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.session.LessonSessionState

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

object ProductLessonPresentationPolicy {
    fun from(
        state: LessonSessionState?,
        packageData: LessonRuntimePackage?,
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

        return when (state) {
            null,
            LessonSessionState.Ready,
            -> base(
                companionState = CompanionSemanticState.INTRODUCING_ACTIVITY,
                eyebrow = "READY TO DRAW",
                instruction = "We’ll make this one part at a time. No hurry.",
                stepLabel = null,
                progress = 0f,
            )

            is LessonSessionState.OverviewDemonstrating -> base(
                companionState = CompanionSemanticState.DEMONSTRATING,
                eyebrow = "WATCH FIRST",
                instruction = "Watch the whole drawing once. You can skip when you’re ready to try.",
                stepLabel = stepLabel,
                progress = progress,
                showPause = true,
                showSkipOverview = true,
            )

            is LessonSessionState.PreparingStep,
            is LessonSessionState.TeacherDemonstrating,
            -> base(
                companionState = CompanionSemanticState.DEMONSTRATING,
                eyebrow = "WATCH THIS PART",
                instruction = "See how this shape moves, then it’s your turn.",
                stepLabel = stepLabel,
                progress = progress,
                showPause = true,
            )

            is LessonSessionState.AwaitingChild -> base(
                companionState = CompanionSemanticState.WATCHING_CHILD,
                eyebrow = "YOUR TURN",
                instruction = "Draw this part your way. It doesn’t need to match perfectly.",
                stepLabel = stepLabel,
                progress = progress,
                showPause = true,
                showReplay = true,
                showHelp = true,
                showDone = true,
                showSkipStep = step?.childTurn?.allowSkip == true,
            )

            is LessonSessionState.HelpActive -> base(
                companionState = CompanionSemanticState.HELPING,
                eyebrow = "A LITTLE HELP",
                instruction = helpCopy(state.context.helpLevel),
                stepLabel = stepLabel,
                progress = progress,
                showPause = true,
                showReplay = true,
                showHelp = true,
                showReduceHelp = true,
                showDismissHelp = true,
                showDone = true,
                showSkipStep = step?.childTurn?.allowSkip == true,
            )

            is LessonSessionState.CompletingStep -> base(
                companionState = CompanionSemanticState.IDLE_PRESENT,
                eyebrow = "NICE TRYING",
                instruction = "That part is in. Let’s get the next one ready.",
                stepLabel = stepLabel,
                progress = progress,
            )

            is LessonSessionState.Paused -> base(
                companionState = CompanionSemanticState.PAUSED,
                eyebrow = "PAUSED",
                instruction = "Your drawing is safe. Take your time.",
                stepLabel = stepLabel,
                progress = progress,
                showResume = true,
            )

            is LessonSessionState.DrawingComplete,
            is LessonSessionState.AwaitingPostDrawingChoice,
            is LessonSessionState.HandingOffToColoring,
            -> base(
                companionState = CompanionSemanticState.CELEBRATING_ARTWORK,
                eyebrow = "YOU MADE IT",
                instruction = "Your cat is drawn. You can color it or finish for now.",
                stepLabel = "Drawing complete",
                progress = 1f,
                showPostDrawingChoices = state !is LessonSessionState.HandingOffToColoring,
            )

            is LessonSessionState.RecoverableError -> base(
                companionState = CompanionSemanticState.GENTLE_ERROR,
                eyebrow = "LET’S TRY THAT AGAIN",
                instruction = "That part didn’t play correctly, but your drawing is safe.",
                stepLabel = stepLabel,
                progress = progress,
                showRetry = true,
            )

            is LessonSessionState.FatalContentError -> base(
                companionState = CompanionSemanticState.GENTLE_ERROR,
                eyebrow = "DRAWING SAVED",
                instruction = "This lesson needs a refresh. Your artwork stays safe.",
                stepLabel = null,
                progress = 0f,
                isTerminal = true,
            )

            is LessonSessionState.Finished -> base(
                companionState = CompanionSemanticState.IDLE_PRESENT,
                eyebrow = "SAVED FOR YOU",
                instruction = "Your drawing is safe in your studio.",
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

    private fun helpCopy(level: Int): String = when {
        level >= 4 -> "Tracing is a normal way to learn. Follow the guide at your own speed."
        level >= 3 -> "Look for the start and direction. We can do this part together."
        level >= 2 -> "Use the guide as a soft map. Your line can still be your own."
        else -> "Here’s a small hint. Take your time and try the shape again."
    }

    private fun String.toChildLabel(): String = split('_', '-')
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }
}
