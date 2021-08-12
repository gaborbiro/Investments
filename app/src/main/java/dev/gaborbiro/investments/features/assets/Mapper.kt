package dev.gaborbiro.investments.features.assets

import android.text.SpannableString
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.gaborbiro.investments.App
import dev.gaborbiro.investments.R
import dev.gaborbiro.investments.data.model.RecordDBModel
import dev.gaborbiro.investments.features.assets.model.*
import dev.gaborbiro.investments.getHighlightedText
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object Mapper {

    private val gson: Gson by lazy { Gson() }
    private val dbJsonType: Type by lazy { object : TypeToken<HashMap<String, Double>>() {}.type }

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
        sortType: SortType = SortType.ORIGINAL,
    ): CombinedModel.Builder {
        data class FTAsset(
            val name: String,
            val value: Double,
            val currency: String,
            val price: Double,
            val dayChange: Double,
            val gain: Double,
            val stockTicker: Ticker.FT,
            val ticker: String
        )

        /**
         * Map it into a more convenient (temporary) format
         */
//        val ftPrices: Map<String, FTAsset> = ftPrices.data.items.map {
//            it.basic.symbol to FTAsset(
//                name = it.basic.name,
//                price = it.timeSeries.lastSession.lastPrice,
//                currency = it.basic.currency,
//                dayChange = it.timeSeries.lastSession.let { it.lastPrice - it.previousClosePrice }
//            )
//        }.associate { it }
        var ftTotal = 0.0
        var ftGain = 0.0
        var ftCost = 0.0
        var cryptoTotal = 0.0
        val ftAssets = ftPrices.data.items.map {
            val ticker = it.basic.symbol
            val stockTicker = stockTickers[ticker]!!
            val bookCost = stockTicker.purchases.sumOf { it.cost }
            val currency = stockTicker.currencyOverride ?: it.basic.currency
            val rawPrice = it.timeSeries.lastSession.lastPrice
            val (priceGBP, price, currencySymbol, bookCostGBP) = when (currency) {
                "USD" -> Quad(rawPrice * usd2gbp, rawPrice, "$", bookCost * usd2gbp)
                "GBP" -> Quad(rawPrice, rawPrice, "£", bookCost)
                "GBp" -> Quad(rawPrice / 100.0, rawPrice / 100.0, "£", bookCost)
                else -> throw IllegalStateException("Unsupported currency: $currency")
            }
            val value = stockTicker.quantity * priceGBP
            val gain: Double = value - bookCostGBP
            ftGain += gain
            ftCost += bookCostGBP
            ftTotal += value

            FTAsset(
                name = it.basic.name,
                value = value,
                price = price,
                currency = currencySymbol,
                dayChange = it.timeSeries.lastSession.let { it.lastPrice - it.previousClosePrice },
                gain = gain,
                stockTicker = stockTicker,
                ticker = ticker,
            )
        }

        fun Map<String, Double>.getPoundValue(ticker: String) = binancePrices[ticker + "GBP"]
            ?: (binancePrices[ticker + "BTC"]!! * binancePrices["BTCGBP"]!!)

        val cryptoDetails = cryptoTickers.map { (ticker, amount) ->
            val cryptoPrice = binancePrices.getPoundValue(ticker)
            val value = cryptoPrice * amount
            cryptoTotal += value
            AssetUIModel(
                label = ticker,
                value = "£${value.money()}",
                details = SpannableString("£${cryptoPrice.money()} x $amount"),
                gain = "",
                gainColor = R.color.white,
                yearlyGain = "",
            )
        }
        val stocksTotal = ftTotal.roundToInt()
        val stocksGain = ftGain.roundToInt()
        val cryptoTotal2 = cryptoTotal.roundToInt()
        val total = (ftTotal + cryptoTotal).roundToInt()

        val ftDetails = ftAssets
            .let {
                when (sortType) {
                    SortType.ORIGINAL -> it
                    SortType.DAY_CHANGE -> it.sortedByDescending { it.dayChange }
                    SortType.GAIN_LOSS -> it.sortedByDescending { it.gain }
                }
            }
            .map {
                val (directionChar, color) = when (it.dayChange >= 0) {
                    true -> "▲" to ContextCompat.getColor(App.appContext, R.color.green_light)
                    false -> "▼" to ContextCompat.getColor(App.appContext, R.color.red_light)
                }
                val detailsStr =
                    "${it.currency}${it.price.money()} " +
                            "(${it.currency}${it.dayChange.money()}$directionChar) x " +
                            "${it.stockTicker.quantity} (${it.ticker})"
                val details =
                    detailsStr.getHighlightedText(directionChar, color, highlightForeground = true)

                AssetUIModel(
                    label = it.name,
                    value = "£${it.value.money()}",
                    details = details,
                    gain = if (it.gain < 0) "-£${it.gain.absoluteValue.money()}" else "+£${it.gain.absoluteValue.money()}",
                    gainColor = if (it.gain >= 0) R.color.green else R.color.red,
                    yearlyGain = "" //  DecimalFormat("#.##%").format(yearlyGain)
                )
            }

        val record = RecordData().apply {
            put(KEY_STOCKS_TOTAL, stocksTotal.toDouble())
            put(KEY_CRYPTO_TOTAL, cryptoTotal2.toDouble())
            put(KEY_TOTAL, total.toDouble())
            putAll(cryptoTickers.mapValues { (ticker, _) -> binancePrices.getPoundValue(ticker) })
            putAll(ftPrices.data.items.map {
                it.basic.symbol to it.timeSeries.lastSession.lastPrice
            }.associate { it })
        }

        return CombinedModel.Builder(
            stocksTotal = stocksTotal,
            stocksGain = stocksGain,
            cryptoTotal = cryptoTotal2,
            total = total,
            assets = ftDetails + cryptoDetails,
            recordData = record
        )
    }

    fun map(dbData: List<RecordDBModel>): Triple<ChartUIModel, ChartUIModel, ChartUIModel> {
        val stocksChart = mutableListOf<Pair<Float, Double>>()
        val cryptoChart = mutableListOf<Pair<Float, Double>>()
        val totalChart = mutableListOf<Pair<Float, Double>>()

        dbData.forEach {
            val recordData = map(it)
            stocksChart.add(it.day.toEpochDay().toFloat() to recordData[KEY_STOCKS_TOTAL]!!)
            cryptoChart.add(it.day.toEpochDay().toFloat() to recordData[KEY_CRYPTO_TOTAL]!!)
            totalChart.add(it.day.toEpochDay().toFloat() to recordData[KEY_TOTAL]!!)
        }

        return Triple(
            ChartUIModel(stocksChart),
            ChartUIModel(cryptoChart),
            ChartUIModel(totalChart)
        )
    }

    fun map(dbModel: RecordDBModel): HashMap<String, Double> {
        return gson.fromJson<HashMap<String, Double>>(dbModel.record, dbJsonType)
    }

    fun map(record: RecordData): RecordDBModel {
        return RecordDBModel(
            day = LocalDate.now(ZoneOffset.UTC),
            zoneId = ZoneOffset.UTC,
            record = Gson().toJson(record)
        )
    }
}

