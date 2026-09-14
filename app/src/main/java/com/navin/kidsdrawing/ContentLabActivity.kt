package com.navin.kidsdrawing

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.ContentInspectionLesson
import com.navin.kidsdrawing.lesson.content.ContentInspectionRepository
import com.navin.kidsdrawing.lesson.content.ContentInspectionSnapshot
import com.navin.kidsdrawing.lesson.model.AuthoredStroke
import com.navin.kidsdrawing.lesson.model.DrawingStep

class ContentLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inspection = ContentInspectionRepository(AndroidAssetLessonSource(assets)).load()
        setContent {
            MaterialTheme {
                ContentLabScreen(inspection)
            }
        }
    }
}

@Composable
private fun ContentLabScreen(inspection: ContentInspectionSnapshot) {
    var selectedLessonId by remember {
        mutableStateOf(inspection.lessons.firstOrNull()?.entry?.identity?.lessonId)
    }
    val lesson = inspection.lessons.firstOrNull { it.entry.identity.lessonId == selectedLessonId }
        ?: inspection.lessons.firstOrNull()
    var selectedStepIndex by remember { mutableIntStateOf(0) }
    var showThumbnail by remember { mutableStateOf(false) }

    LaunchedEffect(selectedLessonId) {
        selectedStepIndex = 0
        showThumbnail = false
    }

    Surface(modifier = Modifier.fillMaxSize(), color = LabPaper) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Kids Drawing · Content Lab",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = LabInk,
            )
            Text(
                text = "P5.2 read-only authoring inspection · no child document/session writes",
                fontSize = 12.sp,
                color = LabMuted,
            )

            ReportSummary(inspection)

            if (inspection.lessons.isEmpty()) {
                Text("No validated release lessons are available.", color = LabDanger)
                return@Column
            }

            Text("Lesson", fontWeight = FontWeight.Bold, color = LabInk)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(inspection.lessons, key = { it.entry.identity.lessonId }) { item ->
                    val selected = item.entry.identity.lessonId == lesson?.entry?.identity?.lessonId
                    if (selected) {
                        Button(onClick = { selectedLessonId = item.entry.identity.lessonId }) {
                            Text(item.entry.title, fontSize = 11.sp)
                        }
                    } else {
                        OutlinedButton(onClick = { selectedLessonId = item.entry.identity.lessonId }) {
                            Text(item.entry.title, fontSize = 11.sp)
                        }
                    }
                }
            }

            lesson?.let { selected ->
                LessonHeader(selected)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!showThumbnail) {
                        Button(onClick = { showThumbnail = false }) { Text("Preview") }
                        OutlinedButton(onClick = { showThumbnail = true }) { Text("Thumbnail") }
                    } else {
                        OutlinedButton(onClick = { showThumbnail = false }) { Text("Preview") }
                        Button(onClick = { showThumbnail = true }) { Text("Thumbnail") }
                    }
                }

                SvgAssetPreview(
                    svg = if (showThumbnail) selected.thumbnailSvg else selected.previewSvg,
                    label = if (showThumbnail) selected.entry.thumbnailPath else selected.entry.previewPath,
                )

                val steps = selected.runtime.lesson.drawing.steps
                Text("Drawing step", fontWeight = FontWeight.Bold, color = LabInk)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(steps.indices.toList()) { index ->
                        val step = steps[index]
                        if (selectedStepIndex == index) {
                            Button(onClick = { selectedStepIndex = index }) {
                                Text("${index + 1}. ${step.id}", fontSize = 11.sp)
                            }
                        } else {
                            OutlinedButton(onClick = { selectedStepIndex = index }) {
                                Text("${index + 1}. ${step.id}", fontSize = 11.sp)
                            }
                        }
                    }
                }

                val step = steps.getOrNull(selectedStepIndex)
                GeometryInspector(selected, step)
                StepDetails(selected, step)
                LessonDiagnostics(selected)
                LocalizationInspector(selected, step)
            }
        }
    }
}

@Composable
private fun ReportSummary(inspection: ContentInspectionSnapshot) {
    val report = inspection.qualityReport
    Surface(
        color = LabPanel,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Catalog quality", fontWeight = FontWeight.Bold, color = LabInk)
            Text(
                "${report.lessonCount} lessons · ${report.errorCount} errors · ${report.warningCount} warnings",
                fontSize = 12.sp,
                color = if (report.errorCount == 0) LabSuccess else LabDanger,
            )
            Text(
                "Phase 5 progress: ${report.phase5Progress.lessonCount}/${report.phase5Progress.lessonTarget} lessons · " +
                    "Watch Then Draw ${report.phase5Progress.watchThenDrawCount}/${report.phase5Progress.watchThenDrawTarget}",
                fontSize = 11.sp,
                color = LabMuted,
            )
            Text(
                "Age coverage: " + report.ageBandCounts.entries.joinToString(" · ") { "${it.key.name} ${it.value}" },
                fontSize = 10.sp,
                color = LabMuted,
            )
        }
    }
}

