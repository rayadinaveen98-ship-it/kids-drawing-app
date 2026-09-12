package com.navin.kidsdrawing.product.lesson

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import com.navin.kidsdrawing.drawing.ui.TeacherPlaybackOverlay
import com.navin.kidsdrawing.lesson.model.TeachingMode
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
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.coloring.ProductColoringStartResult
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.profile.AgeBand
import kotlinx.coroutines.launch

@Composable
fun GuidedLessonScreen(
    runtime: ProductLessonRuntime,
    coloringRuntime: ProductColoringRuntime,
    ageBand: AgeBand,
    startMode: TeachingMode,
    startPace: TeachingPace,
    startFreshRequested: Boolean,
    onFreshSessionStarted: () -> Unit,
    onColoringReady: () -> Unit,
    onExitToHome: () -> Unit,
    onFinishedForNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val layout = lessonLayoutPolicyFor(ageBand)
    val surfaceController = remember { DrawingSurfaceController() }
    val documentState by runtime.documentEngine.state.collectAsState()
    val toolSettings by runtime.toolEngine.state.collectAsState()
    val teacherState by runtime.teacherSession.state.collectAsState()
    val sessionState by runtime.sessionState.collectAsState()
    val guideOverlay by runtime.guideOverlay.collectAsState()
    val diagnostics by runtime.diagnostics.collectAsState()
    var recoveryReady by remember { mutableStateOf(false) }
    var startupMessage by remember { mutableStateOf<String?>(null) }
    var coloringMessage by remember { mutableStateOf<String?>(null) }
    var coloringStarting by remember { mutableStateOf(false) }

    LaunchedEffect(runtime) {
        val outcome = runtime.recover()
        val recoveredState = runtime.sessionState.value
        when (
            ProductLessonStartupPolicy.decide(
                startFreshRequested = startFreshRequested,
                recoveryOutcome = outcome,
                recoveredState = recoveredState,
            )
        ) {
            ProductLessonStartupDecision.RESUME_RESTORED -> Unit
            ProductLessonStartupDecision.RESUME_NOT_FOUND -> {
                startupMessage = "We couldn’t find a drawing to continue. Your studio is still safe."
            }
            ProductLessonStartupDecision.START_FRESH -> {
                runtime.newSessionDocument()
                val started = runtime.start(startMode, startPace)
                if (started != null) {
                    onFreshSessionStarted()
                } else {
                    startupMessage = "This lesson needs a moment before it can start. Your studio is still safe."
                }
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
                    minimumControlHeight = layout.minimumControlHeight,
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
                                    .fillMaxSize()
                                    .semantics {
                                        contentDescription = "Drawing is paused while the teacher demonstrates"
                                    }
                                    .consumeDrawingInput(),
                            )
                        }
                    }
                }

                if (presentation.showPostDrawingChoices) {
                    PostDrawingBoundary(
                        minimumControlHeight = layout.minimumControlHeight,
                        message = coloringMessage,
                        enabled = !coloringStarting,
                        onColorWithMe = {
                            coloringStarting = true
                            coloringMessage = null
                            scope.launch {
                                when (val result = coloringRuntime.beginFromLesson(ColoringSessionMode.COLOR_WITH_ME)) {
                                    is ProductColoringStartResult.Ready -> onColoringReady()
                                    is ProductColoringStartResult.Failed -> {
                                        coloringStarting = false
                                        coloringMessage = result.message
                                    }
                                }
                            }
                        },
                        onColorMyself = {
                            coloringStarting = true
                            coloringMessage = null
                            scope.launch {
                                when (val result = coloringRuntime.beginFromLesson(ColoringSessionMode.COLOR_MYSELF)) {
                                    is ProductColoringStartResult.Ready -> onColoringReady()
                                    is ProductColoringStartResult.Failed -> {
                                        coloringStarting = false
                                        coloringMessage = result.message
                                    }
                                }
                            }
                        },
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
                        minimumControlHeight = layout.minimumControlHeight,
                        maxColumns = layout.maxCompactActionColumns,
                    )
                    DrawingToolControls(
                        runtime = runtime,
                        childCanDraw = childCanDraw,
                        minimumControlHeight = layout.minimumControlHeight,
                    )
                }
            }
        }
    }
}

private fun Modifier.consumeDrawingInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            event.changes.forEach { change -> change.consume() }
        }
    }
}

