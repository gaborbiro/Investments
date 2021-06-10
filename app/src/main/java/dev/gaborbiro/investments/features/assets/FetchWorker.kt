package dev.gaborbiro.investments.features.assets

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dev.gaborbiro.investments.AppPreferences
import dev.gaborbiro.investments.NotificationManager
import dev.gaborbiro.investments.data.DB
import dev.gaborbiro.investments.data.model.RecordDBModel
import dev.gaborbiro.investments.fetchNewAPIKey
import dev.gaborbiro.investments.model.Error
import java.time.LocalDate
import java.time.ZoneOffset

class FetchWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val repository: Repository by lazy { Repository() }

    override suspend fun doWork(): Result {
        return doFetch()
    }

    private suspend fun doFetch(noApiKeyRefresh: Boolean = false): Result {
        val (data, error) = repository.doFetch()
        val stockTickers = stockTickers.map { it.symbol to it }.associate { it }
        val cryptoTickers = cryptoTickers.map { it.symbol to it.amount }.associate { it }
        data?.let { (ftPrices, forex, binancePrices) ->
            val (_, record) = Mapper.generateModels(
                stockTickers = stockTickers,
                ftPrices = ftPrices,
                cryptoTickers = cryptoTickers,
                binancePrices = binancePrices.map { it.symbol to it.price }.associate { it },
                usd2gbp = forex.rates.gbp,
            )
            val dbModel = RecordDBModel(
                null,
                day = LocalDate.now(ZoneOffset.UTC),
                zoneId = ZoneOffset.UTC,
                Gson().toJson(record.data)
            )
            DB.getInstance().recordsDAO().insert(listOf(dbModel))
            NotificationManager.showSyncSuccessNotification()
        }
        error?.let {
            return handleError(it, noApiKeyRefresh).also { result ->
                if (result is Result.Failure) {
                    NotificationManager.showSyncFailedNotification()
                }
            }
        }
        return Result.success()
    }

    private suspend fun handleError(error: Error, noApiKeyRefresh: Boolean = false): Result {
        return if (error is Error.FTServerError && error.errors.any { it.reason == "MissingAPIKey" } && !noApiKeyRefresh) {
            val apiKey = fetchNewAPIKey()
            if (!apiKey.isNullOrBlank()) {
                AppPreferences.setApiKey(apiKey)
                doFetch(noApiKeyRefresh = true)
            } else {
                Result.failure()
            }
        } else {
            Result.failure()
        }
    }
}