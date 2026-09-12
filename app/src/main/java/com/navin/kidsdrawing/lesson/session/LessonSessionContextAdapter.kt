package com.navin.kidsdrawing.lesson.session

/**
 * Narrowing adapter used by the session engine after it has proved the current state is contextual.
 * Keeping the copy logic inside the lesson/session package prevents callers from setting arbitrary
 * engine state while still allowing immutable context updates such as a pace change.
 */
internal fun replaceContext(
    state: LessonSessionState,
    context: LessonActiveContext,
): LessonSessionState = when (state) {
    is LessonSessionState.OverviewDemonstrating -> state.copy(context = context)
    is LessonSessionState.PreparingStep -> state.copy(context = context)
    is LessonSessionState.TeacherDemonstrating -> state.copy(context = context)
    is LessonSessionState.AwaitingChild -> state.copy(context = context)
    is LessonSessionState.HelpActive -> state.copy(context = context)
    is LessonSessionState.CompletingStep -> state.copy(context = context)
    is LessonSessionState.Paused -> LessonSessionState.Paused(
        replacePausableContextForAdapter(state.previousStableState, context),
    )
    is LessonSessionState.DrawingComplete -> state.copy(context = context)
    is LessonSessionState.AwaitingPostDrawingChoice -> state.copy(context = context)
    is LessonSessionState.HandingOffToColoring -> state.copy(context = context)
    is LessonSessionState.RecoverableError -> state.copy(context = context)
    LessonSessionState.Ready,
    is LessonSessionState.Finished,
    is LessonSessionState.FatalContentError,
    -> error("replaceContext requires a contextual lesson state.")
}

private fun replacePausableContextForAdapter(
    state: LessonSessionState.Pausable,
    context: LessonActiveContext,
): LessonSessionState.Pausable = when (state) {
    is LessonSessionState.OverviewDemonstrating -> state.copy(context = context)
    is LessonSessionState.PreparingStep -> state.copy(context = context)
    is LessonSessionState.TeacherDemonstrating -> state.copy(context = context)
    is LessonSessionState.AwaitingChild -> state.copy(context = context)
    is LessonSessionState.HelpActive -> state.copy(context = context)
}
