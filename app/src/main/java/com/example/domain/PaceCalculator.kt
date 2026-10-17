package com.example.domain

import com.example.data.model.GoalItem

enum class PaceStatus {
    DONE,
    AHEAD,
    ON_TRACK,
    BEHIND
}

data class PaceMetrics(
    val requiredPerDay: Double,
    val actualPerDay: Double,
    val status: PaceStatus,
    val projectedFinish: Double,
    val daysRemaining: Long,
    val daysElapsed: Long
)

object PaceCalculator {

    fun calculate(
        goal: GoalItem,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): PaceMetrics {
        return calculate(
            current = goal.currentValue,
            target = goal.targetValue,
            createdAtEpochMillis = goal.createdAtEpochMillis,
            targetEpochMillis = goal.targetEpochMillis,
            nowEpochMillis = nowEpochMillis
        )
    }

    fun calculate(
        current: Double,
        target: Double,
        createdAtEpochMillis: Long,
        targetEpochMillis: Long,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): PaceMetrics {
        val daysRemaining = maxOf(0L, (targetEpochMillis - nowEpochMillis) / (24 * 3600 * 1000L))
        val daysElapsed = maxOf(1L, (nowEpochMillis - createdAtEpochMillis) / (24 * 3600 * 1000L))

        if (current >= target) {
            val actual = if (daysElapsed > 0) current / daysElapsed.toDouble() else current
            return PaceMetrics(
                requiredPerDay = 0.0,
                actualPerDay = actual,
                status = PaceStatus.DONE,
                projectedFinish = current,
                daysRemaining = daysRemaining,
                daysElapsed = daysElapsed
            )
        }

        val remainingTarget = maxOf(0.0, target - current)
        val requiredPerDay = if (daysRemaining > 0) {
            remainingTarget / daysRemaining.toDouble()
        } else {
            remainingTarget
        }

        val actualPerDay = maxOf(0.0, current / daysElapsed.toDouble())
        val projectedFinish = current + (actualPerDay * daysRemaining.toDouble())

        val status = when {
            actualPerDay >= requiredPerDay * 1.1 -> PaceStatus.AHEAD
            actualPerDay >= requiredPerDay * 0.9 -> PaceStatus.ON_TRACK
            else -> PaceStatus.BEHIND
        }

        return PaceMetrics(
            requiredPerDay = requiredPerDay,
            actualPerDay = actualPerDay,
            status = status,
            projectedFinish = projectedFinish,
            daysRemaining = daysRemaining,
            daysElapsed = daysElapsed
        )
    }
}
