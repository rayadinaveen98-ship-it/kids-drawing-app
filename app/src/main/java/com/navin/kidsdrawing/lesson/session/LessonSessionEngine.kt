package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode

class LessonSessionEngine private constructor(
    val lessonPackage: LessonRuntimePackage,
    val identity: LessonSessionIdentity,
    private val clock: () -> Long,
    initialState: LessonSessionState,
) {
    var state: LessonSessionState = initialState
        private set

    fun dispatch(command: LessonCommand): LessonCommandResult = when (command) {
        is LessonCommand.StartLesson -> startLesson(command.mode, command.pace)
        LessonCommand.Pause -> pause()
        LessonCommand.Resume -> resume()
        LessonCommand.SaveAndExit -> saveAndExit()
        is LessonCommand.SetPace -> setPace(command.pace)
    }

    fun createSnapshot(): LessonSnapshotResult = snapshotForState(state, clock())

    private fun startLesson(mode: TeachingMode, pace: TeachingPace): LessonCommandResult {
        if (state != LessonSessionState.Ready) {
            return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "StartLesson is only valid from Ready.",
            )
        }
        if (mode !in lessonPackage.lesson.supportedModes) {
            return reject(
                LessonCommandRejectionCode.UNSUPPORTED_MODE,
                "Lesson ${lessonPackage.lesson.lessonId} does not support $mode.",
            )
        }

        val firstStep = lessonPackage.lesson.drawing.steps.first()
        val context = LessonActiveContext(
            mode = mode,
            pace = pace,
            currentStepIndex = 0,
            currentStepId = firstStep.id,
            helpLevel = 0,
            overviewCompleted = mode != TeachingMode.WATCH_THEN_DRAW,
        )
        val nextState: LessonSessionState = if (mode == TeachingMode.WATCH_THEN_DRAW) {
            LessonSessionState.OverviewDemonstrating(context)
        } else {
            LessonSessionState.PreparingStep(context)
        }

        return transition(
            nextState,
            LessonSessionEvent.SessionStarted(mode, pace),
        )
    }

    private fun pause(): LessonCommandResult {
        val current = state
        if (current !is LessonSessionState.Pausable) {
            return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "Pause is not valid from ${current::class.simpleName}.",
            )
        }

        return transition(
            LessonSessionState.Paused(current),
            LessonSessionEvent.SessionPaused(runtimePhaseOf(current)),
        )
    }

    private fun resume(): LessonCommandResult {
        val current = state
        if (current !is LessonSessionState.Paused) {
            return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "Resume is only valid from Paused.",
            )
        }

        val resumed = current.previousStableState
        return transition(
            resumed,
            LessonSessionEvent.SessionResumed(runtimePhaseOf(resumed)),
        )
    }

    private fun setPace(pace: TeachingPace): LessonCommandResult {
        val current = state
        val contextual = when (current) {
            is LessonSessionState.Contextual -> current
            else -> null
        } ?: return reject(
            LessonCommandRejectionCode.INVALID_STATE,
            "SetPace requires an active or paused lesson session.",
        )

        val previousPace = contextual.context.pace
        if (previousPace == pace) {
            return LessonCommandResult.Accepted(state, emptyList())
        }

        val updatedContext = contextual.context.copy(pace = pace)
        val nextState = replaceContext(current, updatedContext)
        return transition(
            nextState,
            LessonSessionEvent.PaceChanged(previousPace, pace),
        )
    }

    private fun saveAndExit(): LessonCommandResult {
        if (state is LessonSessionState.Finished || state is LessonSessionState.FatalContentError) {
            return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "SaveAndExit is not valid from the current terminal state.",
            )
        }

        val snapshotResult = createSnapshot()
        val snapshot = (snapshotResult as? LessonSnapshotResult.Created)?.snapshot
            ?: return reject(
                LessonCommandRejectionCode.SNAPSHOT_UNAVAILABLE,
                "The current session state cannot be safely snapshotted.",
            )

        val finalContext = when (val current = state) {
            is LessonSessionState.Contextual -> current.context
            else -> null
        }
        return transition(
            LessonSessionState.Finished(
                reason = LessonFinishReason.SAVED_FOR_LATER,
                finalContext = finalContext,
            ),
            LessonSessionEvent.SaveAndExitRequested(snapshot),
        )
    }

    private fun transition(
        nextState: LessonSessionState,
        vararg additionalEvents: LessonSessionEvent,
    ): LessonCommandResult.Accepted {
        val previous = state
        state = nextState
        return LessonCommandResult.Accepted(
            state = nextState,
            events = listOf(LessonSessionEvent.StateChanged(previous, nextState)) + additionalEvents,
        )
    }

    private fun reject(
        code: LessonCommandRejectionCode,
        message: String,
    ): LessonCommandResult.Rejected = LessonCommandResult.Rejected(
        state = state,
        rejection = LessonCommandRejection(code, message),
    )

    private fun snapshotForState(
        current: LessonSessionState,
        savedAtEpochMillis: Long,
    ): LessonSnapshotResult {
        if (current is LessonSessionState.FatalContentError) {
            return LessonSnapshotResult.Unavailable(LessonSnapshotUnavailableReason.FATAL_CONTENT_STATE)
        }

        val context = when (current) {
            is LessonSessionState.Contextual -> current.context
            is LessonSessionState.Finished -> current.finalContext
            else -> null
        }

        val persistedPhase: LessonSnapshotPhase
        var pausedResumePhase: LessonSnapshotPhase? = null
        var finishReason: LessonFinishReason? = null

        when (current) {
            LessonSessionState.Ready -> persistedPhase = LessonSnapshotPhase.READY
            is LessonSessionState.OverviewDemonstrating -> persistedPhase = LessonSnapshotPhase.OVERVIEW_DEMONSTRATING
            is LessonSessionState.PreparingStep -> persistedPhase = LessonSnapshotPhase.PREPARING_STEP
            is LessonSessionState.TeacherDemonstrating -> persistedPhase = LessonSnapshotPhase.PREPARING_STEP
            is LessonSessionState.AwaitingChild -> persistedPhase = LessonSnapshotPhase.AWAITING_CHILD
            is LessonSessionState.HelpActive -> persistedPhase = LessonSnapshotPhase.HELP_ACTIVE
            is LessonSessionState.CompletingStep -> persistedPhase = LessonSnapshotPhase.AWAITING_CHILD
            is LessonSessionState.Paused -> {
                persistedPhase = LessonSnapshotPhase.PAUSED
                pausedResumePhase = safeResumePhase(current.previousStableState)
            }
            is LessonSessionState.DrawingComplete -> persistedPhase = LessonSnapshotPhase.DRAWING_COMPLETE
            is LessonSessionState.AwaitingPostDrawingChoice -> persistedPhase = LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE
            is LessonSessionState.HandingOffToColoring -> persistedPhase = LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE
            is LessonSessionState.Finished -> {
                persistedPhase = LessonSnapshotPhase.FINISHED
                finishReason = current.reason
            }
            is LessonSessionState.RecoverableError -> persistedPhase = safeRecoveryPhase(current.recoveryPhase)
            is LessonSessionState.FatalContentError -> error("Handled above")
        }

        return LessonSnapshotResult.Created(
            LessonSessionSnapshot(
                sessionId = identity.sessionId,
                lessonId = identity.lessonId,
                lessonRevision = identity.lessonRevision,
                childDocumentId = identity.childDocumentId,
                mode = context?.mode,
                pace = context?.pace,
                phase = persistedPhase,
                pausedResumePhase = pausedResumePhase,
                currentStepIndex = context?.currentStepIndex,
                currentStepId = context?.currentStepId,
                helpLevel = context?.helpLevel ?: 0,
                overviewCompleted = context?.overviewCompleted ?: false,
                finishReason = finishReason,
                savedAtEpochMillis = savedAtEpochMillis,
            ),
        )
    }

    companion object {
        fun create(
            lessonPackage: LessonRuntimePackage,
            sessionId: String,
            childDocumentId: String,
            clock: () -> Long = System::currentTimeMillis,
        ): LessonSessionEngine {
            require(sessionId.isNotBlank()) { "sessionId cannot be blank." }
            require(childDocumentId.isNotBlank()) { "childDocumentId cannot be blank." }
            require(lessonPackage.lesson.drawing.steps.isNotEmpty()) {
                "LessonSessionEngine requires a validated lesson with at least one drawing step."
            }

            return LessonSessionEngine(
                lessonPackage = lessonPackage,
                identity = LessonSessionIdentity(
                    sessionId = sessionId,
                    lessonId = lessonPackage.lesson.lessonId,
                    lessonRevision = lessonPackage.lesson.revision,
                    childDocumentId = childDocumentId,
                ),
                clock = clock,
                initialState = LessonSessionState.Ready,
            )
        }

        fun restore(
            lessonPackage: LessonRuntimePackage,
            snapshot: LessonSessionSnapshot,
            clock: () -> Long = System::currentTimeMillis,
        ): LessonRestoreResult {
            compatibilityFailure(lessonPackage, snapshot)?.let {
                return LessonRestoreResult.Incompatible(snapshot, it)
            }

            val contextResult = restoreContext(lessonPackage, snapshot)
            if (contextResult is RestoreContextResult.Invalid) {
                return LessonRestoreResult.Incompatible(snapshot, contextResult.incompatibility)
            }
            val context = (contextResult as RestoreContextResult.Valid).context

            val restored = restoreState(snapshot, context)
            if (restored is RestoreStateResult.Invalid) {
                return LessonRestoreResult.Incompatible(snapshot, restored.incompatibility)
            }
            restored as RestoreStateResult.Valid

            val engine = LessonSessionEngine(
                lessonPackage = lessonPackage,
                identity = LessonSessionIdentity(
                    sessionId = snapshot.sessionId,
                    lessonId = snapshot.lessonId,
                    lessonRevision = snapshot.lessonRevision,
                    childDocumentId = snapshot.childDocumentId,
                ),
                clock = clock,
                initialState = restored.state,
            )
            val event = LessonSessionEvent.SessionRestored(restored.normalization)
            return LessonRestoreResult.Restored(
                engine = engine,
                normalization = restored.normalization,
                events = listOf(event),
            )
        }

        private fun compatibilityFailure(
            lessonPackage: LessonRuntimePackage,
            snapshot: LessonSessionSnapshot,
        ): LessonRestoreIncompatibility? {
            if (snapshot.formatVersion != LessonSessionSnapshot.CURRENT_FORMAT_VERSION) {
                return incompatibility(
                    LessonRestoreIncompatibilityCode.UNSUPPORTED_SNAPSHOT_VERSION,
                    "Snapshot format ${snapshot.formatVersion} is not supported.",
                )
            }
            if (snapshot.lessonId != lessonPackage.lesson.lessonId) {
                return incompatibility(
                    LessonRestoreIncompatibilityCode.LESSON_ID_MISMATCH,
                    "Snapshot lesson ${snapshot.lessonId} does not match ${lessonPackage.lesson.lessonId}.",
                )
            }
            if (snapshot.lessonRevision != lessonPackage.lesson.revision) {
                return incompatibility(
                    LessonRestoreIncompatibilityCode.LESSON_REVISION_MISMATCH,
                    "Snapshot revision ${snapshot.lessonRevision} does not match available revision ${lessonPackage.lesson.revision}.",
                )
            }
            if (snapshot.sessionId.isBlank() || snapshot.childDocumentId.isBlank()) {
                return incompatibility(
                    LessonRestoreIncompatibilityCode.MISSING_ACTIVE_CONTEXT,
                    "Snapshot session/document identity is incomplete.",
                )
            }
            if (snapshot.mode != null && snapshot.mode !in lessonPackage.lesson.supportedModes) {
                return incompatibility(
                    LessonRestoreIncompatibilityCode.UNSUPPORTED_MODE,
                    "Snapshot mode ${snapshot.mode} is not supported by this lesson revision.",
                )
            }
            return null
        }

        private fun restoreContext(
            lessonPackage: LessonRuntimePackage,
            snapshot: LessonSessionSnapshot,
        ): RestoreContextResult {
            if (snapshot.phase == LessonSnapshotPhase.READY) {
                return RestoreContextResult.Valid(null)
            }
            if (snapshot.phase == LessonSnapshotPhase.FINISHED && snapshot.mode == null && snapshot.pace == null) {
                return RestoreContextResult.Valid(null)
            }

            val mode = snapshot.mode
                ?: return invalidContext(
                    LessonRestoreIncompatibilityCode.MISSING_ACTIVE_CONTEXT,
                    "Active snapshot is missing teaching mode.",
                )
            val pace = snapshot.pace
                ?: return invalidContext(
                    LessonRestoreIncompatibilityCode.MISSING_ACTIVE_CONTEXT,
                    "Active snapshot is missing teaching pace.",
                )
            val stepIndex = snapshot.currentStepIndex
                ?: return invalidContext(
                    LessonRestoreIncompatibilityCode.MISSING_ACTIVE_CONTEXT,
                    "Active snapshot is missing current step index.",
                )
            val stepId = snapshot.currentStepId
                ?: return invalidContext(
                    LessonRestoreIncompatibilityCode.MISSING_ACTIVE_CONTEXT,
                    "Active snapshot is missing current step ID.",
                )
            val step = lessonPackage.lesson.drawing.steps.getOrNull(stepIndex)
                ?: return invalidContext(
                    LessonRestoreIncompatibilityCode.INVALID_STEP,
                    "Snapshot step index $stepIndex is not present in the lesson.",
                )
            if (step.id != stepId) {
                return invalidContext(
                    LessonRestoreIncompatibilityCode.INVALID_STEP,
                    "Snapshot step ID $stepId does not match authored step ${step.id} at index $stepIndex.",
                )
            }
            if (snapshot.helpLevel !in 0..5) {
                return invalidContext(
                    LessonRestoreIncompatibilityCode.INVALID_HELP_LEVEL,
                    "Snapshot help level ${snapshot.helpLevel} is outside 0..5.",
                )
            }

            return RestoreContextResult.Valid(
                LessonActiveContext(
                    mode = mode,
                    pace = pace,
                    currentStepIndex = stepIndex,
                    currentStepId = stepId,
                    helpLevel = snapshot.helpLevel,
                    overviewCompleted = snapshot.overviewCompleted,
                ),
            )
        }

        private fun restoreState(
            snapshot: LessonSessionSnapshot,
            context: LessonActiveContext?,
        ): RestoreStateResult {
            fun requireContext(): LessonActiveContext = checkNotNull(context)

            return when (snapshot.phase) {
                LessonSnapshotPhase.READY -> RestoreStateResult.Valid(
                    LessonSessionState.Ready,
                    LessonRestoreNormalization.NONE,
                )
                LessonSnapshotPhase.OVERVIEW_DEMONSTRATING -> RestoreStateResult.Valid(
                    LessonSessionState.OverviewDemonstrating(requireContext()),
                    LessonRestoreNormalization.RESTART_OVERVIEW,
                )
                LessonSnapshotPhase.PREPARING_STEP -> RestoreStateResult.Valid(
                    LessonSessionState.PreparingStep(requireContext()),
                    LessonRestoreNormalization.NONE,
                )
                LessonSnapshotPhase.TEACHER_DEMONSTRATING -> RestoreStateResult.Valid(
                    LessonSessionState.PreparingStep(requireContext()),
                    LessonRestoreNormalization.RESTART_CURRENT_TEACHER_DEMONSTRATION,
                )
                LessonSnapshotPhase.AWAITING_CHILD -> RestoreStateResult.Valid(
                    LessonSessionState.AwaitingChild(requireContext()),
                    LessonRestoreNormalization.NONE,
                )
                LessonSnapshotPhase.HELP_ACTIVE -> {
                    val active = requireContext()
                    if (active.helpLevel <= 0) {
                        RestoreStateResult.Invalid(
                            incompatibility(
                                LessonRestoreIncompatibilityCode.INVALID_HELP_LEVEL,
                                "HELP_ACTIVE requires a persisted help level greater than zero.",
                            ),
                        )
                    } else {
                        RestoreStateResult.Valid(
                            LessonSessionState.HelpActive(active),
                            LessonRestoreNormalization.NONE,
                        )
                    }
                }
                LessonSnapshotPhase.COMPLETING_STEP -> RestoreStateResult.Valid(
                    LessonSessionState.AwaitingChild(requireContext()),
                    LessonRestoreNormalization.RETURN_TO_CHILD_TURN,
                )
                LessonSnapshotPhase.PAUSED -> restorePaused(snapshot, requireContext())
                LessonSnapshotPhase.DRAWING_COMPLETE -> RestoreStateResult.Valid(
                    LessonSessionState.DrawingComplete(requireContext()),
                    LessonRestoreNormalization.NONE,
                )
                LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE -> RestoreStateResult.Valid(
                    LessonSessionState.AwaitingPostDrawingChoice(requireContext()),
                    LessonRestoreNormalization.NONE,
                )
                LessonSnapshotPhase.HANDING_OFF_TO_COLORING -> RestoreStateResult.Valid(
                    LessonSessionState.AwaitingPostDrawingChoice(requireContext()),
                    LessonRestoreNormalization.RETRY_POST_DRAWING_CHOICE,
                )
                LessonSnapshotPhase.FINISHED -> RestoreStateResult.Valid(
                    LessonSessionState.Finished(
                        reason = snapshot.finishReason ?: LessonFinishReason.COMPLETED,
                        finalContext = context,
                    ),
                    LessonRestoreNormalization.NONE,
                )
            }
        }

        private fun restorePaused(
            snapshot: LessonSessionSnapshot,
            context: LessonActiveContext,
        ): RestoreStateResult {
            return when (snapshot.pausedResumePhase) {
                LessonSnapshotPhase.OVERVIEW_DEMONSTRATING -> RestoreStateResult.Valid(
                    LessonSessionState.Paused(LessonSessionState.OverviewDemonstrating(context)),
                    LessonRestoreNormalization.RESTART_OVERVIEW,
                )
                LessonSnapshotPhase.PREPARING_STEP -> RestoreStateResult.Valid(
                    LessonSessionState.Paused(LessonSessionState.PreparingStep(context)),
                    LessonRestoreNormalization.NONE,
                )
                LessonSnapshotPhase.TEACHER_DEMONSTRATING -> RestoreStateResult.Valid(
                    LessonSessionState.Paused(LessonSessionState.PreparingStep(context)),
                    LessonRestoreNormalization.RESTART_CURRENT_TEACHER_DEMONSTRATION,
                )
                LessonSnapshotPhase.AWAITING_CHILD -> RestoreStateResult.Valid(
                    LessonSessionState.Paused(LessonSessionState.AwaitingChild(context)),
                    LessonRestoreNormalization.NONE,
                )
                LessonSnapshotPhase.HELP_ACTIVE -> {
                    if (context.helpLevel <= 0) {
                        RestoreStateResult.Invalid(
                            incompatibility(
                                LessonRestoreIncompatibilityCode.INVALID_HELP_LEVEL,
                                "Paused HELP_ACTIVE requires help level greater than zero.",
                            ),
                        )
                    } else {
                        RestoreStateResult.Valid(
                            LessonSessionState.Paused(LessonSessionState.HelpActive(context)),
                            LessonRestoreNormalization.NONE,
                        )
                    }
                }
                else -> RestoreStateResult.Invalid(
                    incompatibility(
                        LessonRestoreIncompatibilityCode.INVALID_PAUSED_PHASE,
                        "Paused snapshot does not contain a valid pausable resume phase.",
                    ),
                )
            }
        }

        private fun incompatibility(
            code: LessonRestoreIncompatibilityCode,
            message: String,
        ) = LessonRestoreIncompatibility(code, message)

        private fun invalidContext(
            code: LessonRestoreIncompatibilityCode,
            message: String,
        ) = RestoreContextResult.Invalid(incompatibility(code, message))
    }
}

