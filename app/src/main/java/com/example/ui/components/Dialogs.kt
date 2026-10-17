package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalType
import com.example.data.model.Priority
import com.example.service.GoalBreakdownResult
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, GoalType, Priority, Long, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Career") }
    var priority by remember { mutableStateOf(Priority.HIGH) }
    var targetValue by remember { mutableStateOf("10") }
    var unit by remember { mutableStateOf("Units") }
    var targetDaysAhead by remember { mutableStateOf("90") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Goal", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title *") },
                    placeholder = { Text("e.g. Master Full-Stack AI") },
                    singleLine = true,
                    isError = title.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Why this matters / Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selector
                Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Career", "Health", "Business", "Learning").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { targetValue = it },
                        label = { Text("Target Number") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g. apps, workouts)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Configurable Target Deadline
                Text("Target Deadline", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("30" to "30 Days", "90" to "90 Days", "180" to "6 Months", "78" to "Jan 1 2027").forEach { (days, label) ->
                        FilterChip(
                            selected = targetDaysAhead == days,
                            onClick = { targetDaysAhead = days },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = targetDaysAhead,
                    onValueChange = { targetDaysAhead = it },
                    label = { Text("Custom Deadline (Days from today)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Priority Level", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(Priority.URGENT, Priority.HIGH, Priority.MEDIUM).forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.name, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val numVal = targetValue.toDoubleOrNull() ?: 1.0
                        val days = targetDaysAhead.toLongOrNull() ?: 90L
                        val targetEpoch = System.currentTimeMillis() + (days * 24 * 3600 * 1000L)
                        onConfirm(
                            title,
                            description,
                            category,
                            GoalType.NUMERIC,
                            priority,
                            targetEpoch,
                            numVal,
                            unit
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("btn_confirm_add_goal")
            ) {
                Text("Create Goal", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AddMilestoneDialog(
    goalTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var weeksAhead by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Milestone for '$goalTitle'", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Milestone Title *") },
                    placeholder = { Text("e.g. Complete Phase 1 Prototype") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_milestone_title")
                )

                OutlinedTextField(
                    value = weeksAhead,
                    onValueChange = { weeksAhead = it },
                    label = { Text("Due in Weeks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val w = weeksAhead.toIntOrNull() ?: 4
                        val targetMillis = System.currentTimeMillis() + (w * 7L * 24 * 3600 * 1000)
                        onConfirm(title, targetMillis)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("btn_confirm_add_milestone")
            ) {
                Text("Save Milestone", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun AddTaskDialog(
    goals: List<com.example.data.model.GoalItem> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, Double, Priority, Int?, Int, Boolean, Double, String, Boolean, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGoalId by remember { mutableStateOf<String?>(null) }
    var goalContributionInput by remember { mutableStateOf("1.0") }
    var priority by remember { mutableStateOf(Priority.HIGH) }
    var scheduledHour by remember { mutableStateOf("9") }
    var scheduledMinute by remember { mutableStateOf("00") }
    var isPm by remember { mutableStateOf(false) }
    var durationMinutes by remember { mutableStateOf("45") }
    var isQuantifiable by remember { mutableStateOf(false) }
    var targetAmount by remember { mutableStateOf("10") }
    var measurementUnit by remember { mutableStateOf("reps") }
    var setInAppReminder by remember { mutableStateOf(true) }
    var setSystemAlarm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Today's Task", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. 90-min Coding Sprint") },
                    isError = title.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Output deliverable") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional Link to Goal
                if (goals.isNotEmpty()) {
                    Text("Link to Goal (Optional)", style = MaterialTheme.typography.labelSmall, color = RadiantGold, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedGoalId == null,
                            onClick = { selectedGoalId = null },
                            label = { Text("None", fontSize = 11.sp) }
                        )
                        goals.take(3).forEach { g ->
                            FilterChip(
                                selected = selectedGoalId == g.id,
                                onClick = { selectedGoalId = g.id },
                                label = { Text(g.title.take(15) + if (g.title.length > 15) "..." else "", fontSize = 11.sp) }
                            )
                        }
                    }

                    if (selectedGoalId != null) {
                        OutlinedTextField(
                            value = goalContributionInput,
                            onValueChange = { goalContributionInput = it },
                            label = { Text("Contributes to Goal (+N)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text("Scheduled Time & Duration", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = scheduledHour,
                        onValueChange = { scheduledHour = it },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = scheduledMinute,
                        onValueChange = { scheduledMinute = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isPm,
                        onClick = { isPm = !isPm },
                        label = { Text(if (isPm) "PM" else "AM") }
                    )
                }

                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    label = { Text("Estimated Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority
                Text("Priority", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(Priority.URGENT, Priority.HIGH, Priority.MEDIUM).forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.name, fontSize = 11.sp) }
                        )
                    }
                }

                // Quantifiable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isQuantifiable = !isQuantifiable }
                ) {
                    Checkbox(checked = isQuantifiable, onCheckedChange = { isQuantifiable = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Quantifiable Metric (e.g. pages, calls)", style = MaterialTheme.typography.bodySmall)
                }

                if (isQuantifiable) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = targetAmount,
                            onValueChange = { targetAmount = it },
                            label = { Text("Target") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = measurementUnit,
                            onValueChange = { measurementUnit = it },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // In-App Reminder Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { setInAppReminder = !setInAppReminder }
                ) {
                    Checkbox(checked = setInAppReminder, onCheckedChange = { setInAppReminder = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule In-App Notification Reminder", style = MaterialTheme.typography.bodySmall)
                }

                // System Clock Alarm Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { setSystemAlarm = !setSystemAlarm }
                ) {
                    Checkbox(checked = setSystemAlarm, onCheckedChange = { setSystemAlarm = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create in Android System Clock app", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val rawH = scheduledHour.toIntOrNull() ?: 9
                        val m = scheduledMinute.toIntOrNull() ?: 0
                        val h24 = if (isPm && rawH < 12) rawH + 12 else if (!isPm && rawH == 12) 0 else rawH
                        val timeMins = (h24 * 60) + m
                        val dur = durationMinutes.toIntOrNull() ?: 45
                        val targetNum = targetAmount.toDoubleOrNull() ?: 1.0
                        val contribution = goalContributionInput.toDoubleOrNull() ?: 1.0

                        onConfirm(
                            title,
                            description,
                            selectedGoalId,
                            if (selectedGoalId != null) contribution else 0.0,
                            priority,
                            timeMins,
                            dur,
                            isQuantifiable,
                            targetNum,
                            measurementUnit,
                            setInAppReminder,
                            setSystemAlarm
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("btn_confirm_add_task")
            ) {
                Text("Schedule Task", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Discipline") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track New Daily Habit", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. 20 min morning workout") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_habit_title")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title, category, 7)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("btn_confirm_add_habit")
            ) {
                Text("Save Habit", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Celebration") }
    var daysAhead by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Countdown Event", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Name") },
                    placeholder = { Text("e.g. Product Launch / Exam") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle / Objective") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysAhead,
                    onValueChange = { daysAhead = it },
                    label = { Text("Days from today") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val d = daysAhead.toLongOrNull() ?: 30L
                        val target = System.currentTimeMillis() + (d * 24 * 3600 * 1000)
                        onConfirm(title, subtitle, category, target, "#38BDF8")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Create Event", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun AiGoalBreakdownDialog(
    goalTitle: String,
    breakdown: GoalBreakdownResult,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = RadiantGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Plan for '$goalTitle'", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "STRATEGIC INSIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = breakdown.successStrategy,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )

                HorizontalDivider(color = BorderSubtle)

                Text(
                    text = "PROPOSED MILESTONES (CHRONOLOGICAL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
                breakdown.suggestedMilestones.forEachIndexed { i, m ->
                    Text("• $m", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }

                HorizontalDivider(color = BorderSubtle)

                Text(
                    text = "RECOMMENDED DAILY TASKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.Bold
                )
                breakdown.suggestedDailyTasks.forEach { t ->
                    Text("✦ $t", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = RadiantGold)
            ) {
                Text("Add to Schedule", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextSecondary) }
        }
    )
}
