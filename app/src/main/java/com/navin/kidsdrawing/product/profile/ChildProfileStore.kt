package com.navin.kidsdrawing.product.profile

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.coroutines.flow.first

private val Context.childProfileDataStore by preferencesDataStore(name = "child_profile")

class ChildProfileStore(
    private val context: Context,
) {
    suspend fun loadDraft(): ChildProfileDraft {
        val prefs = context.childProfileDataStore.data.first()
        return ChildProfileDraft(
            nickname = prefs[Keys.NICKNAME].orEmpty(),
            ageBand = enumValueOrNull(prefs[Keys.AGE_BAND]),
            teachingMode = enumValueOrNull(prefs[Keys.TEACHING_MODE]),
            pace = enumValueOrNull(prefs[Keys.PACE]),
            interests = decodeInterests(prefs[Keys.INTERESTS]),
            handedness = enumValueOrNull(prefs[Keys.HANDEDNESS]),
            narrationPreference = enumValueOrNull(prefs[Keys.NARRATION]),
            currentStepIndex = prefs[Keys.STEP_INDEX]
                ?.coerceIn(0, OnboardingStep.entries.lastIndex)
                ?: 0,
        )
    }

    suspend fun loadCompletedProfile(): ChildProfile? {
        val prefs = context.childProfileDataStore.data.first()
        if (prefs[Keys.COMPLETE] != true) return null
        return draftFromPreferences(prefs).toCompletedProfileOrNull()
    }

    suspend fun saveDraft(draft: ChildProfileDraft) {
        context.childProfileDataStore.edit { prefs ->
            prefs[Keys.NICKNAME] = draft.nickname
            putEnumOrRemove(prefs, Keys.AGE_BAND, draft.ageBand)
            putEnumOrRemove(prefs, Keys.TEACHING_MODE, draft.teachingMode)
            putEnumOrRemove(prefs, Keys.PACE, draft.pace)
            prefs[Keys.INTERESTS] = encodeInterests(draft.interests)
            putEnumOrRemove(prefs, Keys.HANDEDNESS, draft.handedness)
            putEnumOrRemove(prefs, Keys.NARRATION, draft.narrationPreference)
            prefs[Keys.STEP_INDEX] = draft.currentStepIndex
                .coerceIn(0, OnboardingStep.entries.lastIndex)
            prefs[Keys.COMPLETE] = false
        }
    }

    suspend fun saveCompletedProfile(profile: ChildProfile) {
        context.childProfileDataStore.edit { prefs ->
            prefs[Keys.NICKNAME] = profile.nickname
            prefs[Keys.AGE_BAND] = profile.ageBand.name
            prefs[Keys.TEACHING_MODE] = profile.teachingMode.name
            prefs[Keys.PACE] = profile.pace.name
            prefs[Keys.INTERESTS] = encodeInterests(profile.interests)
            prefs[Keys.HANDEDNESS] = profile.handedness.name
            prefs[Keys.NARRATION] = profile.narrationPreference.name
            prefs[Keys.STEP_INDEX] = OnboardingStep.entries.lastIndex
            prefs[Keys.COMPLETE] = true
        }
    }

    private fun draftFromPreferences(prefs: Preferences): ChildProfileDraft = ChildProfileDraft(
        nickname = prefs[Keys.NICKNAME].orEmpty(),
        ageBand = enumValueOrNull(prefs[Keys.AGE_BAND]),
        teachingMode = enumValueOrNull(prefs[Keys.TEACHING_MODE]),
        pace = enumValueOrNull(prefs[Keys.PACE]),
        interests = decodeInterests(prefs[Keys.INTERESTS]),
        handedness = enumValueOrNull(prefs[Keys.HANDEDNESS]),
        narrationPreference = enumValueOrNull(prefs[Keys.NARRATION]),
        currentStepIndex = prefs[Keys.STEP_INDEX]
            ?.coerceIn(0, OnboardingStep.entries.lastIndex)
            ?: 0,
    )

    private fun decodeInterests(raw: String?): Set<ChildInterest> = raw
        ?.split('|')
        ?.mapNotNull { enumValueOrNull<ChildInterest>(it) }
        ?.toSet()
        .orEmpty()

    private fun encodeInterests(interests: Set<ChildInterest>): String = interests
        .sortedBy { it.ordinal }
        .joinToString("|") { it.name }

    private fun <T : Enum<T>> putEnumOrRemove(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        key: Preferences.Key<String>,
        value: T?,
    ) {
        if (value == null) prefs.remove(key) else prefs[key] = value.name
    }

    private object Keys {
        val NICKNAME = stringPreferencesKey("nickname")
        val AGE_BAND = stringPreferencesKey("age_band")
        val TEACHING_MODE = stringPreferencesKey("teaching_mode")
        val PACE = stringPreferencesKey("pace")
        val INTERESTS = stringPreferencesKey("interests")
        val HANDEDNESS = stringPreferencesKey("handedness")
        val NARRATION = stringPreferencesKey("narration")
        val STEP_INDEX = intPreferencesKey("onboarding_step")
        val COMPLETE = booleanPreferencesKey("profile_complete")
    }
}
