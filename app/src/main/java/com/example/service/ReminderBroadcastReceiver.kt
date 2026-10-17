package com.example.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SuperCountdownApplication
import com.example.data.local.AppDatabase
import com.example.data.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getStringExtra(AlarmAndReminderManager.EXTRA_TASK_ID) ?: ""
        val taskTitle = intent.getStringExtra(AlarmAndReminderManager.EXTRA_TASK_TITLE) ?: "Task Reminder"

        when (action) {
            AlarmAndReminderManager.ACTION_TRIGGER_REMINDER -> {
                val notificationId = taskId.hashCode()

                // Intent to open App
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val openPendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Quick Action: Complete Task
                val completeIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                    this.action = AlarmAndReminderManager.ACTION_COMPLETE_TASK_NOTIFICATION
                    putExtra(AlarmAndReminderManager.EXTRA_TASK_ID, taskId)
                }
                val completePendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + 1,
                    completeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Quick Action: Snooze 15 Min
                val snoozeIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                    this.action = AlarmAndReminderManager.ACTION_SNOOZE_TASK
                    putExtra(AlarmAndReminderManager.EXTRA_TASK_ID, taskId)
                    putExtra(AlarmAndReminderManager.EXTRA_TASK_TITLE, taskTitle)
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + 2,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)

                val builder = NotificationCompat.Builder(context, SuperCountdownApplication.CHANNEL_ID_ALARMS)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Super 2027 Alarm: $taskTitle")
                    .setContentText("It's time to execute this scheduled task and protect your 2027 momentum!")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("It's time to execute: '$taskTitle'. Stay committed to your 2027 goals!"))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSound(alarmSoundUri)
                    .setVibrate(vibrationPattern)
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                    .setContentIntent(openPendingIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_launcher_foreground, "Complete", completePendingIntent)
                    .addAction(R.drawable.ic_launcher_foreground, "Snooze 15m", snoozePendingIntent)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, builder.build())
            }

            AlarmAndReminderManager.ACTION_COMPLETE_TASK_NOTIFICATION -> {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(taskId.hashCode())

                if (taskId.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getInstance(context)
                        val task = db.taskDao().getTaskById(taskId)
                        if (task != null) {
                            db.taskDao().updateTask(
                                task.copy(
                                    status = TaskStatus.COMPLETED,
                                    completedAtEpochMillis = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }

            AlarmAndReminderManager.ACTION_SNOOZE_TASK -> {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(taskId.hashCode())

                if (taskId.isNotBlank()) {
                    val snoozeTime = System.currentTimeMillis() + 15 * 60 * 1000
                    AlarmAndReminderManager.scheduleInAppReminder(
                        context,
                        taskId,
                        "$taskTitle (Snoozed)",
                        snoozeTime
                    )
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // Restore scheduled tasks
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(context)
                    val tasks = db.taskDao().getTopIncompleteTasksSync(com.example.data.local.getTodayDateString())
                    for (task in tasks) {
                        if ((task.reminderEnabled || task.alarmEnabled) && task.scheduledTimeMinutes != null) {
                            val cal = java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.HOUR_OF_DAY, task.scheduledTimeMinutes / 60)
                                set(java.util.Calendar.MINUTE, task.scheduledTimeMinutes % 60)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                                if (timeInMillis <= System.currentTimeMillis()) {
                                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                                }
                            }
                            AlarmAndReminderManager.scheduleInAppReminder(
                                context,
                                task.id,
                                task.title,
                                cal.timeInMillis
                            )
                        }
                    }
                }
            }
        }
    }
}
