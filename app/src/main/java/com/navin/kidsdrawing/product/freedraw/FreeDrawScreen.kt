package com.navin.kidsdrawing.product.freedraw

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.navin.kidsdrawing.drawing.domain.DrawingBrushPreset
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.profile.AgeBand
import kotlinx.coroutines.launch

@Composable
fun FreeDrawScreen(
    runtime: ProductFreeDrawRuntime,
    ageBand: AgeBand,
    onExitToHome: () -> Unit,
    onArtworkCompleted: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceController = remember { DrawingSurfaceController() }
    val toolTrayScrollState = rememberScrollState()
    val documentState by runtime.documentEngine.state.collectAsState()
    val toolSettings by runtime.toolEngine.state.collectAsState()
    val policy = remember(ageBand) { freeDrawPresentationPolicyFor(ageBand) }
    var recoveryReady by remember(runtime) { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var finishing by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(runtime) {
        val outcome = runtime.recover()
        message = when (outcome) {
            FreeDrawRecoveryOutcome.BlankCreated -> null
            is FreeDrawRecoveryOutcome.Restored -> null
            FreeDrawRecoveryOutcome.CorruptOrUnreadable ->
                "We couldn’t reopen the last Free Draw file. A safe blank canvas is open."
            FreeDrawRecoveryOutcome.IncompatibleDocument ->
                "That older Free Draw file needs a compatible version. A safe blank canvas is open."
        }
        surfaceController.reconcileDocument(runtime.documentEngine.state.value.document)
        recoveryReady = true
    }

    DisposableEffect(runtime, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                scope.launch { runCatching { runtime.onBackground() } }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(
        documentState.document.operations.size,
        documentState.document.modifiedAtEpochMillis,
        recoveryReady,
    ) {
        if (recoveryReady) surfaceController.reconcileDocument(documentState.document)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            runCatching { runtime.saveNow() }
                                .onFailure { message = "Your drawing could not save yet. Please try again." }
                                .onSuccess { onExitToHome() }
                        }
                    },
                    modifier = Modifier.heightIn(min = policy.minimumControlHeightDp.dp),
                ) {
                    Text("← Save & leave", color = StudioColors.Ink700)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Free Draw",
                        style = MaterialTheme.typography.titleLarge,
                        color = StudioColors.Ink900,
                    )
                    Text(
                        text = "Make anything you imagine",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioColors.Ink600,
                    )
                }
                Button(
                    enabled = runtime.hasVisibleArtwork() && !finishing,
                    onClick = {
                        finishing = true
                        message = null
                        scope.launch {
                            when (val result = runtime.finishToGallery("My Free Drawing")) {
                                is FreeDrawFinishResult.Failed -> {
                                    finishing = false
                                    message = result.message
                                }
                                is FreeDrawFinishResult.Saved -> {
                                    finishing = false
                                    if (!result.workingCanvasReset) {
                                        message = "Your Gallery copy is safe. The working canvas will reset next time."
                                    }
                                    onArtworkCompleted(result.completion.record.entryId)
                                }
                            }
                        }
                    },
                    modifier = Modifier.heightIn(min = policy.minimumControlHeightDp.dp),
                ) {
                    Text(if (finishing) "Saving…" else "Save to Gallery")
                }
            }

            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink700,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, StudioColors.Line200),
            ) {
                if (!recoveryReady) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StudioColors.Studio600)
                    }
                } else {
                    DrawingSurface(
                        modifier = Modifier.fillMaxSize(),
                        controller = surfaceController,
                        toolSettings = toolSettings,
                        onStrokeCommitted = { stroke ->
                            scope.launch {
                                runCatching { runtime.commitChildStroke(stroke) }
                                    .onFailure { message = "That stroke could not save yet. Your canvas is still open." }
                            }
                        },
                        onEraseMaskCommitted = { mask ->
                            scope.launch {
                                runCatching { runtime.commitEraseMask(mask) }
                                    .onFailure { message = "That erase could not save yet. Your canvas is still open." }
                            }
                        },
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = policy.maxControlTrayHeightDp.dp),
                shape = RoundedCornerShape(22.dp),
                color = StudioColors.Paper100,
                border = BorderStroke(1.dp, StudioColors.Line200),
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(toolTrayScrollState)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Tools", style = MaterialTheme.typography.titleSmall, color = StudioColors.Ink800)
                    ToolGrid(
                        columns = policy.toolColumns,
                        minimumHeightDp = policy.minimumControlHeightDp,
                        showDescriptions = policy.showToolDescriptions,
                        selectedTool = toolSettings.tool,
                        selectedPreset = toolSettings.brushPreset,
                        onPreset = { preset -> scope.launch { runtime.selectBrush(preset) } },
                        onEraser = { scope.launch { runtime.selectEraser() } },
                    )

                    Text("Colors", style = MaterialTheme.typography.titleSmall, color = StudioColors.Ink800)
                    policy.paletteArgb.chunked(policy.paletteColumns).forEach { colors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            colors.forEach { argb ->
                                val selected = toolSettings.tool == DrawingTool.PENCIL &&
                                    toolSettings.colorArgb == argb
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = (policy.minimumControlHeightDp - 10).dp)
                                        .semantics { contentDescription = "Drawing color" }
                                        .clickable {
                                            scope.launch {
                                                runtime.setColor(argb)
                                                if (toolSettings.tool == DrawingTool.ERASER) {
                                                    runtime.selectBrush(toolSettings.brushPreset)
                                                }
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(argb),
                                    border = BorderStroke(
                                        if (selected) 3.dp else 1.dp,
                                        if (selected) StudioColors.Studio600 else StudioColors.Line200,
                                    ),
                                ) {}
                            }
                            repeat(policy.paletteColumns - colors.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    Text("Size", style = MaterialTheme.typography.titleSmall, color = StudioColors.Ink800)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        freeDrawSizeChoices(toolSettings.tool, toolSettings.brushPreset).forEach { choice ->
                            val selected = kotlin.math.abs(toolSettings.width - choice.width) < 0.5f
                            if (selected) {
                                Button(
                                    onClick = { scope.launch { runtime.setWidth(choice.width) } },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = policy.minimumControlHeightDp.dp),
                                ) { Text(choice.label) }
                            } else {
                                OutlinedButton(
                                    onClick = { scope.launch { runtime.setWidth(choice.width) } },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = policy.minimumControlHeightDp.dp),
                                ) { Text(choice.label) }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            enabled = documentState.canUndo,
                            onClick = { scope.launch { runtime.undo() } },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = policy.minimumControlHeightDp.dp),
                        ) { Text("Undo") }
                        OutlinedButton(
                            enabled = documentState.canRedo,
                            onClick = { scope.launch { runtime.redo() } },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = policy.minimumControlHeightDp.dp),
                        ) { Text("Redo") }
                        OutlinedButton(
                            enabled = documentState.document.operations.isNotEmpty(),
                            onClick = { confirmClear = true },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = policy.minimumControlHeightDp.dp),
                        ) { Text("Clear") }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear this canvas?") },
            text = { Text("Your current Free Draw canvas will become blank. You can still Undo right after clearing.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        scope.launch { runtime.clear(confirmed = true) }
                    },
                ) { Text("Clear canvas") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        scope.launch { runtime.clear(confirmed = false) }
                    },
                ) { Text("Keep drawing") }
            },
        )
    }
}

