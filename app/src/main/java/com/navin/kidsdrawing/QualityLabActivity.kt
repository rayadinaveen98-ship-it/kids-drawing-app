package com.navin.kidsdrawing

import android.app.ActivityManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.SystemClock
import android.view.Display
import android.view.MotionEvent
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
import com.navin.kidsdrawing.drawing.quality.DurationPerformanceMonitor
import com.navin.kidsdrawing.drawing.quality.DurationPerformanceSnapshot
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
    private val inputDispatchMonitor = DurationPerformanceMonitor()
    @Volatile private var inputMeasurementArmed = false
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
                    inputDispatchMonitor = inputDispatchMonitor,
                    deviceSummary = deviceSummary(),
                    onResetMeasurements = ::resetMeasurementsAfterCurrentTouch,
                    onWorkloadChanged = ::setPerformanceWorkloadState,
                )
            }
        }

        jankStats = JankStats.createAndTrack(window) { frameData ->
            framePerformanceMonitor.record(
                frameDurationUiNanos = frameData.frameDurationUiNanos,
                isJank = frameData.isJank,
            )
        }
        PerformanceMetricsState.getHolderForHierarchy(window.decorView)
            .state
            ?.apply {
                putState("Workspace", "QualityLab")
                putState("Workload", "Blank")
            }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!inputMeasurementArmed) return super.dispatchTouchEvent(event)
        val started = SystemClock.elapsedRealtimeNanos()
        return try {
            super.dispatchTouchEvent(event)
        } finally {
            inputDispatchMonitor.record(SystemClock.elapsedRealtimeNanos() - started)
        }
    }

    override fun onStart() {
        super.onStart()
        if (::jankStats.isInitialized) jankStats.isTrackingEnabled = true
    }

    override fun onStop() {
        if (::jankStats.isInitialized) jankStats.isTrackingEnabled = false
        super.onStop()
    }

    private fun resetMeasurementsAfterCurrentTouch() {
        inputMeasurementArmed = false
        window.decorView.post {
            framePerformanceMonitor.reset()
            inputDispatchMonitor.reset()
            inputMeasurementArmed = true
        }
    }

    private fun setPerformanceWorkloadState(value: String) {
        if (!::jankStats.isInitialized) return
        PerformanceMetricsState.getHolderForHierarchy(window.decorView)
            .state
            ?.putState("Workload", value)
    }

    private fun deviceSummary(): String {
        val activityManager = getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val totalRamMb = if (memoryInfo.totalMem > 0L) memoryInfo.totalMem / BYTES_PER_MIB else null
        val memoryClass = activityManager?.memoryClass
        val refreshRate = getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
            ?.refreshRate
        return buildString {
            append(Build.MANUFACTURER)
            append(' ')
            append(Build.MODEL)
            append(" · API ")
            append(Build.VERSION.SDK_INT)
            if (totalRamMb != null) append(" · RAM ${totalRamMb}MB")
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
    inputDispatchMonitor: DurationPerformanceMonitor,
    deviceSummary: String,
    onResetMeasurements: () -> Unit,
    onWorkloadChanged: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember { DrawingSurfaceController() }
    val documentState by documentEngine.state.collectAsState()
    val toolSettings by toolEngine.state.collectAsState()
    val saveMonitor = remember { DurationPerformanceMonitor() }
    val loadMonitor = remember { DurationPerformanceMonitor() }
    val historyMonitor = remember { DurationPerformanceMonitor() }
    var surfaceMetrics by remember { mutableStateOf(DrawingSurfaceMetrics()) }
    var frameStats by remember { mutableStateOf(framePerformanceMonitor.snapshot()) }
    var inputStats by remember { mutableStateOf(inputDispatchMonitor.snapshot()) }
    var saveStats by remember { mutableStateOf(saveMonitor.snapshot()) }
    var loadStats by remember { mutableStateOf(loadMonitor.snapshot()) }
    var historyStats by remember { mutableStateOf(historyMonitor.snapshot()) }
    var memoryStats by remember { mutableStateOf(currentMemorySnapshot()) }
    var workloadStatus by remember { mutableStateOf("Blank manual workload") }
    var busy by remember { mutableStateOf(false) }
    var lastReconcileToFrameMillis by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            frameStats = framePerformanceMonitor.snapshot()
            inputStats = inputDispatchMonitor.snapshot()
            saveStats = saveMonitor.snapshot()
            loadStats = loadMonitor.snapshot()
            historyStats = historyMonitor.snapshot()
            memoryStats = currentMemorySnapshot()
        }
    }

    fun installDocument(label: String, stateLabel: String, producer: suspend () -> DrawingDocument) {
        if (busy) return
        scope.launch {
            busy = true
            workloadStatus = "Generating $label…"
            val document = producer()
            documentEngine.replaceDocument(document)
            onWorkloadChanged(stateLabel)
            val started = SystemClock.elapsedRealtimeNanos()
            controller.reconcileDocument(document)
            withFrameNanos { }
            lastReconcileToFrameMillis = elapsedMillisSince(started)
            workloadStatus = "$label loaded · ${document.operations.size} ops · measurements reset · draw now"
            busy = false

            // Let the load/status/button-state frames settle, then start a clean interaction window.
            withFrameNanos { }
            withFrameNanos { }
            onResetMeasurements()
        }
    }

    fun runSave20() {
        if (busy) return
        scope.launch {
            busy = true
            workloadStatus = "Save×20 benchmark running…"
            saveMonitor.reset()
            val current = documentEngine.state.value.document
            val benchmarkId = "${current.documentId}-save-benchmark"
            val baseTime = System.currentTimeMillis().coerceAtLeast(current.createdAtEpochMillis + 1L)
            repeat(BENCHMARK_SAMPLE_COUNT) { index ->
                val candidate = current.copy(
                    documentId = benchmarkId,
                    modifiedAtEpochMillis = baseTime + index,
                )
                val started = SystemClock.elapsedRealtimeNanos()
                store.save(candidate)
                saveMonitor.record(SystemClock.elapsedRealtimeNanos() - started)
            }
            saveStats = saveMonitor.snapshot()
            workloadStatus = "Save×20 complete"
            busy = false
        }
    }

    fun runLoad20() {
        if (busy) return
        scope.launch {
            busy = true
            workloadStatus = "Load→render×20 benchmark running…"
            loadMonitor.reset()
            val current = documentEngine.state.value.document
            val benchmarkId = "${current.documentId}-load-benchmark"
            val benchmarkDocument = current.copy(
                documentId = benchmarkId,
                modifiedAtEpochMillis = System.currentTimeMillis().coerceAtLeast(current.createdAtEpochMillis + 1L),
            )
            store.save(benchmarkDocument)
            repeat(BENCHMARK_SAMPLE_COUNT) {
                val started = SystemClock.elapsedRealtimeNanos()
                val loaded = checkNotNull(store.load(benchmarkId))
                documentEngine.replaceDocument(loaded.document)
                controller.reconcileDocument(loaded.document)
                withFrameNanos { }
                loadMonitor.record(SystemClock.elapsedRealtimeNanos() - started)
            }
            loadStats = loadMonitor.snapshot()
            workloadStatus = "Load→render×20 complete"
            busy = false
        }
    }

    fun runHistory20() {
        if (busy || documentEngine.state.value.document.operations.size < BENCHMARK_SAMPLE_COUNT) return
        scope.launch {
            busy = true
            workloadStatus = "Visible Undo/Redo×20 benchmark running…"
            historyMonitor.reset()
            repeat(BENCHMARK_SAMPLE_COUNT) {
                val undoStarted = SystemClock.elapsedRealtimeNanos()
                check(documentEngine.undo())
                controller.reconcileDocument(documentEngine.state.value.document)
                withFrameNanos { }
                historyMonitor.record(SystemClock.elapsedRealtimeNanos() - undoStarted)

                val redoStarted = SystemClock.elapsedRealtimeNanos()
                check(documentEngine.redo())
                controller.reconcileDocument(documentEngine.state.value.document)
                withFrameNanos { }
                historyMonitor.record(SystemClock.elapsedRealtimeNanos() - redoStarted)
            }
            historyStats = historyMonitor.snapshot()
            workloadStatus = "Visible Undo/Redo×20 complete"
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
                        installDocument("W1 normal", "W1") {
                            withContext(Dispatchers.Default) { ArtLabQualityWorkloadFactory.w1() }
                        }
                    },
                ) { Text("Load W1 · 500", maxLines = 1) }
                Button(
                    enabled = !busy,
                    onClick = {
                        installDocument("W2 heavy", "W2") {
                            withContext(Dispatchers.Default) { ArtLabQualityWorkloadFactory.w2() }
                        }
                    },
                ) { Text("Load W2 · 2k/250k", maxLines = 1) }
                Button(
                    enabled = !busy,
                    onClick = {
                        installDocument("W3 stress", "W3") {
                            withContext(Dispatchers.Default) { ArtLabQualityWorkloadFactory.w3() }
                        }
                    },
                ) { Text("Load W3 · 5k", maxLines = 1) }
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        installDocument("Blank", "Blank") {
                            DrawingDocumentEngine.newDocument(documentId = QUALITY_BLANK_DOCUMENT_ID)
                        }
                    },
                ) { Text("Blank", maxLines = 1) }
                OutlinedButton(onClick = onResetMeasurements) {
                    Text("Reset measurements", maxLines = 1)
                }
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
                OutlinedButton(enabled = !busy, onClick = ::runSave20) {
                    Text("Save×20", maxLines = 1)
                }
                OutlinedButton(enabled = !busy, onClick = ::runLoad20) {
                    Text("Load×20", maxLines = 1)
                }
                OutlinedButton(
                    enabled = !busy && documentState.document.operations.size >= BENCHMARK_SAMPLE_COUNT,
                    onClick = ::runHistory20,
                ) { Text("Undo/Redo×20", maxLines = 1) }
            }

            QualityMetricsCard(
                workloadStatus = workloadStatus,
                operationCount = documentState.document.operations.size,
                activeInk = documentState.document.activeInkStrokes().size,
                surfaceMetrics = surfaceMetrics,
                frameStats = frameStats,
                inputStats = inputStats,
                saveStats = saveStats,
                loadStats = loadStats,
                historyStats = historyStats,
                memoryStats = memoryStats,
                reconcileMillis = lastReconcileToFrameMillis,
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
                text = "W1 frame gate: Load W1; measurements auto-reset after load. Draw continuously for ~30s without controls. Use Reset measurements to repeat the window.",
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = Color(0xFF4E4A45),
            )
            Text(
                text = "Input proxy is window-dispatch timing: a conservative upper bound, not the narrower Ink-only CPU trace.",
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = Color(0xFF4E4A45),
            )
            Text(
                text = "Class M: W1 frame P95 ≤16.7ms · P99 ≤33.4ms · jank ≤3%; input P95 ≤4ms · P99 ≤8ms; W2 save P95 ≤1000ms; load P95 ≤1500ms; undo/redo P95 ≤50ms.",
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
    inputStats: DurationPerformanceSnapshot,
    saveStats: DurationPerformanceSnapshot,
    loadStats: DurationPerformanceSnapshot,
    historyStats: DurationPerformanceSnapshot,
    memoryStats: QualityMemorySnapshot,
    reconcileMillis: Double?,
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
                "input upper",
                "n=${inputStats.sampleCount} p95=${inputStats.p95Millis ?: "—"}ms p99=${inputStats.p99Millis ?: "—"}ms max=${inputStats.maxMillis?.let(::format) ?: "—"}ms",
            )
            QualityLine(
                "W2 bench",
                "save20 p95=${saveStats.p95Millis ?: "—"} p99=${saveStats.p99Millis ?: "—"}ms · load20 p95=${loadStats.p95Millis ?: "—"} p99=${loadStats.p99Millis ?: "—"}ms",
            )
            QualityLine(
                "history",
                "n=${historyStats.sampleCount} p95=${historyStats.p95Millis ?: "—"} p99=${historyStats.p99Millis ?: "—"}ms",
            )
            QualityLine(
                "surface",
                "commit=${surfaceMetrics.lastCommitLatencyMillis?.let { "${it}ms" } ?: "—"} samples=${surfaceMetrics.lastSampleCount} reconcile→frame=${reconcileMillis?.let(::format) ?: "—"}ms",
            )
            QualityLine(
                "memory",
                "java=${format(memoryStats.javaUsedMiB)}MiB native=${format(memoryStats.nativeAllocatedMiB)}MiB",
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

private data class QualityMemorySnapshot(
    val javaUsedMiB: Double,
    val nativeAllocatedMiB: Double,
)

private fun currentMemorySnapshot(): QualityMemorySnapshot {
    val runtime = Runtime.getRuntime()
    return QualityMemorySnapshot(
        javaUsedMiB = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MIB.toDouble(),
        nativeAllocatedMiB = Debug.getNativeHeapAllocatedSize() / BYTES_PER_MIB.toDouble(),
    )
}

private fun elapsedMillisSince(startedNanos: Long): Double =
    (SystemClock.elapsedRealtimeNanos() - startedNanos) / 1_000_000.0

private fun format(value: Double): String = "%.1f".format(value)

private const val QUALITY_BLANK_DOCUMENT_ID = "quality-lab-manual"
private const val BENCHMARK_SAMPLE_COUNT = 20
private const val BYTES_PER_MIB = 1024L * 1024L
