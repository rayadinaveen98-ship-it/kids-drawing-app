from pathlib import Path

path = Path("app/src/main/java/com/navin/kidsdrawing/drawing/infrastructure/InkDrawingSurfaceView.kt")
text = path.read_text(encoding="utf-8")
start_marker = "    private class CommittedInkView("
end_marker = "\n    private companion object {"
start = text.index(start_marker)
end = text.index(end_marker, start)

replacement = r'''    private class CommittedInkView(
        context: Context,
        private val documentSize: DocumentSize,
    ) : View(context) {
        private val rasterCache = CommittedRasterCache(documentSize)
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        var viewportTransform: DocumentViewportMapper.Transform? = null

        fun addInkStroke(strokeId: String, stroke: Stroke) {
            require(strokeId.isNotBlank())
            rasterCache.appendLiveInk(stroke)
        }

        fun addEraseMask(mask: EraseMaskRecord) {
            rasterCache.appendLiveErase(mask)
        }

        fun replaceDocument(document: DrawingDocument) {
            rasterCache.reconcile(
                newDocumentId = document.documentId,
                operations = document.operations,
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val transform = viewportTransform ?: return
            val saveCount = canvas.save()
            canvas.translate(transform.offsetX, transform.offsetY)
            canvas.scale(transform.scale, transform.scale)
            canvas.clipRect(0f, 0f, documentSize.width, documentSize.height)
            canvas.drawBitmap(rasterCache.bitmap(), 0f, 0f, bitmapPaint)
            canvas.restoreToCount(saveCount)
        }
    }
'''

updated = text[:start] + replacement + text[end:]
if updated == text:
    raise SystemExit("No raster-view change produced")
path.write_text(updated, encoding="utf-8")
