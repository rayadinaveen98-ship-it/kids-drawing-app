package com.navin.kidsdrawing.product.adaptive

import com.navin.kidsdrawing.lesson.model.HelpKind

/** Stable lesson identity used only for local progression facts. */
data class AdaptiveLessonIdentity(
    val lessonId: String,
    val revision: Int,
) {
    init {
        require(lessonId.isNotBlank()) { "lessonId cannot be blank." }
        require(revision > 0) { "revision must be positive." }
    }
}

/**
 * Bounded, local-only advisory state for P5.7.
 *
 * This deliberately contains no artwork, strokes, timing telemetry, free-form child text,
 * cloud/device identifiers, scores, grades, or inferred ability labels.
 */
data class LocalAdaptiveState(
    val formatVersion: Int = CURRENT_FORMAT_VERSION,
    val revision: Long = 0L,
    val completedLessons: List<AdaptiveLessonIdentity> = emptyList(),
    val skillExposureCounts: Map<String, Int> = emptyMap(),
    val recentCompletions: List<AdaptiveLessonIdentity> = emptyList(),
    val helpRequestCounts: Map<String, Int> = emptyMap(),
    val processedEventKeys: List<String> = emptyList(),
) {
    init {
        require(formatVersion == CURRENT_FORMAT_VERSION) { "Unsupported adaptive state formatVersion $formatVersion." }
        require(revision >= 0L) { "revision cannot be negative." }
        require(completedLessons.size <= MAX_COMPLETED_LESSONS) { "completedLessons exceeds bound." }
        require(completedLessons.distinct().size == completedLessons.size) { "completedLessons must be unique." }
        require(recentCompletions.size <= MAX_RECENT_COMPLETIONS) { "recentCompletions exceeds bound." }
        require(skillExposureCounts.size <= MAX_SKILL_COUNTERS) { "skillExposureCounts exceeds bound." }
        require(helpRequestCounts.size <= MAX_HELP_COUNTERS) { "helpRequestCounts exceeds bound." }
        require(processedEventKeys.size <= MAX_PROCESSED_EVENT_KEYS) { "processedEventKeys exceeds bound." }
        require(skillExposureCounts.keys.all(String::isNotBlank)) { "skill exposure keys cannot be blank." }
        require(helpRequestCounts.keys.all(String::isNotBlank)) { "help request keys cannot be blank." }
        require(processedEventKeys.all(String::isNotBlank)) { "processed event keys cannot be blank." }
        require(skillExposureCounts.values.all { it in 0..MAX_COUNTER_VALUE }) { "skill exposure count out of range." }
        require(helpRequestCounts.values.all { it in 0..MAX_COUNTER_VALUE }) { "help request count out of range." }
    }

    fun hasCompleted(lessonId: String): Boolean = completedLessons.any { it.lessonId == lessonId }

    companion object {
        const val CURRENT_FORMAT_VERSION = 1
        const val MAX_COMPLETED_LESSONS = 128
        const val MAX_RECENT_COMPLETIONS = 8
        const val MAX_SKILL_COUNTERS = 128
        const val MAX_HELP_COUNTERS = 128
        const val MAX_PROCESSED_EVENT_KEYS = 128
        const val MAX_COUNTER_VALUE = 99

        fun empty(): LocalAdaptiveState = LocalAdaptiveState()
    }
}

enum class AdaptiveHelpChoice {
    AUTHORED_HELP,
    REPLAY,
}

/** Explicit product-meaning events are the only allowed mutation input for adaptive state. */
sealed interface AdaptiveEvent {
    val eventKey: String

    data class LessonCompleted(
        override val eventKey: String,
        val lessonId: String,
        val lessonRevision: Int,
        val skillIds: List<String>,
        val categoryIds: List<String>,
        val journeyIds: List<String>,
        val difficulty: Int,
    ) : AdaptiveEvent {
        init {
            require(eventKey.isNotBlank()) { "eventKey cannot be blank." }
            require(lessonId.isNotBlank()) { "lessonId cannot be blank." }
            require(lessonRevision > 0) { "lessonRevision must be positive." }
            require(difficulty in 1..5) { "difficulty must be 1..5." }
        }
    }

    data class HelpRequested(
        override val eventKey: String,
        val lessonId: String,
        val lessonRevision: Int,
        val skillIds: List<String>,
        val categoryIds: List<String>,
        val helpKind: HelpKind?,
        val choice: AdaptiveHelpChoice = AdaptiveHelpChoice.AUTHORED_HELP,
    ) : AdaptiveEvent {
        init {
            require(eventKey.isNotBlank()) { "eventKey cannot be blank." }
            require(lessonId.isNotBlank()) { "lessonId cannot be blank." }
            require(lessonRevision > 0) { "lessonRevision must be positive." }
        }
    }
}
