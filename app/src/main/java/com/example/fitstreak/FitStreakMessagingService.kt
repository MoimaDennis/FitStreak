package com.example.fitstreak

import android.util.Log
import com.example.fitstreak.data.FcmHelper
import com.example.fitstreak.util.NotificationHelper
import com.example.fitstreak.util.Session
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** PART 3: receives push notifications from Firebase Cloud Messaging. */
class FitStreakMessagingService : FirebaseMessagingService() {

    /** Called when Firebase gives this phone a new token. */
    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        Session.prefs(this).edit().putString("fcm_token", token).apply()
        CoroutineScope(Dispatchers.IO).launch { FcmHelper.uploadToken(applicationContext) }
    }

    /** Called when a push arrives while the app is open (in the background Android shows it automatically). */
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Push received from ${message.from}")
        if (!Session.notificationsEnabled(this)) {
            Log.d(TAG, "Notifications are switched off in Settings - ignored")
            return
        }
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        NotificationHelper.show(this, title, body)
    }

    private companion object {
        const val TAG = "FitStreakFCM"
    }
}