package com.navin.kidsdrawing.product.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.StudioPrimaryButton
import com.navin.kidsdrawing.product.design.densityPolicyFor
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile

@Composable
fun StudioHomeScreen(
    profile: ChildProfile,
    model: StudioHomeModel?,
    onPrimaryLessonAction: (StudioDestination) -> Unit,
    onOpenRecommendation: () -> Unit,
    onOpenDestination: (StudioDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val presentation = StudioRecommendationPolicy.presentationFor(profile.ageBand)
    val density = densityPolicyFor(profile.ageBand)
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(density.contentGap),
        ) {
            StudioGreeting(
                profile = profile,
                onOpenParentZone = { onOpenDestination(StudioDestination.PARENT_ZONE) },
            )

            when {
                model == null -> StudioHomeLoading()
                model.recommendation == null -> StudioContentUnavailable(model.contentMessage)
                else -> {
                    PrimaryStudioHero(
                        profile = profile,
                        recommendation = model.recommendation,
                        resume = model.resumeCandidate,
                        coloringResume = model.coloringResumeCandidate,
                        presentation = presentation,
                        onClick = {
                            onPrimaryLessonAction(
                                when {
                                    model.coloringResumeCandidate != null -> StudioDestination.COLORING_RESUME
                                    model.resumeCandidate != null -> StudioDestination.LESSON_RESUME
                                    else -> StudioDestination.LESSON_START
                                },
                            )
                        },
                    )

                    SectionHeader(
                        title = "Picked for you",
                        subtitle = when (profile.ageBand) {
                            AgeBand.LITTLE_ARTIST -> "One friendly place to begin"
                            AgeBand.CREATIVE_EXPLORER -> "A lesson that fits your studio"
                            else -> "Based on your age, interests and learning style"
                        },
                    )
                    RecommendationCard(
                        profile = profile,
                        recommendation = model.recommendation,
                        presentation = presentation,
                        onClick = onOpenRecommendation,
                    )
                }
            }

            SectionHeader(
                title = "Explore your studio",
                subtitle = if (profile.ageBand == AgeBand.LITTLE_ARTIST) {
                    "Choose what sounds fun"
                } else {
                    "More ways to make art"
                },
            )

            if (presentation.twoColumnSecondaryCards) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StudioRouteCard(
                        symbol = "✦",
                        title = "Animal Artist",
                        subtitle = "An Art Journey",
                        accent = StudioColors.Sun500,
                        onClick = { onOpenDestination(StudioDestination.ART_JOURNEY) },
                        modifier = Modifier.weight(1f),
                        minimumHeight = density.minimumTouchTarget * 2,
                    )
                    StudioRouteCard(
                        symbol = "✎",
                        title = "Free Draw",
                        subtitle = "Blank paper, your ideas",
                        accent = StudioColors.Sky500,
                        onClick = { onOpenDestination(StudioDestination.FREE_DRAW) },
                        modifier = Modifier.weight(1f),
                        minimumHeight = density.minimumTouchTarget * 2,
                    )
                }
            } else {
                StudioRouteCard(
                    symbol = "✦",
                    title = "Animal Artist",
                    subtitle = "A little Art Journey",
                    accent = StudioColors.Sun500,
                    onClick = { onOpenDestination(StudioDestination.ART_JOURNEY) },
                    minimumHeight = density.minimumTouchTarget,
                )
                StudioRouteCard(
                    symbol = "✎",
                    title = "Free Draw",
                    subtitle = "Start with a clean page",
                    accent = StudioColors.Sky500,
                    onClick = { onOpenDestination(StudioDestination.FREE_DRAW) },
                    minimumHeight = density.minimumTouchTarget,
                )
            }

            StudioRouteCard(
                symbol = "▣",
                title = "My Gallery",
                subtitle = "Your drawings will live here",
                accent = StudioColors.Lavender500,
                onClick = { onOpenDestination(StudioDestination.GALLERY) },
                minimumHeight = density.minimumTouchTarget,
            )

            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}

@Composable
private fun StudioGreeting(
    profile: ChildProfile,
    onOpenParentZone: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hi ${profile.nickname}",
                style = MaterialTheme.typography.headlineLarge,
                color = StudioColors.Ink900,
            )
            Text(
                text = greetingSubtitle(profile.ageBand),
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
        }

        Surface(
            modifier = Modifier.size(46.dp),
            shape = CircleShape,
            color = StudioColors.Studio100,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "✦",
                    color = StudioColors.Studio600,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        TextButton(
            onClick = onOpenParentZone,
            modifier = Modifier
                .padding(start = 4.dp)
                .heightIn(min = 48.dp)
                .semantics { contentDescription = "Open grown-ups area" },
        ) {
            Text(
                text = "Grown-ups",
                color = StudioColors.Ink500,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StudioHomeLoading() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp),
        shape = RoundedCornerShape(28.dp),
        color = StudioColors.Studio100,
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(color = StudioColors.Studio600)
            Text(
                text = "Picking something lovely for you…",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
        }
    }
}

