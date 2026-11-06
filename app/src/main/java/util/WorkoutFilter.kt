package com.example.fitstreak.util

import com.example.fitstreak.data.Workout

/**
 * Filters a workout list by body part / intensity / equipment (null = "All").
 * Used when the phone is OFFLINE so the filters still work on the cached list.
 */
object WorkoutFilter {
    fun apply(
        workouts: List<Workout>,
        bodyPart: String?,
        intensity: String?,
        equipment: String?
    ): List<Workout> = workouts.filter {
        (bodyPart == null || it.body_part.equals(bodyPart, ignoreCase = true)) &&
                (intensity == null || it.intensity.equals(intensity, ignoreCase = true)) &&
                (equipment == null || it.equipment.equals(equipment, ignoreCase = true))
    }
}