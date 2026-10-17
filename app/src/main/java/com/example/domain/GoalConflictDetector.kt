package com.example.domain

import com.example.data.model.*

object GoalConflictDetector {

    fun detectConflicts(
        goals: List<GoalItem>,
        tasks: List<TaskItem>,
        milestones: List<MilestoneItem>
    ): List<GoalConflict> {
        val conflicts = mutableListOf<GoalConflict>()

        // 1. Check for Excessive Daily Load (> 420 mins / 7 hours of scheduled tasks)
        val totalScheduledMinutes = tasks.sumOf { it.estimatedDurationMinutes }
        if (totalScheduledMinutes > 420) {
            val hours = totalScheduledMinutes / 60
            val mins = totalScheduledMinutes % 60
            conflicts.add(
                GoalConflict(
                    id = "conflict_excessive_load",
                    title = "Overloaded Daily Capacity (${hours}h ${mins}m Scheduled)",
                    conflictType = ConflictType.EXCESSIVE_DAILY_LOAD,
                    description = "Scheduling more than 7 hours of concentrated tasks creates high cognitive fatigue and reduces execution probability.",
                    affectedItems = tasks.map { it.title }.take(4),
                    suggestedOptimization = "Cap daily planned commitments at 5-6 hours. Move non-urgent secondary tasks to tomorrow's queue.",
                    severity = ConflictSeverity.CRITICAL
                )
            )
        }

        // 2. Check for Direct Time Overlaps between scheduled tasks
        val timedTasks = tasks.filter { it.scheduledTimeMinutes != null }
            .sortedBy { it.scheduledTimeMinutes }

        for (i in 0 until timedTasks.size - 1) {
            val current = timedTasks[i]
            val next = timedTasks[i + 1]
            val currentStart = current.scheduledTimeMinutes!!
            val currentEnd = currentStart + current.estimatedDurationMinutes
            val nextStart = next.scheduledTimeMinutes!!

            if (currentEnd > nextStart) {
                val overlapMins = currentEnd - nextStart
                val curTimeStr = formatMinutes(currentStart)
                val nextTimeStr = formatMinutes(nextStart)
                conflicts.add(
                    GoalConflict(
                        id = "conflict_overlap_${current.id}_${next.id}",
                        title = "Schedule Clash: '${current.title}' & '${next.title}'",
                        conflictType = ConflictType.TIME_OVERLAP,
                        description = "'${current.title}' ($curTimeStr, ${current.estimatedDurationMinutes}m) overlaps with '${next.title}' ($nextTimeStr) by $overlapMins minutes.",
                        affectedItems = listOf(current.title, next.title),
                        suggestedOptimization = "Shift '${next.title}' forward by at least $overlapMins minutes to provide an intentional focus buffer.",
                        severity = ConflictSeverity.CRITICAL
                    )
                )
            }
        }

        // 3. Priority Discord: Urgent tasks scheduled in late evening vs Low-priority in morning
        val urgentTasks = tasks.filter { it.priority == Priority.URGENT && it.scheduledTimeMinutes != null }
        val lowTasks = tasks.filter { it.priority == Priority.LOW && it.scheduledTimeMinutes != null }

        urgentTasks.forEach { u ->
            val uTime = u.scheduledTimeMinutes!!
            val morningLow = lowTasks.filter { (it.scheduledTimeMinutes ?: 0) < 12 * 60 }
            if (uTime >= 17 * 60 && morningLow.isNotEmpty()) {
                conflicts.add(
                    GoalConflict(
                        id = "conflict_priority_discord_${u.id}",
                        title = "Suboptimal Energy Sequencing for '${u.title}'",
                        conflictType = ConflictType.PRIORITY_DISCORD,
                        description = "Urgent task '${u.title}' is scheduled after 5:00 PM while lower-priority items occupy morning prime focus hours.",
                        affectedItems = listOf(u.title) + morningLow.map { it.title },
                        suggestedOptimization = "Swap '${u.title}' into the morning window (09:00 - 11:30 AM) when executive willpower and focus are at their peak.",
                        severity = ConflictSeverity.MODERATE
                    )
                )
            }
        }

        // 4. Milestone Deadline Collision (Multiple milestones due within 48 hours)
        val now = System.currentTimeMillis()
        val upcomingMilestones = milestones.filter { !it.isCompleted && it.dueEpochMillis > now }
            .sortedBy { it.dueEpochMillis }

        for (i in 0 until upcomingMilestones.size - 1) {
            val m1 = upcomingMilestones[i]
            val m2 = upcomingMilestones[i + 1]
            val diffHours = (m2.dueEpochMillis - m1.dueEpochMillis) / (3600 * 1000)

            if (diffHours in 0..48) {
                conflicts.add(
                    GoalConflict(
                        id = "conflict_milestone_${m1.id}_${m2.id}",
                        title = "Milestone Bottleneck: '${m1.title}' & '${m2.title}'",
                        conflictType = ConflictType.DEADLINE_COLLISION,
                        description = "Two major goal checkpoints are due within $diffHours hours of each other, creating sprint bottlenecking.",
                        affectedItems = listOf(m1.title, m2.title),
                        suggestedOptimization = "Stagger the deadline of '${m2.title}' by 5-7 days or complete '${m1.title}' 3 days ahead of time.",
                        severity = ConflictSeverity.MODERATE
                    )
                )
            }
        }

        return conflicts
    }

    private fun formatMinutes(minsFromMidnight: Int): String {
        val h = minsFromMidnight / 60
        val m = minsFromMidnight % 60
        val ampm = if (h >= 12) "PM" else "AM"
        val displayH = if (h % 12 == 0) 12 else h % 12
        return String.format("%02d:%02d %s", displayH, m, ampm)
    }
}
