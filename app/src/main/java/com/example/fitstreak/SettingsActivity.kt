package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.FcmHelper
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.SettingsRequest
import com.example.fitstreak.util.Session
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val prefs = Session.prefs(this)

        // ---- Account ----
        findViewById<TextView>(R.id.tvAccount).text =
            "${prefs.getString("username", "")}\n${prefs.getString("email", "")}"

        // ---- Notifications (also subscribes / unsubscribes the push topic) ----
        val switchNotif = findViewById<SwitchMaterial>(R.id.switchNotifications)
        switchNotif.isChecked = Session.notificationsEnabled(this)
        switchNotif.setOnCheckedChangeListener { _, on ->
            prefs.edit().putBoolean("notifications_enabled", on).apply()
            if (on) {
                askNotificationPermission()
                FcmHelper.subscribe()
            } else {
                FcmHelper.unsubscribe()
            }
            saveToServer(on)
        }

        // ---- Language: English / Afrikaans / isiZulu ----
        val rgLanguage = findViewById<RadioGroup>(R.id.rgLanguage)
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        rgLanguage.check(
            when {
                current.startsWith("af") -> R.id.rbAf
                current.startsWith("zu") -> R.id.rbZu
                else -> R.id.rbEn
            }
        )
        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val tag = when (checkedId) {
                R.id.rbAf -> "af"
                R.id.rbZu -> "zu"
                else -> "en"
            }
            Log.d(TAG, "Language changed to $tag")
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))  // recreates screens
        }

        // ---- Weekly goal (UD5) ----
        val slider = findViewById<Slider>(R.id.sliderGoal)
        val tvGoal = findViewById<TextView>(R.id.tvGoalValue)
        slider.value = Session.weeklyGoal(this).toFloat()
        tvGoal.text = getString(R.string.fmt_goal_value, Session.weeklyGoal(this))
        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                Session.setWeeklyGoal(this, value.toInt())
                tvGoal.text = getString(R.string.fmt_goal_value, value.toInt())
            }
        }

        // ---- Log out ----
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Session.logout(this)
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun saveToServer(notificationsOn: Boolean) {
        val units = Session.prefs(this).getString("units", "metric") ?: "metric"
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.updateProfile(Session.bearer(this@SettingsActivity), SettingsRequest(units, if (notificationsOn) 1 else 0))
                if (res.code() == 401) Session.expire(this@SettingsActivity)
            } catch (e: Exception) {
                Log.w(TAG, "Settings saved on the phone only (offline)", e)
            }
        }
    }

    private companion object {
        const val TAG = "SettingsActivity"
    }
}