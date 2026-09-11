package com.navin.kidsdrawing

import android.app.ActivityManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Display
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.PerformanceMetricsState
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolEngine
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.drawing.quality.ArtLabQualityWorkloadFactory
import com.navin.kidsdrawing.drawing.quality.FramePerformanceMonitor
import com.navin.kidsdrawing.drawing.quality.FramePerformanceSnapshot
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Internal physical-device quality harness. Not a production child-facing screen. */
class QualityLabActivity : ComponentActivity() {
    private val framePerformanceMonitor = FramePerformanceMonitor()
    private lateinit var jankStats: JankStats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val documentEngine = DrawingDocumentEngine(
            initialDocument = DrawingDocumentEngine.newDocument(documentId = QUALITY_BLANK_DOCUMENT_ID),
        )
        val toolEngine = DrawingToolEngine()
        val store = AtomicDrawingDocumentStore(
            rootDirectory = File(filesDir, "quality-lab-documents"),
        )

        setContent {
            MaterialTheme {
                QualityLabScreen(
                    documentEngine = documentEngine,
                    toolEngine = toolEngine,
                    store = store,
                    framePerformanceMonitor = framePerformanceMonitor,
                    deviceSummary = deviceSummary(),
                )
            }
        }

        jankStats = JankStats.createAndTrack(window) { frameData ->
            // FrameData is reused by JankStats, so only primitive values cross this callback.
            framePerformanceMonitor.record(
                frameDurationUiNanos = frameData.frameDurationUiNanos,
                isJank = frameData.isJank,
            )
        }
        PerformanceMetricsState.getHolderForHierarchy(window.decorView)
            .state
            ?.putState("Workspace", "QualityLab")
    }

    override fun onStart() {
        super.onStart()
        if (::jankStats.isInitialized) jankStats.isTrackingEnabled = true
    }

    override fun onStop() {
        if (::jankStats.isInitialized) jankStats.isTrackingEnabled = false
        super.onStop()
    }

    private fun deviceSummary(): String {
        val memoryClass = getSystemService(ActivityManager::class.java)?.memoryClass
        val refreshRate = getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
            ?.refreshRate
        return buildString {
            append(Build.MANUFACTURER)
            append(' ')
            append(Build.MODEL)
            append(" · API ")
            append(Build.VERSION.SDK_INT)
            if (memoryClass != null) append(" · heapClass ${memoryClass}MB")
            if (refreshRate != null) append(" · ${"%.0f".format(refreshRate)}Hz")
        }
    }
}

