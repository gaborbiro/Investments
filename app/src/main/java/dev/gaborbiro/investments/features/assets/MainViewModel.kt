package dev.gaborbiro.investments.features.assets

import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaborbiro.investments.App
import dev.gaborbiro.investments.AppPreferences
import dev.gaborbiro.investments.R
import dev.gaborbiro.investments.data.DB
import dev.gaborbiro.investments.data.model.RecordDBModel
import dev.gaborbiro.investments.features.assets.model.MainUIModel
import dev.gaborbiro.investments.features.assets.model.MappingException
import dev.gaborbiro.investments.fetchNewAPIKey
import dev.gaborbiro.investments.model.Error
import getHighlightedText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs

class MainViewModel : ViewModel() {

    private val _uiModel = MutableLiveData<MainUIModel>()
    val uiModel: LiveData<MainUIModel> = _uiModel

    private val repository: Repository by lazy { Repository() }

    fun loadData(sortType: SortType) = viewModelScope.launch {
        _uiModel.value = MainUIModel.Loading
        try {
            doFetch(sortType)
        } catch (e: MappingException) {
            e.printStackTrace()
            val message = e.message ?: "Unknown error"
            _uiModel.value = MainUIModel.Error("Error mapping data ($message). Check logs")
        }
    }

    private suspend fun doFetch(sortType: SortType, noApiKeyRefresh: Boolean = false) {
        val (data, error) = repository.doFetch()
        val stockTickers = stockTickers.map { it.symbol to it }.associate { it }
        val cryptoTickers = cryptoTickers.map { it.symbol to it.quantity }.associate { it }
        error?.let {
            handleError(it, sortType, noApiKeyRefresh)
        }
        data?.let { (ftPrices, forex, binancePrices) ->
            val (stocksTotal, stocksGain, stocksDayChange, cryptoTotal, total, assets, recordData) = Mapper.generateModels(
                stockTickers = stockTickers,
                ftPrices = ftPrices,
                cryptoTickers = cryptoTickers,
                binancePrices = binancePrices.map { it.symbol to it.price }.associate { it },
                usd2gbp = forex.rates.gbp,
                sortType = sortType,
            )
            val dao = DB.getInstance().recordsDAO()

            dao.insert(listOf(recordData))
            val dbData: List<RecordDBModel> = dao.get(LocalDate.now().minusMonths(HISTORY_LENGTH_MONTHS))
            val (stocksChart, cryptoChart, totalChart) = Mapper.map(dbData)

            val (directionChar, color) = when (stocksDayChange >= 0) {
                true -> "▲" to ContextCompat.getColor(App.appContext, R.color.green_light)
                false -> "▼" to ContextCompat.getColor(App.appContext, R.color.red_light)
            }
            val stocksDayChangeStr = "(£${abs(stocksDayChange).bigMoney()}$directionChar)"

            CoroutineScope(Dispatchers.Main).launch {
                _uiModel.value = MainUIModel.Data(
                    stocksTotal,
                    stocksGain,
                    stocksDayChangeStr.getHighlightedText(directionChar, color, highlightForeground = true),
                    cryptoTotal,
                    total,
                    assets,
                    stocksChart,
                    cryptoChart,
                    totalChart,
                    sortType
                )
            }
        }
    }

    private suspend fun handleError(
        error: Error,
        sortType: SortType,
        noApiKeyRefresh: Boolean = false
    ) {
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
                        doFetch(sortType, noApiKeyRefresh = true)
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