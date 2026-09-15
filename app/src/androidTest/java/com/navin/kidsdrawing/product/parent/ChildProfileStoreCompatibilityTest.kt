package com.navin.kidsdrawing.product.parent

import androidx.test.platform.app.InstrumentationRegistry
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.ChildProfileStore
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ChildProfileStoreCompatibilityTest {
    @Test
    fun accepted_phase5_profile_shape_round_trips_without_migration() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = ChildProfileStore(context)
        val profile = ChildProfile(
            nickname = "Mira",
            ageBand = AgeBand.CREATIVE_EXPLORER,
            teachingMode = TeachingMode.DRAW_WITH_ME,
            pace = TeachingPace.NORMAL,
            interests = setOf(ChildInterest.ANIMALS, ChildInterest.NATURE),
            handedness = Handedness.RIGHT,
            narrationPreference = NarrationPreference.VOICE_AND_TEXT,
        )

        store.saveCompletedProfile(profile)

        assertEquals(profile, store.loadCompletedProfile())
    }
}
