package com.navin.kidsdrawing.product.lesson

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import com.navin.kidsdrawing.drawing.ui.TeacherPlaybackOverlay
import com.navin.kidsdrawing.lesson.lab.LessonLabRecoveryOutcome
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.ChooseColorMyself
import com.navin.kidsdrawing.lesson.session.ChooseColorWithMe
import com.navin.kidsdrawing.lesson.session.DismissHelp
import com.navin.kidsdrawing.lesson.session.FinishForNow
import com.navin.kidsdrawing.lesson.session.LessonCommand
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.MarkChildTurnDone
import com.navin.kidsdrawing.lesson.session.ReduceHelp
import com.navin.kidsdrawing.lesson.session.ReplayDemonstration
import com.navin.kidsdrawing.lesson.session.RequestHelp
import com.navin.kidsdrawing.lesson.session.RetryRecoverable
import com.navin.kidsdrawing.lesson.session.SkipOverview
import com.navin.kidsdrawing.lesson.session.SkipStep
import com.navin.kidsdrawing.product.design.StudioColors
import kotlinx.coroutines.launch

@Composable
fun GuidedLessonScreen(
    runtime: ProductLessonRuntime,
    startMode: TeachingMode,
    startPace: TeachingPace,
    resumeOnly: Boolean,
    onExitToHome: () -> Unit,
    onFinishedForNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceController = remember { DrawingSurfaceController() }
    val documentState by runtime.documentEngine.state.collectAsState()
    val toolSettings by runtime.toolEngine.state.collectAsState()
    val teacherState by runtime.teacherSession.state.collectAsState()
    val sessionState by runtime.sessionState.collectAsState()
    val guideOverlay by runtime.guideOverlay.collectAsState()
    val diagnostics by runtime.diagnostics.collectAsState()
    var recoveryReady by remember { mutableStateOf(false) }
    var startupMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(runtime) {
        val outcome = runtime.recover()
        when {
            outcome == LessonLabRecoveryOutcome.RESTORED && sessionState !is LessonSessionState.Finished -> Unit
            resumeOnly -> startupMessage = "We couldn’t find a drawing to continue. Your studio is still safe."
            else -> {
                runtime.newSessionDocument()
                runtime.start(startMode, startPace)
            }
        }
        surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
        recoveryReady = true
    }

    DisposableEffect(runtime, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                scope.launch { runtime.onBackground() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(documentState.document.operations.size, documentState.document.modifiedAtEpochMillis) {
        if (recoveryReady) {
            surfaceController.reconcileDocument(documentState.document)
        }
    }

    val teacherFrame = teacherState.frame
    LaunchedEffect(teacherFrame?.status, teacherState.sequenceId) {
        if (teacherFrame?.status != TeacherPlaybackStatus.PLAYING) return@LaunchedEffect
        var previousFrameNanos = withFrameNanos { it }
        while (true) {
            val frameNanos = withFrameNanos { it }
            val elapsedMillis = ((frameNanos - previousFrameNanos) / 1_000_000L).coerceAtLeast(0L)
            previousFrameNanos = frameNanos
            runtime.advanceTeacherBy(elapsedMillis)
            if (runtime.teacherSession.state.value.frame?.status != TeacherPlaybackStatus.PLAYING) break
        }
    }

    val presentation = ProductLessonPresentationPolicy.from(sessionState, runtime.packageData)
    val childCanDraw = sessionState is LessonSessionState.AwaitingChild ||
        sessionState is LessonSessionState.HelpActive

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        if (startupMessage != null) {
            LessonStartupMessage(
                message = startupMessage.orEmpty(),
                onBack = onExitToHome,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WorkspaceTopBar(
                    presentation = presentation,
                    onSaveAndExit = {
                        scope.launch {
                            runtime.saveNow()
                            onExitToHome()
                        }
                    },
                )

                CompanionInstruction(
                    presentation = presentation,
                    isolationPass = diagnostics.overlayIsolationPass,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White),
                ) {
                    if (!recoveryReady) {
                        Text(
                            text = "Opening your drawing…",
                            modifier = Modifier.align(Alignment.Center),
                            color = StudioColors.Ink500,
                        )
                    } else {
                        DrawingSurface(
                            modifier = Modifier.fillMaxSize(),
                            controller = surfaceController,
                            toolSettings = toolSettings,
                            onStrokeCommitted = { stroke ->
                                scope.launch { runtime.commitChildStroke(stroke) }
                            },
                            onEraseMaskCommitted = { mask ->
                                scope.launch { runtime.commitEraseMask(mask) }
                            },
                        )

                        TeacherPlaybackOverlay(
                            strokes = guideOverlay?.sequence?.strokes?.map { it.stroke }.orEmpty(),
                            modifier = Modifier.fillMaxSize(),
                        )
                        TeacherPlaybackOverlay(
                            strokes = teacherFrame?.visibleStrokes.orEmpty(),
                            modifier = Modifier.fillMaxSize(),
                        )

                        if (!childCanDraw) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .semantics {
                                        contentDescription = "Drawing is paused while the teacher demonstrates"
                                    }
                                    .consumeDrawingInput(),
                            )
                        }
                    }
                }

                if (presentation.showPostDrawingChoices) {
                    PostDrawingChoices(
                        onColorWithMe = { scope.launch { runtime.dispatch(ChooseColorWithMe) } },
                        onColorMyself = { scope.launch { runtime.dispatch(ChooseColorMyself) } },
                        onFinish = {
                            scope.launch {
                                runtime.dispatch(FinishForNow)
                                onFinishedForNow()
                            }
                        },
                    )
                } else {
                    EssentialLessonControls(
                        runtime = runtime,
                        presentation = presentation,
                        currentPace = (sessionState as? LessonSessionState.Contextual)?.context?.pace ?: startPace,
                    )
                    DrawingToolControls(runtime = runtime, childCanDraw = childCanDraw)
                }
            }
        }
    }
}