private sealed interface RestoreContextResult {
    data class Valid(val context: LessonActiveContext?) : RestoreContextResult
    data class Invalid(val incompatibility: LessonRestoreIncompatibility) : RestoreContextResult
}

private sealed interface RestoreStateResult {
    data class Valid(
        val state: LessonSessionState,
        val normalization: LessonRestoreNormalization,
    ) : RestoreStateResult

    data class Invalid(val incompatibility: LessonRestoreIncompatibility) : RestoreStateResult
}

private fun runtimePhaseOf(state: LessonSessionState.Pausable): LessonSnapshotPhase = when (state) {
    is LessonSessionState.OverviewDemonstrating -> LessonSnapshotPhase.OVERVIEW_DEMONSTRATING
    is LessonSessionState.PreparingStep -> LessonSnapshotPhase.PREPARING_STEP
    is LessonSessionState.TeacherDemonstrating -> LessonSnapshotPhase.TEACHER_DEMONSTRATING
    is LessonSessionState.AwaitingChild -> LessonSnapshotPhase.AWAITING_CHILD
    is LessonSessionState.HelpActive -> LessonSnapshotPhase.HELP_ACTIVE
}

private fun safeResumePhase(state: LessonSessionState.Pausable): LessonSnapshotPhase = when (state) {
    is LessonSessionState.OverviewDemonstrating -> LessonSnapshotPhase.OVERVIEW_DEMONSTRATING
    is LessonSessionState.PreparingStep -> LessonSnapshotPhase.PREPARING_STEP
    is LessonSessionState.TeacherDemonstrating -> LessonSnapshotPhase.PREPARING_STEP
    is LessonSessionState.AwaitingChild -> LessonSnapshotPhase.AWAITING_CHILD
    is LessonSessionState.HelpActive -> LessonSnapshotPhase.HELP_ACTIVE
}

