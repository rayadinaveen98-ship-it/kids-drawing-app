package com.navin.kidsdrawing.product.gallery

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceContentRole
import com.navin.kidsdrawing.drawing.ui.DrawingSurface
import com.navin.kidsdrawing.drawing.ui.DrawingSurfaceController
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkCardModel
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.gallery.domain.GalleryDeleteResult
import com.navin.kidsdrawing.gallery.domain.GalleryListResult
import com.navin.kidsdrawing.gallery.domain.GalleryReopenResult
import com.navin.kidsdrawing.product.accessibility.AccessibilityPolicy
import com.navin.kidsdrawing.product.design.StudioColors
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GalleryScreen(
    runtime: ProductGalleryRuntime,
    onOpenArtwork: (String) -> Unit,
    onBack: () -> Unit,
) {
    var result by remember(runtime) { mutableStateOf<GalleryListResult?>(null) }
    val accessibilityLayout = AccessibilityPolicy.layout(LocalDensity.current.fontScale)
    val minimumCardWidth = if (accessibilityLayout.avoidFixedTwoColumnCards) 220.dp else 150.dp
    LaunchedEffect(runtime) { result = runtime.listArtwork() }

    Surface(modifier = Modifier.fillMaxSize(), color = StudioColors.Paper50) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GalleryHeader(onBack = onBack)
            Text(
                text = "My Gallery",
                style = MaterialTheme.typography.headlineMedium,
                color = StudioColors.Ink900,
            )
            Text(
                text = "Your finished drawings live here.",
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink600,
            )

            when (val loaded = result) {
                null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudioColors.Studio600)
                }
                GalleryListResult.Empty -> GalleryEmptyState()
                is GalleryListResult.Unavailable -> GalleryMessage(loaded.message)
                is GalleryListResult.Ready -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = minimumCardWidth),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(loaded.cards, key = { it.record.entryId }) { card ->
                        GalleryArtworkCard(
                            runtime = runtime,
                            card = card,
                            onClick = { onOpenArtwork(card.record.entryId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryArtworkDetailScreen(
    runtime: ProductGalleryRuntime,
    entryId: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var result by remember(runtime, entryId) { mutableStateOf<GalleryReopenResult?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(runtime, entryId) { result = runtime.reopen(entryId) }

    Surface(modifier = Modifier.fillMaxSize(), color = StudioColors.Paper50) {
        when (val loaded = result) {
            null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StudioColors.Studio600)
            }
            is GalleryReopenResult.Ready -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GalleryHeader(onBack = onBack)
                Text(
                    text = loaded.record.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = StudioColors.Ink900,
                )
                Text(
                    text = detailLabel(loaded.record.completionKind, loaded.record.completedAtEpochMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink600,
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                ) {
                    ReadOnlyArtworkCanvas(document = loaded.document)
                }
                message?.let { GalleryMessage(it) }
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Delete artwork")
                }
            }
            GalleryReopenResult.EntryMissing -> GallerySafeError(
                "This Gallery entry is no longer available.",
                onBack,
            )
            GalleryReopenResult.ArtworkMissing -> GallerySafeError(
                "The artwork file could not be opened. The app did not rebuild it from a preview.",
                onBack,
            )
            GalleryReopenResult.ArtworkIncompatible -> GallerySafeError(
                "This artwork needs a compatible app version before it can open.",
                onBack,
            )
            is GalleryReopenResult.CatalogUnavailable -> GallerySafeError(loaded.message, onBack)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this artwork?") },
            text = { Text("This removes this saved Gallery artwork from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        scope.launch {
                            when (val deletion = runtime.delete(entryId, confirmed = true)) {
                                GalleryDeleteResult.Deleted -> onDeleted()
                                is GalleryDeleteResult.Failed -> message = deletion.message
                                GalleryDeleteResult.ProtectedWorkingDocument ->
                                    message = "This working drawing is protected and was not deleted."
                                GalleryDeleteResult.EntryMissing -> onDeleted()
                                GalleryDeleteResult.ConfirmationRequired -> Unit
                            }
                        }
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Keep artwork") }
            },
        )
    }
}

@Composable
fun ArtworkCompletionScreen(
    runtime: ProductGalleryRuntime,
    entryId: String,
    onSeeGallery: () -> Unit,
    onBackToStudio: () -> Unit,
) {
    var result by remember(runtime, entryId) { mutableStateOf<GalleryReopenResult?>(null) }
    LaunchedEffect(runtime, entryId) { result = runtime.reopen(entryId) }

    Surface(modifier = Modifier.fillMaxSize(), color = StudioColors.Paper50) {
        when (val loaded = result) {
            null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StudioColors.Studio600)
            }
            is GalleryReopenResult.Ready -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "You finished your artwork",
                    style = MaterialTheme.typography.headlineMedium,
                    color = StudioColors.Ink900,
                )
                Text(
                    text = "${loaded.record.title} is saved in your Gallery.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StudioColors.Ink600,
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                ) {
                    ReadOnlyArtworkCanvas(loaded.document)
                }
                Button(
                    onClick = onSeeGallery,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("See in My Gallery") }
                OutlinedButton(
                    onClick = onBackToStudio,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Back to studio") }
            }
            else -> GallerySafeError(
                "Your artwork was saved, but this celebration view could not reopen it right now.",
                onBackToStudio,
            )
        }
    }
}

