from pathlib import Path

path = Path('app/src/main/java/com/navin/kidsdrawing/lesson/session/LessonSessionEngine.kt')
text = path.read_text()


def replace_once(old: str, new: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'Expected exactly one match, found {count}: {old[:100]!r}')
    text = text.replace(old, new, 1)

replace_once(
'''import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.execution.LessonTeacherSequenceFactory
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
''',
'''import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.assistance.GuideOverlayPurpose
import com.navin.kidsdrawing.lesson.assistance.GuideOverlayRequest
import com.navin.kidsdrawing.lesson.assistance.LessonGuideOverlayFactory
import com.navin.kidsdrawing.lesson.execution.LessonOverviewSequenceFactory
import com.navin.kidsdrawing.lesson.execution.LessonTeacherSequenceFactory
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
''')

replace_once(
'''    private var teacherRequestOrdinal: Long = 0L

    var state: LessonSessionState = initialState
''',
'''    private var teacherRequestOrdinal: Long = 0L
    private var activeOverviewRequestId: String? = null

    var state: LessonSessionState = initialState
''')

replace_once(
'''        ReplayDemonstration -> replayDemonstration()
        MarkChildTurnDone -> markChildTurnDone()
        SkipStep -> skipStep()
''',
'''        ReplayDemonstration -> replayDemonstration()
        MarkChildTurnDone -> markChildTurnDone()
        SkipStep -> skipStep()
        SkipOverview -> skipOverview()
        RequestHelp -> requestHelp()
        ReduceHelp -> reduceHelp()
        DismissHelp -> dismissHelp()
''')

replace_once(
'''            TeachingMode.WATCH_THEN_DRAW -> {
                events += moveTo(LessonSessionState.OverviewDemonstrating(context))
                events += LessonSessionEvent.SessionStarted(mode, pace)
            }

            TeachingMode.TRACE_AND_LEARN -> {
                // Full trace-guide orchestration belongs to P2.4. P2.2 semantics remain intact.
                events += moveTo(LessonSessionState.PreparingStep(context))
                events += LessonSessionEvent.SessionStarted(mode, pace)
            }
''',
'''            TeachingMode.WATCH_THEN_DRAW -> {
                events += moveTo(LessonSessionState.OverviewDemonstrating(context))
                events += LessonSessionEvent.SessionStarted(mode, pace)
                launchOverview(context, events)
            }

            TeachingMode.TRACE_AND_LEARN -> {
                events += moveTo(LessonSessionState.PreparingStep(context))
                events += LessonSessionEvent.SessionStarted(mode, pace)
                launchTeacherDemonstration(context, replay = false, events = events)
            }
''')

replace_once(
'''        if (current is LessonSessionState.TeacherDemonstrating) {
            events += TeacherPlaybackPauseRequested(current.requestId)
        }
''',
'''        activeTeacherRequestId(current)?.let { requestId ->
            events += TeacherPlaybackPauseRequested(requestId)
        }
''')

replace_once(
'''        if (resumed is LessonSessionState.TeacherDemonstrating) {
            events += TeacherPlaybackResumeRequested(resumed.requestId)
        }
''',
'''        activeTeacherRequestId(resumed)?.let { requestId ->
            events += TeacherPlaybackResumeRequested(requestId)
        }
''')

replace_once(
'''        val events = mutableListOf<LessonSessionEvent>()
        launchTeacherDemonstration(context, replay = true, events = events)
        return accepted(events)
''',
'''        val events = mutableListOf<LessonSessionEvent>()
        clearGuideIfNeeded(context, events)
        launchTeacherDemonstration(context, replay = true, events = events)
        return accepted(events)
''')