private fun safeRecoveryPhase(phase: LessonSnapshotPhase): LessonSnapshotPhase = when (phase) {
    LessonSnapshotPhase.TEACHER_DEMONSTRATING -> LessonSnapshotPhase.PREPARING_STEP
    LessonSnapshotPhase.COMPLETING_STEP -> LessonSnapshotPhase.AWAITING_CHILD
    LessonSnapshotPhase.HANDING_OFF_TO_COLORING -> LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE
    LessonSnapshotPhase.PAUSED -> LessonSnapshotPhase.AWAITING_CHILD
    else -> phase
}

private fun replaceContext(
    state: LessonSessionState.Contextual,
    context: LessonActiveContext,
): LessonSessionState = when (state) {
    is LessonSessionState.OverviewDemonstrating -> state.copy(context = context)
    is LessonSessionState.PreparingStep -> state.copy(context = context)
    is LessonSessionState.TeacherDemonstrating -> state.copy(context = context)
    is LessonSessionState.AwaitingChild -> state.copy(context = context)
    is LessonSessionState.HelpActive -> state.copy(context = context)
    is LessonSessionState.CompletingStep -> state.copy(context = context)
    is LessonSessionState.Paused -> LessonSessionState.Paused(
        replacePausableContext(state.previousStableState, context),
    )
    is LessonSessionState.DrawingComplete -> state.copy(context = context)
    is LessonSessionState.AwaitingPostDrawingChoice -> state.copy(context = context)
    is LessonSessionState.HandingOffToColoring -> state.copy(context = context)
    is LessonSessionState.RecoverableError -> state.copy(context = context)
}

private fun replacePausableContext(
    state: LessonSessionState.Pausable,
    context: LessonActiveContext,
): LessonSessionState.Pausable = when (state) {
    is LessonSessionState.OverviewDemonstrating -> state.copy(context = context)
    is LessonSessionState.PreparingStep -> state.copy(context = context)
    is LessonSessionState.TeacherDemonstrating -> state.copy(context = context)
    is LessonSessionState.AwaitingChild -> state.copy(context = context)
    is LessonSessionState.HelpActive -> state.copy(context = context)
}
