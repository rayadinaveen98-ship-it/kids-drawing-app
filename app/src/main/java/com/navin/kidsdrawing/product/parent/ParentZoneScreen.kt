package com.navin.kidsdrawing.product.parent

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.accessibility.AccessibilityPreferences
import com.navin.kidsdrawing.product.design.StudioChoiceCard
import com.navin.kidsdrawing.product.design.StudioChoiceSelectionMode
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.StudioPrimaryButton
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ParentZoneSection(
    val title: String,
    val subtitle: String,
) {
    FAMILY("Family", "Manage the one local child profile on this device"),
    LEARNING("Learning", "Understand learning activity without grades or rankings"),
    ACCESSIBILITY_AUDIO("Accessibility & Audio", "Motion, system text-size guidance and narration defaults"),
    STORAGE_DATA("Storage & Data", "Understand what is stored locally and what is coming later"),
    SAFETY_PRIVACY("Safety & Privacy", "See the app's offline, account and permission boundaries"),
    ABOUT("About", "Version and release information"),
}

@Composable
fun ParentGateScreen(
    session: ParentAccessSession,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
) {
    var isHolding by remember { mutableStateOf(false) }
    var fallbackStep by remember { mutableStateOf(0) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isHolding, reduceMotion) {
        if (!isHolding) {
            session.cancelHold()
            progress.snapTo(0f)
            return@LaunchedEffect
        }

        session.beginHold()
        progress.snapTo(0f)
        if (reduceMotion) {
            delay(ParentAccessSession.HOLD_DURATION_MILLIS)
        } else {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ParentAccessSession.HOLD_DURATION_MILLIS.toInt(),
                    easing = LinearEasing,
                ),
            )
        }
        if (isHolding && session.completeHoldIfEligible()) {
            onUnlocked()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Grown-ups area",
                style = MaterialTheme.typography.headlineLarge,
                color = StudioColors.Ink900,
            )
            Text(
                text = "This area changes family settings and explains local data. The hold below is an adult-intent check, not a password or identity check.",
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 76.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Hold to enter Parent Zone for two and a half seconds"
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHolding = true
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    isHolding = false
                                }
                            },
                        )
                    },
                color = if (isHolding) StudioColors.Studio100 else StudioColors.Paper100,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, StudioColors.Studio600),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = if (isHolding) "Keep holding…" else "Hold to enter Parent Zone",
                        style = MaterialTheme.typography.titleLarge,
                        color = StudioColors.Ink900,
                    )
                    if (reduceMotion) {
                        Text(
                            text = if (isHolding) "Timed hold in progress" else "Timed hold ready",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioColors.Ink700,
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { progress.value },
                            modifier = Modifier.fillMaxWidth(),
                            color = StudioColors.Studio600,
                            trackColor = StudioColors.Line200,
                        )
                    }
                    Text(
                        text = "Hold continuously for 2.5 seconds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioColors.Ink700,
                    )
                }
            }

            when (fallbackStep) {
                0 -> TextButton(
                    onClick = { fallbackStep = 1 },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Use accessible confirmation instead")
                }

                1 -> ParentInfoCard(
                    title = "Accessible confirmation",
                    body = "Use this path if a timed hold is difficult with your input method or accessibility setup.",
                ) {
                    StudioPrimaryButton(
                        text = "Continue to adult controls",
                        onClick = { fallbackStep = 2 },
                    )
                    TextButton(
                        onClick = { fallbackStep = 0 },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Cancel accessible confirmation")
                    }
                }

                else -> ParentInfoCard(
                    title = "Open Parent Zone?",
                    body = "Confirm that you want to enter the grown-ups area on this device.",
                ) {
                    StudioPrimaryButton(
                        text = "Yes, open Parent Zone",
                        onClick = {
                            session.completeAccessibleConfirmation()
                            onUnlocked()
                        },
                    )
                    TextButton(
                        onClick = { fallbackStep = 0 },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Cancel")
                    }
                }
            }

            TextButton(
                onClick = onCancel,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text("Back to the art studio")
            }
        }
    }
}

