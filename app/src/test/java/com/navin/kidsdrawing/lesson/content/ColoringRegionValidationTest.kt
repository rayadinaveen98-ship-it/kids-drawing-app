package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.AuthoredColorRegion
import com.navin.kidsdrawing.lesson.model.AuthoredRegionPoint
import com.navin.kidsdrawing.lesson.model.BackgroundRole
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.ChildTurn
import com.navin.kidsdrawing.lesson.model.ColoringMode
import com.navin.kidsdrawing.lesson.model.ColoringRegionCatalogSource
import com.navin.kidsdrawing.lesson.model.ColoringStep
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.LessonAssets
import com.navin.kidsdrawing.lesson.model.LessonCanvas
import com.navin.kidsdrawing.lesson.model.LessonColoring
import com.navin.kidsdrawing.lesson.model.LessonDrawing
import com.navin.kidsdrawing.lesson.model.LessonMetadata
import com.navin.kidsdrawing.lesson.model.LessonSource
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeacherDemo
import com.navin.kidsdrawing.lesson.model.TeachingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColoringRegionValidationTest {
    @Test
    fun validPreparedRegionsResolveAuthoredReferences() {
        val diagnostics = ColoringRegionValidator.validate(
            lesson = lesson(regionIds = listOf("roof", "wall")),
            catalog = catalog(
                region("roof", 200f to 400f, 500f to 180f, 800f to 400f),
                region("wall", 280f to 400f, 720f to 400f, 720f to 800f, 280f to 800f),
            ),
        )

        assertTrue(diagnostics.toString(), diagnostics.isEmpty())
    }

    @Test
    fun legacyFreehandColoringNeedsNoRegionCatalog() {
        val diagnostics = ColoringRegionValidator.validate(
            lesson = lesson(regionIds = emptyList(), minimumContentApi = 1, coloringRegions = null),
            catalog = null,
        )

        assertTrue(diagnostics.toString(), diagnostics.isEmpty())
    }

    @Test
    fun missingReferencedRegionIsRejected() {
        val diagnostics = ColoringRegionValidator.validate(
            lesson = lesson(regionIds = listOf("missing")),
            catalog = catalog(region("roof", 200f to 400f, 500f to 180f, 800f to 400f)),
        )

        assertTrue(diagnostics.any { it.code == LessonDiagnosticCode.MISSING_REFERENCE })
    }

    @Test
    fun preparedRegionRequiresContentApiTwo() {
        val diagnostics = ColoringRegionValidator.validate(
            lesson = lesson(regionIds = listOf("roof"), minimumContentApi = 1),
            catalog = catalog(region("roof", 200f to 400f, 500f to 180f, 800f to 400f)),
        )

        assertTrue(diagnostics.any { it.code == LessonDiagnosticCode.UNSUPPORTED_CONTENT_API })
    }

    @Test
    fun degenerateOutOfBoundsAndSelfIntersectingPolygonsAreRejected() {
        val degenerate = region("flat", 100f to 100f, 200f to 200f, 300f to 300f)
        val outside = region("outside", 100f to 100f, 1200f to 100f, 100f to 300f)
        val bowTie = region("bow", 100f to 100f, 400f to 400f, 100f to 400f, 400f to 100f)
        val diagnostics = ColoringRegionValidator.validate(
            lesson = lesson(regionIds = listOf("flat", "outside", "bow")),
            catalog = catalog(degenerate, outside, bowTie),
        )

        assertTrue(diagnostics.any { it.message.contains("non-zero") })
        assertTrue(diagnostics.any { it.message.contains("inside the authored canvas") })
        assertTrue(diagnostics.any { it.message.contains("self-intersect") })
    }

    @Test
    fun duplicateRegionIdsAreRejected() {
        val duplicate = region("roof", 200f to 400f, 500f to 180f, 800f to 400f)
        val diagnostics = ColoringRegionValidator.validate(
            lesson = lesson(regionIds = listOf("roof")),
            catalog = catalog(duplicate, duplicate),
        )

        assertEquals(1, diagnostics.count { it.code == LessonDiagnosticCode.DUPLICATE_ID })
    }

    private fun lesson(
        regionIds: List<String>,
        minimumContentApi: Int = 2,
        coloringRegions: String? = "coloring/regions.json",
    ) = LessonSource(
        schemaVersion = "1.0",
        lessonId = "test-house",
        revision = 1,
        status = LessonStatus.RELEASE,
        minimumContentApi = minimumContentApi,
        metadata = LessonMetadata(
            titleKey = "lesson.test.title",
            summaryKey = "lesson.test.summary",
            ageBands = listOf(AgeBand.CREATIVE_EXPLORERS),
            difficulty = 1,
            estimatedMinutes = 5,
            categoryIds = listOf("everyday"),
            skillIds = listOf("shape_construction"),
        ),
        canvas = LessonCanvas(1000, 1000, BackgroundRole.PAPER_WARM),
        supportedModes = listOf(TeachingMode.DRAW_WITH_ME),
        assets = LessonAssets(
            strokeFile = "strokes.json",
            thumbnail = "thumbnail.svg",
            preview = "preview.svg",
            strings = mapOf("en" to "strings/en.json"),
            coloringRegions = coloringRegions,
        ),
        drawing = LessonDrawing(
            listOf(
                DrawingStep(
                    id = "shape",
                    objectiveSkillIds = listOf("shape_construction"),
                    teacher = TeacherDemo(strokeRefs = listOf("shape")),
                    childTurn = ChildTurn(
                        completionPolicy = ChildCompletionPolicy.MANUAL_DONE,
                        allowReplay = true,
                        allowSkip = false,
                    ),
                ),
            ),
        ),
        coloring = LessonColoring(
            enabled = true,
            defaultMode = ColoringMode.GUIDED,
            steps = listOf(ColoringStep(id = "paint", regionIds = regionIds)),
        ),
    )

    private fun catalog(vararg regions: AuthoredColorRegion) = ColoringRegionCatalogSource(
        schemaVersion = "1.0",
        regions = regions.toList(),
    )

    private fun region(id: String, vararg points: Pair<Float, Float>) = AuthoredColorRegion(
        id = id,
        points = points.map { (x, y) -> AuthoredRegionPoint(x, y) },
    )
}