@Composable
private fun QualityLabScreen(
    documentEngine: DrawingDocumentEngine,
    toolEngine: DrawingToolEngine,
    store: AtomicDrawingDocumentStore,
    framePerformanceMonitor: FramePerformanceMonitor,
    deviceSummary: String,
) {
    val scope = rememberCoroutineScope()
    val controller = remember { DrawingSurfaceController() }
    val documentState by documentEngine.state.collectAsState()
    val toolSettings by toolEngine.state.collectAsState()
    var surfaceMetrics by remember { mutableStateOf(DrawingSurfaceMetrics()) }
    var frameStats by remember { mutableStateOf(framePerformanceMonitor.snapshot()) }
    var workloadStatus by remember { mutableStateOf("Blank manual workload") }
    var busy by remember { mutableStateOf(false) }
    var lastReconcileToFrameMillis by remember { mutableStateOf<Double?>(null) }
    var lastSaveMillis by remember { mutableStateOf<Double?>(null) }
    var lastLoadMillis by remember { mutableStateOf<Double?>(null) }
    var lastHistory100Millis by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            frameStats = framePerformanceMonitor.snapshot()
        }
    }

    fun installDocument(label: String, producer: suspend () -> DrawingDocument) {
        if (busy) return
        scope.launch {
            busy = true
            workloadStatus = "Generating $label…"
            val document = producer()
            documentEngine.replaceDocument(document)
            framePerformanceMonitor.reset()
            frameStats = framePerformanceMonitor.snapshot()
            val started = SystemClock.elapsedRealtimeNanos()
            controller.reconcileDocument(document)
            withFrameNanos { }
            lastReconcileToFrameMillis = elapsedMillisSince(started)
            workloadStatus = "$label loaded · ${document.operations.size} ops"
            busy = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFDF8),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = "Kids Drawing · Quality Lab",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF242321),
            )
            Text(
                text = "P1.7 physical performance harness · measurements are evidence, not automatic PASS",
                fontSize = 11.sp,
                color = Color(0xFF4E4A45),
            )
            Text(deviceSummary, fontSize = 10.sp, color = Color(0xFF4E4A45))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Button(
                    enabled = !busy,
                    onClick = {
                        installDocument("W2 heavy") {
                            withContext(Dispatchers.Default) { ArtLabQualityWorkloadFactory.w2() }
                        }
                    },
                ) { Text("Load W2 · 2k/250k", maxLines = 1) }
                Button(
                    enabled = !busy,
                    onClick = {
                        installDocument("W3 stress") {
                            withContext(Dispatchers.Default) { ArtLabQualityWorkloadFactory.w3() }
                        }
                    },
                ) { Text("Load W3 · 5k", maxLines = 1) }
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        installDocument("Blank") {
                            DrawingDocumentEngine.newDocument(documentId = QUALITY_BLANK_DOCUMENT_ID)
                        }
                    },
                ) { Text("Blank", maxLines = 1) }
                OutlinedButton(
                    onClick = {
                        framePerformanceMonitor.reset()
                        frameStats = framePerformanceMonitor.snapshot()
                    },
                ) { Text("Reset frames", maxLines = 1) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                QualityToggle(
                    selected = toolSettings.tool == DrawingTool.PENCIL,
                    label = "Pencil",
                    onClick = { toolEngine.selectTool(DrawingTool.PENCIL) },
                )
                QualityToggle(
                    selected = toolSettings.tool == DrawingTool.ERASER,
                    label = "Eraser",
                    onClick = { toolEngine.selectTool(DrawingTool.ERASER) },
                )
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            val started = SystemClock.elapsedRealtimeNanos()
                            store.save(documentEngine.state.value.document)
                            lastSaveMillis = elapsedMillisSince(started)
                        }
                    },
                ) { Text("Save", maxLines = 1) }
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            val current = documentEngine.state.value.document
                            val started = SystemClock.elapsedRealtimeNanos()
                            val loaded = store.load(current.documentId)
                            if (loaded != null) {
                                documentEngine.replaceDocument(loaded.document)
                                controller.reconcileDocument(loaded.document)
                                withFrameNanos { }
                            }
                            lastLoadMillis = elapsedMillisSince(started)
                        }
                    },
                ) { Text("Reload", maxLines = 1) }
                OutlinedButton(
                    enabled = documentState.canUndo && !busy,
                    onClick = {
                        scope.launch {
                            val started = SystemClock.elapsedRealtimeNanos()
                            repeat(100) { if (!documentEngine.undo()) return@repeat }
                            controller.reconcileDocument(documentEngine.state.value.document)
                            withFrameNanos { }
                            lastHistory100Millis = elapsedMillisSince(started)
                        }
                    },
                ) { Text("Undo 100", maxLines = 1) }
                OutlinedButton(
                    enabled = documentState.canRedo && !busy,
                    onClick = {
                        scope.launch {
                            val started = SystemClock.elapsedRealtimeNanos()
                            repeat(100) { if (!documentEngine.redo()) return@repeat }
                            controller.reconcileDocument(documentEngine.state.value.document)
                            withFrameNanos { }
                            lastHistory100Millis = elapsedMillisSince(started)
                        }
                    },
                ) { Text("Redo 100", maxLines = 1) }
            }

            QualityMetricsCard(
                workloadStatus = workloadStatus,
                operationCount = documentState.document.operations.size,
                activeInk = documentState.document.activeInkStrokes().size,
                surfaceMetrics = surfaceMetrics,
                frameStats = frameStats,
                reconcileMillis = lastReconcileToFrameMillis,
                saveMillis = lastSaveMillis,
                loadMillis = lastLoadMillis,
                history100Millis = lastHistory100Millis,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                DrawingSurface(
                    modifier = Modifier.fillMaxSize(),
                    controller = controller,
                    toolSettings = toolSettings,
                    onStrokeCommitted = { stroke ->
                        scope.launch { documentEngine.commitChildStroke(stroke) }
                    },
                    onEraseMaskCommitted = { mask ->
                        scope.launch {
                            documentEngine.commitEraseMask(mask)
                            controller.reconcileDocument(documentEngine.state.value.document)
                        }
                    },
                    onMetricsChanged = { surfaceMetrics = it },
                )
            }

            Text(
                text = "Class M frame gate: P95 ≤16.7ms · P99 ≤33.4ms · jank ≤3%. Class L: P95 ≤25ms · P99 ≤50ms · jank ≤5%. Classify the physical device before recording PASS/FAIL.",
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = Color(0xFF4E4A45),
            )
        }
    }
}

@Composable
private fun QualityMetricsCard(
    workloadStatus: String,
    operationCount: Int,
    activeInk: Int,
    surfaceMetrics: DrawingSurfaceMetrics,
    frameStats: FramePerformanceSnapshot,
    reconcileMillis: Double?,
    saveMillis: Double?,
    loadMillis: Double?,
    history100Millis: Double?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFEAF0E5),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            QualityLine("workload", "$workloadStatus · ops=$operationCount ink=$activeInk")
            QualityLine(
                "frames",
                "n=${frameStats.frameCount} jank=${frameStats.jankFrameCount} (${format(frameStats.jankRatePercent)}%) p95=${frameStats.p95UiMillis ?: "—"}ms p99=${frameStats.p99UiMillis ?: "—"}ms max=${frameStats.maxUiMillis?.let(::format) ?: "—"}ms",
            )
            QualityLine(
                "surface",
                "commit=${surfaceMetrics.lastCommitLatencyMillis?.let { "${it}ms" } ?: "—"} samples=${surfaceMetrics.lastSampleCount} pressure=${surfaceMetrics.lastPressure?.let { format(it.toDouble()) } ?: "—"}",
            )
            QualityLine(
                "operations",
                "reconcile→frame=${reconcileMillis?.let(::format) ?: "—"}ms save=${saveMillis?.let(::format) ?: "—"}ms load→frame=${loadMillis?.let(::format) ?: "—"}ms undo/redo100=${history100Millis?.let(::format) ?: "—"}ms",
            )
        }
    }
}

@Composable
private fun QualityLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        fontSize = 10.sp,
        lineHeight = 12.sp,
        color = Color(0xFF242321),
        maxLines = 1,
    )
}

@Composable
private fun QualityToggle(selected: Boolean, label: String, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label, maxLines = 1) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label, maxLines = 1) }
    }
}

private fun elapsedMillisSince(startedNanos: Long): Double =
    (SystemClock.elapsedRealtimeNanos() - startedNanos) / 1_000_000.0

private fun format(value: Double): String = "%.1f".format(value)

private const val QUALITY_BLANK_DOCUMENT_ID = "quality-lab-manual"