@Composable
private fun LessonHeader(lesson: ContentInspectionLesson) {
    val source = lesson.runtime.lesson
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(lesson.entry.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LabInk)
            Text(lesson.entry.summary, fontSize = 12.sp, color = LabMuted)
            Text(
                "${lesson.entry.identity.lessonId}@${lesson.entry.identity.revision} · difficulty ${lesson.entry.difficulty} · " +
                    "${lesson.entry.estimatedMinutes} min · canvas ${source.canvas.width}×${source.canvas.height}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = LabInk,
            )
            Text("Ages: ${lesson.entry.ageBands.joinToString { it.name }}", fontSize = 11.sp, color = LabMuted)
            Text("Modes: ${lesson.entry.supportedModes.joinToString { it.name }}", fontSize = 11.sp, color = LabMuted)
            Text("Journeys: ${lesson.entry.journeyIds.ifEmpty { setOf("standalone") }.joinToString()}", fontSize = 11.sp, color = LabMuted)
            Text("Skills: ${lesson.entry.skillIds.joinToString()}", fontSize = 10.sp, color = LabMuted)
        }
    }
}

@Composable
private fun SvgAssetPreview(svg: String?, label: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = LabMuted,
            )
            if (svg.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("SVG asset unavailable", color = LabDanger)
                }
            } else {
                val context = LocalContext.current
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    factory = {
                        WebView(context).apply {
                            settings.javaScriptEnabled = false
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        }
                    },
                    update = { webView ->
                        if (webView.tag != svg) {
                            webView.tag = svg
                            webView.loadDataWithBaseURL(null, svg, "image/svg+xml", "UTF-8", null)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GeometryInspector(lesson: ContentInspectionLesson, step: DrawingStep?) {
    val runtime = lesson.runtime
    val strokeById = runtime.strokeCatalog.strokes.associateBy(AuthoredStroke::id)
    val guideById = runtime.strokeCatalog.guides.associateBy { it.id }
    val teacherStrokes = step?.teacher?.strokeRefs.orEmpty().mapNotNull(strokeById::get)
    val expectedStrokes = step?.childTurn?.expectedStrokeRefs.orEmpty().mapNotNull(strokeById::get)
    val guideStrokes = step?.help.orEmpty()
        .flatMap { it.guideRefs }
        .distinct()
        .mapNotNull(guideById::get)
        .flatMap { it.strokeRefs }
        .distinct()
        .mapNotNull(strokeById::get)
    val regions = runtime.coloringRegionCatalog?.regions.orEmpty()

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text("Authored geometry", fontWeight = FontWeight.Bold, color = LabInk)
            Text(
                "Teacher ${teacherStrokes.size} · expected/trace ${expectedStrokes.size} · help-guide ${guideStrokes.size} · regions ${regions.size}",
                fontSize = 10.sp,
                color = LabMuted,
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(LabCanvas),
            ) {
                val canvasWidth = runtime.lesson.canvas.width.toFloat().coerceAtLeast(1f)
                val canvasHeight = runtime.lesson.canvas.height.toFloat().coerceAtLeast(1f)
                fun sx(x: Float) = x / canvasWidth * size.width
                fun sy(y: Float) = y / canvasHeight * size.height

                regions.forEach { region ->
                    if (region.points.size >= 3) {
                        val path = Path().apply {
                            val first = region.points.first()
                            moveTo(sx(first.x), sy(first.y))
                            region.points.drop(1).forEach { point -> lineTo(sx(point.x), sy(point.y)) }
                            close()
                        }
                        drawPath(path, color = LabRegion.copy(alpha = 0.18f))
                        drawPath(path, color = LabRegion, style = Stroke(width = 2f))
                    }
                }

                expectedStrokes.forEach { stroke -> drawAuthoredStroke(stroke, sx = ::sx, sy = ::sy, color = LabExpected, width = 5f) }
                guideStrokes.forEach { stroke -> drawAuthoredStroke(stroke, sx = ::sx, sy = ::sy, color = LabGuide, width = 4f) }
                teacherStrokes.forEach { stroke -> drawAuthoredStroke(stroke, sx = ::sx, sy = ::sy, color = LabTeacher, width = 3f) }
            }
            Text(
                "Black = teacher · violet = expected/trace · blue = help guides · amber = prepared regions",
                fontSize = 10.sp,
                color = LabMuted,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAuthoredStroke(
    stroke: AuthoredStroke,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    color: Color,
    width: Float,
) {
    if (stroke.points.size < 2) return
    val path = Path().apply {
        val first = stroke.points.first()
        moveTo(sx(first.x), sy(first.y))
        stroke.points.drop(1).forEach { point -> lineTo(sx(point.x), sy(point.y)) }
    }
    drawPath(path, color = color, style = Stroke(width = width))
}

@Composable
private fun StepDetails(lesson: ContentInspectionLesson, step: DrawingStep?) {
    if (step == null) return
    Surface(
        color = LabPanel,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Step contract", fontWeight = FontWeight.Bold, color = LabInk)
            Text("ID: ${step.id}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            Text("Objectives: ${step.objectiveSkillIds.joinToString()}", fontSize = 11.sp)
            Text(
                "Teacher refs: ${step.teacher.strokeRefs.joinToString()} · group=${step.teacher.playAsGroup} · normal=${step.teacher.normalDurationMs ?: "default"}ms",
                fontSize = 10.sp,
            )
            Text(
                "Child: ${step.childTurn.completionPolicy.name} · replay=${step.childTurn.allowReplay} · skip=${step.childTurn.allowSkip} · expected=${step.childTurn.expectedStrokeRefs.joinToString()}",
                fontSize = 10.sp,
            )
            if (step.help.isEmpty()) {
                Text("Help Ladder: none", fontSize = 10.sp, color = LabMuted)
            } else {
                step.help.sortedBy { it.level }.forEach { help ->
                    Text(
                        "Help ${help.level}: ${help.kind.name} · guides=${help.guideRefs.joinToString()}",
                        fontSize = 10.sp,
                    )
                }
            }
            val coloring = lesson.runtime.lesson.coloring
            Text(
                "Coloring: ${if (coloring?.enabled == true) "enabled · ${coloring.defaultMode}" else "off"}",
                fontSize = 10.sp,
                color = LabMuted,
            )
        }
    }
}

@Composable
private fun LessonDiagnostics(lesson: ContentInspectionLesson) {
    val diagnostics = lesson.qualitySummary?.diagnostics.orEmpty()
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Quality diagnostics", fontWeight = FontWeight.Bold, color = LabInk)
            if (diagnostics.isEmpty()) {
                Text("No analyzer diagnostics for this lesson.", color = LabSuccess, fontSize = 11.sp)
            } else {
                diagnostics.forEach { diagnostic ->
                    Text(
                        "${diagnostic.severity} · ${diagnostic.code}: ${diagnostic.message}",
                        fontSize = 10.sp,
                        color = if (diagnostic.severity.name == "ERROR") LabDanger else LabWarning,
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalizationInspector(lesson: ContentInspectionLesson, step: DrawingStep?) {
    val keys = buildList {
        add(lesson.runtime.lesson.metadata.titleKey)
        add(lesson.runtime.lesson.metadata.summaryKey)
        step?.teacher?.narrationKey?.let(::add)
        step?.completionNarrationKey?.let(::add)
        step?.help.orEmpty().forEach { it.narrationKey?.let(::add) }
    }.distinct()

    Surface(
        color = LabPanel,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Default localization", fontWeight = FontWeight.Bold, color = LabInk)
            keys.forEach { key ->
                Text(
                    "$key → ${lesson.defaultStrings[key] ?: "<missing>"}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (lesson.defaultStrings[key].isNullOrBlank()) LabDanger else LabInk,
                )
            }
        }
    }
}

private val LabPaper = Color(0xFFF6F3EC)
private val LabPanel = Color(0xFFECE7DD)
private val LabCanvas = Color(0xFFFFFEFA)
private val LabInk = Color(0xFF282622)
private val LabMuted = Color(0xFF6F6A61)
private val LabSuccess = Color(0xFF2F6F4E)
private val LabWarning = Color(0xFF8A641D)
private val LabDanger = Color(0xFF9A3B34)
private val LabTeacher = Color(0xFF242424)
private val LabExpected = Color(0xFF7651B6)
private val LabGuide = Color(0xFF3478A8)
private val LabRegion = Color(0xFFC8882A)