@Composable
private fun WorkspaceTopBar(
    presentation: LessonWorkspacePresentation,
    minimumControlHeight: Dp,
    onSaveAndExit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onSaveAndExit,
            modifier = Modifier
                .heightIn(min = minimumControlHeight)
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
    minimumControlHeight: Dp,
    maxColumns: Int,
) {
    val scope = rememberCoroutineScope()
    val actions = buildList {
        if (presentation.showPause) add(WorkspaceActionSpec("Pause") { scope.launch { runtime.dispatch(LessonCommand.Pause) } })
        if (presentation.showResume) add(WorkspaceActionSpec("Resume") { scope.launch { runtime.dispatch(LessonCommand.Resume) } })
        if (presentation.showReplay) add(WorkspaceActionSpec("Replay") { scope.launch { runtime.dispatch(ReplayDemonstration) } })
        if (presentation.showHelp) add(WorkspaceActionSpec("Help") { scope.launch { runtime.dispatch(RequestHelp) } })
        if (presentation.showRetry) add(WorkspaceActionSpec("Try again") { scope.launch { runtime.dispatch(RetryRecoverable) } })
        if (presentation.showReduceHelp) add(WorkspaceActionSpec("Less help") { scope.launch { runtime.dispatch(ReduceHelp) } })
        if (presentation.showDismissHelp) add(WorkspaceActionSpec("Hide help") { scope.launch { runtime.dispatch(DismissHelp) } })
        if (presentation.showSkipOverview) add(WorkspaceActionSpec("I’m ready") { scope.launch { runtime.dispatch(SkipOverview) } })
        if (presentation.showSkipStep) add(WorkspaceActionSpec("Skip this part") { scope.launch { runtime.dispatch(SkipStep) } })
        if (presentation.showDone) add(
            WorkspaceActionSpec("Done", primary = true) {
                scope.launch { runtime.dispatch(MarkChildTurnDone) }
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        WorkspaceActionGrid(
            actions = actions,
            minimumControlHeight = minimumControlHeight,
            maxColumns = maxColumns,
        )
        if (!presentation.isTerminal) {
            PaceControl(
                runtime = runtime,
                currentPace = currentPace,
                minimumControlHeight = minimumControlHeight,
            )
        }
    }
}

@Composable
private fun WorkspaceActionGrid(
    actions: List<WorkspaceActionSpec>,
    minimumControlHeight: Dp,
    maxColumns: Int,
) {
    if (actions.isEmpty()) return
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 420.dp) 2 else maxColumns.coerceAtLeast(2)
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            actions.chunked(columns).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    rowActions.forEach { action ->
                        WorkspaceButton(
                            label = action.label,
                            modifier = Modifier.weight(1f),
                            primary = action.primary,
                            minimumHeight = minimumControlHeight,
                            onClick = action.action,
                        )
                    }
                    repeat(columns - rowActions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaceControl(
    runtime: ProductLessonRuntime,
    currentPace: TeachingPace,
    minimumControlHeight: Dp,
) {
    val scope = rememberCoroutineScope()
    val next = TeachingPace.entries[(currentPace.ordinal + 1) % TeachingPace.entries.size]
    TextButton(
        onClick = { scope.launch { runtime.dispatch(LessonCommand.SetPace(next)) } },
        modifier = Modifier
            .heightIn(min = minimumControlHeight)
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
private fun DrawingToolControls(
    runtime: ProductLessonRuntime,
    childCanDraw: Boolean,
    minimumControlHeight: Dp,
) {
    val settings by runtime.toolEngine.state.collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WorkspaceButton(
            label = if (settings.tool == DrawingTool.PENCIL) "✓ Pencil" else "Pencil",
            modifier = Modifier.weight(1f),
            enabled = childCanDraw,
            minimumHeight = minimumControlHeight,
        ) { runtime.toolEngine.selectTool(DrawingTool.PENCIL) }
        WorkspaceButton(
            label = if (settings.tool == DrawingTool.ERASER) "✓ Eraser" else "Eraser",
            modifier = Modifier.weight(1f),
            enabled = childCanDraw,
            minimumHeight = minimumControlHeight,
        ) { runtime.toolEngine.selectTool(DrawingTool.ERASER) }
    }
}

@Composable
private fun PostDrawingBoundary(
    minimumControlHeight: Dp,
    message: String?,
    enabled: Boolean,
    onColorWithMe: () -> Unit,
    onColorMyself: () -> Unit,
    onFinish: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = StudioColors.Studio100,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Your drawing is ready",
                style = MaterialTheme.typography.headlineSmall,
                color = StudioColors.Ink900,
            )
            Text(
                text = "Keep going with colors, or save your cat for another time.",
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Coral500,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WorkspaceButton(
                    label = "Color with me",
                    modifier = Modifier.weight(1f),
                    primary = true,
                    enabled = enabled,
                    minimumHeight = minimumControlHeight,
                    onClick = onColorWithMe,
                )
                WorkspaceButton(
                    label = "Color myself",
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    minimumHeight = minimumControlHeight,
                    onClick = onColorMyself,
                )
            }
            WorkspaceButton(
                label = "Finish for now",
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                minimumHeight = minimumControlHeight,
                onClick = onFinish,
            )
        }
    }
}

@Composable
private fun WorkspaceButton(
    label: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    minimumHeight: Dp = 52.dp,
    onClick: () -> Unit,
) {
    if (primary) {
        Surface(
            modifier = modifier
                .heightIn(min = minimumHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) StudioColors.Studio600 else StudioColors.Line200),
            color = Color.Transparent,
        ) {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minimumHeight),
            ) {
                Text(label, color = if (enabled) Color.White else StudioColors.Ink500, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.heightIn(min = minimumHeight),
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

private data class WorkspaceActionSpec(
    val label: String,
    val primary: Boolean = false,
    val action: () -> Unit,
)

private fun TeachingPace.childLabel(): String = when (this) {
    TeachingPace.EXTRA_SLOW -> "extra slow"
    TeachingPace.SLOW -> "slow"
    TeachingPace.NORMAL -> "normal"
    TeachingPace.FAST -> "fast"
    TeachingPace.VERY_FAST -> "very fast"
}
