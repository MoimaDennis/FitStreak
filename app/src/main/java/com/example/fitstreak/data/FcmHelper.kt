package com.example.fitstreak.data

import android.content.Context
import android.util.Log
import com.example.fitstreak.util.Session
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * PART 3 - REAL-TIME NOTIFICATIONS (Firebase Cloud Messaging).
 * - The phone subscribes to the topic "fitstreak_all" (so you can send a message to everybody
 *   from the Firebase console).
 * - The phone's own FCM token is printed in Logcat and saved to the API (users.fcm_token).
 */
object FcmHelper {
    private const val TAG = "FcmHelper"
    const val TOPIC = "fitstreak_all"

    /** Call after login and when the app starts: applies the notification setting + uploads the token. */
    fun setup(context: Context) {
        val app = context.applicationContext
        if (Session.notificationsEnabled(app)) subscribe() else unsubscribe()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Could not get FCM token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "FCM token: $token")
            Session.prefs(app).edit().putString("fcm_token", token).apply()
            CoroutineScope(Dispatchers.IO).launch { uploadToken(app) }
        }
    }

    fun subscribe() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
            .addOnCompleteListener { Log.d(TAG, "Subscribed to $TOPIC: ${it.isSuccessful}") }
    }

    fun unsubscribe() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
            .addOnCompleteListener { Log.d(TAG, "Unsubscribed from $TOPIC: ${it.isSuccessful}") }
    }

    /** Sends the saved FCM token to the API. Silently skipped when logged out or offline. */
    suspend fun uploadToken(context: Context) {
        val app = context.applicationContext
        val token = Session.prefs(app).getString("fcm_token", null) ?: return
        if (!Session.isLoggedIn(app)) return
        try {
            val res = RetrofitClient.api.saveFcmToken(Session.bearer(app), FcmTokenRequest(token))
            Log.d(TAG, "FCM token uploaded: HTTP ${res.code()}")
        } catch (e: Exception) {
            Log.w(TAG, "FCM token upload failed (offline?)", e)
        }
    }
}