package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonActiveContext
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonAuthoredTextPolicyTest {
    @Test
    fun teacherDemonstrationUsesExactStepNarrationKey() {
        val packageData = cuteCat()
        val context = context(stepIndex = 0, stepId = "head")

        val key = LessonAuthoredTextPolicy.keyFor(
            LessonSessionState.TeacherDemonstrating(context, requestId = "teacher-head"),
            packageData,
        )

        assertEquals("lesson.cute_cat.step.head", key)
    }

    @Test
    fun activeHelpUsesExactAuthoredHelpLevelNarrationKey() {
        val packageData = cuteCat()
        val context = context(stepIndex = 0, stepId = "head").copy(helpLevel = 1)

        val key = LessonAuthoredTextPolicy.keyFor(
            LessonSessionState.HelpActive(context),
            packageData,
        )

        assertEquals("lesson.cute_cat.help.head_hint", key)
    }

    @Test
    fun visualOnlyHelpDoesNotInventText() {
        val packageData = cuteCat()
        val context = context(stepIndex = 0, stepId = "head").copy(helpLevel = 4)

        val key = LessonAuthoredTextPolicy.keyFor(
            LessonSessionState.HelpActive(context),
            packageData,
        )

        assertNull(key)
    }

    @Test
    fun finalDrawingBoundaryUsesAuthoredCompletionNarration() {
        val packageData = cuteCat()
        val context = context(stepIndex = 3, stepId = "body_tail")

        val key = LessonAuthoredTextPolicy.keyFor(
            LessonSessionState.DrawingComplete(context),
            packageData,
        )

        assertEquals("lesson.cute_cat.complete.drawing", key)
    }

    @Test
    fun childTurnWithoutActiveAuthoredMessageKeepsGenericGuidanceOnly() {
        val packageData = cuteCat()
        val state = LessonSessionState.AwaitingChild(context(stepIndex = 1, stepId = "ears"))

        assertNull(LessonAuthoredTextPolicy.keyFor(state, packageData))
    }

    @Test
    fun mismatchedStepIdentityNeverGuessesAuthoredText() {
        val packageData = cuteCat()
        val mismatched = context(stepIndex = 0, stepId = "ears")

        assertNull(
            LessonAuthoredTextPolicy.keyFor(
                LessonSessionState.TeacherDemonstrating(mismatched, requestId = "bad"),
                packageData,
            ),
        )
    }

    private fun context(stepIndex: Int, stepId: String) = LessonActiveContext(
        mode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        currentStepIndex = stepIndex,
        currentStepId = stepId,
        helpLevel = 0,
        overviewCompleted = true,
    )

    private fun cuteCat(): LessonRuntimePackage {
        val loader = LessonPackageLoader(
            LessonPackageSource { path ->
                File("src/main/assets/$path").takeIf(File::isFile)?.readText()
            },
        )
        val loaded = loader.load("lessons/cute-cat")
        assertTrue("Expected Cute Cat to load, got $loaded", loaded is LessonLoadResult.Success)
        return (loaded as LessonLoadResult.Success).packageData
    }
}
