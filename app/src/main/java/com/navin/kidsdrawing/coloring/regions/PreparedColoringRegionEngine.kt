package com.navin.kidsdrawing.coloring.regions

import com.navin.kidsdrawing.drawing.domain.ColorRegionFillRecord
import com.navin.kidsdrawing.drawing.domain.ColorRegionPoint
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.lesson.model.AuthoredColorRegion
import com.navin.kidsdrawing.lesson.model.ColoringRegionCatalogSource
import com.navin.kidsdrawing.lesson.model.ColoringStep

/** Pure geometry/progress helper; product UI never owns prepared-region truth. */
class PreparedColoringRegionEngine(
    catalog: ColoringRegionCatalogSource,
) {
    private val orderedRegions = catalog.regions.toList()
    private val regionsById = orderedRegions.associateBy { it.id }

    val regionIds: Set<String> = regionsById.keys

    fun regionContaining(
        x: Float,
        y: Float,
        allowedRegionIds: Set<String>? = null,
    ): AuthoredColorRegion? {
        if (!x.isFinite() || !y.isFinite()) return null
        return orderedRegions.firstOrNull { region ->
            (allowedRegionIds == null || region.id in allowedRegionIds) &&
                contains(region, x, y)
        }
    }

    fun fillRecord(region: AuthoredColorRegion, colorArgb: Int): ColorRegionFillRecord =
        ColorRegionFillRecord(
            regionId = region.id,
            colorArgb = colorArgb,
            points = region.points.map { ColorRegionPoint(it.x, it.y) },
        )

    fun guidedProgress(
        steps: List<ColoringStep>,
        document: DrawingDocument,
    ): GuidedColoringProgress {
        val filled = document.activeColorRegionFills().mapTo(linkedSetOf()) { it.regionId }
        val hasFreehandColor = document.activeColorStrokes().isNotEmpty()
        val completed = steps.map { step ->
            if (step.regionIds.isEmpty()) hasFreehandColor else step.regionIds.all(filled::contains)
        }
        val firstIncomplete = completed.indexOfFirst { !it }
        return if (firstIncomplete < 0) {
            GuidedColoringProgress(
                currentStepIndex = null,
                totalSteps = steps.size,
                currentStep = null,
                completedStepCount = completed.count { it },
                filledRegionIds = filled,
            )
        } else {
            GuidedColoringProgress(
                currentStepIndex = firstIncomplete,
                totalSteps = steps.size,
                currentStep = steps[firstIncomplete],
                completedStepCount = completed.take(firstIncomplete).count { it },
                filledRegionIds = filled,
            )
        }
    }

    private fun contains(region: AuthoredColorRegion, x: Float, y: Float): Boolean {
        val points = region.points
        var inside = false
        var previous = points.last()
        points.forEach { current ->
            if (pointOnSegment(previous.x, previous.y, current.x, current.y, x, y)) return true
            val crosses = (current.y > y) != (previous.y > y)
            if (crosses) {
                val intersectX = (previous.x - current.x) * (y - current.y) /
                    (previous.y - current.y) + current.x
                if (x < intersectX) inside = !inside
            }
            previous = current
        }
        return inside
    }

    private fun pointOnSegment(
        ax: Float,
        ay: Float,
        bx: Float,
        by: Float,
        px: Float,
        py: Float,
    ): Boolean {
        val cross = (px - ax) * (by - ay) - (py - ay) * (bx - ax)
        if (kotlin.math.abs(cross) > EPSILON) return false
        return px in (minOf(ax, bx) - EPSILON)..(maxOf(ax, bx) + EPSILON) &&
            py in (minOf(ay, by) - EPSILON)..(maxOf(ay, by) + EPSILON)
    }

    companion object {
        private const val EPSILON = 0.001f
    }
}

data class GuidedColoringProgress(
    val currentStepIndex: Int?,
    val totalSteps: Int,
    val currentStep: ColoringStep?,
    val completedStepCount: Int,
    val filledRegionIds: Set<String>,
) {
    val isComplete: Boolean get() = totalSteps > 0 && currentStepIndex == null
}
