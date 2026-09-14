package com.navin.kidsdrawing.product.adaptive

import android.content.Context
import com.navin.kidsdrawing.lesson.session.HelpLevelChanged
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.ReplayDemonstration
import com.navin.kidsdrawing.lesson.session.RequestHelp
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackRequested
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import com.navin.kidsdrawing.product.profile.AgeBand
import java.io.File

sealed interface ProductAdaptiveHelpResult {
    data class Applied(
        val action: AdaptiveHelpAction,
        val companionCue: String,
    ) : ProductAdaptiveHelpResult

    data object Unavailable : ProductAdaptiveHelpResult
    data object Rejected : ProductAdaptiveHelpResult
}

/**
 * Product coordinator for a genuine child Help tap.
 *
 * Lesson Engine remains the only semantic Help/session authority. This coordinator merely chooses
 * one existing authored action, dispatches it through the existing runtime, and records bounded
 * advisory context after the command is accepted.
 */
class ProductAdaptiveHelpCoordinator private constructor(
    private val repository: LocalAdaptiveStateRepository,
) {
    constructor(context: Context) : this(
        LocalAdaptiveStateRepository(
            File(
                context.applicationContext.filesDir,
                LocalAdaptiveStateRepository.DIRECTORY_NAME,
            ),
        ),
    )

    suspend fun onChildHelpRequested(
        runtime: ProductLessonRuntime,
        ageBand: AgeBand,
    ): ProductAdaptiveHelpResult {
        val current = runtime.sessionState.value
        val activeContext = when (current) {
            is LessonSessionState.AwaitingChild -> current.context
            is LessonSessionState.HelpActive -> current.context
            else -> return ProductAdaptiveHelpResult.Unavailable
        }
        val packageData = runtime.packageData ?: return ProductAdaptiveHelpResult.Unavailable
        val step = packageData.lesson.drawing.steps.getOrNull(activeContext.currentStepIndex)
            ?: return ProductAdaptiveHelpResult.Unavailable

        val adaptiveState = runCatching { repository.loadForPolicy() }.getOrNull()
        val suggestion = AdaptiveHelpSuggestionPolicy.suggest(
            childRequested = true,
            ageBand = ageBand,
            step = step,
            currentHelpLevel = activeContext.helpLevel,
            state = adaptiveState,
        ) ?: return ProductAdaptiveHelpResult.Unavailable

        val result = runtime.dispatch(
            when (suggestion.action) {
                AdaptiveHelpAction.NEXT_AUTHORED_HELP -> RequestHelp
                AdaptiveHelpAction.REPLAY -> ReplayDemonstration
            },
        )
        if (result !is LessonCommandResult.Accepted) return ProductAdaptiveHelpResult.Rejected

        val adaptiveEvent = when (suggestion.action) {
            AdaptiveHelpAction.NEXT_AUTHORED_HELP -> {
                val changed = result.events.filterIsInstance<HelpLevelChanged>().lastOrNull()
                    ?: return ProductAdaptiveHelpResult.Applied(
                        action = suggestion.action,
                        companionCue = suggestion.reasonCopy,
                    )
                AdaptiveEvent.HelpRequested(
                    eventKey = buildString {
                        append("help:")
                        append(runtime.runtimeIdentity.sessionId)
                        append(':')
                        append(step.id)
                        append(":level:")
                        append(changed.currentLevel)
                        append(":adaptive-revision:")
                        append(adaptiveState?.revision ?: 0L)
                    },
                    lessonId = packageData.lesson.lessonId,
                    lessonRevision = packageData.lesson.revision,
                    skillIds = step.objectiveSkillIds,
                    categoryIds = packageData.lesson.metadata.categoryIds,
                    helpKind = changed.kind,
                    choice = AdaptiveHelpChoice.AUTHORED_HELP,
                )
            }

            AdaptiveHelpAction.REPLAY -> {
                val playback = result.events
                    .filterIsInstance<TeacherPlaybackRequested>()
                    .firstOrNull { it.request.replay }
                    ?: return ProductAdaptiveHelpResult.Applied(
                        action = suggestion.action,
                        companionCue = suggestion.reasonCopy,
                    )
                AdaptiveEvent.HelpRequested(
                    eventKey = "help:${runtime.runtimeIdentity.sessionId}:${step.id}:replay:${playback.request.requestId}",
                    lessonId = packageData.lesson.lessonId,
                    lessonRevision = packageData.lesson.revision,
                    skillIds = step.objectiveSkillIds,
                    categoryIds = packageData.lesson.metadata.categoryIds,
                    helpKind = null,
                    choice = AdaptiveHelpChoice.REPLAY,
                )
            }
        }

        // Advisory persistence can never turn an accepted lesson action into a product failure.
        runCatching { repository.record(adaptiveEvent) }
        return ProductAdaptiveHelpResult.Applied(
            action = suggestion.action,
            companionCue = suggestion.reasonCopy,
        )
    }
}
