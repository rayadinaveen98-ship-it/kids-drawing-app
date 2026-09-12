package com.navin.kidsdrawing.product.profile

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode

enum class AgeBand(
    val minAge: Int,
    val maxAge: Int,
    val displayName: String,
) {
    LITTLE_ARTIST(4, 5, "Little Artist"),
    CREATIVE_EXPLORER(6, 7, "Creative Explorer"),
    GROWING_ARTIST(8, 9, "Growing Artist"),
    YOUNG_ARTIST(10, 12, "Young Artist"),
}

enum class ChildInterest(val displayName: String, val symbol: String) {
    ANIMALS("Animals", "🐾"),
    VEHICLES("Vehicles", "🚗"),
    NATURE("Nature", "🌿"),
    CHARACTERS("Characters", "🎭"),
    SPACE("Space", "🚀"),
    FANTASY("Fantasy", "✨"),
}

enum class Handedness(val displayName: String) {
    LEFT("Left hand"),
    RIGHT("Right hand"),
}

enum class NarrationPreference(val displayName: String) {
    VOICE_AND_TEXT("Voice + text"),
    TEXT_ONLY("Text only"),
}

enum class OnboardingStep {
    NICKNAME,
    AGE_BAND,
    TEACHING_MODE,
    PACE,
    INTERESTS,
    HANDEDNESS,
    NARRATION,
    ;

    companion object {
        fun fromIndex(index: Int): OnboardingStep = entries[index.coerceIn(0, entries.lastIndex)]
    }
}

data class ChildProfileDraft(
    val nickname: String = "",
    val ageBand: AgeBand? = null,
    val teachingMode: TeachingMode? = null,
    val pace: TeachingPace? = null,
    val interests: Set<ChildInterest> = emptySet(),
    val handedness: Handedness? = null,
    val narrationPreference: NarrationPreference? = null,
    val currentStepIndex: Int = 0,
) {
    val normalizedNickname: String
        get() = nickname.trim().replace(Regex("\\s+"), " ")

    fun isNicknameValid(): Boolean = normalizedNickname.length in 1..24

    fun toCompletedProfileOrNull(): ChildProfile? {
        if (!isNicknameValid()) return null
        val resolvedAgeBand = ageBand ?: return null
        val resolvedMode = teachingMode ?: return null
        val resolvedPace = pace ?: return null
        if (interests.isEmpty()) return null
        val resolvedHandedness = handedness ?: return null
        val resolvedNarration = narrationPreference ?: return null

        return ChildProfile(
            nickname = normalizedNickname,
            ageBand = resolvedAgeBand,
            teachingMode = resolvedMode,
            pace = resolvedPace,
            interests = interests,
            handedness = resolvedHandedness,
            narrationPreference = resolvedNarration,
        )
    }
}

data class ChildProfile(
    val nickname: String,
    val ageBand: AgeBand,
    val teachingMode: TeachingMode,
    val pace: TeachingPace,
    val interests: Set<ChildInterest>,
    val handedness: Handedness,
    val narrationPreference: NarrationPreference,
)

internal inline fun <reified T : Enum<T>> enumValueOrNull(raw: String?): T? =
    raw?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
