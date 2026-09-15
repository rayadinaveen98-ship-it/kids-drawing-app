package com.navin.kidsdrawing.product.parent

fun interface ParentElapsedClock {
    fun nowMillis(): Long
}

class ParentAccessSession(
    private val clock: ParentElapsedClock,
    private val holdDurationMillis: Long = HOLD_DURATION_MILLIS,
    private val sessionDurationMillis: Long = SESSION_DURATION_MILLIS,
    private val backgroundGraceMillis: Long = BACKGROUND_GRACE_MILLIS,
) {
    private var holdStartedAtMillis: Long? = null
    private var sessionStartedAtMillis: Long? = null
    private var backgroundStartedAtMillis: Long? = null

    fun beginHold() {
        holdStartedAtMillis = clock.nowMillis()
    }

    fun cancelHold() {
        holdStartedAtMillis = null
    }

    fun holdProgress(): Float {
        val startedAt = holdStartedAtMillis ?: return 0f
        if (holdDurationMillis <= 0L) return 1f
        val elapsed = (clock.nowMillis() - startedAt).coerceAtLeast(0L)
        return (elapsed.toDouble() / holdDurationMillis.toDouble())
            .coerceIn(0.0, 1.0)
            .toFloat()
    }

    fun completeHoldIfEligible(): Boolean {
        val startedAt = holdStartedAtMillis ?: return false
        val elapsed = (clock.nowMillis() - startedAt).coerceAtLeast(0L)
        if (elapsed < holdDurationMillis) return false
        activateSession()
        return true
    }

    fun completeAccessibleConfirmation() {
        activateSession()
    }

    fun isSessionActive(): Boolean {
        val startedAt = sessionStartedAtMillis ?: return false
        val elapsed = (clock.nowMillis() - startedAt).coerceAtLeast(0L)
        if (elapsed >= sessionDurationMillis) {
            invalidate()
            return false
        }
        return true
    }

    fun remainingSessionMillis(): Long {
        val startedAt = sessionStartedAtMillis ?: return 0L
        val elapsed = (clock.nowMillis() - startedAt).coerceAtLeast(0L)
        return (sessionDurationMillis - elapsed).coerceAtLeast(0L)
    }

    fun onBackgrounded() {
        if (!isSessionActive()) return
        if (backgroundStartedAtMillis == null) {
            backgroundStartedAtMillis = clock.nowMillis()
        }
    }

    fun onForegrounded() {
        val backgroundedAt = backgroundStartedAtMillis ?: return
        val elapsed = (clock.nowMillis() - backgroundedAt).coerceAtLeast(0L)
        backgroundStartedAtMillis = null
        if (elapsed > backgroundGraceMillis) {
            invalidate()
        } else {
            isSessionActive()
        }
    }

    fun invalidateForChildReturn() = invalidate()

    fun invalidateForExternalNavigation() = invalidate()

    fun invalidate() {
        holdStartedAtMillis = null
        sessionStartedAtMillis = null
        backgroundStartedAtMillis = null
    }

    private fun activateSession() {
        sessionStartedAtMillis = clock.nowMillis()
        holdStartedAtMillis = null
        backgroundStartedAtMillis = null
    }

    companion object {
        const val HOLD_DURATION_MILLIS = 2_500L
        const val SESSION_DURATION_MILLIS = 5 * 60 * 1_000L
        const val BACKGROUND_GRACE_MILLIS = 30_000L
    }
}
