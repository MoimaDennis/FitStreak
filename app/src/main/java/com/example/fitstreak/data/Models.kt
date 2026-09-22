package com.example.fitstreak.data

data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val user_id: Int? = null, val user: User? = null, val token: String)
data class User(
    val user_id: Int, val username: String, val email: String,
    val streak_count: Int, val longest_streak: Int, val xp_total: Int,
    val level: Int, val units: String, val notifications_enabled: Int
)
data class Workout(
    val workout_id: Int, val name: String, val duration_minutes: Int,
    val intensity: String, val body_part: String, val equipment: String, val video_url: String?
)
data class WorkoutLogRequest(val workout_id: Int, val duration_seconds: Int, val difficulty_rating: String)
data class WorkoutLogResponse(val streak_count: Int, val xp_gained: Double, val xp_total: Double, val level: Int, val new_badges: List<String>)
data class Badge(val badge_name: String, val criteria: String, val earned_at: String)
data class Challenge(val challenge_id: Int, val description: String, val active_date: String)
data class LeaderboardEntry(val username: String, val result: Int)
data class SettingsRequest(val units: String, val notifications_enabled: Int)