package com.navin.kidsdrawing.product.parent

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentProfileEditorStateTest {
    private val original = ChildProfile(
        nickname = "Mira",
        ageBand = AgeBand.CREATIVE_EXPLORER,
        teachingMode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        interests = setOf(ChildInterest.ANIMALS, ChildInterest.NATURE),
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
    )

    @Test
    fun `editor starts as independent copy of accepted profile`() {
        val editor = ParentProfileEditorState.from(original)

        assertEquals(original, editor.toProfileOrNull())
    }

    @Test
    fun `nickname is normalized only when producing committed profile`() {
        val editor = ParentProfileEditorState.from(original).copy(
            nicknameInput = "  Mira    Rose  ",
        )

        assertEquals("Mira Rose", editor.normalizedNickname)
        assertEquals("Mira Rose", editor.toProfileOrNull()?.nickname)
        assertEquals("Mira", original.nickname)
    }

    @Test
    fun `blank nickname cannot be committed`() {
        val editor = ParentProfileEditorState.from(original).copy(nicknameInput = "   ")

        assertFalse(editor.canSave)
        assertNull(editor.toProfileOrNull())
    }

    @Test
    fun `nickname longer than twenty four characters cannot be committed`() {
        val editor = ParentProfileEditorState.from(original).copy(
            nicknameInput = "a".repeat(25),
        )

        assertFalse(editor.canSave)
        assertNull(editor.toProfileOrNull())
    }

    @Test
    fun `at least one interest remains required`() {
        val editor = ParentProfileEditorState.from(original).copy(interests = emptySet())

        assertFalse(editor.canSave)
        assertNull(editor.toProfileOrNull())
    }

    @Test
    fun `all supported defaults can be changed in one resulting profile`() {
        val editor = ParentProfileEditorState.from(original).copy(
            nicknameInput = "Mira Rose",
            ageBand = AgeBand.GROWING_ARTIST,
            teachingMode = TeachingMode.WATCH_THEN_DRAW,
            pace = TeachingPace.SLOW,
            interests = setOf(ChildInterest.SPACE),
            handedness = Handedness.LEFT,
            narrationPreference = NarrationPreference.TEXT_ONLY,
        )

        val updated = editor.toProfileOrNull()

        assertTrue(editor.canSave)
        assertEquals("Mira Rose", updated?.nickname)
        assertEquals(AgeBand.GROWING_ARTIST, updated?.ageBand)
        assertEquals(TeachingMode.WATCH_THEN_DRAW, updated?.teachingMode)
        assertEquals(TeachingPace.SLOW, updated?.pace)
        assertEquals(setOf(ChildInterest.SPACE), updated?.interests)
        assertEquals(Handedness.LEFT, updated?.handedness)
        assertEquals(NarrationPreference.TEXT_ONLY, updated?.narrationPreference)
    }

    @Test
    fun `abandoned editor never mutates original profile`() {
        ParentProfileEditorState.from(original).copy(
            nicknameInput = "Different",
            ageBand = AgeBand.YOUNG_ARTIST,
            interests = setOf(ChildInterest.FANTASY),
        )

        assertEquals("Mira", original.nickname)
        assertEquals(AgeBand.CREATIVE_EXPLORER, original.ageBand)
        assertEquals(setOf(ChildInterest.ANIMALS, ChildInterest.NATURE), original.interests)
    }
}
