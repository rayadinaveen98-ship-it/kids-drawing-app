package com.navin.kidsdrawing.product.lesson

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.gallery.GalleryAwareColoringWorkspace
import com.navin.kidsdrawing.product.gallery.GalleryAwareGuidedLessonWorkspace
import com.navin.kidsdrawing.product.gallery.ProductGalleryRuntime
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.profile.ChildProfile

private enum class ProductLessonStage {
    PREVIEW,
    WORKSPACE,
    COLORING,
}

@Composable
fun ProductLessonFlow(
    runtime: ProductLessonRuntime,
    coloringRuntime: ProductColoringRuntime,
    galleryRuntime: ProductGalleryRuntime,
    profile: ChildProfile,
    recommendation: LessonRecommendation,
    resumeRequested: Boolean,
    onArtworkCompleted: (String) -> Unit,
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
    var startFreshRequested by rememberSaveable { mutableStateOf(!resumeRequested) }
    val coloringSessionState by coloringRuntime.sessionState.collectAsState()
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
                startFreshRequested = true
                stageName = ProductLessonStage.WORKSPACE.name
            },
            onBack = onExitToHome,
        )

        ProductLessonStage.WORKSPACE -> GalleryAwareGuidedLessonWorkspace(
            lessonRuntime = runtime,
            coloringRuntime = coloringRuntime,
            galleryRuntime = galleryRuntime,
            ageBand = profile.ageBand,
            artworkTitle = recommendation.title,
            startMode = mode,
            startPace = pace,
            startFreshRequested = startFreshRequested,
            onFreshSessionStarted = { startFreshRequested = false },
            onColoringReady = { stageName = ProductLessonStage.COLORING.name },
            onArtworkCompleted = onArtworkCompleted,
            onExitToHome = onExitToHome,
        )

        ProductLessonStage.COLORING -> GalleryAwareColoringWorkspace(
            coloringRuntime = coloringRuntime,
            galleryRuntime = galleryRuntime,
            ageBand = profile.ageBand,
            artworkTitle = recommendation.title,
            // After Activity/process recreation the saveable stage survives but the in-memory
            // runtime does not; the workspace therefore restores from the semantic coloring store.
            recoverRequested = coloringSessionState == null,
            onArtworkCompleted = onArtworkCompleted,
            onExitToHome = onExitToHome,
        )
    }
}
