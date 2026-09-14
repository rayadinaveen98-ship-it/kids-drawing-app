package com.navin.kidsdrawing.lesson.assistance

import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSequence
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSource
import com.navin.kidsdrawing.lesson.model.AuthoredGuide
import com.navin.kidsdrawing.lesson.model.AuthoredStroke
import com.navin.kidsdrawing.lesson.model.DrawingStep
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage

/** Purpose is semantic; renderers decide how a trace path vs anchors should look. */
enum class GuideOverlayPurpose {
    TRACE_MODE,
    HELP,
}

data class GuideOverlayRequest(
    val overlayId: String,
    val stepId: String,
    val purpose: GuideOverlayPurpose,
    val helpLevel: Int,
    val helpKind: HelpKind,
    val sequence: TeacherStrokeSequence,
) {
    init {
        require(overlayId.isNotBlank()) { "overlayId cannot be blank." }
        require(stepId.isNotBlank()) { "stepId cannot be blank." }
        require(helpLevel in 0..5) { "helpLevel must be 0..5." }
    }
}

/**
 * Resolves validated guide references into an isolated static overlay sequence.
 * Generated strokes are TEACHER_GENERATED, so the frozen child document engine rejects them.
 */
object LessonGuideOverlayFactory {
    fun create(
        lessonPackage: LessonRuntimePackage,
        step: DrawingStep,
        guideRefs: List<String>,
        purpose: GuideOverlayPurpose,
        helpLevel: Int,
        helpKind: HelpKind,
    ): GuideOverlayRequest {
        require(guideRefs.isNotEmpty()) { "Guide overlay requires at least one guide reference." }
        val guideCatalog = lessonPackage.strokeCatalog.guides.associateBy(AuthoredGuide::id)

        val strokeIds = linkedSetOf<String>()
        guideRefs.forEach { guideRef ->
            val guide = requireNotNull(guideCatalog[guideRef]) {
                "Validated lesson step ${step.id} references missing guide $guideRef."
            }
            guide.strokeRefs.forEach(strokeIds::add)
        }
        require(strokeIds.isNotEmpty()) { "Guide overlay for step ${step.id} resolved no strokes." }

        return createFromStrokeRefs(
            lessonPackage = lessonPackage,
            step = step,
            strokeRefs = strokeIds.toList(),
            purpose = purpose,
            helpLevel = helpLevel,
            helpKind = helpKind,
        )
    }

    fun traceForStep(
        lessonPackage: LessonRuntimePackage,
        step: DrawingStep,
    ): GuideOverlayRequest {
        val trace = step.help
            .filter { it.kind == HelpKind.TRACE_PATH && it.guideRefs.isNotEmpty() }
            .minByOrNull { it.level }
        if (trace != null) {
            return create(
                lessonPackage = lessonPackage,
                step = step,
                guideRefs = trace.guideRefs,
                purpose = GuideOverlayPurpose.TRACE_MODE,
                helpLevel = 0,
                helpKind = HelpKind.TRACE_PATH,
            )
        }

        require(step.childTurn.expectedStrokeRefs.isNotEmpty()) {
            "Validated Trace & Learn step ${step.id} has neither a trace guide nor expected geometry."
        }
        return createFromStrokeRefs(
            lessonPackage = lessonPackage,
            step = step,
            strokeRefs = step.childTurn.expectedStrokeRefs,
            purpose = GuideOverlayPurpose.TRACE_MODE,
            helpLevel = 0,
            helpKind = HelpKind.TRACE_PATH,
        )
    }

    private fun createFromStrokeRefs(
        lessonPackage: LessonRuntimePackage,
        step: DrawingStep,
        strokeRefs: List<String>,
        purpose: GuideOverlayPurpose,
        helpLevel: Int,
        helpKind: HelpKind,
    ): GuideOverlayRequest {
        val strokeCatalog = lessonPackage.strokeCatalog.strokes.associateBy(AuthoredStroke::id)
        val strokeIds = strokeRefs.distinct()
        require(strokeIds.isNotEmpty()) { "Guide overlay for step ${step.id} resolved no strokes." }

        val sources = strokeIds.mapIndexed { index, strokeId ->
            val authored = requireNotNull(strokeCatalog[strokeId]) {
                "Validated guide references missing authored stroke $strokeId."
            }
            val firstTime = authored.points.first().timeMs
            val points = authored.points.map { point ->
                StrokePoint(
                    x = point.x,
                    y = point.y,
                    elapsedTimeMillis = (point.timeMs - firstTime).coerceAtLeast(0L),
                    pressure = point.pressure.coerceIn(0f, 1f),
                )
            }
            TeacherStrokeSource(
                stroke = InkStrokeRecord(
                    strokeId = "guide-${lessonPackage.lesson.lessonId}-${step.id}-$strokeId",
                    brushPresetId = GUIDE_BRUSH_PRESET,
                    colorArgb = GUIDE_COLOR_ARGB,
                    opacity = GUIDE_OPACITY,
                    baseSize = GUIDE_BASE_SIZE,
                    tool = PointerTool.STYLUS,
                    points = points,
                    authorRole = StrokeAuthorRole.TEACHER_GENERATED,
                ),
                startTimeMillis = index * GUIDE_STAGGER_MS,
            )
        }
        val overlayId = buildString {
            append("guide-")
            append(lessonPackage.lesson.lessonId)
            append("-r")
            append(lessonPackage.lesson.revision)
            append('-')
            append(step.id)
            append('-')
            append(purpose.name.lowercase())
            append("-h")
            append(helpLevel)
        }
        return GuideOverlayRequest(
            overlayId = overlayId,
            stepId = step.id,
            purpose = purpose,
            helpLevel = helpLevel,
            helpKind = helpKind,
            sequence = TeacherStrokeSequence(
                sequenceId = overlayId,
                strokes = sources,
            ),
        )
    }

    private const val GUIDE_BRUSH_PRESET = "marker.standard"
    private val GUIDE_COLOR_ARGB: Int = 0xFF6C63FF.toInt()
    private const val GUIDE_OPACITY = 0.30f
    private const val GUIDE_BASE_SIZE = 6f
    private const val GUIDE_STAGGER_MS = 1L
}
