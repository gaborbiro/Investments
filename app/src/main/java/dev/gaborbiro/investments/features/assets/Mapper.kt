package dev.gaborbiro.investments.features.assets

import dev.gaborbiro.investments.R
import dev.gaborbiro.investments.features.assets.model.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object Mapper {

    fun generateModels(
        stockTickers: Map<String, Ticker.FT>,
        ftPrices: FTPrices,
        /**
         * Symbol to amount
         */
        cryptoTickers: Map<String, Double>,
        /**
         * Symbol to price
         */
        binancePrices: Map<String, Double>,
        usd2gbp: Double,
    ): Pair<MainUIModel, Record> {
        data class FTAsset(
            val symbol: String,
            val name: String,
            val value: Double,
            val currency: String,
        )

        /**
         * Map it into a more convenient (temporary) format
         */
        val ftPrices: Map<String, FTAsset> = ftPrices.data.items.map {
            it.basic.symbol to FTAsset(
                symbol = it.basic.symbol,
                name = it.basic.name,
                value = it.timeSeries.lastPrice.toDouble(),
                currency = it.basic.currency,
            )
        }.associate { it }
        var ftTotal = 0.0
        var ftGain = 0.0
        var ftCost = 0.0
        var cryptoTotal = 0.0
        val ftDetails = ftPrices.map { (ticker, asset) ->
            val (amount, bookCost) = stockTickers[ticker]!!.let { it.amount to it.bookCost }
            val (pounds, normalisedValue, currency, normalisedCost) = when (asset.currency) {
                "USD" -> Quad(asset.value * usd2gbp, asset.value, "$", bookCost * usd2gbp)
                "GBP" -> Quad(asset.value, asset.value, "£", bookCost)
                "GBp" -> Quad(asset.value / 100.0, asset.value / 100.0, "£", bookCost)
                else -> Quad(0.0, 0.0, "", 0.0)
            }
            val value = amount * pounds
            val gain = value - normalisedCost
            ftGain += gain
            ftCost += normalisedCost
            ftTotal += value
            AssetUIModel(
                label = asset.name,
                value = "£${value.money()}",
                details = "$currency${normalisedValue.money()} x $amount ($ticker)",
                gain = if (gain < 0) "-£${gain.absoluteValue.money()}" else "+£${gain.absoluteValue.money()}",
                gainColor = if (gain >= 0) R.color.green else R.color.red,
            )
        }

        val cryptoDetails = cryptoTickers.map { (ticker, amount) ->
            val cryptoPrice = binancePrices[ticker + "GBP"]
                ?: (binancePrices[ticker + "BTC"]!! * binancePrices["BTCGBP"]!!)
            val value = cryptoPrice * amount
            cryptoTotal += value
            AssetUIModel(
                label = ticker,
                value = "£${value.money()}",
                details = "£${cryptoPrice.money()} x $amount",
                gain = "",
                gainColor = R.color.white,
            )
        }
        val stocksTotal = ftTotal.roundToInt()
        val stocksGain = ftGain.roundToInt()
        val cryptoTotal2 = cryptoTotal.roundToInt()
        val total = (ftTotal + cryptoTotal).roundToInt()

        val record = Record(
            data = RecordData(
                stocksTotal = stocksTotal,
                cryptoTotal = cryptoTotal2,
                total = total,
            )
        )
        val mainUIModel = MainUIModel.Data(
            stocksTotal = stocksTotal,
            stocksGain = stocksGain,
            cryptoTotal = cryptoTotal2,
            total = total,
            assets = ftDetails + cryptoDetails
        )
        return mainUIModel to record
    }
}