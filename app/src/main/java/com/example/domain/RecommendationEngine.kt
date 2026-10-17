package com.example.domain

import com.example.data.model.GoalItem
import com.example.data.model.HabitItem
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus

data class IntelligentRecommendation(
    val category: String,
    val title: String,
    val description: String,
    val urgencyScore: Int, // 1 - 100
    val actionableStep: String
)

object RecommendationEngine {

    fun generateInsights(
        goals: List<GoalItem>,
        tasks: List<TaskItem>,
        habits: List<HabitItem>,
        daysTo2027: Long
    ): List<IntelligentRecommendation> {
        val recommendations = mutableListOf<IntelligentRecommendation>()

        val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING }
        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }
        val urgentPending = pendingTasks.filter { it.priority == com.example.data.model.Priority.URGENT }

        // 1. Critical Urgency Check
        if (urgentPending.isNotEmpty()) {
            val task = urgentPending.first()
            recommendations.add(
                IntelligentRecommendation(
                    category = "Urgent Priority",
                    title = "Execute '${task.title}' First",
                    description = "You have ${urgentPending.size} urgent task(s) scheduled for today. Completing high-leverage work before noon eliminates cognitive friction.",
                    urgencyScore = 95,
                    actionableStep = "Dedicate an uninterrupted 45-minute block right now to finish this."
                )
            )
        }

        // 2. 2027 Countdown Trajectory
        val activeGoals = goals.filter { !it.isCompleted }
        if (activeGoals.isNotEmpty()) {
            val goal = activeGoals.minByOrNull { it.currentValue / it.targetValue.coerceAtLeast(1.0) }
            if (goal != null) {
                val progressPct = ((goal.currentValue / goal.targetValue.coerceAtLeast(1.0)) * 100).toInt()
                recommendations.add(
                    IntelligentRecommendation(
                        category = "Goal Trajectory",
                        title = "${goal.title} is at $progressPct%",
                        description = "With $daysTo2027 days remaining until 2027, maintaining a steady daily cadence of +1 unit per week ensures you cross the finish line with comfort.",
                        urgencyScore = 85,
                        actionableStep = "Schedule 2 micro-tasks for this goal into your calendar this week."
                    )
                )
            }
        }

        // 3. Habit Momentum Analysis
        val unbrokenHabits = habits.filter { it.currentStreak >= 7 }
        if (unbrokenHabits.isNotEmpty()) {
            val best = unbrokenHabits.maxByOrNull { it.currentStreak }!!
            recommendations.add(
                IntelligentRecommendation(
                    category = "Habit Momentum",
                    title = "${best.title}: ${best.currentStreak}-Day Streak!",
                    description = "Neuroplasticity solidifies habits after 14-21 days of unbroken execution. You are building an unshakeable identity.",
                    urgencyScore = 70,
                    actionableStep = "Lock in today's execution to push this streak even higher."
                )
            )
        } else if (habits.isNotEmpty()) {
            val lowest = habits.minByOrNull { it.currentStreak }!!
            recommendations.add(
                IntelligentRecommendation(
                    category = "Habit Recovery",
                    title = "Ignite Momentum for '${lowest.title}'",
                    description = "Small daily consistency outperforms sporadic massive effort. Aim for 3 consecutive days to rebuild automaticity.",
                    urgencyScore = 65,
                    actionableStep = "Anchor this habit immediately following your morning beverage."
                )
            )
        }

        // 4. Daily Completion Velocity
        val totalToday = tasks.size
        if (totalToday > 0) {
            val ratio = completedTasks.size.toDouble() / totalToday.toDouble()
            if (ratio >= 0.75) {
                recommendations.add(
                    IntelligentRecommendation(
                        category = "Peak Velocity",
                        title = "Exceptional Focus Today!",
                        description = "You have finished ${completedTasks.size}/$totalToday items (${(ratio * 100).toInt()}%). Transition into restorative review mode this evening.",
                        urgencyScore = 60,
                        actionableStep = "Review tomorrow's top 3 objectives before powering down."
                    )
                )
            } else if (pendingTasks.size >= 4) {
                recommendations.add(
                    IntelligentRecommendation(
                        category = "Cognitive Load",
                        title = "Avoid Task Splintering",
                        description = "You have ${pendingTasks.size} tasks pending. Juggling too many contexts reduces deep execution quality.",
                        urgencyScore = 75,
                        actionableStep = "Pick the single most impactful task and defer secondary items."
                    )
                )
            }
        }

        return recommendations.sortedByDescending { it.urgencyScore }
    }
}
