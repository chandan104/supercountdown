package com.example

import com.example.data.model.*
import com.example.domain.CountdownEngine
import com.example.domain.RecommendationEngine
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun countdownEngine_calculatesDaysHoursMinutesSeconds() {
        val now = 1000000000L
        val target = now + (2 * 24 * 3600 * 1000L) + (3 * 3600 * 1000L) + (4 * 60 * 1000L) + (5 * 1000L)

        val result = CountdownEngine.calculateCountdown(
            targetEpochMillis = target,
            isDefaultNewYear = false,
            nowEpochMillis = now
        )

        assertEquals(2, result.days)
        assertEquals(3, result.hours)
        assertEquals(4, result.minutes)
        assertEquals(5, result.seconds)
        assertFalse(result.isElapsed)
    }

    @Test
    fun countdownEngine_handlesElapsedTargetsGracefully() {
        val now = 2000000000L
        val pastTarget = 1000000000L

        val result = CountdownEngine.calculateCountdown(
            targetEpochMillis = pastTarget,
            isDefaultNewYear = false,
            nowEpochMillis = now
        )

        assertEquals(0, result.days)
        assertEquals(0, result.hours)
        assertTrue(result.isElapsed)
    }

    @Test
    fun countdownEngine_rollsOverNewYearAfter2027() {
        // Mock a timestamp inside 2027 (e.g. March 1, 2027)
        val cal2027 = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2027)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val targetJan1_2027 = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2027)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val result = CountdownEngine.calculateCountdown(
            targetEpochMillis = targetJan1_2027,
            isDefaultNewYear = true,
            nowEpochMillis = cal2027.timeInMillis
        )

        // It should rollover to 2028
        assertEquals("2028", result.nextYearLabel)
        assertFalse(result.isElapsed)
        assertTrue(result.days > 0)
    }

    @Test
    fun recommendationEngine_identifiesUrgentTasks() {
        val tasks = listOf(
            TaskItem(
                id = "1",
                title = "Launch Critical Sprint",
                scheduledDate = "2026-10-14",
                priority = Priority.URGENT,
                status = TaskStatus.PENDING
            )
        )
        val goals = emptyList<GoalItem>()
        val habits = emptyList<HabitItem>()

        val insights = RecommendationEngine.generateInsights(goals, tasks, habits, daysTo2027 = 444)
        assertTrue(insights.any { it.category == "Urgent Priority" })
    }

    @Test
    fun recommendationEngine_celebratesHabitStreaks() {
        val habits = listOf(
            HabitItem(
                id = "1",
                title = "Deep Focus Routine",
                currentStreak = 14
            )
        )
        val insights = RecommendationEngine.generateInsights(emptyList(), emptyList(), habits, daysTo2027 = 444)
        assertTrue(insights.any { it.category == "Habit Momentum" })
    }
}
