package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AuthoredColorRegion
import com.navin.kidsdrawing.lesson.model.AuthoredRegionPoint
import com.navin.kidsdrawing.lesson.model.ColoringRegionCatalogSource
import com.navin.kidsdrawing.lesson.model.LessonSource
import kotlin.math.abs

/** Strict validator for prepared fill geometry and authored coloring references. */
object ColoringRegionValidator {
    private val idRegex = Regex("^[a-z0-9]+(?:[._-][a-z0-9]+)*$")

    fun validate(
        lesson: LessonSource,
        catalog: ColoringRegionCatalogSource?,
    ): List<LessonDiagnostic> = buildList {
        val referencedRegionIds = lesson.coloring
            ?.steps
            .orEmpty()
            .flatMap { it.regionIds }

        if (catalog == null) {
            if (referencedRegionIds.isNotEmpty()) {
                add(
                    LessonDiagnostic(
                        LessonDiagnosticCode.MISSING_ASSET,
                        "assets.coloringRegions",
                        "Prepared coloring region references require a coloringRegions asset.",
                    ),
                )
            }
            return@buildList
        }

        if (catalog.schemaVersion != CURRENT_REGION_SCHEMA_VERSION) {
            add(
                LessonDiagnostic(
                    LessonDiagnosticCode.UNSUPPORTED_SCHEMA_VERSION,
                    "assets.coloringRegions.schemaVersion",
                    "Unsupported coloring region schema ${catalog.schemaVersion}; expected $CURRENT_REGION_SCHEMA_VERSION.",
                ),
            )
        }

        if (lesson.minimumContentApi < PREPARED_REGION_CONTENT_API) {
            add(
                LessonDiagnostic(
                    LessonDiagnosticCode.UNSUPPORTED_CONTENT_API,
                    "minimumContentApi",
                    "Prepared coloring regions require content API $PREPARED_REGION_CONTENT_API+.",
                ),
            )
        }

        val ids = catalog.regions.map { it.id }
        if (ids.size != ids.distinct().size) {
            add(
                LessonDiagnostic(
                    LessonDiagnosticCode.DUPLICATE_ID,
                    "assets.coloringRegions.regions",
                    "Prepared coloring region IDs must be unique.",
                ),
            )
        }

        catalog.regions.forEachIndexed { index, region ->
            val base = "assets.coloringRegions.regions[$index]"
            if (!idRegex.matches(region.id) || region.id.length !in 1..120) {
                add(
                    LessonDiagnostic(
                        LessonDiagnosticCode.INVALID_ID,
                        "$base.id",
                        "Invalid identifier: ${region.id}",
                    ),
                )
            }
            validatePolygon(lesson, region, base).forEach(::add)
        }

        val regionIdSet = ids.toSet()
        lesson.coloring?.steps.orEmpty().forEachIndexed { stepIndex, step ->
            step.regionIds.forEach { regionId ->
                if (regionId !in regionIdSet) {
                    add(
                        LessonDiagnostic(
                            LessonDiagnosticCode.MISSING_REFERENCE,
                            "coloring.steps[$stepIndex].regionIds",
                            "Missing authored reference: $regionId",
                        ),
                    )
                }
            }
        }
    }

    private fun validatePolygon(
        lesson: LessonSource,
        region: AuthoredColorRegion,
        base: String,
    ): List<LessonDiagnostic> = buildList {
        val points = region.points
        if (points.size < 3) {
            add(invalid(base, "Prepared region needs at least three points."))
            return@buildList
        }

        val uniquePoints = points.map { it.x to it.y }.distinct()
        if (uniquePoints.size < 3) {
            add(invalid(base, "Prepared region needs at least three distinct points."))
        }

        points.forEachIndexed { pointIndex, point ->
            if (!point.x.isFinite() || !point.y.isFinite()) {
                add(invalid("$base.points[$pointIndex]", "Region coordinates must be finite."))
            } else if (
                point.x !in 0f..lesson.canvas.width.toFloat() ||
                point.y !in 0f..lesson.canvas.height.toFloat()
            ) {
                add(invalid("$base.points[$pointIndex]", "Region point must stay inside the authored canvas."))
            }
        }

        if (points.all { it.x.isFinite() && it.y.isFinite() }) {
            if (abs(signedArea(points)) <= AREA_EPSILON) {
                add(invalid(base, "Prepared region polygon area must be non-zero."))
            }
            if (hasSelfIntersection(points)) {
                add(invalid(base, "Prepared region polygon must not self-intersect."))
            }
        }
    }