replace_once(
'''    private fun saveAndExit(): LessonCommandResult {
''',
'''    private fun skipOverview(): LessonCommandResult {
        val current = state as? LessonSessionState.OverviewDemonstrating
            ?: return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "SkipOverview is only valid during Watch Then Draw overview.",
            )
        if (current.context.mode != TeachingMode.WATCH_THEN_DRAW) {
            return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "Only Watch Then Draw sessions have an overview.",
            )
        }

        val events = mutableListOf<LessonSessionEvent>()
        activeOverviewRequestId?.let { events += TeacherPlaybackCancelRequested(it) }
        activeOverviewRequestId = null
        val childContext = current.context.copy(overviewCompleted = true)
        events += moveTo(LessonSessionState.AwaitingChild(childContext))
        events += OverviewSkipped
        events += ChildTurnStarted(childContext.currentStepIndex, childContext.currentStepId)
        return accepted(events)
    }

    private fun requestHelp(): LessonCommandResult {
        val context = childTurnContext()
            ?: return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "Help is only available during a child turn.",
            )
        val step = currentStep(context)
        val next = step.help
            .filter { it.level > context.helpLevel }
            .minByOrNull { it.level }
            ?: return reject(
                LessonCommandRejectionCode.HELP_NOT_AVAILABLE,
                "No higher authored help level is available for step ${step.id}.",
            )

        val events = mutableListOf<LessonSessionEvent>()
        clearGuideIfNeeded(context, events)
        val updated = context.copy(helpLevel = next.level)
        events += moveTo(LessonSessionState.HelpActive(updated))
        events += HelpLevelChanged(
            stepId = step.id,
            previousLevel = context.helpLevel,
            currentLevel = next.level,
            kind = next.kind,
            narrationKey = next.narrationKey,
        )
        emitGuideForChildTurn(updated, events)
        return accepted(events)
    }

    private fun reduceHelp(): LessonCommandResult {
        val context = childTurnContext()
            ?: return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "ReduceHelp is only valid during a child turn.",
            )
        if (context.helpLevel <= 0) {
            return reject(
                LessonCommandRejectionCode.HELP_NOT_AVAILABLE,
                "The child is already at independent help level 0.",
            )
        }

        val step = currentStep(context)
        val previous = step.help
            .filter { it.level < context.helpLevel }
            .maxByOrNull { it.level }
        val nextLevel = previous?.level ?: 0
        val events = mutableListOf<LessonSessionEvent>()
        clearGuideIfNeeded(context, events)
        val updated = context.copy(helpLevel = nextLevel)
        events += moveTo(
            if (nextLevel > 0) LessonSessionState.HelpActive(updated)
            else LessonSessionState.AwaitingChild(updated),
        )
        events += HelpLevelChanged(
            stepId = step.id,
            previousLevel = context.helpLevel,
            currentLevel = nextLevel,
            kind = previous?.kind,
            narrationKey = previous?.narrationKey,
        )
        emitGuideForChildTurn(updated, events)
        return accepted(events)
    }

    private fun dismissHelp(): LessonCommandResult {
        val context = childTurnContext()
            ?: return reject(
                LessonCommandRejectionCode.INVALID_STATE,
                "DismissHelp is only valid during a child turn.",
            )
        if (context.helpLevel <= 0) {
            return reject(
                LessonCommandRejectionCode.HELP_NOT_AVAILABLE,
                "No active Help Ladder level is displayed.",
            )

        val events = mutableListOf<LessonSessionEvent>()
        clearGuideIfNeeded(context, events)
        val updated = context.copy(helpLevel = 0)
        events += moveTo(LessonSessionState.AwaitingChild(updated))
        events += HelpLevelChanged(
            stepId = context.currentStepId,
            previousLevel = context.helpLevel,
            currentLevel = 0,
            kind = null,
            narrationKey = null,
        )
        emitGuideForChildTurn(updated, events)
        return accepted(events)
    }

    private fun saveAndExit(): LessonCommandResult {
''')

replace_once(
'''        activeTeacherRequestId(currentState)?.let { events += TeacherPlaybackCancelRequested(it) }
        events += moveTo(
''',
'''        activeTeacherRequestId(currentState)?.let { events += TeacherPlaybackCancelRequested(it) }
        activeOverviewRequestId = null
        finalContext?.let { clearGuideIfNeeded(it, events) }
        events += moveTo(
''')

