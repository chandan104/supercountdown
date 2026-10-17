package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String = "",
    val targetEpochMillis: Long,
    val category: String = "Celebration",
    val isDefaultNewYear: Boolean = false,
    val isAnnualRecurring: Boolean = false,
    val colorHex: String = "#38BDF8",
    val isArchived: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

enum class GoalType {
    BINARY,
    NUMERIC,
    MILESTONE_BASED,
    HABIT_SUPPORTED
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

@Entity(tableName = "goals")
data class GoalItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "Personal Development",
    val goalType: GoalType = GoalType.MILESTONE_BASED,
    val priority: Priority = Priority.HIGH,
    val targetEpochMillis: Long,
    val startingValue: Double = 0.0,
    val targetValue: Double = 100.0,
    val currentValue: Double = 0.0,
    val measurementUnit: String = "%",
    val isCompleted: Boolean = false,
    val completedAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "milestones")
data class MilestoneItem(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val title: String,
    val description: String = "",
    val dueEpochMillis: Long,
    val isCompleted: Boolean = false,
    val completedAtEpochMillis: Long? = null,
    val sortOrder: Int = 0
)

enum class TaskStatus {
    PENDING,
    COMPLETED,
    SKIPPED
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey
    val id: String,
    val goalId: String? = null,
    val milestoneId: String? = null,
    val title: String,
    val description: String = "",
    val scheduledDate: String, // YYYY-MM-DD
    val scheduledTimeMinutes: Int? = null, // minutes from midnight, e.g. 9:00 AM -> 540
    val estimatedDurationMinutes: Int = 30,
    val priority: Priority = Priority.MEDIUM,
    val isQuantifiable: Boolean = false,
    val targetAmount: Double = 1.0,
    val completedAmount: Double = 0.0,
    val measurementUnit: String = "",
    val goalContribution: Double = 0.0,
    val status: TaskStatus = TaskStatus.PENDING,
    val reminderEnabled: Boolean = false,
    val alarmEnabled: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrencePattern: String = "DAILY", // DAILY, WEEKDAYS, WEEKLY
    val completedAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class HabitItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String = "Routine",
    val targetDaysPerWeek: Int = 7,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String = "", // YYYY-MM-DD
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_plans")
data class AIPlanItem(
    @PrimaryKey
    val id: String,
    val planDate: String, // YYYY-MM-DD
    val focusSummary: String,
    val prioritiesJson: String, // JSON array of string tasks
    val risk: String = "",
    val nextAction: String = "",
    val scheduleRecommendation: String = "",
    val obstacleAdvice: String = "",
    val productivityTip: String = "",
    val isAiGenerated: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_resources")
data class LearningResource(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val type: String, // Article, Video, Guide, Podcast
    val durationMinutes: Int,
    val summary: String,
    val externalUrl: String,
    val keyTakeaway: String,
    val isBookmarked: Boolean = false,
    val isCompleted: Boolean = false
)

data class CountdownTimeRemaining(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSecondsRemaining: Long,
    val progressFractionOfYear: Float,
    val isElapsed: Boolean = false,
    val nextYearLabel: String = "2027"
)

data class DailyProgressStats(
    val totalTasks: Int,
    val completedTasks: Int,
    val completionPercentage: Int,
    val totalHabits: Int,
    val completedHabits: Int,
    val activeGoalsCount: Int,
    val upcomingMilestonesCount: Int
)

enum class ConflictType {
    EXCESSIVE_DAILY_LOAD,
    TIME_OVERLAP,
    PRIORITY_DISCORD,
    DEADLINE_COLLISION
}

enum class ConflictSeverity {
    LOW,
    MODERATE,
    CRITICAL
}

data class GoalConflict(
    val id: String,
    val title: String,
    val conflictType: ConflictType,
    val description: String,
    val affectedItems: List<String>,
    val suggestedOptimization: String,
    val severity: ConflictSeverity
)

