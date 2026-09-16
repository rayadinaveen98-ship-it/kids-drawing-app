package com.navin.kidsdrawing.product.coloring

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.coloring.session.ColoringSessionTool
import com.navin.kidsdrawing.drawing.domain.DocumentViewportMapper
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceContentRole
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import com.navin.kidsdrawing.product.accessibility.AccessibilityPolicy
import com.navin.kidsdrawing.product.accessibility.accessibleColorName
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.design.densityPolicyFor
import com.navin.kidsdrawing.product.profile.AgeBand
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ColoringWorkspaceScreen(
    runtime: ProductColoringRuntime,
    ageBand: AgeBand,
    recoverRequested: Boolean,
    onExitToHome: () -> Unit,
    onFinishColoring: suspend () -> Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = densityPolicyFor(ageBand)
    val workspaceTarget = density.minimumTouchTarget.coerceAtMost(58.dp)
    val accessibilityLayout = AccessibilityPolicy.layout(LocalDensity.current.fontScale)
    val surfaceController = remember { DrawingSurfaceController() }
    val documentState by runtime.documentEngine.state.collectAsState()
    val toolSettings by runtime.toolEngine.state.collectAsState()
    val sessionState by runtime.sessionState.collectAsState()
    var ready by remember { mutableStateOf(!recoverRequested && sessionState != null) }
    var startupMessage by remember { mutableStateOf<String?>(null) }
    var finishing by remember { mutableStateOf(false) }
    var finishMessage by remember { mutableStateOf<String?>(null) }

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
            val guidedProgress = runtime.guidedProgress()
            val fillAvailableNow = runtime.fillAvailableNow()
            val companion = ProductColoringPresentationPolicy.from(
                mode = semantic?.mode ?: ColoringSessionMode.COLOR_MYSELF,
                progress = guidedProgress,
                preparedFillAvailable = runtime.preparedFillAvailable,
            )
            val title = semantic?.lessonId
                ?.toChildLabel()
                ?.takeIf(String::isNotBlank)
                ?.let { "Color $it" }
                ?: "Color your drawing"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                ColoringTopBar(
                    title = title,
                    minimumControlHeight = workspaceTarget,
                    stackActions = accessibilityLayout.preferSingleColumnActions,
                    onSaveAndLeave = {
                        scope.launch {
                            runtime.saveNow()
                            onExitToHome()
                        }
                    },
                )

                ColoringCompanionCard(companion)

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
                        Box(modifier = Modifier.fillMaxSize()) {
                            DrawingSurface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .semantics {
                                        contentDescription = "Coloring canvas. Brush and eraser use touch or stylus."
                                    },
                                controller = surfaceController,
                                toolSettings = toolSettings,
                                contentRole = DrawingSurfaceContentRole.COLORING,
                                onStrokeCommitted = { stroke ->
                                    scope.launch {
                                        runCatching { runtime.commitColorStroke(stroke) }
                                    }
                                },
                                onEraseMaskCommitted = { mask ->
                                    scope.launch {
                                        runCatching { runtime.commitColorEraseMask(mask) }
                                    }
                                },
                            )

                            // Fill deliberately intercepts the gesture above AndroidX Ink. The
                            // shared domain viewport mapper converts the tap into stable document
                            // coordinates, so no accidental brush stroke can be created.
                            if (semantic?.selectedTool == ColoringSessionTool.FILL) {
                                val documentSize = documentState.document.logicalSize
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .semantics {
                                            contentDescription = "Fill mode. Tap a prepared coloring area on the canvas to fill it."
                                        }
                                        .pointerInput(
                                            documentSize,
                                            semantic.selectedColorArgb,
                                            guidedProgress?.currentStepIndex,
                                            documentState.document.operations.size,
                                        ) {
                                            val mapper = DocumentViewportMapper(documentSize)
                                            detectTapGestures { offset ->
                                                val transform = mapper.transformFor(
                                                    size.width.toFloat(),
                                                    size.height.toFloat(),
                                                )
                                                val point = transform?.let {
                                                    mapper.viewportToDocumentOrNull(offset.x, offset.y, it)
                                                }
                                                if (point != null) {
                                                    scope.launch {
                                                        finishMessage = when (
                                                            runtime.fillAtDocumentPoint(point.x, point.y)
                                                        ) {
                                                            is ProductColorFillResult.Filled -> null
                                                            ProductColorFillResult.Miss ->
                                                                "Try tapping inside the coloring area for this step."
                                                            ProductColorFillResult.GuidanceComplete ->
                                                                "The guided coloring steps are complete."
                                                            ProductColorFillResult.Unavailable ->
                                                                if (fillAvailableNow) {
                                                                    "That area isn’t available for this step yet."
                                                                } else {
                                                                    "Fill isn’t available for this part. Choose Brush to continue."
                                                                }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                )
                            }
                        }
                    }
                }

                if (semantic?.phase == ColoringSessionPhase.ACTIVE) {
                    ColorPalette(
                        selectedColorArgb = semantic.selectedColorArgb,
                        minimumTarget = workspaceTarget,
                        onSelect = { color -> scope.launch { runtime.selectColor(color) } },
                    )
                    ColoringTools(
                        selectedTool = semantic.selectedTool,
                        brushWidth = semantic.brushWidth,
                        ageBand = ageBand,
                        fillAvailable = fillAvailableNow,
                        canUndo = documentState.canUndoColoring,
                        canRedo = documentState.canRedoColoring,
                        minimumTarget = workspaceTarget,
                        stackActions = accessibilityLayout.preferSingleColumnActions,
                        onBrush = { scope.launch { runtime.selectTool(ColoringSessionTool.BRUSH) } },
                        onFill = { scope.launch { runtime.selectTool(ColoringSessionTool.FILL) } },
                        onEraser = { scope.launch { runtime.selectTool(ColoringSessionTool.ERASER) } },
                        onSize = { width -> scope.launch { runtime.setBrushWidth(width) } },
                        onUndo = { scope.launch { runtime.undo() } },
                        onRedo = { scope.launch { runtime.redo() } },
                    )
                    finishMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioColors.Ink700,
                        )
                    }
                    ColoringActionButton(
                        label = if (finishing) "Saving artwork…" else "Finish coloring",
                        primary = true,
                        enabled = !finishing,
                        minimumHeight = workspaceTarget,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (finishing) return@ColoringActionButton
                        finishing = true
                        finishMessage = null
                        scope.launch {
                            val saved = runCatching { onFinishColoring() }.getOrDefault(false)
                            if (!saved) {
                                finishing = false
                                finishMessage = "We couldn’t save yet. Your coloring is still safe."
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColoringTopBar(
    title: String,
    minimumControlHeight: Dp,
    stackActions: Boolean,
    onSaveAndLeave: () -> Unit,
) {
    if (stackActions) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = StudioColors.Ink900,
            )
            TextButton(
                onClick = onSaveAndLeave,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minimumControlHeight)
                    .semantics { contentDescription = "Save coloring and return to studio" },
            ) {
                Text("← Save & leave", color = StudioColors.Ink700)
            }
        }
        return
    }

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
            text = title,
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
private fun ColoringCompanionCard(presentation: ColoringCompanionPresentation) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(
                text = presentation.eyebrow,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = StudioColors.Studio600,
            )
            presentation.stepLabel?.let { stepLabel ->
                Text(
                    text = stepLabel,
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudioColors.Ink900,
                )
            }
            Text(
                text = presentation.instruction,
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
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = "Colors",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = StudioColors.Ink700,
        )
        palette.chunked(3).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                rowColors.forEach { argb ->
                    val selected = argb == selectedColorArgb
                    val name = accessibleColorName(argb)
                    Surface(
                        modifier = Modifier
                            .size(minimumTarget)
                            .clip(CircleShape)
                            .semantics {
                                contentDescription = "$name coloring color"
                                if (selected) {
                                    this.selected = true
                                    stateDescription = "Selected"
                                }
                            }
                            .clickable { onSelect(argb) },
                        shape = CircleShape,
                        color = Color(argb),
                        border = BorderStroke(
                            if (selected) 4.dp else 1.dp,
                            if (selected) StudioColors.Ink900 else StudioColors.Line200,
                        ),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (selected) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                ) {
                                    Text(
                                        text = "✓",
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        color = StudioColors.Ink900,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColoringTools(
    selectedTool: ColoringSessionTool,
    brushWidth: Float,
    ageBand: AgeBand,
    fillAvailable: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    minimumTarget: Dp,
    stackActions: Boolean,
    onBrush: () -> Unit,
    onFill: () -> Unit,
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
    val sizeLabels = listOf("Small", "Medium", "Large")
    val currentSizeIndex = sizes.indices.minByOrNull { index -> abs(brushWidth - sizes[index]) } ?: 1
    val nextSizeIndex = (currentSizeIndex + 1) % sizes.size

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (stackActions) {
            ColoringActionButton(
                label = if (selectedTool == ColoringSessionTool.BRUSH) "✓ Brush" else "Brush",
                selected = selectedTool == ColoringSessionTool.BRUSH,
                minimumHeight = minimumTarget,
                modifier = Modifier.fillMaxWidth(),
                onClick = onBrush,
            )
            if (fillAvailable || selectedTool == ColoringSessionTool.FILL) {
                ColoringActionButton(
                    label = if (selectedTool == ColoringSessionTool.FILL) "✓ Fill" else "Fill",
                    selected = selectedTool == ColoringSessionTool.FILL,
                    enabled = fillAvailable,
                    minimumHeight = minimumTarget,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onFill,
                )
            }
            ColoringActionButton(
                label = if (selectedTool == ColoringSessionTool.ERASER) "✓ Eraser" else "Eraser",
                selected = selectedTool == ColoringSessionTool.ERASER,
                minimumHeight = minimumTarget,
                modifier = Modifier.fillMaxWidth(),
                onClick = onEraser,
            )
            ColoringActionButton(
                label = "Size: ${sizeLabels[currentSizeIndex]}",
                enabled = selectedTool != ColoringSessionTool.FILL,
                minimumHeight = minimumTarget,
                modifier = Modifier.fillMaxWidth(),
            ) { onSize(sizes[nextSizeIndex]) }
            ColoringActionButton(
                label = "Undo",
                enabled = canUndo,
                minimumHeight = minimumTarget,
                modifier = Modifier.fillMaxWidth(),
                onClick = onUndo,
            )
            ColoringActionButton(
                label = "Redo",
                enabled = canRedo,
                minimumHeight = minimumTarget,
                modifier = Modifier.fillMaxWidth(),
                onClick = onRedo,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ColoringActionButton(
                    label = if (selectedTool == ColoringSessionTool.BRUSH) "✓ Brush" else "Brush",
                    selected = selectedTool == ColoringSessionTool.BRUSH,
                    minimumHeight = minimumTarget,
                    modifier = Modifier.weight(1f),
                    onClick = onBrush,
                )
                if (fillAvailable || selectedTool == ColoringSessionTool.FILL) {
                    ColoringActionButton(
                        label = if (selectedTool == ColoringSessionTool.FILL) "✓ Fill" else "Fill",
                        selected = selectedTool == ColoringSessionTool.FILL,
                        enabled = fillAvailable,
                        minimumHeight = minimumTarget,
                        modifier = Modifier.weight(1f),
                        onClick = onFill,
                    )
                }
                ColoringActionButton(
                    label = if (selectedTool == ColoringSessionTool.ERASER) "✓ Eraser" else "Eraser",
                    selected = selectedTool == ColoringSessionTool.ERASER,
                    minimumHeight = minimumTarget,
                    modifier = Modifier.weight(1f),
                    onClick = onEraser,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ColoringActionButton(
                    label = "Size: ${sizeLabels[currentSizeIndex]}",
                    enabled = selectedTool != ColoringSessionTool.FILL,
                    minimumHeight = minimumTarget,
                    modifier = Modifier.weight(1f),
                ) { onSize(sizes[nextSizeIndex]) }
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
        }
    }
}

@Composable
private fun ColoringActionButton(
    label: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    selected: Boolean = false,
    minimumHeight: Dp,
    onClick: () -> Unit,
) {
    val selectionModifier = if (selected) {
        Modifier.semantics {
            this.selected = true
            stateDescription = "Selected"
        }
    } else {
        Modifier
    }
    if (primary) {
        Surface(
            modifier = modifier
                .heightIn(min = minimumHeight)
                .then(selectionModifier)
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = if (enabled) StudioColors.Studio600 else StudioColors.Line200,
        ) {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minimumHeight),
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
            modifier = modifier
                .heightIn(min = minimumHeight)
                .then(selectionModifier),
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
