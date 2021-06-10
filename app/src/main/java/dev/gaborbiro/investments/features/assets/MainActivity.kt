package dev.gaborbiro.investments.features.assets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.appbar.AppBarLayout
import dev.gaborbiro.investments.R
import dev.gaborbiro.investments.databinding.ActivityMainBinding
import dev.gaborbiro.investments.databinding.ListItemAssetBinding
import dev.gaborbiro.investments.features.assets.model.AssetUIModel
import dev.gaborbiro.investments.features.assets.model.MainUIModel
import dev.gaborbiro.investments.hide
import dev.gaborbiro.investments.observe
import dev.gaborbiro.investments.show
import getHighlightedText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.absoluteValue

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel.init()
        observe(viewModel.uiModel, ::render)
    }

    private fun render(model: MainUIModel?) {
        when (model) {
            is MainUIModel.Loading -> showProgress(true)
            is MainUIModel.Error -> showError(model.message)
            is MainUIModel.Data -> {
                showProgress(false)
                val stocksGain = if (model.stocksGain < 0) {
                    "-£${model.stocksGain.absoluteValue.bigMoney()}"
                } else {
                    "+£${model.stocksGain.absoluteValue.bigMoney()}"
                }
                val gainColor =
                    (if (model.stocksGain > 0) R.color.red_light else R.color.green_light).let {
                        ContextCompat.getColor(this, it)
                    }
                val stocksValue = "£${model.stocksTotal.bigMoney()} ($stocksGain)"
                binding.stocksSharesValue.text = stocksValue.getHighlightedText(
                    stocksGain,
                    gainColor,
                    highlightForeground = true
                )
                binding.cryptoValue.text = "£${model.cryptoTotal.bigMoney()}"
                binding.totalValue.text = "£${model.total.bigMoney()}"
                model.assets.forEach(::addAssetRowToUI)
                setupToolbarAnimation()
            }
        }
    }

    private fun setupToolbarAnimation() {
        val stocksY = binding.stocksSharesLabel.y
        val cryptoY = binding.cryptoLabel.y
        val totalY = binding.totalLabel.y

        val rowHeight = dpToPx(40)

        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar, offset ->
            val scrollY = -offset.toFloat()
            val open = 1 - (scrollY / appBar.totalScrollRange)
            binding.stocksSharesGraph.alpha = open * open
            binding.cryptoGraph.alpha = open * open
            binding.totalGraph.alpha = open * open

            binding.stocksSharesLabel.y = scrollY + stocksY
            binding.stocksSharesValue.y = scrollY + stocksY

            if (scrollY > cryptoY - rowHeight) {
                binding.cryptoLabel.y = scrollY + rowHeight
                binding.cryptoValue.y = scrollY + rowHeight
            } else {
                binding.cryptoLabel.y = cryptoY
                binding.cryptoValue.y = cryptoY
            }

            if (scrollY > totalY - 2 * rowHeight) {
                binding.totalLabel.y = scrollY + 2 * rowHeight
                binding.totalValue.y = scrollY + 2 * rowHeight
            } else {
                binding.totalLabel.y = totalY
                binding.totalValue.y = totalY
            }
        })
    }

    private fun addAssetRowToUI(asset: AssetUIModel) {
        val binding = DataBindingUtil.inflate<ListItemAssetBinding>(
            layoutInflater,
            R.layout.list_item_asset,
            binding.container,
            /* attachToParent: */ true
        )
        binding.item = asset
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

fun Number.bigMoney(): String {
    val format = DecimalFormat("#,###")
    return format.format(this)
}

fun Number.money(): String {
    val format = DecimalFormat("#,###.##")
    (format as NumberFormat).minimumFractionDigits = 2
    return format.format(this)
}

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}