package com.example.fitstreak

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.AppDatabase
import com.example.fitstreak.data.PendingLogEntity
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.WorkoutLogRequest
import com.example.fitstreak.util.GameLogic
import com.example.fitstreak.util.NotificationHelper
import com.example.fitstreak.util.Session
import kotlinx.coroutines.launch
import java.io.IOException

class WorkoutPlayerActivity : AppCompatActivity() {

    private var secondsElapsed = 0
    private var finished = false
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_player)

        val workoutId = intent.getIntExtra("workout_id", 0)
        val workoutName = intent.getStringExtra("workout_name") ?: getString(R.string.app_name)
        val durationMinutes = intent.getIntExtra("duration_minutes", 10)
        val tvTimer = findViewById<TextView>(R.id.tvTimer)

        findViewById<TextView>(R.id.tvWorkoutName).text = workoutName

        val totalMillis = durationMinutes * 60 * 1000L
        timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsElapsed = ((totalMillis - millisUntilFinished) / 1000).toInt()
                tvTimer.text = GameLogic.formatTimer(secondsElapsed)
            }

            override fun onFinish() {
                secondsElapsed = (totalMillis / 1000).toInt()
                tvTimer.text = getString(R.string.msg_done)
            }
        }
        timer.start()

        val btnFinish = findViewById<Button>(R.id.btnFinish)
        btnFinish.setOnClickListener {
            if (finished) return@setOnClickListener      // stops double-tap = double upload
            finished = true
            btnFinish.isEnabled = false
            timer.cancel()
            saveWorkout(workoutId)
        }
    }

    private fun selectedDifficulty(): String =
        when (findViewById<RadioGroup>(R.id.rgDifficulty).checkedRadioButtonId) {
            R.id.rbEasy -> "Easy"
            R.id.rbHard -> "Hard"
            else -> "Medium"
        }

    /** Tries the API first. If the phone is offline (or the server is down) the workout is queued in Room. */
    private fun saveWorkout(workoutId: Int) {
        val app = applicationContext
        val seconds = maxOf(1, secondsElapsed)
        val difficulty = selectedDifficulty()
        val tvResult = findViewById<TextView>(R.id.tvResultMessage)

        lifecycleScope.launch {
            suspend fun queueOffline() {
                AppDatabase.getInstance(app).workoutDao()
                    .insertPending(PendingLogEntity(workout_id = workoutId, duration_seconds = seconds, difficulty_rating = difficulty))
                Log.d(TAG, "Workout queued offline (workout $workoutId, $seconds s)")
                tvResult.text = getString(R.string.msg_saved_offline)
            }

            try {
                val res = RetrofitClient.api.logWorkout(Session.bearer(app), WorkoutLogRequest(workoutId, seconds, difficulty))
                val body = res.body()
                when {
                    res.isSuccessful && body != null -> {
                        Session.saveWorkoutResult(app, body)
                        var msg = getString(R.string.fmt_workout_saved, body.streak_count, body.xp_gained.toInt())
                        if (body.new_badges.isNotEmpty()) {
                            val badges = getString(R.string.fmt_new_badge, body.new_badges.joinToString())
                            msg += "\n$badges"
                            if (Session.notificationsEnabled(app)) NotificationHelper.show(app, getString(R.string.app_name), badges)
                        }
                        tvResult.text = msg
                    }
                    res.code() == 401 -> Session.expire(this@WorkoutPlayerActivity)
                    res.code() >= 500 -> queueOffline()
                    else -> tvResult.text = getString(R.string.msg_save_failed)
                }
            } catch (e: IOException) {
                Log.w(TAG, "No network while saving workout", e)
                queueOffline()
            } catch (e: Exception) {
                Log.e(TAG, "Save workout error", e)
                tvResult.text = getString(R.string.msg_save_failed)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::timer.isInitialized) timer.cancel()
    }

    private companion object {
        const val TAG = "WorkoutPlayer"
    }
}