package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.DismissHelp
import com.navin.kidsdrawing.lesson.session.GuideOverlayRequested
import com.navin.kidsdrawing.lesson.session.LessonCommand
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonRuntimeSignal
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.LessonSignalResult
import com.navin.kidsdrawing.lesson.session.MarkChildTurnDone
import com.navin.kidsdrawing.lesson.session.ReplayDemonstration
import com.navin.kidsdrawing.lesson.session.RequestHelp
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackRequested
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackScope
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Whole-catalog executable proof for Content Library V2.
 *
 * A mode is not considered healthy merely because it appears in supportedModes. This test drives
 * the real LessonSessionEngine for every released lesson + advertised mode through teacher playback,
 * authored Help/Replay where available, every child turn, and final drawing completion.
 */
class ProductionLessonModeExecutionGateTest {
    @Test
    fun everyAdvertisedModeExecutesThroughDrawingCompletion() {
        val snapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
        assertTrue("Catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())

        val failures = mutableListOf<String>()
        var executedModeCount = 0

        snapshot.entries.forEach { entry ->
            val packageData = checkNotNull(snapshot.runtimePackage(entry.identity))
            packageData.lesson.supportedModes.forEach { mode ->
                executedModeCount += 1
                runCatching {
                    executeMode(packageData, mode)
                }.exceptionOrNull()?.let { failure ->
                    failures += "${entry.identity.lessonId} r${entry.identity.revision} · $mode · ${failure.message ?: failure::class.simpleName}"
                }
            }
        }

        assertTrue("Expected at least one advertised mode to execute.", executedModeCount > 0)
        assertTrue(
            buildString {
                appendLine("Advertised lesson modes that failed real LessonSessionEngine execution:")
                failures.forEach(::appendLine)
            },
            failures.isEmpty(),
        )
    }

    private fun executeMode(
        packageData: com.navin.kidsdrawing.lesson.model.LessonRuntimePackage,
        mode: TeachingMode,
    ) {
        val lesson = packageData.lesson
        val childDocumentId = "content-v2-${lesson.lessonId}-${mode.name.lowercase()}-doc"
        val engine = LessonSessionEngine.create(
            lessonPackage = packageData,
            sessionId = "content-v2-${lesson.lessonId}-${mode.name.lowercase()}-session",
            childDocumentId = childDocumentId,
            clock = { 1_000L },
        )

        val start = requireAccepted(
            engine.dispatch(LessonCommand.StartLesson(mode, TeachingPace.NORMAL)),
            "start $mode",
        )

        when (mode) {
            TeachingMode.WATCH_THEN_DRAW -> {
                val request = singlePlaybackRequest(start.events, TeacherPlaybackScope.OVERVIEW)
                requireSignalAccepted(
                    engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(request.request.requestId)),
                    "complete Watch Then Draw overview",
                )
                check(engine.state is LessonSessionState.AwaitingChild) {
                    "Watch Then Draw overview did not enter the first child turn: ${engine.state}"
                }
            }

            TeachingMode.DRAW_WITH_ME,
            TeachingMode.TRACE_AND_LEARN,
            -> {
                val request = singlePlaybackRequest(start.events, TeacherPlaybackScope.STEP)
                val completion = requireSignalAccepted(
                    engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(request.request.requestId)),
                    "complete first teacher step",
                )
                assertTraceOverlayIfRequired(packageData, mode, stepIndex = 0, completion.events)
            }
        }

        lesson.drawing.steps.forEachIndexed { stepIndex, step ->
            check(
                engine.state is LessonSessionState.AwaitingChild ||
                    engine.state is LessonSessionState.HelpActive,
            ) {
                "Step ${step.id} did not begin as a child turn: ${engine.state}"
            }

            if (step.help.isNotEmpty()) {
                val help = requireAccepted(engine.dispatch(RequestHelp), "request Help on ${step.id}")
                check(help.state is LessonSessionState.HelpActive) {
                    "Authored Help on ${step.id} did not become active: ${help.state}"
                }
                requireAccepted(engine.dispatch(DismissHelp), "dismiss Help on ${step.id}")
            }

            if (step.childTurn.allowReplay) {
                val replay = requireAccepted(
                    engine.dispatch(ReplayDemonstration),
                    "Replay on ${step.id}",
                )
                val request = singlePlaybackRequest(replay.events, TeacherPlaybackScope.STEP)
                val replayCompletion = requireSignalAccepted(
                    engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(request.request.requestId)),
                    "complete Replay on ${step.id}",
                )
                assertTraceOverlayIfRequired(packageData, mode, stepIndex, replayCompletion.events)
            }

