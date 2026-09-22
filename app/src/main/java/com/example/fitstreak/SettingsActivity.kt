package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.SettingsRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val prefs = getSharedPreferences("fitstreak_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val rgUnits = findViewById<android.widget.RadioGroup>(R.id.rgUnits)
        val switchNotif = findViewById<SwitchMaterial>(R.id.switchNotifications)

        val saveSettings = {
            val units = if (rgUnits.checkedRadioButtonId == R.id.rbMetric) "metric" else "imperial"
            val notif = if (switchNotif.isChecked) 1 else 0
            lifecycleScope.launch {
                try {
                    RetrofitClient.api.updateProfile("Bearer $token", SettingsRequest(units, notif))
                } catch (e: Exception) { }
            }
        }

        rgUnits.setOnCheckedChangeListener { _, _ -> saveSettings() }
        switchNotif.setOnCheckedChangeListener { _, _ -> saveSettings() }

        findViewById<android.widget.Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}