start = text.index('    private fun teacherPlaybackCompleted(')
end = text.index('    private fun childStrokeCommitted(', start)
text = text[:start] + '''    private fun teacherPlaybackCompleted(
        signal: LessonRuntimeSignal.TeacherPlaybackCompleted,
    ): LessonSignalResult {
        val current = state
        if (current is LessonSessionState.OverviewDemonstrating) {
            val requestId = activeOverviewRequestId
                ?: return rejectSignal(
                    LessonSignalRejectionCode.STALE_TEACHER_REQUEST,
                    "No active overview playback request is registered.",
                )
            if (signal.requestId != requestId) {
                return rejectSignal(
                    LessonSignalRejectionCode.STALE_TEACHER_REQUEST,
                    "Overview completion ${signal.requestId} does not match active request $requestId.",
                )
            }
            activeOverviewRequestId = null
            val context = current.context.copy(overviewCompleted = true)
            val events = mutableListOf<LessonSessionEvent>()
            events += moveTo(LessonSessionState.AwaitingChild(context))
            events += OverviewCompleted
            events += ChildTurnStarted(context.currentStepIndex, context.currentStepId)
            return acceptedSignal(events)
        }

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

        val events = mutableListOf<LessonSessionEvent>()
        enterChildTurn(current.context, events)
        return acceptedSignal(events)
    }

    private fun teacherPlaybackFailed(
        signal: LessonRuntimeSignal.TeacherPlaybackFailed,
    ): LessonSignalResult {
        val current = state
        if (current is LessonSessionState.OverviewDemonstrating) {
            val requestId = activeOverviewRequestId
                ?: return rejectSignal(
                    LessonSignalRejectionCode.STALE_TEACHER_REQUEST,
                    "No active overview playback request is registered.",
                )
            if (signal.requestId != requestId) {
                return rejectSignal(
                    LessonSignalRejectionCode.STALE_TEACHER_REQUEST,
                    "Overview failure ${signal.requestId} does not match active request $requestId.",
                )
            }
            activeOverviewRequestId = null
            val reason = signal.reason.ifBlank { "unknown" }
            val next = LessonSessionState.RecoverableError(
                context = current.context,
                recoveryPhase = LessonSnapshotPhase.OVERVIEW_DEMONSTRATING,
                code = "overview_playback_failed",
            )
            return acceptedSignal(
                listOf(
                    moveTo(next),
                    TeacherPlaybackFailureObserved(requestId, reason),
                ),
            )
        }

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

''' + text[end:]

replace_once(
'''    private fun launchTeacherDemonstration(
''',
'''    private fun launchOverview(
        context: LessonActiveContext,
        events: MutableList<LessonSessionEvent>,
    ) {
        val sequence = LessonOverviewSequenceFactory.create(lessonPackage)
        val requestId = nextOverviewRequestId()
        activeOverviewRequestId = requestId
        events += TeacherPlaybackRequested(
            TeacherPlaybackRequest(
                requestId = requestId,
                stepId = OVERVIEW_STEP_ID,
                sequence = sequence,
                pace = context.pace,
                replay = false,
                narrationKey = null,
                scope = TeacherPlaybackScope.OVERVIEW,
            ),
        )
        events += OverviewStarted(requestId)
    }

    private fun enterChildTurn(
        context: LessonActiveContext,
        events: MutableList<LessonSessionEvent>,
    ) {
        val nextState: LessonSessionState.Pausable = if (context.helpLevel > 0) {
            LessonSessionState.HelpActive(context)
        } else {
            LessonSessionState.AwaitingChild(context)
        }
        events += moveTo(nextState)
        events += ChildTurnStarted(context.currentStepIndex, context.currentStepId)
        emitGuideForChildTurn(context, events)
    }

    private fun guideOverlayFor(context: LessonActiveContext): GuideOverlayRequest? {
        val step = currentStep(context)
        val authoredHelp = step.help.firstOrNull {
            it.level == context.helpLevel && it.guideRefs.isNotEmpty()
        }
        if (authoredHelp != null) {
            return LessonGuideOverlayFactory.create(
                lessonPackage = lessonPackage,
                step = step,
                guideRefs = authoredHelp.guideRefs,
                purpose = GuideOverlayPurpose.HELP,
                helpLevel = authoredHelp.level,
                helpKind = authoredHelp.kind,
            )
        }
        if (context.mode == TeachingMode.TRACE_AND_LEARN) {
            return LessonGuideOverlayFactory.traceForStep(lessonPackage, step)
        }
        return null
    }

    private fun emitGuideForChildTurn(
        context: LessonActiveContext,
        events: MutableList<LessonSessionEvent>,
    ) {
        guideOverlayFor(context)?.let { events += GuideOverlayRequested(it) }
    }

    private fun clearGuideIfNeeded(
        context: LessonActiveContext,
        events: MutableList<LessonSessionEvent>,
    ) {
        if (guideOverlayFor(context) != null) {
            events += GuideOverlayCleared(context.currentStepId)
        }
    }

    private fun launchTeacherDemonstration(
''')

