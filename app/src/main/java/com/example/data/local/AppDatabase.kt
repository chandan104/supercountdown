package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        EventItem::class,
        GoalItem::class,
        MilestoneItem::class,
        TaskItem::class,
        HabitItem::class,
        AIPlanItem::class,
        LearningResource::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun goalDao(): GoalDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun aiPlanDao(): AIPlanDao
    abstract fun resourceDao(): ResourceDao

    suspend fun toggleTaskWithGoalTransaction(task: TaskItem): Pair<TaskItem, GoalItem?> {
        return withTransaction {
            val isNowCompleted = task.status != TaskStatus.COMPLETED
            val updatedTask = task.copy(
                status = if (isNowCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING,
                completedAmount = if (isNowCompleted) task.targetAmount else 0.0,
                completedAtEpochMillis = if (isNowCompleted) System.currentTimeMillis() else null
            )
            taskDao().updateTask(updatedTask)

            var updatedGoal: GoalItem? = null
            val goalId = task.goalId
            val contribution = if (task.goalContribution > 0.0) task.goalContribution else 1.0
            if (!goalId.isNullOrBlank()) {
                val goal = goalDao().getGoalById(goalId)
                if (goal != null) {
                    val delta = if (isNowCompleted) contribution else -contribution
                    val newCurrent = maxOf(0.0, goal.currentValue + delta)
                    val isNowGoalComplete = newCurrent >= goal.targetValue
                    val g = goal.copy(
                        currentValue = newCurrent,
                        isCompleted = isNowGoalComplete,
                        completedAtEpochMillis = if (isNowGoalComplete) System.currentTimeMillis() else null
                    )
                    goalDao().updateGoal(g)
                    updatedGoal = g
                }
            }
            Pair(updatedTask, updatedGoal)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN goalId TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN goalContribution REAL NOT NULL DEFAULT 0.0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE ai_plans ADD COLUMN risk TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE ai_plans ADD COLUMN nextAction TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE ai_plans ADD COLUMN isAiGenerated INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "super_countdown_2027.db"
                )
                    .addCallback(DatabaseSeedCallback())
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialData(database)
                }
            }
        }
    }
}

