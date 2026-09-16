package com.navin.kidsdrawing.product.device

enum class DeviceWidthBand {
    COMPACT,
    EXPANDED,
}

enum class DeviceHeightBand {
    CONSTRAINED,
    REGULAR,
}

data class DeviceLayoutPolicy(
    val widthBand: DeviceWidthBand,
    val heightBand: DeviceHeightBand,
    val maxGeneralContentWidthDp: Int?,
    val preferBoundedArtControls: Boolean,
    val preferExpandedGalleryCards: Boolean,
)

/**
 * Pure window-geometry policy for P6.5.
 *
 * Unknown/invalid dimensions intentionally fall back to the more conservative compact/constrained
 * behavior. This policy never branches on manufacturer/model and does not replace P6.4 font-scale
 * rules; callers compose both policies.
 */
object DeviceLayoutPolicyResolver {
    const val EXPANDED_WIDTH_THRESHOLD_DP = 600
    const val REGULAR_HEIGHT_THRESHOLD_DP = 600
    const val EXPANDED_GENERAL_CONTENT_MAX_DP = 960

    fun resolve(
        widthDp: Int,
        heightDp: Int,
    ): DeviceLayoutPolicy {
        val widthBand = if (widthDp >= EXPANDED_WIDTH_THRESHOLD_DP) {
            DeviceWidthBand.EXPANDED
        } else {
            DeviceWidthBand.COMPACT
        }
        val heightBand = if (heightDp >= REGULAR_HEIGHT_THRESHOLD_DP) {
            DeviceHeightBand.REGULAR
        } else {
            DeviceHeightBand.CONSTRAINED
        }
        return DeviceLayoutPolicy(
            widthBand = widthBand,
            heightBand = heightBand,
            maxGeneralContentWidthDp = if (widthBand == DeviceWidthBand.EXPANDED) {
                EXPANDED_GENERAL_CONTENT_MAX_DP
            } else {
                null
            },
            preferBoundedArtControls = heightBand == DeviceHeightBand.CONSTRAINED,
            preferExpandedGalleryCards = widthBand == DeviceWidthBand.EXPANDED,
        )
    }
}
