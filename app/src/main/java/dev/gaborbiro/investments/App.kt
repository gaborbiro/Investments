package dev.gaborbiro.investments

import android.app.Application
import android.content.Context
import androidx.work.*
import dev.gaborbiro.investments.data.DB
import dev.gaborbiro.investments.features.assets.FetchWorker
import java.time.Duration

class App : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        DB.init(appContext)
        NotificationManager.init(appContext)
        WorkManager.getInstance(appContext).cancelAllWorkByTag("sync")
        val work: PeriodicWorkRequest = PeriodicWorkRequestBuilder<FetchWorker>(Duration.ofDays(1))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresDeviceIdle(true)
                    .build()
            )
            .addTag("sync")
            .build()
        WorkManager.getInstance(appContext).enqueue(work)
    }
}