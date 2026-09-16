package com.navin.kidsdrawing.product.device

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceLayoutPolicyTest {
    @Test
    fun widthBoundaryIsCompactBelowSixHundred() {
        val policy = DeviceLayoutPolicyResolver.resolve(widthDp = 599, heightDp = 700)
        assertEquals(DeviceWidthBand.COMPACT, policy.widthBand)
        assertNull(policy.maxGeneralContentWidthDp)
        assertFalse(policy.preferExpandedGalleryCards)
    }

    @Test
    fun widthBoundaryIsExpandedAtSixHundred() {
        val policy = DeviceLayoutPolicyResolver.resolve(widthDp = 600, heightDp = 700)
        assertEquals(DeviceWidthBand.EXPANDED, policy.widthBand)
        assertEquals(960, policy.maxGeneralContentWidthDp)
        assertTrue(policy.preferExpandedGalleryCards)
    }

    @Test
    fun heightBoundaryIsConstrainedBelowSixHundred() {
        val policy = DeviceLayoutPolicyResolver.resolve(widthDp = 360, heightDp = 599)
        assertEquals(DeviceHeightBand.CONSTRAINED, policy.heightBand)
        assertTrue(policy.preferBoundedArtControls)
    }

    @Test
    fun heightBoundaryIsRegularAtSixHundred() {
        val policy = DeviceLayoutPolicyResolver.resolve(widthDp = 360, heightDp = 600)
        assertEquals(DeviceHeightBand.REGULAR, policy.heightBand)
        assertFalse(policy.preferBoundedArtControls)
    }

    @Test
    fun unknownDimensionsChooseConservativePolicy() {
        val policy = DeviceLayoutPolicyResolver.resolve(widthDp = 0, heightDp = 0)
        assertEquals(DeviceWidthBand.COMPACT, policy.widthBand)
        assertEquals(DeviceHeightBand.CONSTRAINED, policy.heightBand)
        assertNull(policy.maxGeneralContentWidthDp)
        assertTrue(policy.preferBoundedArtControls)
    }

    @Test
    fun expandedConstrainedWindowCombinesIndependentBands() {
        val policy = DeviceLayoutPolicyResolver.resolve(widthDp = 900, heightDp = 500)
        assertEquals(DeviceWidthBand.EXPANDED, policy.widthBand)
        assertEquals(DeviceHeightBand.CONSTRAINED, policy.heightBand)
        assertEquals(960, policy.maxGeneralContentWidthDp)
        assertTrue(policy.preferBoundedArtControls)
        assertTrue(policy.preferExpandedGalleryCards)
    }
}
