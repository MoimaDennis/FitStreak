package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val prefs = getSharedPreferences("fitstreak_prefs", MODE_PRIVATE)
        val streak = prefs.getInt("streak_count", 0)
        val xp = prefs.getInt("xp_total", 0)

        findViewById<android.widget.TextView>(R.id.tvStreakNumber).text = streak.toString()
        animateRing(findViewById(R.id.ringStreak), minOf(100, streak * 100 / 30))
        animateRing(findViewById(R.id.ringXp), (xp % 500) * 100 / 500)
        animateRing(findViewById(R.id.ringChallenge), 0)

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getWorkouts()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    findViewById<android.widget.TextView>(R.id.tvTodayWorkout).text = res.body()!![0].name
                }
            } catch (e: Exception) {
                findViewById<android.widget.TextView>(R.id.tvTodayWorkout).text = "Offline — showing cached data"
            }
        }

        findViewById<android.widget.LinearLayout>(R.id.cardTodayWorkout).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
    }

    private fun animateRing(ring: CircularProgressIndicator, targetProgress: Int) {
        ring.max = 100
        val animator = android.animation.ObjectAnimator.ofInt(ring, "progress", 0, targetProgress)
        animator.duration = 900
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
}