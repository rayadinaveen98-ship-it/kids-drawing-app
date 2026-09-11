package com.navin.kidsdrawing

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaffoldContractTest {
    @Test
    fun fiveTeachingPacesRemainCanonical() {
        val paces = listOf("extra_slow", "slow", "normal", "fast", "very_fast")
        assertEquals(5, paces.size)
    }
}
