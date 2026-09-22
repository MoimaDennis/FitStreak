package com.example.fitstreak

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.WorkoutLogRequest
import kotlinx.coroutines.launch

class WorkoutPlayerActivity : AppCompatActivity() {

    private var secondsElapsed = 0
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_player)

        val workoutId = intent.getIntExtra("workout_id", 0)
        val workoutName = intent.getStringExtra("workout_name") ?: "Workout"
        val durationMinutes = intent.getIntExtra("duration_minutes", 10)

        findViewById<android.widget.TextView>(R.id.tvWorkoutName).text = workoutName

        val totalMillis = durationMinutes * 60 * 1000L
        timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsElapsed = ((totalMillis - millisUntilFinished) / 1000).toInt()
                val min = secondsElapsed / 60
                val sec = secondsElapsed % 60
                findViewById<android.widget.TextView>(R.id.tvTimer).text = String.format("%02d:%02d", min, sec)
            }
            override fun onFinish() {
                findViewById<android.widget.TextView>(R.id.tvTimer).text = "Done!"
            }
        }
        timer.start()

        findViewById<android.widget.Button>(R.id.btnFinish).setOnClickListener {
            timer.cancel()
            finishWorkout(workoutId)
        }
    }

    private fun finishWorkout(workoutId: Int) {
        val prefs = getSharedPreferences("fitstreak_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.logWorkout(
                    "Bearer $token",
                    WorkoutLogRequest(workoutId, secondsElapsed, "just_right")
                )
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    prefs.edit()
                        .putInt("streak_count", body.streak_count)
                        .putInt("xp_total", body.xp_total.toInt())
                        .apply()
                    findViewById<android.widget.TextView>(R.id.tvResultMessage).text =
                        "Streak: ${body.streak_count} days • +${body.xp_gained.toInt()} XP" +
                                if (body.new_badges.isNotEmpty()) "\nNew badge: ${body.new_badges.joinToString()}" else ""
                } else {
                    findViewById<android.widget.TextView>(R.id.tvResultMessage).text = "Could not save workout — try again"
                }
            } catch (e: Exception) {
                findViewById<android.widget.TextView>(R.id.tvResultMessage).text = "Network error — workout not saved"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::timer.isInitialized) timer.cancel()
    }
}