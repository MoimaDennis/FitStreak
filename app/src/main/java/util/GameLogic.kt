package com.example.fitstreak.util

import java.util.Locale

/**
 * Streak / XP / level rules. The PHP API uses the SAME formulas (see fitstreak-api/index.php)
 * so the app and the server always agree. Pure Kotlin so it can be unit tested.
 */
object GameLogic {
    const val XP_PER_LEVEL = 500
    const val STREAK_GOAL_DAYS = 30

    /** Level 1 starts at 0 XP, level 2 at 500 XP, level 3 at 1000 XP ... */
    fun levelFor(xp: Int): Int = (xp.coerceAtLeast(0) / XP_PER_LEVEL) + 1

    /** 0..99: how far the user is into the current level (drives the green ring). */
    fun levelProgressPercent(xp: Int): Int = (xp.coerceAtLeast(0) % XP_PER_LEVEL) * 100 / XP_PER_LEVEL

    /** 0..100: progress towards the 30-day streak goal (drives the orange ring). */
    fun streakProgressPercent(streak: Int): Int =
        (streak.coerceAtLeast(0) * 100 / STREAK_GOAL_DAYS).coerceAtMost(100)

    /** XP for a workout: 10 XP per minute x difficulty multiplier, minimum 10 XP. */
    fun xpForWorkout(durationSeconds: Int, difficulty: String): Int {
        val multiplier = when (difficulty.lowercase(Locale.ROOT)) {
            "easy" -> 1.0
            "medium" -> 1.5
            "hard" -> 2.0
            else -> 1.5
        }
        val raw = (durationSeconds / 60.0) * 10 * multiplier
        return Math.round(maxOf(10.0, raw)).toInt()
    }

    /** 65 -> "01:05" */
    fun formatTimer(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        return String.format(Locale.US, "%02d:%02d", s / 60, s % 60)
    }
}