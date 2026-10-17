package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitItem
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.ui.theme.*

@Composable
fun HabitStreakSection(
    habits: List<HabitItem>,
    todayDateString: String,
    onToggleHabit: (HabitItem) -> Unit,
    onAddHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedHabits = habits.count { it.lastCompletedDate == todayDateString }
    val totalHabits = habits.size

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streaks",
                    tint = RadiantGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Habits: $completedHabits/$totalHabits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            TextButton(
                onClick = onAddHabitClick,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("+ New Habit", color = ElectricCyan, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (habits.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DeepSurface
            ) {
                Text(
                    text = "No habits tracked yet. Tap '+ New Habit' to build daily momentum.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(habits, key = { it.id }) { habit ->
                    val isDoneToday = habit.lastCompletedDate == todayDateString
                    Surface(
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onToggleHabit(habit) }
                            .testTag("habit_item_${habit.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDoneToday) Color(0xFF132E22) else CardSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isDoneToday) MilestoneEmerald else BorderSubtle
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = if (habit.currentStreak > 0) RadiantGold else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${habit.currentStreak}d",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (habit.currentStreak > 0) RadiantGold else TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                                Icon(
                                    imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Complete today",
                                    tint = if (isDoneToday) MilestoneEmerald else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = if (isDoneToday) "Completed today!" else "Tap to log",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDoneToday) MilestoneEmerald else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskItem,
    onToggleCompletion: () -> Unit,
    onUpdateAmount: (Double) -> Unit,
    onTriggerAlarmInClock: (Int, Int, String) -> Unit,
    onDeleteTask: () -> Unit,
    onOpenFocusTimer: () -> Unit = {},
    overlapWarning: String? = null,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val isMinuteBased = task.measurementUnit.contains("min", ignoreCase = true) || task.estimatedDurationMinutes > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF0F1A24) else CardSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isCompleted) Color(0xFF1E3A3A) else if (overlapWarning != null) UrgentRose.copy(alpha = 0.7f) else BorderSubtle
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggleCompletion() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MilestoneEmerald,
                        uncheckedColor = TextSecondary
                    ),
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Task title wraps fully without truncation
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) TextMuted else TextPrimary,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Metadata row: Scheduled time, duration, priority, goal contribution
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (task.scheduledTimeMinutes != null) {
                            val hour = task.scheduledTimeMinutes / 60
                            val min = task.scheduledTimeMinutes % 60
                            val ampm = if (hour >= 12) "PM" else "AM"
                            val displayHour = if (hour % 12 == 0) 12 else hour % 12
                            val timeStr = String.format("%02d:%02d %s", displayHour, min, ampm)

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DeepSurface
                            ) {
                                Text(
                                    text = "⏰ $timeStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DeepSurface
                        ) {
                            Text(
                                text = "⏳ ${task.estimatedDurationMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }

                        PriorityBadge(priority = task.priority)

                        if (task.goalId != null && task.goalContribution > 0.0) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF132E22)
                            ) {
                                Text(
                                    text = "+${task.goalContribution.toInt()} Goal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MilestoneEmerald,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Overlap warning banner if another task is scheduled at the same time
                    if (overlapWarning != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E1214),
                            border = BorderStroke(1.dp, UrgentRose.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Overlap",
                                    tint = UrgentRose,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = overlapWarning,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UrgentRose,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteTask,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Task",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quantifiable Steppers or Focus Timer for minute-based tasks
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.isQuantifiable && !isMinuteBased) {
                    Text(
                        text = "Progress: ${task.completedAmount.toInt()} / ${task.targetAmount.toInt()} ${task.measurementUnit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        OutlinedButton(
                            onClick = { onUpdateAmount((task.completedAmount - 1.0).coerceAtLeast(0.0)) },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("-1", fontSize = 11.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { onUpdateAmount(task.completedAmount + 1.0) },
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RadiantGold),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("+1", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // For minute-based tasks, replace +1/-1 with Focus Timer action
                    Text(
                        text = if (task.completedAmount > 0) "Focused: ${task.completedAmount.toInt()} min logged" else "Session: ${task.estimatedDurationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.completedAmount > 0) MilestoneEmerald else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = onOpenFocusTimer,
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_timer_${task.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Start Timer",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Focus Timer", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 9.2 Android System Clock Alarm Action Button
            if (task.scheduledTimeMinutes != null) {
                val hour = task.scheduledTimeMinutes / 60
                val min = task.scheduledTimeMinutes % 60
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 38.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onTriggerAlarmInClock(hour, min, task.title) },
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_set_alarm_${task.id}"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RadiantGold.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Set in Clock",
                            tint = RadiantGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set Alarm in Clock",
                            style = MaterialTheme.typography.labelSmall,
                            color = RadiantGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
