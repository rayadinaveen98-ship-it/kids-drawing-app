package com.navin.kidsdrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.StudioTheme
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
        is StartupState.Home -> HomeShell(current.profile)
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

@Composable
private fun HomeShell(profile: ChildProfile) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 22.dp, vertical = 24.dp),
        ) {
            Text(
                text = "Hi ${profile.nickname} 👋",
                style = MaterialTheme.typography.headlineLarge,
                color = StudioColors.Ink900,
            )
            Text(
                text = "Your studio is ready.",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )

            Spacer(modifier = Modifier.size(28.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = StudioColors.Studio100,
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "🎨  Ready to make something?",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Picked-for-you lessons and Continue Drawing arrive in the next Phase 3 slice.",
                        modifier = Modifier.padding(top = 10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = StudioColors.Ink700,
                    )
                    Text(
                        text = "Starting style: ${profile.teachingMode.name.replace('_', ' ').lowercase()} · ${profile.pace.name.replace('_', ' ').lowercase()}",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioColors.Studio600,
                    )
                }
            }
        }
    }
}
