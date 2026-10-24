package com.example.engine

import com.example.BuildConfig
import com.example.model.RoutineOutput
import com.example.model.RoutineSummary
import com.example.model.ScheduleBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.Locale
import java.util.concurrent.TimeUnit

// --- Gemini REST API Data Classes ---

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Float? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiScheduleApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateSchedule(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

data class EngineGenerationResult(
    val routineOutput: RoutineOutput,
    val strictJson: String,
    val usedCloudGemini: Boolean,
    val engineStatusNote: String
)

object ScheduleRoutineEngine {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val strictJsonFormatter = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val networkJson = Json {
        ignoreUnknownKeys = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val apiService: GeminiScheduleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiScheduleApiService::class.java)
    }

    private val SYSTEM_INSTRUCTION = """
        You are the Schedule & Routine Engine for an intelligent student desk-clock Android application.

        Your objective:
        1. Accept unstructured user input (available study hours, list of subjects/tasks, priority, break preferences, target exams, energy levels).
        2. Calculate and generate an optimized, realistic, sequential study timetable.
        3. Automatically schedule hydration checks, eye-rest cues, and subject-switching notifications.
        4. Always respond in valid, strictly formatted JSON adhering to the provided structure.

        Output Rules:
        - Allocate dedicated transition windows (3-5 minutes) between subjects with activity_type = "HYDRATE_SWITCH".
        - Insert a hydration reminder every 45-60 minutes (hydration_alert = true).
        - Maintain accurate military time (HH:mm) format based on the user's defined starting point, with strictly contiguous start_time and end_time values.
        - Keep notification announcements (tts_reminder_text) short, motivational, and natural for Text-to-Speech (TTS).
        - Ensure routine_summary accurately reflects the exact sum of STUDY minutes in total_study_minutes, the exact sum of BREAK + HYDRATE_SWITCH minutes in total_break_minutes, and the exact count of schedule items with hydration_alert = true in hydration_count.
    """.trimIndent()

    private fun buildRoutineResponseSchema(): JsonObject = buildJsonObject {
        put("type", "OBJECT")
        putJsonObject("properties") {
            putJsonObject("routine_summary") {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    putJsonObject("total_study_minutes") { put("type", "INTEGER") }
                    putJsonObject("total_break_minutes") { put("type", "INTEGER") }
                    putJsonObject("hydration_count") { put("type", "INTEGER") }
                }
                putJsonArray("required") {
                    add("total_study_minutes")
                    add("total_break_minutes")
                    add("hydration_count")
                }
            }
            putJsonObject("schedule") {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonObject("properties") {
                        putJsonObject("id") { put("type", "INTEGER") }
                        putJsonObject("start_time") { put("type", "STRING") }
                        putJsonObject("end_time") { put("type", "STRING") }
                        putJsonObject("activity_type") {
                            put("type", "STRING")
                            putJsonArray("enum") {
                                add("STUDY")
                                add("BREAK")
                                add("HYDRATE_SWITCH")
                            }
                        }
                        putJsonObject("subject") { put("type", "STRING") }
                        putJsonObject("tts_reminder_text") { put("type", "STRING") }
                        putJsonObject("hydration_alert") { put("type", "BOOLEAN") }
                        putJsonObject("color_hex") { put("type", "STRING") }
                    }
                    putJsonArray("required") {
                        add("id")
                        add("start_time")
                        add("end_time")
                        add("activity_type")
                        add("subject")
                        add("tts_reminder_text")
                        add("hydration_alert")
                        add("color_hex")
                    }
                }
            }
        }
        putJsonArray("required") {
            add("routine_summary")
            add("schedule")
        }
    }

    suspend fun generateOptimizedRoutine(
        unstructuredInput: String,
        preferredStartTime: String = "09:00",
        transitionMinutes: Int = 4,
        studyCycleMinutes: Int = 45
    ): EngineGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        val hasValidApiKey = apiKey.isNotEmpty() &&
            apiKey != "MY_GEMINI_API_KEY" &&
            !apiKey.startsWith("YOUR_")

        if (hasValidApiKey) {
            try {
                val prompt = buildString {
                    appendLine("User Unstructured Study Brief:")
                    appendLine(unstructuredInput)
                    appendLine()
                    appendLine("Constraints:")
                    appendLine("- Preferred Starting Military Time: $preferredStartTime")
                    appendLine("- Subject Transition Window: ${transitionMinutes.coerceIn(3, 5)} minutes (HYDRATE_SWITCH)")
                    appendLine("- Max Continuous Study Interval Before Hydration Check: ${studyCycleMinutes.coerceIn(45, 55)} minutes")
                }

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    systemInstruction = Content(parts = listOf(Part(text = SYSTEM_INSTRUCTION))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        responseSchema = buildRoutineResponseSchema(),
                        temperature = 0.3f
                    )
                )

                val response = apiService.generateSchedule(apiKey, request)
                val rawJson = response.candidates.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?.trim()
                    ?.removePrefix("```json")
                    ?.removePrefix("```")
                    ?.removeSuffix("```")
                    ?.trim()

                if (!rawJson.isNullOrBlank()) {
                    val parsed = strictJsonFormatter.decodeFromString<RoutineOutput>(rawJson)
                    val normalized = normalizeAndVerifyRoutine(parsed)
                    val formattedJson = strictJsonFormatter.encodeToString(normalized)
                    return@withContext EngineGenerationResult(
                        routineOutput = normalized,
                        strictJson = formattedJson,
                        usedCloudGemini = true,
                        engineStatusNote = "Generated via Gemini 3.5 Flash Schedule Engine"
                    )
                }
            } catch (e: Exception) {
                // Fall back seamlessly to local deterministic engine if network/key fails
            }
        }

        val fallbackRoutine = generateLocalDeterministicRoutine(
            unstructuredInput = unstructuredInput,
            preferredStartTime = preferredStartTime,
            transitionMinutes = transitionMinutes,
            studyCycleMinutes = studyCycleMinutes
        )
        val formattedJson = strictJsonFormatter.encodeToString(fallbackRoutine)
        EngineGenerationResult(
            routineOutput = fallbackRoutine,
            strictJson = formattedJson,
            usedCloudGemini = false,
            engineStatusNote = if (hasValidApiKey) {
                "Generated via Local Deterministic Engine (Offline Fallback)"
            } else {
                "Generated via Built-in Schedule & Routine Optimizer"
            }
        )
    }

    /**
     * Ensures summary totals and IDs are 100% mathematically accurate.
     */
    fun normalizeAndVerifyRoutine(input: RoutineOutput): RoutineOutput {
        val verifiedSchedule = input.schedule.mapIndexed { idx, block ->
            val cleanType = when (block.activityType.uppercase(Locale.US)) {
                "STUDY" -> "STUDY"
                "BREAK" -> "BREAK"
                "HYDRATE_SWITCH" -> "HYDRATE_SWITCH"
                else -> "STUDY"
            }
            val cleanColor = if (block.colorHex.startsWith("#") && (block.colorHex.length == 7 || block.colorHex.length == 9)) {
                block.colorHex.uppercase(Locale.US)
            } else {
                when (cleanType) {
                    "STUDY" -> "#3F51B5"
                    "HYDRATE_SWITCH" -> "#00ACC1"
                    else -> "#43A047"
                }
            }
            block.copy(
                id = idx + 1,
                activityType = cleanType,
                colorHex = cleanColor
            )
        }

        var studyMin = 0
        var breakMin = 0
        var hydrationCount = 0

        for (item in verifiedSchedule) {
            val dur = ScheduleBlock.calculateMinutesBetween(item.startTime, item.endTime)
            if (item.activityType == "STUDY") {
                studyMin += dur
            } else {
                breakMin += dur
            }
            if (item.hydrationAlert) {
                hydrationCount++
            }
        }

        return RoutineOutput(
            routineSummary = RoutineSummary(
                totalStudyMinutes = studyMin,
                totalBreakMinutes = breakMin,
                hydrationCount = hydrationCount
            ),
            schedule = verifiedSchedule
        )
    }

    /**
     * Parses unstructured user input (available hours, start/end times, subjects, priorities,
     * energy levels, exams) and builds an optimized sequential military-time timetable.
     */
    fun generateLocalDeterministicRoutine(
        unstructuredInput: String,
        preferredStartTime: String = "09:00",
        transitionMinutes: Int = 4,
        studyCycleMinutes: Int = 45
    ): RoutineOutput {
        val startMin = extractStartMilitaryMinutes(unstructuredInput, preferredStartTime)
        val totalAvailableMinutes = extractTotalAvailableMinutes(unstructuredInput, startMin)
        val subjects = extractSubjectsFromBrief(unstructuredInput)
        val safeTransition = transitionMinutes.coerceIn(3, 5)
        val safeStudyBlock = studyCycleMinutes.coerceIn(40, 50)

        val studyColors = listOf("#3F51B5", "#5C6BC0", "#7E57C2", "#1E88E5", "#3949AB")
        val hydrateColor = "#00ACC1"
        val breakColor = "#43A047"

        val blocks = mutableListOf<ScheduleBlock>()
        var cursorMin = startMin
        val targetEndMin = startMin + totalAvailableMinutes
        var blockId = 1
        var subjectIndex = 0
        var minutesSinceLastHydration = 0
        var consecutiveStudyBlocks = 0

        while (cursorMin < targetEndMin) {
            val remainingTotal = targetEndMin - cursorMin
            if (remainingTotal < 15) {
                break
            }

            val currentSubject = subjects[subjectIndex % subjects.size]
            val studyColor = studyColors[subjectIndex % studyColors.size]
            val studyDuration = minOf(safeStudyBlock, remainingTotal)

            val blockStartStr = ScheduleBlock.formatMilitaryMinutes(cursorMin)
            val blockEndStr = ScheduleBlock.formatMilitaryMinutes(cursorMin + studyDuration)

            // Trigger hydration alert on study start if >= 45 min elapsed or first focus lock
            val needsHydrationOnStudy = minutesSinceLastHydration >= 45
            val studyTts = buildStudyTtsMessage(
                subject = currentSubject,
                durationMin = studyDuration,
                isFirstBlock = (blockId == 1),
                includeHydrationCue = needsHydrationOnStudy
            )

            blocks.add(
                ScheduleBlock(
                    id = blockId++,
                    startTime = blockStartStr,
                    endTime = blockEndStr,
                    activityType = "STUDY",
                    subject = currentSubject,
                    tts_reminder_text_internal = studyTts,
                    hydrationAlert = needsHydrationOnStudy,
                    colorHex = studyColor
                )
            )

            cursorMin += studyDuration
            minutesSinceLastHydration = if (needsHydrationOnStudy) studyDuration else (minutesSinceLastHydration + studyDuration)
            consecutiveStudyBlocks++

            val nextSubject = subjects[(subjectIndex + 1) % subjects.size]
            val remainingAfterStudy = targetEndMin - cursorMin

            if (remainingAfterStudy <= 8) {
                break
            }

            // Decide between a dedicated HYDRATE_SWITCH (3-5 min) between subjects
            // or a restorative BREAK (10-12 min) + eye-rest cue after 2 study blocks
            if (consecutiveStudyBlocks >= 2 && remainingAfterStudy >= 28) {
                // First insert a 4-min HYDRATE_SWITCH or a 10-min restorative BREAK with hydration + 20-20-20 eye rest
                val breakDuration = 10.coerceAtMost(remainingAfterStudy - 15)
                val bStart = ScheduleBlock.formatMilitaryMinutes(cursorMin)
                val bEnd = ScheduleBlock.formatMilitaryMinutes(cursorMin + breakDuration)
                blocks.add(
                    ScheduleBlock(
                        id = blockId++,
                        startTime = bStart,
                        endTime = bEnd,
                        activityType = "BREAK",
                        subject = "Eye-Rest & Recharge Break",
                        tts_reminder_text_internal = "Great focus session. Take a $breakDuration minute break, drink water, and look 20 feet away for 20 seconds to rest your eyes.",
                        hydrationAlert = true,
                        colorHex = breakColor
                    )
                )
                cursorMin += breakDuration
                minutesSinceLastHydration = 0
                consecutiveStudyBlocks = 0

                // Also add a 3-5 min HYDRATE_SWITCH transition window if switching subjects and time permits
                if (targetEndMin - cursorMin >= 25 && subjects.size > 1) {
                    val switchDur = safeTransition
                    val sStart = ScheduleBlock.formatMilitaryMinutes(cursorMin)
                    val sEnd = ScheduleBlock.formatMilitaryMinutes(cursorMin + switchDur)
                    blocks.add(
                        ScheduleBlock(
                            id = blockId++,
                            startTime = sStart,
                            endTime = sEnd,
                            activityType = "HYDRATE_SWITCH",
                            subject = "Switch to $nextSubject",
                            tts_reminder_text_internal = "Take $switchDur minutes to clear your desk and set up your materials for $nextSubject.",
                            hydrationAlert = false,
                            colorHex = hydrateColor
                        )
                    )
                    cursorMin += switchDur
                }
            } else {
                // Dedicated 3-5 minute HYDRATE_SWITCH transition window between subjects
                val switchDur = safeTransition.coerceAtMost(remainingAfterStudy)
                val sStart = ScheduleBlock.formatMilitaryMinutes(cursorMin)
                val sEnd = ScheduleBlock.formatMilitaryMinutes(cursorMin + switchDur)
                blocks.add(
                    ScheduleBlock(
                        id = blockId++,
                        startTime = sStart,
                        endTime = sEnd,
                        activityType = "HYDRATE_SWITCH",
                        subject = "Hydrate & Switch to $nextSubject",
                        tts_reminder_text_internal = "Time to hydrate and switch gears. Drink a glass of water, rest your eyes for 20 seconds, and prepare for $nextSubject.",
                        hydrationAlert = true,
                        colorHex = hydrateColor
                    )
                )
                cursorMin += switchDur
                minutesSinceLastHydration = 0
            }

            subjectIndex++
        }

        return normalizeAndVerifyRoutine(
            RoutineOutput(
                routineSummary = RoutineSummary(0, 0, 0),
                schedule = blocks
            )
        )
    }

    @Suppress("FunctionName")
    private fun ScheduleBlock(
        id: Int,
        startTime: String,
        endTime: String,
        activityType: String,
        subject: String,
        tts_reminder_text_internal: String,
        hydrationAlert: Boolean,
        colorHex: String
    ): ScheduleBlock = com.example.model.ScheduleBlock(
        id = id,
        startTime = startTime,
        endTime = endTime,
        activityType = activityType,
        subject = subject,
        ttsReminderText = tts_reminder_text_internal,
        hydrationAlert = hydrationAlert,
        colorHex = colorHex
    )

    private fun buildStudyTtsMessage(
        subject: String,
        durationMin: Int,
        isFirstBlock: Boolean,
        includeHydrationCue: Boolean
    ): String {
        return when {
            isFirstBlock -> "Starting your study routine with $subject for $durationMin minutes. Keep water nearby and lock in your focus."
            includeHydrationCue -> "Starting $subject for $durationMin minutes. Take a quick sip of water first, keep your posture tall, and dive in."
            else -> "Now focusing on $subject for the next $durationMin minutes. You are making steady progress—stay sharp."
        }
    }

    private fun extractStartMilitaryMinutes(text: String, fallbackStartTime: String): Int {
        // Check explicit HH:mm pattern in text
        val regex24 = Regex("""\b([01]?\d|2[0-3]):([0-5]\d)\b""")
        val firstMatch = regex24.find(text)
        if (firstMatch != null) {
            val h = firstMatch.groupValues[1].toIntOrNull() ?: 9
            val m = firstMatch.groupValues[2].toIntOrNull() ?: 0
            return h * 60 + m
        }
        return ScheduleBlock.parseMilitaryMinutes(fallbackStartTime) ?: (9 * 60)
    }

    private fun extractTotalAvailableMinutes(text: String, startMin: Int): Int {
        // Check range like "09:00 to 13:00" or "14:00 - 17:00"
        val rangeRegex = Regex("""\b([01]?\d|2[0-3]):([0-5]\d)\s*(?:to|-|–|until)\s*([01]?\d|2[0-3]):([0-5]\d)\b""", RegexOption.IGNORE_CASE)
        val rangeMatch = rangeRegex.find(text)
        if (rangeMatch != null) {
            val sH = rangeMatch.groupValues[1].toIntOrNull() ?: 9
            val sM = rangeMatch.groupValues[2].toIntOrNull() ?: 0
            val eH = rangeMatch.groupValues[3].toIntOrNull() ?: 12
            val eM = rangeMatch.groupValues[4].toIntOrNull() ?: 0
            val sTotal = sH * 60 + sM
            val eTotal = eH * 60 + eM
            val diff = if (eTotal > sTotal) eTotal - sTotal else (eTotal + 24 * 60 - sTotal)
            if (diff in 45..600) return diff
        }

        // Check explicit minutes like "150 minutes" or "180 mins"
        val minRegex = Regex("""(\d{2,3})\s*(?:minutes|minute|mins|min)\b""", RegexOption.IGNORE_CASE)
        for (match in minRegex.findAll(text)) {
            val v = match.groupValues[1].toIntOrNull() ?: continue
            if (v in 60..600) return v
        }

        // Check explicit hours like "3.5 hours" or "4 hrs"
        val hrRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:hours|hour|hrs|hr)\b""", RegexOption.IGNORE_CASE)
        val hrMatch = hrRegex.find(text)
        if (hrMatch != null) {
            val hrs = hrMatch.groupValues[1].toDoubleOrNull()
            if (hrs != null && hrs in 1.0..10.0) {
                return (hrs * 60).toInt()
            }
        }

        // Default to a realistic 3-hour (180-minute) study timetable
        return 180
    }

    private fun extractSubjectsFromBrief(text: String): List<String> {
        val knownCandidates = mutableListOf<String>()

        // Look for numbered or labeled subjects or phrases after colons/commas
        val cleanedLines = text
            .replace(Regex("""\b([01]?\d|2[0-3]):([0-5]\d)\b"""), "")
            .split(Regex("""[.;\n]"""))

        val stopPrefixes = listOf(
            "start", "available", "starting", "prefer", "energy", "moderate",
            "need", "include", "high energy", "morning", "afternoon", "evening"
        )

        for (segment in cleanedLines) {
            val trimmed = segment.trim()
            if (trimmed.isEmpty()) continue

            // Check if segment has "Subjects:" or "Tasks:" or "High priority:"
            val afterColon = if (trimmed.contains(":")) {
                trimmed.substringAfter(":").trim()
            } else {
                trimmed
            }

            val parts = afterColon.split(Regex("""(?:,|and\b|\d+\))""", RegexOption.IGNORE_CASE))
            for (rawPart in parts) {
                val candidate = rawPart
                    .replace(Regex("""\([^)]*\)"""), "")
                    .replace(Regex("""\b(?:high|medium|low)\s+priority\b""", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("""\b(?:subjects|tasks|study|need to)\b""", RegexOption.IGNORE_CASE), "")
                    .trim(' ', '-', ':', ',', '.')

                val lower = candidate.lowercase(Locale.US)
                if (candidate.length in 4..42 &&
                    stopPrefixes.none { lower.startsWith(it) } &&
                    !lower.contains("min ") &&
                    !lower.contains("minute") &&
                    !lower.contains("hour") &&
                    !lower.contains("eye rest") &&
                    !lower.contains("hydration")
                ) {
                    val capitalized = candidate.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
                    }
                    if (knownCandidates.none { it.equals(capitalized, ignoreCase = true) }) {
                        knownCandidates.add(capitalized)
                    }
                }
            }
        }

        return if (knownCandidates.isNotEmpty()) {
            knownCandidates.take(5)
        } else {
            listOf(
                "Core Exam Priority Concept Review",
                "Problem Set & Active Recall Drills",
                "Synthesis Notes & Practice Quiz"
            )
        }
    }
}
