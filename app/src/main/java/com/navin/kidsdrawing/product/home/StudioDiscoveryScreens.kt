package com.navin.kidsdrawing.product.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.product.design.StudioColors

@Composable
fun StudioCategoryScreen(
    category: StudioCategory?,
    onOpenLesson: (LessonRecommendation) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiscoveryListScreen(
        title = category?.title ?: "Explore Lessons",
        subtitle = category?.let {
            if (it.lessonCount == 1) "1 lesson ready in your studio" else "${it.lessonCount} lessons ready in your studio"
        } ?: "This collection is not available right now.",
        lessons = category?.lessons.orEmpty(),
        onOpenLesson = onOpenLesson,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun StudioJourneyScreen(
    journey: StudioJourney?,
    onOpenLesson: (LessonRecommendation) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiscoveryListScreen(
        title = journey?.title ?: "Art Journey",
        subtitle = journey?.progressLabel ?: "This journey is not available right now.",
        lessons = journey?.lessons.orEmpty(),
        onOpenLesson = onOpenLesson,
        onBack = onBack,
        modifier = modifier,
        showSequence = true,
        activeLessonId = journey?.activeLessonId,
    )
}

@Composable
private fun DiscoveryListScreen(
    title: String,
    subtitle: String,
    lessons: List<LessonRecommendation>,
    onOpenLesson: (LessonRecommendation) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
    showSequence: Boolean = false,
    activeLessonId: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text("Back to studio")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = StudioColors.Ink900,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )

            if (lessons.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = StudioColors.Paper100,
                    border = BorderStroke(1.dp, StudioColors.Line200),
                ) {
                    Text(
                        text = "Nothing to choose here yet. Your other studio lessons are still ready.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = StudioColors.Ink700,
                    )
                }
            } else {
                lessons.forEachIndexed { index, recommendation ->
                    DiscoveryLessonCard(
                        recommendation = recommendation,
                        leadingLabel = if (showSequence) "${index + 1}" else null,
                        statusLabel = if (activeLessonId == recommendation.lessonId) "In progress" else null,
                        onClick = { onOpenLesson(recommendation) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveryLessonCard(
    recommendation: LessonRecommendation,
    leadingLabel: String?,
    statusLabel: String?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp)
            .semantics { contentDescription = "Open ${recommendation.title}" }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingLabel != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioColors.Studio100,
                ) {
                    Text(
                        text = leadingLabel,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StudioColors.Studio600,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (leadingLabel != null) 14.dp else 0.dp),
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = StudioColors.Ink900,
                )
                Text(
                    text = recommendation.summary,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink700,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildList {
                        add("${recommendation.estimatedMinutes} min")
                        add("Level ${recommendation.difficulty}")
                        statusLabel?.let(::add)
                    }.joinToString(" · "),
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (statusLabel != null) StudioColors.Studio600 else StudioColors.Ink500,
                )
            }
        }
    }
}
