package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GoalItem
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.ui.components.*
import com.example.ui.theme.*

enum class NavigationTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.HourglassEmpty),
    GOALS("Goals", Icons.Default.Flag),
    TASKS("Tasks", Icons.Default.CheckCircle),
    AI_COACH("AI Coach", Icons.Default.AutoAwesome),
    DISCOVER("Discover", Icons.Default.Explore),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: SuperCountdownViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }

    val events by viewModel.events.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()
    val countdown by viewModel.currentCountdown.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    val progressStats by viewModel.progressStats.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val aiPlan by viewModel.aiPlan.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiGoalBreakdown by viewModel.aiGoalBreakdown.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog States
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedGoalForMilestone by remember { mutableStateOf<GoalItem?>(null) }
    var selectedGoalForAiBreakdown by remember { mutableStateOf<GoalItem?>(null) }
    var selectedTaskForTimer by remember { mutableStateOf<TaskItem?>(null) }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendProgressNotification()
        }
    }

    // React to user message toast
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("super_countdown_root"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RadiantGold,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("27", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SUPER 2027",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.sendProgressNotification()
                            }
                        },
                        modifier = Modifier.testTag("action_notify")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Send notification",
                            tint = RadiantGold
                        )
                    }
                    IconButton(
                        onClick = { viewModel.requestPinAppWidget() },
                        modifier = Modifier.testTag("action_widget_pin")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Pin Widget",
                            tint = ElectricCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicBackground)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepSurface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = RadiantGold,
                            indicatorColor = if (tab == NavigationTab.AI_COACH) RadiantGold else ElectricCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmicBackground)
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        countdown = countdown,
                        events = events,
                        selectedEventId = selectedEventId,
                        onSelectEvent = { viewModel.selectEvent(it) },
                        onAddEventClick = { showAddEventDialog = true },
                        progressStats = progressStats,
                        todayTasks = todayTasks,
                        habits = habits,
                        recommendations = recommendations,
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onToggleHabit = { viewModel.toggleHabitToday(it) },
                        onAddTaskClick = { showAddTaskDialog = true },
                        onAddHabitClick = { showAddHabitDialog = true },
                        onNavigateToAi = { selectedTab = NavigationTab.AI_COACH },
                        onTriggerAlarm = { h, m, label -> viewModel.triggerSystemAlarm(h, m, label) },
                        onOpenFocusTimer = { selectedTaskForTimer = it }
                    )
                }

                NavigationTab.GOALS -> {
                    GoalsScreen(
                        goals = goals,
                        milestones = milestones,
                        onAddGoalClick = { showAddGoalDialog = true },
                        onUpdateGoalProgress = { id, value -> viewModel.updateGoalProgress(id, value) },
                        onAddMilestoneClick = { goal -> selectedGoalForMilestone = goal },
                        onToggleMilestone = { viewModel.toggleMilestoneCompletion(it) },
                        onDeleteGoal = { viewModel.deleteGoal(it) },
                        onAiPlanClick = { goal ->
                            selectedGoalForAiBreakdown = goal
                            viewModel.requestGoalBreakdown(goal.title, goal.category)
                        }
                    )
                }

                NavigationTab.TASKS -> {
                    TasksScreen(
                        todayTasks = todayTasks,
                        habits = habits,
                        todayDateString = viewModel.todayDateString,
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onUpdateAmount = { task, amt -> viewModel.updateTaskAmount(task, amt) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onTriggerAlarm = { h, m, label -> viewModel.triggerSystemAlarm(h, m, label) },
                        onToggleHabit = { viewModel.toggleHabitToday(it) },
                        onAddTaskClick = { showAddTaskDialog = true },
                        onAddHabitClick = { showAddHabitDialog = true },
                        onOpenFocusTimer = { selectedTaskForTimer = it }
                    )
                }

                NavigationTab.AI_COACH -> {
                    AiCoachView(
                        aiPlan = aiPlan,
                        isAiLoading = isAiLoading,
                        recommendations = recommendations,
                        daysTo2027 = countdown.days,
                        onRequestAiPlan = { viewModel.requestAiDailyPlan() },
                        onSendNotification = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.sendProgressNotification()
                            }
                        }
                    )
                }

                NavigationTab.DISCOVER -> {
                    DiscoverView(
                        resources = resources,
                        onToggleBookmark = { viewModel.toggleBookmarkResource(it) },
                        onToggleComplete = { viewModel.toggleCompleteResource(it) }
                    )
                }

                NavigationTab.SETTINGS -> {
                    val sendDataToAi by viewModel.sendDataToAi.collectAsStateWithLifecycle()
                    SettingsAndWidgetView(
                        countdown = countdown,
                        progressStats = progressStats,
                        onPinWidgetClick = { viewModel.requestPinAppWidget() },
                        onTestNotificationClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.sendProgressNotification()
                            }
                        },
                        sendDataToAi = sendDataToAi,
                        onToggleSendDataToAi = { viewModel.setSendDataToAi(it) }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { t, d, c, gt, p, epoch, targetVal, u ->
                viewModel.addGoal(t, d, c, gt, p, epoch, targetVal, u)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            goals = goals,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { t, d, goalId, goalContribution, p, timeM, dur, isQ, targetA, u, rem, alarm ->
                viewModel.addTask(t, d, goalId, goalContribution, p, timeM, dur, isQ, targetA, u, rem, alarm)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { t, c, days ->
                viewModel.addHabit(t, c, days)
                showAddHabitDialog = false
            }
        )
    }

    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirm = { t, s, c, epoch, hex ->
                viewModel.addCustomEvent(t, s, c, epoch, hex)
                showAddEventDialog = false
            }
        )
    }

    selectedGoalForMilestone?.let { goal ->
        AddMilestoneDialog(
            goalTitle = goal.title,
            onDismiss = { selectedGoalForMilestone = null },
            onConfirm = { title, dueMillis ->
                viewModel.addMilestone(goal.id, title, dueMillis)
                selectedGoalForMilestone = null
            }
        )
    }

    aiGoalBreakdown?.let { breakdown ->
        selectedGoalForAiBreakdown?.let { goal ->
            AiGoalBreakdownDialog(
                goalTitle = goal.title,
                breakdown = breakdown,
                onDismiss = { selectedGoalForAiBreakdown = null },
                onApply = {
                    viewModel.applyGoalBreakdown(goal.id, breakdown)
                    selectedGoalForAiBreakdown = null
                }
            )
        }
    }

    selectedTaskForTimer?.let { task ->
        val isRunning by com.example.service.FocusTimerState.isRunning.collectAsStateWithLifecycle()
        val isPaused by com.example.service.FocusTimerState.isPaused.collectAsStateWithLifecycle()
        val elapsedSeconds by com.example.service.FocusTimerState.elapsedSeconds.collectAsStateWithLifecycle()
        val currentTaskId by com.example.service.FocusTimerState.currentTaskId.collectAsStateWithLifecycle()
        val context = androidx.compose.ui.platform.LocalContext.current

        com.example.ui.components.FocusTimerDialog(
            task = task,
            elapsedSeconds = if (currentTaskId == task.id) elapsedSeconds else 0,
            isRunning = if (currentTaskId == task.id) isRunning else false,
            isPaused = if (currentTaskId == task.id) isPaused else false,
            onStart = {
                com.example.service.FocusTimerService.startTimer(context, task.id, task.title, task.estimatedDurationMinutes)
            },
            onPause = {
                com.example.service.FocusTimerService.pauseTimer(context)
            },
            onResume = {
                com.example.service.FocusTimerService.resumeTimer(context)
            },
            onStopAndLog = { minutesLogged ->
                com.example.service.FocusTimerService.stopTimer(context)
                viewModel.updateTaskAmount(task, task.completedAmount + minutesLogged)
                selectedTaskForTimer = null
            },
            onDismiss = {
                selectedTaskForTimer = null
            }
        )
    }
}

