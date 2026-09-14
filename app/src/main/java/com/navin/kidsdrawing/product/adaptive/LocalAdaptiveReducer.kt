package com.navin.kidsdrawing.product.adaptive

/** Pure deterministic reducer. The same state + event always yields the same state. */
object LocalAdaptiveReducer {
    fun reduce(state: LocalAdaptiveState, event: AdaptiveEvent): LocalAdaptiveState {
        if (event.eventKey in state.processedEventKeys) return state

        val reduced = when (event) {
            is AdaptiveEvent.LessonCompleted -> reduceCompletion(state, event)
            is AdaptiveEvent.HelpRequested -> reduceHelp(state, event)
        }
        return reduced.copy(
            revision = state.revision + 1L,
            processedEventKeys = (state.processedEventKeys + event.eventKey)
                .takeLast(LocalAdaptiveState.MAX_PROCESSED_EVENT_KEYS),
        )
    }

    private fun reduceCompletion(
        state: LocalAdaptiveState,
        event: AdaptiveEvent.LessonCompleted,
    ): LocalAdaptiveState {
        val identity = AdaptiveLessonIdentity(event.lessonId, event.lessonRevision)
        val alreadyCompleted = identity in state.completedLessons
        val canRecordFirstCompletion = !alreadyCompleted &&
            state.completedLessons.size < LocalAdaptiveState.MAX_COMPLETED_LESSONS
        val completed = if (canRecordFirstCompletion) {
            (state.completedLessons + identity)
                .sortedWith(compareBy({ it.lessonId }, { it.revision }))
        } else {
            state.completedLessons
        }

        val skillCounts = if (canRecordFirstCompletion) {
            incrementBounded(
                existing = state.skillExposureCounts,
                keys = event.skillIds,
                maxEntries = LocalAdaptiveState.MAX_SKILL_COUNTERS,
            )
        } else {
            state.skillExposureCounts
        }

        val recent = (state.recentCompletions.filterNot { it == identity } + identity)
            .takeLast(LocalAdaptiveState.MAX_RECENT_COMPLETIONS)

        return state.copy(
            completedLessons = completed,
            skillExposureCounts = skillCounts,
            recentCompletions = recent,
        )
    }

    private fun reduceHelp(
        state: LocalAdaptiveState,
        event: AdaptiveEvent.HelpRequested,
    ): LocalAdaptiveState {
        val keys = buildList {
            event.skillIds.forEach { skillId ->
                skillId.trim().takeIf(String::isNotBlank)?.let { add("skill:$it") }
            }
            event.categoryIds.forEach { categoryId ->
                categoryId.trim().takeIf(String::isNotBlank)?.let { add("category:$it") }
            }
            event.helpKind?.let { add("kind:${it.name}") }
            add("choice:${event.choice.name}")
        }
        return state.copy(
            helpRequestCounts = incrementBounded(
                existing = state.helpRequestCounts,
                keys = keys,
                maxEntries = LocalAdaptiveState.MAX_HELP_COUNTERS,
            ),
        )
    }

    private fun incrementBounded(
        existing: Map<String, Int>,
        keys: List<String>,
        maxEntries: Int,
    ): Map<String, Int> {
        val result = existing.toMutableMap()
        keys.asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .sorted()
            .forEach { key ->
                if (key !in result && result.size >= maxEntries) return@forEach
                val previous = result[key] ?: 0
                result[key] = (previous + 1).coerceAtMost(LocalAdaptiveState.MAX_COUNTER_VALUE)
            }
        return result.toSortedMap()
    }
}