    private fun signedArea(points: List<AuthoredRegionPoint>): Double {
        var twiceArea = 0.0
        points.indices.forEach { index ->
            val a = points[index]
            val b = points[(index + 1) % points.size]
            twiceArea += a.x.toDouble() * b.y.toDouble() - b.x.toDouble() * a.y.toDouble()
        }
        return twiceArea / 2.0
    }

    private fun hasSelfIntersection(points: List<AuthoredRegionPoint>): Boolean {
        val edgeCount = points.size
        for (first in 0 until edgeCount) {
            val firstNext = (first + 1) % edgeCount
            for (second in first + 1 until edgeCount) {
                val secondNext = (second + 1) % edgeCount
                if (first == second || firstNext == second || secondNext == first) continue
                // First and last edges are adjacent through the implicit closing vertex.
                if (first == 0 && secondNext == 0) continue
                if (segmentsIntersect(points[first], points[firstNext], points[second], points[secondNext])) {
                    return true
                }
            }
        }
        return false
    }

    private fun segmentsIntersect(
        a: AuthoredRegionPoint,
        b: AuthoredRegionPoint,
        c: AuthoredRegionPoint,
        d: AuthoredRegionPoint,
    ): Boolean {
        val o1 = orientation(a, b, c)
        val o2 = orientation(a, b, d)
        val o3 = orientation(c, d, a)
        val o4 = orientation(c, d, b)

        if (oppositeSigns(o1, o2) && oppositeSigns(o3, o4)) return true
        if (abs(o1) <= GEOMETRY_EPSILON && onSegment(a, b, c)) return true
        if (abs(o2) <= GEOMETRY_EPSILON && onSegment(a, b, d)) return true
        if (abs(o3) <= GEOMETRY_EPSILON && onSegment(c, d, a)) return true
        if (abs(o4) <= GEOMETRY_EPSILON && onSegment(c, d, b)) return true
        return false
    }

    private fun orientation(a: AuthoredRegionPoint, b: AuthoredRegionPoint, c: AuthoredRegionPoint): Double =
        (b.x - a.x).toDouble() * (c.y - a.y).toDouble() -
            (b.y - a.y).toDouble() * (c.x - a.x).toDouble()

    private fun oppositeSigns(a: Double, b: Double): Boolean =
        (a > GEOMETRY_EPSILON && b < -GEOMETRY_EPSILON) ||
            (a < -GEOMETRY_EPSILON && b > GEOMETRY_EPSILON)

    private fun onSegment(a: AuthoredRegionPoint, b: AuthoredRegionPoint, p: AuthoredRegionPoint): Boolean =
        p.x.toDouble() in (minOf(a.x, b.x) - GEOMETRY_EPSILON)..(maxOf(a.x, b.x) + GEOMETRY_EPSILON) &&
            p.y.toDouble() in (minOf(a.y, b.y) - GEOMETRY_EPSILON)..(maxOf(a.y, b.y) + GEOMETRY_EPSILON)

    private fun invalid(path: String, message: String) = LessonDiagnostic(
        LessonDiagnosticCode.INVALID_VALUE,
        path,
        message,
    )

    const val PREPARED_REGION_CONTENT_API: Int = 2
    const val CURRENT_REGION_SCHEMA_VERSION: String = "1.0"
    private const val AREA_EPSILON = 0.01
    private const val GEOMETRY_EPSILON = 0.0001
}
