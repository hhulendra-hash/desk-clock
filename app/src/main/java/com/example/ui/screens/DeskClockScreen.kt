package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ScheduleBlock
import com.example.tts.TtsUiState
import com.example.ui.AppTab
import com.example.ui.DeskClockUiState
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.HydrationCyanLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PriorityAmberLight
import com.example.ui.theme.RestEmeraldLight
import java.util.Locale

@Composable
fun DeskClockScreen(
    uiState: DeskClockUiState,
    ttsState: TtsUiState,
    onTogglePlayPause: () -> Unit,
    onResetTimer: () -> Unit,
    onPreviousBlock: () -> Unit,
    onNextBlock: () -> Unit,
    onSelectBlock: (Int) -> Unit,
    onSpeakActiveReminder: () -> Unit,
    onSpeakBlockReminder: (ScheduleBlock) -> Unit,
    onToggleMuteTts: () -> Unit,
    onLogHydration: () -> Unit,
    onTriggerEyeRest: () -> Unit,
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlock = uiState.activeBlock
    val blockColor = activeBlock?.parsedColor() ?: ElectricIndigoLight

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("desk_clock_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Military Desk-Clock Hero Display
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DeskClockFlipBezelCard(
                    uiState = uiState,
                    ttsState = ttsState,
                    activeBlock = activeBlock,
                    blockColor = blockColor,
                    onTogglePlayPause = onTogglePlayPause,
                    onResetTimer = onResetTimer,
                    onPreviousBlock = onPreviousBlock,
                    onNextBlock = onNextBlock,
                    onSpeakActiveReminder = onSpeakActiveReminder,
                    onToggleMuteTts = onToggleMuteTts
                )
            }
        }

        // 2. Active Eye-Rest 20-20-20 Countdown Banner (when triggered)
        item {
            AnimatedVisibility(
                visible = uiState.eyeRestCountdownSeconds > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("eye_rest_active_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF064E3B)
                    ),
                    border = BorderStroke(1.dp, RestEmeraldLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveRedEye,
                                contentDescription = "Eye Rest Active",
                                tint = RestEmeraldLight,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "20-20-20 Eye-Rest Cue Active",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Focus on an object 20 feet away to relax ciliary muscles.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFD1FAE5)
                                )
                            }
                        }
                        Text(
                            text = "${uiState.eyeRestCountdownSeconds}s",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = RestEmeraldLight
                        )
                    }
                }
            }
        }

        // 3. Routine Summary & Wellness Cues Bar
        item {
            RoutineSummaryAndWellnessRow(
                uiState = uiState,
                onLogHydration = onLogHydration,
                onTriggerEyeRest = onTriggerEyeRest,
                onOpenEngine = { onNavigateToTab(AppTab.ROUTINE_ENGINE) },
                onOpenJson = { onNavigateToTab(AppTab.JSON_SCHEMA) }
            )
        }

        // 4. Sequential Schedule Timeline Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.routineTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${uiState.activeRoutine.schedule.size} Sequential Blocks • Tap any block to load & speak TTS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { onNavigateToTab(AppTab.ROUTINE_ENGINE) },
                    modifier = Modifier.testTag("customize_routine_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Edit in Routine Engine",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Brief", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // 5. Sequential Schedule Blocks List
        itemsIndexed(
            items = uiState.activeRoutine.schedule,
            key = { _, block -> block.id }
        ) { index, block ->
            ScheduleTimelineBlockCard(
                block = block,
                isActive = (index == uiState.activeBlockIndex),
                onSelect = { onSelectBlock(index) },
                onSpeakTts = { onSpeakBlockReminder(block) }
            )
        }
    }
}

