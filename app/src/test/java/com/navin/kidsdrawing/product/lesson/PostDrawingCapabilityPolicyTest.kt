package com.navin.kidsdrawing.product.lesson

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostDrawingCapabilityPolicyTest {
    @Test
    fun lessonWithAuthoredColoringMayOfferColoringChoices() {
        val result = postDrawingCapabilityPolicy(coloringAvailable = true)

        assertTrue(result.showColoringChoices)
        assertTrue(result.guidanceCopy.contains("add color", ignoreCase = true))
    }

    @Test
    fun lessonWithoutAuthoredColoringHidesColoringChoices() {
        val result = postDrawingCapabilityPolicy(coloringAvailable = false)

        assertFalse(result.showColoringChoices)
        assertFalse(result.guidanceCopy.contains("add color", ignoreCase = true))
    }
}
