package dev.gaborbiro.investments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dev.gaborbiro.investments.databinding.ActivityMainBinding
import dev.gaborbiro.investments.databinding.ListItemAssetBinding
import dev.gaborbiro.investments.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        showProgress(true)
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
            error?.let {
                showProgress(false)
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
            binding.stocksSharesValue.text = "£${ftTotal.roundToInt().money()}"
            binding.cryptoValue.text = "£${cryptoTotal.roundToInt().money()}"
            binding.totalValue.text = "£${(ftTotal + cryptoTotal).roundToInt().money()}"

            ftDetails.forEach(::addAssetToUI)
            cryptoDetails.forEach(::addAssetToUI)
            showProgress(false)
        }
    }

    private fun addAssetToUI(asset: AssetUIModel) {
        val binding = DataBindingUtil.inflate<ListItemAssetBinding>(
            layoutInflater,
            R.layout.list_item_asset,
            binding.container,
            /* attachToParent: */ true
        )
        binding.item = asset
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
                    showProgress(true)
                    val apiKey = fetchNewAPIKey()
                    if (apiKey.isNullOrBlank()) {
                        showProgress(false)
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
        showError("Error: $message")
    }

    private fun showError(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.errorStatus.text = text
            binding.errorStatus.show()
        }
    }

    private fun hideError() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.errorStatus.text = null
            binding.errorStatus.hide()
        }
    }

    private fun showProgress(visible: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            if (visible) {
                binding.progress.show()
            } else {
                binding.progress.hide()
            }
        }
    }
}

fun Number.money(): String {
    val format = DecimalFormat("#,###.##")
    (format as NumberFormat).minimumFractionDigits = 2
    return format.format(this)
}