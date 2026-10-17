package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.getTodayDateString
import com.example.data.model.*
import com.example.domain.CountdownEngine
import com.example.domain.IntelligentRecommendation
import com.example.domain.RecommendationEngine
import com.example.service.AlarmAndReminderManager
import com.example.service.GeminiDailyPlan
import com.example.service.GeminiService
import com.example.service.GoalBreakdownResult
import com.example.widget.WidgetHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class SuperCountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val todayDateString: String = getTodayDateString()

    // 1. Events State & Live Ticker
    val events: StateFlow<List<EventItem>> = db.eventDao().getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedEventId = MutableStateFlow<String?>("event_new_year_2027")
    val selectedEventId: StateFlow<String?> = _selectedEventId.asStateFlow()

    private val _currentEpoch = MutableStateFlow(System.currentTimeMillis())
    val currentEpoch: StateFlow<Long> = _currentEpoch.asStateFlow()

    // Live dynamic countdown for currently selected event
    val currentCountdown: StateFlow<CountdownTimeRemaining> = combine(
        events,
        selectedEventId,
        currentEpoch
    ) { eventList, selectedId, epochNow ->
        val activeEvent = eventList.find { it.id == selectedId }
            ?: eventList.find { it.isDefaultNewYear }
            ?: eventList.firstOrNull()

        val target = activeEvent?.targetEpochMillis ?: 1798761600000L // 2027-01-01
        CountdownEngine.calculateCountdown(
            targetEpochMillis = target,
            isDefaultNewYear = activeEvent?.isDefaultNewYear ?: true,
            nowEpochMillis = epochNow
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CountdownEngine.calculateCountdown(1798761600000L, isDefaultNewYear = true)
    )

    // 2. Goals & Milestones
    val goals: StateFlow<List<GoalItem>> = db.goalDao().getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<MilestoneItem>> = db.milestoneDao().getAllMilestones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Tasks & Habits for Today
    val todayTasks: StateFlow<List<TaskItem>> = db.taskDao().getTasksForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitItem>> = db.habitDao().getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. Discover Learning Resources
    val resources: StateFlow<List<LearningResource>> = db.resourceDao().getAllResources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5. Daily Progress Statistics
    val progressStats: StateFlow<DailyProgressStats> = combine(
        todayTasks,
        habits,
        goals,
        milestones
    ) { tasksList, habitList, goalList, milestoneList ->
        val completedTasks = tasksList.count { it.status == TaskStatus.COMPLETED }
        val totalTasks = tasksList.size
        val taskPct = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
        val completedHabits = habitList.count { it.lastCompletedDate == todayDateString }
        val activeGoals = goalList.count { !it.isCompleted }
        val upcomingMilestones = milestoneList.count { !it.isCompleted }

        DailyProgressStats(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            completionPercentage = taskPct,
            totalHabits = habitList.size,
            completedHabits = completedHabits,
            activeGoalsCount = activeGoals,
            upcomingMilestonesCount = upcomingMilestones
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyProgressStats(0, 0, 0, 0, 0, 0, 0)
    )

    // 6. Intelligent Recommendations
    val recommendations: StateFlow<List<IntelligentRecommendation>> = combine(
        goals,
        todayTasks,
        habits,
        currentCountdown
    ) { goalList, taskList, habitList, countdown ->
        RecommendationEngine.generateInsights(
            goals = goalList,
            tasks = taskList,
            habits = habitList,
            daysTo2027 = countdown.days
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 7. AI Daily Plan State & Privacy Toggle
    private val prefs = application.getSharedPreferences("super_settings", android.content.Context.MODE_PRIVATE)
    private val _sendDataToAi = MutableStateFlow(prefs.getBoolean("send_data_to_ai", true))
    val sendDataToAi: StateFlow<Boolean> = _sendDataToAi.asStateFlow()

    fun setSendDataToAi(enabled: Boolean) {
        _sendDataToAi.value = enabled
        prefs.edit().putBoolean("send_data_to_ai", enabled).apply()
    }

    private val _aiPlan = MutableStateFlow<GeminiDailyPlan?>(null)
    val aiPlan: StateFlow<GeminiDailyPlan?> = _aiPlan.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiGoalBreakdown = MutableStateFlow<GoalBreakdownResult?>(null)
    val aiGoalBreakdown: StateFlow<GoalBreakdownResult?> = _aiGoalBreakdown.asStateFlow()

    // Status message toast/banner
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Ticking loop for second-by-second countdown
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentEpoch.value = System.currentTimeMillis()
            }
        }

        // Cache the plan for the day: observe DB cached plan
        viewModelScope.launch {
            db.aiPlanDao().getPlanForDate(todayDateString).collect { cached ->
                if (cached != null) {
                    val priorities = try {
                        val arr = org.json.JSONArray(cached.prioritiesJson)
                        (0 until arr.length()).map { arr.getString(it) }
                    } catch (_: Exception) {
                        emptyList()
                    }
                    _aiPlan.value = GeminiDailyPlan(
                        summary = cached.focusSummary,
                        top3Priorities = priorities,
                        risk = cached.risk,
                        nextAction = cached.nextAction,
                        isAiGenerated = cached.isAiGenerated
                    )
                }
            }
        }
    }

    fun selectEvent(eventId: String) {
        _selectedEventId.value = eventId
    }

    fun addCustomEvent(title: String, subtitle: String, category: String, epochMillis: Long, colorHex: String) {
        viewModelScope.launch {
            val event = EventItem(
                id = "event_${UUID.randomUUID().toString().take(8)}",
                title = title,
                subtitle = subtitle,
                targetEpochMillis = epochMillis,
                category = category,
                isDefaultNewYear = false,
                colorHex = colorHex
            )
            db.eventDao().insertEvent(event)
            _selectedEventId.value = event.id
            _userMessage.value = "Event '$title' created!"
            WidgetHelper.updateAllWidgets(getApplication())
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            db.eventDao().deleteEvent(eventId)
            _selectedEventId.value = "event_new_year_2027"
            _userMessage.value = "Event deleted."
            WidgetHelper.updateAllWidgets(getApplication())
        }
    }

    // Goal Actions
    fun addGoal(
        title: String,
        description: String,
        category: String,
        goalType: GoalType,
        priority: Priority,
        targetEpochMillis: Long,
        targetValue: Double,
        unit: String
    ) {
        viewModelScope.launch {
            val goal = GoalItem(
                id = "goal_${UUID.randomUUID().toString().take(8)}",
                title = title,
                description = description,
                category = category,
                goalType = goalType,
                priority = priority,
                targetEpochMillis = targetEpochMillis,
                startingValue = 0.0,
                targetValue = targetValue,
                currentValue = 0.0,
                measurementUnit = unit
            )
            db.goalDao().insertGoal(goal)
            _userMessage.value = "Goal created: $title"
        }
    }

    fun updateGoalProgress(goalId: String, newValue: Double) {
        viewModelScope.launch {
            val goal = db.goalDao().getGoalById(goalId) ?: return@launch
            val isNowComplete = newValue >= goal.targetValue
            db.goalDao().updateGoal(
                goal.copy(
                    currentValue = newValue,
                    isCompleted = isNowComplete,
                    completedAtEpochMillis = if (isNowComplete) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            db.goalDao().deleteGoal(goalId)
            _userMessage.value = "Goal removed"
        }
    }

    fun addMilestone(goalId: String, title: String, dueEpochMillis: Long) {
        viewModelScope.launch {
            val milestone = MilestoneItem(
                id = "ms_${UUID.randomUUID().toString().take(8)}",
                goalId = goalId,
                title = title,
                dueEpochMillis = dueEpochMillis
            )
            db.milestoneDao().insertMilestone(milestone)
            _userMessage.value = "Milestone added: $title"
        }
    }

    fun toggleMilestoneCompletion(milestone: MilestoneItem) {
        viewModelScope.launch {
            val updated = milestone.copy(
                isCompleted = !milestone.isCompleted,
                completedAtEpochMillis = if (!milestone.isCompleted) System.currentTimeMillis() else null
            )
            db.milestoneDao().updateMilestone(updated)
        }
    }

    // Task Actions
    fun addTask(
        title: String,
        description: String,
        goalId: String?,
        goalContribution: Double = 0.0,
        priority: Priority,
        scheduledTimeMinutes: Int?,
        estimatedDurationMinutes: Int,
        isQuantifiable: Boolean,
        targetAmount: Double,
        unit: String,
        reminderEnabled: Boolean,
        alarmEnabled: Boolean
    ) {
        viewModelScope.launch {
            val taskId = "task_${UUID.randomUUID().toString().take(8)}"
            val task = TaskItem(
                id = taskId,
                goalId = goalId,
                title = title,
                description = description,
                scheduledDate = todayDateString,
                scheduledTimeMinutes = scheduledTimeMinutes,
                estimatedDurationMinutes = estimatedDurationMinutes,
                priority = priority,
                isQuantifiable = isQuantifiable,
                targetAmount = targetAmount,
                completedAmount = 0.0,
                measurementUnit = unit,
                goalContribution = goalContribution,
                reminderEnabled = reminderEnabled,
                alarmEnabled = alarmEnabled
            )
            db.taskDao().insertTask(task)

            // If reminder requested, schedule via in-app reminder
            if (reminderEnabled && scheduledTimeMinutes != null) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, scheduledTimeMinutes / 60)
                    set(Calendar.MINUTE, scheduledTimeMinutes % 60)
                    set(Calendar.SECOND, 0)
                }
                AlarmAndReminderManager.scheduleInAppReminder(
                    getApplication(),
                    taskId,
                    title,
                    cal.timeInMillis
                )
            }

            _userMessage.value = "Task scheduled for today"
            WidgetHelper.updateAllWidgets(getApplication())
        }
    }

    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            val (updatedTask, updatedGoal) = db.toggleTaskWithGoalTransaction(task)
            if (updatedGoal != null) {
                val sign = if (updatedTask.status == TaskStatus.COMPLETED) "+" else "-"
                _userMessage.value = "Task ${if (updatedTask.status == TaskStatus.COMPLETED) "completed" else "uncompleted"}: '${updatedGoal.title}' $sign${task.goalContribution.toInt()}"
            }
            WidgetHelper.updateAllWidgets(getApplication())
        }
    }

    fun updateTaskAmount(task: TaskItem, newAmount: Double) {
        viewModelScope.launch {
            val isComplete = newAmount >= task.targetAmount
            val updated = task.copy(
                completedAmount = newAmount,
                status = if (isComplete) TaskStatus.COMPLETED else TaskStatus.PENDING,
                completedAtEpochMillis = if (isComplete) System.currentTimeMillis() else null
            )
            db.taskDao().updateTask(updated)
            WidgetHelper.updateAllWidgets(getApplication())
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            AlarmAndReminderManager.cancelInAppReminder(getApplication(), taskId)
            db.taskDao().deleteTask(taskId)
            _userMessage.value = "Task deleted"
            WidgetHelper.updateAllWidgets(getApplication())
        }
    }

    // Habit Actions
    fun toggleHabitToday(habit: HabitItem) {
        viewModelScope.launch {
            val isDoneToday = habit.lastCompletedDate == todayDateString
            val updatedStreak = if (isDoneToday) {
                (habit.currentStreak - 1).coerceAtLeast(0)
            } else {
                habit.currentStreak + 1
            }
            val best = maxOf(habit.bestStreak, updatedStreak)
            val updated = habit.copy(
                currentStreak = updatedStreak,
                bestStreak = best,
                lastCompletedDate = if (isDoneToday) "" else todayDateString
            )
            db.habitDao().updateHabit(updated)
            _userMessage.value = if (!isDoneToday) "Streak recorded: $updatedStreak days!" else "Habit reset"
        }
    }

    fun addHabit(title: String, category: String, targetDays: Int) {
        viewModelScope.launch {
            val habit = HabitItem(
                id = "habit_${UUID.randomUUID().toString().take(8)}",
                title = title,
                category = category,
                targetDaysPerWeek = targetDays
            )
            db.habitDao().insertHabit(habit)
            _userMessage.value = "New habit: $title"
        }
    }

    // AI Daily Plan & Goal Breakdown
    fun requestAiDailyPlan() {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val plan = GeminiService.generateDailyPlan(
                    goals = goals.value,
                    tasks = todayTasks.value,
                    habits = habits.value,
                    daysTo2027 = currentCountdown.value.days,
                    allowOnlineAi = _sendDataToAi.value
                )
                _aiPlan.value = plan

                // Cache plan into Room database for today
                val planItem = AIPlanItem(
                    id = "plan_$todayDateString",
                    planDate = todayDateString,
                    focusSummary = plan.summary,
                    prioritiesJson = org.json.JSONArray(plan.top3Priorities).toString(),
                    risk = plan.risk,
                    nextAction = plan.nextAction,
                    isAiGenerated = plan.isAiGenerated
                )
                db.aiPlanDao().insertPlan(planItem)

                _userMessage.value = if (plan.isAiGenerated) "Gemini Daily Plan generated!" else "Intelligence Daily Plan created!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to generate plan: ${e.message}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun requestGoalBreakdown(goalTitle: String, category: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val result = GeminiService.breakdownGoal(goalTitle, category)
                _aiGoalBreakdown.value = result
                _userMessage.value = "Goal breakdown completed!"
            } catch (e: Exception) {
                _userMessage.value = "Error breaking down goal: ${e.message}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun applyGoalBreakdown(goalId: String, breakdown: GoalBreakdownResult) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            breakdown.suggestedMilestones.forEachIndexed { index, mTitle ->
                val due = now + (index + 1) * 30L * 24 * 3600 * 1000 // 30 days apart
                db.milestoneDao().insertMilestone(
                    MilestoneItem(
                        id = "ms_${UUID.randomUUID().toString().take(8)}",
                        goalId = goalId,
                        title = mTitle,
                        dueEpochMillis = due,
                        sortOrder = index + 1
                    )
                )
            }
            breakdown.suggestedDailyTasks.forEach { tTitle ->
                db.taskDao().insertTask(
                    TaskItem(
                        id = "task_${UUID.randomUUID().toString().take(8)}",
                        goalId = goalId,
                        title = tTitle,
                        scheduledDate = todayDateString,
                        priority = Priority.HIGH
                    )
                )
            }
            _aiGoalBreakdown.value = null
            _userMessage.value = "Milestones and daily tasks saved to your schedule!"
            WidgetHelper.updateAllWidgets(getApplication())
        }
    }

    // Discover Resource Actions
    fun toggleBookmarkResource(resource: LearningResource) {
        viewModelScope.launch {
            val updated = resource.copy(isBookmarked = !resource.isBookmarked)
            db.resourceDao().updateResource(updated)
        }
    }

    fun toggleCompleteResource(resource: LearningResource) {
        viewModelScope.launch {
            val updated = resource.copy(isCompleted = !resource.isCompleted)
            db.resourceDao().updateResource(updated)
            _userMessage.value = if (updated.isCompleted) "Resource completed!" else "Marked as unread"
        }
    }

    // System Alarm Trigger (Section 9.2)
    fun triggerSystemAlarm(hour: Int, minute: Int, label: String): Boolean {
        val success = AlarmAndReminderManager.openSystemClockAlarm(
            context = getApplication(),
            hour = hour,
            minute = minute,
            message = "Super 2027: $label"
        )
        if (!success) {
            _userMessage.value = "System Clock app could not be launched directly. Follow manual instructions in settings."
        }
        return success
    }

    // Send Progress Push Notification
    fun sendProgressNotification() {
        val stats = progressStats.value
        val cd = currentCountdown.value
        val title = "Super 2027 Daily Progress"
        val content = "${cd.days} days to 2027! You have completed ${stats.completedTasks}/${stats.totalTasks} tasks (${stats.completionPercentage}%) today."
        AlarmAndReminderManager.postProgressNotification(
            context = getApplication(),
            notificationId = 101,
            title = title,
            content = content
        )
        _userMessage.value = "Progress push notification sent!"
    }

    fun requestPinAppWidget(): Boolean {
        val success = WidgetHelper.requestPinWidget(getApplication())
        _userMessage.value = if (success) "Home screen widget prompt opened!" else "Add widget manually from your home screen launcher."
        return success
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
