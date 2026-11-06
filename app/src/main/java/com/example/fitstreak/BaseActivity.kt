package com.example.fitstreak

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

/** Shared by the 5 main screens: bottom navigation + the Android 13 notification permission. */
abstract class BaseActivity : AppCompatActivity() {

    abstract fun selectedNavId(): Int

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("BaseActivity", "POST_NOTIFICATIONS granted = $granted")
        }

    /** Android 13+ must ask the user before notifications can be shown. */
    protected fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    protected fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.selectedItemId = selectedNavId()
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == selectedNavId()) {
                true
            } else {
                val target = when (item.itemId) {
                    R.id.nav_home -> HomeActivity::class.java
                    R.id.nav_library -> LibraryActivity::class.java
                    R.id.nav_challenge -> ChallengeActivity::class.java
                    R.id.nav_progress -> ProgressActivity::class.java
                    R.id.nav_settings -> SettingsActivity::class.java
                    else -> HomeActivity::class.java
                }
                val intent = Intent(this, target).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                true
            }
        }
    }
}