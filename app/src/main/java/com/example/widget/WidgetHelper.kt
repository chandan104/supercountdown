package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.getTodayDateString
import com.example.data.model.TaskStatus
import com.example.domain.CountdownEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetHelper {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, SuperCountdownAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                for (id in appWidgetIds) {
                    updateSingleWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    suspend fun updateSingleWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val db = AppDatabase.getInstance(context)
        val defaultEvent = db.eventDao().getDefaultNewYearEvent()
        val targetMillis = defaultEvent?.targetEpochMillis ?: 1798761600000L // 2027-01-01 00:00:00
        val countdown = CountdownEngine.calculateCountdown(
            targetEpochMillis = targetMillis,
            isDefaultNewYear = true
        )

        val today = getTodayDateString()
        val tasks = db.taskDao().getTasksForDateSync(today)
        val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
        val totalCount = tasks.size
        val completionPct = if (totalCount > 0) (completedCount * 100) / totalCount else 0
        val topIncomplete = tasks.firstOrNull { it.status == TaskStatus.PENDING }

        val views = RemoteViews(context.packageName, R.layout.widget_super_countdown).apply {
            setTextViewText(R.id.widget_event_title, defaultEvent?.title ?: "SUPER 2027")
            setTextViewText(R.id.widget_event_target, "Jan 1, ${countdown.nextYearLabel}")
            setTextViewText(R.id.widget_days_number, "${countdown.days}")
            setTextViewText(R.id.widget_days_label, "DAYS\nREMAINING")

            val summaryText = if (totalCount > 0) {
                "Today: $completedCount/$totalCount completed • $completionPct%"
            } else {
                "No tasks scheduled today • Tap to plan"
            }
            setTextViewText(R.id.widget_progress_summary, summaryText)

            val priorityText = if (topIncomplete != null) {
                "★ Priority: ${topIncomplete.title}"
            } else if (totalCount > 0 && completedCount == totalCount) {
                "✓ All daily tasks completed! Exceptional focus."
            } else {
                "✦ Add tasks or ask AI Coach for a daily plan"
            }
            setTextViewText(R.id.widget_priority_task, priorityText)

            // Deep link into application
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * Request OS to pin the widget directly to home screen (Android 8.0+ / API 26+)
     */
    fun requestPinWidget(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(context, SuperCountdownAppWidgetProvider::class.java)

            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
                return true
            }
        }
        return false
    }
}
