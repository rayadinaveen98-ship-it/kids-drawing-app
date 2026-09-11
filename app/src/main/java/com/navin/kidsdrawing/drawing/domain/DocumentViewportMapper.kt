package com.navin.kidsdrawing.drawing.domain

import kotlin.math.min

/**
 * Fit-center transform between stable logical document coordinates and the current viewport.
 *
 * The mapper deliberately has no Android dependency so coordinate behavior can be unit tested and
 * remains independent from density, orientation, or a specific rendering toolkit.
 */
class DocumentViewportMapper(
    val documentSize: DocumentSize,
) {
    data class Transform(
        val scale: Float,
        val offsetX: Float,
        val offsetY: Float,
        val viewportWidth: Float,
        val viewportHeight: Float,
    )

    fun transformFor(viewportWidth: Float, viewportHeight: Float): Transform? {
        if (!viewportWidth.isFinite() || !viewportHeight.isFinite()) return null
        if (viewportWidth <= 0f || viewportHeight <= 0f) return null

        val scale = min(
            viewportWidth / documentSize.width,
            viewportHeight / documentSize.height,
        )
        val renderedWidth = documentSize.width * scale
        val renderedHeight = documentSize.height * scale

        return Transform(
            scale = scale,
            offsetX = (viewportWidth - renderedWidth) / 2f,
            offsetY = (viewportHeight - renderedHeight) / 2f,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
        )
    }

    fun documentToViewport(
        point: DocumentPoint,
        transform: Transform,
    ): DocumentPoint = DocumentPoint(
        x = transform.offsetX + (point.x * transform.scale),
        y = transform.offsetY + (point.y * transform.scale),
    )

    fun viewportToDocument(
        viewportX: Float,
        viewportY: Float,
        transform: Transform,
    ): DocumentPoint = DocumentPoint(
        x = (viewportX - transform.offsetX) / transform.scale,
        y = (viewportY - transform.offsetY) / transform.scale,
    )

    /** Returns null for the letterboxed area outside the logical paper. */
    fun viewportToDocumentOrNull(
        viewportX: Float,
        viewportY: Float,
        transform: Transform,
    ): DocumentPoint? {
        val point = viewportToDocument(viewportX, viewportY, transform)
        return point.takeIf {
            it.x in 0f..documentSize.width && it.y in 0f..documentSize.height
        }
    }
}
