# Platform Specifics & Limitations

## Android Integration Details

1. **System Clock Alarm (`AlarmClock.ACTION_SET_ALARM`)**:
   - Android policy strictly prohibits third-party applications from silently overwriting or modifying alarms in the native Clock app.
   - Super 2027 uses the supported public intent with `EXTRA_SKIP_UI = false`, ensuring the user reviews and confirms the alarm in their system Clock.
   - If a custom Android ROM lacks a default Clock app, an honest fallback message explains manual setup instructions.

2. **Exact Alarms (`SCHEDULE_EXACT_ALARM`)**:
   - On Android 12+ (API 31+), exact alarms require permission checking. If exact alarm permission is revoked by the user, the app automatically falls back to inexact reminders via `AlarmManager.setAndAllowWhileIdle()`.

3. **Home Screen AppWidget**:
   - Widgets update on standard OS broadcast cycles (`APPWIDGET_UPDATE`) and on every task or event modification via internal broadcast triggers.
   - Pinned widget support (`requestPinAppWidget`) is supported on Android 8.0+ (API 26+). Manual launcher instructions are displayed for older operating systems or custom launchers.

4. **Notifications (`POST_NOTIFICATIONS`)**:
   - On Android 13+ (API 33+), runtime notification permission is requested gracefully when the user first triggers a reminder or requests daily progress push insights.
