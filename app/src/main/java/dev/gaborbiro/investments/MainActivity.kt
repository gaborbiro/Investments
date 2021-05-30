package dev.gaborbiro.investments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dev.gaborbiro.investments.databinding.ActivityMainBinding
import dev.gaborbiro.investments.model.ForexResponse
import dev.gaborbiro.investments.model.Security
import dev.gaborbiro.investments.model.TimeSeriesResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val symbols = mapOf(
            "GOOGL:NSQ" to 7.00f,
            "GB00B4W52V57:GBX" to 615.385f,
            "GB0006061963:GBX" to 37.835f,
            "GB00B7QK1Y37:GBX" to 4043.915f,
            "GB00B7VT0938:GBX" to 1105.97f,
            "COF:NYQ" to 39.00f,
            "FB:NSQ" to 29.00f,
            "0R06:LSE" to 11.00f,
            "GSK:LSE" to 30.00f,
            "SWDA:LSE:GBX" to 43.00f,
            "IWFV:LSE:GBX" to 139.00f,
            "NG.:LSE" to 45.00f,
            "PBEE:LSE" to 54.00f,
            "ULVR:LSE" to 9.00f,
            "VWRL:LSE:GBP" to 14.00f,
            "GB00BD3RZ368:GBP" to 16.6694f,
            "VOD:LSE" to 325.00f,
        )
        binding.text.text = "Fetching..."
        CoroutineScope(Dispatchers.IO).launch {
            val usd2gbp = fetchUSD2GBPRate()
            val securities = fetchSecurities(symbols.map { it.key })
            var total = 0f
            CoroutineScope(Dispatchers.Main).launch {
                val text = securities.map {
                    val sharePrice = mapValueToPounds(it.value.value, it.value.currency, usd2gbp)
                    val shares = symbols[it.key]!!
                    val value = sharePrice * shares
                    total += value
                    "${it.key} (${it.value.name}) => £$sharePrice x $shares = £$value"
                }.joinToString("\n\n")
                binding.text.text = "Total: £$total\n\n\n" +
                        "$text\n" +
                        "(USD2GBP: ${usd2gbp})"
            }
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

    private suspend fun fetchSecurities(symbols: List<String>): Map<String, Security> {
        val (success, error) = fetch<TimeSeriesResponse>(BASE_URL.format(symbols.joinToString(",")))
        error?.let(this::handleError)
        return success?.let {
            it.data.items.map {
                it.basic.symbol to Security(
                    symbol = it.basic.symbol,
                    name = it.basic.name,
                    value = it.timeSeries.lastPrice.toFloat(),
                    currency = it.basic.currency,
                )
            }.associate { it }
        } ?: emptyMap()
    }

    private suspend fun fetchUSD2GBPRate(): Float {
        val (success, error) = fetch<ForexResponse>(USD2GBP_BASE_URL)
        error?.let(this::handleError)
        return success?.rates?.gbp ?: 0f
    }

    private fun handleError(error: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.text.text = "Error: $error"
        }
    }
}