replace_once(
'''        val step = currentStep(context)
        val events = mutableListOf<LessonSessionEvent>()
        events += moveTo(LessonSessionState.CompletingStep(context))
''',
'''        val step = currentStep(context)
        val events = mutableListOf<LessonSessionEvent>()
        clearGuideIfNeeded(context, events)
        events += moveTo(LessonSessionState.CompletingStep(context))
''')

replace_once(
'''        if (nextIndex >= lessonPackage.lesson.drawing.steps.size) {
            events += moveTo(LessonSessionState.DrawingComplete(context))
''',
'''        if (nextIndex >= lessonPackage.lesson.drawing.steps.size) {
            events += moveTo(LessonSessionState.DrawingComplete(context.copy(helpLevel = 0)))
''')

replace_once(
'''        events += moveTo(LessonSessionState.PreparingStep(nextContext))
        if (nextContext.mode == TeachingMode.DRAW_WITH_ME) {
            launchTeacherDemonstration(nextContext, replay = false, events = events)
        }
        return events
''',
'''        when (nextContext.mode) {
            TeachingMode.DRAW_WITH_ME,
            TeachingMode.TRACE_AND_LEARN,
            -> {
                events += moveTo(LessonSessionState.PreparingStep(nextContext))
                launchTeacherDemonstration(nextContext, replay = false, events = events)
            }
            TeachingMode.WATCH_THEN_DRAW -> {
                events += moveTo(LessonSessionState.AwaitingChild(nextContext))
                events += ChildTurnStarted(nextContext.currentStepIndex, nextContext.currentStepId)
            }
        }
        return events
''')

replace_once(
'''    private fun nextTeacherRequestId(step: DrawingStep): String {
        teacherRequestOrdinal += 1
        return "${identity.sessionId}:r${identity.lessonRevision}:${step.id}:teacher:$teacherRequestOrdinal"
    }

    private fun activeTeacherRequestId(current: LessonSessionState): String? = when (current) {
        is LessonSessionState.TeacherDemonstrating -> current.requestId
        is LessonSessionState.Paused ->
            (current.previousStableState as? LessonSessionState.TeacherDemonstrating)?.requestId
        else -> null
    }
''',
'''    private fun nextTeacherRequestId(step: DrawingStep): String {
        teacherRequestOrdinal += 1
        return "${identity.sessionId}:r${identity.lessonRevision}:${step.id}:teacher:$teacherRequestOrdinal"
    }

    private fun nextOverviewRequestId(): String {
        teacherRequestOrdinal += 1
        return "${identity.sessionId}:r${identity.lessonRevision}:overview:teacher:$teacherRequestOrdinal"
    }

    private fun activeTeacherRequestId(current: LessonSessionState): String? = when (current) {
        is LessonSessionState.TeacherDemonstrating -> current.requestId
        is LessonSessionState.OverviewDemonstrating -> activeOverviewRequestId
        is LessonSessionState.Paused -> when (current.previousStableState) {
            is LessonSessionState.TeacherDemonstrating ->
                current.previousStableState.requestId
            is LessonSessionState.OverviewDemonstrating -> activeOverviewRequestId
            else -> null
        }
        else -> null
    }
''')

replace_once(
'''private fun runtimePhaseOf(state: LessonSessionState.Pausable): LessonSnapshotPhase = when (state) {
''',
'''private const val OVERVIEW_STEP_ID = "__overview__"

private fun runtimePhaseOf(state: LessonSessionState.Pausable): LessonSnapshotPhase = when (state) {
''')

path.write_text(text)
print('patched', path)
