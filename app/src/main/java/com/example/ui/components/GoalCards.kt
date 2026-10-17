package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalItem
import com.example.data.model.GoalType
import com.example.data.model.MilestoneItem
import com.example.data.model.Priority
import com.example.domain.CountdownEngine
import com.example.service.GoalBreakdownResult
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun GoalCard(
    goal: GoalItem,
    milestones: List<MilestoneItem>,
    onUpdateProgress: (Double) -> Unit,
    onAddMilestoneClick: () -> Unit,
    onToggleMilestone: (MilestoneItem) -> Unit,
    onDeleteGoal: () -> Unit,
    onAiPlanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goalCountdown = remember(goal.targetEpochMillis) {
        CountdownEngine.calculateCountdown(goal.targetEpochMillis)
    }

    val progressFraction = (goal.currentValue / goal.targetValue.coerceAtLeast(1.0))
        .toFloat().coerceIn(0.0f, 1.0f)
    val progressPercent = (progressFraction * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("goal_card_${goal.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, if (goal.isCompleted) MilestoneEmerald else BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Category, Priority, and Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = goal.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityBadge(priority = goal.priority)
                    Spacer(modifier = Modifier.width(8.dp))
                    val pace = remember(goal.currentValue, goal.targetValue, goal.targetEpochMillis, goal.createdAtEpochMillis) {
                        com.example.domain.PaceCalculator.calculate(goal)
                    }
                    PaceStatusBadge(status = pace.status)
                }

                IconButton(
                    onClick = onDeleteGoal,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Goal",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Milestone Personalized Countdown Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = DeepSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Countdown",
                            tint = RadiantGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Goal Target Countdown:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = if (goalCountdown.isElapsed) "COMPLETED" else "${goalCountdown.days}d ${goalCountdown.hours}h ${goalCountdown.minutes}m remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (goalCountdown.isElapsed) MilestoneEmerald else RadiantGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar & Value Adjuster
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.measurementUnit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (goal.isCompleted) MilestoneEmerald else ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) MilestoneEmerald else ElectricCyan,
                trackColor = Color(0xFF0F172A)
            )

            // Pace Engine Display
            val paceMetrics = remember(goal.currentValue, goal.targetValue, goal.targetEpochMillis, goal.createdAtEpochMillis) {
                com.example.domain.PaceCalculator.calculate(goal)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DeepSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Need ${String.format("%.1f", paceMetrics.requiredPerDay)}/day · You're at ${String.format("%.1f", paceMetrics.actualPerDay)}/day",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (paceMetrics.status == com.example.domain.PaceStatus.BEHIND) UrgentRose else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Projected: ${paceMetrics.projectedFinish.toInt()}/${goal.targetValue.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper buttons for quick update
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    OutlinedButton(
                        onClick = { onUpdateProgress((goal.currentValue - 1.0).coerceAtLeast(0.0)) },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("-1", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onUpdateProgress(goal.currentValue + 1.0) },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("+1 ${goal.measurementUnit}", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // AI Plan assistant button
                TextButton(
                    onClick = onAiPlanClick,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Plan",
                        tint = RadiantGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Plan with AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Milestones Section
            if (milestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "MILESTONES & PERSONAL COUNTDOWNS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                milestones.forEach { ms ->
                    val msCd = remember(ms.dueEpochMillis) {
                        CountdownEngine.calculateCountdown(ms.dueEpochMillis)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .clickable { onToggleMilestone(ms) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = ms.isCompleted,
                                onCheckedChange = { onToggleMilestone(ms) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MilestoneEmerald,
                                    uncheckedColor = TextSecondary
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = ms.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (ms.isCompleted) TextMuted else TextPrimary
                                )
                                Text(
                                    text = if (ms.isCompleted) "✓ Completed" else "⏱ ${msCd.days}d ${msCd.hours}h left",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (ms.isCompleted) MilestoneEmerald else RadiantGold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Add Milestone Button
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onAddMilestoneClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Milestone", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Add Milestone to this Goal", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: Priority) {
    val (color, text) = when (priority) {
        Priority.URGENT -> UrgentRose to "URGENT"
        Priority.HIGH -> RadiantGold to "HIGH"
        Priority.MEDIUM -> ElectricCyan to "MED"
        Priority.LOW -> TextSecondary to "LOW"
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp
        )
    }
}

@Composable
fun PaceStatusBadge(status: com.example.domain.PaceStatus) {
    val (color, label) = when (status) {
        com.example.domain.PaceStatus.DONE -> MilestoneEmerald to "DONE"
        com.example.domain.PaceStatus.AHEAD -> MilestoneEmerald to "AHEAD"
        com.example.domain.PaceStatus.ON_TRACK -> RadiantGold to "ON TRACK"
        com.example.domain.PaceStatus.BEHIND -> UrgentRose to "BEHIND"
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            fontSize = 10.sp
        )
    }
}
