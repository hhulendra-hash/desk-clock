package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.DeskClockViewModel
import com.example.ui.screens.DeskClockScreen
import com.example.ui.screens.JsonSchemaScreen
import com.example.ui.screens.RoutineEngineScreen
import com.example.ui.screens.SavedRoutinesScreen
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.HydrationCyanLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                ChronoStudyApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronoStudyApp(
    viewModel: DeskClockViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()
    val savedRoutines by viewModel.savedRoutines.collectAsStateWithLifecycle()

    // BackHandler on secondary tabs returns to DESK_CLOCK
    BackHandler(enabled = uiState.selectedTab != AppTab.DESK_CLOCK) {
        viewModel.selectTab(AppTab.DESK_CLOCK)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 720.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, HydrationCyanLight.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = uiState.currentMilitaryHHmm,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = HydrationCyanLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF090D16)
                    )
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        containerColor = Color(0xFF0E1525),
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = uiState.selectedTab == AppTab.DESK_CLOCK,
                            onClick = { viewModel.selectTab(AppTab.DESK_CLOCK) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.selectedTab == AppTab.DESK_CLOCK) {
                                        Icons.Filled.AccessTimeFilled
                                    } else {
                                        Icons.Outlined.AccessTime
                                    },
                                    contentDescription = stringResource(R.string.nav_desk_clock)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_desk_clock)) },
                            modifier = Modifier.testTag("nav_tab_desk_clock")
                        )
                        NavigationBarItem(
                            selected = uiState.selectedTab == AppTab.ROUTINE_ENGINE,
                            onClick = { viewModel.selectTab(AppTab.ROUTINE_ENGINE) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.selectedTab == AppTab.ROUTINE_ENGINE) {
                                        Icons.Filled.AutoAwesome
                                    } else {
                                        Icons.Outlined.AutoAwesome
                                    },
                                    contentDescription = stringResource(R.string.nav_routine_engine)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_routine_engine)) },
                            modifier = Modifier.testTag("nav_tab_routine_engine")
                        )
                        NavigationBarItem(
                            selected = uiState.selectedTab == AppTab.JSON_SCHEMA,
                            onClick = { viewModel.selectTab(AppTab.JSON_SCHEMA) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.selectedTab == AppTab.JSON_SCHEMA) {
                                        Icons.Filled.Code
                                    } else {
                                        Icons.Outlined.Code
                                    },
                                    contentDescription = stringResource(R.string.nav_json_output)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_json_output)) },
                            modifier = Modifier.testTag("nav_tab_json_schema")
                        )
                        NavigationBarItem(
                            selected = uiState.selectedTab == AppTab.SAVED_ROUTINES,
                            onClick = { viewModel.selectTab(AppTab.SAVED_ROUTINES) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.selectedTab == AppTab.SAVED_ROUTINES) {
                                        Icons.Filled.Bookmarks
                                    } else {
                                        Icons.Outlined.Bookmarks
                                    },
                                    contentDescription = stringResource(R.string.nav_saved_routines)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_saved_routines)) },
                            modifier = Modifier.testTag("nav_tab_saved_routines")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = Color(0xFF0E1525),
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("side_navigation_rail")
                    ) {
                        NavigationRailItem(
                            selected = uiState.selectedTab == AppTab.DESK_CLOCK,
                            onClick = { viewModel.selectTab(AppTab.DESK_CLOCK) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.AccessTimeFilled,
                                    contentDescription = stringResource(R.string.nav_desk_clock)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_desk_clock)) }
                        )
                        NavigationRailItem(
                            selected = uiState.selectedTab == AppTab.ROUTINE_ENGINE,
                            onClick = { viewModel.selectTab(AppTab.ROUTINE_ENGINE) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = stringResource(R.string.nav_routine_engine)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_routine_engine)) }
                        )
                        NavigationRailItem(
                            selected = uiState.selectedTab == AppTab.JSON_SCHEMA,
                            onClick = { viewModel.selectTab(AppTab.JSON_SCHEMA) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Code,
                                    contentDescription = stringResource(R.string.nav_json_output)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_json_output)) }
                        )
                        NavigationRailItem(
                            selected = uiState.selectedTab == AppTab.SAVED_ROUTINES,
                            onClick = { viewModel.selectTab(AppTab.SAVED_ROUTINES) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Bookmarks,
                                    contentDescription = stringResource(R.string.nav_saved_routines)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_saved_routines)) }
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Status / Notification Banner
                    AnimatedVisibility(visible = uiState.statusBannerMessage != null) {
                        uiState.statusBannerMessage?.let { message ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .testTag("status_banner"),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E1B4B),
                                border = BorderStroke(1.dp, ElectricIndigoLight.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.dismissBannerMessage() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss notification",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (uiState.selectedTab) {
                            AppTab.DESK_CLOCK -> DeskClockScreen(
                                uiState = uiState,
                                ttsState = ttsState,
                                onTogglePlayPause = viewModel::toggleTimerPlayPause,
                                onResetTimer = viewModel::resetCurrentBlockTimer,
                                onPreviousBlock = viewModel::previousBlock,
                                onNextBlock = viewModel::nextBlock,
                                onSelectBlock = { idx -> viewModel.selectScheduleBlock(idx) },
                                onSpeakActiveReminder = viewModel::speakActiveBlockReminder,
                                onSpeakBlockReminder = viewModel::speakSpecificBlockReminder,
                                onToggleMuteTts = viewModel.ttsManager::toggleMute,
                                onLogHydration = viewModel::logWaterIntake,
                                onTriggerEyeRest = viewModel::trigger202020EyeRestCue,
                                onNavigateToTab = viewModel::selectTab
                            )

                            AppTab.ROUTINE_ENGINE -> RoutineEngineScreen(
                                uiState = uiState,
                                onUpdateRoutineTitle = viewModel::updateRoutineTitle,
                                onUpdateUnstructuredInput = viewModel::updateUnstructuredInput,
                                onUpdateStartTime = viewModel::updatePreferredStartTime,
                                onSyncStartTimeToNow = viewModel::syncStartTimeToCurrentClock,
                                onUpdateTransitionMinutes = viewModel::updateTransitionMinutes,
                                onUpdateStudyCycleMinutes = viewModel::updateStudyCycleMinutes,
                                onApplyPreset = viewModel::applyScenarioPreset,
                                onGenerateSchedule = viewModel::generateRoutineSchedule,
                                onSaveRoutine = viewModel::saveCurrentRoutineToDatabase,
                                onSelectBlockForClock = { idx -> viewModel.selectScheduleBlock(idx) },
                                onSpeakBlockTts = viewModel::speakSpecificBlockReminder,
                                onNavigateToTab = viewModel::selectTab
                            )

                            AppTab.JSON_SCHEMA -> JsonSchemaScreen(
                                uiState = uiState,
                                onUpdateEditableJson = viewModel::updateEditableJsonText,
                                onValidateAndApplyJson = viewModel::validateAndApplyEditedJson,
                                onResetJson = viewModel::resetEditableJsonToCurrentRoutine
                            )

                            AppTab.SAVED_ROUTINES -> SavedRoutinesScreen(
                                savedRoutines = savedRoutines,
                                onLoadRoutine = viewModel::loadSavedRoutine,
                                onDeleteRoutine = viewModel::deleteSavedRoutine,
                                onNavigateToEngine = { viewModel.selectTab(AppTab.ROUTINE_ENGINE) }
                            )
                        }
                    }
                }
            }
        }
    }
}
