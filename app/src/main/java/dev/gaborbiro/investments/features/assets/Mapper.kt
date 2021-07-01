package dev.gaborbiro.investments.features.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.gaborbiro.investments.R
import dev.gaborbiro.investments.data.model.RecordDBModel
import dev.gaborbiro.investments.features.assets.model.*
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

        ): CombinedModel.Builder {
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
            val stockTicker: Ticker.FT = stockTickers[ticker]!!
            val (amount, bookCost) = stockTicker.let { it.amount to it.bookCost }
            val stockCurrency = stockTicker.currencyOverride ?: asset.currency
            val (pounds, normalisedValue, currency, normalisedCost) = when (stockCurrency) {
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

        fun Map<String, Double>.getPoundValue(ticker: String) = binancePrices[ticker + "GBP"]
            ?: (binancePrices[ticker + "BTC"]!! * binancePrices["BTCGBP"]!!)

        val cryptoDetails = cryptoTickers.map { (ticker, amount) ->
            val cryptoPrice = binancePrices.getPoundValue(ticker)
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

        val record = RecordData().apply {
            put(KEY_STOCKS_TOTAL, stocksTotal.toDouble())
            put(KEY_CRYPTO_TOTAL, cryptoTotal2.toDouble())
            put(KEY_TOTAL, total.toDouble())
            putAll(cryptoTickers.mapValues { (ticker, _) -> binancePrices.getPoundValue(ticker) })
            putAll(ftPrices.mapValues { (_, value) -> value.value })
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

    lateinit var stocksChartUIModel: ChartUIModel
        private set

    lateinit var cryptoChartUIModel: ChartUIModel
        private set

    lateinit var totalChartUIModel: ChartUIModel
        private set

    data class Builder(
        val stocksTotal: Int,
        val stocksGain: Int,
        val cryptoTotal: Int,
        val total: Int,
        val assets: List<AssetUIModel>,
        val recordData: RecordData,
    ) {

        fun withCharts(
            stocksChartUIModel: ChartUIModel,
            cryptoChartUIModel: ChartUIModel,
            totalChartUIModel: ChartUIModel
        ): CombinedModel {
            return CombinedModel(
                stocksTotal,
                stocksGain,
                cryptoTotal,
                total,
                assets,
                recordData
            ).apply {
                this.stocksChartUIModel = stocksChartUIModel
                this.cryptoChartUIModel = cryptoChartUIModel
                this.totalChartUIModel = totalChartUIModel
            }
        }
    }
}