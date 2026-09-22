package com.example.fitstreak.data

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/register")
    suspend fun register(@Body req: RegisterRequest): Response<AuthResponse>

    @POST("api/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @GET("api/workouts")
    suspend fun getWorkouts(
        @Query("body_part") bodyPart: String? = null,
        @Query("intensity") intensity: String? = null,
        @Query("equipment") equipment: String? = null
    ): Response<List<Workout>>

    @POST("api/workout-logs")
    suspend fun logWorkout(@Header("Authorization") token: String, @Body req: WorkoutLogRequest): Response<WorkoutLogResponse>

    @GET("api/badges/{user_id}")
    suspend fun getBadges(@Path("user_id") userId: Int): Response<List<Badge>>

    @GET("api/challenges/today")
    suspend fun getTodayChallenge(): Response<Challenge>

    @GET("api/challenges/{id}/leaderboard")
    suspend fun getLeaderboard(@Path("id") id: Int): Response<List<LeaderboardEntry>>

    @POST("api/challenges/{id}/submit")
    suspend fun submitChallenge(@Path("id") id: Int, @Header("Authorization") token: String, @Body body: Map<String, Int>): Response<Map<String, String>>

    @PUT("api/profile")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body req: SettingsRequest): Response<Map<String, String>>
}