@Composable
private fun ToolGrid(
    columns: Int,
    minimumHeightDp: Int,
    showDescriptions: Boolean,
    selectedTool: DrawingTool,
    selectedPreset: DrawingBrushPreset,
    onPreset: (DrawingBrushPreset) -> Unit,
    onEraser: () -> Unit,
) {
    val choices = listOf(
        ToolChoice("Pencil", "Fine lines", DrawingBrushPreset.PENCIL),
        ToolChoice("Crayon", "Soft color", DrawingBrushPreset.CRAYON),
        ToolChoice("Marker", "Bold lines", DrawingBrushPreset.MARKER),
        ToolChoice("Eraser", "Erase gently", null),
    )
    choices.chunked(columns).forEach { rowChoices ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rowChoices.forEach { choice ->
                val selected = if (choice.preset == null) {
                    selectedTool == DrawingTool.ERASER
                } else {
                    selectedTool == DrawingTool.PENCIL && selectedPreset == choice.preset
                }
                val action = { if (choice.preset == null) onEraser() else onPreset(choice.preset) }
                if (selected) {
                    Button(
                        onClick = action,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = minimumHeightDp.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(choice.label)
                            if (showDescriptions) {
                                Text(choice.description, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = action,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = minimumHeightDp.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(choice.label)
                            if (showDescriptions) {
                                Text(choice.description, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            repeat(columns - rowChoices.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

private data class ToolChoice(
    val label: String,
    val description: String,
    val preset: DrawingBrushPreset?,
)
