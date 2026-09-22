package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.LoginRequest
import com.example.fitstreak.data.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val prefs = getSharedPreferences("fitstreak_prefs", MODE_PRIVATE)

        val etEmail = findViewById<android.widget.EditText>(R.id.etEmail)
        val etPassword = findViewById<android.widget.EditText>(R.id.etPassword)

        findViewById<android.widget.Button>(R.id.btnLogin).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    val res = RetrofitClient.api.login(LoginRequest(email, password))
                    if (res.isSuccessful && res.body() != null) {
                        val body = res.body()!!
                        prefs.edit()
                            .putString("token", body.token)
                            .putInt("user_id", body.user?.user_id ?: 0)
                            .putInt("streak_count", body.user?.streak_count ?: 0)
                            .putInt("xp_total", body.user?.xp_total ?: 0)
                            .apply()
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<android.widget.TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}