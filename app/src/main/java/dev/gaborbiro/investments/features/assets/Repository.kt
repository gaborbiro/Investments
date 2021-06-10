package dev.gaborbiro.investments.features.assets

import dev.gaborbiro.investments.*
import dev.gaborbiro.investments.features.assets.model.CryptoPrice
import dev.gaborbiro.investments.features.assets.model.FTPrices
import dev.gaborbiro.investments.features.assets.model.Forex
import dev.gaborbiro.investments.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository {

    suspend fun doFetch(): Response<Triple<FTPrices, Forex, ArrayList<CryptoPrice>>> {
        val stockTickers = stockTickers.map { it.symbol to it }.associate { it }
        return fetchAssets(stockTickers)
    }

    private suspend fun fetchAssets(stockTickers: Map<String, Ticker.FT>): Response<Triple<FTPrices, Forex, ArrayList<CryptoPrice>>> {
        return withContext(Dispatchers.IO) {
            val ftUrl = FT_TIME_SERIES_URL.format(
                stockTickers.map { it.key }.joinToString(","),
                AppPreferences.getApiKey()
            )
            fetch<FTPrices>(ftUrl)
                .flatMapSuspend { ftPrices: FTPrices ->
                    val forexResponse = fetch<Forex>(USD2GBP_BASE_URL)
                    forexResponse.flatMap { forex -> Pair(ftPrices, forex) }
                }
                .flatMapSuspend { (ftPrices, forex) ->
                    val binanceResponse = fetch<ArrayList<CryptoPrice>>(BINANCE_PRICES_URL)
                    binanceResponse.flatMap { binancePrices ->
                        Triple(ftPrices, forex, binancePrices)
                    }
                }
        }
    }
}