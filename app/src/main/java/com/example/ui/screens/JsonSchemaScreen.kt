package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeskClockUiState
import com.example.ui.theme.AlertRose
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.HydrationCyanLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RestEmeraldLight

@Composable
fun JsonSchemaScreen(
    uiState: DeskClockUiState,
    onUpdateEditableJson: (String) -> Unit,
    onValidateAndApplyJson: () -> Unit,
    onResetJson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copiedFeedback by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("json_schema_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Schema Compliance Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (uiState.isJsonValid) RestEmeraldLight.copy(alpha = 0.6f) else AlertRose
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isJsonValid) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = "Schema Validation Status",
                            tint = if (uiState.isJsonValid) RestEmeraldLight else AlertRose,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Strict JSON Output Schema",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = uiState.jsonValidationMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isJsonValid) RestEmeraldLight else AlertRose,
                                modifier = Modifier.testTag("json_validation_status_text")
                            )
                        }
                    }

                    // Output Rules Verification Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SchemaRuleBadge("HH:mm Military", HydrationCyanLight, Modifier.weight(1f))
                        SchemaRuleBadge("3–5m Transitions", ElectricIndigoLight, Modifier.weight(1f))
                        SchemaRuleBadge("TTS + Hydrate", RestEmeraldLight, Modifier.weight(1f))
                    }

                    // Action Buttons: Copy Strict JSON + Validate & Apply + Restore
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(
                                    ClipData.newPlainText("ChronoStudy Routine JSON", uiState.strictJsonOutput)
                                )
                                copiedFeedback = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HydrationCyanLight,
                                contentColor = Color(0xFF042F2E)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("copy_strict_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy JSON",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (copiedFeedback) "Copied JSON!" else "Copy JSON",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        FilledTonalButton(
                            onClick = onValidateAndApplyJson,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("validate_apply_json_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF1E1B4B),
                                contentColor = ElectricIndigoLight
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Validate and Apply JSON",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Apply JSON", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = onResetJson,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("reset_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Reset JSON",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Live Editable / Inspectable Strict JSON Payload
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                border = BorderStroke(1.dp, Color(0xFF283548))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "routine_summary + schedule JSON Payload",
                        style = MaterialTheme.typography.labelSmall,
                        color = HydrationCyanLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.editableJsonText,
                        onValueChange = onUpdateEditableJson,
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFE2E8F0)
                        ),
                        minLines = 18,
                        maxLines = 45,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("strict_json_editor_field")
                    )
                }
            }
        }
    }
}

@Composable
private fun SchemaRuleBadge(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
