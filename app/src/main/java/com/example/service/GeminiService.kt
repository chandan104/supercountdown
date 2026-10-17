package com.example.service

import com.example.BuildConfig
import com.example.data.model.GoalItem
import com.example.data.model.HabitItem
import com.example.data.model.TaskItem
import com.example.domain.PaceCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiDailyPlan(
    val summary: String,
    val top3Priorities: List<String>,
    val risk: String,
    val nextAction: String,
    val isAiGenerated: Boolean
) {
    // Backward compatibility helpers
    val topPriorities: List<String> get() = top3Priorities
    val obstacleAdvice: String get() = risk
    val scheduleAdvice: String get() = nextAction
    val inspirationalQuote: String get() = summary
}

data class GoalBreakdownResult(
    val suggestedMilestones: List<String>,
    val suggestedDailyTasks: List<String>,
    val successStrategy: String
)

object GeminiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateDailyPlan(
        goals: List<GoalItem>,
        tasks: List<TaskItem>,
        habits: List<HabitItem>,
        daysTo2027: Long,
        allowOnlineAi: Boolean = true
    ): GeminiDailyPlan = withContext(Dispatchers.IO) {
        // If user disabled sending data to AI or key not set, use rule-based offline plan
        if (!allowOnlineAi || !isApiKeyConfigured()) {
            return@withContext fallbackDailyPlan(goals, tasks, habits, daysTo2027)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY

        // 1. Compute PaceEngine metrics deterministically (Gemini NEVER calculates pace)
        val goalsPaceArray = JSONArray()
        for (goal in goals) {
            val pace = PaceCalculator.calculate(goal)
            val gObj = JSONObject().apply {
                put("title", goal.title)
                put("category", goal.category)
                put("target", goal.targetValue)
                put("current", goal.currentValue)
                put("unit", goal.measurementUnit)
                put("status", pace.status.name)
                put("requiredPerDay", String.format("%.2f", pace.requiredPerDay))
                put("actualPerDay", String.format("%.2f", pace.actualPerDay))
                put("projectedFinish", String.format("%.1f", pace.projectedFinish))
                put("daysRemaining", pace.daysRemaining)
            }
            goalsPaceArray.put(gObj)
        }

        val tasksSummary = tasks.joinToString("; ") { "${it.title} [${it.status}, priority ${it.priority}]" }
        val habitsSummary = habits.joinToString("; ") { "${it.title} (${it.currentStreak}d streak)" }

        val prompt = """
            You are the executive productivity coach for Super 2027.
            CRITICAL INSTRUCTION: The pace calculations have ALREADY been computed deterministically below by our PaceEngine. Do NOT calculate or recalculate any numbers, percentages, or paces. Write ONLY the narrative interpretation, strategic guidance, and daily priorities around these pre-computed results.

            Goals & Pre-Computed Pace Metrics:
            ${goalsPaceArray.toString(2)}

            Today's Scheduled Tasks:
            $tasksSummary

            Active Habits:
            $habitsSummary

            Days Remaining to 2027: $daysTo2027

            Generate a valid JSON object matching this exact schema:
            {
              "summary": "1-2 sentence high-conviction daily narrative referencing the pre-computed pace results",
              "top3Priorities": ["Priority 1", "Priority 2", "Priority 3"],
              "risk": "The primary operational or psychological risk today based on pace and task schedule",
              "nextAction": "The immediate single next action the user must execute in the next 30 minutes"
            }
            Respond with valid JSON only. No markdown formatting.
        """.trimIndent()

        try {
            val responseText = callGeminiRestApi(apiKey, prompt)
            val json = parseJsonClean(responseText)

            // Validate structured output
            val summary = json.optString("summary", "").trim()
            val risk = json.optString("risk", "").trim()
            val nextAction = json.optString("nextAction", "").trim()
            val prioritiesArray = json.optJSONArray("top3Priorities")

            val prioritiesList = mutableListOf<String>()
            if (prioritiesArray != null) {
                for (i in 0 until prioritiesArray.length()) {
                    val p = prioritiesArray.optString(i, "").trim()
                    if (p.isNotBlank()) prioritiesList.add(p)
                }
            }

            // Validation: ensure required fields are present
            if (summary.isBlank() || prioritiesList.isEmpty() || risk.isBlank() || nextAction.isBlank()) {
                throw IllegalArgumentException("Schema validation failed on Gemini response")
            }

            GeminiDailyPlan(
                summary = summary,
                top3Priorities = prioritiesList.take(3),
                risk = risk,
                nextAction = nextAction,
                isAiGenerated = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback to rule-based plan
            fallbackDailyPlan(goals, tasks, habits, daysTo2027)
        }
    }

    suspend fun breakdownGoal(goalTitle: String, category: String): GoalBreakdownResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            You are a master strategist. Break down the goal '$goalTitle' in category '$category' into achievable components before 2027.
            Return a JSON object with:
            {
              "milestones": ["Milestone 1 deadline & output", "Milestone 2 deadline & output", "Milestone 3 deadline & output"],
              "dailyTasks": ["Daily action 1", "Daily action 2", "Daily action 3"],
              "strategy": "Core strategic insight for continuous progress"
            }
            Valid JSON only.
        """.trimIndent()

        if (!isApiKeyConfigured()) {
            return@withContext GoalBreakdownResult(
                suggestedMilestones = listOf(
                    "Phase 1: Foundation & Specs (Month 1)",
                    "Phase 2: Core Execution & Iteration (Month 2-3)",
                    "Phase 3: Polishing, Validation & Release (Final Month)"
                ),
                suggestedDailyTasks = listOf(
                    "Daily 45-minute sprint block on primary deliverable",
                    "Track weekly quantitative output metrics",
                    "Eliminate 1 energy-draining distraction"
                ),
                successStrategy = "Consistency in non-negotiable daily increments always compounds faster than sporadic bursts."
            )
        }

        try {
            val responseText = callGeminiRestApi(apiKey, prompt)
            val json = parseJsonClean(responseText)
            val milestones = mutableListOf<String>()
            json.optJSONArray("milestones")?.let { arr ->
                for (i in 0 until arr.length()) milestones.add(arr.getString(i))
            }
            val tasks = mutableListOf<String>()
            json.optJSONArray("dailyTasks")?.let { arr ->
                for (i in 0 until arr.length()) tasks.add(arr.getString(i))
            }

            GoalBreakdownResult(
                suggestedMilestones = if (milestones.isNotEmpty()) milestones else listOf("Milestone 1: Prototype", "Milestone 2: Validation", "Milestone 3: Launch"),
                suggestedDailyTasks = if (tasks.isNotEmpty()) tasks else listOf("Daily 45-min progress block", "Log measurable metrics"),
                successStrategy = json.optString("strategy", "Focus on leading indicators over lagging results.")
            )
        } catch (_: Exception) {
            GoalBreakdownResult(
                suggestedMilestones = listOf("Milestone 1: Architectural Blueprint", "Milestone 2: 50% Milestone Execution", "Milestone 3: Final 2027 Delivery"),
                suggestedDailyTasks = listOf("Daily high-impact action block", "Review metrics each evening"),
                successStrategy = "Protect focus time and systematically measure weekly progress."
            )
        }
    }

    private fun callGeminiRestApi(apiKey: String, promptText: String): String {
        val url = "$BASE_URL?key=$apiKey"

        val partObj = JSONObject().apply { put("text", promptText) }
        val partsArray = JSONArray().apply { put(partObj) }
        val contentObj = JSONObject().apply { put("parts", partsArray) }
        val contentsArray = JSONArray().apply { put(contentObj) }

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            throw RuntimeException("Gemini API error ${response.code}: $errorBody")
        }

        val rawBody = response.body?.string() ?: throw RuntimeException("Empty response from Gemini")
        val root = JSONObject(rawBody)
        val candidates = root.optJSONArray("candidates") ?: throw RuntimeException("No candidates in Gemini response")
        if (candidates.length() == 0) throw RuntimeException("Empty candidates list")

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: throw RuntimeException("No content object")
        val parts = content.optJSONArray("parts") ?: throw RuntimeException("No parts array")
        if (parts.length() == 0) throw RuntimeException("Empty parts array")

        return parts.getJSONObject(0).optString("text", "")
    }

    private fun parseJsonClean(raw: String): JSONObject {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        }
        if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        clean = clean.trim()
        return JSONObject(clean)
    }

    fun fallbackDailyPlan(
        goals: List<GoalItem>,
        tasks: List<TaskItem>,
        habits: List<HabitItem>,
        daysTo2027: Long
    ): GeminiDailyPlan {
        val topTasks = tasks.filter { it.status == com.example.data.model.TaskStatus.PENDING }
            .take(3)
            .map { it.title }

        val priorities = if (topTasks.isNotEmpty()) {
            topTasks
        } else {
            listOf(
                "Execute 90-minute deep work block for primary 2027 goal",
                "Maintain unbroken daily habit routines",
                "Review pace trajectory and plan tomorrow's deliverables"
            )
        }

        val behindGoal = goals.find { PaceCalculator.calculate(it).status == com.example.domain.PaceStatus.BEHIND }
        val summary = if (behindGoal != null) {
            "Goal '${behindGoal.title}' is currently running behind required daily pace. Prioritize catch-up output today."
        } else {
            "All active goals are tracking on pace. Maintain high velocity to compound momentum before 2027."
        }

        val risk = if (behindGoal != null) {
            "Pace deficit on '${behindGoal.title}'. Multitasking could dilute critical focus time."
        } else {
            "Complacency on green-status goals. Protect deep work blocks from shallow interruptions."
        }

        val nextAction = if (priorities.isNotEmpty()) {
            "Start a 45-minute focus session on '${priorities.first()}' immediately."
        } else {
            "Schedule top 3 non-negotiable tasks into your calendar."
        }

        return GeminiDailyPlan(
            summary = summary,
            top3Priorities = priorities.take(3),
            risk = risk,
            nextAction = nextAction,
            isAiGenerated = false
        )
    }
}
