package com.example.fitstreak

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import com.example.fitstreak.data.SyncManager
import com.example.fitstreak.util.NotificationHelper
import com.example.fitstreak.util.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** PART 3: runs once when the app starts. Creates the notification channel and watches the network. */
class FitStreakApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)

        // OFFLINE MODE: as soon as the phone gets internet again, upload the queued workouts.
        try {
            val cm = getSystemService(ConnectivityManager::class.java)
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "Network available - syncing pending workouts")
                    CoroutineScope(Dispatchers.IO).launch {
                        val synced = SyncManager.syncPending(applicationContext)
                        if (synced > 0 && Session.notificationsEnabled(applicationContext)) {
                            NotificationHelper.show(
                                applicationContext,
                                getString(R.string.app_name),
                                getString(R.string.fmt_synced, synced)
                            )
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Could not register network callback", e)
        }
    }

    private companion object {
        const val TAG = "FitStreakApp"
    }
}