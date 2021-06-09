package dev.gaborbiro.investments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaborbiro.investments.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainViewModel : ViewModel() {

    private val _uiModel = MutableLiveData<MainUIModel>()
    val uiModel: LiveData<MainUIModel> = _uiModel

    fun init() = viewModelScope.launch {
        _uiModel.value = MainUIModel.Loading
        val assetMap = ftAssets.map { it.symbol to it.amount }.associate { it }
        val cryptosMap = cryptoAssets.map { it.symbol to it.amount }.associate { it }
        fetchAssets(assetMap, cryptosMap)
    }

    private suspend fun fetchAssets(
        ftAssets: Map<String, Double>,
        cryptoAssets: Map<String, Double>,
        noApiKeyRefresh: Boolean = false,
    ) {
        withContext(Dispatchers.IO) {
            val ftUrl = FT_TIME_SERIES_URL.format(
                ftAssets.map { it.key }.joinToString(","),
                AppPreferences.getApiKey()
            )
            val (success, error) = fetch<TimeSeriesResponse>(ftUrl)
                .flatMapSuspend { timeSeries ->
                    val forex = fetch<ForexResponse>(USD2GBP_BASE_URL)
                    forex.flatMap { Pair(timeSeries, it) }
                }
                .flatMapSuspend { (timeSeries, forex) ->
                    val cryptoPrices = fetch<ArrayList<CryptoPrice>>(BINANCE_PRICES_URL)
                    cryptoPrices.flatMap { Triple(timeSeries, forex, it) }
                }
            error?.let {
                handleError(it, ftAssets, cryptoAssets, noApiKeyRefresh)
            }
            success?.let { (timeSeries, forex, cryptoPrices) ->
                handleAssetsSuccess(
                    response = timeSeries,
                    ftAssets = ftAssets,
                    cryptoAssets = cryptoAssets,
                    usd2gbp = forex.rates.gbp,
                    cryptoPrices = cryptoPrices.map { it.symbol to it.price }.associate { it },
                )
            }
        }
    }

    private fun handleAssetsSuccess(
        response: TimeSeriesResponse,
        ftAssets: Map<String, Double>,
        cryptoAssets: Map<String, Double>,
        usd2gbp: Double,
        cryptoPrices: Map<String, Double>,
    ) {
        val ftPrices = response.data.items.map {
            it.basic.symbol to FTAsset(
                symbol = it.basic.symbol,
                name = it.basic.name,
                value = it.timeSeries.lastPrice.toDouble(),
                currency = it.basic.currency,
            )
        }.associate { it }
        var ftTotal = 0.0
        var cryptoTotal = 0.0
        val ftDetails = ftPrices.map { (ticker, asset) ->
            val shares = ftAssets[ticker]!!
            val (pounds, normalisedValue, currency) = when (asset.currency) {
                "USD" -> Triple(asset.value * usd2gbp, asset.value, "$")
                "GBP" -> Triple(asset.value, asset.value, "£")
                "GBp" -> Triple(asset.value / 100.0, asset.value / 100.0, "£")
                else -> Triple(0.0, 0.0, "")
            }
            val value = shares * pounds
            ftTotal += value
            AssetUIModel(
                label = asset.name,
                value = "£${value.money()}",
                details = "$currency${normalisedValue.money()} x $shares ($ticker)"
            )
        }

        val cryptoDetails = cryptoAssets.map { (ticker, value) ->
            val cryptoPrice = cryptoPrices[ticker + "GBP"]
                ?: (cryptoPrices[ticker + "BTC"]!! * cryptoPrices["BTCGBP"]!!)
            val value = cryptoPrice * value
            cryptoTotal += value
            AssetUIModel(
                label = ticker,
                value = "£${value.money()}",
                details = "£${cryptoPrice.money()} x $value"
            )
        }

        CoroutineScope(Dispatchers.Main).launch {

            _uiModel.value = MainUIModel.Data(
                stocksTotal = ftTotal.roundToInt(),
                cryptoTotal = cryptoTotal.roundToInt(),
                total = (ftTotal + cryptoTotal).roundToInt(),
                assets = ftDetails + cryptoDetails
            )
        }
    }

    private suspend fun handleError(
        error: Error,
        ftAssets: Map<String, Double>,
        cryptoAssets: Map<String, Double>,
        noApiKeyRefresh: Boolean = false,
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
                        fetchAssets(ftAssets, cryptoAssets, noApiKeyRefresh = true)
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