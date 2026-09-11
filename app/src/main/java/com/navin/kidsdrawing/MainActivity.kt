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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.navin.kidsdrawing.drawing.demo.ArtLabTeacherDemo
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.DrawingEngineState
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolEngine
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackSession
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackSessionState
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeacherSequenceFactory
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import com.navin.kidsdrawing.drawing.ui.TeacherPlaybackOverlay
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var documentEngine: DrawingDocumentEngine
    private lateinit var toolEngine: DrawingToolEngine
    private lateinit var teacherSession: TeacherPlaybackSession
    private lateinit var documentStore: AtomicDrawingDocumentStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentEngine = DrawingDocumentEngine(
            initialDocument = DrawingDocumentEngine.newDocument(
                documentId = ART_LAB_DOCUMENT_ID,
            ),
        )
        toolEngine = DrawingToolEngine()
        teacherSession = TeacherPlaybackSession().apply {
            load(ArtLabTeacherDemo.sequence())
        }
        documentStore = AtomicDrawingDocumentStore(
            rootDirectory = File(filesDir, "art-lab-documents"),
        )

        setContent {
            ArtLabTheme {
                ArtLabLauncher(
                    documentEngine = documentEngine,
                    toolEngine = toolEngine,
                    teacherSession = teacherSession,
                    documentStore = documentStore,
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!::documentEngine.isInitialized || !::documentStore.isInitialized) return

        lifecycleScope.launch {
            runCatching {
                documentStore.save(documentEngine.state.value.document)
            }
        }
    }
}

@Composable
private fun ArtLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

