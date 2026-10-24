package com.example.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Exact JSON Output Schema required by the Schedule & Routine Engine specification.
 */
@Serializable
data class RoutineOutput(
    @SerialName("routine_summary")
    val routineSummary: RoutineSummary,
    @SerialName("schedule")
    val schedule: List<ScheduleBlock>
)

@Serializable
data class RoutineSummary(
    @SerialName("total_study_minutes")
    val totalStudyMinutes: Int,
    @SerialName("total_break_minutes")
    val totalBreakMinutes: Int,
    @SerialName("hydration_count")
    val hydrationCount: Int
)

@Serializable
data class ScheduleBlock(
    @SerialName("id")
    val id: Int,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("end_time")
    val endTime: String,
    @SerialName("activity_type")
    val activityType: String, // "STUDY" | "BREAK" | "HYDRATE_SWITCH"
    @SerialName("subject")
    val subject: String,
    @SerialName("tts_reminder_text")
    val ttsReminderText: String,
    @SerialName("hydration_alert")
    val hydrationAlert: Boolean,
    @SerialName("color_hex")
    val colorHex: String
) {
    val durationMinutes: Int
        get() = calculateMinutesBetween(startTime, endTime).coerceAtLeast(1)

    fun parsedColor(): Color {
        return try {
            val clean = colorHex.trim().removePrefix("#")
            val longVal = clean.toLong(16)
            if (clean.length == 6) {
                Color(0xFF000000 or longVal)
            } else if (clean.length == 8) {
                Color(longVal)
            } else {
                defaultColorForType(activityType)
            }
        } catch (_: Exception) {
            defaultColorForType(activityType)
        }
    }

    companion object {
        fun defaultColorForType(type: String): Color = when (type.uppercase()) {
            "STUDY" -> Color(0xFF3F51B5)
            "HYDRATE_SWITCH" -> Color(0xFF00ACC1)
            "BREAK" -> Color(0xFF43A047)
            else -> Color(0xFF6366F1)
        }

        fun calculateMinutesBetween(startHHmm: String, endHHmm: String): Int {
            val startMin = parseMilitaryMinutes(startHHmm) ?: return 25
            val endMin = parseMilitaryMinutes(endHHmm) ?: return 25
            val diff = endMin - startMin
            return if (diff >= 0) diff else (diff + 24 * 60)
        }

        fun parseMilitaryMinutes(hhMm: String): Int? {
            val parts = hhMm.trim().split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            if (h !in 0..23 || m !in 0..59) return null
            return h * 60 + m
        }

        fun formatMilitaryMinutes(totalMinutes: Int): String {
            val normalized = ((totalMinutes % (24 * 60)) + (24 * 60)) % (24 * 60)
            val h = normalized / 60
            val m = normalized % 60
            return String.format(java.util.Locale.US, "%02d:%02d", h, m)
        }
    }
}

data class StudyScenarioPreset(
    val id: String,
    val title: String,
    val badge: String,
    val startTime: String,
    val unstructuredInput: String
)

object SamplePresets {
    val presets = listOf(
        StudyScenarioPreset(
            id = "stem_morning",
            title = "Morning STEM Exam Sprint",
            badge = "09:00 • 3.5 hrs",
            startTime = "09:00",
            unstructuredInput = "Start at 09:00 for 3.5 hours. High priority: Organic Chemistry Reactions (Exam tomorrow, peak morning energy). Medium priority: Linear Algebra Eigenvalues problem set. Low priority: Physics Lab Report review. Prefer 45-50 min study blocks with 10 min restorative eye-rest breaks and 4 min transition/hydration windows."
        ),
        StudyScenarioPreset(
            id = "cs_deep_work",
            title = "Algorithms & Systems Deep Work",
            badge = "14:00 • 3 hrs",
            startTime = "14:00",
            unstructuredInput = "Available from 14:00 to 17:00. Subjects: Dynamic Programming & Graph Algorithms (High Priority, coding interview prep), Operating Systems Virtual Memory notes (Medium Priority), and Database SQL indexing drills (Medium Priority). Energy is steady early afternoon then dips at 16:00. Need hydration every 45 mins and 5 min subject switches."
        ),
        StudyScenarioPreset(
            id = "night_owl_finals",
            title = "Evening Finals Review Session",
            badge = "19:00 • 2.5 hrs",
            startTime = "19:00",
            unstructuredInput = "Starting 19:00, 150 minutes available. Tasks: Macroeconomics Monetary Policy essay outline (High Priority), Cognitive Psychology flashcards (Medium Priority), Ethics reading summary (Low Priority). Moderate evening energy, eye strain risk—include 20-20-20 eye rest cues and 4 min hydration switches."
        ),
        StudyScenarioPreset(
            id = "med_intensive",
            title = "Pre-Med Anatomy & Patho Block",
            badge = "08:00 • 4 hrs",
            startTime = "08:00",
            unstructuredInput = "08:00 to 12:00 intensive block. 1) Neuroanatomy Cranial Nerves (High Priority, shelf exam Friday), 2) Cardiovascular Pharmacology mechanisms (High Priority), 3) Biochemistry Metabolic Pathways diagramming (Medium Priority). High energy at 08:00, need 50-min study intervals, 4-min hydrate switches, and 10-min eye-rest breaks."
        )
    )
}