@Composable
fun HomeScreen(
    countdown: com.example.data.model.CountdownTimeRemaining,
    events: List<com.example.data.model.EventItem>,
    selectedEventId: String?,
    onSelectEvent: (String) -> Unit,
    onAddEventClick: () -> Unit,
    progressStats: com.example.data.model.DailyProgressStats,
    todayTasks: List<TaskItem>,
    habits: List<com.example.data.model.HabitItem>,
    recommendations: List<com.example.domain.IntelligentRecommendation>,
    onToggleTask: (TaskItem) -> Unit,
    onToggleHabit: (com.example.data.model.HabitItem) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddHabitClick: () -> Unit,
    onNavigateToAi: () -> Unit,
    onTriggerAlarm: (Int, Int, String) -> Unit,
    onOpenFocusTimer: (TaskItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Countdown Card
        item {
            CountdownCard(
                countdown = countdown,
                events = events,
                selectedEventId = selectedEventId,
                onSelectEvent = onSelectEvent,
                onAddEventClick = onAddEventClick
            )
        }

        // Daily Velocity & Progress Bar
        item {
            DailyProgressMetricCard(
                stats = progressStats,
                daysRemaining = countdown.days
            )
        }

        // Habits Streak Row
        item {
            HabitStreakSection(
                habits = habits,
                todayDateString = com.example.data.local.getTodayDateString(),
                onToggleHabit = onToggleHabit,
                onAddHabitClick = onAddHabitClick
            )
        }

        // AI Briefing Quick Trigger
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DeepSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, RadiantGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✦", fontSize = 18.sp, color = RadiantGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI Productivity Coach Ready",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${recommendations.size} personalized recommendations computed",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Button(
                        onClick = onNavigateToAi,
                        colors = ButtonDefaults.buttonColors(containerColor = RadiantGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Open Coach", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Today's Priority Tasks Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S PRIORITIES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onAddTaskClick) {
                    Text("+ Add Task", color = ElectricCyan, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Task items
        if (todayTasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurface
                ) {
                    Text(
                        text = "No tasks scheduled for today. Tap '+ Add Task' to plan your momentum.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(todayTasks.take(4), key = { it.id }) { task ->
                val overlapWarning = remember(task, todayTasks) {
                    findTaskOverlapWarning(task, todayTasks)
                }
                TaskCard(
                    task = task,
                    onToggleCompletion = { onToggleTask(task) },
                    onUpdateAmount = { /* Handled in task tab */ },
                    onTriggerAlarmInClock = onTriggerAlarm,
                    onDeleteTask = { /* Handled in task tab */ },
                    onOpenFocusTimer = { onOpenFocusTimer(task) },
                    overlapWarning = overlapWarning
                )
            }
        }
    }
}

@Composable
fun DailyProgressMetricCard(
    stats: com.example.data.model.DailyProgressStats,
    daysRemaining: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAILY EXECUTION VELOCITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stats.completedTasks} of ${stats.totalTasks} Tasks Finished",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepSurface
                ) {
                    Text(
                        text = "${stats.completionPercentage}% Done",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RadiantGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { stats.completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = ElectricCyan,
                trackColor = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Habits: ${stats.completedHabits}/${stats.totalHabits}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "Active Goals: ${stats.activeGoalsCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "Milestones: ${stats.upcomingMilestonesCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GoalsScreen(
    goals: List<GoalItem>,
    milestones: List<com.example.data.model.MilestoneItem>,
    onAddGoalClick: () -> Unit,
    onUpdateGoalProgress: (String, Double) -> Unit,
    onAddMilestoneClick: (GoalItem) -> Unit,
    onToggleMilestone: (com.example.data.model.MilestoneItem) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onAiPlanClick: (GoalItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("goals_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANNUAL & PERSONAL GOALS",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mission Targets for 2027",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Button(
                    onClick = onAddGoalClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_goal")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Goal", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (goals.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurface
                ) {
                    Text(
                        text = "No goals created yet. Set high-conviction goals before 2027 by tapping '+ New Goal'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                val goalMilestones = milestones.filter { it.goalId == goal.id }
                GoalCard(
                    goal = goal,
                    milestones = goalMilestones,
                    onUpdateProgress = { onUpdateGoalProgress(goal.id, it) },
                    onAddMilestoneClick = { onAddMilestoneClick(goal) },
                    onToggleMilestone = onToggleMilestone,
                    onDeleteGoal = { onDeleteGoal(goal.id) },
                    onAiPlanClick = { onAiPlanClick(goal) }
                )
            }
        }
    }
}

@Composable
fun TasksScreen(
    todayTasks: List<TaskItem>,
    habits: List<com.example.data.model.HabitItem>,
    todayDateString: String,
    onToggleTask: (TaskItem) -> Unit,
    onUpdateAmount: (TaskItem, Double) -> Unit,
    onDeleteTask: (String) -> Unit,
    onTriggerAlarm: (Int, Int, String) -> Unit,
    onToggleHabit: (com.example.data.model.HabitItem) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddHabitClick: () -> Unit,
    onOpenFocusTimer: (TaskItem) -> Unit
) {
    var taskFilter by remember { mutableStateOf(TaskFilter.ALL) }

    val filteredTasks = when (taskFilter) {
        TaskFilter.ALL -> todayTasks
        TaskFilter.PENDING -> todayTasks.filter { it.status == TaskStatus.PENDING }
        TaskFilter.COMPLETED -> todayTasks.filter { it.status == TaskStatus.COMPLETED }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("tasks_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HabitStreakSection(
                habits = habits,
                todayDateString = todayDateString,
                onToggleHabit = onToggleHabit,
                onAddHabitClick = onAddHabitClick
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY'S SCHEDULE",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Daily Action Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Button(
                    onClick = onAddTaskClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_schedule_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filter chips: All, Pending, Completed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.values().forEach { filter ->
                    FilterChip(
                        selected = taskFilter == filter,
                        onClick = { taskFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurface
                ) {
                    Text(
                        text = "No tasks matching this filter. Add items to maintain daily discipline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                val overlapWarning = remember(task, todayTasks) {
                    findTaskOverlapWarning(task, todayTasks)
                }
                TaskCard(
                    task = task,
                    onToggleCompletion = { onToggleTask(task) },
                    onUpdateAmount = { onUpdateAmount(task, it) },
                    onTriggerAlarmInClock = onTriggerAlarm,
                    onDeleteTask = { onDeleteTask(task.id) },
                    onOpenFocusTimer = { onOpenFocusTimer(task) },
                    overlapWarning = overlapWarning
                )
            }
        }
    }
}

enum class TaskFilter(val label: String) {
    ALL("All Tasks"),
    PENDING("Pending"),
    COMPLETED("Completed")
}

fun findTaskOverlapWarning(task: TaskItem, allTasks: List<TaskItem>): String? {
    val startA = task.scheduledTimeMinutes ?: return null
    val durA = task.estimatedDurationMinutes.coerceAtLeast(1)
    val endA = startA + durA

    for (other in allTasks) {
        if (other.id == task.id) continue
        val startB = other.scheduledTimeMinutes ?: continue
        val durB = other.estimatedDurationMinutes.coerceAtLeast(1)
        val endB = startB + durB

        if (startA < endB && startB < endA) {
            val hour = startB / 60
            val min = startB % 60
            val timeStr = String.format("%d:%02d", hour, min)
            return "Overlaps with ${other.title} $timeStr"
        }
    }
    return null
}
