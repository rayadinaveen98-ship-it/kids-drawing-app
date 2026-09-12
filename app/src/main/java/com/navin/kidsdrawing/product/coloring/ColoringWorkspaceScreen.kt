package com.navin.kidsdrawing.product.coloring

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.coloring.session.ColoringSessionTool
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceContentRole
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.densityPolicyFor
import com.navin.kidsdrawing.product.profile.AgeBand
import kotlinx.coroutines.launch

@Composable
fun ColoringWorkspaceScreen(
    runtime: ProductColoringRuntime,
    ageBand: AgeBand,
    recoverRequested: Boolean,
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = densityPolicyFor(ageBand)
    val surfaceController = remember { DrawingSurfaceController() }
    val documentState by runtime.documentEngine.state.collectAsState()
    val toolSettings by runtime.toolEngine.state.collectAsState()
    val sessionState by runtime.sessionState.collectAsState()
    var ready by remember { mutableStateOf(!recoverRequested && sessionState != null) }
    var startupMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(runtime, recoverRequested) {
        if (recoverRequested) {
            when (runtime.recoverActive()) {
                ProductColoringRecoveryResult.RESTORED -> ready = true
                ProductColoringRecoveryResult.FINISHED ->
                    startupMessage = "This coloring session is already finished and saved."
                ProductColoringRecoveryResult.MISSING ->
                    startupMessage = "We couldn’t find coloring work to continue."
                ProductColoringRecoveryResult.CORRUPT ->
                    startupMessage = "The coloring session needs a refresh, but the drawing is still protected."
                ProductColoringRecoveryResult.ARTWORK_MISSING ->
                    startupMessage = "The coloring session is here, but its drawing could not be found."
                ProductColoringRecoveryResult.ARTWORK_INCOMPATIBLE ->
                    startupMessage = "This saved coloring belongs to a different lesson version."
            }
        } else if (sessionState != null) {
            ready = true
        } else {
            startupMessage = "Coloring could not open, but your drawing is safe."
        }
        if (ready) {
            surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
        }
    }

    DisposableEffect(runtime, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                scope.launch { runtime.onBackground() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(documentState.document.operations.size, documentState.document.modifiedAtEpochMillis) {
        if (ready) surfaceController.reconcileDocument(documentState.document)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        if (startupMessage != null) {
            ColoringStartupMessage(
                message = startupMessage.orEmpty(),
                onBack = onExitToHome,
            )
        } else {
            val semantic = sessionState
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ColoringTopBar(
                    minimumControlHeight = density.minimumTouchTarget,
                    onSaveAndLeave = {
                        scope.launch {
                            runtime.saveNow()
                            onExitToHome()
                        }
                    },
                )

                ColoringCompanionCard(
                    mode = semantic?.mode ?: ColoringSessionMode.COLOR_MYSELF,
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, StudioColors.Line200),
                ) {
                    if (!ready) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Opening your colors…", color = StudioColors.Ink500)
                        }
                    } else {
                        DrawingSurface(
                            modifier = Modifier.fillMaxSize(),
                            controller = surfaceController,
                            toolSettings = toolSettings,
                            contentRole = DrawingSurfaceContentRole.COLORING,
                            onStrokeCommitted = { stroke ->
                                scope.launch { runtime.commitColorStroke(stroke) }
                            },
                            onEraseMaskCommitted = { mask ->
                                scope.launch { runtime.commitColorEraseMask(mask) }
                            },
                        )
                    }
                }

                if (semantic?.phase == ColoringSessionPhase.ACTIVE) {
                    ColorPalette(
                        selectedColorArgb = semantic.selectedColorArgb,
                        minimumTarget = density.minimumTouchTarget,
                        onSelect = { color -> scope.launch { runtime.selectColor(color) } },
                    )
                    ColoringTools(
                        selectedTool = semantic.selectedTool,
                        brushWidth = semantic.brushWidth,
                        ageBand = ageBand,
                        canUndo = documentState.canUndoColoring,
                        canRedo = documentState.canRedoColoring,
                        minimumTarget = density.minimumTouchTarget,
                        onBrush = { scope.launch { runtime.selectTool(ColoringSessionTool.BRUSH) } },
                        onEraser = { scope.launch { runtime.selectTool(ColoringSessionTool.ERASER) } },
                        onSize = { width -> scope.launch { runtime.setBrushWidth(width) } },
                        onUndo = { scope.launch { runtime.undo() } },
                        onRedo = { scope.launch { runtime.redo() } },
                    )
                    ColoringActionButton(
                        label = "Finish coloring",
                        primary = true,
                        minimumHeight = density.minimumTouchTarget,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        scope.launch {
                            if (runtime.finish()) onExitToHome()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColoringTopBar(
    minimumControlHeight: Dp,
    onSaveAndLeave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onSaveAndLeave,
            modifier = Modifier
                .heightIn(min = minimumControlHeight)
                .semantics { contentDescription = "Save coloring and return to studio" },
        ) {
            Text("← Save & leave", color = StudioColors.Ink700)
        }
        Text(
            text = "Color your cat",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = StudioColors.Ink900,
        )
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = StudioColors.Sun500.copy(alpha = 0.18f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("✦", color = StudioColors.Ink900)
            }
        }
    }
}

@Composable
private fun ColoringCompanionCard(mode: ColoringSessionMode) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = if (mode == ColoringSessionMode.COLOR_WITH_ME) "COLOR WITH ME" else "YOUR COLORS",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = StudioColors.Studio600,
            )
            Text(
                text = if (mode == ColoringSessionMode.COLOR_WITH_ME) {
                    "Try one area at a time. Pick any color you like — I’ll stay with you."
                } else {
                    "Your colors, your way. The drawing lines stay safe while you experiment."
                },
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = StudioColors.Ink700,
            )
        }
    }
}

