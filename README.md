# FitStreak – Gamified Home Workout App

!\[Build](https://github.com/MoimaDennis/FitStreak/actions/workflows/build.yml/badge.svg)



## Demo video

▶ **YouTube (unlisted): PASTE-YOUR-VIDEO-LINK-HERE**

## Purpose

FitStreak helps people build a consistent home-workout habit through gamification. Instead of raw statistics, it rewards daily consistency with streaks, XP, levels and badges – the same habit loop used by Duolingo and Apple Fitness+.

## Design considerations

* **Dark, high-contrast theme** (black background, rounded dark cards) so the three rings stand out: orange = streak, green = XP/level, cyan = weekly goal.
* **One shared bottom navigation bar** (`BaseActivity`) so every main feature is one tap away and tabs never stack up on the back button.
* **Consistent styles** (`Headline.Big`, `Label.Caps`, `Card.Rounded`, `Btn.Primary`, `Input.Dark` in `themes.xml`) on every screen.
* **Forgiving input handling:** every field is validated before the API is called, errors appear on the field, and every API/network error is shown as a translated message – the app never crashes on bad input.
* **Offline first:** the app keeps working without internet (cached workout library, queued workouts).
* **Three languages:** English, Afrikaans, isiZulu.

## Screenshots

|Login|Home|Library|Progress|Settings|
|-|-|-|-|-|
|!\[](screenshots/login.png)|!\[](screenshots/home.png)|!\[](screenshots/library.png)|!\[](screenshots/progress.png)|!\[](screenshots/settings.png)|

## Features

|Feature|Details|
|-|-|
|Register / Login|JWT sessions; passwords hashed with bcrypt on the server|
|**Single sign-on (Part 3)**|Google sign-in via Firebase Authentication + Credential Manager; API exchanges the Firebase ID token for a FitStreak session|
|Settings|Notifications on/off, language, weekly goal, account info, log out|
|REST API|PHP + MySQL (see table below)|
|**Offline mode + sync (Part 3)**|Room database queues finished workouts when offline; `SyncManager` uploads them when the phone reconnects; library cached for offline use|
|**Real-time notifications (Part 3)**|Firebase Cloud Messaging; topic `fitstreak\_all`; token saved in the database|
|**Multi-language (Part 3)**|English, Afrikaans, isiZulu (`values`, `values-af`, `values-zu`)|
|Workout Library (UD1)|Filter by body part, intensity, equipment|
|Progress \& Streaks (UD2)|Streak, longest streak, XP, level, badges|
|Daily Challenge (UD3)|Daily challenge, result submission, leaderboard|
|**Workout History (UD4)**|Last 50 workouts with date, duration and XP|
|**Weekly Goal (UD5)**|Choose 1–7 workouts per week; ring + text on Home|

## Tech stack

Kotlin · Retrofit · Room · Firebase Authentication \& Cloud Messaging · Credential Manager · Material Components · PHP 8 + MySQL (PDO) · JWT (HS256) · GitHub Actions

## REST API (`fitstreak-api/index.php`)

|Method|Endpoint|Auth|Purpose|
|-|-|-|-|
|POST|`/api/register`|–|Create account|
|POST|`/api/login`|–|Login, returns JWT|
|POST|`/api/sso`|–|Google/Firebase sign-in, returns JWT|
|GET / PUT|`/api/profile`|JWT|Read / update settings|
|PUT|`/api/fcm-token`|JWT|Save push token|
|GET|`/api/workouts?body\_part=\&intensity=\&equipment=`|–|Workout library|
|POST|`/api/workout-logs`|JWT|Log workout (streak, XP, badges)|
|GET|`/api/workout-logs`|JWT|Workout history|
|GET|`/api/badges/{user\_id}`|–|Earned badges|
|GET|`/api/challenges/today`|–|Daily challenge|
|GET / POST|`/api/challenges/{id}/leaderboard`, `/submit`|JWT for POST|Leaderboard / submit result|

## How to run

1. Open the project in Android Studio and let Gradle sync.
2. Put your own `app/google-services.json` (Firebase) in the `app` folder.
3. API: run `fitstreak-api/migration\_part3.sql` once in phpMyAdmin, copy `config.sample.php` to `config.php` on the server with your values, and upload `index.php`.
4. Run on a real phone (USB debugging on).

## GitHub and GitHub Actions

* Work was committed regularly (see the commit history); the final commit is tagged **`final-PoE`**.
* `.github/workflows/build.yml` runs on every push and pull request: it sets up JDK 17, runs the **unit tests** (`./gradlew testDebugUnitTest`), builds the **debug APK**, and uploads the test report and APK as downloadable artifacts. A green tick on a commit means the code builds and the tests pass on a clean machine, not just mine.
* Secrets (keystore, `config.php`) are listed in `.gitignore` and never committed.

## Automated tests

`app/src/test/java/...` – `ValidatorsTest`, `GameLogicTest`, `WorkoutFilterTest` (streak/XP/level rules, input validation, offline filtering). Run with `./gradlew testDebugUnitTest`.

## Release notes

See [RELEASE\_NOTES.md](RELEASE_NOTES.md).

## Google Play Store preparation

Signed APK/AAB, 512×512 icon, feature graphic, screenshots, listing text and privacy policy are in [`/PlayStore`](PlayStore) and [PRIVACY.md](PRIVACY.md).

## AI tool usage

See [AI\_USAGE.md](AI_USAGE.md).

## References

* Firebase. 2025. *Firebase Cloud Messaging*. Available at: https://firebase.google.com/docs/cloud-messaging \[Accessed 3 November 2025].
* GitHub. 2025. *Automated build Android app with GitHub Action*. Available at: https://github.com/marketplace/actions/automated-build-android-app-with-github-action \[Accessed 3 November 2025].

