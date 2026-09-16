package com.navin.kidsdrawing.product.device

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Compose adapter for the pure P6.5 window-geometry policy.
 *
 * Product surfaces consume the resolved policy rather than branching directly on device model or
 * repeating raw configuration thresholds. P6.4 font-scale/accessibility policy remains separate
 * and is composed by each screen.
 */
@Composable
fun currentDeviceLayoutPolicy(): DeviceLayoutPolicy {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    return remember(widthDp, heightDp) {
        DeviceLayoutPolicyResolver.resolve(widthDp = widthDp, heightDp = heightDp)
    }
}