@Composable
private fun ColorPalette(
    selectedColorArgb: Int,
    minimumTarget: Dp,
    onSelect: (Int) -> Unit,
) {
    val palette = listOf(
        0xFFE47C68.toInt(),
        0xFFE9A94A.toInt(),
        0xFFF4D35E.toInt(),
        0xFF78A86B.toInt(),
        0xFF6C9CB8.toInt(),
        0xFF9A83B8.toInt(),
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Colors",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = StudioColors.Ink700,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            palette.forEachIndexed { index, argb ->
                Surface(
                    modifier = Modifier
                        .size(minimumTarget.coerceAtMost(58.dp))
                        .clip(CircleShape)
                        .semantics {
                            contentDescription = "Color ${index + 1}${if (argb == selectedColorArgb) ", selected" else ""}"
                        }
                        .clickable { onSelect(argb) },
                    shape = CircleShape,
                    color = Color(argb),
                    border = BorderStroke(
                        if (argb == selectedColorArgb) 4.dp else 1.dp,
                        if (argb == selectedColorArgb) StudioColors.Ink900 else StudioColors.Line200,
                    ),
                ) {}
            }
        }
    }
}

@Composable
private fun ColoringTools(
    selectedTool: ColoringSessionTool,
    brushWidth: Float,
    ageBand: AgeBand,
    canUndo: Boolean,
    canRedo: Boolean,
    minimumTarget: Dp,
    onBrush: () -> Unit,
    onEraser: () -> Unit,
    onSize: (Float) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    val sizes = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> listOf(36f, 54f, 72f)
        AgeBand.CREATIVE_EXPLORER -> listOf(30f, 46f, 64f)
        AgeBand.GROWING_ARTIST -> listOf(24f, 38f, 56f)
        AgeBand.YOUNG_ARTIST -> listOf(18f, 32f, 48f)
    }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            ColoringActionButton(
                label = if (selectedTool == ColoringSessionTool.BRUSH) "✓ Brush" else "Brush",
                minimumHeight = minimumTarget,
                modifier = Modifier.weight(1f),
                onClick = onBrush,
            )
            ColoringActionButton(
                label = if (selectedTool == ColoringSessionTool.ERASER) "✓ Eraser" else "Eraser",
                minimumHeight = minimumTarget,
                modifier = Modifier.weight(1f),
                onClick = onEraser,
            )
            ColoringActionButton(
                label = "Undo",
                enabled = canUndo,
                minimumHeight = minimumTarget,
                modifier = Modifier.weight(1f),
                onClick = onUndo,
            )
            ColoringActionButton(
                label = "Redo",
                enabled = canRedo,
                minimumHeight = minimumTarget,
                modifier = Modifier.weight(1f),
                onClick = onRedo,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            sizes.forEachIndexed { index, width ->
                val selected = kotlin.math.abs(brushWidth - width) < 1f
                ColoringActionButton(
                    label = "${if (selected) "✓ " else ""}${listOf("Small", "Medium", "Large")[index]}",
                    minimumHeight = minimumTarget,
                    modifier = Modifier.weight(1f),
                ) { onSize(width) }
            }
        }
    }
}

@Composable
private fun ColoringActionButton(
    label: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    minimumHeight: Dp,
    onClick: () -> Unit,
) {
    if (primary) {
        Surface(
            modifier = modifier
                .heightIn(min = minimumHeight)
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = if (enabled) StudioColors.Studio600 else StudioColors.Line200,
        ) {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    label,
                    color = if (enabled) Color.White else StudioColors.Ink500,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.heightIn(min = minimumHeight),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StudioColors.Line200),
        ) {
            Text(label, color = StudioColors.Ink700)
        }
    }
}

@Composable
private fun ColoringStartupMessage(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = StudioColors.Sun500.copy(alpha = 0.18f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("✦", color = StudioColors.Ink900)
            }
        }
        Text(
            text = "Your drawing is safe",
            modifier = Modifier.padding(top = 18.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = StudioColors.Ink900,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = StudioColors.Ink700,
        )
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text("Back to studio", color = StudioColors.Studio600)
        }
    }
}