@Composable
private fun ArtLabLauncher(
    documentEngine: DrawingDocumentEngine,
    toolEngine: DrawingToolEngine,
    teacherSession: TeacherPlaybackSession,
    documentStore: AtomicDrawingDocumentStore,
) {
    var metrics by remember { mutableStateOf(DrawingSurfaceMetrics()) }
    var recoveryComplete by remember { mutableStateOf(false) }
    var persistenceStatus by remember { mutableStateOf("Checking saved artwork…") }
    var autosaveJob by remember { mutableStateOf<Job?>(null) }
    var showDebugMetrics by rememberSaveable { mutableStateOf(false) }
    var lastPlaybackFrameDeltaMillis by remember { mutableStateOf<Long?>(null) }

    val scope = rememberCoroutineScope()
    val surfaceController = remember { DrawingSurfaceController() }
    val documentState by documentEngine.state.collectAsState()
    val toolSettings by toolEngine.state.collectAsState()
    val teacherState by teacherSession.state.collectAsState()
    val teacherFrame = teacherState.frame

    fun queueAutosave(document: DrawingDocument) {
        autosaveJob?.cancel()
        autosaveJob = scope.launch {
            delay(AUTOSAVE_DEBOUNCE_MILLIS)
            runCatching { documentStore.save(document) }
                .onSuccess { persistenceStatus = "Autosaved" }
                .onFailure { persistenceStatus = "Autosave failed · use Save" }
        }
    }

    LaunchedEffect(Unit) {
        val restored = runCatching { documentStore.load(ART_LAB_DOCUMENT_ID) }.getOrNull()
        if (restored != null) {
            documentEngine.replaceDocument(restored.document)
            surfaceController.reconcileDocument(restored.document)
            persistenceStatus = when (restored.source) {
                AtomicDrawingDocumentStore.LoadSource.PRIMARY -> "Restored saved artwork"
                AtomicDrawingDocumentStore.LoadSource.BACKUP -> "Recovered last good backup"
            }
        } else {
            surfaceController.reconcileDocument(documentEngine.state.value.document)
            persistenceStatus = "New Art Lab document"
        }
        recoveryComplete = true
    }

    // Compose contributes frame deltas only. The playback session/engine remains authoritative for
    // virtual source time, pace and lifecycle.
    LaunchedEffect(teacherFrame?.status, teacherState.sequenceId) {
        if (teacherFrame?.status != TeacherPlaybackStatus.PLAYING) return@LaunchedEffect
        var previousFrameNanos = withFrameNanos { it }
        while (true) {
            val frameNanos = withFrameNanos { it }
            val elapsedMillis = ((frameNanos - previousFrameNanos) / 1_000_000L).coerceAtLeast(0L)
            previousFrameNanos = frameNanos
            lastPlaybackFrameDeltaMillis = elapsedMillis
            val updated = teacherSession.advanceBy(elapsedMillis).frame
            if (updated?.status != TeacherPlaybackStatus.PLAYING) break
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Kids Drawing · Art Lab",
                color = Ink900,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "P1.6 engineering console · tools, history, persistence, playback",
                color = Ink700,
                fontSize = 13.sp,
            )

            StatusStrip(persistenceStatus)

            ToolControlPanel(
                settings = toolSettings,
                onToolSelected = toolEngine::selectTool,
                onColorSelected = toolEngine::setColor,
                onWidthChanged = toolEngine::setWidth,
            )

            TeacherControlPanel(
                state = teacherState,
                hasCapturableStroke = documentState.document.activeInkStrokes().isNotEmpty(),
                onLoadDemo = { teacherSession.load(ArtLabTeacherDemo.sequence()) },
                onCaptureLastStroke = {
                    documentState.document.activeInkStrokes().lastOrNull()?.let { stroke ->
                        teacherSession.load(TeacherSequenceFactory.fromChildStroke(stroke))
                    }
                },
                onUnload = teacherSession::unload,
                onPlayPause = {
                    when (teacherSession.state.value.frame?.status) {
                        TeacherPlaybackStatus.PLAYING -> teacherSession.pause()
                        TeacherPlaybackStatus.PAUSED -> teacherSession.resume()
                        else -> teacherSession.play()
                    }
                },
                onReplay = teacherSession::replay,
                onPaceSelected = teacherSession::setPace,
            )

            ActionControlBar(
                recoveryComplete = recoveryComplete,
                documentState = documentState,
                showDebugMetrics = showDebugMetrics,
                onNew = {
                    scope.launch {
                        autosaveJob?.cancelAndJoin()
                        val blank = DrawingDocumentEngine.newDocument(
                            documentId = ART_LAB_DOCUMENT_ID,
                        )
                        documentEngine.replaceDocument(blank)
                        surfaceController.reconcileDocument(blank)
                        teacherSession.cancel()
                        val saved = runCatching { documentStore.save(blank) }.isSuccess
                        persistenceStatus = if (saved) "New blank document saved" else "New document · save failed"
                    }
                },
                onClear = {
                    scope.launch {
                        documentEngine.clear()
                        val document = documentEngine.state.value.document
                        surfaceController.reconcileDocument(document)
                        queueAutosave(document)
                        persistenceStatus = "Clear · undo available"
                    }
                },
                onUndo = {
                    scope.launch {
                        if (documentEngine.undo()) {
                            val document = documentEngine.state.value.document
                            surfaceController.reconcileDocument(document)
                            queueAutosave(document)
                            persistenceStatus = "Undo · autosave queued"
                        }
                    }
                },
                onRedo = {
                    scope.launch {
                        if (documentEngine.redo()) {
                            val document = documentEngine.state.value.document
                            surfaceController.reconcileDocument(document)
                            queueAutosave(document)
                            persistenceStatus = "Redo · autosave queued"
                        }
                    }
                },
                onSave = {
                    scope.launch {
                        autosaveJob?.cancelAndJoin()
                        val result = runCatching {
                            documentStore.save(documentEngine.state.value.document)
                        }
                        persistenceStatus = if (result.isSuccess) "Saved now" else "Save failed"
                    }
                },
                onReload = {
                    scope.launch {
                        autosaveJob?.cancelAndJoin()
                        val restored = runCatching {
                            documentStore.load(ART_LAB_DOCUMENT_ID)
                        }.getOrNull()
                        if (restored == null) {
                            persistenceStatus = "No valid saved artwork found"
                        } else {
                            documentEngine.replaceDocument(restored.document)
                            surfaceController.reconcileDocument(restored.document)
                            persistenceStatus = when (restored.source) {
                                AtomicDrawingDocumentStore.LoadSource.PRIMARY -> "Reloaded saved artwork"
                                AtomicDrawingDocumentStore.LoadSource.BACKUP -> "Reloaded backup artwork"
                            }
                        }
                    }
                },
                onToggleDebug = { showDebugMetrics = !showDebugMetrics },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (recoveryComplete) {
                    DrawingSurface(
                        modifier = Modifier.fillMaxSize(),
                        controller = surfaceController,
                        toolSettings = toolSettings,
                        onStrokeCommitted = { stroke ->
                            scope.launch {
                                documentEngine.commitChildStroke(stroke)
                                queueAutosave(documentEngine.state.value.document)
                            }
                        },
                        onEraseMaskCommitted = { mask ->
                            scope.launch {
                                documentEngine.commitEraseMask(mask)
                                val document = documentEngine.state.value.document
                                surfaceController.reconcileDocument(document)
                                queueAutosave(document)
                            }
                        },
                        onMetricsChanged = { metrics = it },
                    )
                    TeacherPlaybackOverlay(
                        strokes = teacherFrame?.visibleStrokes.orEmpty(),
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (showDebugMetrics) {
                        DebugMetricsOverlay(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            documentState = documentState,
                            toolSettings = toolSettings,
                            surfaceMetrics = metrics,
                            teacherState = teacherState,
                            persistenceStatus = persistenceStatus,
                            playbackFrameDeltaMillis = lastPlaybackFrameDeltaMillis,
                        )
                    }
                } else {
                    Text(
                        text = "Restoring Art Lab…",
                        color = Ink700,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolControlPanel(
    settings: DrawingToolSettings,
    onToolSelected: (DrawingTool) -> Unit,
    onColorSelected: (Int) -> Unit,
    onWidthChanged: (Float) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionButton(
                    selected = settings.tool == DrawingTool.PENCIL,
                    label = "Pencil",
                    onClick = { onToolSelected(DrawingTool.PENCIL) },
                )
                SelectionButton(
                    selected = settings.tool == DrawingTool.ERASER,
                    label = "Eraser",
                    onClick = { onToolSelected(DrawingTool.ERASER) },
                )
                Text(
                    text = "Width ${settings.width.roundToInt()}",
                    color = Ink700,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Slider(
                    value = settings.width,
                    onValueChange = onWidthChanged,
                    valueRange = DrawingToolSettings.MIN_TOOL_WIDTH..DrawingToolSettings.MAX_TOOL_WIDTH,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ART_LAB_COLORS.forEach { option ->
                    SelectionButton(
                        selected = option.argb == settings.colorArgb,
                        label = option.label,
                        onClick = { onColorSelected(option.argb) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherControlPanel(
    state: TeacherPlaybackSessionState,
    hasCapturableStroke: Boolean,
    onLoadDemo: () -> Unit,
    onCaptureLastStroke: () -> Unit,
    onUnload: () -> Unit,
    onPlayPause: () -> Unit,
    onReplay: () -> Unit,
    onPaceSelected: (TeachingPace) -> Unit,
) {
    val frame = state.frame
    val status = frame?.status ?: TeacherPlaybackStatus.IDLE
    val progress = frame?.progress ?: 0f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Studio100,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Teacher: ${state.sequenceId ?: "none"} · ${status.name.lowercase()} · ${(progress * 100).toInt()}%",
                    color = Ink700,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                OutlinedButton(onClick = onLoadDemo) { Text("Load demo", maxLines = 1) }
                OutlinedButton(
                    enabled = hasCapturableStroke,
                    onClick = onCaptureLastStroke,
                ) { Text("Use last stroke", maxLines = 1) }
                OutlinedButton(
                    enabled = state.isLoaded,
                    onClick = onUnload,
                ) { Text("Unload", maxLines = 1) }
                OutlinedButton(
                    enabled = state.isLoaded,
                    onClick = onReplay,
                ) { Text("Replay", maxLines = 1) }
                Button(
                    enabled = state.isLoaded,
                    onClick = onPlayPause,
                ) {
                    Text(
                        text = if (status == TeacherPlaybackStatus.PLAYING) "Pause" else "Play",
                        maxLines = 1,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TeachingPace.entries.forEach { option ->
                    SelectionButton(
                        selected = option == state.selectedPace,
                        label = paceLabel(option),
                        onClick = { onPaceSelected(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionControlBar(
    recoveryComplete: Boolean,
    documentState: DrawingEngineState,
    showDebugMetrics: Boolean,
    onNew: () -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onReload: () -> Unit,
    onToggleDebug: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedButton(enabled = recoveryComplete, onClick = onNew) { Text("New", maxLines = 1) }
        OutlinedButton(
            enabled = recoveryComplete && documentState.document.activeOperations().isNotEmpty(),
            onClick = onClear,
        ) { Text("Clear", maxLines = 1) }
        OutlinedButton(
            enabled = recoveryComplete && documentState.canUndo,
            onClick = onUndo,
        ) { Text("Undo", maxLines = 1) }
        OutlinedButton(
            enabled = recoveryComplete && documentState.canRedo,
            onClick = onRedo,
        ) { Text("Redo", maxLines = 1) }
        Button(enabled = recoveryComplete, onClick = onSave) { Text("Save", maxLines = 1) }
        Button(enabled = recoveryComplete, onClick = onReload) { Text("Reload", maxLines = 1) }
        SelectionButton(
            selected = showDebugMetrics,
            label = "Debug",
            onClick = onToggleDebug,
        )
    }
}

@Composable
private fun SelectionButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(onClick = onClick) { Text(label, maxLines = 1) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label, maxLines = 1) }
    }
}

@Composable
private fun DebugMetricsOverlay(
    modifier: Modifier,
    documentState: DrawingEngineState,
    toolSettings: DrawingToolSettings,
    surfaceMetrics: DrawingSurfaceMetrics,
    teacherState: TeacherPlaybackSessionState,
    persistenceStatus: String,
    playbackFrameDeltaMillis: Long?,
) {
    val document = documentState.document
    val activeOperations = document.activeOperations()
    val activeInk = activeOperations.count { it is DocumentOperation.AddInkStroke }
    val activeErase = activeOperations.count { it is DocumentOperation.AddEraseMask }
    val frame = teacherState.frame

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xE6242321),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            DebugLine("tool", "${toolSettings.tool} · ${argbHex(toolSettings.colorArgb)} · ${toolSettings.width.roundToInt()}")
            DebugLine("document", "ops=${document.operations.size} active ink=$activeInk erase=$activeErase")
            DebugLine("history", "cursor=${documentState.historyCursor} depth=${documentState.historyDepth} redo=${documentState.redoDepth}")
            DebugLine("surface", "ink=${surfaceMetrics.committedStrokeCount} erase=${surfaceMetrics.committedEraseMaskCount} samples=${surfaceMetrics.lastSampleCount}")
            DebugLine("input", "${surfaceMetrics.activeTool ?: "idle"} pressure=${surfaceMetrics.lastPressure?.let { "%.2f".format(it) } ?: "—"}")
            DebugLine(
                "viewport",
                "${surfaceMetrics.viewportWidthPx}×${surfaceMetrics.viewportHeightPx} scale=${surfaceMetrics.documentToViewportScale?.let { "%.3f".format(it) } ?: "—"}",
            )
            DebugLine(
                "offset",
                "x=${surfaceMetrics.documentOffsetXPx?.let { "%.1f".format(it) } ?: "—"} y=${surfaceMetrics.documentOffsetYPx?.let { "%.1f".format(it) } ?: "—"}",
            )
            DebugLine(
                "teacher",
                "${frame?.status ?: "none"} ${paceLabel(teacherState.selectedPace)} t=${frame?.sourceTimeMillis?.toLong() ?: 0}/${frame?.sourceDurationMillis ?: 0}ms",
            )
            DebugLine(
                "timing",
                "handoff=${surfaceMetrics.lastCommitLatencyMillis?.let { "${it}ms" } ?: "—"} frame=${playbackFrameDeltaMillis?.let { "${it}ms" } ?: "—"}",
            )
            DebugLine("store", persistenceStatus)
        }
    }
}

@Composable
private fun DebugLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        color = Color.White,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun StatusStrip(persistenceStatus: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatusChip("Ink 1.0")
        StatusChip("1000 × 1000")
        StatusChip("Offline")
        StatusChip(persistenceStatus)
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Studio100,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = Studio600,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun paceLabel(pace: TeachingPace): String = when (pace) {
    TeachingPace.EXTRA_SLOW -> "0.4×"
    TeachingPace.SLOW -> "0.7×"
    TeachingPace.NORMAL -> "1×"
    TeachingPace.FAST -> "1.5×"
    TeachingPace.VERY_FAST -> "2×"
}

private fun argbHex(argb: Int): String = "#%08X".format(argb)

private data class ArtLabColorOption(
    val label: String,
    val argb: Int,
)

private val ART_LAB_COLORS = listOf(
    ArtLabColorOption("Black", 0xFF242321.toInt()),
    ArtLabColorOption("Blue", 0xFF1565C0.toInt()),
    ArtLabColorOption("Red", 0xFFD84343.toInt()),
    ArtLabColorOption("Green", 0xFF2E7D32.toInt()),
    ArtLabColorOption("Purple", 0xFF7B4DBB.toInt()),
    ArtLabColorOption("Orange", 0xFFE8752C.toInt()),
)

private const val ART_LAB_DOCUMENT_ID = "art-lab-session"
private const val AUTOSAVE_DEBOUNCE_MILLIS = 650L

private val Paper50 = Color(0xFFFFFDF8)
private val Ink900 = Color(0xFF242321)
private val Ink700 = Color(0xFF4E4A45)
private val Studio600 = Color(0xFF5C6F52)
private val Studio100 = Color(0xFFEAF0E5)
