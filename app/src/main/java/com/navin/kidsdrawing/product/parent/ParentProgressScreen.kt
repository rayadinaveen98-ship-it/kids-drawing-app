package com.navin.kidsdrawing.product.parent

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.profile.ChildProfile
import java.text.DateFormat
import java.util.Date

private sealed interface ParentProgressUiState {
    data object Loading : ParentProgressUiState
    data class Ready(val model: ParentProgressModel) : ParentProgressUiState
    data object Failed : ParentProgressUiState
}

@Composable
fun ParentProgressRoute(
    profile: ChildProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember(context) { ParentProgressRepository(context) }
    var state: ParentProgressUiState by remember(profile) { mutableStateOf(ParentProgressUiState.Loading) }

    LaunchedEffect(profile, repository) {
        state = runCatching { ParentProgressUiState.Ready(repository.load(profile)) }
            .getOrDefault(ParentProgressUiState.Failed)
    }

    ParentProgressScaffold(
        title = "Learning",
        onBack = onBack,
        modifier = modifier,
    ) {
        when (val current = state) {
            ParentProgressUiState.Loading -> {
                CircularProgressIndicator(color = StudioColors.Studio600)
                Text(
                    text = "Reading local learning activity…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StudioColors.Ink700,
                )
            }
            ParentProgressUiState.Failed -> ParentProgressInfoCard(
                title = "Learning view unavailable",
                body = "The parent learning summary could not open right now. No artwork, lesson progress or profile data was changed.",
            )
            is ParentProgressUiState.Ready -> ParentProgressContent(current.model)
        }
    }
}

@Composable
private fun ParentProgressContent(model: ParentProgressModel) {
    ParentProgressInfoCard(
        title = "Local and descriptive",
        body = "This view describes activity stored on this device. It does not grade, rank, compare or predict your child's ability, and it does not upload behavioral analytics.",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ParentProgressMetricCard(
            value = model.completedLessonCount.toString(),
            label = "Lessons completed",
            modifier = Modifier.weight(1f),
        )
        ParentProgressMetricCard(
            value = model.savedArtworkCount.toString(),
            label = "Artworks saved",
            modifier = Modifier.weight(1f),
        )
    }

    ParentProgressInfoCard(
        title = "Curriculum",
        body = if (model.totalCurriculumLessons > 0) {
            "${model.totalCurriculumLessons} lessons are currently available in the local curriculum. Completion counts describe finished lesson saves, not mastery."
        } else {
            "Curriculum details are not available right now."
        },
    )

    if (!model.hasLearningHistory) {
        ParentProgressInfoCard(
            title = "No learning history yet",
            body = "Once a lesson is completed or artwork is saved, this page will describe that activity here. Nothing needs to be enabled or connected online.",
        )
    }

    if (model.inProgress.isNotEmpty()) {
        ParentProgressSection(title = "In progress") {
            model.inProgress.forEach { item ->
                ParentProgressListCard(
                    title = item.title,
                    body = "${item.kindLabel} · saved ${formatTimestamp(item.savedAtEpochMillis)}",
                )
            }
        }
    }

    if (model.recentCompletions.isNotEmpty()) {
        ParentProgressSection(title = "Recently completed") {
            model.recentCompletions.forEach { lesson ->
                val detail = buildList {
                    if (lesson.categories.isNotEmpty()) add(lesson.categories.joinToString())
                    if (lesson.skills.isNotEmpty()) add("Skills: ${lesson.skills.joinToString()}")
                }.joinToString(" · ")
                ParentProgressListCard(
                    title = lesson.title,
                    body = detail.ifBlank { "Completed lesson" },
                )
            }
            Text(
                text = "Recent completion order is stored locally without wall-clock timestamps, so this section intentionally does not invent dates.",
                style = MaterialTheme.typography.bodySmall,
                color = StudioColors.Ink600,
            )
        }
    }

    if (model.exploredCategories.isNotEmpty()) {
        ParentProgressInfoCard(
            title = "Curriculum areas explored",
            body = model.exploredCategories.joinToString(" · "),
        )
    }

    if (model.practicedSkills.isNotEmpty()) {
        ParentProgressInfoCard(
            title = "Authored skills practiced",
            body = model.practicedSkills.joinToString(" · "),
        )
    }

    if (model.journeys.isNotEmpty()) {
        ParentProgressSection(title = "Journeys") {
            model.journeys.forEach { journey ->
                val next = journey.nextAvailableLessonTitle?.let { " · Next available: $it" }.orEmpty()
                ParentProgressListCard(
                    title = journey.title,
                    body = "${journey.completedLessons} of ${journey.totalLessons} lessons completed$next",
                )
            }
        }
    }

    if (model.recentArtwork.isNotEmpty()) {
        ParentProgressSection(title = "Recent saved artwork") {
            model.recentArtwork.forEach { artwork ->
                ParentProgressListCard(
                    title = artwork.title,
                    body = "${artwork.sourceLabel} · ${artwork.completionLabel} · ${formatTimestamp(artwork.completedAtEpochMillis)}",
                )
            }
        }
    }

    if (model.notices.isNotEmpty()) {
        ParentProgressSection(title = "Data notes") {
            model.notices.forEach { notice ->
                ParentProgressInfoCard(title = "Partial local data", body = notice)
            }
        }
    }
}

@Composable
private fun ParentProgressScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineLarge,
                    color = StudioColors.Ink900,
                )
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Back")
                }
            }
            content()
        }
    }
}

@Composable
private fun ParentProgressSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = StudioColors.Ink900,
        )
        content()
    }
}

@Composable
private fun ParentProgressMetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = StudioColors.Ink900,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = StudioColors.Ink700,
            )
        }
    }
}

@Composable
private fun ParentProgressInfoCard(
    title: String,
    body: String,
) {
    ParentProgressListCard(title = title, body = body)
}

@Composable
private fun ParentProgressListCard(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = StudioColors.Ink900,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = StudioColors.Ink700,
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