@Composable
private fun StudioContentUnavailable(message: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Your studio is open",
                style = MaterialTheme.typography.headlineSmall,
                color = StudioColors.Ink900,
            )
            Text(
                text = message ?: "This lesson is taking a moment to get ready.",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
        }
    }
}

@Composable
private fun PrimaryStudioHero(
    profile: ChildProfile,
    recommendation: LessonRecommendation,
    resume: ResumeLessonCandidate?,
    coloringResume: ColoringResumeCandidate?,
    presentation: HomePresentationPolicy,
    onClick: () -> Unit,
) {
    val continuing = resume != null || coloringResume != null
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (presentation.density == HomeCardDensity.SPACIOUS) 28.dp else 24.dp),
        color = StudioColors.Studio100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Column(modifier = Modifier.padding(if (presentation.density == HomeCardDensity.SPACIOUS) 24.dp else 20.dp)) {
            Text(
                text = when {
                    coloringResume != null -> "CONTINUE COLORING"
                    resume != null -> "CONTINUE DRAWING"
                    else -> "DRAW TOGETHER"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = StudioColors.Studio600,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendation.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = StudioColors.Ink900,
                    )
                    Text(
                        text = coloringResume?.progressLabel
                            ?: resume?.progressLabel
                            ?: recommendationReason(profile, recommendation),
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = StudioColors.Ink700,
                    )
                    Text(
                        text = heroMetadata(recommendation, presentation),
                        modifier = Modifier.padding(top = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioColors.Ink500,
                    )
                }

                CuteCatPreview(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .width(if (presentation.density == HomeCardDensity.SPACIOUS) 118.dp else 104.dp)
                        .aspectRatio(1f),
                )
            }

            StudioPrimaryButton(
                text = if (continuing) "Continue" else recommendation.actionLabel,
                onClick = onClick,
                modifier = Modifier
                    .padding(top = 18.dp)
                    .semantics {
                        contentDescription = when {
                            coloringResume != null -> "Continue coloring ${recommendation.title}"
                            resume != null -> "Continue ${recommendation.title} drawing"
                            else -> "Start ${recommendation.title} lesson"
                        }
                    },
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    profile: ChildProfile,
    recommendation: LessonRecommendation,
    presentation: HomePresentationPolicy,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .semantics { contentDescription = "Open ${recommendation.title} lesson recommendation" }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CuteCatPreview(
                modifier = Modifier
                    .size(if (presentation.density == HomeCardDensity.SPACIOUS) 82.dp else 72.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = StudioColors.Ink900,
                )
                Text(
                    text = recommendationReason(profile, recommendation),
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink700,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = heroMetadata(recommendation, presentation),
                    modifier = Modifier.padding(top = 5.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Studio600,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = StudioColors.Ink900,
        )
        Text(
            text = subtitle,
            modifier = Modifier.padding(top = 2.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = StudioColors.Ink500,
        )
    }
}

@Composable
private fun StudioRouteCard(
    symbol: String,
    title: String,
    subtitle: String,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minimumHeight: androidx.compose.ui.unit.Dp = 64.dp,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minimumHeight)
            .semantics { contentDescription = "Open $title" }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = accent.copy(alpha = 0.16f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = symbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = StudioColors.Ink900,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = StudioColors.Ink900,
                )
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink700,
                )
            }
        }
    }
}

@Composable
fun StudioPlaceholderRoute(
    destination: StudioDestination,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (destination) {
        StudioDestination.LESSON_START -> "Cute Cat"
        StudioDestination.LESSON_RESUME -> "Continue Drawing"
        StudioDestination.COLORING_RESUME -> "Continue Coloring"
        StudioDestination.ART_JOURNEY -> "Animal Artist"
        StudioDestination.FREE_DRAW -> "Free Draw"
        StudioDestination.GALLERY -> "My Gallery"
        StudioDestination.PARENT_ZONE -> "Grown-ups area"
        StudioDestination.HOME -> "Studio"
    }
    val body = when (destination) {
        StudioDestination.LESSON_START -> "Your Cute Cat lesson is picked and ready for its drawing room."
        StudioDestination.LESSON_RESUME -> "Your saved drawing is safe and ready for you to continue."
        StudioDestination.COLORING_RESUME -> "Your colors and drawing are safe and ready to continue."
        StudioDestination.ART_JOURNEY -> "A calm path of animal drawings will grow here as your studio grows."
        StudioDestination.FREE_DRAW -> "A clean page for your own ideas will open here."
        StudioDestination.GALLERY -> "This will become your personal wall of saved artwork."
        StudioDestination.PARENT_ZONE -> "Profile, sound and safety settings belong in this quiet grown-ups area."
        StudioDestination.HOME -> "Back to your studio."
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = StudioColors.Ink900,
            )
            Text(
                text = body,
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
            StudioPrimaryButton(
                text = "Back to studio",
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 28.dp)
                    .semantics { contentDescription = "Back to studio home" },
            )
        }
    }
}

