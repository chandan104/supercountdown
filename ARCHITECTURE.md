# Architecture: Super 2027 Countdown

## Overview
Super 2027 Countdown follows modern Android Clean Architecture and MVVM (Model-View-ViewModel) patterns utilizing Kotlin Coroutines, StateFlow, Room Database, and Jetpack Compose Material 3.

```
com.example
├── MainActivity.kt                      // Entry point, Compose host, permission handling
├── SuperCountdownApplication.kt         // NotificationChannel creation, DB warmup
├── data
│   ├── model/Models.kt                  // Entities: EventItem, GoalItem, MilestoneItem, TaskItem, HabitItem, etc.
│   └── local
│       ├── Converters.kt                // Room TypeConverters
│       ├── Dao.kt                       // EventDao, GoalDao, MilestoneDao, TaskDao, HabitDao, ResourceDao
│       └── AppDatabase.kt               // Room Database singleton & pre-seeded defaults
├── domain
│   ├── CountdownEngine.kt               // Precision countdown, rollover, year progress calculations
│   └── RecommendationEngine.kt          // Algorithmic intelligence analyzing urgency, velocity, and streaks
├── service
│   ├── GeminiService.kt                 // Gemini 3.5 Flash REST client + offline intelligence fallback
│   ├── AlarmAndReminderManager.kt       // AlarmClock intent + AlarmManager scheduler
│   └── ReminderBroadcastReceiver.kt     // Notification broadcast handler & quick actions
├── widget
│   ├── SuperCountdownAppWidgetProvider.kt // Native Android AppWidgetProvider receiver
│   └── WidgetHelper.kt                  // RemoteViews database synchronization & pinning
└── ui
    ├── SuperCountdownViewModel.kt       // Centralized state container with 1-second dynamic ticker
    ├── MainAppScreen.kt                 // Bottom navigation tabs coordination & dialog router
    ├── theme/                           // Luxury dark theme tokens (Theme.kt, Color.kt, Type.kt)
    └── components/
        ├── CountdownCard.kt             // Animated timer, event switcher dropdown, year progress
        ├── GoalCards.kt                 // Goal progress, milestone countdowns, AI plan button
        ├── TaskAndHabitCards.kt         // Daily tasks, quantifiable stepper, system clock alarm
        ├── AiCoachView.kt               // Gemini daily plan, intelligence recommendations, push trigger
        ├── DiscoverView.kt              // Curated educational feed
        ├── SettingsAndWidgetView.kt     // Widget pin action, diagnostics table, privacy, export
        └── Dialogs.kt                   // Input modals for goals, milestones, tasks, events, and AI
```

## Data Persistence & Zero-Cloud Privacy
- All user data is persisted in a local Room SQLite database (`super_countdown_2027.db`).
- Changes made in the app immediately synchronize with the native Android AppWidget through `WidgetHelper.updateAllWidgets()`.

## Native OS Integrations
- **Android System Clock**: Uses `Intent(AlarmClock.ACTION_SET_ALARM)` with `EXTRA_SKIP_UI = false` to respect user control.
- **In-App Scheduled Reminders**: Uses `AlarmManager.setExactAndAllowWhileIdle()` with inexact fallback.
- **Home Screen AppWidget**: Uses `RemoteViews` and `AppWidgetManager.requestPinAppWidget()` (API 26+).
