package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.execution.LessonTeacherSequenceFactory
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode

class LessonSessionEngine private constructor(
    val lessonPackage: LessonRuntimePackage,
    val identity: LessonSessionIdentity,
    private val clock: () -> Long,
    initialState: LessonSessionState,
) {
    private var teacherRequestOrdinal: Long = 0L

    var state: LessonSessionState = initialState
        private set

    fun dispatch(command: LessonCommand): LessonCommandResult = when (command) {
        is LessonCommand.StartLesson -> startLesson(command.mode, command.pace)
        LessonCommand.Pause -> pause()
        LessonCommand.Resume -> resume()
        LessonCommand.SaveAndExit -> saveAndExit()
        is LessonCommand.SetPace -> setPace(command.pace)
        ReplayDemonstration -> replayDemonstration()
        MarkChildTurnDone -> markChildTurnDone()
        SkipStep -> skipStep()
    }

    fun handle(signal: LessonRuntimeSignal): LessonSignalResult = when (signal) {
        is LessonRuntimeSignal.TeacherPlaybackCompleted -> teacherPlaybackCompleted(signal)
        is LessonRuntimeSignal.TeacherPlaybackFailed -> teacherPlaybackFailed(signal)
        is LessonRuntimeSignal.ChildStrokeCommitted -> childStrokeCommitted(signal)
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

        val events = mutableListOf<LessonSessionEvent>()
        when (mode) {
            TeachingMode.DRAW_WITH_ME -> {
                events += moveTo(LessonSessionState.PreparingStep(context))
                events += LessonSessionEvent.SessionStarted(mode, pace)
                launchTeacherDemonstration(context, replay = false, events = events)
            }

            TeachingMode.WATCH_THEN_DRAW -> {
                events += moveTo(LessonSessionState.OverviewDemonstrating(context))
                events += LessonSessionEvent.SessionStarted(mode, pace)
            }

            TeachingMode.TRACE_AND_LEARN -> {
                // Full trace-guide orchestration belongs to P2.4. P2.2 semantics remain intact.
                events += moveTo(LessonSessionState.PreparingStep(context))
                events += LessonSessionEvent.SessionStarted(mode, pace)
            }
        }
        return accepted(events)
    }

    private fun pause(): LessonCommandResult {
        val current = state
        if (current !is LessonSessionState.Pausable) {
            return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "Pause is not valid from ${current::class.simpleName}.",
            )
        }

        val events = mutableListOf<LessonSessionEvent>()
        events += moveTo(LessonSessionState.Paused(current))
        events += LessonSessionEvent.SessionPaused(runtimePhaseOf(current))
        if (current is LessonSessionState.TeacherDemonstrating) {
            events += TeacherPlaybackPauseRequested(current.requestId)
        }
        return accepted(events)
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
        val events = mutableListOf<LessonSessionEvent>()
        events += moveTo(resumed)
        events += LessonSessionEvent.SessionResumed(runtimePhaseOf(resumed))
        if (resumed is LessonSessionState.TeacherDemonstrating) {
            events += TeacherPlaybackResumeRequested(resumed.requestId)
        }
        return accepted(events)
    }

    private fun setPace(pace: TeachingPace): LessonCommandResult {
        val contextual = state as? LessonSessionState.Contextual
            ?: return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "SetPace requires an active or paused lesson session.",
            )

        val previousPace = contextual.context.pace
        if (previousPace == pace) {
            return LessonCommandResult.Accepted(state, emptyList())
        }

        val activeTeacherRequestId = activeTeacherRequestId(contextual)
        val updatedContext = contextual.context.copy(pace = pace)
        val nextState = replaceContext(contextual, updatedContext)
        val events = mutableListOf<LessonSessionEvent>()
        events += moveTo(nextState)
        events += LessonSessionEvent.PaceChanged(previousPace, pace)
        if (activeTeacherRequestId != null) {
            events += TeacherPlaybackPaceChangeRequested(activeTeacherRequestId, pace)
        }
        return accepted(events)
    }

    private fun replayDemonstration(): LessonCommandResult {
        val context = childTurnContext()
            ?: return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "Replay is only valid during a child turn.",
            )
        val step = currentStep(context)
        if (!step.childTurn.allowReplay) {
            return reject(
                LessonCommandRejectionCode.REPLAY_NOT_ALLOWED,
                "Step ${step.id} does not allow teacher replay.",
            )
        }

        val events = mutableListOf<LessonSessionEvent>()
        launchTeacherDemonstration(context, replay = true, events = events)
        return accepted(events)
    }

    private fun markChildTurnDone(): LessonCommandResult {
        val context = childTurnContext()
            ?: return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "Done is only valid during a child turn.",
            )
        return when (val policy = currentStep(context).childTurn.completionPolicy) {
            ChildCompletionPolicy.MANUAL_DONE -> accepted(completeCurrentStep(context, skipped = false))
            ChildCompletionPolicy.ANY_STROKE -> reject(
                LessonCommandRejectionCode.COMPLETION_POLICY_NOT_SATISFIED,
                "This step completes after a committed child stroke rather than manual Done.",
            )
            ChildCompletionPolicy.AUTHORED_SIGNAL -> reject(
                LessonCommandRejectionCode.UNSUPPORTED_COMPLETION_POLICY,
                "Authored completion signals are not enabled in Lesson Engine 0.2.",
            )
        }
    }

    private fun skipStep(): LessonCommandResult {
        val context = childTurnContext()
            ?: return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "Skip is only valid during a child turn.",
            )
        val step = currentStep(context)
        if (!step.childTurn.allowSkip) {
            return reject(
                LessonCommandRejectionCode.SKIP_NOT_ALLOWED,
                "Step ${step.id} does not allow Skip.",
            )
        }
        return accepted(completeCurrentStep(context, skipped = true))
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

        val currentState = state
        val finalContext = (currentState as? LessonSessionState.Contextual)?.context
        val events = mutableListOf<LessonSessionEvent>()
        activeTeacherRequestId(currentState)?.let { events += TeacherPlaybackCancelRequested(it) }
        events += moveTo(
            LessonSessionState.Finished(
                reason = LessonFinishReason.SAVED_FOR_LATER,
                finalContext = finalContext,
            ),
        )
        events += LessonSessionEvent.SaveAndExitRequested(snapshot)
        return accepted(events)
    }

    private fun teacherPlaybackCompleted(
        signal: LessonRuntimeSignal.TeacherPlaybackCompleted,
    ): LessonSignalResult {
        val current = state
        if (current !is LessonSessionState.TeacherDemonstrating) {
            return rejectSignal(
                LessonSignalRejectionCode.INVALID_STATE,
                "Teacher completion is only valid while a teacher demonstration is active.",
            )
        }
        if (signal.requestId != current.requestId) {
            return rejectSignal(
                LessonSignalRejectionCode.STALE_TEACHER_REQUEST,
                "Teacher completion ${signal.requestId} does not match active request ${current.requestId}.",
            )
        }

        val nextState: LessonSessionState.Pausable = if (current.context.helpLevel > 0) {
            LessonSessionState.HelpActive(current.context)
        } else {
            LessonSessionState.AwaitingChild(current.context)
        }
        val events = listOf(
            moveTo(nextState),
            ChildTurnStarted(
                stepIndex = current.context.currentStepIndex,
                stepId = current.context.currentStepId,
            ),
        )
        return acceptedSignal(events)
    }

    private fun teacherPlaybackFailed(
        signal: LessonRuntimeSignal.TeacherPlaybackFailed,
    ): LessonSignalResult {
        val current = state
        if (current !is LessonSessionState.TeacherDemonstrating) {
            return rejectSignal(
                LessonSignalRejectionCode.INVALID_STATE,
                "Teacher failure is only valid while a teacher demonstration is active.",
            )
        }
        if (signal.requestId != current.requestId) {
            return rejectSignal(
                LessonSignalRejectionCode.STALE_TEACHER_REQUEST,
                "Teacher failure ${signal.requestId} does not match active request ${current.requestId}.",
            )
        }

        val reason = signal.reason.ifBlank { "unknown" }
        val next = LessonSessionState.RecoverableError(
            context = current.context,
            recoveryPhase = LessonSnapshotPhase.PREPARING_STEP,
            code = "teacher_playback_failed",
        )
        val events = listOf(
            moveTo(next),
            TeacherPlaybackFailureObserved(current.requestId, reason),
        )
        return acceptedSignal(events)
    }

    private fun childStrokeCommitted(
        signal: LessonRuntimeSignal.ChildStrokeCommitted,
    ): LessonSignalResult {
        val context = childTurnContext()
            ?: return rejectSignal(
                LessonSignalRejectionCode.INVALID_STATE,
                "A child stroke can affect lesson progression only during a child turn.",
            )
        if (signal.childDocumentId != identity.childDocumentId) {
            return rejectSignal(
                LessonSignalRejectionCode.DOCUMENT_MISMATCH,
                "Committed child stroke belongs to a different drawing document.",
            )
        }
        if (signal.operationId.isBlank()) {
            return rejectSignal(
                LessonSignalRejectionCode.COMPLETION_POLICY_NOT_APPLICABLE,
                "Committed child operation ID cannot be blank.",
            )
        }

        return when (currentStep(context).childTurn.completionPolicy) {
            ChildCompletionPolicy.ANY_STROKE -> acceptedSignal(
                completeCurrentStep(context, skipped = false),
            )
            ChildCompletionPolicy.MANUAL_DONE -> LessonSignalResult.Accepted(state, emptyList())
            ChildCompletionPolicy.AUTHORED_SIGNAL -> rejectSignal(
                LessonSignalRejectionCode.COMPLETION_POLICY_NOT_APPLICABLE,
                "A committed child stroke does not satisfy authored_signal completion.",
            )
        }
    }

    private fun launchTeacherDemonstration(
        context: LessonActiveContext,
        replay: Boolean,
        events: MutableList<LessonSessionEvent>,
    ) {
        val step = currentStep(context)
        val sequence = LessonTeacherSequenceFactory.create(lessonPackage, step)
        val requestId = nextTeacherRequestId(step)
        val request = TeacherPlaybackRequest(
            requestId = requestId,
            stepId = step.id,
            sequence = sequence,
            pace = context.pace,
            replay = replay,
            narrationKey = step.teacher.narrationKey,
        )
        events += moveTo(
            LessonSessionState.TeacherDemonstrating(
                context = context,
                requestId = requestId,
                replay = replay,
            ),
        )
        events += TeacherPlaybackRequested(request)
    }

    private fun completeCurrentStep(
        context: LessonActiveContext,
        skipped: Boolean,
    ): List<LessonSessionEvent> {
        val step = currentStep(context)
        val events = mutableListOf<LessonSessionEvent>()
        events += moveTo(LessonSessionState.CompletingStep(context))
        events += StepCompleted(
            stepIndex = context.currentStepIndex,
            stepId = step.id,
            skipped = skipped,
        )
        events += LessonAutosaveRequested(LessonAutosaveReason.STEP_COMPLETED)

        val nextIndex = context.currentStepIndex + 1
        if (nextIndex >= lessonPackage.lesson.drawing.steps.size) {
            events += moveTo(LessonSessionState.DrawingComplete(context))
            events += DrawingLessonCompleted(lessonPackage.lesson.lessonId)
            events += LessonAutosaveRequested(LessonAutosaveReason.DRAWING_COMPLETED)
            return events
        }

        val nextStep = lessonPackage.lesson.drawing.steps[nextIndex]
        val nextContext = context.copy(
            currentStepIndex = nextIndex,
            currentStepId = nextStep.id,
            helpLevel = 0,
        )
        events += moveTo(LessonSessionState.PreparingStep(nextContext))
        if (nextContext.mode == TeachingMode.DRAW_WITH_ME) {
            launchTeacherDemonstration(nextContext, replay = false, events = events)
        }
        return events
    }

    private fun currentStep(context: LessonActiveContext): DrawingStep {
        val step = lessonPackage.lesson.drawing.steps.getOrNull(context.currentStepIndex)
            ?: error("Session step index ${context.currentStepIndex} is outside the validated lesson.")
        check(step.id == context.currentStepId) {
            "Session step identity ${context.currentStepId} does not match authored step ${step.id}."
        }
        return step
    }

    private fun childTurnContext(): LessonActiveContext? = when (val current = state) {
        is LessonSessionState.AwaitingChild -> current.context
        is LessonSessionState.HelpActive -> current.context
        else -> null
    }

    private fun nextTeacherRequestId(step: DrawingStep): String {
        teacherRequestOrdinal += 1
        return "${identity.sessionId}:r${identity.lessonRevision}:${step.id}:teacher:$teacherRequestOrdinal"
    }

    private fun activeTeacherRequestId(current: LessonSessionState): String? = when (current) {
        is LessonSessionState.TeacherDemonstrating -> current.requestId
        is LessonSessionState.Paused ->
            (current.previousStableState as? LessonSessionState.TeacherDemonstrating)?.requestId
        else -> null
    }

    private fun moveTo(nextState: LessonSessionState): LessonSessionEvent.StateChanged {
        val previous = state
        state = nextState
        return LessonSessionEvent.StateChanged(previous, nextState)
    }

    private fun accepted(events: List<LessonSessionEvent>) = LessonCommandResult.Accepted(
        state = state,
        events = events,
    )

    private fun acceptedSignal(events: List<LessonSessionEvent>) = LessonSignalResult.Accepted(
        state = state,
        events = events,
    )

    private fun reject(
        code: LessonCommandRejectionCode,
        message: String,
    ): LessonCommandResult.Rejected = LessonCommandResult.Rejected(
        state = state,
        rejection = LessonCommandRejection(code, message),
    )

    private fun rejectSignal(
        code: LessonSignalRejectionCode,
        message: String,
    ): LessonSignalResult.Rejected = LessonSignalResult.Rejected(
        state = state,
        rejection = LessonSignalRejection(code, message),
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
