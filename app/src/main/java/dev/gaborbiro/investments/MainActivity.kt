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

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val ftAssets = listOf(
            Asset.FT("GOOGL:NSQ", 7.00),
            Asset.FT("GB00B4W52V57:GBX", 615.385),
            Asset.FT("GB0006061963:GBX", 37.835),
            Asset.FT("GB00B7QK1Y37:GBX", 4043.915),
            Asset.FT("GB00B7C44X99:GBX", 1105.97),
            Asset.FT("COF:NYQ", 39.00),
            Asset.FT("FB:NSQ", 29.00),
            Asset.FT("FSLR:NSQ", 11.00),
            Asset.FT("GSK:LSE", 30.00),
            Asset.FT("SWDA:LSE:GBX", 43.00),
            Asset.FT("IWFV:LSE:GBX", 139.00),
            Asset.FT("NG.:LSE", 45.00),
            Asset.FT("PBEE:LSE", 54.00),
            Asset.FT("ULVR:LSE", 9.00),
            Asset.FT("VWRL:LSE:GBP", 14.00),
            Asset.FT("GB00BD3RZ368:GBP", 16.6694),
            Asset.FT("VOD:LSE", 325.00),
        )
        val cryptoAssets = listOf(
            Asset.Crypto("BTC", 0.21775232),
            Asset.Crypto("ETH", 0.56514631),
            Asset.Crypto("ZRX", 30.67180979),
            Asset.Crypto("OGN", 25.59088487),
            Asset.Crypto("BAT", 25.2691627),
            Asset.Crypto("MATIC", 22.8),
            Asset.Crypto("BCH", 0.04585),
            Asset.Crypto("DOGE", 96.3),
            Asset.Crypto("TRX", 334.9),
            Asset.Crypto("YFI", 0.000425),
            Asset.Crypto("VET", 143.6),
            Asset.Crypto("MKR", 0.00493),
            Asset.Crypto("WRX", 8.5),
            Asset.Crypto("AAVE", 0.0414),
            Asset.Crypto("SNX", 1.163),
            Asset.Crypto("ZEC", 0.08823),
            Asset.Crypto("COMP", 0.0321),
            Asset.Crypto("ANKR", 126.3),
            Asset.Crypto("THETA", 1.877),
            Asset.Crypto("DOT", 0.571),
            Asset.Crypto("BTT", 3384.0),
            Asset.Crypto("ENJ", 7.72),
            Asset.Crypto("ARDR", 55.2),
            Asset.Crypto("XEM", 62.93),
            Asset.Crypto("ZIL", 105.1),
            Asset.Crypto("BNB", 0.0339),
            Asset.Crypto("ATOM", 0.805),
            Asset.Crypto("SXP", 5.228),
            Asset.Crypto("FIO", 60.82),
            Asset.Crypto("1INCH", 3.4),
            Asset.Crypto("MANA", 12.9),
            Asset.Crypto("GRT", 13.43),
            Asset.Crypto("CHZ", 37.48),
            Asset.Crypto("IOTA", 8.78),
            Asset.Crypto("FIL", 0.1312),
            Asset.Crypto("LUNA", 1.404),
            Asset.Crypto("CAKE", 0.485),
            Asset.Crypto("XRP", 0.268),

            )
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
        usd2gbp: Float,
        cryptoPrices: Map<String, Double>,
    ) {
        val ftPrices = response.data.items.map {
            it.basic.symbol to FTAssetUIModel(
                symbol = it.basic.symbol,
                name = it.basic.name,
                value = it.timeSeries.lastPrice.toFloat(),
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
            "${it.key} (${it.value.name}) => £$sharePrice x $shares = £$value"
        }.joinToString("\n\n")

        val cryptoDetails = cryptoAssets.map {
            val cryptoPrice = cryptoPrices[it.key + "GBP"]
                ?: (cryptoPrices[it.key + "BTC"]!! * cryptoPrices["BTCGBP"]!!)
            val value = cryptoPrice * it.value
            cryptoTotal += value
            "${it.key} => £$cryptoPrice x ${it.value} = £$value"
        }.joinToString("\n\n")

        CoroutineScope(Dispatchers.Main).launch {
            binding.text.text = "Stocks & Shares: £$ftTotal\n" +
                    "Crypto: £$cryptoTotal\n" +
                    "=================\n" +
                    "Total: ${ftTotal + cryptoTotal}" +
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

    private fun mapValueToPounds(value: Float, currency: String, usd2gbp: Float): Float {
        return when (currency) {
            "USD" -> value * usd2gbp
            "GBP" -> value
            "GBp" -> value / 100f
            else -> 0f
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

