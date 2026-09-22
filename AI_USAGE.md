# AI Tool Usage

AI assistance (Claude, by Anthropic) was used throughout the development of this Part 2
prototype to help plan structure, generate boilerplate code, and debug build errors. AI was
not used to design the app's core logic or feature set — those were defined in the Part 1
planning document prior to any AI assistance.

## Where AI was used

- **REST API structure**: Generated the initial PHP/Slim route handlers for authentication
  (register/login with JWT), workout retrieval and filtering, the streak/XP/badge calculation
  logic, and the daily challenge/leaderboard endpoints, based on the schema and business rules
  defined in the Part 1 planning document.
- **Android/Kotlin boilerplate**: Generated Retrofit interface definitions, data model classes,
  RecyclerView adapters, and the Room database setup for offline workout caching.
- **UI implementation**: Generated the dark theme (colors, styles) and the three-ring streak
  visualization on the Home Dashboard, and the shared `BaseActivity` pattern used for consistent
  bottom navigation across all screens.
- **CI/CD configuration**: Generated the GitHub Actions workflow file for automated unit testing
  and building the debug APK on every push.
- **Debugging**: AI assisted in resolving several build environment issues, including
  [describe ONE real issue you personally hit and fixed — e.g. "a KSP2/Room annotation processor
  bug that caused a 'java.lang.IllegalStateException: unexpected jvm signature V' error, resolved
  by upgrading the Room library version" — write this in your own words based on what actually
  happened].

## What was not AI-generated

The app's feature set, database schema, gamification logic (streak/XP/level formulas, badge
criteria), and overall screen flow were designed independently in the Part 1 planning and
research document, before any AI tool was used. All AI-suggested code was reviewed, tested, and
adapted by the student before inclusion in the final submission.
