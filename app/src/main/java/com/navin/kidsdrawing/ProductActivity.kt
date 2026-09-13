package com.navin.kidsdrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.lesson.content.LessonCatalogIdentity
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.StudioTheme
import com.navin.kidsdrawing.product.gallery.ArtworkCompletionScreen
import com.navin.kidsdrawing.product.gallery.GalleryArtworkDetailScreen
import com.navin.kidsdrawing.product.gallery.GalleryAwareColoringWorkspace
import com.navin.kidsdrawing.product.gallery.GalleryScreen
import com.navin.kidsdrawing.product.gallery.ProductGalleryRuntime
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.home.StudioCategoryScreen
import com.navin.kidsdrawing.product.home.StudioDestination
import com.navin.kidsdrawing.product.home.StudioHomeModel
import com.navin.kidsdrawing.product.home.StudioHomeRepository
import com.navin.kidsdrawing.product.home.StudioHomeScreen
import com.navin.kidsdrawing.product.home.StudioJourneyScreen
import com.navin.kidsdrawing.product.home.StudioPlaceholderRoute
import com.navin.kidsdrawing.product.lesson.ProductLessonFlow
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import com.navin.kidsdrawing.product.onboarding.OnboardingFlow
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.ChildProfileDraft
import com.navin.kidsdrawing.product.profile.ChildProfileStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudioTheme {
                val store = remember { ChildProfileStore(applicationContext) }
                ProductRoot(store = store)
            }
        }
    }
}

private sealed interface StartupState {
    data object Loading : StartupState
    data class Onboarding(val draft: ChildProfileDraft) : StartupState
    data class Home(val profile: ChildProfile) : StartupState
}

@Composable
private fun ProductRoot(store: ChildProfileStore) {
    var state: StartupState by remember { mutableStateOf(StartupState.Loading) }
    val scope = rememberCoroutineScope()
    var persistenceJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(store) {
        val completed = runCatching { store.loadCompletedProfile() }.getOrNull()
        state = if (completed != null) {
            StartupState.Home(completed)
        } else {
            val draft = runCatching { store.loadDraft() }.getOrDefault(ChildProfileDraft())
            StartupState.Onboarding(draft)
        }
    }

    when (val current = state) {
        StartupState.Loading -> LoadingStudio()
        is StartupState.Onboarding -> OnboardingFlow(
            draft = current.draft,
            onDraftChange = { next ->
                state = StartupState.Onboarding(next)
                persistenceJob?.cancel()
                persistenceJob = scope.launch {
                    runCatching { store.saveDraft(next) }
                }
            },
            onComplete = { profile ->
                persistenceJob?.cancel()
                scope.launch {
                    val saved = runCatching { store.saveCompletedProfile(profile) }.isSuccess
                    if (saved) state = StartupState.Home(profile)
                }
            },
        )
        is StartupState.Home -> ProductStudio(profile = current.profile)
    }
}

