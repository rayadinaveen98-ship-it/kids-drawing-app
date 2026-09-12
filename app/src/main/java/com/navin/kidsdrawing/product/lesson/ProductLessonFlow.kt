package com.navin.kidsdrawing.product.lesson

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.profile.ChildProfile

private enum class ProductLessonStage {
    PREVIEW,
    WORKSPACE,
}

@Composable
fun ProductLessonFlow(
    runtime: ProductLessonRuntime,
    profile: ChildProfile,
    recommendation: LessonRecommendation,
    resumeRequested: Boolean,
    onExitToHome: () -> Unit,
) {
    var stageName by rememberSaveable {
        mutableStateOf(
            if (resumeRequested) ProductLessonStage.WORKSPACE.name
            else ProductLessonStage.PREVIEW.name,
        )
    }
    var modeName by rememberSaveable { mutableStateOf(recommendation.defaultMode.name) }
    var paceName by rememberSaveable { mutableStateOf(recommendation.defaultPace.name) }
    val stage = runCatching { ProductLessonStage.valueOf(stageName) }
        .getOrDefault(ProductLessonStage.PREVIEW)
    val mode = runCatching { TeachingMode.valueOf(modeName) }
        .getOrDefault(recommendation.defaultMode)
    val pace = runCatching { TeachingPace.valueOf(paceName) }
        .getOrDefault(recommendation.defaultPace)

    when (stage) {
        ProductLessonStage.PREVIEW -> LessonPreviewScreen(
            profile = profile,
            recommendation = recommendation,
            packageData = runtime.packageData,
            onBegin = { selectedMode, selectedPace ->
                modeName = selectedMode.name
                paceName = selectedPace.name
                stageName = ProductLessonStage.WORKSPACE.name
            },
            onBack = onExitToHome,
        )

        ProductLessonStage.WORKSPACE -> GuidedLessonScreen(
            runtime = runtime,
            startMode = mode,
            startPace = pace,
            resumeOnly = resumeRequested,
            onExitToHome = onExitToHome,
            onFinishedForNow = onExitToHome,
        )
    }
}
