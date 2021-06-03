package dev.gaborbiro.investments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dev.gaborbiro.investments.databinding.ActivityMainBinding
import dev.gaborbiro.investments.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.text.text = "Fetching..."
        val assetMap = ftAssets.map { it.symbol to it.amount }.associate { it }
        val cryptosMap = cryptoAssets.map { it.symbol to it.amount }.associate { it }
        fetchAssets(assetMap, cryptosMap)

    }

    private fun fetchAssets(
        ftAssets: Map<String, Double>,
        cryptoAssets: Map<String, Double>,
        noApiKeyRefresh: Boolean = false,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
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
            error?.let { handleError(it, ftAssets, cryptoAssets, noApiKeyRefresh) }
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
            it.basic.symbol to FTAssetUIModel(
                symbol = it.basic.symbol,
                name = it.basic.name,
                value = it.timeSeries.lastPrice.toDouble(),
                currency = it.basic.currency,
            )
        }.associate { it }
        var ftTotal = 0.0
        var cryptoTotal = 0.0
        val ftDetails = ftPrices.map {
            val sharePrice = mapValueToPounds(it.value.value, it.value.currency, usd2gbp)
            val shares = ftAssets[it.key]!!
            val value = sharePrice * shares
            ftTotal += value
            "${it.key} (${it.value.name}) => £${sharePrice.d2()} x $shares = £${value.d2()}"
        }.joinToString("\n\n")

        val cryptoDetails = cryptoAssets.map {
            val cryptoPrice = cryptoPrices[it.key + "GBP"]
                ?: (cryptoPrices[it.key + "BTC"]!! * cryptoPrices["BTCGBP"]!!)
            val value = cryptoPrice * it.value
            cryptoTotal += value
            "${it.key} => £${cryptoPrice.d2()} x ${it.value} = £${value.d2()}"
        }.joinToString("\n\n")

        CoroutineScope(Dispatchers.Main).launch {
            binding.text.text = "Stocks & Shares: £${ftTotal.roundToInt()}\n" +
                    "Crypto:                   £${cryptoTotal.roundToInt()}\n" +
                    "==================================\n" +
                    "Total:                      £${ftTotal.roundToInt() + cryptoTotal.roundToInt()}" +
                    "\n\n" +
                    "$ftDetails\n\n" +
                    "$cryptoDetails\n\n\n" +
                    "(USD2GBP: ${usd2gbp})"
        }
    }

    private fun handleError(
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

    private fun mapValueToPounds(value: Double, currency: String, usd2gbp: Double): Double {
        return when (currency) {
            "USD" -> value * usd2gbp
            "GBP" -> value
            "GBp" -> value / 100.0
            else -> 0.0
        }
    }

    private fun handleError(error: Error) {
        val message = when (error) {
            is Error.NetworkError -> "Couldn't talk to server. Check your internet connection"
            is Error.FTServerError -> error.uiMessage
            is Error.AppError -> error.uiMessage
        }
        CoroutineScope(Dispatchers.Main).launch {
            binding.text.text = "Error: $message"
        }
    }

}

fun Double.d2() = (this * 100).roundToInt() / 100.0

