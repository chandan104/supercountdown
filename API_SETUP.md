# API Setup & Configuration

## Gemini AI API Configuration

Super 2027 uses Google's `gemini-3.5-flash` model for intelligent daily planning and goal breakdowns.

### Steps to Configure in Google AI Studio Build:
1. Open the **Secrets panel** in the AI Studio interface.
2. Add your secret key named `GEMINI_API_KEY`.
3. The project's Secrets Gradle Plugin automatically injects `GEMINI_API_KEY` into `BuildConfig.GEMINI_API_KEY` at build time.
4. Verify `/.env.example` contains `GEMINI_API_KEY=MY_GEMINI_API_KEY`.

### Offline Intelligence Mode
If no API key is provided or if network connectivity is interrupted:
- `GeminiService` automatically transitions into **Offline Intelligence Mode**.
- Rich, personalized action plans and goal breakdowns are generated locally without crashing or blocking the user interface.