private fun Modifier.consumeDrawingInput(): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitPointerEventScope {
            val first = awaitPointerEvent().changes.firstOrNull() ?: return@awaitPointerEventScope
            first.consume()
            while (first.pressed) {
                val event = awaitPointerEvent()
                event.changes.forEach { it.consume() }
                if (event.changes.none { it.pressed }) break
            }
        }
    }
}

@Composable
private fun WorkspaceTopBar(
    presentation: LessonWorkspacePresentation,
    onSaveAndExit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onSaveAndExit,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .semantics { contentDescription = "Save drawing and return to studio" },
        ) {
            Text("← Save & leave", color = StudioColors.Ink700)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = presentation.stepLabel ?: "Cute Cat",
                style = MaterialTheme.typography.titleLarge,
                color = StudioColors.Ink900,
            )
            LinearProgressIndicator(
                progress = { presentation.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                color = StudioColors.Studio600,
                trackColor = StudioColors.Studio100,
            )
        }
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = StudioColors.Studio100,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("✦", color = StudioColors.Studio600)
            }
        }
    }
}

@Composable
private fun CompanionInstruction(
    presentation: LessonWorkspacePresentation,
    isolationPass: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = when (presentation.companionState) {
            CompanionSemanticState.HELPING -> StudioColors.Sun500.copy(alpha = 0.12f)
            CompanionSemanticState.GENTLE_ERROR -> StudioColors.Coral500.copy(alpha = 0.10f)
            else -> StudioColors.Paper100
        },
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompanionPreviewFace(modifier = Modifier.size(44.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = presentation.eyebrow,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudioColors.Studio600,
                )
                Text(
                    text = presentation.instruction,
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink700,
                )
            }
            if (!isolationPass) {
                Text("!", color = StudioColors.Coral500, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EssentialLessonControls(
    runtime: ProductLessonRuntime,
    presentation: LessonWorkspacePresentation,
    currentPace: TeachingPace,
) {
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (presentation.showPause) {
                WorkspaceButton("Pause", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(LessonCommand.Pause) }
                }
            }
            if (presentation.showResume) {
                WorkspaceButton("Resume", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(LessonCommand.Resume) }
                }
            }
            if (presentation.showReplay) {
                WorkspaceButton("Replay", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(ReplayDemonstration) }
                }
            }
            if (presentation.showHelp) {
                WorkspaceButton("Help", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(RequestHelp) }
                }
            }
            if (presentation.showRetry) {
                WorkspaceButton("Try again", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(RetryRecoverable) }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (presentation.showReduceHelp) {
                WorkspaceButton("Less help", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(ReduceHelp) }
                }
            }
            if (presentation.showDismissHelp) {
                WorkspaceButton("Hide help", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(DismissHelp) }
                }
            }
            if (presentation.showSkipOverview) {
                WorkspaceButton("I’m ready", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(SkipOverview) }
                }
            }
            if (presentation.showSkipStep) {
                WorkspaceButton("Skip this part", Modifier.weight(1f)) {
                    scope.launch { runtime.dispatch(SkipStep) }
                }
            }
            if (presentation.showDone) {
                WorkspaceButton("Done", Modifier.weight(1f), primary = true) {
                    scope.launch { runtime.dispatch(MarkChildTurnDone) }
                }
            }
        }

        if (!presentation.isTerminal) {
            PaceControl(runtime = runtime, currentPace = currentPace)
        }
    }
}