@Composable
private fun CuteCatPreview(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stroke = width * 0.045f
        val ink = StudioColors.Ink700
        val warm = StudioColors.Coral500.copy(alpha = 0.12f)

        drawCircle(
            color = warm,
            radius = width * 0.48f,
            center = Offset(width * 0.5f, height * 0.5f),
        )

        val headCenter = Offset(width * 0.5f, height * 0.38f)
        drawCircle(
            color = ink,
            radius = width * 0.22f,
            center = headCenter,
            style = Stroke(width = stroke),
        )

        val ears = Path().apply {
            moveTo(width * 0.33f, height * 0.25f)
            lineTo(width * 0.30f, height * 0.08f)
            lineTo(width * 0.43f, height * 0.20f)
            moveTo(width * 0.57f, height * 0.20f)
            lineTo(width * 0.70f, height * 0.08f)
            lineTo(width * 0.67f, height * 0.25f)
        }
        drawPath(
            path = ears,
            color = ink,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        drawCircle(color = ink, radius = width * 0.025f, center = Offset(width * 0.43f, height * 0.36f))
        drawCircle(color = ink, radius = width * 0.025f, center = Offset(width * 0.57f, height * 0.36f))

        val mouth = Path().apply {
            moveTo(width * 0.50f, height * 0.40f)
            cubicTo(
                width * 0.47f,
                height * 0.45f,
                width * 0.44f,
                height * 0.45f,
                width * 0.42f,
                height * 0.42f,
            )
            moveTo(width * 0.50f, height * 0.40f)
            cubicTo(
                width * 0.53f,
                height * 0.45f,
                width * 0.56f,
                height * 0.45f,
                width * 0.58f,
                height * 0.42f,
            )
        }
        drawPath(mouth, ink, style = Stroke(width = stroke * 0.7f, cap = StrokeCap.Round))

        drawOval(
            color = ink,
            topLeft = Offset(width * 0.37f, height * 0.56f),
            size = androidx.compose.ui.geometry.Size(width * 0.30f, height * 0.34f),
            style = Stroke(width = stroke),
        )

        val tail = Path().apply {
            moveTo(width * 0.64f, height * 0.75f)
            cubicTo(
                width * 0.86f,
                height * 0.82f,
                width * 0.88f,
                height * 0.58f,
                width * 0.76f,
                height * 0.56f,
            )
        }
        drawPath(tail, ink, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

private fun greetingSubtitle(ageBand: AgeBand): String = when (ageBand) {
    AgeBand.LITTLE_ARTIST -> "What should we make today?"
    AgeBand.CREATIVE_EXPLORER -> "Your art table is ready."
    AgeBand.GROWING_ARTIST -> "Ready for your next idea?"
    AgeBand.YOUNG_ARTIST -> "Pick up where you left off or try a new technique."
}

private fun recommendationReason(
    profile: ChildProfile,
    recommendation: LessonRecommendation,
): String = when (recommendation.reason) {
    RecommendationReason.INTEREST_MATCH -> if (ChildInterest.ANIMALS in profile.interests) {
        "Because animals are one of your favorites"
    } else {
        "Picked from your favorite subjects"
    }

    RecommendationReason.AGE_MATCH -> "A comfortable match for your studio"
    RecommendationReason.STARTER_PICK -> "A gentle studio starter"
}

private fun heroMetadata(
    recommendation: LessonRecommendation,
    presentation: HomePresentationPolicy,
): String = buildList {
    add("${recommendation.estimatedMinutes} min")
    if (presentation.showDifficulty) add(difficultyLabel(recommendation.difficulty))
    if (presentation.showSkills && recommendation.primarySkillIds.isNotEmpty()) {
        add(
            recommendation.primarySkillIds
                .take(2)
                .joinToString(" + ") { it.replace('_', ' ') },
        )
    }
    if (presentation.density != HomeCardDensity.SPACIOUS) {
        add(modeLabel(recommendation.defaultMode))
    }
}.joinToString(" · ")

private fun difficultyLabel(difficulty: Int): String = when (difficulty) {
    1 -> "Easy start"
    2 -> "Gentle challenge"
    3 -> "Growing skills"
    4 -> "Focused practice"
    else -> "Advanced"
}

private fun modeLabel(mode: TeachingMode): String = when (mode) {
    TeachingMode.DRAW_WITH_ME -> "Draw with me"
    TeachingMode.WATCH_THEN_DRAW -> "Watch first"
    TeachingMode.TRACE_AND_LEARN -> "Trace & learn"
}
