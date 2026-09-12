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
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.StudioTheme
import com.navin.kidsdrawing.product.home.StudioDestination
import com.navin.kidsdrawing.product.home.StudioHomeModel
import com.navin.kidsdrawing.product.home.StudioHomeRepository
import com.navin.kidsdrawing.product.home.StudioHomeScreen
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
    val lessonRuntime = remember(context) { ProductLessonRuntime(context) }
    var routeName by rememberSaveable { mutableStateOf(StudioDestination.HOME.name) }
    val route = runCatching { StudioDestination.valueOf(routeName) }
        .getOrDefault(StudioDestination.HOME)
    var homeModel by remember(profile) { mutableStateOf<StudioHomeModel?>(null) }

    LaunchedEffect(profile, route) {
        if (homeModel == null || route == StudioDestination.HOME) {
            homeModel = runCatching { repository.load(profile) }
                .getOrElse {
                    StudioHomeModel(
                        recommendation = null,
                        resumeCandidate = null,
                        contentMessage = "Your studio is open. This lesson needs a moment before it can start.",
                    )
                }
        }
    }

    when (route) {
        StudioDestination.HOME -> StudioHomeScreen(
            profile = profile,
            model = homeModel,
            onPrimaryLessonAction = { destination -> routeName = destination.name },
            onOpenRecommendation = {
                routeName = if (homeModel?.resumeCandidate != null) {
                    StudioDestination.LESSON_RESUME.name
                } else {
                    StudioDestination.LESSON_START.name
                }
            },
            onOpenDestination = { destination -> routeName = destination.name },
        )

        StudioDestination.LESSON_START,
        StudioDestination.LESSON_RESUME,
        -> {
            val recommendation = homeModel?.recommendation
            if (recommendation == null) {
                LoadingStudio()
            } else {
                ProductLessonFlow(
                    runtime = lessonRuntime,
                    profile = profile,
                    recommendation = recommendation,
                    resumeRequested = route == StudioDestination.LESSON_RESUME,
                    onExitToHome = { routeName = StudioDestination.HOME.name },
                )
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
