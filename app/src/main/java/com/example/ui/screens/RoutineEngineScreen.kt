package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.SamplePresets
import com.example.model.ScheduleBlock
import com.example.model.StudyScenarioPreset
import com.example.ui.AppTab
import com.example.ui.DeskClockUiState
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.HydrationCyanLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RestEmeraldLight

@Composable
fun RoutineEngineScreen(
    uiState: DeskClockUiState,
    onUpdateRoutineTitle: (String) -> Unit,
    onUpdateUnstructuredInput: (String) -> Unit,
    onUpdateStartTime: (String) -> Unit,
    onSyncStartTimeToNow: () -> Unit,
    onUpdateTransitionMinutes: (Int) -> Unit,
    onUpdateStudyCycleMinutes: (Int) -> Unit,
    onApplyPreset: (StudyScenarioPreset) -> Unit,
    onGenerateSchedule: (Boolean) -> Unit,
    onSaveRoutine: () -> Unit,
    onSelectBlockForClock: (Int) -> Unit,
    onSpeakBlockTts: (ScheduleBlock) -> Unit,
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("routine_engine_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner Card with Custom Desk-Clock Vector Illustration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(1.dp, Color(0xFF283548))
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.ic_desk_hero_illustration),
                        contentDescription = "Intelligent Student Desk Clock Illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Schedule & Routine Engine",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter unstructured study hours, subjects, priorities, exams, and energy levels. Generates sequential military-time blocks with 3–5m transitions, hydration alerts every 45–60m, and TTS announcements.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Quick Scenario Presets Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_quick_presets),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SamplePresets.presets.forEach { preset ->
                        FilterChip(
                            selected = (uiState.routineTitle == preset.title),
                            onClick = { onApplyPreset(preset) },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = preset.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HydrationCyanLight
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E1B4B),
                                containerColor = Color(0xFF131C2E)
                            ),
                            modifier = Modifier.testTag("preset_chip_${preset.id}")
                        )
                    }
                }
            }
        }

        // 3. Unstructured User Input & Scheduling Parameters Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                border = BorderStroke(1.dp, Color(0xFF26334D))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.routineTitle,
                        onValueChange = onUpdateRoutineTitle,
                        label = { Text("Routine Title") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("routine_title_input")
                    )

                    OutlinedTextField(
                        value = uiState.unstructuredInput,
                        onValueChange = onUpdateUnstructuredInput,
                        label = { Text(stringResource(R.string.label_unstructured_prompt)) },
                        placeholder = { Text(stringResource(R.string.hint_unstructured_prompt)) },
                        minLines = 4,
                        maxLines = 7,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unstructured_prompt_input")
                    )

                    // Military Start Time (HH:mm) Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.preferredStartTime,
                            onValueChange = onUpdateStartTime,
                            label = { Text(stringResource(R.string.label_start_time)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_time_input")
                        )

                        FilledTonalButton(
                            onClick = onSyncStartTimeToNow,
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("sync_now_time_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = HydrationCyanLight
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Use Current Military Time",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Use Now (${uiState.currentMilitaryHHmm})",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    // Transition Window (3-5 min rule) & Hydration/Study Cycle (45-50 min rule)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subject Transition (HYDRATE_SWITCH):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(3, 4, 5).forEach { min ->
                                FilterChip(
                                    selected = (uiState.transitionMinutes == min),
                                    onClick = { onUpdateTransitionMinutes(min) },
                                    label = { Text("${min}m") },
                                    modifier = Modifier.testTag("transition_chip_$min")
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hydration Check Interval:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(45, 50, 55).forEach { min ->
                                FilterChip(
                                    selected = (uiState.studyCycleMinutes == min),
                                    onClick = { onUpdateStudyCycleMinutes(min) },
                                    label = { Text("${min}m") },
                                    modifier = Modifier.testTag("hydration_cycle_chip_$min")
                                )
                            }
                        }
                    }

                    // Primary Generate CTA + Launch in Desk Clock CTA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onGenerateSchedule(false) },
                            enabled = !uiState.isGenerating,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("generate_schedule_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricIndigoLight,
                                contentColor = Color(0xFF090D16)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (uiState.isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF090D16)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Calculating…", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate Schedule",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { onGenerateSchedule(true) },
                            enabled = !uiState.isGenerating,
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("generate_and_start_clock_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF083344),
                                contentColor = HydrationCyanLight
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Generate and Open Desk Clock",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("To Clock", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    // Save Routine & View Strict JSON Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSaveRoutine,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_routine_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Save Routine",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_save_routine))
                        }

                        OutlinedButton(
                            onClick = { onNavigateToTab(AppTab.JSON_SCHEMA) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("inspect_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Inspect Strict JSON",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View JSON Output")
                        }
                    }
                }
            }
        }

        // 4. Prototype Security Notice Card (Required by gemini-api / android-secret-management skills)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF162033),
                border = BorderStroke(1.dp, Color(0xFF283548))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = HydrationCyanLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${uiState.engineSourceNote} • ${stringResource(R.string.security_prototype_notice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. Generated Routine Summary + Preview List
        item {
            val summary = uiState.activeRoutine.routineSummary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryPill(
                    label = "total_study_minutes",
                    value = "${summary.totalStudyMinutes}",
                    accentColor = ElectricIndigoLight,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryPill(
                    label = "total_break_minutes",
                    value = "${summary.totalBreakMinutes}",
                    accentColor = RestEmeraldLight,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryPill(
                    label = "hydration_count",
                    value = "${summary.hydrationCount}",
                    accentColor = HydrationCyanLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        itemsIndexed(
            items = uiState.activeRoutine.schedule,
            key = { _, item -> "engine_${item.id}" }
        ) { index, block ->
            ScheduleTimelineBlockCard(
                block = block,
                isActive = (index == uiState.activeBlockIndex),
                onSelect = {
                    onSelectBlockForClock(index)
                    onNavigateToTab(AppTab.DESK_CLOCK)
                },
                onSpeakTts = { onSpeakBlockTts(block) }
            )
        }
    }
}
