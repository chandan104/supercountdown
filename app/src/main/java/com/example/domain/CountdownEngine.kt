package com.example.domain

import com.example.data.model.CountdownTimeRemaining
import java.util.Calendar

object CountdownEngine {

    /**
     * Calculates precision countdown to a target timestamp.
     * If the target is New Year 2027 and it has passed, it gracefully rolls over
     * to the next New Year (2028), satisfying the rollover requirement.
     */
    fun calculateCountdown(
        targetEpochMillis: Long,
        isDefaultNewYear: Boolean = false,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): CountdownTimeRemaining {
        var adjustedTarget = targetEpochMillis
        var nextYear = 2027

        if (isDefaultNewYear) {
            val nowCal = Calendar.getInstance().apply { timeInMillis = nowEpochMillis }
            val currentYear = nowCal.get(Calendar.YEAR)

            if (nowEpochMillis >= targetEpochMillis) {
                // If 2027 has arrived or passed, target next upcoming Jan 1
                nextYear = currentYear + 1
                val rolloverCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, nextYear)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                adjustedTarget = rolloverCal.timeInMillis
            }
        }

        val diffMillis = adjustedTarget - nowEpochMillis
        if (diffMillis <= 0) {
            return CountdownTimeRemaining(
                days = 0,
                hours = 0,
                minutes = 0,
                seconds = 0,
                totalSecondsRemaining = 0,
                progressFractionOfYear = 1.0f,
                isElapsed = true,
                nextYearLabel = nextYear.toString()
            )
        }

        val totalSeconds = diffMillis / 1000
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        // Year progress calculation: how much of the journey to the target has elapsed
        val startOfYearCal = Calendar.getInstance().apply {
            timeInMillis = nowEpochMillis
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startMillis = startOfYearCal.timeInMillis
        val totalSpan = (adjustedTarget - startMillis).coerceAtLeast(1L)
        val elapsed = (nowEpochMillis - startMillis).coerceAtLeast(0L)
        val progress = (elapsed.toFloat() / totalSpan.toFloat()).coerceIn(0.0f, 1.0f)

        return CountdownTimeRemaining(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalSecondsRemaining = totalSeconds,
            progressFractionOfYear = progress,
            isElapsed = false,
            nextYearLabel = nextYear.toString()
        )
    }

    fun formatCountdownShort(targetEpochMillis: Long): String {
        val cd = calculateCountdown(targetEpochMillis)
        return if (cd.isElapsed) "Completed" else "${cd.days}d ${cd.hours}h remaining"
    }
}
