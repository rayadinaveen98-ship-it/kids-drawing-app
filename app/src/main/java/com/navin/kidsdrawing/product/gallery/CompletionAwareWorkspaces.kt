package com.navin.kidsdrawing.product.gallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionResult
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.product.coloring.ColoringWorkspaceScreen
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.coloring.ProductColoringStartResult
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.lesson.GuidedLessonScreen
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import com.navin.kidsdrawing.product.profile.AgeBand
import kotlinx.coroutines.launch

/** Product wrapper that owns the drawing-completion boundary without changing verified lesson UI. */
@Composable
fun GalleryAwareGuidedLessonWorkspace(
    lessonRuntime: ProductLessonRuntime,
    coloringRuntime: ProductColoringRuntime,
    galleryRuntime: ProductGalleryRuntime,
    ageBand: AgeBand,
    artworkTitle: String,
    startMode: TeachingMode,
    startPace: TeachingPace,
    startFreshRequested: Boolean,
    onFreshSessionStarted: () -> Unit,
    onColoringReady: () -> Unit,
    onArtworkCompleted: (String) -> Unit,
    onExitToHome: () -> Unit,
) {
    val sessionState by lessonRuntime.sessionState.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        GuidedLessonScreen(
            runtime = lessonRuntime,
            coloringRuntime = coloringRuntime,
            ageBand = ageBand,
            startMode = startMode,
            startPace = startPace,
            startFreshRequested = startFreshRequested,
            onFreshSessionStarted = onFreshSessionStarted,
            onColoringReady = onColoringReady,
            onExitToHome = onExitToHome,
            // Covered by the product-owned completion overlay below whenever this callback could be
            // reached. Keeping it inert prevents a second semantic FinishForNow path.
            onFinishedForNow = {},
        )

        if (sessionState is LessonSessionState.DrawingComplete ||
            sessionState is LessonSessionState.AwaitingPostDrawingChoice
        ) {
            DrawingCompletionOverlay(
                coloringRuntime = coloringRuntime,
                galleryRuntime = galleryRuntime,
                artworkTitle = artworkTitle,
                onColoringReady = onColoringReady,
                onArtworkCompleted = onArtworkCompleted,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/** Product wrapper that replaces only the final coloring action while leaving canvas/tools intact. */
@Composable
fun GalleryAwareColoringWorkspace(
    coloringRuntime: ProductColoringRuntime,
    galleryRuntime: ProductGalleryRuntime,
    ageBand: AgeBand,
    artworkTitle: String,
    recoverRequested: Boolean,
    onArtworkCompleted: (String) -> Unit,
    onExitToHome: () -> Unit,
) {
    val semantic by coloringRuntime.sessionState.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        ColoringWorkspaceScreen(
            runtime = coloringRuntime,
            ageBand = ageBand,
            recoverRequested = recoverRequested,
            onExitToHome = onExitToHome,
        )
        if (semantic?.phase == ColoringSessionPhase.ACTIVE) {
            ColoringCompletionOverlay(
                galleryRuntime = galleryRuntime,
                artworkTitle = artworkTitle,
                onArtworkCompleted = onArtworkCompleted,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun DrawingCompletionOverlay(
    coloringRuntime: ProductColoringRuntime,
    galleryRuntime: ProductGalleryRuntime,
    artworkTitle: String,
    onColoringReady: () -> Unit,
    onArtworkCompleted: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .safeDrawingPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        color = StudioColors.Paper50,
        border = BorderStroke(1.dp, StudioColors.Line200),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Your drawing is ready",
                style = MaterialTheme.typography.titleMedium,
                color = StudioColors.Ink900,
            )
            Text(
                text = "Add color, or save this drawing to your Gallery.",
                style = MaterialTheme.typography.bodyMedium,
                color = StudioColors.Ink600,
            )
            message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = StudioColors.Coral500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        if (busy) return@OutlinedButton
                        busy = true
                        message = null
                        scope.launch {
                            when (val result = coloringRuntime.beginFromLesson(ColoringSessionMode.COLOR_WITH_ME)) {
                                is ProductColoringStartResult.Ready -> onColoringReady()
                                is ProductColoringStartResult.Failed -> {
                                    busy = false
                                    message = result.message
                                }
                            }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                ) { Text("Color with me") }
                OutlinedButton(
                    onClick = {
                        if (busy) return@OutlinedButton
                        busy = true
                        message = null
                        scope.launch {
                            when (val result = coloringRuntime.beginFromLesson(ColoringSessionMode.COLOR_MYSELF)) {
                                is ProductColoringStartResult.Ready -> onColoringReady()
                                is ProductColoringStartResult.Failed -> {
                                    busy = false
                                    message = result.message
                                }
                            }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                ) { Text("Color myself") }
            }
            Button(
                onClick = {
                    if (busy) return@Button
                    busy = true
                    message = null
                    scope.launch {
                        when (val result = galleryRuntime.finishDrawingForNow(artworkTitle)) {
                            is ArtworkCompletionResult.Saved -> onArtworkCompleted(result.record.entryId)
                            is ArtworkCompletionResult.Failed -> {
                                busy = false
                                message = result.message
                            }
                        }
                    }
                },
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) { Text("Finish for now") }
        }
    }
}

@Composable
private fun ColoringCompletionOverlay(
    galleryRuntime: ProductGalleryRuntime,
    artworkTitle: String,
    onArtworkCompleted: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .safeDrawingPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = StudioColors.Paper50,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = StudioColors.Coral500)
            }
            Button(
                onClick = {
                    if (busy) return@Button
                    busy = true
                    message = null
                    scope.launch {
                        when (val result = galleryRuntime.finishColoring(artworkTitle)) {
                            is ArtworkCompletionResult.Saved -> onArtworkCompleted(result.record.entryId)
                            is ArtworkCompletionResult.Failed -> {
                                busy = false
                                message = result.message
                            }
                        }
                    }
                },
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) { Text(if (busy) "Saving artwork…" else "Finish coloring") }
        }
    }
}
