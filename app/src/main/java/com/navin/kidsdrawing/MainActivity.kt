package com.navin.kidsdrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var documentEngine: DrawingDocumentEngine
    private lateinit var documentStore: AtomicDrawingDocumentStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentEngine = DrawingDocumentEngine(
            initialDocument = DrawingDocumentEngine.newDocument(
                documentId = ART_LAB_DOCUMENT_ID,
            ),
        )
        documentStore = AtomicDrawingDocumentStore(
            rootDirectory = File(filesDir, "art-lab-documents"),
        )

        setContent {
            ArtLabTheme {
                ArtLabLauncher(
                    documentEngine = documentEngine,
                    documentStore = documentStore,
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!::documentEngine.isInitialized || !::documentStore.isInitialized) return

        // Autosave handles normal stable boundaries. This is an additional lifecycle flush for
        // configuration/background transitions; store IO is serialized and runs off the UI thread.
        lifecycleScope.launch {
            runCatching {
                documentStore.save(documentEngine.state.value.document)
            }
        }
    }
}

@Composable
private fun ArtLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

@Composable
private fun ArtLabLauncher(
    documentEngine: DrawingDocumentEngine,
    documentStore: AtomicDrawingDocumentStore,
) {
    var metrics by remember { mutableStateOf(DrawingSurfaceMetrics()) }
    var recoveryComplete by remember { mutableStateOf(false) }
    var persistenceStatus by remember { mutableStateOf("Checking saved artwork…") }
    var autosaveJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val surfaceController = remember { DrawingSurfaceController() }
    val documentState by documentEngine.state.collectAsState()

    fun queueAutosave(document: DrawingDocument) {
        autosaveJob?.cancel()
        autosaveJob = scope.launch {
            delay(AUTOSAVE_DEBOUNCE_MILLIS)
            runCatching { documentStore.save(document) }
                .onSuccess { persistenceStatus = "Autosaved" }
                .onFailure { persistenceStatus = "Autosave failed · use Save" }
        }
    }

    LaunchedEffect(Unit) {
        val restored = runCatching { documentStore.load(ART_LAB_DOCUMENT_ID) }.getOrNull()
        if (restored != null) {
            documentEngine.replaceDocument(restored.document)
            surfaceController.reconcileDocument(restored.document)
            persistenceStatus = when (restored.source) {
                AtomicDrawingDocumentStore.LoadSource.PRIMARY -> "Restored saved artwork"
                AtomicDrawingDocumentStore.LoadSource.BACKUP -> "Recovered last good backup"
            }
        } else {
            surfaceController.reconcileDocument(documentEngine.state.value.document)
            persistenceStatus = "New Art Lab document"
        }
        recoveryComplete = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Kids Drawing · Art Lab",
                color = Ink900,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "P1.4 atomic persistence · editable document recovery",
                color = Ink700,
                fontSize = 15.sp,
            )

            ResponsiveStatusSection(persistenceStatus)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("surface", metrics.committedStrokeCount.toString())
                MetricChip("doc ops", documentState.document.operations.size.toString())
                MetricChip("samples", metrics.lastSampleCount.toString())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("pressure", metrics.lastPressure?.let { "%.2f".format(it) } ?: "—")
                MetricChip(
                    "handoff",
                    metrics.lastCommitLatencyMillis?.let { "${it}ms" } ?: "—",
                )
                MetricChip("redo", if (documentState.canRedo) "yes" else "no")
            }

            ResponsiveActionSection(
                recoveryComplete = recoveryComplete,
                canUndo = documentState.canUndo,
                canRedo = documentState.canRedo,
                onUndo = {
                    scope.launch {
                        if (documentEngine.undo()) {
                            val document = documentEngine.state.value.document
                            surfaceController.reconcileDocument(document)
                            queueAutosave(document)
                            persistenceStatus = "Undo · autosave queued"
                        }
                    }
                },
                onRedo = {
                    scope.launch {
                        if (documentEngine.redo()) {
                            val document = documentEngine.state.value.document
                            surfaceController.reconcileDocument(document)
                            queueAutosave(document)
                            persistenceStatus = "Redo · autosave queued"
                        }
                    }
                },
                onSave = {
                    scope.launch {
                        autosaveJob?.cancelAndJoin()
                        val result = runCatching {
                            documentStore.save(documentEngine.state.value.document)
                        }
                        persistenceStatus = if (result.isSuccess) "Saved now" else "Save failed"
                    }
                },
                onReload = {
                    scope.launch {
                        autosaveJob?.cancelAndJoin()
                        val restored = runCatching {
                            documentStore.load(ART_LAB_DOCUMENT_ID)
                        }.getOrNull()
                        if (restored == null) {
                            persistenceStatus = "No valid saved artwork found"
                        } else {
                            documentEngine.replaceDocument(restored.document)
                            surfaceController.reconcileDocument(restored.document)
                            persistenceStatus = when (restored.source) {
                                AtomicDrawingDocumentStore.LoadSource.PRIMARY -> "Reloaded saved artwork"
                                AtomicDrawingDocumentStore.LoadSource.BACKUP -> "Reloaded backup artwork"
                            }
                        }
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (recoveryComplete) {
                    DrawingSurface(
                        modifier = Modifier.fillMaxSize(),
                        controller = surfaceController,
                        onStrokeCommitted = { stroke ->
                            scope.launch {
                                documentEngine.commitChildStroke(stroke)
                                queueAutosave(documentEngine.state.value.document)
                            }
                        },
                        onMetricsChanged = { metrics = it },
                    )
                } else {
                    Text(
                        text = "Restoring Art Lab…",
                        color = Ink700,
                        fontSize = 14.sp,
                    )
                }
            }

            Text(
                text = "Test path: draw a few strokes → Save → Reload. Undo/Redo now reconcile the dry Ink renderer from the authoritative document. Autosave waits briefly after stable edits, and backgrounding the app triggers an additional safety save.",
                color = Ink700,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun ResponsiveStatusSection(persistenceStatus: String) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 520.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusChip("Ink 1.0", Modifier.weight(1f))
                    StatusChip("1000 × 1000 doc", Modifier.weight(1.5f))
                    StatusChip("Offline", Modifier.weight(1f))
                }
                StatusChip(
                    text = persistenceStatus,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip("Ink 1.0")
                StatusChip("1000 × 1000 doc")
                StatusChip("Offline")
                StatusChip(persistenceStatus)
            }
        }
    }
}

