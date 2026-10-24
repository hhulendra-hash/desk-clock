package com.example

import com.example.engine.ScheduleRoutineEngine
import com.example.model.RoutineOutput
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun scheduleRoutineEngine_generatesValidSequentialMilitaryScheduleAndStrictJson() {
        val unstructured = "Start at 09:00 for 3 hours. Subjects: Organic Chemistry (High priority), Linear Algebra, and Physics Lab. Include 4 min subject switches and hydration alerts."
        val output = ScheduleRoutineEngine.generateLocalDeterministicRoutine(
            unstructuredInput = unstructured,
            preferredStartTime = "09:00",
            transitionMinutes = 4,
            studyCycleMinutes = 45
        )

        assertTrue(output.schedule.isNotEmpty())
        assertEquals("09:00", output.schedule.first().startTime)
        assertTrue(output.routineSummary.totalStudyMinutes > 0)
        assertTrue(output.routineSummary.totalBreakMinutes > 0)
        assertTrue(output.routineSummary.hydrationCount > 0)

        // Verify contiguous military times and valid activity types
        for (i in 0 until output.schedule.size) {
            val item = output.schedule[i]
            assertEquals(i + 1, item.id)
            assertTrue(item.activityType in setOf("STUDY", "BREAK", "HYDRATE_SWITCH"))
            assertTrue(item.ttsReminderText.isNotBlank())
            assertTrue(item.colorHex.startsWith("#"))
            if (i > 0) {
                assertEquals(output.schedule[i - 1].endTime, item.startTime)
            }
        }

        // Verify strict JSON roundtrip
        val jsonString = ScheduleRoutineEngine.strictJsonFormatter.encodeToString(output)
        assertTrue(jsonString.contains("\"routine_summary\""))
        assertTrue(jsonString.contains("\"schedule\""))
        val decoded = ScheduleRoutineEngine.strictJsonFormatter.decodeFromString<RoutineOutput>(jsonString)
        assertEquals(output, decoded)
    }
}
