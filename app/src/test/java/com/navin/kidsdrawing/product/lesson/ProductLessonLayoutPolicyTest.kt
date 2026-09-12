package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.product.profile.AgeBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductLessonLayoutPolicyTest {
    @Test
    fun little_artist_uses_larger_targets_and_fewer_compact_columns() {
        val little = lessonLayoutPolicyFor(AgeBand.LITTLE_ARTIST)
        val young = lessonLayoutPolicyFor(AgeBand.YOUNG_ARTIST)

        assertTrue(little.minimumControlHeight > young.minimumControlHeight)
        assertTrue(little.optionCardMinHeight > young.optionCardMinHeight)
        assertEquals(2, little.maxCompactActionColumns)
        assertEquals(3, young.maxCompactActionColumns)
    }

    @Test
    fun all_age_bands_keep_child_safe_control_targets() {
        AgeBand.entries.forEach { ageBand ->
            assertTrue(lessonLayoutPolicyFor(ageBand).minimumControlHeight.value >= 54f)
        }
    }
}
