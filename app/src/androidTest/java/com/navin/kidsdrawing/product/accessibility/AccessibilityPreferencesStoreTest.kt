package com.navin.kidsdrawing.product.accessibility

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityPreferencesStoreTest {
    @Test
    fun reduceMotionRoundTripsLocally() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val store = AccessibilityPreferencesStore(context)

        try {
            store.setReduceMotion(false)
            assertFalse(store.load().reduceMotion)

            store.setReduceMotion(true)
            assertTrue(store.load().reduceMotion)
        } finally {
            store.setReduceMotion(false)
        }
    }
}
