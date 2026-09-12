from pathlib import Path


def replace_exact(path_str: str, old: str, new: str) -> None:
    path = Path(path_str)
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one match in {path_str}, found {count}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")


replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/drawing/infrastructure/InkDrawingSurfaceView.kt",
    "rasterCache.appendLiveInk(stroke)",
    "rasterCache.appendLiveInk(strokeId, stroke)",
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/MainActivity.kt",
    '''                        onStrokeCommitted = { stroke ->
                            scope.launch {
                                documentEngine.commitChildStroke(stroke)
                                queueAutosave(documentEngine.state.value.document)
                            }
                        },''',
    '''                        onStrokeCommitted = { stroke ->
                            scope.launch {
                                documentEngine.commitChildStroke(stroke)
                                val document = documentEngine.state.value.document
                                surfaceController.reconcileDocument(document)
                                queueAutosave(document)
                            }
                        },''',
)

replace_exact(
    "app/src/main/java/com/navin/kidsdrawing/QualityLabActivity.kt",
    '''                    onStrokeCommitted = { stroke ->
                        scope.launch { documentEngine.commitChildStroke(stroke) }
                    },''',
    '''                    onStrokeCommitted = { stroke ->
                        scope.launch {
                            documentEngine.commitChildStroke(stroke)
                            controller.reconcileDocument(documentEngine.state.value.document)
                        }
                    },''',
)
