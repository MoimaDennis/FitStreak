package com.example.fitstreak

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    abstract fun selectedNavId(): Int

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

