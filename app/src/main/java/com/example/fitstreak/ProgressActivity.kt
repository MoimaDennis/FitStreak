package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.util.GameLogic
import com.example.fitstreak.util.Session
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ProgressActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_progress

    private lateinit var badgeAdapter: BadgeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val rv = findViewById<RecyclerView>(R.id.rvBadges)
        rv.layoutManager = LinearLayoutManager(this)
        badgeAdapter = BadgeAdapter(emptyList())
        rv.adapter = badgeAdapter

        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        render()
        lifecycleScope.launch {
            if (Session.refreshProfile(this@ProgressActivity)) render()
            try {
                val res = RetrofitClient.api.getBadges(Session.userId(this@ProgressActivity))
                if (res.isSuccessful) badgeAdapter.updateList(res.body() ?: emptyList())
            } catch (e: Exception) {
                Log.w(TAG, "Badges not loaded (offline?)", e)
            }
        }
    }

    private fun render() {
        val p = Session.prefs(this)
        val streak = p.getInt("streak_count", 0)
        val xp = p.getInt("xp_total", 0)
        findViewById<TextView>(R.id.tvCurrentStreak).text = streak.toString()
        findViewById<TextView>(R.id.tvLongestStreak).text = maxOf(p.getInt("longest_streak", 0), streak).toString()
        findViewById<TextView>(R.id.tvLevelXp).text = getString(R.string.fmt_level_xp, GameLogic.levelFor(xp), xp)
    }

    private companion object {
        const val TAG = "ProgressActivity"
    }
}