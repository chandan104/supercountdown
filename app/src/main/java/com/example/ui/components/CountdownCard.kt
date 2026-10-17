package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountdownTimeRemaining
import com.example.data.model.EventItem
import com.example.ui.theme.*

@Composable
fun CountdownCard(
    countdown: CountdownTimeRemaining,
    events: List<EventItem>,
    selectedEventId: String?,
    onSelectEvent: (String) -> Unit,
    onAddEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeEvent = events.find { it.id == selectedEventId }
        ?: events.find { it.isDefaultNewYear }
        ?: events.firstOrNull()

    var showEventMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_countdown_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF131D33),
                            Color(0xFF0D1424)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Event Switcher Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showEventMenu = true }
                                .testTag("event_selector_chip"),
                            color = DeepSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (activeEvent?.isDefaultNewYear == true) Icons.Default.Celebration else Icons.Default.Event,
                                    contentDescription = "Event Icon",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeEvent?.title ?: "SUPER 2027",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Dropdown",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showEventMenu,
                            onDismissRequest = { showEventMenu = false },
                            modifier = Modifier.background(DeepSurface)
                        ) {
                            events.forEach { event ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = event.title,
                                                fontWeight = if (event.id == activeEvent?.id) FontWeight.Bold else FontWeight.Normal,
                                                color = if (event.id == activeEvent?.id) ElectricCyan else TextPrimary
                                            )
                                            if (event.subtitle.isNotBlank()) {
                                                Text(
                                                    text = event.subtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSelectEvent(event.id)
                                        showEventMenu = false
                                    }
                                )
                            }
                            HorizontalDivider(color = BorderSubtle)
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = RadiantGold)
                                },
                                text = { Text("+ Create Custom Event", color = RadiantGold, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    showEventMenu = false
                                    onAddEventClick()
                                }
                            )
                        }
                    }

                    // Year Badge / Target Subtitle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = if (activeEvent?.isDefaultNewYear == true) "TARGET: JAN 1, ${countdown.nextYearLabel}" else activeEvent?.category ?: "MILESTONE",
                            style = MaterialTheme.typography.labelSmall,
                            color = RadiantGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Subtitle
                Text(
                    text = activeEvent?.subtitle.takeIf { !it.isNullOrBlank() }
                        ?: "COUNTDOWN TO JANUARY 1, 2027",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // The 4-digit Countdown Digits Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeUnitBox(value = countdown.days, label = "DAYS")
                    Text(":", color = RadiantGold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    TimeUnitBox(value = countdown.hours, label = "HOURS")
                    Text(":", color = RadiantGold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    TimeUnitBox(value = countdown.minutes, label = "MINS")
                    Text(":", color = RadiantGold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    TimeUnitBox(value = countdown.seconds, label = "SECS", isAccented = true)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Bar: Journey to 2027
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Countdown progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(countdown.progressFractionOfYear * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { countdown.progressFractionOfYear },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ElectricCyan,
                        trackColor = Color(0xFF1E293B),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeUnitBox(
    value: Long,
    label: String,
    isAccented: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(68.dp, 64.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.5.dp,
                    color = if (isAccented) RadiantGold.copy(alpha = 0.8f) else BorderSubtle,
                    shape = RoundedCornerShape(14.dp)
                ),
            color = if (isAccented) Color(0xFF261E14) else Color(0xFF0F172A)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        slideInVertically { height -> height } togetherWith
                                slideOutVertically { height -> -height }
                    },
                    label = "time_box_$label"
                ) { targetVal ->
                    Text(
                        text = String.format("%02d", targetVal),
                        color = if (isAccented) RadiantGold else TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isAccented) RadiantGold else TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}
