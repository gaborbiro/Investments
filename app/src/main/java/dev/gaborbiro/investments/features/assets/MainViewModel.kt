package dev.gaborbiro.investments.features.assets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dev.gaborbiro.investments.AppPreferences
import dev.gaborbiro.investments.data.DB
import dev.gaborbiro.investments.data.model.RecordDBModel
import dev.gaborbiro.investments.features.assets.model.MainUIModel
import dev.gaborbiro.investments.fetchNewAPIKey
import dev.gaborbiro.investments.model.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

class MainViewModel : ViewModel() {

    private val _uiModel = MutableLiveData<MainUIModel>()
    val uiModel: LiveData<MainUIModel> = _uiModel

    private val repository: Repository by lazy { Repository() }

    fun init() = viewModelScope.launch {
        _uiModel.value = MainUIModel.Loading
        doFetch()
    }

    private suspend fun doFetch(noApiKeyRefresh: Boolean = false) {
        val (data, error) = repository.doFetch()
        val stockTickers = stockTickers.map { it.symbol to it }.associate { it }
        val cryptoTickers = cryptoTickers.map { it.symbol to it.amount }.associate { it }
        error?.let {
            handleError(it, noApiKeyRefresh)
        }
        data?.let { (ftPrices, forex, binancePrices) ->
            val (uiModel, record) = Mapper.generateModels(
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
            CoroutineScope(Dispatchers.Main).launch {
                _uiModel.value = uiModel
            }
        }
    }

    private suspend fun handleError(error: Error, noApiKeyRefresh: Boolean = false) {
        if (error is Error.FTServerError) {
            if (error.errors.any { it.reason == "MissingAPIKey" }) {
                if (noApiKeyRefresh) {
                    handleError(error)
                } else {
                    _uiModel.value = MainUIModel.Loading
                    val apiKey = fetchNewAPIKey()
                    if (apiKey.isNullOrBlank()) {
                        handleError(Error.AppError("Could not refresh Api key"))
                    } else {
                        AppPreferences.setApiKey(apiKey)
                        doFetch(noApiKeyRefresh = true)
                    }
                }
            } else {
                handleError(Error.AppError("Could not refresh Api key"))
            }
        } else {
            handleError(error)
        }
    }

    private fun handleError(error: Error) {
        val message = when (error) {
            is Error.NetworkError -> "Couldn't talk to server. Check your internet connection"
            is Error.FTServerError -> error.uiMessage
            is Error.AppError -> error.uiMessage
        }
        _uiModel.value = MainUIModel.Error("Error: $message")
    }
}

data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)