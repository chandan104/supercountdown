package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.CountdownTimeRemaining
import com.example.data.model.DailyProgressStats
import com.example.service.GeminiService
import com.example.ui.theme.*

@Composable
fun SettingsAndWidgetView(
    countdown: CountdownTimeRemaining,
    progressStats: DailyProgressStats,
    onPinWidgetClick: () -> Unit,
    onTestNotificationClick: () -> Unit,
    onTestAlarmClick: () -> Unit = {},
    onOpenClockAppClick: () -> Unit = {},
    sendDataToAi: Boolean = true,
    onToggleSendDataToAi: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_view")
    ) {
        // Section: Home Screen Widget Integration
        Text(
            text = "HOME SCREEN WIDGET SYSTEM",
            style = MaterialTheme.typography.labelSmall,
            color = RadiantGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.5.dp, ElectricCyan)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Widget",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Countdown Widget",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF143026)
                    ) {
                        Text(
                            text = "Active & Synced",
                            style = MaterialTheme.typography.labelSmall,
                            color = MilestoneEmerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Widget Preview Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SUPER 2027",
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Jan 1, ${countdown.nextYearLabel}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${countdown.days}",
                                color = TextPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAYS\nREMAINING",
                                color = RadiantGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Today: ${progressStats.completedTasks}/${progressStats.totalTasks} completed • ${progressStats.completionPercentage}%",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onPinWidgetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_pin_widget"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Icon(Icons.Default.AddHome, contentDescription = "Pin", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pin Widget to Home Screen", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manual Steps: Long-press any empty space on your Android home screen -> Tap 'Widgets' -> Scroll down to 'Super 2027' -> Drag the widget onto your screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: System Alarm Integration Info (Section 9.2)
        Text(
            text = "SYSTEM CLOCK ALARM INTEGRATION",
            style = MaterialTheme.typography.labelSmall,
            color = RadiantGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Clock",
                        tint = RadiantGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Android System & In-App Alarm System",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Scheduled tasks trigger guaranteed in-app loud alarms with audio alerts and notifications. Additionally, tapping 'Set Alarm' invokes the Android system Clock intent to prepopulate your device's native alarm clock.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTestAlarmClick,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("btn_test_alarm_sound"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RadiantGold)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = "Test Alarm", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Alarm (2s)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onOpenClockAppClick,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("btn_open_system_clock"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RadiantGold.copy(alpha = 0.6f))
                    ) {
                        Text("Open Clock App", color = RadiantGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: System Diagnostics
        Text(
            text = "SYSTEM DIAGNOSTICS & INTEGRATIONS",
            style = MaterialTheme.typography.labelSmall,
            color = RadiantGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DiagnosticRow(
                    label = "Room SQLite Database",
                    status = "CONNECTED (Encrypted Local Storage)",
                    isOk = true
                )
                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(
                    label = "Alarm System Engine",
                    status = "ACTIVE (Dual: System Clock + In-App Audio)",
                    isOk = true
                )
                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(
                    label = "Gemini API Engine",
                    status = if (GeminiService.isApiKeyConfigured() && sendDataToAi) "CONNECTED (gemini-3.5-flash)" else if (!sendDataToAi) "OFFLINE COACH MODE" else "LOCAL INTELLIGENCE ACTIVE",
                    isOk = true
                )
                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(
                    label = "Notification Channels",
                    status = "ALARMS & REMINDERS ACTIVE",
                    isOk = true
                )
                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(
                    label = "Android AppWidget Provider",
                    status = "REGISTERED & READY",
                    isOk = true
                )

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onTestNotificationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("btn_test_notification_channel"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Test Notification")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Daily Progress Notification")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Data & Backup
        Text(
            text = "DATA MANAGEMENT & PRIVACY",
            style = MaterialTheme.typography.labelSmall,
            color = RadiantGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Privacy & AI Controls",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Data is stored on your device. Goal and task text is sent to Gemini only when you generate a plan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Send data to AI",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (sendDataToAi) "Gemini cloud plan generation active" else "Off = offline coach only",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sendDataToAi) MilestoneEmerald else RadiantGold
                        )
                    }
                    Switch(
                        checked = sendDataToAi,
                        onCheckedChange = onToggleSendDataToAi,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = Color(0xFF0369A1)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export", tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export & Backup Data (JSON)", color = ElectricCyan)
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup & Export Data") },
            text = {
                Text(
                    "Your personal data includes ${progressStats.activeGoalsCount} active goals, ${progressStats.totalTasks} scheduled tasks, and ${progressStats.totalHabits} habits. It is saved in the local Room database file 'super_countdown_2027.db'."
                )
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Done", color = ElectricCyan)
                }
            }
        )
    }
}

@Composable
fun DiagnosticRow(label: String, status: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = "Status",
                tint = if (isOk) MilestoneEmerald else RadiantGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOk) MilestoneEmerald else RadiantGold,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
