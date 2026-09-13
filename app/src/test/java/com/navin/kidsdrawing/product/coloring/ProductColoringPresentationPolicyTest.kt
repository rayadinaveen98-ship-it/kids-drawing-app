package com.navin.kidsdrawing.product.coloring

import com.navin.kidsdrawing.coloring.regions.GuidedColoringProgress
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode
import com.navin.kidsdrawing.lesson.model.ColoringStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductColoringPresentationPolicyTest {
    @Test
    fun prepared_guided_step_uses_authored_index_and_fill_instruction() {
        val progress = GuidedColoringProgress(
            currentStepIndex = 1,
            totalSteps = 3,
            currentStep = ColoringStep(
                id = "left_wing",
                regionIds = listOf("wing-left"),
            ),
            completedStepCount = 1,
            filledRegionIds = setOf("body"),
        )

        val presentation = ProductColoringPresentationPolicy.from(
            mode = ColoringSessionMode.COLOR_WITH_ME,
            progress = progress,
            preparedFillAvailable = true,
        )

        assertEquals("Step 2 of 3 · Left Wing", presentation.stepLabel)
        assertTrue(presentation.instruction.contains("select Fill"))
    }

    @Test
    fun legacy_freehand_guided_step_does_not_invent_three_fake_steps() {
        val progress = GuidedColoringProgress(
            currentStepIndex = 0,
            totalSteps = 1,
            currentStep = ColoringStep(
                id = "freehand_color",
                regionIds = emptyList(),
            ),
            completedStepCount = 0,
            filledRegionIds = emptySet(),
        )

        val presentation = ProductColoringPresentationPolicy.from(
            mode = ColoringSessionMode.COLOR_WITH_ME,
            progress = progress,
            preparedFillAvailable = false,
        )

        assertEquals("Step 1 of 1 · Freehand Color", presentation.stepLabel)
        assertTrue(presentation.instruction.contains("Brush"))
    }

    @Test
    fun guided_completion_is_derived_from_authored_progress() {
        val progress = GuidedColoringProgress(
            currentStepIndex = null,
            totalSteps = 2,
            currentStep = null,
            completedStepCount = 2,
            filledRegionIds = setOf("body", "wing"),
        )

        val presentation = ProductColoringPresentationPolicy.from(
            mode = ColoringSessionMode.COLOR_WITH_ME,
            progress = progress,
            preparedFillAvailable = true,
        )

        assertEquals("COLORING COMPLETE", presentation.eyebrow)
        assertEquals("2 of 2", presentation.stepLabel)
    }

    @Test
    fun color_myself_mentions_fill_only_when_regions_exist() {
        val prepared = ProductColoringPresentationPolicy.from(
            mode = ColoringSessionMode.COLOR_MYSELF,
            progress = null,
            preparedFillAvailable = true,
        )
        val freehand = ProductColoringPresentationPolicy.from(
            mode = ColoringSessionMode.COLOR_MYSELF,
            progress = null,
            preparedFillAvailable = false,
        )

        assertTrue(prepared.instruction.contains("Fill"))
        assertTrue(!freehand.instruction.contains("Fill"))
    }
}
