package com.navin.kidsdrawing.product.lesson

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.drawing.ui.TeacherPlaybackOverlay
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.StudioPrimaryButton
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.profile.ChildProfile

@Composable
fun LessonPreviewScreen(
    profile: ChildProfile,
    recommendation: LessonRecommendation,
    packageData: LessonRuntimePackage?,
    onBegin: (TeachingMode, TeachingPace) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = lessonLayoutPolicyFor(profile.ageBand)
    val supportedModes = packageData?.lesson?.supportedModes.orEmpty().ifEmpty {
        listOf(recommendation.defaultMode)
    }
    var selectedModeName by rememberSaveable { mutableStateOf(recommendation.defaultMode.name) }
    var selectedPaceName by rememberSaveable { mutableStateOf(recommendation.defaultPace.name) }
    val selectedMode = runCatching { TeachingMode.valueOf(selectedModeName) }
        .getOrDefault(recommendation.defaultMode)
        .takeIf { it in supportedModes } ?: supportedModes.first()
    val selectedPace = runCatching { TeachingPace.valueOf(selectedPaceName) }
        .getOrDefault(recommendation.defaultPace)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = layout.horizontalGutter, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(layout.sectionGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .heightIn(min = layout.minimumControlHeight)
                        .semantics { contentDescription = "Back to studio home" },
                ) {
                    Text("← Studio", color = StudioColors.Ink700)
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = StudioColors.Studio100,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✦", color = StudioColors.Studio600)
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = StudioColors.Paper100,
                border = BorderStroke(1.dp, StudioColors.Line200),
            ) {
                Column(modifier = Modifier.padding(if (profile.ageBand.maxAge <= 7) 22.dp else 18.dp)) {
                    LessonPreviewArt(
                        packageData = packageData,
                        lessonTitle = recommendation.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (profile.ageBand.maxAge <= 7) 1.55f else 1.8f),
                    )
                    Text(
                        text = recommendation.title,
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.headlineLarge,
                        color = StudioColors.Ink900,
                    )
                    Text(
                        text = recommendation.summary,
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = StudioColors.Ink700,
                    )
                    Text(
                        text = buildString {
                            append("${recommendation.estimatedMinutes} min")
                            append(" · ")
                            append(if (recommendation.difficulty <= 1) "Easy start" else "Gentle challenge")
                            append(" · ${packageData?.lesson?.drawing?.steps?.size ?: 4} parts")
                        },
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioColors.Studio600,
                    )
                }
            }

            PreviewSection(
                title = "How should we learn?",
                subtitle = "Your usual style is already selected. You can change it for this drawing.",
            ) {
                supportedModes.forEach { mode ->
                    LessonOptionCard(
                        title = mode.previewTitle(),
                        subtitle = mode.previewSubtitle(),
                        selected = mode == selectedMode,
                        onClick = { selectedModeName = mode.name },
                        minimumHeight = layout.optionCardMinHeight,
                    )
                }
            }

            PreviewSection(
                title = "Drawing speed",
                subtitle = "This changes how fast the teacher draws, not the shape itself.",
            ) {
                TeachingPace.entries.chunked(2).forEach { rowPaces ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowPaces.forEach { pace ->
                            LessonOptionCard(
                                title = pace.previewTitle(),
                                subtitle = "${pace.multiplier}×",
                                selected = pace == selectedPace,
                                onClick = { selectedPaceName = pace.name },
                                modifier = Modifier.weight(1f),
                                minimumHeight = layout.optionCardMinHeight,
                            )
                        }
                        if (rowPaces.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = StudioColors.Studio100,
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompanionPreviewFace()
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(
                            text = "We’ll take it one part at a time.",
                            style = MaterialTheme.typography.titleLarge,
                            color = StudioColors.Ink900,
                        )
                        Text(
                            text = if (profile.narrationPreference.name == "TEXT_ONLY") {
                                "Everything you need will stay on screen."
                            } else {
                                "You can follow the words and the drawing together."
                            },
                            modifier = Modifier.padding(top = 3.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioColors.Ink700,
                        )
                    }
                }
            }

            StudioPrimaryButton(
                text = "Start drawing",
                onClick = { onBegin(selectedMode, selectedPace) },
                modifier = Modifier
                    .heightIn(min = layout.minimumControlHeight)
                    .semantics {
                        contentDescription = "Start ${recommendation.title} with ${selectedMode.previewTitle()}"
                    },
            )
            Spacer(modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun PreviewSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = StudioColors.Ink900)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = StudioColors.Ink500)
        content()
    }
}

@Composable
private fun LessonOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minimumHeight: Dp = 62.dp,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minimumHeight)
            .semantics { contentDescription = "$title. $subtitle${if (selected) ". Selected" else ""}" }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) StudioColors.Studio100 else StudioColors.Paper100,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) StudioColors.Studio600 else StudioColors.Line200,
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = StudioColors.Ink900)
            Text(
                subtitle,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = StudioColors.Ink700,
            )
        }
    }
}

@Composable
internal fun LessonPreviewArt(
    packageData: LessonRuntimePackage?,
    lessonTitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.semantics { contentDescription = "$lessonTitle drawing preview" },
        shape = RoundedCornerShape(22.dp),
        color = StudioColors.Studio100.copy(alpha = 0.55f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (packageData == null) {
                Text(
                    text = "✦",
                    style = MaterialTheme.typography.headlineLarge,
                    color = StudioColors.Studio600,
                )
            } else {
                TeacherPlaybackOverlay(
                    strokes = ProductLessonReferencePolicy.allTeacherStrokes(packageData),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    documentSize = DocumentSize(
                        packageData.lesson.canvas.width.toFloat(),
                        packageData.lesson.canvas.height.toFloat(),
                    ),
                    opacityMultiplier = 0.95f,
                )
            }
        }
    }
}

@Composable
internal fun CompanionPreviewFace(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(54.dp),
        shape = RoundedCornerShape(18.dp),
        color = StudioColors.Sun500.copy(alpha = 0.18f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = StudioColors.Ink900) {}
                Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = StudioColors.Ink900) {}
            }
        }
    }
}

private fun TeachingMode.previewTitle(): String = when (this) {
    TeachingMode.DRAW_WITH_ME -> "Draw With Me"
    TeachingMode.WATCH_THEN_DRAW -> "Watch Then Draw"
    TeachingMode.TRACE_AND_LEARN -> "Trace & Learn"
}

private fun TeachingMode.previewSubtitle(): String = when (this) {
    TeachingMode.DRAW_WITH_ME -> "Watch one part, then draw that part"
    TeachingMode.WATCH_THEN_DRAW -> "Watch the full drawing first, then try it"
    TeachingMode.TRACE_AND_LEARN -> "Learn with a gentle guide on your turn"
}

private fun TeachingPace.previewTitle(): String = when (this) {
    TeachingPace.EXTRA_SLOW -> "Extra slow"
    TeachingPace.SLOW -> "Slow"
    TeachingPace.NORMAL -> "Normal"
    TeachingPace.FAST -> "Fast"
    TeachingPace.VERY_FAST -> "Very fast"
}
