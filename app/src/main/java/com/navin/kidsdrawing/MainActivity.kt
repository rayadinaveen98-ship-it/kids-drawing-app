package com.navin.kidsdrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navin.kidsdrawing.drawing.domain.DrawingSurfaceMetrics
import com.navin.kidsdrawing.drawing.ui.DrawingSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtLabTheme {
                ArtLabLauncher()
            }
        }
    }
}

@Composable
private fun ArtLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

@Composable
private fun ArtLabLauncher() {
    var metrics by remember { mutableStateOf(DrawingSurfaceMetrics()) }

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
                text = "P1.2 low-latency engine surface · finger/stylus test canvas",
                color = Ink700,
                fontSize = 15.sp,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("Ink 1.0")
                StatusChip("1000 × 1000 doc")
                StatusChip("Offline")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("strokes", metrics.committedStrokeCount.toString())
                MetricChip("samples", metrics.lastSampleCount.toString())
                MetricChip("pressure", metrics.lastPressure?.let { "%.2f".format(it) } ?: "—")
                MetricChip(
                    "handoff",
                    metrics.lastCommitLatencyMillis?.let { "${it}ms" } ?: "—",
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
            ) {
                DrawingSurface(
                    modifier = Modifier.fillMaxSize(),
                    onMetricsChanged = { metrics = it },
                )
            }

            Text(
                text = "Draw repeatedly. Secondary pointers are ignored; stylus pressure is captured. Resize/rotation keeps committed geometry in logical document coordinates.",
                color = Ink700,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Studio100,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            color = Studio600,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
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

private val Paper50 = Color(0xFFFFFDF8)
private val Ink900 = Color(0xFF242321)
private val Ink700 = Color(0xFF4E4A45)
private val Studio600 = Color(0xFF5C6F52)
private val Studio100 = Color(0xFFEAF0E5)

@Preview(showBackground = true, widthDp = 900, heightDp = 600)
@Composable
private fun ArtLabLauncherPreview() {
    ArtLabTheme {
        ArtLabLauncher()
    }
}