@Composable
private fun DeskClockFlipBezelCard(
    uiState: DeskClockUiState,
    ttsState: TtsUiState,
    activeBlock: ScheduleBlock?,
    blockColor: Color,
    onTogglePlayPause: () -> Unit,
    onResetTimer: () -> Unit,
    onPreviousBlock: () -> Unit,
    onNextBlock: () -> Unit,
    onSpeakActiveReminder: () -> Unit,
    onToggleMuteTts: () -> Unit
) {
    val remMinutes = uiState.remainingBlockSeconds / 60
    val remSeconds = uiState.remainingBlockSeconds % 60
    val countdownFormatted = String.format(Locale.US, "%02d:%02d", remMinutes, remSeconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .testTag("desk_clock_hero_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    blockColor.copy(alpha = 0.85f),
                    HydrationCyanLight.copy(alpha = 0.45f)
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E1525)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status Bar: 24H Military Time Badge + Date + TTS Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = blockColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, blockColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = activeBlock?.activityType ?: "STUDY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = uiState.currentDateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleMuteTts,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("tts_mute_toggle_button")
                ) {
                    Icon(
                        imageVector = if (ttsState.isMuted) {
                            Icons.AutoMirrored.Filled.VolumeOff
                        } else {
                            Icons.AutoMirrored.Filled.VolumeUp
                        },
                        contentDescription = if (ttsState.isMuted) "Unmute TTS Voice" else "Mute TTS Voice",
                        tint = if (ttsState.isMuted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            HydrationCyanLight
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main 24-Hour Military Desk Clock Readout + Active Block Ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MILITARY DESK CLOCK (HH:mm:ss)",
                        style = MaterialTheme.typography.labelSmall,
                        color = HydrationCyanLight
                    )
                    Text(
                        text = uiState.currentMilitaryTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 38.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.testTag("live_military_clock_text")
                    )
                    if (activeBlock != null) {
                        Text(
                            text = "Block Window: ${activeBlock.startTime} – ${activeBlock.endTime} (${activeBlock.durationMinutes} min)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Circular Countdown Gauge for Current Block
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(108.dp)
                ) {
                    val progress = uiState.blockProgressFraction
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 9.dp.toPx()
                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = blockColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = countdownFormatted,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.testTag("block_countdown_text")
                        )
                        Text(
                            text = if (uiState.isTimerRunning) "ACTIVE" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isTimerRunning) RestEmeraldLight else PriorityAmberLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Block Subject & TTS Reminder Teleprompter Box
            if (activeBlock != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF162033),
                    border = BorderStroke(1.dp, Color(0xFF283548))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${activeBlock.id} • ${activeBlock.subject}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (activeBlock.hydrationAlert) {
                                Surface(
                                    color = HydrationCyanLight.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = "Hydration Alert",
                                            tint = HydrationCyanLight,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "HYDRATE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HydrationCyanLight
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "\"${activeBlock.ttsReminderText}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.testTag("active_tts_reminder_text")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { uiState.blockProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = blockColor,
                            trackColor = Color(0xFF090D16)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Desk Clock Transport Controls + TTS Speak Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = onPreviousBlock,
                        enabled = uiState.activeBlockIndex > 0,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("prev_block_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Block",
                            tint = Color.White
                        )
                    }

                    Button(
                        onClick = onTogglePlayPause,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isTimerRunning) PriorityAmberLight else blockColor
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("play_pause_timer_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isTimerRunning) "Pause Timer" else "Start Timer",
                            tint = if (uiState.isTimerRunning) Color(0xFF090D16) else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isTimerRunning) "Pause" else "Start Focus",
                            color = if (uiState.isTimerRunning) Color(0xFF090D16) else Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    FilledIconButton(
                        onClick = onNextBlock,
                        enabled = uiState.activeBlockIndex + 1 < uiState.activeRoutine.schedule.size,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("next_block_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Block",
                            tint = Color.White
                        )
                    }

                    FilledIconButton(
                        onClick = onResetTimer,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("reset_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Block Timer",
                            tint = Color.White
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onSpeakActiveReminder,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("speak_tts_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = HydrationCyanLight.copy(alpha = 0.18f),
                        contentColor = HydrationCyanLight
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak TTS Reminder",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TTS Cue", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun RoutineSummaryAndWellnessRow(
    uiState: DeskClockUiState,
    onLogHydration: () -> Unit,
    onTriggerEyeRest: () -> Unit,
    onOpenEngine: () -> Unit,
    onOpenJson: () -> Unit
) {
    val summary = uiState.activeRoutine.routineSummary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 3 Metric Summary Cards matching JSON routine_summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricSummaryPill(
                label = "STUDY MIN",
                value = "${summary.totalStudyMinutes}m",
                accentColor = ElectricIndigoLight,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_study_minutes")
            )
            MetricSummaryPill(
                label = "BREAK / SWITCH",
                value = "${summary.totalBreakMinutes}m",
                accentColor = RestEmeraldLight,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_break_minutes")
            )
            MetricSummaryPill(
                label = "HYDRATION",
                value = "${summary.hydrationCount} alerts",
                accentColor = HydrationCyanLight,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_hydration_count")
            )
        }

        // Quick Desk Wellness Station: Log Water + 20-20-20 Eye Rest + View Strict JSON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onLogHydration,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("log_hydration_button"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF083344),
                    contentColor = HydrationCyanLight
                )
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Log Water",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hydrate (${uiState.hydrationGlassesLogged})",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }

            FilledTonalButton(
                onClick = onTriggerEyeRest,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("eye_rest_cue_button"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF064E3B),
                    contentColor = RestEmeraldLight
                )
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = "20-20-20 Eye Rest",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "20-20-20 Eye Cue",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }

            FilledTonalButton(
                onClick = onOpenJson,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("open_json_tab_shortcut"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Strict JSON Output",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("JSON", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun MetricSummaryPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131C2E),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor
            )
        }
    }
}

@Composable
fun ScheduleTimelineBlockCard(
    block: ScheduleBlock,
    isActive: Boolean,
    onSelect: () -> Unit,
    onSpeakTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blockColor = block.parsedColor()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onSelect)
            .testTag("schedule_block_card_${block.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                blockColor.copy(alpha = 0.18f)
            } else {
                Color(0xFF131C2E)
            }
        ),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) blockColor else Color(0xFF26334D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Military Time Column (start_time - end_time)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp)
            ) {
                Text(
                    text = block.startTime,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .width(2.dp)
                        .height(12.dp)
                        .background(blockColor)
                )
                Text(
                    text = block.endTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Subject, Activity Badge, Hydration Alert, and TTS String
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = blockColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (block.activityType == "HYDRATE_SWITCH") {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = blockColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = "${block.activityType} • ${block.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    if (block.hydrationAlert) {
                        Surface(
                            color = HydrationCyanLight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Hydration Alert",
                                    tint = HydrationCyanLight,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Hydrate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HydrationCyanLight
                                )
                            }
                        }
                    }

                    // Hex Color Swatch Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(blockColor)
                        )
                        Text(
                            text = block.colorHex,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = block.subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "TTS: \"${block.ttsReminderText}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onSpeakTts,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("speak_block_${block.id}_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Speak TTS Reminder for ${block.subject}",
                    tint = HydrationCyanLight
                )
            }
        }
    }
}
