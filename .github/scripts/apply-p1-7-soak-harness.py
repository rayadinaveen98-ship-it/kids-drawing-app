from pathlib import Path


def replace_exact(path_str: str, old: str, new: str) -> None:
    path = Path(path_str)
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one match in {path_str}, found {count}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")


replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''import android.view.MotionEvent\n''',
    '''import android.view.MotionEvent\nimport android.view.WindowManager\n''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''        super.onCreate(savedInstanceState)\n\n        val documentEngine = DrawingDocumentEngine(''',
    '''        super.onCreate(savedInstanceState)\n        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)\n\n        val documentEngine = DrawingDocumentEngine(''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''    Surface(\n        modifier = Modifier.fillMaxSize(),''',
    '''    fun runSoak30m() {\n        if (busy) return\n        scope.launch {\n            busy = true\n            onWorkloadChanged("Soak30m")\n            workloadStatus = "SOAK preparing W2…"\n\n            val startedAt = SystemClock.elapsedRealtime()\n            val deadline = startedAt + SOAK_DURATION_MILLIS\n            val initialMemory = currentMemorySnapshot()\n            var peakJavaMiB = initialMemory.javaUsedMiB\n            var peakNativeMiB = initialMemory.nativeAllocatedMiB\n            var cycles = 0\n            var failures = 0\n\n            try {\n                val generated = withContext(Dispatchers.Default) { ArtLabQualityWorkloadFactory.w2() }\n                val soakDocument = generated.copy(documentId = QUALITY_SOAK_DOCUMENT_ID)\n                documentEngine.replaceDocument(soakDocument)\n                controller.reconcileDocument(soakDocument)\n                withFrameNanos { }\n                store.save(soakDocument)\n\n                while (SystemClock.elapsedRealtime() < deadline && failures == 0) {\n                    when (cycles % 4) {\n                        0 -> {\n                            check(documentEngine.undo())\n                            controller.reconcileDocument(documentEngine.state.value.document)\n                            withFrameNanos { }\n                            check(documentEngine.redo())\n                            controller.reconcileDocument(documentEngine.state.value.document)\n                            withFrameNanos { }\n                        }\n                        1 -> {\n                            store.save(documentEngine.state.value.document)\n                        }\n                        2 -> {\n                            val loaded = checkNotNull(store.load(QUALITY_SOAK_DOCUMENT_ID))\n                            documentEngine.replaceDocument(loaded.document)\n                            controller.reconcileDocument(loaded.document)\n                            withFrameNanos { }\n                        }\n                        else -> {\n                            repeat(4) {\n                                controller.invalidateCommittedProjectionForBenchmark()\n                                withFrameNanos { }\n                            }\n                        }\n                    }\n\n                    cycles++\n                    val memory = currentMemorySnapshot()\n                    peakJavaMiB = maxOf(peakJavaMiB, memory.javaUsedMiB)\n                    peakNativeMiB = maxOf(peakNativeMiB, memory.nativeAllocatedMiB)\n                    val elapsedMinutes = (SystemClock.elapsedRealtime() - startedAt) / 60_000L\n                    workloadStatus =\n                        "SOAK running ${elapsedMinutes}m/30m · cycles=$cycles · failures=$failures"\n                    delay(SOAK_CYCLE_DELAY_MILLIS)\n                }\n\n                val current = documentEngine.state.value.document\n                store.save(current)\n                val finalLoad = checkNotNull(store.load(QUALITY_SOAK_DOCUMENT_ID))\n                check(finalLoad.document.operations == current.operations) {\n                    "final persisted operation timeline mismatch"\n                }\n                documentEngine.replaceDocument(finalLoad.document)\n                controller.reconcileDocument(finalLoad.document)\n                withFrameNanos { }\n\n                val endMemory = currentMemorySnapshot()\n                workloadStatus =\n                    "SOAK PASS · 30m · cycles=$cycles · failures=0 · java ${format(initialMemory.javaUsedMiB)}→${format(endMemory.javaUsedMiB)}MiB peak=${format(peakJavaMiB)} · native ${format(initialMemory.nativeAllocatedMiB)}→${format(endMemory.nativeAllocatedMiB)}MiB peak=${format(peakNativeMiB)}"\n            } catch (t: Throwable) {\n                failures++\n                workloadStatus =\n                    "SOAK FAIL · cycle=$cycles · ${t::class.java.simpleName}: ${t.message ?: "no message"}"\n            } finally {\n                memoryStats = currentMemorySnapshot()\n                busy = false\n            }\n        }\n    }\n\n    Surface(\n        modifier = Modifier.fillMaxSize(),''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''                OutlinedButton(\n                    enabled = !busy && documentState.document.operations.size >= W1_MIN_OPERATION_COUNT,\n                    onClick = ::runW1Frame600,\n                ) {\n                    Text("W1 Frame×600", maxLines = 1)\n                }\n                OutlinedButton(enabled = !busy, onClick = ::runSave20) {''',
    '''                OutlinedButton(\n                    enabled = !busy && documentState.document.operations.size >= W1_MIN_OPERATION_COUNT,\n                    onClick = ::runW1Frame600,\n                ) {\n                    Text("W1 Frame×600", maxLines = 1)\n                }\n                OutlinedButton(enabled = !busy, onClick = ::runSoak30m) {\n                    Text("Soak 30m", maxLines = 1)\n                }\n                OutlinedButton(enabled = !busy, onClick = ::runSave20) {''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''            Text(\n                text = "Input proxy is window-dispatch timing: a conservative upper bound, not the narrower Ink-only CPU trace.",''',
    '''            Text(\n                text = "Soak 30m: leave Quality Lab in the foreground and connected to power if convenient. The harness cycles history, save/load, renderer frames and memory checks automatically; only an explicit SOAK PASS closes the reliability gate.",\n                fontSize = 10.sp,\n                lineHeight = 13.sp,\n                color = Color(0xFF4E4A45),\n            )\n            Text(\n                text = "Input proxy is window-dispatch timing: a conservative upper bound, not the narrower Ink-only CPU trace.",''',
)

quality_path = Path("app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt")
quality_text = quality_path.read_text(encoding="utf-8")
anchor = 'private const val W1_MIN_OPERATION_COUNT = 500'
if anchor not in quality_text:
    raise SystemExit("Could not find W1 constants anchor")
quality_text = quality_text.replace(
    anchor,
    '''private const val W1_MIN_OPERATION_COUNT = 500\nprivate const val QUALITY_SOAK_DOCUMENT_ID = "quality-lab-soak"\nprivate const val SOAK_DURATION_MILLIS = 30L * 60L * 1_000L\nprivate const val SOAK_CYCLE_DELAY_MILLIS = 250L''',
    1,
)
quality_path.write_text(quality_text, encoding="utf-8")

replace_exact(
    "app/build.gradle.kts",
    '''        versionCode = 9\n        versionName = "0.0.9-p1.7-frame-harness-profile-test"''',
    '''        versionCode = 10\n        versionName = "0.0.10-p1.7-soak-profile-test"''',
)
