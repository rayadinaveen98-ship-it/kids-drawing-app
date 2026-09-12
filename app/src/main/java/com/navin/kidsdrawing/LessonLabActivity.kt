package com.navin.kidsdrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import com.navin.kidsdrawing.drawing.ui.TeacherPlaybackOverlay
import com.navin.kidsdrawing.lesson.lab.LessonLabDiagnostics
import com.navin.kidsdrawing.lesson.lab.LessonLabRuntime
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
import kotlinx.coroutines.launch

class LessonLabActivity : ComponentActivity() {
    private lateinit var runtime: LessonLabRuntime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runtime = LessonLabRuntime(this)
        setContent {
            MaterialTheme {
                LessonLabScreen(runtime)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!::runtime.isInitialized) return
        lifecycleScope.launch { runtime.onBackground() }
    }
}

@Composable
private fun LessonLabScreen(runtime: LessonLabRuntime) {
    val scope = rememberCoroutineScope()
    val surfaceController = remember { DrawingSurfaceController() }
    val documentState by runtime.documentEngine.state.collectAsState()
    val toolSettings by runtime.toolEngine.state.collectAsState()
    val teacherState by runtime.teacherSession.state.collectAsState()
    val sessionState by runtime.sessionState.collectAsState()
    val guideOverlay by runtime.guideOverlay.collectAsState()
    val diagnostics by runtime.diagnostics.collectAsState()

    var selectedMode by rememberSaveable { mutableStateOf(TeachingMode.DRAW_WITH_ME) }
    var selectedPace by rememberSaveable { mutableStateOf(TeachingPace.NORMAL) }
    var metrics by remember { mutableStateOf(DrawingSurfaceMetrics()) }
    var recoveryReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runtime.recover()
        surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
        recoveryReady = true
    }

    // The engine remains authoritative after process recreation. Mirror restored mode/pace back into
    // the engineering selectors so the controls never imply a different session configuration.
    LaunchedEffect(sessionState) {
        val contextual = sessionState as? LessonSessionState.Contextual ?: return@LaunchedEffect
        selectedMode = contextual.context.mode
        selectedPace = contextual.context.pace
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

    Surface(modifier = Modifier.fillMaxSize(), color = LabPaper) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                "Kids Drawing · Lesson Lab",
                color = LabInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "0.2 · Cute Cat · Lesson Engine + Drawing Engine + recovery",
                color = LabMuted,
                fontSize = 11.sp,
            )

            DiagnosticStrip(diagnostics)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.56f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                SelectorGrid(
                    title = "Mode",
                    values = TeachingMode.entries,
                    selected = selectedMode,
                    label = TeachingMode::labLabel,
                    onSelected = { selectedMode = it },
                )
                SelectorGrid(
                    title = "Pace",
                    values = TeachingPace.entries,
                    selected = selectedPace,
                    label = { pace -> pace.labLabel() },
                    onSelected = {
                        selectedPace = it
                        scope.launch { runtime.dispatch(LessonCommand.SetPace(it)) }
                    },
                )

                Text("Session", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = LabInk)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            runtime.start(selectedMode, selectedPace)
                            surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
                        }
                    },
                ) {
                    Text("Start lesson", fontSize = 11.sp)
                }
                ResponsiveActionGrid(
                    actions = listOf(
                        LabActionSpec("New") {
                            scope.launch {
                                runtime.newSessionDocument()
                                surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
                            }
                        },
                        LabActionSpec("Recover") {
                            scope.launch {
                                runtime.recover()
                                surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
                            }
                        },
                        LabActionSpec("Save") { scope.launch { runtime.saveNow() } },
                        LabActionSpec("Save & Exit") {
                            scope.launch { runtime.dispatch(LessonCommand.SaveAndExit) }
                        },
                    ),
                )

                SessionCommandRows(runtime, sessionState)
                DrawingToolRow(toolSettings, runtime)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.44f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (!recoveryReady) {
                    Text("Recovering Lesson Lab…", color = LabMuted)
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
                        onMetricsChanged = { metrics = it },
                    )

                    val guideStrokes = guideOverlay
                        ?.sequence
                        ?.strokes
                        ?.map { it.stroke }
                        .orEmpty()
                    TeacherPlaybackOverlay(
                        strokes = guideStrokes,
                        modifier = Modifier.fillMaxSize(),
                    )
                    TeacherPlaybackOverlay(
                        strokes = teacherFrame?.visibleStrokes.orEmpty(),
                        modifier = Modifier.fillMaxSize(),
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LabPanel.copy(alpha = 0.94f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text("Runtime diagnostics", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("surface ${metrics.viewportWidthPx}×${metrics.viewportHeightPx}", fontSize = 10.sp)
                        Text(
                            "playback ${teacherFrame?.status ?: "idle"} · guide ${guideOverlay?.purpose ?: "none"}",
                            fontSize = 10.sp,
                        )
                        Text(
                            "operations ${diagnostics.childOperationCount} · child ink ${diagnostics.activeChildInkCount}",
                            fontSize = 10.sp,
                        )
                        Text(
                            "teacher/guide in child history: ${diagnostics.nonChildInkOperationCount} · " +
                                if (diagnostics.overlayIsolationPass) "PASS" else "FAIL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCommandRows(
    runtime: LessonLabRuntime,
    sessionState: LessonSessionState?,
) {
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        LabSection("Playback & help") {
            ResponsiveActionGrid(
                actions = listOf(
                    LabActionSpec("Pause") { scope.launch { runtime.dispatch(LessonCommand.Pause) } },
                    LabActionSpec("Resume") { scope.launch { runtime.dispatch(LessonCommand.Resume) } },
                    LabActionSpec("Replay") { scope.launch { runtime.dispatch(ReplayDemonstration) } },
                    LabActionSpec("Help +") { scope.launch { runtime.dispatch(RequestHelp) } },
                    LabActionSpec("Help −") { scope.launch { runtime.dispatch(ReduceHelp) } },
                    LabActionSpec("Dismiss Help") { scope.launch { runtime.dispatch(DismissHelp) } },
                ),
            )
        }

        LabSection("Lesson progression") {
            ResponsiveActionGrid(
                actions = listOf(
                    LabActionSpec("Done") { scope.launch { runtime.dispatch(MarkChildTurnDone) } },
                    LabActionSpec("Skip Step") { scope.launch { runtime.dispatch(SkipStep) } },
                    LabActionSpec("Skip Overview") { scope.launch { runtime.dispatch(SkipOverview) } },
                ),
            )
        }

        LabSection("Failure & retry") {
            ResponsiveActionGrid(
                actions = listOf(
                    LabActionSpec("Inject Playback Failure") { scope.launch { runtime.injectTeacherFailure() } },
                    LabActionSpec("Retry") { scope.launch { runtime.dispatch(RetryRecoverable) } },
                ),
            )
        }

        LabSection("Post-drawing handoff") {
            ResponsiveActionGrid(
                actions = listOf(
                    LabActionSpec("Color With Me") { scope.launch { runtime.dispatch(ChooseColorWithMe) } },
                    LabActionSpec("Color Myself") { scope.launch { runtime.dispatch(ChooseColorMyself) } },
                    LabActionSpec("Finish Now") { scope.launch { runtime.dispatch(FinishForNow) } },
                    LabActionSpec("Coloring Unavailable") { scope.launch { runtime.simulateColoringUnavailable() } },
                    LabActionSpec("Handoff ACK") { scope.launch { runtime.simulateColoringContractAck() } },
                ),
            )
        }

        Text(
            text = "Engine state: ${sessionState?.let { it::class.simpleName } ?: "No session"}. " +
                "Invalid controls intentionally exercise deterministic command rejection.",
            color = LabMuted,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun DrawingToolRow(settings: DrawingToolSettings, runtime: LessonLabRuntime) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Drawing tools", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = LabInk)
        ResponsiveActionGrid(
            actions = listOf(
                LabActionSpec(if (settings.tool == DrawingTool.PENCIL) "✓ Pencil" else "Pencil") {
                    runtime.toolEngine.selectTool(DrawingTool.PENCIL)
                },
                LabActionSpec(if (settings.tool == DrawingTool.ERASER) "✓ Eraser" else "Eraser") {
                    runtime.toolEngine.selectTool(DrawingTool.ERASER)
                },
                LabActionSpec("Ink 1") { runtime.toolEngine.setColor(0xFF202124.toInt()) },
                LabActionSpec("Ink 2") { runtime.toolEngine.setColor(0xFF1565C0.toInt()) },
                LabActionSpec("Ink 3") { runtime.toolEngine.setColor(0xFFC62828.toInt()) },
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Width ${settings.width.roundToOne()}", fontSize = 10.sp, color = LabMuted)
            Slider(
                value = settings.width,
                onValueChange = runtime.toolEngine::setWidth,
                valueRange = DrawingToolSettings.MIN_TOOL_WIDTH..DrawingToolSettings.MAX_TOOL_WIDTH,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DiagnosticStrip(diagnostics: LessonLabDiagnostics) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LabPanel)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(diagnostics.packageStatus, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = LabInk)
        Text(
            "${diagnostics.lessonState} · step ${diagnostics.step} · help ${diagnostics.helpLevel} · " +
                "overview ${diagnostics.overviewCompleted ?: "—"}",
            fontSize = 10.sp,
            color = LabMuted,
        )
        Text(
            "teacher ${diagnostics.activeTeacherRequestId?.takeLast(22) ?: "—"} · " +
                "guide ${diagnostics.activeGuideId ?: "—"}",
            fontSize = 10.sp,
            color = LabMuted,
        )
        Text(diagnostics.message, fontSize = 10.sp, color = LabInk)
    }
}

@Composable
private fun <T> SelectorGrid(
    title: String,
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = LabInk)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = responsiveColumnCount(maxWidth.value)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                values.chunked(columns).forEach { rowValues ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        rowValues.forEach { value ->
                            if (value == selected) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onSelected(value) },
                                ) {
                                    Text(label(value), fontSize = 10.sp)
                                }
                            } else {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onSelected(value) },
                                ) {
                                    Text(label(value), fontSize = 10.sp)
                                }
                            }
                        }
                        repeat(columns - rowValues.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponsiveActionGrid(actions: List<LabActionSpec>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = responsiveColumnCount(maxWidth.value)
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            actions.chunked(columns).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    rowActions.forEach { action ->
                        LabAction(
                            label = action.label,
                            action = action.action,
                            modifier = Modifier.weight(1f),
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
private fun LabSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = LabInk)
        content()
    }
}

@Composable
private fun LabAction(label: String, action: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(modifier = modifier, onClick = action) {
        Text(label, fontSize = 10.sp)
    }
}

private data class LabActionSpec(
    val label: String,
    val action: () -> Unit,
)

private fun responsiveColumnCount(widthDp: Float): Int = when {
    widthDp >= 720f -> 4
    widthDp >= 480f -> 3
    else -> 2
}

private fun TeachingMode.labLabel(): String = when (this) {
    TeachingMode.DRAW_WITH_ME -> "Draw With Me"
    TeachingMode.WATCH_THEN_DRAW -> "Watch Then Draw"
    TeachingMode.TRACE_AND_LEARN -> "Trace & Learn"
}

private fun TeachingPace.labLabel(): String =
    "${name.lowercase().replace('_', ' ')} · ${multiplier}×"

private fun Float.roundToOne(): String = "%.1f".format(this)

private val LabPaper = Color(0xFFF7F1E7)
private val LabPanel = Color(0xFFEDE5D7)
private val LabInk = Color(0xFF2C2925)
private val LabMuted = Color(0xFF6D675F)
