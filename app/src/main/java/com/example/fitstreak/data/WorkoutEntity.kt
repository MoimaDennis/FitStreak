package com.example.fitstreak.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached copy of a workout so the Library works offline. */
@Entity(tableName = "cached_workouts")
data class WorkoutEntity(
    @PrimaryKey val workout_id: Int,
    val name: String,
    val duration_minutes: Int,
    val intensity: String,
    val body_part: String,
    val equipment: String,
    val video_url: String?
)

/** PART 3 (offline mode): a finished workout that could not be uploaded yet. */
@Entity(tableName = "pending_logs")
data class PendingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workout_id: Int,
    val duration_seconds: Int,
    val difficulty_rating: String,
    val created_at: Long = System.currentTimeMillis()
)

fun Workout.toEntity() = WorkoutEntity(workout_id, name, duration_minutes, intensity, body_part, equipment, video_url)
fun WorkoutEntity.toWorkout() = Workout(workout_id, name, duration_minutes, intensity, body_part, equipment, video_url)