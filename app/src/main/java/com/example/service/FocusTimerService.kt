package com.example.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SuperCountdownApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FocusTimerState {
    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private val _currentTaskId = MutableStateFlow<String?>(null)
    val currentTaskId = _currentTaskId.asStateFlow()

    private val _currentTaskTitle = MutableStateFlow<String?>(null)
    val currentTaskTitle = _currentTaskTitle.asStateFlow()

    private val _targetMinutes = MutableStateFlow(25)
    val targetMinutes = _targetMinutes.asStateFlow()

    fun updateState(running: Boolean, paused: Boolean, seconds: Int, taskId: String?, title: String?, target: Int) {
        _isRunning.value = running
        _isPaused.value = paused
        _elapsedSeconds.value = seconds
        _currentTaskId.value = taskId
        _currentTaskTitle.value = title
        _targetMinutes.value = target
    }

    fun tick(seconds: Int) {
        _elapsedSeconds.value = seconds
    }

    fun reset() {
        _isRunning.value = false
        _isPaused.value = false
        _elapsedSeconds.value = 0
        _currentTaskId.value = null
        _currentTaskTitle.value = null
    }
}

class FocusTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var seconds = 0
    private var isPaused = false
    private var taskId: String? = null
    private var taskTitle: String? = null
    private var targetMins: Int = 25

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                taskId = intent.getStringExtra(EXTRA_TASK_ID)
                taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Focus Session"
                targetMins = intent.getIntExtra(EXTRA_TARGET_MINUTES, 25)
                seconds = 0
                isPaused = false

                startForegroundServiceNotification()
                startTicker()
            }
            ACTION_PAUSE -> {
                isPaused = true
                FocusTimerState.updateState(true, true, seconds, taskId, taskTitle, targetMins)
                updateNotification()
            }
            ACTION_RESUME -> {
                isPaused = false
                FocusTimerState.updateState(true, false, seconds, taskId, taskTitle, targetMins)
                updateNotification()
            }
            ACTION_STOP -> {
                stopTicker()
                FocusTimerState.reset()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            FocusTimerState.updateState(true, false, seconds, taskId, taskTitle, targetMins)
            while (isActive) {
                delay(1000)
                if (!isPaused) {
                    seconds++
                    FocusTimerState.tick(seconds)
                    if (seconds % 5 == 0) { // Update notification periodically
                        updateNotification()
                    }
                }
            }
        }
    }

    private fun stopTicker() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun startForegroundServiceNotification() {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): android.app.Notification {
        val mm = seconds / 60
        val ss = seconds % 60
        val timeFormatted = String.format("%02d:%02d", mm, ss)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isPaused) "Paused • $timeFormatted elapsed" else "$timeFormatted elapsed • Target: ${targetMins}m"

        return NotificationCompat.Builder(this, SuperCountdownApplication.CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Focus: ${taskTitle ?: "Task Session"}")
            .setContentText(statusText)
            .setOngoing(!isPaused)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "com.example.supercountdown.ACTION_START_TIMER"
        const val ACTION_PAUSE = "com.example.supercountdown.ACTION_PAUSE_TIMER"
        const val ACTION_RESUME = "com.example.supercountdown.ACTION_RESUME_TIMER"
        const val ACTION_STOP = "com.example.supercountdown.ACTION_STOP_TIMER"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TARGET_MINUTES = "extra_target_minutes"

        const val NOTIFICATION_ID = 2027

        fun startTimer(context: Context, taskId: String, taskTitle: String, targetMinutes: Int) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
                putExtra(EXTRA_TARGET_MINUTES, targetMinutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
