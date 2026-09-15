package com.navin.kidsdrawing.product.parent

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference

data class ParentProfileEditorState(
    val nicknameInput: String,
    val ageBand: AgeBand,
    val teachingMode: TeachingMode,
    val pace: TeachingPace,
    val interests: Set<ChildInterest>,
    val handedness: Handedness,
    val narrationPreference: NarrationPreference,
) {
    val normalizedNickname: String
        get() = nicknameInput.trim().replace(Regex("\\s+"), " ")

    val isNicknameValid: Boolean
        get() = normalizedNickname.length in 1..24

    val canSave: Boolean
        get() = isNicknameValid && interests.isNotEmpty()

    fun toProfileOrNull(): ChildProfile? {
        if (!canSave) return null
        return ChildProfile(
            nickname = normalizedNickname,
            ageBand = ageBand,
            teachingMode = teachingMode,
            pace = pace,
            interests = interests,
            handedness = handedness,
            narrationPreference = narrationPreference,
        )
    }

    companion object {
        fun from(profile: ChildProfile): ParentProfileEditorState = ParentProfileEditorState(
            nicknameInput = profile.nickname,
            ageBand = profile.ageBand,
            teachingMode = profile.teachingMode,
            pace = profile.pace,
            interests = profile.interests,
            handedness = profile.handedness,
            narrationPreference = profile.narrationPreference,
        )
    }
}
