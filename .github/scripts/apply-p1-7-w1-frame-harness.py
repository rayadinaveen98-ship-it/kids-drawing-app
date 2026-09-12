from pathlib import Path


def replace_exact(path_str: str, old: str, new: str) -> None:
    path = Path(path_str)
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one match in {path_str}, found {count}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")


replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/drawing/ui/DrawingSurface.kt",
    '''    fun reconcileDocument(document: DrawingDocument) {
        pendingDocument = document
        attachedSurface?.reconcileDocument(document)
    }

    internal fun attach(surface: InkDrawingSurfaceView) {''',
    '''    fun reconcileDocument(document: DrawingDocument) {
        pendingDocument = document
        attachedSurface?.reconcileDocument(document)
    }

    /** Internal Quality Lab hook. Never mutates document state. */
    fun invalidateCommittedProjectionForBenchmark() {
        attachedSurface?.invalidateCommittedProjectionForBenchmark()
    }

    internal fun attach(surface: InkDrawingSurfaceView) {''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/drawing/infrastructure/InkDrawingSurfaceView.kt",
    '''    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {''',
    '''    /**
     * Internal Quality Lab hook that forces only the committed projection to participate in a
     * ViewRoot frame. It never mutates the authoritative document or transient Ink input.
     */
    fun invalidateCommittedProjectionForBenchmark() {
        committedInkView.invalidate()
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''    Surface(
        modifier = Modifier.fillMaxSize(),''',
    '''    fun runW1Frame600() {
        if (busy || documentEngine.state.value.document.operations.size < W1_MIN_OPERATION_COUNT) return
        scope.launch {
            busy = true
            workloadStatus = "W1 committed frame×600 preparing…"
            onWorkloadChanged("W1Frame600")
            onResetMeasurements()

            // Let the post-touch reset and button/status frames settle before the measured pulse.
            withFrameNanos { }
            withFrameNanos { }

            repeat(W1_FRAME_PULSE_COUNT) {
                controller.invalidateCommittedProjectionForBenchmark()
                withFrameNanos { }
            }
            withFrameNanos { }

            val snapshot = framePerformanceMonitor.snapshot()
            frameStats = snapshot
            val validity = if (snapshot.frameCount >= W1_MIN_VALID_FRAME_SAMPLES) {
                "sample-valid"
            } else {
                "INVALID-sample<${W1_MIN_VALID_FRAME_SAMPLES}"
            }
            workloadStatus =
                "W1 committed frame×600 complete · n=${snapshot.frameCount} · $validity"
            busy = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''                QualityToggle(
                    selected = toolSettings.tool == DrawingTool.ERASER,
                    label = "Eraser",
                    onClick = { toolEngine.selectTool(DrawingTool.ERASER) },
                )
                OutlinedButton(enabled = !busy, onClick = ::runSave20) {''',
    '''                QualityToggle(
                    selected = toolSettings.tool == DrawingTool.ERASER,
                    label = "Eraser",
                    onClick = { toolEngine.selectTool(DrawingTool.ERASER) },
                )
                OutlinedButton(
                    enabled = !busy && documentState.document.operations.size >= W1_MIN_OPERATION_COUNT,
                    onClick = ::runW1Frame600,
                ) {
                    Text("W1 Frame×600", maxLines = 1)
                }
                OutlinedButton(enabled = !busy, onClick = ::runSave20) {''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''                text = "W1 frame gate: Load W1; measurements auto-reset after load. Draw continuously for ~30s without controls. Use Reset measurements to repeat the window.",''',
    '''                text = "W1 committed-frame gate: Load W1, then tap W1 Frame×600. Do not draw or press controls while it runs. Acceptance requires at least 500 sampled ViewRoot frames; continuous AndroidX Ink drawing remains diagnostic because its wet-stroke path is not fully represented by JankStats.",''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''                text = "Frame card shows native-refresh JankStats and >16.7ms rate separately. On 90/120Hz devices the Phase 1 60Hz-equivalent gate uses >16.7ms plus P95/P99; native jank is recorded too.",''',
    '''                text = "During W1 Frame×600 the frame card measures the committed raster projection under forced ViewRoot draws. The Phase 1 60Hz-equivalent gate still uses >16.7ms plus P95/P99; native-refresh jank is recorded separately.",''',
)

quality_path = Path("app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt")
quality_text = quality_path.read_text(encoding="utf-8")
anchor = 'private const val BENCHMARK_SAMPLE_COUNT = 20'
if anchor not in quality_text:
    raise SystemExit("Could not find BENCHMARK_SAMPLE_COUNT anchor")
quality_text = quality_text.replace(
    anchor,
    '''private const val BENCHMARK_SAMPLE_COUNT = 20
private const val W1_FRAME_PULSE_COUNT = 600
private const val W1_MIN_VALID_FRAME_SAMPLES = 500L
private const val W1_MIN_OPERATION_COUNT = 500''',
    1,
)
quality_path.write_text(quality_text, encoding="utf-8")

replace_exact(
    "app/build.gradle.kts",
    '''        versionCode = 8
        versionName = "0.0.8-p1.7-raster-profile-test"''',
    '''        versionCode = 9
        versionName = "0.0.9-p1.7-frame-harness-profile-test"''',
)
