package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.fitstreak.data.LoginRequest
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.SsoRequest
import com.example.fitstreak.util.Session
import com.example.fitstreak.util.Validators
import com.example.fitstreak.util.apiErrorText
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Already logged in? Skip this screen.
        if (Session.isLoggedIn(this)) {
            Log.d(TAG, "Saved session found - opening Home")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            when {
                email.isEmpty() || password.isEmpty() -> toast(R.string.err_fill_fields)
                !Validators.isValidEmail(email) -> etEmail.error = getString(R.string.err_email_invalid)
                !Validators.isValidPassword(password) -> etPassword.error = getString(R.string.err_password_short)
                else -> login(email, password)
            }
        }

        btnGoogle.setOnClickListener { googleSignIn() }

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /** Normal email + password login (password is checked against the bcrypt hash on the server). */
    private fun login(email: String, password: String) {
        setBusy(true)
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.login(LoginRequest(email, password))
                val body = res.body()
                if (res.isSuccessful && body != null) {
                    Session.completeLogin(this@LoginActivity, body)
                    return@launch
                }
                Log.w(TAG, "Login failed: HTTP ${res.code()}")
                toast(if (res.code() == 401) R.string.err_login_failed else 0, res.code())
            } catch (e: IOException) {
                Log.w(TAG, "Login: no network", e)
                toast(R.string.err_network)
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                toast(R.string.err_generic)
            }
            setBusy(false)
        }
    }

    /** PART 3 - SSO: Google account -> Firebase Auth -> Firebase ID token -> our API -> our own JWT. */
    private fun googleSignIn() {
        setBusy(true)
        lifecycleScope.launch {
            try {
                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val result = CredentialManager.create(this@LoginActivity).getCredential(this@LoginActivity, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val google = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCred = GoogleAuthProvider.getCredential(google.idToken, null)
                    val user = FirebaseAuth.getInstance().signInWithCredential(firebaseCred).await().user
                    val idToken = user?.getIdToken(false)?.await()?.token
                    Log.d(TAG, "Firebase sign-in OK for ${user?.email}")
                    if (idToken == null) {
                        toast(R.string.err_google_failed)
                    } else {
                        val res = RetrofitClient.api.sso(SsoRequest(idToken))
                        val body = res.body()
                        if (res.isSuccessful && body != null) {
                            Session.completeLogin(this@LoginActivity, body)
                            return@launch
                        }
                        Log.w(TAG, "SSO rejected by API: HTTP ${res.code()}")
                        toast(0, res.code())
                    }
                } else {
                    toast(R.string.err_google_failed)
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "User closed the Google sign-in sheet")
            } catch (e: GetCredentialException) {
                Log.w(TAG, "Google sign-in failed", e)
                toast(R.string.err_google_failed)
            } catch (e: IOException) {
                Log.w(TAG, "SSO: no network", e)
                toast(R.string.err_network)
            } catch (e: Exception) {
                Log.e(TAG, "SSO error", e)
                toast(R.string.err_google_failed)
            }
            setBusy(false)
        }
    }

    private fun setBusy(busy: Boolean) {
        btnLogin.isEnabled = !busy
        btnGoogle.isEnabled = !busy
    }

    /** Shows a message: pass a string id, or 0 plus an HTTP code to get the matching error text. */
    private fun toast(resId: Int, httpCode: Int = 0) {
        val text = if (resId != 0) getString(resId) else apiErrorText(httpCode)
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private companion object {
        const val TAG = "LoginActivity"
    }
}