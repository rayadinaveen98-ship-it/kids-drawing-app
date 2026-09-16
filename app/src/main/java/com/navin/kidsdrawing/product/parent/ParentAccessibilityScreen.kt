package com.navin.kidsdrawing.product.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.product.accessibility.AccessibilityPreferences
import com.navin.kidsdrawing.product.design.StudioColors
import com.navin.kidsdrawing.product.profile.NarrationPreference
import kotlinx.coroutines.launch

@Composable
fun ParentAccessibilityScreen(
    narrationPreference: NarrationPreference,
    preferences: AccessibilityPreferences,
    onSetReduceMotion: suspend (Boolean) -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioColors.Paper50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Accessibility & Audio",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineLarge,
                    color = StudioColors.Ink900,
                )
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Back")
                }
            }

            AccessibilityInfoCard(
                title = "Text size follows Android",
                body = "Kids Drawing App respects the device's system font size. There is no separate in-app text-size slider, so families do not have two competing text settings.",
            )

            AccessibilityInfoCard(
                title = "Narration default",
                body = "Current child-profile default: ${narrationPreference.displayName}. Change this learning preference in Family. Important instructions remain visible as text.",
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .semantics {
                        stateDescription = if (preferences.reduceMotion) {
                            "Reduce motion on"
                        } else {
                            "Reduce motion off"
                        }
                    }
                    .toggleable(
                        value = preferences.reduceMotion,
                        enabled = !saving,
                        role = Role.Switch,
                        onValueChange = { enabled ->
                            saving = true
                            message = null
                            scope.launch {
                                val saved = onSetReduceMotion(enabled)
                                saving = false
                                if (!saved) {
                                    message = "That setting could not save yet. Your previous accessibility setting is unchanged."
                                }
                            }
                        },
                    ),
                shape = RoundedCornerShape(20.dp),
                color = StudioColors.Paper100,
                border = BorderStroke(1.dp, StudioColors.Line200),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reduce motion",
                            style = MaterialTheme.typography.titleLarge,
                            color = StudioColors.Ink900,
                        )
                        Text(
                            text = "Simplifies decorative and transition motion. It does not shorten Parent Gate timing or skip teaching demonstrations.",
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioColors.Ink700,
                        )
                    }
                    // The whole row is the accessible switch. This child is visual state only so
                    // TalkBack does not encounter a duplicate nested switch node.
                    Switch(
                        checked = preferences.reduceMotion,
                        onCheckedChange = null,
                        enabled = !saving,
                        modifier = Modifier.clearAndSetSemantics { },
                    )
                }
            }

            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioColors.Ink700,
                )
            }

            AccessibilityInfoCard(
                title = "Drawing canvas",
                body = "Freehand drawing and spatial Fill are direct touch or stylus creative interactions. Accessibility V2 improves the surrounding labels, controls and status without pretending the drawing geometry itself has an equivalent non-visual interaction.",
            )

            AccessibilityInfoCard(
                title = "What Reduce motion does not change",
                body = "The 2.5-second adult-intent hold, accessible two-confirmation fallback, five-minute Parent Zone session limit, child/adult ownership boundaries and lesson truth remain exactly the same.",
            )
        }
    }
}

@Composable
private fun AccessibilityInfoCard(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = StudioColors.Paper100,
        border = BorderStroke(1.dp, StudioColors.Line200),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = StudioColors.Ink900,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = StudioColors.Ink700,
            )
        }
    }
}