@Composable
private fun GalleryArtworkCard(
    runtime: ProductGalleryRuntime,
    card: GalleryArtworkCardModel,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GalleryCardPreview(runtime, card)
            Text(
                text = card.record.title,
                style = MaterialTheme.typography.titleMedium,
                color = StudioColors.Ink900,
            )
            Text(
                text = formatDate(card.record.completedAtEpochMillis),
                style = MaterialTheme.typography.bodySmall,
                color = StudioColors.Ink600,
            )
        }
    }
}

@Composable
private fun GalleryCardPreview(runtime: ProductGalleryRuntime, card: GalleryArtworkCardModel) {
    val reference = card.usablePreviewReference
    val preview by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = runtime,
        key2 = reference,
    ) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                reference
                    ?.let(runtime::previewFile)
                    ?.takeIf { it.isFile }
                    ?.let { BitmapFactory.decodeFile(it.absolutePath) }
                    ?.asImageBitmap()
            }.getOrNull()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(StudioColors.Paper100),
        contentAlignment = Alignment.Center,
    ) {
        if (preview != null) {
            Image(
                bitmap = preview!!,
                contentDescription = "${card.record.title} preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("My artwork", style = MaterialTheme.typography.titleMedium, color = StudioColors.Studio700)
                Spacer(Modifier.height(4.dp))
                Text("Tap to open", style = MaterialTheme.typography.bodySmall, color = StudioColors.Ink600)
            }
        }
    }
}

@Composable
private fun ReadOnlyArtworkCanvas(document: DrawingDocument) {
    val controller = remember { DrawingSurfaceController() }
    LaunchedEffect(document) { controller.reconcileDocument(document) }
    Box(modifier = Modifier.fillMaxSize()) {
        DrawingSurface(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Read-only saved artwork preview"
                },
            controller = controller,
            contentRole = DrawingSurfaceContentRole.COLORING,
        )
        // The Gallery renders the authoritative editable document through DrawingSurface, but the
        // detail/completion presentation is intentionally read-only in P3.5.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        }
                    }
                },
        )
    }
}

@Composable
private fun GalleryHeader(onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack) { Text("Back") }
        Spacer(Modifier.width(8.dp))
        Text("Art Studio", style = MaterialTheme.typography.titleMedium, color = StudioColors.Ink700)
    }
}

@Composable
private fun GalleryEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Your Gallery is waiting", style = MaterialTheme.typography.titleLarge, color = StudioColors.Ink800)
            Spacer(Modifier.height(8.dp))
            Text("Finished artwork will appear here.", color = StudioColors.Ink600)
        }
    }
}

@Composable
private fun GalleryMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = StudioColors.Ink600,
    )
}

@Composable
private fun GallerySafeError(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = StudioColors.Ink700)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

private fun detailLabel(kind: GalleryCompletionKind, completedAt: Long): String {
    val type = when (kind) {
        GalleryCompletionKind.DRAWING_ONLY -> "Finished drawing"
        GalleryCompletionKind.COLORED -> "Finished drawing and coloring"
    }
    return "$type · ${formatDate(completedAt)}"
}

private fun formatDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))
