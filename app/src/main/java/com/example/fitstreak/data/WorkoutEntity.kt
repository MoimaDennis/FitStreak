package com.example.fitstreak.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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

fun Workout.toEntity() = WorkoutEntity(workout_id, name, duration_minutes, intensity, body_part, equipment, video_url)
fun WorkoutEntity.toWorkout() = Workout(workout_id, name, duration_minutes, intensity, body_part, equipment, video_url)