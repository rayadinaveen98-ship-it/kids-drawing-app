package com.navin.kidsdrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.navin.kidsdrawing.lesson.lab.LessonLabRuntime
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.*
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                "Kids Drawing · Lesson Lab 0.2",
                color = LabInk,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Real Cute Cat package · Lesson Engine + Drawing Engine + recovery integration",
                color = LabMuted,
                fontSize = 12.sp,
            )

            DiagnosticStrip(diagnostics)

            SelectorRow(
                title = "Mode",
                values = TeachingMode.entries,
                selected = selectedMode,
                label = TeachingMode::labLabel,
                onSelected = { selectedMode = it },
            )
            SelectorRow(
                title = "Pace",
                values = TeachingPace.entries,
                selected = selectedPace,
                label = { pace -> "${pace.name.lowercase().replace('_', ' ')} · ${pace.multiplier}×" },
                onSelected = {
                    selectedPace = it
                    scope.launch { runtime.dispatch(LessonCommand.SetPace(it)) }
                },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Button(onClick = {
                    scope.launch {
                        runtime.start(selectedMode, selectedPace)
                        surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
                    }
                }) { Text("Start") }
                OutlinedButton(onClick = {
                    scope.launch {
                        runtime.newSessionDocument()
                        surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
                    }
                }) { Text("New") }
                OutlinedButton(onClick = {
                    scope.launch {
                        runtime.recover()
                        surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
                    }
                }) { Text("Recover") }
                OutlinedButton(onClick = { scope.launch { runtime.saveNow() } }) { Text("Save") }
                OutlinedButton(onClick = { scope.launch { runtime.dispatch(LessonCommand.SaveAndExit) } }) {
                    Text("Save & Exit")
                }
            }

            SessionCommandRows(runtime, sessionState)
            DrawingToolRow(toolSettings, runtime)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                        Text("playback ${teacherFrame?.status ?: "idle"} · guide ${guideOverlay?.purpose ?: "none"}", fontSize = 10.sp)
                        Text("operations ${diagnostics.childOperationCount} · child ink ${diagnostics.activeChildInkCount}", fontSize = 10.sp)
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
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            LabAction("Pause") { scope.launch { runtime.dispatch(LessonCommand.Pause) } }
            LabAction("Resume") { scope.launch { runtime.dispatch(LessonCommand.Resume) } }
            LabAction("Replay") { scope.launch { runtime.dispatch(ReplayDemonstration) } }
            LabAction("Help +") { scope.launch { runtime.dispatch(RequestHelp) } }
            LabAction("Help −") { scope.launch { runtime.dispatch(ReduceHelp) } }
            LabAction("Dismiss Help") { scope.launch { runtime.dispatch(DismissHelp) } }
            LabAction("Done") { scope.launch { runtime.dispatch(MarkChildTurnDone) } }
            LabAction("Skip Step") { scope.launch { runtime.dispatch(SkipStep) } }
            LabAction("Skip Overview") { scope.launch { runtime.dispatch(SkipOverview) } }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            LabAction("Inject Playback Failure") { scope.launch { runtime.injectTeacherFailure() } }
            LabAction("Retry") { scope.launch { runtime.dispatch(RetryRecoverable) } }
            LabAction("Color With Me") { scope.launch { runtime.dispatch(ChooseColorWithMe) } }
            LabAction("Color Myself") { scope.launch { runtime.dispatch(ChooseColorMyself) } }
            LabAction("Finish Now") { scope.launch { runtime.dispatch(FinishForNow) } }
            LabAction("Coloring Unavailable") { scope.launch { runtime.simulateColoringUnavailable() } }
            LabAction("Handoff ACK") { scope.launch { runtime.simulateColoringContractAck() } }
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LabAction(if (settings.tool == DrawingTool.PENCIL) "✓ Pencil" else "Pencil") {
                runtime.toolEngine.selectTool(DrawingTool.PENCIL)
            }
            LabAction(if (settings.tool == DrawingTool.ERASER) "✓ Eraser" else "Eraser") {
                runtime.toolEngine.selectTool(DrawingTool.ERASER)
            }
            listOf(0xFF202124.toInt(), 0xFF1565C0.toInt(), 0xFFC62828.toInt()).forEachIndexed { index, color ->
                LabAction("Ink ${index + 1}") { runtime.toolEngine.setColor(color) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Width ${settings.width.roundToOne()}", fontSize = 10.sp, color = LabMuted)
            Slider(
                value = settings.width,
                onValueChange = runtime.toolEngine::setWidth,
                valueRange = 1f..24f,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DiagnosticStrip(diagnostics: com.navin.kidsdrawing.lesson.lab.LessonLabDiagnostics) {
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
            "teacher ${diagnostics.activeTeacherRequestId?.takeLast(26) ?: "—"} · guide ${diagnostics.activeGuideId ?: "—"}",
            fontSize = 10.sp,
            color = LabMuted,
        )
        Text(diagnostics.message, fontSize = 10.sp, color = LabInk)
    }
}

@Composable
private fun <T> SelectorRow(
    title: String,
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = LabInk)
        values.forEach { value ->
            if (value == selected) {
                Button(onClick = { onSelected(value) }) { Text(label(value), fontSize = 10.sp) }
            } else {
                OutlinedButton(onClick = { onSelected(value) }) { Text(label(value), fontSize = 10.sp) }
            }
        }
    }
}

@Composable
private fun LabAction(label: String, action: () -> Unit) {
    OutlinedButton(onClick = action) { Text(label, fontSize = 10.sp) }
}

private fun TeachingMode.labLabel(): String = when (this) {
    TeachingMode.DRAW_WITH_ME -> "Draw With Me"
    TeachingMode.WATCH_THEN_DRAW -> "Watch Then Draw"
    TeachingMode.TRACE_AND_LEARN -> "Trace & Learn"
}

private fun Float.roundToOne(): String = "%.1f".format(this)

private val LabPaper = Color(0xFFF7F1E7)
private val LabPanel = Color(0xFFEDE5D7)
private val LabInk = Color(0xFF2C2925)
private val LabMuted = Color(0xFF6D675F)