@Composable
fun ParentZoneScreen(
    profile: ChildProfile,
    appVersion: String,
    onSaveProfile: suspend (ChildProfile) -> Boolean,
    onReturnToChild: () -> Unit,
    modifier: Modifier = Modifier,
    accessibilityPreferences: AccessibilityPreferences = AccessibilityPreferences(),
    onSetReduceMotion: suspend (Boolean) -> Boolean = { false },
) {
    var section by remember { mutableStateOf<ParentZoneSection?>(null) }

    if (section == null) {
        ParentZoneOverview(
            profile = profile,
            onOpenSection = { section = it },
            onReturnToChild = onReturnToChild,
            modifier = modifier,
        )
        return
    }

    when (section) {
        ParentZoneSection.FAMILY -> ParentProfileEditor(
            profile = profile,
            onSaveProfile = onSaveProfile,
            onBack = { section = null },
            modifier = modifier,
        )
        ParentZoneSection.LEARNING -> ParentProgressRoute(
            profile = profile,
            onBack = { section = null },
            modifier = modifier,
        )
        ParentZoneSection.ACCESSIBILITY_AUDIO -> ParentAccessibilityScreen(
            narrationPreference = profile.narrationPreference,
            preferences = accessibilityPreferences,
            onSetReduceMotion = onSetReduceMotion,
            onBack = { section = null },
            modifier = modifier,
        )
        ParentZoneSection.STORAGE_DATA -> ParentSectionDetail(
            title = "Storage & Data",
            body = "Profiles, progress and artwork are stored locally on this device. Bulk reset and recovery controls are deliberately deferred to P6.6 so destructive actions are not added before their exact ownership and recovery rules are implemented.",
            onBack = { section = null },
            modifier = modifier,
        )
        ParentZoneSection.SAFETY_PRIVACY -> ParentSectionDetail(
            title = "Safety & Privacy",
            body = "Core learning works offline. This version requires no parent or child account, has no advertising SDK and does not upload behavioral analytics. The current Android manifest requests no runtime permissions. Sharing outside the app is not enabled in this Parent Zone foundation slice.",
            onBack = { section = null },
            modifier = modifier,
        )
        ParentZoneSection.ABOUT -> ParentSectionDetail(
            title = "About",
            body = "Kids Drawing App\nVersion: $appVersion\n\nPhase 6 is building family readiness while preserving the accepted offline drawing and lesson experience.",
            onBack = { section = null },
            modifier = modifier,
        )
        null -> Unit
    }
}

@Composable
private fun ParentZoneOverview(
    profile: ChildProfile,
    onOpenSection: (ParentZoneSection) -> Unit,
    onReturnToChild: () -> Unit,
    modifier: Modifier = Modifier,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Parent Zone",
                        style = MaterialTheme.typography.headlineLarge,
                        color = StudioColors.Ink900,
                    )
                    Text(
                        text = "Local controls for ${profile.nickname}'s studio",
                        style = MaterialTheme.typography.bodyLarge,
                        color = StudioColors.Ink700,
                    )
                }
                TextButton(
                    onClick = onReturnToChild,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Back to child mode")
                }
            }

            ParentInfoCard(
                title = "One local profile in 0.6",
                body = "This release safely manages the existing child profile. Multiple-child switching is not being added until all artwork, sessions and progress data have a deliberate ownership migration.",
            )

            ParentZoneSection.entries.forEach { item ->
                StudioChoiceCard(
                    title = item.title,
                    subtitle = item.subtitle,
                    selected = false,
                    onClick = { onOpenSection(item) },
                    ageBand = AgeBand.YOUNG_ARTIST,
                    selectionMode = StudioChoiceSelectionMode.NAVIGATION,
                )
            }
        }
    }
}

