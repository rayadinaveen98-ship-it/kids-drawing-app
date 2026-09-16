package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.ColoringMode
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.LessonSource
import com.navin.kidsdrawing.lesson.model.StrokeCatalogSource
import com.navin.kidsdrawing.lesson.model.TeachingMode

/**
 * Content Library V2 release-capability gate.
 *
 * The schema answers whether a package is structurally valid. This validator answers the stricter
 * product question: can every declared child-facing capability in this lesson actually be executed
 * by the accepted runtime using authored lesson-specific data?
 *
 * It is intentionally pure and deterministic so the same rules can be reused by CI and a future
 * Content Studio. It never invents missing guides, text, completion signals, colors, or geometry.
 */
object LessonCapabilityValidator {
    fun validate(packageData: LessonRuntimePackage): List<LessonCapabilityDiagnostic> =
        validate(
            lesson = packageData.lesson,
            catalog = packageData.strokeCatalog,
        )

    fun validate(
        lesson: LessonSource,
        catalog: StrokeCatalogSource,
    ): List<LessonCapabilityDiagnostic> = buildList {
        val guideIds = catalog.guides.mapTo(linkedSetOf()) { it.id }

        lesson.drawing.steps.forEachIndexed { stepIndex, step ->
            val base = "drawing.steps[$stepIndex]"

            if (step.childTurn.completionPolicy == ChildCompletionPolicy.AUTHORED_SIGNAL) {
                add(
                    diagnostic(
                        code = LessonCapabilityDiagnosticCode.UNSUPPORTED_RUNTIME_CAPABILITY,
                        path = "$base.childTurn.completionPolicy",
                        message = "authored_signal is declared by the content model but is not implemented by the accepted Lesson Engine runtime.",
                    ),
                )
            }

            step.help.forEachIndexed { helpIndex, help ->
                val helpPath = "$base.help[$helpIndex]"
                when (help.kind) {
                    HelpKind.GENTLE_HINT -> {
                        if (help.narrationKey.isNullOrBlank()) {
                            add(
                                diagnostic(
                                    LessonCapabilityDiagnosticCode.INCOMPLETE_HELP_OUTPUT,
                                    helpPath,
                                    "gentle_hint requires an authored narration/text key.",
                                ),
                            )
                        }
                    }

                    HelpKind.VISUAL_GUIDE,
                    HelpKind.DIRECTION_ANCHORS,
                    HelpKind.TRACE_PATH,
                    -> {
                        if (help.guideRefs.isEmpty()) {
                            add(
                                diagnostic(
                                    LessonCapabilityDiagnosticCode.INCOMPLETE_HELP_OUTPUT,
                                    "$helpPath.guideRefs",
                                    "${help.kind.name.lowercase()} declares visual guidance but has no authored guide geometry.",
                                ),
                            )
                        } else if (help.guideRefs.none(guideIds::contains)) {
                            add(
                                diagnostic(
                                    LessonCapabilityDiagnosticCode.INCOMPLETE_HELP_OUTPUT,
                                    "$helpPath.guideRefs",
                                    "${help.kind.name.lowercase()} does not resolve any authored guide geometry.",
                                ),
                            )
                        }
                    }

                    HelpKind.ASSISTED_SUCCESS -> {
                        if (help.narrationKey.isNullOrBlank() && help.guideRefs.isEmpty()) {
                            add(
                                diagnostic(
                                    LessonCapabilityDiagnosticCode.INCOMPLETE_HELP_OUTPUT,
                                    helpPath,
                                    "assisted_success requires authored narration/text and/or guide geometry.",
                                ),
                            )
                        }
                    }
                }
            }

            if (TeachingMode.TRACE_AND_LEARN in lesson.supportedModes) {
                val intentionalOpenAuthorship =
                    step.childTurn.completionPolicy == ChildCompletionPolicy.MANUAL_DONE &&
                        step.childTurn.allowSkip &&
                        step.childTurn.expectedStrokeRefs.isEmpty()
                val hasAuthoredTraceGuide = step.help.any { help ->
                    help.kind == HelpKind.TRACE_PATH && help.guideRefs.any(guideIds::contains)
                }
                val hasExpectedGeometry = step.childTurn.expectedStrokeRefs.isNotEmpty()

                if (!intentionalOpenAuthorship && !hasAuthoredTraceGuide && !hasExpectedGeometry) {
                    add(
                        diagnostic(
                            LessonCapabilityDiagnosticCode.INCOMPLETE_TRACE_SUPPORT,
                            base,
                            "Trace & Learn is declared but this structured step has no authored trace guide or expected geometry.",
                        ),
                    )
                }
            }
        }

        val coloring = lesson.coloring
        if (coloring?.enabled == true) {
            coloring.steps.forEachIndexed { index, step ->
                if (step.enforceSuggestedColors) {
                    add(
                        diagnostic(
                            LessonCapabilityDiagnosticCode.UNSUPPORTED_RUNTIME_CAPABILITY,
                            "coloring.steps[$index].enforceSuggestedColors",
                            "Enforced suggested colors are modeled in content but are not enforced by the accepted coloring runtime.",
                        ),
                    )
                }
            }

            if (coloring.defaultMode == ColoringMode.GUIDED) {
                val regionlessSteps = coloring.steps.withIndex().filter { it.value.regionIds.isEmpty() }
                if (regionlessSteps.size > 1) {
                    add(
                        diagnostic(
                            LessonCapabilityDiagnosticCode.AMBIGUOUS_GUIDED_COLORING,
                            "coloring.steps",
                            "Guided coloring currently uses one global freehand-color signal for regionless steps; more than one regionless guided step cannot progress independently.",
                        ),
                    )
                }
            }
        }
    }

    private fun diagnostic(
        code: LessonCapabilityDiagnosticCode,
        path: String,
        message: String,
    ) = LessonCapabilityDiagnostic(code, path, message)
}

enum class LessonCapabilityDiagnosticCode {
    UNSUPPORTED_RUNTIME_CAPABILITY,
    INCOMPLETE_HELP_OUTPUT,
    INCOMPLETE_TRACE_SUPPORT,
    AMBIGUOUS_GUIDED_COLORING,
}

data class LessonCapabilityDiagnostic(
    val code: LessonCapabilityDiagnosticCode,
    val path: String,
    val message: String,
)
