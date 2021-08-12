package dev.gaborbiro.investments

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dev.gaborbiro.investments.data.DB
import dev.gaborbiro.investments.features.assets.FetchWorker
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class App : Application() {

    companion object {
        lateinit var appContext: Context
            private set

        fun setWorkTimer() {
            WorkManager.getInstance(appContext).cancelAllWorkByTag("sync")

            val nextMondayMorning = LocalDate.now().atStartOfDay().plusDays(1L)

            val work = OneTimeWorkRequest.Builder(FetchWorker::class.java)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
//                        .setRequiresDeviceIdle(true)
                        .build()
                )
                .setInitialDelay(Duration.between(LocalDateTime.now(), nextMondayMorning))
                .addTag("sync")
                .build()
            WorkManager.getInstance(appContext).enqueue(work)
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        DB.init(appContext)
        NotificationManager.init(appContext)
        setWorkTimer()
    }
}