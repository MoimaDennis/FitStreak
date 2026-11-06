package com.example.fitstreak.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WorkoutDao {
    // ---- cached workouts (Library works offline) ----
    @Query("SELECT * FROM cached_workouts")
    suspend fun getAll(): List<WorkoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workouts: List<WorkoutEntity>)

    // ---- PART 3: offline queue ----
    @Insert
    suspend fun insertPending(item: PendingLogEntity)

    @Query("SELECT * FROM pending_logs ORDER BY id")
    suspend fun getPending(): List<PendingLogEntity>

    @Query("SELECT COUNT(*) FROM pending_logs")
    suspend fun pendingCount(): Int

    @Query("DELETE FROM pending_logs WHERE id = :id")
    suspend fun deletePending(id: Int)

    @Query("DELETE FROM pending_logs")
    suspend fun clearPending()
}

