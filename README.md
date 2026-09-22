# FitStreak — Gamified Home Workout App

## Purpose
FitStreak is a mobile fitness app designed to help users build consistent home workout habits
through gamification. Rather than tracking raw stats, the app rewards daily consistency with
streaks, XP, levels, and badges, turning workout completion into a habit-forming loop similar
to Duolingo or Apple Fitness+.

## Design Considerations
The app uses a dark, high-contrast interface inspired by Apple Fitness+: a black background,
rounded dark cards, and a central three-ring visual on the Home Dashboard representing streak
progress (orange), XP/level progress (green), and daily challenge progress (cyan). Navigation
uses a persistent bottom bar shared across every screen through a common `BaseActivity`, so
every core feature — Library, Progress, Challenge, Settings — is reachable in a single tap from
anywhere in the app, and switching tabs never stacks duplicate screens on the back stack.

## Features Implemented in Part 2
- **Sign In** — user registration and login with JWT-based authentication; passwords are hashed
  server-side with bcrypt before storage.
- **REST API Integration** — a PHP (Slim Framework) + MySQL REST API handles authentication,
  workout data, streak/XP calculation, badges, and daily challenges.
- **Workout Library & Filtering** (User Defined Feature 1) — browse all workouts, filter by body
  part, intensity, and equipment.
- **Progress & Streak Tracking** (User Defined Feature 2) — current streak, longest streak, XP,
  level, and earned badges.
- **Daily Challenge & Leaderboard** (User Defined Feature 3) — a daily challenge with result
  submission and a live leaderboard of other users.
- **Settings** — unit preference (metric/imperial), notification toggle, and logout.

## Tech Stack
- **Android app:** Kotlin, Retrofit (networking), Room (offline cache), Material Components
- **Backend API:** PHP 8, Slim Framework 4, MySQL, JWT (firebase/php-jwt)
- **Hosting:** InfinityFree (API + database)
- **CI/CD:** GitHub Actions (automated build and unit tests on every push)

## Screenshots
<!-- Drag screenshot images directly into this GitHub editor to embed them, or add
     ![description](images/filename.png) once you've committed an /images folder -->

## Demo Video
<!-- Paste your unlisted YouTube link here once recorded -->

## AI Tool Usage
See [AI_USAGE.md](AI_USAGE.md) for a full breakdown of how AI tools were used during development.

## GitHub Actions
This repository runs automated unit tests and a debug build on every push via
`.github/workflows/build.yml`. Check the **Actions** tab for build status.
