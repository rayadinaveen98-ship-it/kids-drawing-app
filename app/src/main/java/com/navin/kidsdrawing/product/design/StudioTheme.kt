package com.navin.kidsdrawing.product.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navin.kidsdrawing.product.profile.AgeBand

object StudioColors {
    val Paper50 = Color(0xFFFFFDF8)
    val Paper100 = Color(0xFFFAF6ED)
    val Ink900 = Color(0xFF242321)
    // Semantic hierarchy aliases intentionally reuse the locked palette; they do not add hues.
    val Ink800 = Ink900
    val Ink700 = Color(0xFF4E4A45)
    val Ink600 = Ink700
    // P6.4: >=4.5:1 on Paper100/white for normal supporting text.
    val Ink500 = Color(0xFF746E67)
    val Line200 = Color(0xFFE8E0D4)
    val Studio600 = Color(0xFF5C6F52)
    val Studio700 = Studio600
    // P6.4: darkened enough for normal text use on light product surfaces.
    val Studio500 = Color(0xFF64755C)
    val Studio100 = Color(0xFFEAF0E5)
    val Sun500 = Color(0xFFE9A94A)
    val Coral500 = Color(0xFFE47C68)
    val Sky500 = Color(0xFF6C9CB8)
    val Lavender500 = Color(0xFF9A83B8)
}

data class StudioDensityPolicy(
    val minimumTouchTarget: Dp,
    val cardPadding: Dp,
    val contentGap: Dp,
)

fun densityPolicyFor(ageBand: AgeBand?): StudioDensityPolicy = when (ageBand) {
    AgeBand.LITTLE_ARTIST -> StudioDensityPolicy(68.dp, 22.dp, 18.dp)
    AgeBand.CREATIVE_EXPLORER -> StudioDensityPolicy(64.dp, 20.dp, 16.dp)
    AgeBand.GROWING_ARTIST -> StudioDensityPolicy(58.dp, 18.dp, 14.dp)
    AgeBand.YOUNG_ARTIST -> StudioDensityPolicy(54.dp, 16.dp, 12.dp)
    null -> StudioDensityPolicy(64.dp, 20.dp, 16.dp)
}

private val StudioColorScheme = lightColorScheme(
    primary = StudioColors.Studio600,
    onPrimary = Color.White,
    primaryContainer = StudioColors.Studio100,
    onPrimaryContainer = StudioColors.Ink900,
    secondary = StudioColors.Sky500,
    tertiary = StudioColors.Lavender500,
    background = StudioColors.Paper50,
    onBackground = StudioColors.Ink900,
    surface = StudioColors.Paper100,
    onSurface = StudioColors.Ink900,
    surfaceVariant = StudioColors.Paper100,
    onSurfaceVariant = StudioColors.Ink700,
    outline = StudioColors.Line200,
)

private val StudioTypography = Typography(
    displaySmall = Typography().displaySmall.copy(
        fontSize = 36.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    headlineLarge = Typography().headlineLarge.copy(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineSmall = Typography().headlineSmall.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = Typography().titleLarge.copy(
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
)

@Composable
fun StudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = StudioTypography,
        shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp),
        ),
        content = content,
    )
}

@Composable
fun StudioPrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StudioColors.Studio600,
            contentColor = Color.White,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun StudioChoiceCard(
    title: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ageBand: AgeBand? = null,
) {
    val density = densityPolicyFor(ageBand)
    val selectionModifier = if (selected) {
        Modifier.semantics {
            this.selected = true
            stateDescription = "Selected"
        }
    } else {
        Modifier
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = density.minimumTouchTarget)
            .then(selectionModifier)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) StudioColors.Studio100 else StudioColors.Paper100,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) StudioColors.Studio600 else StudioColors.Line200,
        ),
    ) {
        Column(modifier = Modifier.padding(density.cardPadding)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = StudioColors.Ink900,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink700,
                )
            }
            if (selected) {
                Text(
                    text = "✓ Selected",
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Studio600,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
