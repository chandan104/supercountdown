package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import android.media.AudioAttributes
import android.media.RingtoneManager

class SuperCountdownApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        // Warm up Room DB
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(this@SuperCountdownApplication).eventDao().getDefaultNewYearEvent()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Alarm channel: Loud audio alarm sound, persistent vibration, high importance
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                CHANNEL_ID_ALARMS,
                "Super 2027 Alarms & Critical Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alarms and scheduled milestone alerts with sound and vibration"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(alarmSoundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(alarmChannel)

            // Reminder channel: Daily briefings and progress updates
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_ALARMS = "super_countdown_alarms"
        const val CHANNEL_ID_REMINDERS = "super_countdown_reminders"
    }
}