suspend fun seedInitialData(database: AppDatabase) {
    val cal2027 = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2027)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val calChristmas = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.DECEMBER)
        set(Calendar.DAY_OF_MONTH, 25)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val calQ1 = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.NOVEMBER)
        set(Calendar.DAY_OF_MONTH, 30)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
    }

    // Seed Events
    database.eventDao().insertEvent(
        EventItem(
            id = "event_new_year_2027",
            title = "SUPER 2027",
            subtitle = "New Year 2027 Countdown",
            targetEpochMillis = cal2027.timeInMillis,
            category = "Milestone Year",
            isDefaultNewYear = true,
            isAnnualRecurring = true,
            colorHex = "#38BDF8"
        )
    )

    database.eventDao().insertEvent(
        EventItem(
            id = "event_christmas_2026",
            title = "Christmas 2026",
            subtitle = "Holiday Season & Year-End Sprint",
            targetEpochMillis = calChristmas.timeInMillis,
            category = "Holiday",
            isDefaultNewYear = false,
            isAnnualRecurring = true,
            colorHex = "#10B981"
        )
    )

    database.eventDao().insertEvent(
        EventItem(
            id = "event_q4_sprint",
            title = "Q4 Mastery Sprint",
            subtitle = "Final push before 2027 launch",
            targetEpochMillis = calQ1.timeInMillis,
            category = "Career & Focus",
            isDefaultNewYear = false,
            colorHex = "#F59E0B"
        )
    )

    // Seed Goals
    val goal1Id = "goal_reach_fullstack_ai"
    database.goalDao().insertGoal(
        GoalItem(
            id = goal1Id,
            title = "Master Full-Stack AI & Mobile Architecture",
            description = "Build 5 production-grade applications with generative intelligence before 2027.",
            category = "Career",
            goalType = GoalType.NUMERIC,
            priority = Priority.URGENT,
            targetEpochMillis = cal2027.timeInMillis,
            startingValue = 1.0,
            targetValue = 5.0,
            currentValue = 2.0,
            measurementUnit = "Apps Shipped"
        )
    )

    val goal2Id = "goal_physical_vitality"
    database.goalDao().insertGoal(
        GoalItem(
            id = goal2Id,
            title = "Elite Fitness & Endurance Sprint",
            description = "Complete 150 workouts and log 10,000 active minutes before Jan 1, 2027.",
            category = "Health",
            goalType = GoalType.NUMERIC,
            priority = Priority.HIGH,
            targetEpochMillis = cal2027.timeInMillis,
            startingValue = 0.0,
            targetValue = 150.0,
            currentValue = 42.0,
            measurementUnit = "Workouts"
        )
    )

    // Milestones for Goal 1
    val milestoneCal1 = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 14)
    }
    val milestoneCal2 = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 45)
    }

    database.milestoneDao().insertMilestone(
        MilestoneItem(
            id = "milestone_1",
            goalId = goal1Id,
            title = "Deploy Super 2027 to Production",
            description = "Finalize Room DB, Gemini AI Coach, Home Widget, and system alarm actions.",
            dueEpochMillis = milestoneCal1.timeInMillis,
            isCompleted = false,
            sortOrder = 1
        )
    )
    database.milestoneDao().insertMilestone(
        MilestoneItem(
            id = "milestone_2",
            goalId = goal1Id,
            title = "Launch Cross-Platform AI Workspace",
            description = "Publish backend sync and dynamic intelligence engine.",
            dueEpochMillis = milestoneCal2.timeInMillis,
            isCompleted = false,
            sortOrder = 2
        )
    )

    // Today's date string YYYY-MM-DD
    val today = getTodayDateString()

    // Tasks for today
    database.taskDao().insertTask(
        TaskItem(
            id = "task_morning_deep_work",
            goalId = goal1Id,
            milestoneId = "milestone_1",
            title = "90-Min Focused Deep Work: AI Prompt & Logic",
            description = "Implement Gemini productivity coach and fine-tune reasoning recommendations.",
            scheduledDate = today,
            scheduledTimeMinutes = 9 * 60, // 09:00 AM
            estimatedDurationMinutes = 90,
            priority = Priority.URGENT,
            isQuantifiable = true,
            targetAmount = 90.0,
            completedAmount = 90.0,
            measurementUnit = "min",
            status = TaskStatus.COMPLETED,
            reminderEnabled = true,
            alarmEnabled = true
        )
    )

    database.taskDao().insertTask(
        TaskItem(
            id = "task_outreach_or_reading",
            goalId = goal1Id,
            title = "Technical Study: Jetpack Compose & AppWidgets",
            description = "Read official architecture documentation and benchmark widget performance.",
            scheduledDate = today,
            scheduledTimeMinutes = 14 * 60, // 02:00 PM
            estimatedDurationMinutes = 45,
            priority = Priority.HIGH,
            isQuantifiable = true,
            targetAmount = 25.0,
            completedAmount = 15.0,
            measurementUnit = "pages",
            status = TaskStatus.PENDING,
            reminderEnabled = true,
            alarmEnabled = false
        )
    )

    database.taskDao().insertTask(
        TaskItem(
            id = "task_evening_workout",
            goalId = goal2Id,
            title = "Daily Strength & Mobility Training",
            description = "Complete full body resistance workout and 20 min cardio.",
            scheduledDate = today,
            scheduledTimeMinutes = 18 * 60, // 06:00 PM
            estimatedDurationMinutes = 50,
            priority = Priority.HIGH,
            status = TaskStatus.PENDING,
            reminderEnabled = true,
            alarmEnabled = true
        )
    )

    // Habits
    database.habitDao().insertHabit(
        HabitItem(
            id = "habit_wake_up_early",
            title = "Rise at 05:30 AM",
            category = "Discipline",
            targetDaysPerWeek = 7,
            currentStreak = 14,
            bestStreak = 28,
            lastCompletedDate = today
        )
    )

    database.habitDao().insertHabit(
        HabitItem(
            id = "habit_reading",
            title = "Read 30 mins before sleep",
            category = "Wisdom",
            targetDaysPerWeek = 7,
            currentStreak = 9,
            bestStreak = 21,
            lastCompletedDate = ""
        )
    )

    database.habitDao().insertHabit(
        HabitItem(
            id = "habit_no_distractions",
            title = "No Social Media during Deep Work",
            category = "Focus",
            targetDaysPerWeek = 6,
            currentStreak = 19,
            bestStreak = 30,
            lastCompletedDate = today
        )
    )

    // Curated Learning Resources for Discover
    val resources = listOf(
        LearningResource(
            id = "res_1",
            title = "Architecture of Autonomous AI Agents",
            category = "Programming & AI",
            type = "Guide",
            durationMinutes = 18,
            summary = "How modern LLM reasoning loops, tool-calling, and retrieval engines structure mission-critical workflows.",
            externalUrl = "https://developer.android.com/ai",
            keyTakeaway = "Reliable AI apps rely on validated schemas and deterministic fallback logic."
        ),
        LearningResource(
            id = "res_2",
            title = "High-Performance Habits of Top 1% Builders",
            category = "Personal Development",
            type = "Article",
            durationMinutes = 12,
            summary = "Proven frameworks for turning long-term New Year targets into daily executable milestones.",
            externalUrl = "https://m3.material.io",
            keyTakeaway = "Time blocking and immediate measurable feedback generate lasting momentum."
        ),
        LearningResource(
            id = "res_3",
            title = "Mastering Modern Android Widgets & RemoteViews",
            category = "Programming & AI",
            type = "Video",
            durationMinutes = 24,
            summary = "How to build battery-efficient, glanceable Android AppWidgets that stay synchronized with Room DB.",
            externalUrl = "https://developer.android.com/develop/ui/views/appwidgets",
            keyTakeaway = "Update widgets via broadcast intents and background database triggers."
        ),
        LearningResource(
            id = "res_4",
            title = "Progressive Overload & Peak Cardiovascular Longevity",
            category = "Fitness",
            type = "Guide",
            durationMinutes = 15,
            summary = "Science-backed training volume guidelines to safely accelerate physical performance before 2027.",
            externalUrl = "https://hubermanlab.com",
            keyTakeaway = "Zone 2 base training paired with strength training produces optimal endurance."
        ),
        LearningResource(
            id = "res_5",
            title = "Product Strategy: Turning 1000 True Fans into SaaS",
            category = "Business",
            type = "Article",
            durationMinutes = 20,
            summary = "A tactical playbook for bootstrapping digital products, validated pricing, and conversion funnels.",
            externalUrl = "https://news.ycombinator.com",
            keyTakeaway = "Solve a painful problem with uncompromising speed and visual polish."
        )
    )
    database.resourceDao().insertAll(resources)
}

fun getTodayDateString(): String {
    val cal = Calendar.getInstance()
    val y = cal.get(Calendar.YEAR)
    val m = String.format("%02d", cal.get(Calendar.MONTH) + 1)
    val d = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
    return "$y-$m-$d"
}
