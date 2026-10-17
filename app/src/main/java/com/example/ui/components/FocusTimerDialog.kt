package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.ui.theme.*

@Composable
fun FocusTimerDialog(
    task: TaskItem,
    elapsedSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopAndLog: (Int) -> Unit, // returns elapsed minutes
    onDismiss: () -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    val elapsedMinutesToLog = maxOf(1, (elapsedSeconds + 30) / 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "FOCUS SESSION",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepSurface
                ) {
                    Text(
                        text = "Estimated: ${task.estimatedDurationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Timer Display Circle
                Surface(
                    modifier = Modifier
                        .size(170.dp)
                        .testTag("focus_timer_clock"),
                    shape = CircleShape,
                    color = Color(0xFF0F172A),
                    border = BorderStroke(3.dp, if (isRunning && !isPaused) ElectricCyan else RadiantGold)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                fontSize = 34.sp
                            )
                            Text(
                                text = if (!isRunning) "Ready" else if (isPaused) "Paused" else "Running",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!isRunning) TextMuted else if (isPaused) RadiantGold else MilestoneEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Controls: Start, Pause, Resume
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRunning) {
                        Button(
                            onClick = onStart,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            modifier = Modifier
                                .size(56.dp)
                                .testTag("btn_start_focus")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                        }
                    } else if (!isPaused) {
                        Button(
                            onClick = onPause,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = RadiantGold),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.Black)
                        }
                    } else {
                        Button(
                            onClick = onResume,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.Black)
                        }
                    }

                    if (isRunning || elapsedSeconds > 0) {
                        Button(
                            onClick = { onStopAndLog(elapsedMinutesToLog) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MilestoneEmerald),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("btn_stop_log_focus")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Log ${if (elapsedSeconds > 0) "$elapsedMinutesToLog min" else "Stop"}",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}
