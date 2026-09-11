package com.navin.kidsdrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Kids Drawing · Art Lab",
                color = Ink900,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Internal engineering workspace for Drawing Engine 0.1. Production child UI starts after the core engine is proven.",
                color = Ink700,
                fontSize = 17.sp,
                lineHeight = 24.sp,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip("Phase 1")
                StatusChip("0.1.0 target")
                StatusChip("Offline")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "DrawingSurface lands in P1.2 · Issue #9",
                    color = Ink700,
                    fontSize = 16.sp,
                )
            }

            Text(
                text = "Scaffold goal: clean build → CI → debug APK → engine implementation.",
                color = Ink700,
                fontSize = 14.sp,
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Studio600,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
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
