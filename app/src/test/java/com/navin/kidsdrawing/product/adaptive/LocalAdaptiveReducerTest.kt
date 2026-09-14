package com.navin.kidsdrawing.product.adaptive

import com.navin.kidsdrawing.lesson.model.HelpKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAdaptiveReducerTest {
    @Test
    fun duplicateCompletionEventIsIdempotentAndFirstCompletionSkillsCountOnce() {
        val event = completion(
            eventKey = "complete:session-a",
            lessonId = "cute-cat",
            skills = listOf("shape.combine", "line.curve"),
        )

        val once = LocalAdaptiveReducer.reduce(LocalAdaptiveState.empty(), event)
        val duplicate = LocalAdaptiveReducer.reduce(once, event)
        val genuineRepeat = LocalAdaptiveReducer.reduce(
            duplicate,
            event.copy(eventKey = "complete:session-b"),
        )

        assertEquals(once, duplicate)
        assertEquals(1, once.skillExposureCounts["shape.combine"])
        assertEquals(1, once.skillExposureCounts["line.curve"])
        assertEquals(1, genuineRepeat.skillExposureCounts["shape.combine"])
        assertEquals(1, genuineRepeat.completedLessons.size)
        assertEquals(2L, genuineRepeat.revision)
    }

    @Test
    fun sameInputAlwaysProducesSameOutput() {
        val initial = LocalAdaptiveState.empty()
        val event = completion(
            eventKey = "complete:deterministic",
            lessonId = "simple-rocket",
            skills = listOf("shape.combine", "composition.centering"),
        )

        assertEquals(
            LocalAdaptiveReducer.reduce(initial, event),
            LocalAdaptiveReducer.reduce(initial, event),
        )
    }

    @Test
    fun recentCompletionsAndProcessedKeysStayBounded() {
        var state = LocalAdaptiveState.empty()
        repeat(160) { index ->
            state = LocalAdaptiveReducer.reduce(
                state,
                completion(
                    eventKey = "complete:$index",
                    lessonId = "lesson-$index",
                    skills = emptyList(),
                ),
            )
        }

        assertEquals(LocalAdaptiveState.MAX_RECENT_COMPLETIONS, state.recentCompletions.size)
        assertEquals(LocalAdaptiveState.MAX_COMPLETED_LESSONS, state.completedLessons.size)
        assertEquals(LocalAdaptiveState.MAX_PROCESSED_EVENT_KEYS, state.processedEventKeys.size)
    }

    @Test
    fun helpRequestsAreBoundedCappedAndContainOnlyAuthoredContextKeys() {
        var state = LocalAdaptiveState.empty()
        repeat(140) { index ->
            state = LocalAdaptiveReducer.reduce(
                state,
                AdaptiveEvent.HelpRequested(
                    eventKey = "help:$index",
                    lessonId = "one-point-room",
                    lessonRevision = 1,
                    skillIds = listOf("perspective.one_point"),
                    categoryIds = listOf("places.rooms"),
                    helpKind = HelpKind.DIRECTION_ANCHORS,
                ),
            )
        }

        assertEquals(LocalAdaptiveState.MAX_COUNTER_VALUE, state.helpRequestCounts["skill:perspective.one_point"])
        assertEquals(LocalAdaptiveState.MAX_COUNTER_VALUE, state.helpRequestCounts["category:places.rooms"])
        assertEquals(LocalAdaptiveState.MAX_COUNTER_VALUE, state.helpRequestCounts["kind:DIRECTION_ANCHORS"])
        assertTrue(state.helpRequestCounts.size <= LocalAdaptiveState.MAX_HELP_COUNTERS)
        assertFalse(state.helpRequestCounts.keys.any { key ->
            listOf("stroke", "coordinate", "score", "ability", "duration", "latency").any {
                forbidden -> key.contains(forbidden, ignoreCase = true)
            }
        })
    }

    @Test
    fun duplicateHelpEventDoesNotInflateSummary() {
        val event = AdaptiveEvent.HelpRequested(
            eventKey = "help:stable",
            lessonId = "face-and-expressions",
            lessonRevision = 1,
            skillIds = listOf("face.landmarks"),
            categoryIds = listOf("characters"),
            helpKind = HelpKind.VISUAL_GUIDE,
        )

        val once = LocalAdaptiveReducer.reduce(LocalAdaptiveState.empty(), event)
        val duplicate = LocalAdaptiveReducer.reduce(once, event)

        assertEquals(once, duplicate)
        assertEquals(1, once.helpRequestCounts["skill:face.landmarks"])
    }

    private fun completion(
        eventKey: String,
        lessonId: String,
        skills: List<String>,
    ) = AdaptiveEvent.LessonCompleted(
        eventKey = eventKey,
        lessonId = lessonId,
        lessonRevision = 1,
        skillIds = skills,
        categoryIds = listOf("test.category"),
        journeyIds = emptyList(),
        difficulty = 2,
    )
}
