package com.example.fitstreak

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.RegisterRequest
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.util.Session
import com.example.fitstreak.util.Validators
import com.example.fitstreak.util.apiErrorText
import kotlinx.coroutines.launch
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btn = findViewById<Button>(R.id.btnRegister)

        btn.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // Validate BEFORE calling the API so bad input never crashes or wastes a request
            if (!Validators.isValidUsername(username)) { etUsername.error = getString(R.string.err_username_invalid); return@setOnClickListener }
            if (!Validators.isValidEmail(email)) { etEmail.error = getString(R.string.err_email_invalid); return@setOnClickListener }
            if (!Validators.isValidPassword(password)) { etPassword.error = getString(R.string.err_password_short); return@setOnClickListener }

            btn.isEnabled = false
            lifecycleScope.launch {
                try {
                    val res = RetrofitClient.api.register(RegisterRequest(username, email, password))
                    val body = res.body()
                    if (res.isSuccessful && body != null) {
                        Session.completeLogin(this@RegisterActivity, body)   // registered = logged in
                        return@launch
                    }
                    Log.w(TAG, "Register failed: HTTP ${res.code()}")
                    Toast.makeText(this@RegisterActivity, apiErrorText(res.code()), Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    Toast.makeText(this@RegisterActivity, R.string.err_network, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Register error", e)
                    Toast.makeText(this@RegisterActivity, R.string.err_generic, Toast.LENGTH_LONG).show()
                }
                btn.isEnabled = true
            }
        }
    }

    private companion object {
        const val TAG = "RegisterActivity"
    }
}