@Composable
private fun ProductStudio(profile: ChildProfile) {
    val context = LocalContext.current
    val repository = remember(context) { StudioHomeRepository(context) }
    var routeName by rememberSaveable { mutableStateOf(StudioDestination.HOME.name) }
    val route = runCatching { StudioDestination.valueOf(routeName) }
        .getOrDefault(StudioDestination.HOME)
    var homeModel by remember(profile) { mutableStateOf<StudioHomeModel?>(null) }
    var selectedLessonId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedLessonRevision by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedJourneyId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGalleryEntryId by rememberSaveable { mutableStateOf<String?>(null) }
    var completionEntryId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(profile, route) {
        if (homeModel == null || route == StudioDestination.HOME) {
            homeModel = runCatching { repository.load(profile) }
                .getOrElse {
                    StudioHomeModel(
                        recommendation = null,
                        resumeCandidate = null,
                        coloringResumeCandidate = null,
                        contentMessage = "Your studio is open. These lessons need a moment before they can start.",
                    )
                }
        }
    }

    val selectedRecommendation = homeModel?.recommendations?.firstOrNull {
        it.lessonId == selectedLessonId && it.lessonRevision == selectedLessonRevision
    }
    val runtimeRecommendation = selectedRecommendation ?: homeModel?.recommendation
    val runtimeLessonId = runtimeRecommendation?.lessonId
    val runtimeLessonRevision = runtimeRecommendation?.lessonRevision
    val lessonRuntime = remember(context, runtimeLessonId, runtimeLessonRevision) {
        if (runtimeLessonId != null && runtimeLessonRevision != null) {
            ProductLessonRuntime.forLesson(
                context,
                LessonCatalogIdentity(runtimeLessonId, runtimeLessonRevision),
            )
        } else {
            null
        }
    }
    val coloringRuntime = remember(context, lessonRuntime) {
        lessonRuntime?.let { ProductColoringRuntime(context, it) }
    }
    val galleryRuntime = remember(context, lessonRuntime, coloringRuntime) {
        if (lessonRuntime != null && coloringRuntime != null) {
            ProductGalleryRuntime(context, lessonRuntime, coloringRuntime)
        } else {
            null
        }
    }

    fun selectLesson(recommendation: LessonRecommendation?) {
        selectedLessonId = recommendation?.lessonId
        selectedLessonRevision = recommendation?.lessonRevision
    }

    fun recommendationForResume(): LessonRecommendation? {
        val resume = homeModel?.resumeCandidate ?: return null
        return homeModel?.recommendations?.firstOrNull {
            it.lessonId == resume.lessonId && it.lessonRevision == resume.lessonRevision
        }
    }

    fun recommendationForColoring(): LessonRecommendation? {
        val resume = homeModel?.coloringResumeCandidate ?: return null
        return homeModel?.recommendations?.firstOrNull {
            it.lessonId == resume.lessonId && it.lessonRevision == resume.lessonRevision
        }
    }

    completionEntryId?.let { entryId ->
        val gallery = galleryRuntime
        if (gallery != null) {
            ArtworkCompletionScreen(
                runtime = gallery,
                entryId = entryId,
                onSeeGallery = {
                    completionEntryId = null
                    selectedGalleryEntryId = null
                    routeName = StudioDestination.GALLERY.name
                },
                onBackToStudio = {
                    completionEntryId = null
                    selectedGalleryEntryId = null
                    routeName = StudioDestination.HOME.name
                },
            )
        } else {
            LoadingStudio()
        }
        return
    }

    when (route) {
        StudioDestination.HOME -> StudioHomeScreen(
            profile = profile,
            model = homeModel,
            onPrimaryLessonAction = { destination ->
                val recommendation = when (destination) {
                    StudioDestination.COLORING_RESUME -> recommendationForColoring()
                    StudioDestination.LESSON_RESUME -> recommendationForResume()
                    else -> homeModel?.recommendation
                }
                selectLesson(recommendation)
                routeName = destination.name
            },
            onOpenRecommendation = {
                val recommendation = homeModel?.recommendation
                selectLesson(recommendation)
                routeName = when {
                    homeModel?.coloringResumeCandidate != null -> StudioDestination.COLORING_RESUME.name
                    homeModel?.resumeCandidate != null -> StudioDestination.LESSON_RESUME.name
                    else -> StudioDestination.LESSON_SELECTED.name
                }
            },
            onOpenLesson = { recommendation ->
                selectLesson(recommendation)
                val coloring = homeModel?.coloringResumeCandidate
                val drawing = homeModel?.resumeCandidate
                routeName = when {
                    coloring?.lessonId == recommendation.lessonId &&
                        coloring.lessonRevision == recommendation.lessonRevision ->
                        StudioDestination.COLORING_RESUME.name
                    drawing?.lessonId == recommendation.lessonId &&
                        drawing.lessonRevision == recommendation.lessonRevision ->
                        StudioDestination.LESSON_RESUME.name
                    else -> StudioDestination.LESSON_SELECTED.name
                }
            },
            onOpenCategory = { categoryId ->
                selectedCategoryId = categoryId
                routeName = StudioDestination.CATEGORY.name
            },
            onOpenJourney = { journeyId ->
                selectedJourneyId = journeyId
                routeName = StudioDestination.JOURNEY.name
            },
            onOpenDestination = { destination ->
                if (destination == StudioDestination.GALLERY) selectedGalleryEntryId = null
                routeName = destination.name
            },
        )

        StudioDestination.CATEGORY -> StudioCategoryScreen(
            category = homeModel?.categories?.firstOrNull { it.categoryId == selectedCategoryId },
            onOpenLesson = { recommendation ->
                selectLesson(recommendation)
                routeName = StudioDestination.LESSON_SELECTED.name
            },
            onBack = { routeName = StudioDestination.HOME.name },
        )

        StudioDestination.JOURNEY,
        StudioDestination.ART_JOURNEY,
        -> StudioJourneyScreen(
            journey = homeModel?.journeys?.firstOrNull { it.journeyId == selectedJourneyId },
            onOpenLesson = { recommendation ->
                selectLesson(recommendation)
                routeName = StudioDestination.LESSON_SELECTED.name
            },
            onBack = { routeName = StudioDestination.HOME.name },
        )

        StudioDestination.LESSON_START,
        StudioDestination.LESSON_SELECTED,
        StudioDestination.LESSON_RESUME,
        -> {
            val recommendation = selectedRecommendation ?: when (route) {
                StudioDestination.LESSON_RESUME -> recommendationForResume()
                else -> homeModel?.recommendation
            }
            val runtime = lessonRuntime
            val coloring = coloringRuntime
            val gallery = galleryRuntime
            if (recommendation == null || runtime == null || coloring == null || gallery == null) {
                LoadingStudio()
            } else {
                ProductLessonFlow(
                    runtime = runtime,
                    coloringRuntime = coloring,
                    galleryRuntime = gallery,
                    profile = profile,
                    recommendation = recommendation,
                    resumeRequested = route == StudioDestination.LESSON_RESUME,
                    onArtworkCompleted = { entryId -> completionEntryId = entryId },
                    onExitToHome = { routeName = StudioDestination.HOME.name },
                )
            }
        }

        StudioDestination.COLORING_RESUME -> {
            val coloring = coloringRuntime
            val gallery = galleryRuntime
            val recommendation = selectedRecommendation ?: recommendationForColoring()
            if (coloring == null || gallery == null || recommendation == null) {
                LoadingStudio()
            } else {
                GalleryAwareColoringWorkspace(
                    coloringRuntime = coloring,
                    galleryRuntime = gallery,
                    ageBand = profile.ageBand,
                    artworkTitle = recommendation.title,
                    recoverRequested = true,
                    onArtworkCompleted = { entryId -> completionEntryId = entryId },
                    onExitToHome = { routeName = StudioDestination.HOME.name },
                )
            }
        }

        StudioDestination.GALLERY -> {
            val gallery = galleryRuntime
            if (gallery == null) {
                StudioPlaceholderRoute(
                    destination = StudioDestination.GALLERY,
                    onBack = { routeName = StudioDestination.HOME.name },
                )
            } else {
                val selected = selectedGalleryEntryId
                if (selected == null) {
                    GalleryScreen(
                        runtime = gallery,
                        onOpenArtwork = { entryId -> selectedGalleryEntryId = entryId },
                        onBack = { routeName = StudioDestination.HOME.name },
                    )
                } else {
                    GalleryArtworkDetailScreen(
                        runtime = gallery,
                        entryId = selected,
                        onBack = { selectedGalleryEntryId = null },
                        onDeleted = { selectedGalleryEntryId = null },
                    )
                }
            }
        }

        else -> StudioPlaceholderRoute(
            destination = route,
            onBack = { routeName = StudioDestination.HOME.name },
        )
    }
}

@Composable
private fun LoadingStudio() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier.safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.size(120.dp))
            CircularProgressIndicator(color = StudioColors.Studio600)
            Text(
                text = "Opening your art studio…",
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
        }
    }
}
