package com.navin.kidsdrawing.product.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.accessibility.AccessibilityPolicy
import com.navin.kidsdrawing.product.design.StudioChoiceCard
import com.navin.kidsdrawing.product.design.StudioChoiceSelectionMode
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.StudioPrimaryButton
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.ChildProfileDraft
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import com.navin.kidsdrawing.product.profile.OnboardingStep

@Composable
fun OnboardingFlow(
    draft: ChildProfileDraft,
    onDraftChange: (ChildProfileDraft) -> Unit,
    onComplete: (ChildProfile) -> Unit,
) {
    val step = OnboardingStep.fromIndex(draft.currentStepIndex)

    BackHandler(enabled = draft.currentStepIndex > 0) {
        onDraftChange(draft.copy(currentStepIndex = draft.currentStepIndex - 1))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Text(
            text = "Your art studio",
            style = MaterialTheme.typography.bodyMedium,
            color = StudioColors.Studio600,
        )
        Text(
            text = "${draft.currentStepIndex + 1} of ${OnboardingStep.entries.size}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = StudioColors.Ink500,
        )
        Spacer(modifier = Modifier.size(22.dp))

        when (step) {
            OnboardingStep.NICKNAME -> NicknameStep(draft, onDraftChange)
            OnboardingStep.AGE_BAND -> AgeStep(draft, onDraftChange)
            OnboardingStep.TEACHING_MODE -> TeachingModeStep(draft, onDraftChange)
            OnboardingStep.PACE -> PaceStep(draft, onDraftChange)
            OnboardingStep.INTERESTS -> InterestsStep(draft, onDraftChange)
            OnboardingStep.HANDEDNESS -> HandednessStep(draft, onDraftChange)
            OnboardingStep.NARRATION -> NarrationStep(draft, onDraftChange)
        }

        Spacer(modifier = Modifier.size(28.dp))
        StudioPrimaryButton(
            text = if (step == OnboardingStep.NARRATION) "Enter my studio" else "Continue",
            enabled = canContinue(step, draft),
            onClick = {
                if (step == OnboardingStep.NARRATION) {
                    draft.toCompletedProfileOrNull()?.let(onComplete)
                } else {
                    onDraftChange(draft.copy(currentStepIndex = draft.currentStepIndex + 1))
                }
            },
        )
        if (draft.currentStepIndex > 0) {
            Text(
                text = "You can go back anytime. Your choices are saved on this device.",
                modifier = Modifier.padding(top = 14.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = StudioColors.Ink500,
            )
        }
    }
}

@Composable
private fun Question(title: String, subtitle: String? = null) {
    Text(text = title, style = MaterialTheme.typography.headlineLarge)
    if (!subtitle.isNullOrBlank()) {
        Text(
            text = subtitle,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = StudioColors.Ink700,
        )
    } else {
        Spacer(modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun NicknameStep(
    draft: ChildProfileDraft,
    onDraftChange: (ChildProfileDraft) -> Unit,
) {
    var localName by remember(draft.nickname) { mutableStateOf(draft.nickname) }
    Question(
        title = "What should we call you?",
        subtitle = "A nickname is perfect. You don't need to use your full name.",
    )
    OutlinedTextField(
        value = localName,
        onValueChange = { value ->
            if (value.length <= 24) {
                localName = value
                onDraftChange(draft.copy(nickname = value))
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Nickname") },
        supportingText = { Text("1–24 characters") },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
}

@Composable
private fun AgeStep(
    draft: ChildProfileDraft,
    onDraftChange: (ChildProfileDraft) -> Unit,
) {
    Question(
        title = "Which studio feels right for you?",
        subtitle = "This changes button sizes, lesson detail and how much help we show.",
    )
    AgeBand.entries.forEach { ageBand ->
        StudioChoiceCard(
            title = "${ageBand.minAge}–${ageBand.maxAge}  ·  ${ageBand.displayName}",
            subtitle = when (ageBand) {
                AgeBand.LITTLE_ARTIST -> "Big choices, simple steps, help close by"
                AgeBand.CREATIVE_EXPLORER -> "Draw together with a little more freedom"
                AgeBand.GROWING_ARTIST -> "More detail, technique and independence"
                AgeBand.YOUNG_ARTIST -> "A calmer studio with real art vocabulary"
            },
            selected = draft.ageBand == ageBand,
            onClick = { onDraftChange(draft.copy(ageBand = ageBand)) },
            modifier = Modifier.padding(bottom = 12.dp),
            ageBand = draft.ageBand,
        )
    }
}

@Composable
private fun TeachingModeStep(
    draft: ChildProfileDraft,
    onDraftChange: (ChildProfileDraft) -> Unit,
) {
    Question(
        title = "How do you like to learn?",
        subtitle = "You can change this later. This just chooses your starting style.",
    )
    listOf(
        Triple(TeachingMode.DRAW_WITH_ME, "✏️  Draw With Me", "I draw a part, then you draw that part."),
        Triple(TeachingMode.WATCH_THEN_DRAW, "👀  Watch Then Draw", "Watch the whole drawing first, then make yours."),
        Triple(TeachingMode.TRACE_AND_LEARN, "🪄  Trace & Learn", "See a gentle path while you learn the shapes."),
    ).forEach { (mode, title, subtitle) ->
        StudioChoiceCard(
            title = title,
            subtitle = subtitle,
            selected = draft.teachingMode == mode,
            onClick = { onDraftChange(draft.copy(teachingMode = mode)) },
            modifier = Modifier.padding(bottom = 12.dp),
            ageBand = draft.ageBand,
        )
    }
}

@Composable
private fun PaceStep(
    draft: ChildProfileDraft,
    onDraftChange: (ChildProfileDraft) -> Unit,
) {
    Question(
        title = "How fast should I draw?",
        subtitle = "No rush. You can change speed during a lesson too.",
    )
    listOf(
        TeachingPace.EXTRA_SLOW to "Extra slow · 0.4×",
        TeachingPace.SLOW to "Slow · 0.7×",
        TeachingPace.NORMAL to "Normal · 1.0×",
        TeachingPace.FAST to "Fast · 1.5×",
        TeachingPace.VERY_FAST to "Very fast · 2.0×",
    ).forEach { (pace, label) ->
        StudioChoiceCard(
            title = label,
            selected = draft.pace == pace,
            onClick = { onDraftChange(draft.copy(pace = pace)) },
            modifier = Modifier.padding(bottom = 10.dp),
            ageBand = draft.ageBand,
        )
    }
}

@Composable
private fun InterestsStep(
    draft: ChildProfileDraft,
    onDraftChange: (ChildProfileDraft) -> Unit,
) {
    Question(
        title = "What do you love drawing?",
        subtitle = "Pick one or more. We'll use this for your studio recommendations.",
    )
    ChildInterest.entries.forEach { interest ->
        val selected = interest in draft.interests
        StudioChoiceCard(
            title = "${interest.symbol}  ${interest.displayName}",
            selected = selected,
            onClick = {
                val next = if (selected) draft.interests - interest else draft.interests + interest
                onDraftChange(draft.copy(interests = next))
            },
            modifier = Modifier.padding(bottom = 10.dp),
            ageBand = draft.ageBand,
            selectionMode = StudioChoiceSelectionMode.MULTIPLE,
        )
    }
}

@Composable
private fun HandednessStep(
    draft: ChildProfileDraft,
    onDraftChange: (ChildProfileDraft) -> Unit,
) {
    val stackChoices = AccessibilityPolicy.layout(LocalDensity.current.fontScale).avoidFixedTwoColumnCards
    Question(
        title = "Which hand do you draw with most?",
        subtitle = "This preference is saved with your profile. The main drawing tools currently stay below the canvas for both left- and right-handed artists.",
    )
    if (stackChoices) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StudioChoiceCard(
                title = "🤚  Left hand",
                selected = draft.handedness == Handedness.LEFT,
                onClick = { onDraftChange(draft.copy(handedness = Handedness.LEFT)) },
                ageBand = draft.ageBand,
            )
            StudioChoiceCard(
                title = "✋  Right hand",
                selected = draft.handedness == Handedness.RIGHT,
                onClick = { onDraftChange(draft.copy(handedness = Handedness.RIGHT)) },
                ageBand = draft.ageBand,
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StudioChoiceCard(
                title = "🤚  Left hand",
                selected = draft.handedness == Handedness.LEFT,
                onClick = { onDraftChange(draft.copy(handedness = Handedness.LEFT)) },
                modifier = Modifier.weight(1f),
                ageBand = draft.ageBand,
            )
            StudioChoiceCard(
                title = "✋  Right hand",
                selected = draft.handedness == Handedness.RIGHT,
                onClick = { onDraftChange(draft.copy(handedness = Handedness.RIGHT)) },
                modifier = Modifier.weight(1f),
                ageBand = draft.ageBand,
            )
        }
    }
}

@Composable
private fun NarrationStep(
    draft: ChildProfileDraft,
    onDraftChange: (ChildProfileDraft) -> Unit,
) {
    Question(
        title = "Would you like voice instructions?",
        subtitle = "Important instructions are always shown on screen too.",
    )
    StudioChoiceCard(
        title = "🔊  Voice + text",
        subtitle = "Hear short instructions and see them on screen.",
        selected = draft.narrationPreference == NarrationPreference.VOICE_AND_TEXT,
        onClick = { onDraftChange(draft.copy(narrationPreference = NarrationPreference.VOICE_AND_TEXT)) },
        modifier = Modifier.padding(bottom = 12.dp),
        ageBand = draft.ageBand,
    )
    StudioChoiceCard(
        title = "💬  Text only",
        subtitle = "Keep the studio quiet and read the same instructions.",
        selected = draft.narrationPreference == NarrationPreference.TEXT_ONLY,
        onClick = { onDraftChange(draft.copy(narrationPreference = NarrationPreference.TEXT_ONLY)) },
        ageBand = draft.ageBand,
    )
}

private fun canContinue(step: OnboardingStep, draft: ChildProfileDraft): Boolean = when (step) {
    OnboardingStep.NICKNAME -> draft.isNicknameValid()
    OnboardingStep.AGE_BAND -> draft.ageBand != null
    OnboardingStep.TEACHING_MODE -> draft.teachingMode != null
    OnboardingStep.PACE -> draft.pace != null
    OnboardingStep.INTERESTS -> draft.interests.isNotEmpty()
    OnboardingStep.HANDEDNESS -> draft.handedness != null
    OnboardingStep.NARRATION -> draft.toCompletedProfileOrNull() != null
}
