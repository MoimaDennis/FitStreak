package com.example.fitstreak.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.fitstreak.util.Session
import java.util.concurrent.atomic.AtomicBoolean

/**
 * PART 3 - OFFLINE MODE WITH SYNC.
 * Finished workouts that could not be uploaded are saved in Room (table pending_logs).
 * When the phone is online again, syncPending() uploads them to the API in the original order.
 */
object SyncManager {
    private const val TAG = "SyncManager"
    private val running = AtomicBoolean(false)

    /** True when the phone currently has an internet-capable network. */
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Uploads every queued workout. Returns how many were synced. Safe to call many times. */
    suspend fun syncPending(context: Context): Int {
        val app = context.applicationContext
        if (!Session.isLoggedIn(app)) return 0
        if (!running.compareAndSet(false, true)) {
            Log.d(TAG, "Sync already running - skipped")
            return 0
        }
        var synced = 0
        try {
            val dao = AppDatabase.getInstance(app).workoutDao()
            val pending = dao.getPending()
            Log.d(TAG, "Pending workouts to sync: ${pending.size}")
            for (item in pending) {
                val res = RetrofitClient.api.logWorkout(
                    Session.bearer(app),
                    WorkoutLogRequest(item.workout_id, item.duration_seconds, item.difficulty_rating)
                )
                val body = res.body()
                if (res.isSuccessful && body != null) {
                    dao.deletePending(item.id)
                    Session.saveWorkoutResult(app, body)
                    synced++
                    Log.d(TAG, "Synced pending workout #${item.id}")
                } else if (res.code() == 401 || res.code() >= 500) {
                    Log.w(TAG, "Sync paused (HTTP ${res.code()}) - will retry later")
                    break
                } else {
                    // 4xx other than 401: the server will never accept this item, drop it
                    Log.w(TAG, "Dropping pending workout #${item.id} (HTTP ${res.code()})")
                    dao.deletePending(item.id)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sync stopped - network problem, will retry when online", e)
        } finally {
            running.set(false)
        }
        return synced
    }
}