class CombinedModel(
    val stocksTotal: Int,
    val stocksGain: Int,
    val cryptoTotal: Int,
    val total: Int,
    val assets: List<AssetUIModel>,
    val recordData: RecordData,
) {

//    lateinit var stocksChartUIModel: ChartUIModel
//        private set
//
//    lateinit var cryptoChartUIModel: ChartUIModel
//        private set
//
//    lateinit var totalChartUIModel: ChartUIModel
//        private set

    data class Builder(
        val stocksTotal: Int,
        val stocksGain: Int,
        val cryptoTotal: Int,
        val total: Int,
        val assets: List<AssetUIModel>,
        val recordData: RecordData,
    )
//    {
//
//        fun withCharts(
//            stocksChartUIModel: ChartUIModel,
//            cryptoChartUIModel: ChartUIModel,
//            totalChartUIModel: ChartUIModel
//        ): CombinedModel {
//            return CombinedModel(
//                stocksTotal,
//                stocksGain,
//                cryptoTotal,
//                total,
//                assets,
//                recordData
//            ).apply {
//                this.stocksChartUIModel = stocksChartUIModel
//                this.cryptoChartUIModel = cryptoChartUIModel
//                this.totalChartUIModel = totalChartUIModel
//            }
//        }
//    }
}

enum class SortType {
    ORIGINAL, DAY_CHANGE, GAIN_LOSS
}