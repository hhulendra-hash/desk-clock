package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChronoStudyDatabase
import com.example.data.RoutineRepository
import com.example.data.SavedRoutineEntity
import com.example.engine.ScheduleRoutineEngine
import com.example.model.RoutineOutput
import com.example.model.SamplePresets
import com.example.model.ScheduleBlock
import com.example.model.StudyScenarioPreset
import com.example.tts.DeskClockTtsManager
import com.example.tts.TtsUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab {
    DESK_CLOCK,
    ROUTINE_ENGINE,
    JSON_SCHEMA,
    SAVED_ROUTINES
}

data class DeskClockUiState(
    val selectedTab: AppTab = AppTab.DESK_CLOCK,
    val currentMilitaryTime: String = "09:00:00",
    val currentMilitaryHHmm: String = "09:00",
    val currentDateLabel: String = "SAT • OCT 24",
    val unstructuredInput: String = SamplePresets.presets.first().unstructuredInput,
    val preferredStartTime: String = SamplePresets.presets.first().startTime,
    val transitionMinutes: Int = 4,
    val studyCycleMinutes: Int = 45,
    val routineTitle: String = SamplePresets.presets.first().title,
    val activeRoutine: RoutineOutput,
    val strictJsonOutput: String,
    val editableJsonText: String,
    val jsonValidationMessage: String = "Valid • Strictly adheres to Schedule & Routine Engine JSON Schema",
    val isJsonValid: Boolean = true,
    val activeBlockIndex: Int = 0,
    val remainingBlockSeconds: Int = 45 * 60,
    val totalBlockSeconds: Int = 45 * 60,
    val isTimerRunning: Boolean = false,
    val isGenerating: Boolean = false,
    val engineSourceNote: String = "Built-in Schedule & Routine Engine Ready",
    val hydrationGlassesLogged: Int = 0,
    val eyeRestCountdownSeconds: Int = 0,
    val statusBannerMessage: String? = null
) {
    val activeBlock: ScheduleBlock?
        get() = activeRoutine.schedule.getOrNull(activeBlockIndex)

    val blockProgressFraction: Float
        get() {
            if (totalBlockSeconds <= 0) return 0f
            val elapsed = (totalBlockSeconds - remainingBlockSeconds).coerceAtLeast(0)
            return (elapsed.toFloat() / totalBlockSeconds.toFloat()).coerceIn(0f, 1f)
        }
}

class DeskClockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RoutineRepository = RoutineRepository(
        ChronoStudyDatabase.getInstance(application).savedRoutineDao()
    )

    val ttsManager = DeskClockTtsManager(application)
    val ttsState: StateFlow<TtsUiState> = ttsManager.state

    val savedRoutines: StateFlow<List<SavedRoutineEntity>> = repository.allSavedRoutines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val initialRoutine: RoutineOutput = ScheduleRoutineEngine.generateLocalDeterministicRoutine(
        unstructuredInput = SamplePresets.presets.first().unstructuredInput,
        preferredStartTime = SamplePresets.presets.first().startTime,
        transitionMinutes = 4,
        studyCycleMinutes = 45
    )

    private val initialJson: String =
        ScheduleRoutineEngine.strictJsonFormatter.encodeToString(initialRoutine)

    private val firstBlockDurationSec =
        (initialRoutine.schedule.firstOrNull()?.durationMinutes ?: 45) * 60

    private val _uiState = MutableStateFlow(
        DeskClockUiState(
            activeRoutine = initialRoutine,
            strictJsonOutput = initialJson,
            editableJsonText = initialJson,
            remainingBlockSeconds = firstBlockDurationSec,
            totalBlockSeconds = firstBlockDurationSec
        )
    )
    val uiState: StateFlow<DeskClockUiState> = _uiState.asStateFlow()

    private val timeFormat24 = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val hhMmFormat24 = SimpleDateFormat("HH:mm", Locale.US)
    private val dateFormat = SimpleDateFormat("EEE • MMM d, yyyy", Locale.US)

    init {
        updateClockStrings()
        startClockAndTimerLoop()
    }

    private fun updateClockStrings() {
        val now = Date()
        _uiState.update { state ->
            state.copy(
                currentMilitaryTime = timeFormat24.format(now),
                currentMilitaryHHmm = hhMmFormat24.format(now),
                currentDateLabel = dateFormat.format(now).uppercase(Locale.US)
            )
        }
    }

    private fun startClockAndTimerLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val now = Date()
                val currentState = _uiState.value

                // Update 20-20-20 eye rest micro-timer if active
                val nextEyeRest = if (currentState.eyeRestCountdownSeconds > 0) {
                    val updated = currentState.eyeRestCountdownSeconds - 1
                    if (updated == 0) {
                        ttsManager.speakAnnouncement("20 second eye rest complete. Your eyes are refreshed!")
                    }
                    updated
                } else {
                    0
                }

                if (currentState.isTimerRunning && currentState.remainingBlockSeconds > 0) {
                    val nextRem = currentState.remainingBlockSeconds - 1
                    if (nextRem == 0) {
                        advanceToNextBlockAutomatically()
                    } else {
                        _uiState.update {
                            it.copy(
                                currentMilitaryTime = timeFormat24.format(now),
                                currentMilitaryHHmm = hhMmFormat24.format(now),
                                currentDateLabel = dateFormat.format(now).uppercase(Locale.US),
                                remainingBlockSeconds = nextRem,
                                eyeRestCountdownSeconds = nextEyeRest
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            currentMilitaryTime = timeFormat24.format(now),
                            currentMilitaryHHmm = hhMmFormat24.format(now),
                            currentDateLabel = dateFormat.format(now).uppercase(Locale.US),
                            eyeRestCountdownSeconds = nextEyeRest
                        )
                    }
                }
            }
        }
    }

    private fun advanceToNextBlockAutomatically() {
        val state = _uiState.value
        val schedule = state.activeRoutine.schedule
        if (schedule.isEmpty()) return

        if (state.activeBlockIndex + 1 < schedule.size) {
            val nextIdx = state.activeBlockIndex + 1
            val nextBlock = schedule[nextIdx]
            val durSec = nextBlock.durationMinutes * 60
            _uiState.update {
                it.copy(
                    activeBlockIndex = nextIdx,
                    remainingBlockSeconds = durSec,
                    totalBlockSeconds = durSec,
                    isTimerRunning = true,
                    statusBannerMessage = "Switched to Block #${nextBlock.id}: ${nextBlock.subject}"
                )
            }
            ttsManager.speakAnnouncement(nextBlock.ttsReminderText)
        } else {
            _uiState.update {
                it.copy(
                    isTimerRunning = false,
                    remainingBlockSeconds = 0,
                    statusBannerMessage = "Routine complete! All scheduled blocks finished."
                )
            }
            ttsManager.speakAnnouncement("Congratulations! You have completed your entire study routine.")
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab, statusBannerMessage = null) }
    }

    fun updateUnstructuredInput(text: String) {
        _uiState.update { it.copy(unstructuredInput = text) }
    }

    fun updatePreferredStartTime(hhMm: String) {
        _uiState.update { it.copy(preferredStartTime = hhMm) }
    }

    fun updateRoutineTitle(title: String) {
        _uiState.update { it.copy(routineTitle = title) }
    }

    fun updateTransitionMinutes(minutes: Int) {
        _uiState.update { it.copy(transitionMinutes = minutes.coerceIn(3, 5)) }
    }

    fun updateStudyCycleMinutes(minutes: Int) {
        _uiState.update { it.copy(studyCycleMinutes = minutes.coerceIn(40, 55)) }
    }

    fun syncStartTimeToCurrentClock() {
        val currentHHmm = _uiState.value.currentMilitaryHHmm
        _uiState.update {
            it.copy(
                preferredStartTime = currentHHmm,
                statusBannerMessage = "Start time set to current military time ($currentHHmm)"
            )
        }
    }

    fun applyScenarioPreset(preset: StudyScenarioPreset) {
        _uiState.update {
            it.copy(
                routineTitle = preset.title,
                preferredStartTime = preset.startTime,
                unstructuredInput = preset.unstructuredInput
            )
        }
        generateRoutineSchedule(switchToClockAfter = false)
    }

    fun generateRoutineSchedule(switchToClockAfter: Boolean = false) {
        val snapshot = _uiState.value
        if (snapshot.unstructuredInput.isBlank()) {
            _uiState.update {
                it.copy(statusBannerMessage = "Please enter your study goals or pick a preset first.")
            }
            return
        }

        _uiState.update { it.copy(isGenerating = true, statusBannerMessage = null) }

        viewModelScope.launch {
            val result = ScheduleRoutineEngine.generateOptimizedRoutine(
                unstructuredInput = snapshot.unstructuredInput,
                preferredStartTime = snapshot.preferredStartTime,
                transitionMinutes = snapshot.transitionMinutes,
                studyCycleMinutes = snapshot.studyCycleMinutes
            )

            val firstDurSec = (result.routineOutput.schedule.firstOrNull()?.durationMinutes ?: 45) * 60

            _uiState.update { state ->
                state.copy(
                    isGenerating = false,
                    activeRoutine = result.routineOutput,
                    strictJsonOutput = result.strictJson,
                    editableJsonText = result.strictJson,
                    isJsonValid = true,
                    jsonValidationMessage = "Valid • ${result.routineOutput.schedule.size} blocks • ${result.engineStatusNote}",
                    activeBlockIndex = 0,
                    remainingBlockSeconds = firstDurSec,
                    totalBlockSeconds = firstDurSec,
                    isTimerRunning = false,
                    engineSourceNote = result.engineStatusNote,
                    selectedTab = if (switchToClockAfter) AppTab.DESK_CLOCK else state.selectedTab,
                    statusBannerMessage = "Generated ${result.routineOutput.schedule.size}-block sequential schedule (${result.routineOutput.routineSummary.totalStudyMinutes}m study, ${result.routineOutput.routineSummary.hydrationCount} hydration checks)."
                )
            }

            result.routineOutput.schedule.firstOrNull()?.let { firstBlock ->
                ttsManager.speakAnnouncement(firstBlock.ttsReminderText)
            }
        }
    }

    fun selectScheduleBlock(index: Int, autoStartTimer: Boolean = false, speakTts: Boolean = true) {
        val schedule = _uiState.value.activeRoutine.schedule
        if (index !in schedule.indices) return
        val block = schedule[index]
        val durSec = block.durationMinutes * 60

        _uiState.update {
            it.copy(
                activeBlockIndex = index,
                remainingBlockSeconds = durSec,
                totalBlockSeconds = durSec,
                isTimerRunning = autoStartTimer,
                statusBannerMessage = "Loaded Block #${block.id} (${block.startTime}–${block.endTime}): ${block.subject}"
            )
        }
        if (speakTts) {
            ttsManager.speakAnnouncement(block.ttsReminderText)
        }
    }

    fun toggleTimerPlayPause() {
        val current = _uiState.value
        val nextRunning = !current.isTimerRunning
        _uiState.update { it.copy(isTimerRunning = nextRunning) }
        if (nextRunning) {
            current.activeBlock?.let { block ->
                if (current.remainingBlockSeconds == current.totalBlockSeconds) {
                    ttsManager.speakAnnouncement(block.ttsReminderText)
                }
            }
        }
    }

    fun resetCurrentBlockTimer() {
        val block = _uiState.value.activeBlock ?: return
        val durSec = block.durationMinutes * 60
        _uiState.update {
            it.copy(
                remainingBlockSeconds = durSec,
                totalBlockSeconds = durSec,
                isTimerRunning = false,
                statusBannerMessage = "Timer reset to ${block.durationMinutes}:00"
            )
        }
    }

    fun nextBlock() {
        val nextIdx = _uiState.value.activeBlockIndex + 1
        if (nextIdx < _uiState.value.activeRoutine.schedule.size) {
            selectScheduleBlock(nextIdx, autoStartTimer = _uiState.value.isTimerRunning, speakTts = true)
        }
    }

    fun previousBlock() {
        val prevIdx = _uiState.value.activeBlockIndex - 1
        if (prevIdx >= 0) {
            selectScheduleBlock(prevIdx, autoStartTimer = _uiState.value.isTimerRunning, speakTts = true)
        }
    }

    fun speakActiveBlockReminder() {
        val block = _uiState.value.activeBlock ?: return
        ttsManager.speakAnnouncement(block.ttsReminderText, forceSpeakEvenIfMuted = true)
        _uiState.update {
            it.copy(statusBannerMessage = "Speaking TTS: \"${block.ttsReminderText}\"")
        }
    }

    fun speakSpecificBlockReminder(block: ScheduleBlock) {
        ttsManager.speakAnnouncement(block.ttsReminderText, forceSpeakEvenIfMuted = true)
        _uiState.update {
            it.copy(statusBannerMessage = "Speaking Block #${block.id} TTS: \"${block.ttsReminderText}\"")
        }
    }

    fun logWaterIntake() {
        val nextCount = _uiState.value.hydrationGlassesLogged + 1
        _uiState.update {
            it.copy(
                hydrationGlassesLogged = nextCount,
                statusBannerMessage = "Hydration logged! ($nextCount glasses today)"
            )
        }
        ttsManager.speakAnnouncement("Hydration logged. Great job staying hydrated while studying!")
    }

    fun trigger202020EyeRestCue() {
        _uiState.update {
            it.copy(
                eyeRestCountdownSeconds = 20,
                statusBannerMessage = "20-20-20 Eye-Rest Active: Look 20 feet away for 20 seconds."
            )
        }
        ttsManager.speakAnnouncement("Starting 20-20-20 eye rest. Look at an object 20 feet away for the next 20 seconds.")
    }

    fun updateEditableJsonText(newJson: String) {
        _uiState.update { it.copy(editableJsonText = newJson) }
    }

    fun validateAndApplyEditedJson() {
        val raw = _uiState.value.editableJsonText.trim()
        try {
            val parsed = ScheduleRoutineEngine.strictJsonFormatter.decodeFromString<RoutineOutput>(raw)
            val normalized = ScheduleRoutineEngine.normalizeAndVerifyRoutine(parsed)
            val pretty = ScheduleRoutineEngine.strictJsonFormatter.encodeToString(normalized)
            val firstDurSec = (normalized.schedule.firstOrNull()?.durationMinutes ?: 25) * 60

            _uiState.update {
                it.copy(
                    activeRoutine = normalized,
                    strictJsonOutput = pretty,
                    editableJsonText = pretty,
                    isJsonValid = true,
                    jsonValidationMessage = "Valid JSON • ${normalized.schedule.size} blocks verified & loaded into Desk Clock",
                    activeBlockIndex = 0,
                    remainingBlockSeconds = firstDurSec,
                    totalBlockSeconds = firstDurSec,
                    isTimerRunning = false,
                    statusBannerMessage = "Custom JSON validated and applied to Desk Clock!"
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isJsonValid = false,
                    jsonValidationMessage = "JSON Schema Error: ${e.message?.take(120) ?: "Invalid format"}"
                )
            }
        }
    }

    fun resetEditableJsonToCurrentRoutine() {
        _uiState.update {
            it.copy(
                editableJsonText = it.strictJsonOutput,
                isJsonValid = true,
                jsonValidationMessage = "Restored to active Routine JSON"
            )
        }
    }

    fun saveCurrentRoutineToDatabase() {
        val current = _uiState.value
        val titleToSave = current.routineTitle.ifBlank {
            "Study Routine (${current.preferredStartTime})"
        }
        viewModelScope.launch {
            val entity = SavedRoutineEntity(
                title = titleToSave,
                unstructuredPrompt = current.unstructuredInput,
                strictJsonPayload = current.strictJsonOutput,
                totalStudyMinutes = current.activeRoutine.routineSummary.totalStudyMinutes,
                totalBreakMinutes = current.activeRoutine.routineSummary.totalBreakMinutes,
                hydrationCount = current.activeRoutine.routineSummary.hydrationCount,
                startTime = current.activeRoutine.schedule.firstOrNull()?.startTime ?: current.preferredStartTime
            )
            repository.saveRoutine(entity)
            _uiState.update {
                it.copy(statusBannerMessage = "Saved \"$titleToSave\" toSaved Routines.")
            }
        }
    }

    fun loadSavedRoutine(entity: SavedRoutineEntity) {
        try {
            val parsed = ScheduleRoutineEngine.strictJsonFormatter.decodeFromString<RoutineOutput>(entity.strictJsonPayload)
            val normalized = ScheduleRoutineEngine.normalizeAndVerifyRoutine(parsed)
            val pretty = ScheduleRoutineEngine.strictJsonFormatter.encodeToString(normalized)
            val firstDurSec = (normalized.schedule.firstOrNull()?.durationMinutes ?: 45) * 60

            _uiState.update {
                it.copy(
                    routineTitle = entity.title,
                    unstructuredInput = entity.unstructuredPrompt,
                    preferredStartTime = entity.startTime,
                    activeRoutine = normalized,
                    strictJsonOutput = pretty,
                    editableJsonText = pretty,
                    activeBlockIndex = 0,
                    remainingBlockSeconds = firstDurSec,
                    totalBlockSeconds = firstDurSec,
                    isTimerRunning = false,
                    selectedTab = AppTab.DESK_CLOCK,
                    statusBannerMessage = "Loaded saved routine \"${entity.title}\" into Desk Clock."
                )
            }
            normalized.schedule.firstOrNull()?.let { first ->
                ttsManager.speakAnnouncement(first.ttsReminderText)
            }
        } catch (_: Exception) {
            _uiState.update {
                it.copy(statusBannerMessage = "Could not parse saved routine JSON.")
            }
        }
    }

    fun deleteSavedRoutine(id: Int) {
        viewModelScope.launch {
            repository.deleteRoutine(id)
            _uiState.update {
                it.copy(statusBannerMessage = "Removed routine from Saved list.")
            }
        }
    }

    fun dismissBannerMessage() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
