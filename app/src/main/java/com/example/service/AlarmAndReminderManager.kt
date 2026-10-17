package com.example.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.SuperCountdownApplication

object AlarmAndReminderManager {

    const val ACTION_TRIGGER_REMINDER = "com.example.supercountdown.ACTION_TRIGGER_REMINDER"
    const val ACTION_COMPLETE_TASK_NOTIFICATION = "com.example.supercountdown.ACTION_COMPLETE_TASK_NOTIFICATION"
    const val ACTION_SNOOZE_TASK = "com.example.supercountdown.ACTION_SNOOZE_TASK"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"

    /**
     * Section 9.2 Android System Clock Integration:
     * Opens the Android system Clock application with prepopulated time and title.
     * EXTRA_SKIP_UI = false ensures the user reviews and confirms in their clock application.
     * Includes fallback to ACTION_SHOW_ALARMS if ACTION_SET_ALARM is not directly supported.
     */
    fun openSystemClockAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        message: String
    ): Boolean {
        // Try direct SET_ALARM first
        try {
            val setAlarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false) // Prompt user confirmation in Clock
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(setAlarmIntent)
            return true
        } catch (_: Exception) {
            // Fallback to showing alarms in the system clock app
            return try {
                val showAlarmsIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(showAlarmsIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun openSystemClockApp(context: Context): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isExactAlarmPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    /**
     * Native in-app task reminder scheduling using AlarmManager and BroadcastReceiver.
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleInAppReminder(
        context: Context,
        taskId: String,
        taskTitle: String,
        triggerEpochMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }

        val requestCode = taskId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ensure trigger time is in the future
        val actualTriggerMillis = if (triggerEpochMillis <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 1000L
        } else {
            triggerEpochMillis
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        actualTriggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        actualTriggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    actualTriggerMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            // Inexact fallback
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    actualTriggerMillis,
                    pendingIntent
                )
            } catch (_: Exception) {
                // Graceful fallback
            }
        }
    }

    /**
     * Test utility to ring a real test alarm in 2 seconds.
     */
    fun triggerTestAlarm(context: Context): Boolean {
        val testId = "test_alarm_${System.currentTimeMillis()}"
        val triggerTime = System.currentTimeMillis() + 2000L
        scheduleInAppReminder(
            context = context,
            taskId = testId,
            taskTitle = "Super 2027 Milestone Focus Alarm",
            triggerEpochMillis = triggerTime
        )
        return true
    }

    fun cancelInAppReminder(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
        }
        val requestCode = taskId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    @SuppressLint("MissingPermission")
    fun postProgressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String
    ) {
        try {
            val builder = NotificationCompat.Builder(context, SuperCountdownApplication.CHANNEL_ID_REMINDERS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: Exception) {
            // Graceful permission check
        }
    }
}
