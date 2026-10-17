package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PaceCalculatorTest {

    private val oneDayMillis = 24 * 3600 * 1000L

    @Test
    fun paceCalculator_behind() {
        val now = 1000000000000L
        val createdAt = now - (10 * oneDayMillis) // 10 days elapsed
        val targetEpoch = now + (50 * oneDayMillis) // 50 days remaining

        val metrics = PaceCalculator.calculate(
            current = 10.0,
            target = 100.0,
            createdAtEpochMillis = createdAt,
            targetEpochMillis = targetEpoch,
            nowEpochMillis = now
        )

        // Required: (100 - 10) / 50 = 1.8 / day
        // Actual: 10 / 10 = 1.0 / day
        // 1.0 < 1.8 * 0.9 (1.62) -> BEHIND
        assertEquals(PaceStatus.BEHIND, metrics.status)
        assertEquals(1.8, metrics.requiredPerDay, 0.01)
        assertEquals(1.0, metrics.actualPerDay, 0.01)
        assertEquals(60.0, metrics.projectedFinish, 0.01)
    }

    @Test
    fun paceCalculator_onTrack() {
        val now = 1000000000000L
        val createdAt = now - (50 * oneDayMillis) // 50 days elapsed
        val targetEpoch = now + (50 * oneDayMillis) // 50 days remaining

        val metrics = PaceCalculator.calculate(
            current = 50.0,
            target = 100.0,
            createdAtEpochMillis = createdAt,
            targetEpochMillis = targetEpoch,
            nowEpochMillis = now
        )

        // Required: (100 - 50) / 50 = 1.0 / day
        // Actual: 50 / 50 = 1.0 / day
        // Within 10% -> ON_TRACK
        assertEquals(PaceStatus.ON_TRACK, metrics.status)
        assertEquals(1.0, metrics.requiredPerDay, 0.01)
        assertEquals(1.0, metrics.actualPerDay, 0.01)
        assertEquals(100.0, metrics.projectedFinish, 0.01)
    }

    @Test
    fun paceCalculator_completed() {
        val now = 1000000000000L
        val createdAt = now - (20 * oneDayMillis)
        val targetEpoch = now + (30 * oneDayMillis)

        val metrics = PaceCalculator.calculate(
            current = 100.0,
            target = 100.0,
            createdAtEpochMillis = createdAt,
            targetEpochMillis = targetEpoch,
            nowEpochMillis = now
        )

        // Target reached -> DONE
        assertEquals(PaceStatus.DONE, metrics.status)
        assertEquals(0.0, metrics.requiredPerDay, 0.001)
        assertEquals(100.0, metrics.projectedFinish, 0.001)
    }
}