@Composable
private fun ParentProfileEditor(
    profile: ChildProfile,
    onSaveProfile: suspend (ChildProfile) -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editor by remember(profile) { mutableStateOf(ParentProfileEditorState.from(profile)) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ParentScrollableSurface(modifier = modifier) {
        ParentHeader(title = "Family", onBack = onBack)
        Text(
            text = "Changes are kept as a draft until you choose Save. Cancel leaves the accepted profile untouched.",
            style = MaterialTheme.typography.bodyMedium,
            color = StudioColors.Ink700,
        )

        OutlinedTextField(
            value = editor.nicknameInput,
            onValueChange = { editor = editor.copy(nicknameInput = it.take(40)); saveError = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nickname") },
            supportingText = {
                Text(
                    if (editor.isNicknameValid) "1–24 characters after spaces are cleaned up"
                    else "Enter a nickname from 1 to 24 characters",
                )
            },
            isError = !editor.isNicknameValid,
            singleLine = true,
        )

        ParentChoiceGroup("Age band") {
            AgeBand.entries.forEach { option ->
                StudioChoiceCard(
                    title = option.displayName,
                    subtitle = "Ages ${option.minAge}–${option.maxAge}",
                    selected = editor.ageBand == option,
                    onClick = { editor = editor.copy(ageBand = option); saveError = null },
                    ageBand = AgeBand.YOUNG_ARTIST,
                )
            }
        }

        ParentChoiceGroup("Default learning style") {
            TeachingMode.entries.forEach { option ->
                StudioChoiceCard(
                    title = teachingModeLabel(option),
                    selected = editor.teachingMode == option,
                    onClick = { editor = editor.copy(teachingMode = option); saveError = null },
                    ageBand = AgeBand.YOUNG_ARTIST,
                )
            }
        }

        ParentChoiceGroup("Default pace") {
            TeachingPace.entries.forEach { option ->
                StudioChoiceCard(
                    title = teachingPaceLabel(option),
                    selected = editor.pace == option,
                    onClick = { editor = editor.copy(pace = option); saveError = null },
                    ageBand = AgeBand.YOUNG_ARTIST,
                )
            }
        }

        ParentChoiceGroup("Interests — choose at least one") {
            ChildInterest.entries.forEach { option ->
                val selected = option in editor.interests
                StudioChoiceCard(
                    title = "${option.symbol} ${option.displayName}",
                    selected = selected,
                    onClick = {
                        val next = if (selected) editor.interests - option else editor.interests + option
                        editor = editor.copy(interests = next)
                        saveError = null
                    },
                    ageBand = AgeBand.YOUNG_ARTIST,
                    selectionMode = StudioChoiceSelectionMode.MULTIPLE,
                )
            }
        }

        ParentChoiceGroup("Handedness") {
            Handedness.entries.forEach { option ->
                StudioChoiceCard(
                    title = option.displayName,
                    selected = editor.handedness == option,
                    onClick = { editor = editor.copy(handedness = option); saveError = null },
                    ageBand = AgeBand.YOUNG_ARTIST,
                )
            }
        }

        ParentChoiceGroup("Narration default") {
            NarrationPreference.entries.forEach { option ->
                StudioChoiceCard(
                    title = option.displayName,
                    selected = editor.narrationPreference == option,
                    onClick = { editor = editor.copy(narrationPreference = option); saveError = null },
                    ageBand = AgeBand.YOUNG_ARTIST,
                )
            }
        }

        saveError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = StudioColors.Ink700,
            )
        }

        StudioPrimaryButton(
            text = if (saving) "Saving…" else "Save profile",
            enabled = editor.canSave && !saving,
            onClick = {
                val nextProfile = editor.toProfileOrNull() ?: return@StudioPrimaryButton
                saving = true
                saveError = null
                scope.launch {
                    val saved = onSaveProfile(nextProfile)
                    saving = false
                    if (saved) {
                        onBack()
                    } else {
                        saveError = "We couldn't save that change. The previous profile is still safe."
                    }
                }
            },
        )
        TextButton(
            onClick = onBack,
            enabled = !saving,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text("Cancel changes")
        }
    }
}

@Composable
private fun ParentSectionDetail(
    title: String,
    body: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ParentScrollableSurface(modifier = modifier) {
        ParentHeader(title = title, onBack = onBack)
        ParentInfoCard(title = "What this means", body = body)
    }
}

@Composable
private fun ParentScrollableSurface(
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
            content()
        }
    }
}

@Composable
private fun ParentHeader(
    title: String,
    onBack: () -> Unit,
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
}

@Composable
private fun ParentChoiceGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = StudioColors.Ink900,
        )
        content()
    }
}

@Composable
private fun ParentInfoCard(
    title: String,
    body: String,
    content: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = StudioColors.Ink900,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
            content?.invoke()
        }
    }
}

private fun teachingModeLabel(mode: TeachingMode): String = when (mode) {
    TeachingMode.DRAW_WITH_ME -> "Draw With Me"
    TeachingMode.WATCH_THEN_DRAW -> "Watch Then Draw"
    TeachingMode.TRACE_AND_LEARN -> "Trace & Learn"
}

private fun teachingPaceLabel(pace: TeachingPace): String = when (pace) {
    TeachingPace.EXTRA_SLOW -> "Extra Slow"
    TeachingPace.SLOW -> "Slow"
    TeachingPace.NORMAL -> "Normal"
    TeachingPace.FAST -> "Fast"
    TeachingPace.VERY_FAST -> "Very Fast"
}

fun currentAppVersionName(context: Context): String = runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
}.getOrDefault("").ifBlank { "Unknown" }
