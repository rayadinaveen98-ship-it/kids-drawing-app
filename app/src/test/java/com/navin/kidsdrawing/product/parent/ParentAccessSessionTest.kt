package com.navin.kidsdrawing.product.parent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentAccessSessionTest {
    private class FakeClock(var now: Long = 1_000L) : ParentElapsedClock {
        override fun nowMillis(): Long = now
        fun advanceBy(millis: Long) { now += millis }
    }

    @Test
    fun `hold cannot complete before 2_5 seconds`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)

        session.beginHold()
        clock.advanceBy(2_499L)

        assertFalse(session.completeHoldIfEligible())
        assertFalse(session.isSessionActive())
    }

    @Test
    fun `eligible hold activates parent session`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)

        session.beginHold()
        clock.advanceBy(2_500L)

        assertTrue(session.completeHoldIfEligible())
        assertTrue(session.isSessionActive())
    }

    @Test
    fun `cancelled hold resets progress and never unlocks`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)

        session.beginHold()
        clock.advanceBy(1_250L)
        assertEquals(0.5f, session.holdProgress(), 0.001f)

        session.cancelHold()
        clock.advanceBy(10_000L)

        assertEquals(0f, session.holdProgress(), 0f)
        assertFalse(session.completeHoldIfEligible())
        assertFalse(session.isSessionActive())
    }

    @Test
    fun `accessible confirmation activates same in-memory session`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)

        session.completeAccessibleConfirmation()

        assertTrue(session.isSessionActive())
    }

    @Test
    fun `session expires at five minutes`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)
        session.completeAccessibleConfirmation()

        clock.advanceBy(ParentAccessSession.SESSION_DURATION_MILLIS - 1L)
        assertTrue(session.isSessionActive())

        clock.advanceBy(1L)
        assertFalse(session.isSessionActive())
    }

    @Test
    fun `background up to thirty seconds preserves active session`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)
        session.completeAccessibleConfirmation()
        session.onBackgrounded()

        clock.advanceBy(ParentAccessSession.BACKGROUND_GRACE_MILLIS)
        session.onForegrounded()

        assertTrue(session.isSessionActive())
    }

    @Test
    fun `background longer than thirty seconds invalidates session`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)
        session.completeAccessibleConfirmation()
        session.onBackgrounded()

        clock.advanceBy(ParentAccessSession.BACKGROUND_GRACE_MILLIS + 1L)
        session.onForegrounded()

        assertFalse(session.isSessionActive())
    }

    @Test
    fun `returning to child mode invalidates session`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)
        session.completeAccessibleConfirmation()

        session.invalidateForChildReturn()

        assertFalse(session.isSessionActive())
    }

    @Test
    fun `external navigation invalidates session`() {
        val clock = FakeClock()
        val session = ParentAccessSession(clock)
        session.completeAccessibleConfirmation()

        session.invalidateForExternalNavigation()

        assertFalse(session.isSessionActive())
    }

    @Test
    fun `fresh controller starts locked and models process recreation safely`() {
        val clock = FakeClock()
        val oldSession = ParentAccessSession(clock)
        oldSession.completeAccessibleConfirmation()
        assertTrue(oldSession.isSessionActive())

        val recreatedSession = ParentAccessSession(clock)

        assertFalse(recreatedSession.isSessionActive())
    }
}