@Composable
private fun PaceControl(runtime: ProductLessonRuntime, currentPace: TeachingPace) {
    val scope = rememberCoroutineScope()
    val next = TeachingPace.entries[(currentPace.ordinal + 1) % TeachingPace.entries.size]
    TextButton(
        onClick = { scope.launch { runtime.dispatch(LessonCommand.SetPace(next)) } },
        modifier = Modifier
            .heightIn(min = 44.dp)
            .semantics { contentDescription = "Drawing speed ${currentPace.childLabel()}. Tap for ${next.childLabel()}" },
    ) {
        Text(
            text = "Speed: ${currentPace.childLabel()}",
            style = MaterialTheme.typography.bodyMedium,
            color = StudioColors.Ink700,
        )
    }
}

@Composable
private fun DrawingToolControls(runtime: ProductLessonRuntime, childCanDraw: Boolean) {
    val settings by runtime.toolEngine.state.collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WorkspaceButton(
            label = if (settings.tool == DrawingTool.PENCIL) "✓ Pencil" else "Pencil",
            modifier = Modifier.weight(1f),
            enabled = childCanDraw,
        ) { runtime.toolEngine.selectTool(DrawingTool.PENCIL) }
        WorkspaceButton(
            label = if (settings.tool == DrawingTool.ERASER) "✓ Eraser" else "Eraser",
            modifier = Modifier.weight(1f),
            enabled = childCanDraw,
        ) { runtime.toolEngine.selectTool(DrawingTool.ERASER) }
    }
}

@Composable
private fun PostDrawingChoices(
    onColorWithMe: () -> Unit,
    onColorMyself: () -> Unit,
    onFinish: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "What next?",
            style = MaterialTheme.typography.headlineSmall,
            color = StudioColors.Ink900,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkspaceButton("Color with me", Modifier.weight(1f), primary = true, onClick = onColorWithMe)
            WorkspaceButton("Color myself", Modifier.weight(1f), onClick = onColorMyself)
        }
        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Finish for now", color = StudioColors.Ink700)
        }
    }
}

@Composable
private fun WorkspaceButton(
    label: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    if (primary) {
        Surface(
            modifier = modifier
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) StudioColors.Studio600 else StudioColors.Line200),
            color = Color.Transparent,
        ) {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(label, color = if (enabled) Color.White else StudioColors.Ink500, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StudioColors.Line200),
        ) {
            Text(label, color = StudioColors.Ink700)
        }
    }
}

@Composable
private fun LessonStartupMessage(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompanionPreviewFace()
        Text(
            text = "Your artwork is safe",
            modifier = Modifier.padding(top = 18.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = StudioColors.Ink900,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = StudioColors.Ink700,
        )
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text("Back to studio", color = StudioColors.Studio600)
        }
    }
}

private fun TeachingPace.childLabel(): String = when (this) {
    TeachingPace.EXTRA_SLOW -> "extra slow"
    TeachingPace.SLOW -> "slow"
    TeachingPace.NORMAL -> "normal"
    TeachingPace.FAST -> "fast"
    TeachingPace.VERY_FAST -> "very fast"
}
