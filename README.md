# Super 2027 Countdown

A high-performance personal productivity and goal-acceleration application engineered with Kotlin and Jetpack Compose. Super 2027 turns your remaining time before January 1, 2027, into measurable, compounding daily progress.

## Core Features

1. **Precision 2027 Countdown Engine**:
   - Dynamic real-time calculation of remaining days, hours, minutes, and seconds.
   - Handles local time zones, leap years, and New Year rollover (seamlessly transitions to subsequent New Years while preserving all history).
   - Visual year-progression indicator.

2. **Custom Events & Annual Milestones**:
   - Switch effortlessly between Super 2027, Christmas 2026, Q4 Mastery Sprint, and custom user-created events.

3. **Goal & Milestone Management**:
   - Measurable goals across Career, Health, Business, Personal Development, and Learning.
   - Individual personalized countdown timers for each goal and milestone checkpoint.
   - Stepper increments for quantifiable targets (e.g., workouts, apps shipped, pages read).

4. **Daily Tasks & Habit Streak Tracker**:
   - Scheduled tasks with estimated duration, priority levels, and quantifiable metrics.
   - Habit streak tracker with unbroken day counters and visual flame indicators.

5. **Android System Clock & Native In-App Reminders**:
   - **System Clock Integration**: One-tap "Set Alarm in Clock" launching `android.provider.AlarmClock.ACTION_SET_ALARM` with pre-filled time and label, prompting the user for confirmation.
   - **In-App Scheduled Reminders**: NotificationChannel-based alarms with action buttons ("Complete", "Snooze 15m").

6. **Home Screen AppWidget System**:
   - Native Android `AppWidgetProvider` showing live days remaining to 2027, today's priority task, and completion percentage.
   - One-tap "Pin Widget to Home Screen" via Android `requestPinAppWidget` (API 26+) and step-by-step launcher instructions.

7. **Gemini AI Productivity Coach & Offline Intelligence**:
   - Uses Gemini 3.5 Flash (`gemini-3.5-flash`) via direct REST API with 60-second OkHttp timeouts for structured Daily Action Plans and Goal Breakdowns.
   - Built-in heuristic intelligence fallback ensuring the app is always fully functional offline and without API keys.
   - Immediate push notifications delivering daily progress insights and remaining days to 2027.

8. **Discover & Skill Acceleration Feed**:
   - Curated high-signal frameworks and educational articles with key takeaways and bookmarks.

9. **Security, Diagnostics & Data Privacy**:
   - Local encrypted SQLite persistence via Room DB.
   - Diagnostic panel inspecting Room database health, API key status, alarm permissions, and AppWidget connectivity.
   - JSON export and backup generator.