            val completionEvents = when (step.childTurn.completionPolicy) {
                ChildCompletionPolicy.MANUAL_DONE -> requireAccepted(
                    engine.dispatch(MarkChildTurnDone),
                    "complete manual child turn ${step.id}",
                ).events

                ChildCompletionPolicy.ANY_STROKE -> requireSignalAccepted(
                    engine.handle(
                        LessonRuntimeSignal.ChildStrokeCommitted(
                            childDocumentId = childDocumentId,
                            operationId = "content-v2-op-$stepIndex",
                        ),
                    ),
                    "complete any-stroke child turn ${step.id}",
                ).events

                ChildCompletionPolicy.AUTHORED_SIGNAL -> error(
                    "Release gate encountered runtime-unsupported authored_signal on ${lesson.lessonId}/${step.id}.",
                )
            }

            val lastStep = stepIndex == lesson.drawing.steps.lastIndex
            if (lastStep) {
                check(engine.state is LessonSessionState.DrawingComplete) {
                    "Final step ${step.id} did not reach DrawingComplete: ${engine.state}"
                }
            } else {
                when (mode) {
                    TeachingMode.WATCH_THEN_DRAW -> check(engine.state is LessonSessionState.AwaitingChild) {
                        "Watch Then Draw next step did not enter child turn: ${engine.state}"
                    }

                    TeachingMode.DRAW_WITH_ME,
                    TeachingMode.TRACE_AND_LEARN,
                    -> {
                        val request = singlePlaybackRequest(completionEvents, TeacherPlaybackScope.STEP)
                        val teacherCompletion = requireSignalAccepted(
                            engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(request.request.requestId)),
                            "complete teacher step ${lesson.drawing.steps[stepIndex + 1].id}",
                        )
                        assertTraceOverlayIfRequired(
                            packageData,
                            mode,
                            stepIndex = stepIndex + 1,
                            events = teacherCompletion.events,
                        )
                    }
                }
            }
        }
    }

    private fun assertTraceOverlayIfRequired(
        packageData: com.navin.kidsdrawing.lesson.model.LessonRuntimePackage,
        mode: TeachingMode,
        stepIndex: Int,
        events: List<com.navin.kidsdrawing.lesson.session.LessonSessionEvent>,
    ) {
        if (mode != TeachingMode.TRACE_AND_LEARN) return
        val step = packageData.lesson.drawing.steps[stepIndex]
        val intentionalOpenAuthorship =
            step.childTurn.completionPolicy == ChildCompletionPolicy.MANUAL_DONE &&
                step.childTurn.allowSkip &&
                step.childTurn.expectedStrokeRefs.isEmpty()
        val authoredTrace = step.help.any {
            it.kind == HelpKind.TRACE_PATH && it.guideRefs.isNotEmpty()
        } || step.childTurn.expectedStrokeRefs.isNotEmpty()

        if (!intentionalOpenAuthorship && authoredTrace) {
            check(events.any { it is GuideOverlayRequested }) {
                "Trace & Learn step ${step.id} has authored trace geometry but emitted no guide overlay."
            }
        }
    }

    private fun singlePlaybackRequest(
        events: List<com.navin.kidsdrawing.lesson.session.LessonSessionEvent>,
        expectedScope: TeacherPlaybackScope,
    ): TeacherPlaybackRequested {
        val requests = events.filterIsInstance<TeacherPlaybackRequested>()
        check(requests.size == 1) { "Expected one $expectedScope playback request, got $requests" }
        val request = requests.single()
        check(request.request.scope == expectedScope) {
            "Expected playback scope $expectedScope, got ${request.request.scope}."
        }
        check(request.request.sequence.strokes.isNotEmpty()) {
            "Playback request ${request.request.requestId} resolved no authored teacher strokes."
        }
        return request
    }

    private fun requireAccepted(result: LessonCommandResult, operation: String): LessonCommandResult.Accepted =
        result as? LessonCommandResult.Accepted
            ?: error("$operation was rejected: ${(result as LessonCommandResult.Rejected).rejection}")

    private fun requireSignalAccepted(result: LessonSignalResult, operation: String): LessonSignalResult.Accepted =
        result as? LessonSignalResult.Accepted
            ?: error("$operation was rejected: ${(result as LessonSignalResult.Rejected).rejection}")

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
