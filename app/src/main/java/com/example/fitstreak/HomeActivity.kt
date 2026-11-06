package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.AppDatabase
import com.example.fitstreak.data.FcmHelper
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.SyncManager
import com.example.fitstreak.data.toWorkout
import com.example.fitstreak.util.GameLogic
import com.example.fitstreak.util.Session
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        askNotificationPermission()
        FcmHelper.setup(this)

        findViewById<LinearLayout>(R.id.cardTodayWorkout).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        render()
        refresh()
    }

    /** Draws the rings and numbers from the saved data (works offline). */
    private fun render() {
        val streak = Session.prefs(this).getInt("streak_count", 0)
        val xp = Session.prefs(this).getInt("xp_total", 0)
        val goal = Session.weeklyGoal(this)
        val week = Session.weekCount(this)

        findViewById<TextView>(R.id.tvStreakNumber).text = streak.toString()
        animateRing(findViewById(R.id.ringStreak), GameLogic.streakProgressPercent(streak))
        animateRing(findViewById(R.id.ringXp), GameLogic.levelProgressPercent(xp))
        animateRing(findViewById(R.id.ringChallenge), minOf(100, week * 100 / goal))

        findViewById<TextView>(R.id.tvWeekly).text =
            if (week >= goal) getString(R.string.msg_goal_reached)
            else getString(R.string.fmt_weekly_progress, week, goal)
    }

    /** Uploads queued workouts, refreshes streak/XP from the API, loads today's workout. */
    private fun refresh() {
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(this@HomeActivity).workoutDao()
            val tvToday = findViewById<TextView>(R.id.tvTodayWorkout)
            val tvSync = findViewById<TextView>(R.id.tvSyncStatus)

            SyncManager.syncPending(this@HomeActivity)
            if (Session.refreshProfile(this@HomeActivity)) render()

            try {
                val res = RetrofitClient.api.getWorkouts()
                val list = res.body()
                if (res.isSuccessful && !list.isNullOrEmpty()) {
                    // a different workout each day
                    tvToday.text = list[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % list.size].name
                }
            } catch (e: Exception) {
                Log.w(TAG, "Offline - using cached workouts", e)
                val cached = dao.getAll().map { it.toWorkout() }
                tvToday.text = if (cached.isNotEmpty()) cached[0].name else getString(R.string.msg_offline_cached)
            }

            val pending = dao.pendingCount()
            tvSync.visibility = if (pending > 0) View.VISIBLE else View.GONE
            tvSync.text = getString(R.string.fmt_pending_sync, pending)
        }
    }

    private fun animateRing(ring: CircularProgressIndicator, target: Int) {
        ring.max = 100
        ring.setProgressCompat(target, true)
    }

    private companion object {
        const val TAG = "HomeActivity"
    }
}