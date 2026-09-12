package com.navin.kidsdrawing.lesson.lab

import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.ChooseColorMyself
import com.navin.kidsdrawing.lesson.session.ChooseColorWithMe
import com.navin.kidsdrawing.lesson.session.ColoringHandoffMode
import com.navin.kidsdrawing.lesson.session.DismissHelp
import com.navin.kidsdrawing.lesson.session.LessonCommand
import com.navin.kidsdrawing.lesson.session.LessonCommandRejectionCode
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.LessonSignalRejectionCode
import com.navin.kidsdrawing.lesson.session.LessonSignalResult
import com.navin.kidsdrawing.lesson.session.MarkChildTurnDone
import com.navin.kidsdrawing.lesson.session.ReduceHelp
import com.navin.kidsdrawing.lesson.session.ReplayDemonstration
import com.navin.kidsdrawing.lesson.session.RequestHelp
import com.navin.kidsdrawing.lesson.session.RetryRecoverable
import com.navin.kidsdrawing.lesson.session.SkipOverview
import com.navin.kidsdrawing.lesson.session.SkipStep
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonLabRuntimeIntegrationTest {
    @Test
    fun allThreeModesAndFivePacesStartThroughRealRuntimeBridge() = runBlocking {
        TeachingMode.entries.forEach { mode ->
            TeachingPace.entries.forEach { pace ->
                withRuntime { runtime, _ ->
                    val result = runtime.start(mode, pace)
                    assertTrue("$mode / $pace should start", result is LessonCommandResult.Accepted)
                    assertNotNull(runtime.activeTeacherRequest())
                    assertEquals(pace, runtime.teacherSession.state.value.selectedPace)
                    assertEquals(TeacherPlaybackStatus.PLAYING, runtime.teacherSession.state.value.frame?.status)
                    when (mode) {
                        TeachingMode.WATCH_THEN_DRAW ->
                            assertTrue(runtime.sessionState.value is LessonSessionState.OverviewDemonstrating)
                        TeachingMode.DRAW_WITH_ME,
                        TeachingMode.TRACE_AND_LEARN,
                        -> assertTrue(runtime.sessionState.value is LessonSessionState.TeacherDemonstrating)
                    }
                    assertTrue(runtime.diagnostics.value.overlayIsolationPass)
                }
            }
        }
    }

    @Test
    fun traceGuideAndTeacherPlaybackRemainOutsideChildHistoryWhileChildStrokePersists() = runBlocking {
        withRuntime { runtime, _ ->
            runtime.start(TeachingMode.TRACE_AND_LEARN, TeachingPace.NORMAL)
            completeTeacher(runtime)

            assertTrue(runtime.sessionState.value is LessonSessionState.AwaitingChild)
            assertNotNull(runtime.guideOverlay.value)
            assertTrue(runtime.guideOverlay.value!!.sequence.strokes.all {
                it.stroke.authorRole == StrokeAuthorRole.TEACHER_GENERATED
            })
            assertTrue(runtime.documentEngine.state.value.document.operations.isEmpty())

            // Host JVM tests deliberately stop at the app-owned drawing-domain boundary here.
            // Production AtomicDrawingDocumentStore encodes ink samples through AndroidX Ink JNI;
            // that exact native payload path is exercised on Android, not fabricated on the host.
            runtime.documentEngine.commitChildStroke(childStroke("trace-child"))
            assertAccepted(runtime.dispatch(RequestHelp))

            val document = runtime.documentEngine.state.value.document
            assertEquals(1, document.operations.size)
            assertTrue(document.activeInkStrokes().all { it.authorRole == StrokeAuthorRole.CHILD })
            assertEquals(0, runtime.diagnostics.value.nonChildInkOperationCount)
            assertTrue(runtime.diagnostics.value.overlayIsolationPass)
        }
    }

    @Test
    fun pauseResumeReplayHelpAndSkipStayEngineOwned() = runBlocking {
        withRuntime { runtime, _ ->
            runtime.start(TeachingMode.WATCH_THEN_DRAW, TeachingPace.NORMAL)

            assertAccepted(runtime.dispatch(LessonCommand.Pause))
            assertEquals(TeacherPlaybackStatus.PAUSED, runtime.teacherSession.state.value.frame?.status)

            assertAccepted(runtime.dispatch(LessonCommand.Resume))
            assertEquals(TeacherPlaybackStatus.PLAYING, runtime.teacherSession.state.value.frame?.status)

            assertAccepted(runtime.dispatch(SkipOverview))
            assertTrue(runtime.sessionState.value is LessonSessionState.AwaitingChild)
            assertNull(runtime.activeTeacherRequest())

            assertAccepted(runtime.dispatch(ReplayDemonstration))
            assertTrue(runtime.sessionState.value is LessonSessionState.TeacherDemonstrating)
            val helpDuringReplay = runtime.dispatch(RequestHelp) as LessonCommandResult.Rejected
            assertEquals(LessonCommandRejectionCode.INVALID_STATE, helpDuringReplay.rejection.code)

            completeTeacher(runtime)
            assertAccepted(runtime.dispatch(RequestHelp))
            assertEquals(1, (runtime.sessionState.value as LessonSessionState.HelpActive).context.helpLevel)
            assertAccepted(runtime.dispatch(RequestHelp))
            assertEquals(4, (runtime.sessionState.value as LessonSessionState.HelpActive).context.helpLevel)
            assertNotNull(runtime.guideOverlay.value)
            assertAccepted(runtime.dispatch(ReduceHelp))
            assertEquals(1, (runtime.sessionState.value as LessonSessionState.HelpActive).context.helpLevel)
            assertAccepted(runtime.dispatch(DismissHelp))
            assertTrue(runtime.sessionState.value is LessonSessionState.AwaitingChild)

            val invalidSkip = runtime.dispatch(SkipStep) as LessonCommandResult.Rejected
            assertEquals(LessonCommandRejectionCode.SKIP_NOT_ALLOWED, invalidSkip.rejection.code)

            assertAccepted(runtime.dispatch(MarkChildTurnDone)) // head -> ears
            assertAccepted(runtime.dispatch(MarkChildTurnDone)) // ears -> face
            val face = runtime.sessionState.value as LessonSessionState.AwaitingChild
            assertEquals("face", face.context.currentStepId)
            assertAccepted(runtime.dispatch(SkipStep))
            val body = runtime.sessionState.value as LessonSessionState.AwaitingChild
            assertEquals("body_tail", body.context.currentStepId)
        }
    }

    @Test
    fun backgroundAndProcessRecreationRestoreDocumentHelpAndGuideThroughRealStores() = runBlocking {
        val root = newRoot()
        try {
            val first = runtime(root)
            first.start(TeachingMode.DRAW_WITH_ME, TeachingPace.SLOW)
            completeTeacher(first)
            assertAccepted(first.dispatch(RequestHelp))
            assertAccepted(first.dispatch(RequestHelp))
            assertNotNull(first.guideOverlay.value)

            // Erase-mask persistence uses the same real document envelope/store without invoking the
            // Android-only Ink JNI payload codec, so this remains a legitimate host integration test.
            first.commitEraseMask(
                EraseMaskRecord(
                    maskId = "persisted-mask",
                    baseSize = 20f,
                    points = listOf(
                        StrokePoint(
                            x = 430f,
                            y = 420f,
                            elapsedTimeMillis = 0L,
                            pressure = 1f,
                        ),
                    ),
                ),
            )
            first.onBackground()

            val recreated = runtime(root)
            assertEquals(LessonLabRecoveryOutcome.RESTORED, recreated.recover())
            assertEquals(1, recreated.documentEngine.state.value.document.operations.size)
            val state = recreated.sessionState.value as LessonSessionState.HelpActive
            assertEquals(4, state.context.helpLevel)
            assertEquals("head", state.context.currentStepId)
            assertNotNull(recreated.guideOverlay.value)
            assertEquals(0, recreated.diagnostics.value.nonChildInkOperationCount)
            assertTrue(recreated.diagnostics.value.overlayIsolationPass)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun staleTeacherCompletionCannotAdvanceRecreatedRuntime() = runBlocking {
        val root = newRoot()
        try {
            val first = runtime(root)
            first.start(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL)
            val staleRequest = requireNotNull(first.activeTeacherRequest())
            first.onBackground()

            val recreated = runtime(root)
            assertEquals(LessonLabRecoveryOutcome.RESTORED, recreated.recover())
            val freshRequest = requireNotNull(recreated.activeTeacherRequest())
            assertNotEquals(staleRequest, freshRequest)
            assertTrue(recreated.sessionState.value is LessonSessionState.TeacherDemonstrating)

            val stale = recreated.deliverTeacherCompletion(staleRequest) as LessonSignalResult.Rejected
            assertEquals(LessonSignalRejectionCode.STALE_TEACHER_REQUEST, stale.rejection.code)
            assertEquals(freshRequest, recreated.activeTeacherRequest())
            assertTrue(recreated.sessionState.value is LessonSessionState.TeacherDemonstrating)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun teacherFailureRetryUsesFreshRequestAndReturnsToPlayback() = runBlocking {
        withRuntime { runtime, _ ->
            runtime.start(TeachingMode.DRAW_WITH_ME, TeachingPace.FAST)
            val failedRequest = requireNotNull(runtime.activeTeacherRequest())
            runtime.injectTeacherFailure("matrix failure")
            assertTrue(runtime.sessionState.value is LessonSessionState.RecoverableError)

            assertAccepted(runtime.dispatch(RetryRecoverable))
            val retriedRequest = requireNotNull(runtime.activeTeacherRequest())
            assertNotEquals(failedRequest, retriedRequest)
            assertTrue(runtime.sessionState.value is LessonSessionState.TeacherDemonstrating)
            assertEquals(TeacherPlaybackStatus.PLAYING, runtime.teacherSession.state.value.frame?.status)
        }
    }

    @Test
    fun corruptNewestSessionFallsBackToBackupWithDocumentIntact() = runBlocking {
        val root = newRoot()
        try {
            val first = runtime(root)
            first.start(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL)
            assertAccepted(first.dispatch(LessonCommand.Pause))
            first.onBackground()

            val primary = File(
                File(root, "sessions"),
                "${sha256Hex(LessonLabRuntimeCore.SESSION_ID)}.kls",
            )
            assertTrue(primary.isFile)
            primary.writeText("intentionally-corrupt-newest-session")

            val recreated = runtime(root)
            assertEquals(LessonLabRecoveryOutcome.RESTORED, recreated.recover())
            assertTrue(recreated.diagnostics.value.message.contains("session=backup"))
            assertTrue(recreated.diagnostics.value.overlayIsolationPass)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun coloringHandoffFailureAndProcessRecreationReturnToRetryableChoice() = runBlocking {
        val root = newRoot()
        try {
            val first = runtime(root)
            first.start(TeachingMode.WATCH_THEN_DRAW, TeachingPace.VERY_FAST)
            completeTeacher(first)
            repeat(4) { assertAccepted(first.dispatch(MarkChildTurnDone)) }
            assertTrue(first.sessionState.value is LessonSessionState.DrawingComplete)

            assertAccepted(first.dispatch(ChooseColorWithMe))
            assertEquals(ColoringHandoffMode.COLOR_WITH_ME, first.pendingColoringMode())
            assertTrue(first.sessionState.value is LessonSessionState.HandingOffToColoring)
            first.onBackground()

            val recreated = runtime(root)
            assertEquals(LessonLabRecoveryOutcome.RESTORED, recreated.recover())
            assertTrue(recreated.sessionState.value is LessonSessionState.AwaitingPostDrawingChoice)
            assertNull(recreated.pendingColoringMode())

            assertAccepted(recreated.dispatch(ChooseColorMyself))
            assertEquals(ColoringHandoffMode.COLOR_MYSELF, recreated.pendingColoringMode())
            recreated.simulateColoringUnavailable()
            assertTrue(recreated.sessionState.value is LessonSessionState.AwaitingPostDrawingChoice)
            assertNull(recreated.pendingColoringMode())
        } finally {
            root.deleteRecursively()
        }
    }

    private suspend fun completeTeacher(runtime: LessonLabRuntimeCore) {
        assertNotNull("Expected active teacher playback", runtime.activeTeacherRequest())
        runtime.advanceTeacherBy(1_000_000L)
        assertNull("Playback should complete and clear active request", runtime.activeTeacherRequest())
    }

    private fun childStroke(id: String) = InkStrokeRecord(
        strokeId = id,
        brushPresetId = "pencil-soft",
        colorArgb = 0xFF202124.toInt(),
        opacity = 1f,
        baseSize = 10f,
        tool = PointerTool.FINGER,
        points = listOf(
            StrokePoint(
                x = 420f,
                y = 410f,
                elapsedTimeMillis = 0L,
                pressure = 1f,
            ),
            StrokePoint(
                x = 450f,
                y = 440f,
                elapsedTimeMillis = 20L,
                pressure = 1f,
            ),
        ),
        authorRole = StrokeAuthorRole.CHILD,
    )

    private fun assertAccepted(result: LessonCommandResult?) {
        assertTrue("Expected command acceptance, got $result", result is LessonCommandResult.Accepted)
    }

    private suspend fun withRuntime(block: suspend (LessonLabRuntimeCore, File) -> Unit) {
        val root = newRoot()
        try {
            block(runtime(root), root)
        } finally {
            root.deleteRecursively()
        }
    }

    private fun runtime(root: File): LessonLabRuntimeCore = LessonLabRuntimeCore.forTest(
        rootDirectory = root,
        source = LessonPackageSource { path ->
            File("src/main/assets/$path").takeIf(File::isFile)?.readText()
        },
    )

    private fun newRoot(): File = Files.createTempDirectory("lesson-lab-runtime-").toFile()

    private fun sha256Hex(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
