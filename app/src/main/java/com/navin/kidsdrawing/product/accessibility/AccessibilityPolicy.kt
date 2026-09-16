package com.navin.kidsdrawing.product.accessibility

enum class AccessibilityTextScaleBand {
    STANDARD,
    LARGE,
    EXTRA_LARGE,
}

data class AccessibilityLayoutPolicy(
    val textScaleBand: AccessibilityTextScaleBand,
    val preferSingleColumnActions: Boolean,
    val avoidFixedTwoColumnCards: Boolean,
)

object AccessibilityPolicy {
    const val LARGE_TEXT_SCALE = 1.30f
    const val EXTRA_LARGE_TEXT_SCALE = 1.60f

    fun textScaleBand(fontScale: Float): AccessibilityTextScaleBand {
        val safeScale = fontScale.takeIf { it.isFinite() && it > 0f } ?: 1f
        return when {
            safeScale >= EXTRA_LARGE_TEXT_SCALE -> AccessibilityTextScaleBand.EXTRA_LARGE
            safeScale >= LARGE_TEXT_SCALE -> AccessibilityTextScaleBand.LARGE
            else -> AccessibilityTextScaleBand.STANDARD
        }
    }

    fun layout(fontScale: Float): AccessibilityLayoutPolicy = when (textScaleBand(fontScale)) {
        AccessibilityTextScaleBand.STANDARD -> AccessibilityLayoutPolicy(
            textScaleBand = AccessibilityTextScaleBand.STANDARD,
            preferSingleColumnActions = false,
            avoidFixedTwoColumnCards = false,
        )
        AccessibilityTextScaleBand.LARGE -> AccessibilityLayoutPolicy(
            textScaleBand = AccessibilityTextScaleBand.LARGE,
            preferSingleColumnActions = true,
            avoidFixedTwoColumnCards = true,
        )
        AccessibilityTextScaleBand.EXTRA_LARGE -> AccessibilityLayoutPolicy(
            textScaleBand = AccessibilityTextScaleBand.EXTRA_LARGE,
            preferSingleColumnActions = true,
            avoidFixedTwoColumnCards = true,
        )
    }
}

/** Human-readable names for the authored drawing/coloring palettes. */
fun accessibleColorName(argb: Int): String = when (argb) {
    0xFF242321.toInt() -> "Charcoal"
    0xFFEF6C68.toInt() -> "Coral"
    0xFFF2A93B.toInt() -> "Warm yellow"
    0xFF5C8D63.toInt() -> "Leaf green"
    0xFF3D7CC9.toInt() -> "Blue"
    0xFF6B5AA6.toInt() -> "Violet"
    0xFFB65B8A.toInt() -> "Berry"
    0xFF8A5C3B.toInt() -> "Warm brown"
    0xFFE47C68.toInt() -> "Coral"
    0xFFE9A94A.toInt() -> "Orange"
    0xFFF4D35E.toInt() -> "Yellow"
    0xFF78A86B.toInt() -> "Green"
    0xFF6C9CB8.toInt() -> "Blue"
    0xFF9A83B8.toInt() -> "Purple"
    else -> "Drawing color"
}
