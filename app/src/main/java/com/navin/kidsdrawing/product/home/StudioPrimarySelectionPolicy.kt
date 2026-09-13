package com.navin.kidsdrawing.product.home

data class StudioPrimarySelection(
    val lessonId: String?,
    val lessonRevision: Int?,
    val drawingResume: ResumeLessonCandidate?,
    val coloringResume: ColoringResumeCandidate?,
)

/** Pure priority rule used by Home so persisted work always outranks fresh recommendations. */
object StudioPrimarySelectionPolicy {
    fun select(
        rankedRecommendations: List<LessonRecommendation>,
        drawingCandidates: List<ResumeLessonCandidate>,
        coloringCandidates: List<ColoringResumeCandidate>,
    ): StudioPrimarySelection {
        val coloring = coloringCandidates.maxWithOrNull(
            compareBy<ColoringResumeCandidate> { it.savedAtEpochMillis }
                .thenBy { it.lessonId }
                .thenBy { it.lessonRevision },
        )
        if (coloring != null) {
            return StudioPrimarySelection(
                lessonId = coloring.lessonId,
                lessonRevision = coloring.lessonRevision,
                drawingResume = null,
                coloringResume = coloring,
            )
        }

        val drawing = drawingCandidates.maxWithOrNull(
            compareBy<ResumeLessonCandidate> { it.savedAtEpochMillis }
                .thenBy { it.lessonId }
                .thenBy { it.lessonRevision },
        )
        if (drawing != null) {
            return StudioPrimarySelection(
                lessonId = drawing.lessonId,
                lessonRevision = drawing.lessonRevision,
                drawingResume = drawing,
                coloringResume = null,
            )
        }

        val fresh = rankedRecommendations.firstOrNull()
        return StudioPrimarySelection(
            lessonId = fresh?.lessonId,
            lessonRevision = fresh?.lessonRevision,
            drawingResume = null,
            coloringResume = null,
        )
    }
}
