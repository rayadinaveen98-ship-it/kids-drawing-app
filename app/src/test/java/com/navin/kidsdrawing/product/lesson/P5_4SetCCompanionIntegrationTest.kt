package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonCatalog
import com.navin.kidsdrawing.lesson.content.LessonCatalogSource
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonActiveContext
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.product.profile.AgeBand
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class P5_4SetCCompanionIntegrationTest {
    @Test
    fun setCOpenAuthorshipTurnsUseGenericNonScoringCompanionPresentation() {
        val snapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
        val cases = listOf(
            Triple("happy-lines", "make_marks_yours", AgeBand.LITTLE_ARTIST),
            Triple("shape-friends", "make_friend_yours", AgeBand.CREATIVE_EXPLORER),
            Triple("rainbow-weather", "weather_details", AgeBand.LITTLE_ARTIST),
            Triple("tree-through-seasons", "season_story", AgeBand.YOUNG_ARTIST),
            Triple("ice-cream-shop", "make_it_yours", AgeBand.GROWING_ARTIST),
        )

        cases.forEach { (lessonId, stepId, ageBand) ->
            val entry = checkNotNull(snapshot.byLessonId(lessonId).singleOrNull())
            val runtime = checkNotNull(snapshot.runtimePackage(entry.identity))
            val stepIndex = runtime.lesson.drawing.steps.indexOfFirst { it.id == stepId }
            assertTrue("Missing open step $lessonId/$stepId", stepIndex >= 0)
            val state = LessonSessionState.AwaitingChild(
                LessonActiveContext(
                    mode = TeachingMode.DRAW_WITH_ME,
                    pace = TeachingPace.NORMAL,
                    currentStepIndex = stepIndex,
                    currentStepId = stepId,
                    helpLevel = 0,
                    overviewCompleted = true,
                ),
            )

            val presentation = ProductLessonPresentationPolicy.from(state, runtime, ageBand)
            val text = listOfNotNull(presentation.instruction, presentation.secondaryCue).joinToString(" ").lowercase()
            assertTrue("$lessonId open choice must expose Done", presentation.showDone)
            assertTrue("$lessonId open choice must preserve optional Skip", presentation.showSkipStep)
            assertTrue(
                "$lessonId did not receive generic authorship language: $text",
                listOf("choice", "choose", "invent", "details", "your way").any(text::contains),
            )
            listOf("copy", "match", "score", "points", "stars", "xp", "rank", "grade", "accuracy").forEach { forbidden ->
                assertFalse("$lessonId exposed forbidden '$forbidden' language: $text", text.contains(forbidden))
            }
        }
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
