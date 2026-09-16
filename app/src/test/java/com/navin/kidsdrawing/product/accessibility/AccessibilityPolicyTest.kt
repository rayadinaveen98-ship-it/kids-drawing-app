package com.navin.kidsdrawing.product.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityPolicyTest {
    @Test
    fun standardBandIsBelowLargeThreshold() {
        assertEquals(
            AccessibilityTextScaleBand.STANDARD,
            AccessibilityPolicy.textScaleBand(1.29f),
        )
    }

    @Test
    fun largeBandStartsAtOnePointThree() {
        assertEquals(
            AccessibilityTextScaleBand.LARGE,
            AccessibilityPolicy.textScaleBand(1.30f),
        )
    }

    @Test
    fun extraLargeBandStartsAtOnePointSix() {
        assertEquals(
            AccessibilityTextScaleBand.EXTRA_LARGE,
            AccessibilityPolicy.textScaleBand(1.60f),
        )
    }

    @Test
    fun invalidFontScaleFallsBackToStandard() {
        assertEquals(
            AccessibilityTextScaleBand.STANDARD,
            AccessibilityPolicy.textScaleBand(Float.NaN),
        )
        assertEquals(
            AccessibilityTextScaleBand.STANDARD,
            AccessibilityPolicy.textScaleBand(0f),
        )
    }

    @Test
    fun largeTextPrefersSingleColumnAndAvoidsFixedTwoColumnCards() {
        val policy = AccessibilityPolicy.layout(1.45f)
        assertTrue(policy.preferSingleColumnActions)
        assertTrue(policy.avoidFixedTwoColumnCards)
    }

    @Test
    fun standardTextKeepsNormalLayoutDensity() {
        val policy = AccessibilityPolicy.layout(1f)
        assertFalse(policy.preferSingleColumnActions)
        assertFalse(policy.avoidFixedTwoColumnCards)
    }

    @Test
    fun authoredPaletteColorsHaveHumanReadableNames() {
        assertEquals("Charcoal", accessibleColorName(0xFF242321.toInt()))
        assertEquals("Leaf green", accessibleColorName(0xFF5C8D63.toInt()))
        assertEquals("Purple", accessibleColorName(0xFF9A83B8.toInt()))
    }

    @Test
    fun unknownColorHasHonestFallbackName() {
        assertEquals("Drawing color", accessibleColorName(0xFF123456.toInt()))
    }
}
