package com.example.fitstreak

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.util.Session
import com.example.fitstreak.util.Validators
import com.example.fitstreak.util.apiErrorText
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.IOException

class ChallengeActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_challenge

    private var currentChallengeId = 0
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val rv = findViewById<RecyclerView>(R.id.rvLeaderboard)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(emptyList())
        rv.adapter = adapter

        val tvDesc = findViewById<TextView>(R.id.tvChallengeDesc)
        val etResult = findViewById<EditText>(R.id.etResult)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getTodayChallenge()
                val challenge = res.body()
                if (res.isSuccessful && challenge != null) {
                    currentChallengeId = challenge.challenge_id
                    tvDesc.text = challenge.description
                    loadLeaderboard()
                } else {
                    tvDesc.setText(R.string.msg_no_challenge)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Challenge not loaded (offline?)", e)
                tvDesc.setText(R.string.msg_no_challenge)
            }
        }

        btnSubmit.setOnClickListener {
            val result = Validators.parseChallengeResult(etResult.text.toString())
            if (result == null) {
                etResult.error = getString(R.string.err_result_range)     // handles empty, letters, 0, huge numbers
                return@setOnClickListener
            }
            if (currentChallengeId == 0) {
                toast(getString(R.string.msg_no_challenge))
                return@setOnClickListener
            }
            btnSubmit.isEnabled = false
            lifecycleScope.launch {
                try {
                    val res = RetrofitClient.api.submitChallenge(currentChallengeId, Session.bearer(this@ChallengeActivity), mapOf("result" to result))
                    when {
                        res.isSuccessful -> {
                            toast(getString(R.string.msg_result_submitted))
                            etResult.text.clear()
                            loadLeaderboard()
                        }
                        res.code() == 401 -> Session.expire(this@ChallengeActivity)
                        else -> toast(apiErrorText(res.code()))
                    }
                } catch (e: IOException) {
                    toast(getString(R.string.err_network))
                } catch (e: Exception) {
                    Log.e(TAG, "Submit error", e)
                    toast(getString(R.string.err_generic))
                }
                btnSubmit.isEnabled = true
            }
        }
    }

    private suspend fun loadLeaderboard() {
        try {
            val lb = RetrofitClient.api.getLeaderboard(currentChallengeId)
            if (lb.isSuccessful) adapter.updateList(lb.body() ?: emptyList())
        } catch (e: Exception) {
            Log.w(TAG, "Leaderboard not loaded", e)
        }
    }

    private fun toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    private companion object {
        const val TAG = "ChallengeActivity"
    }
}