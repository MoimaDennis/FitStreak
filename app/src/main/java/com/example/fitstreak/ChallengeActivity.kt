package com.example.fitstreak

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ChallengeActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_challenge

    private var currentChallengeId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val prefs = getSharedPreferences("fitstreak_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val rv = findViewById<RecyclerView>(R.id.rvLeaderboard)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = LeaderboardAdapter(emptyList())
        rv.adapter = adapter

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getTodayChallenge()
                if (res.isSuccessful && res.body() != null) {
                    val challenge = res.body()!!
                    currentChallengeId = challenge.challenge_id
                    findViewById<android.widget.TextView>(R.id.tvChallengeDesc).text = challenge.description

                    val lb = RetrofitClient.api.getLeaderboard(currentChallengeId)
                    if (lb.isSuccessful) {
                        adapter.updateList(lb.body() ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                findViewById<android.widget.TextView>(R.id.tvChallengeDesc).text = "No challenge available"
            }
        }

        findViewById<android.widget.Button>(R.id.btnSubmit).setOnClickListener {
            val resultText = findViewById<android.widget.EditText>(R.id.etResult).text.toString()
            if (resultText.isEmpty() || currentChallengeId == 0) {
                Toast.makeText(this, "Enter a result first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = resultText.toIntOrNull() ?: 0
            lifecycleScope.launch {
                try {
                    val res = RetrofitClient.api.submitChallenge(currentChallengeId, "Bearer $token", mapOf("result" to result))
                    if (res.isSuccessful) {
                        Toast.makeText(this@ChallengeActivity, "Result submitted!", Toast.LENGTH_SHORT).show()
                        val lb = RetrofitClient.api.getLeaderboard(currentChallengeId)
                        if (lb.isSuccessful) adapter.updateList(lb.body() ?: emptyList())
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ChallengeActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}