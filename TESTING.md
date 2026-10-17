# Testing Documentation

## Automated Tests Executed
1. **CountdownEngine Unit Tests** (`ExampleUnitTest.kt`):
   - Precision calculation of days, hours, minutes, and seconds.
   - Rollover logic when current timestamp passes January 1, 2027.
   - Elapsed target edge case handling.
   - Annual journey progress fraction calculation.

2. **RecommendationEngine Unit Tests** (`ExampleUnitTest.kt`):
   - Urgent priority task detection and deep work recommendation.
   - Habit momentum and streak celebration logic.
   - Velocity metrics calculation.

3. **Robolectric Local JVM Tests** (`ExampleRobolectricTest.kt`):
   - Context string resource verification (`R.string.app_name` matches "Super 2027").
   - Room Database instantiation and DAO availability.

## Running Tests
Run unit tests locally via command line:
```bash
gradle :app:testDebugUnitTest
```
