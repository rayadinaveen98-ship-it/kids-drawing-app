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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.StudioPrimaryButton
import com.navin.kidsdrawing.product.design.densityPolicyFor
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildProfile
import kotlin.math.absoluteValue

@Composable
fun StudioHomeScreen(
    profile: ChildProfile,
    model: StudioHomeModel?,
    onPrimaryLessonAction: (StudioDestination) -> Unit,
    onOpenRecommendation: () -> Unit,
    onOpenDestination: (StudioDestination) -> Unit,
    modifier: Modifier = Modifier,
    onOpenLesson: ((LessonRecommendation) -> Unit)? = null,
    onOpenCategory: (String) -> Unit = {},
    onOpenJourney: (String) -> Unit = {},
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

                    val visibleRecommendations = model.recommendations
                        .take(presentation.recommendationLimit)
                    if (visibleRecommendations.isNotEmpty()) {
                        SectionHeader(
                            title = "Picked for you",
                            subtitle = when (profile.ageBand) {
                                AgeBand.LITTLE_ARTIST -> "A few friendly places to begin"
                                AgeBand.CREATIVE_EXPLORER -> "Lessons that fit your studio"
                                else -> "Based on your age, interests and learning style"
                            },
                        )
                        visibleRecommendations.forEach { recommendation ->
                            RecommendationCard(
                                profile = profile,
                                recommendation = recommendation,
                                presentation = presentation,
                                onClick = {
                                    if (onOpenLesson != null) onOpenLesson(recommendation)
                                    else onOpenRecommendation()
                                },
                            )
                        }
                    }
                }
            }

            model?.journeys?.take(if (profile.ageBand == AgeBand.LITTLE_ARTIST) 2 else 4)?.let { journeys ->
                if (journeys.isNotEmpty()) {
                    SectionHeader(
                        title = "Art Journeys",
                        subtitle = "Small steps that grow into bigger drawing skills",
                    )
                    journeys.forEach { journey ->
                        StudioRouteCard(
                            symbol = "✦",
                            title = journey.title,
                            subtitle = journey.progressLabel,
                            accent = StudioColors.Sun500,
                            onClick = { onOpenJourney(journey.journeyId) },
                            minimumHeight = density.minimumTouchTarget,
                        )
                    }
                }
            }

            model?.categories?.take(if (profile.ageBand == AgeBand.LITTLE_ARTIST) 3 else 6)?.let { categories ->
                if (categories.isNotEmpty()) {
                    SectionHeader(
                        title = "Explore by idea",
                        subtitle = "Choose what sounds fun today",
                    )
                    categories.forEachIndexed { index, category ->
                        StudioRouteCard(
                            symbol = categorySymbol(category.categoryId),
                            title = category.title,
                            subtitle = if (category.lessonCount == 1) "1 lesson" else "${category.lessonCount} lessons",
                            accent = if (index % 2 == 0) StudioColors.Sky500 else StudioColors.Lavender500,
                            onClick = { onOpenCategory(category.categoryId) },
                            minimumHeight = density.minimumTouchTarget,
                        )
                    }
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
                        symbol = "✎",
                        title = "Free Draw",
                        subtitle = "Blank paper, your ideas",
                        accent = StudioColors.Sky500,
                        onClick = { onOpenDestination(StudioDestination.FREE_DRAW) },
                        modifier = Modifier.weight(1f),
                        minimumHeight = density.minimumTouchTarget * 2,
                    )
                    StudioRouteCard(
                        symbol = "▣",
                        title = "My Gallery",
                        subtitle = "See the art you finished",
                        accent = StudioColors.Lavender500,
                        onClick = { onOpenDestination(StudioDestination.GALLERY) },
                        modifier = Modifier.weight(1f),
                        minimumHeight = density.minimumTouchTarget * 2,
                    )
                }
            } else {
                StudioRouteCard(
                    symbol = "✎",
                    title = "Free Draw",
                    subtitle = "Start with a clean page",
                    accent = StudioColors.Sky500,
                    onClick = { onOpenDestination(StudioDestination.FREE_DRAW) },
                    minimumHeight = density.minimumTouchTarget,
                )
                StudioRouteCard(
                    symbol = "▣",
                    title = "My Gallery",
                    subtitle = "Your finished drawings live here",
                    accent = StudioColors.Lavender500,
                    onClick = { onOpenDestination(StudioDestination.GALLERY) },
                    minimumHeight = density.minimumTouchTarget,
                )
            }

            model?.contentMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink500,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

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
                text = message ?: "These lessons are taking a moment to get ready.",
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

                LessonDecorativePreview(
                    lessonId = recommendation.lessonId,
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
            LessonDecorativePreview(
                lessonId = recommendation.lessonId,
                modifier = Modifier.size(if (presentation.density == HomeCardDensity.SPACIOUS) 82.dp else 72.dp),
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
private fun LessonDecorativePreview(
    lessonId: String,
    modifier: Modifier = Modifier,
) {
    val variant = lessonId.hashCode().absoluteValue % 3
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = size.minDimension * 0.065f
            val center = Offset(size.width / 2f, size.height / 2f)
            when (variant) {
                0 -> {
                    drawCircle(
                        color = StudioColors.Studio600,
                        radius = size.minDimension * 0.27f,
                        center = center,
                        style = Stroke(width = strokeWidth),
                    )
                    drawLine(
                        color = StudioColors.Sun500,
                        start = Offset(size.width * 0.2f, size.height * 0.78f),
                        end = Offset(size.width * 0.8f, size.height * 0.22f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
                1 -> {
                    drawRoundRect(
                        color = StudioColors.Sky500,
                        topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                        size = Size(size.width * 0.6f, size.height * 0.6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.12f),
                        style = Stroke(width = strokeWidth),
                    )
                    drawCircle(
                        color = StudioColors.Studio600,
                        radius = size.minDimension * 0.09f,
                        center = center,
                    )
                }
                else -> {
                    drawLine(
                        color = StudioColors.Lavender500,
                        start = Offset(size.width * 0.18f, size.height * 0.7f),
                        end = Offset(size.width * 0.5f, size.height * 0.25f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = StudioColors.Lavender500,
                        start = Offset(size.width * 0.5f, size.height * 0.25f),
                        end = Offset(size.width * 0.82f, size.height * 0.7f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = StudioColors.Sun500,
                        start = Offset(size.width * 0.28f, size.height * 0.66f),
                        end = Offset(size.width * 0.72f, size.height * 0.66f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink700,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
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
        StudioDestination.LESSON_START,
        StudioDestination.LESSON_SELECTED,
        -> "Drawing Lesson"
        StudioDestination.LESSON_RESUME -> "Continue Drawing"
        StudioDestination.COLORING_RESUME -> "Continue Coloring"
        StudioDestination.CATEGORY -> "Explore Lessons"
        StudioDestination.JOURNEY,
        StudioDestination.ART_JOURNEY,
        -> "Art Journey"
        StudioDestination.FREE_DRAW -> "Free Draw"
        StudioDestination.GALLERY -> "My Gallery"
        StudioDestination.PARENT_ZONE -> "Grown-ups area"
        StudioDestination.HOME -> "Studio Home"
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = StudioColors.Ink900,
            )
            Text(
                text = "This studio space is getting ready.",
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
            TextButton(onClick = onBack) {
                Text("Back to studio")
            }
        }
    }
}

private fun greetingSubtitle(ageBand: AgeBand): String = when (ageBand) {
    AgeBand.LITTLE_ARTIST -> "What should we draw today?"
    AgeBand.CREATIVE_EXPLORER -> "Ready to make something fun?"
    AgeBand.GROWING_ARTIST -> "Your art studio is ready"
    AgeBand.YOUNG_ARTIST -> "Pick a lesson or explore your own idea"
}

private fun recommendationReason(
    profile: ChildProfile,
    recommendation: LessonRecommendation,
): String = when (recommendation.reason) {
    RecommendationReason.INTEREST_MATCH -> "Picked because it matches what you like"
    RecommendationReason.AGE_MATCH -> "A good fit for ${profile.ageBand.displayName.lowercase()}s"
    RecommendationReason.STARTER_PICK -> "A calm place to start"
}

private fun heroMetadata(
    recommendation: LessonRecommendation,
    presentation: HomePresentationPolicy,
): String = buildList {
    add("${recommendation.estimatedMinutes} min")
    if (presentation.showDifficulty) add("Level ${recommendation.difficulty}")
    if (presentation.showSkills && recommendation.primarySkillIds.isNotEmpty()) {
        add(recommendation.primarySkillIds.first().replace('_', ' '))
    }
}.joinToString(" · ")

private fun categorySymbol(categoryId: String): String {
    val symbols = listOf("○", "△", "◇", "✦", "□")
    return symbols[categoryId.hashCode().absoluteValue % symbols.size]
}
