package dev.gaborbiro.investments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationManager {

    private lateinit var appContext: Context

    fun init(appContext: Context) {
        this.appContext = appContext
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val syncChannel = NotificationChannel(
            CHANNEL_ID_SYNC,
            "Investment Sync Service",
            importance
        ).apply {
            description = "Periodic sync"
        }
        val notificationManager: NotificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(listOf(syncChannel))
    }

    fun showSyncFailedNotification() {
        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID_SYNC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Investments sync failed")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(false)
            .setAutoCancel(true)
        appContext.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID_SYNC_FAILED, builder.build())
    }

    fun showSyncSuccessNotification() {
        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID_SYNC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Investments sync finished")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(false)
            .setAutoCancel(true)
        appContext.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID_SYNC_SUCCESS, builder.build())
    }
}

private const val CHANNEL_ID_SYNC = "sync"
private const val NOTIFICATION_ID_SYNC_FAILED = 1534
private const val NOTIFICATION_ID_SYNC_SUCCESS = 1535