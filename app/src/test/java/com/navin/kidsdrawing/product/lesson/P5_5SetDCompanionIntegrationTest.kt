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

class P5_5SetDCompanionIntegrationTest {
    @Test
    fun setDOpenAuthorshipTurnsUseGenericNonScoringCompanionPresentation() {
        val snapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
        val cases = listOf(
            Triple("snail-garden", "make_garden_yours", AgeBand.LITTLE_ARTIST),
            Triple("elephant-from-shapes", "make_elephant_yours", AgeBand.YOUNG_ARTIST),
            Triple("simple-car", "design_your_car", AgeBand.CREATIVE_EXPLORER),
            Triple("sailboat-scene", "make_scene_yours", AgeBand.YOUNG_ARTIST),
            Triple("planet-with-rings", "design_your_planet", AgeBand.GROWING_ARTIST),
            Triple("friendly-alien", "give_it_personality", AgeBand.YOUNG_ARTIST),
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
