package com.example.fitstreak

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ProgressActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_progress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val prefs = getSharedPreferences("fitstreak_prefs", MODE_PRIVATE)
        val streak = prefs.getInt("streak_count", 0)
        val xp = prefs.getInt("xp_total", 0)
        val userId = prefs.getInt("user_id", 0)

        findViewById<android.widget.TextView>(R.id.tvCurrentStreak).text = streak.toString()
        findViewById<android.widget.TextView>(R.id.tvLongestStreak).text = streak.toString()
        findViewById<android.widget.TextView>(R.id.tvLevelXp).text = "Level ${xp / 500} — $xp XP"

        val rv = findViewById<RecyclerView>(R.id.rvBadges)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = BadgeAdapter(emptyList())
        rv.adapter = adapter

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getBadges(userId)
                if (res.isSuccessful) {
                    adapter.updateList(res.body() ?: emptyList())
                }
            } catch (e: Exception) { }
        }
    }
}