package com.example.fitstreak.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM cached_workouts")
    suspend fun getAll(): List<WorkoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workouts: List<WorkoutEntity>)
}