@Composable
private fun ResponsiveActionSection(
    recoveryComplete: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onReload: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 520.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = recoveryComplete && canUndo,
                        onClick = onUndo,
                    ) { Text("Undo", maxLines = 1) }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = recoveryComplete && canRedo,
                        onClick = onRedo,
                    ) { Text("Redo", maxLines = 1) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = recoveryComplete,
                        onClick = onSave,
                    ) { Text("Save", maxLines = 1) }
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = recoveryComplete,
                        onClick = onReload,
                    ) { Text("Reload", maxLines = 1) }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = recoveryComplete && canUndo,
                    onClick = onUndo,
                ) { Text("Undo", maxLines = 1) }
                OutlinedButton(
                    enabled = recoveryComplete && canRedo,
                    onClick = onRedo,
                ) { Text("Redo", maxLines = 1) }
                Button(enabled = recoveryComplete, onClick = onSave) {
                    Text("Save", maxLines = 1)
                }
                Button(enabled = recoveryComplete, onClick = onReload) {
                    Text("Reload", maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Studio100,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            color = Studio600,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
    ) {
        Text(
            text = "$label: $value",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = Ink700,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private const val ART_LAB_DOCUMENT_ID = "art-lab-session"
private const val AUTOSAVE_DEBOUNCE_MILLIS = 650L

private val Paper50 = Color(0xFFFFFDF8)
private val Ink900 = Color(0xFF242321)
private val Ink700 = Color(0xFF4E4A45)
private val Studio600 = Color(0xFF5C6F52)
private val Studio100 = Color(0xFFEAF0E5)
