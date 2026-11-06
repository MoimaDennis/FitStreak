package com.example.fitstreak.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.example.fitstreak.HomeActivity
import com.example.fitstreak.LoginActivity
import com.example.fitstreak.R
import com.example.fitstreak.data.AppDatabase
import com.example.fitstreak.data.AuthResponse
import com.example.fitstreak.data.FcmHelper
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.User
import com.example.fitstreak.data.WorkoutLogResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** One place for everything stored in SharedPreferences ("fitstreak_prefs"): token, user, streak, XP, settings. */
object Session {
    private const val TAG = "Session"
    private const val PREFS = "fitstreak_prefs"

    fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun token(context: Context): String = prefs(context).getString("token", "") ?: ""
    fun bearer(context: Context): String = "Bearer ${token(context)}"
    fun isLoggedIn(context: Context): Boolean = token(context).isNotEmpty()
    fun userId(context: Context): Int = prefs(context).getInt("user_id", 0)
    fun notificationsEnabled(context: Context): Boolean = prefs(context).getBoolean("notifications_enabled", true)

    /** Weekly workout goal (1-7) chosen in Settings, and workouts done this week (from the API). */
    fun weeklyGoal(context: Context): Int = prefs(context).getInt("weekly_goal", 3)
    fun setWeeklyGoal(context: Context, days: Int) = prefs(context).edit().putInt("weekly_goal", days.coerceIn(1, 7)).apply()
    fun weekCount(context: Context): Int = prefs(context).getInt("week_count", 0)

    /** yyyy-MM-dd of today (phone time). */
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /** Saves the token + user returned by login / register / SSO. */
    fun saveAuth(context: Context, res: AuthResponse) {
        val editor = prefs(context).edit().putString("token", res.token)
        val u = res.user
        editor.putInt("user_id", u?.user_id ?: res.user_id ?: 0)
        if (u != null) putUser(editor, u)
        editor.apply()
    }

    fun saveUser(context: Context, user: User) {
        val editor = prefs(context).edit()
        putUser(editor, user)
        editor.apply()
    }

    private fun putUser(editor: SharedPreferences.Editor, u: User) {
        editor.putInt("user_id", u.user_id)
            .putString("username", u.username)
            .putString("email", u.email)
            .putInt("streak_count", u.streak_count)
            .putInt("longest_streak", u.longest_streak)
            .putInt("xp_total", u.xp_total)
            .putInt("week_count", u.workouts_this_week)
            .putString("units", u.units)
            .putBoolean("notifications_enabled", u.notifications_enabled != 0)
    }

    /** Called after a workout is saved on the server. */
    fun saveWorkoutResult(context: Context, r: WorkoutLogResponse) {
        val p = prefs(context)
        val longest = maxOf(p.getInt("longest_streak", 0), r.streak_count)
        p.edit()
            .putInt("streak_count", r.streak_count)
            .putInt("longest_streak", longest)
            .putInt("xp_total", r.xp_total.toInt())
            .putInt("week_count", p.getInt("week_count", 0) + 1)
            .apply()
    }

    /** Finishes a successful login/register/SSO: save data, set up push, open Home. */
    fun completeLogin(activity: Activity, res: AuthResponse) {
        saveAuth(activity, res)
        Log.d(TAG, "Login complete for user ${userId(activity)}")
        FcmHelper.setup(activity)
        activity.startActivity(
            Intent(activity, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }

    /** Clears the local session (and the Firebase user created by Google sign-in). */
    fun logout(context: Context) {
        val app = context.applicationContext
        // queued workouts belong to the user who is logging out - never upload them for someone else
        CoroutineScope(Dispatchers.IO).launch { AppDatabase.getInstance(app).workoutDao().clearPending() }
        FcmHelper.unsubscribe()
        prefs(context).edit().clear().apply()
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signOut failed", e)
        }
    }

    /** The token is invalid/expired (HTTP 401): go back to the login screen. */
    fun expire(context: Context) {
        Log.w(TAG, "Session expired - returning to login")
        logout(context)
        Toast.makeText(context, context.getString(R.string.msg_session_expired), Toast.LENGTH_LONG).show()
        context.startActivity(
            Intent(context, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    /** Downloads the latest streak/XP from the API. Returns true when the data was refreshed. */
    suspend fun refreshProfile(context: Context): Boolean {
        if (!isLoggedIn(context)) return false
        return try {
            val res = RetrofitClient.api.getProfile(bearer(context))
            val body = res.body()
            when {
                res.isSuccessful && body != null -> { saveUser(context, body); true }
                res.code() == 401 -> { expire(context); false }
                else -> false
            }
        } catch (e: IOException) {
            Log.w(TAG, "refreshProfile: offline", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "refreshProfile failed", e)
            false
        }
    }
}