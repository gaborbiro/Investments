package dev.gaborbiro.investments.features.assets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.AppBarLayout
import dev.gaborbiro.investments.R
import dev.gaborbiro.investments.databinding.ActivityMainBinding
import dev.gaborbiro.investments.databinding.ListItemAssetBinding
import dev.gaborbiro.investments.databinding.ListItemFilterBinding
import dev.gaborbiro.investments.features.assets.model.AssetUIModel
import dev.gaborbiro.investments.features.assets.model.ChartUIModel
import dev.gaborbiro.investments.features.assets.model.MainUIModel
import dev.gaborbiro.investments.util.hide
import dev.gaborbiro.investments.observe
import dev.gaborbiro.investments.util.show
import dev.gaborbiro.investments.util.ConstraintTransitionListener
import dev.gaborbiro.investments.util.CrossContainerRadioGroup
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
    private var appBarLayoutListener: AppBarLayout.OnOffsetChangedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initUI()
        viewModel.loadData(SortType.ORIGINAL)
        observe(viewModel.uiModel, ::render)
    }

    private fun initUI() {
        initChart(binding.stocksSharesGraph, binding.stocksSharesMaximize)
        initChart(binding.cryptoGraph, binding.cryptoMaximize)
        initChart(binding.totalGraph, binding.totalMaximize)
    }

    private fun render(model: MainUIModel?) {
        when (model) {
            is MainUIModel.Loading -> showProgress(true)
            is MainUIModel.Error -> {
                showProgress(false)
                showError(model.message)
            }
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
                binding.stocksSharesGain.text = stocksValue.getHighlightedText(
                    stocksGain,
                    gainColor,
                    highlightForeground = true
                )
                binding.stocksSharesDayChange.text = model.stocksDayChange
                binding.cryptoValue.text = "£${model.cryptoTotal.bigMoney()}"
                binding.totalValue.text = "£${model.total.bigMoney()}"
                binding.container.removeAllViews()
                addFilterRowToUI(model.sortType)
                model.assets.forEach(::addAssetRowToUI)

                appBarLayoutListener = setupToolbarAnimation()

                setupChart(binding.stocksSharesGraph, model.stocksChart)
                setupChart(binding.cryptoGraph, model.cryptoChart)
                setupChart(binding.totalGraph, model.totalChart)
            }
        }
    }

    private fun initChart(lineChart: LineChart, maximizeButton: ImageView) {
        with(lineChart) {
            setNoDataText("Loading...")
            setMaxVisibleValueCount(10)
            with(xAxis) {
                textColor = ContextCompat.getColor(this@MainActivity, R.color.white)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                labelCount = 5
                textSize = 14f
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = DayAxisValueFormatter()
                legend.isEnabled = false
            }
            with(axisLeft) {
                textColor = ContextCompat.getColor(this@MainActivity, R.color.white)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                labelCount = 3
                textSize = 14f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val thousands = value / 1000f
                        return "£${thousands.bigMoney()}k"
                    }
                }
            }
            with(axisRight) {
                isEnabled = false
            }
            isHighlightPerTapEnabled = false
            isHighlightPerDragEnabled = false
            description.isEnabled = false
            extraBottomOffset = 10f
            extraTopOffset = 0f
            setTouchEnabled(true)
        }
        setupChartAnimation(lineChart, maximizeButton)
    }

    private fun setupChart(lineChart: LineChart, chartData: ChartUIModel) {
        val dataSet = LineDataSet(
            chartData.data.map { (day, value) -> Entry(day, value.toFloat()) },
            ""
        )
        with(dataSet) {
            setDrawValues(true)
            color = ContextCompat.getColor(this@MainActivity, R.color.purple_200)
            lineWidth = 3f
            setDrawCircles(false)
            valueTextColor = ContextCompat.getColor(this@MainActivity, R.color.white)
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "£${value.bigMoney()}"
                }
            }
        }
        with(lineChart) {
            data = LineData(dataSet)
            animateY(1000, Easing.EaseInOutCubic)
            invalidate()
        }
    }

    private fun setupToolbarAnimation(): AppBarLayout.OnOffsetChangedListener {
        val stocksY = binding.stocksSharesGraph.y - binding.stocksSharesLabel.height
        val cryptoY = binding.cryptoGraph.y - binding.cryptoLabel.height
        val totalY = binding.totalGraph.y - binding.totalLabel.height

        val rowHeight = dpToPx(40)
        return AppBarLayout.OnOffsetChangedListener { appBar, offset ->
            val scrollY = -offset.toFloat()
            val open = 1 - (scrollY / appBar.totalScrollRange)
            binding.stocksSharesGraph.alpha = open * open
            binding.stocksSharesMaximize.alpha = open * open
            binding.cryptoGraph.alpha = open * open
            binding.cryptoMaximize.alpha = open * open
            binding.totalGraph.alpha = open * open
            binding.totalMaximize.alpha = open * open

            binding.stocksSharesLabel.y = scrollY + stocksY
            binding.stocksSharesGain.y = scrollY + stocksY
            binding.stocksSharesDayChange.y = scrollY + stocksY

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
        }.also {
            binding.appbar.addOnOffsetChangedListener(it)
        }
    }

    private fun addFilterRowToUI(sortType: SortType = SortType.ORIGINAL) {
        val binding = DataBindingUtil.inflate<ListItemFilterBinding>(
            layoutInflater,
            R.layout.list_item_filter,
            binding.container,
            /* attachToParent: */ true
        )
        binding.radioButtonOriginal.isChecked = false
        binding.radioButtonDayChange.isChecked = false
        binding.radioButtonAlltimeChange.isChecked = false
        when (sortType) {
            SortType.ORIGINAL -> binding.radioButtonOriginal.isChecked = true
            SortType.DAY_GAIN_LOSS -> binding.radioButtonDayChange.isChecked = true
            SortType.GAIN_LOSS -> binding.radioButtonAlltimeChange.isChecked = true
        }
        CrossContainerRadioGroup(
            binding.radioButtonOriginal,
            binding.radioButtonDayChange,
            binding.radioButtonAlltimeChange
        ) {
            when (it.id) {
                R.id.radio_button_original -> {
                    viewModel.loadData(SortType.ORIGINAL)
                }
                R.id.radio_button_day_change -> {
                    viewModel.loadData(SortType.DAY_GAIN_LOSS)
                }
                R.id.radio_button_alltime_change -> {
                    viewModel.loadData(SortType.GAIN_LOSS)
                }
            }
        }
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
            binding.stocksSharesGraph.setNoDataText("Error")
            binding.cryptoGraph.setNoDataText("Error")
            binding.totalGraph.setNoDataText("Error")
            binding.stocksSharesGraph.invalidate()
            binding.cryptoGraph.invalidate()
            binding.totalGraph.invalidate()
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

    private fun setupChartAnimation(lineChart: LineChart, maximizeButton: ImageView) {
        maximizeButton.setOnClickListener {
            with(ConstraintSet()) {
                clone(binding.header)
                val chartHeight = resources.getDimensionPixelSize(R.dimen.chart_height)
                val tallChartHeight = resources.getDimensionPixelSize(R.dimen.chart_height_tall)
                val (newHeight, labelCount) = if (lineChart.height > chartHeight) {
                    maximizeButton.setImageResource(R.drawable.ic_fullscreen)
                    lineChart.axisLeft.labelCount = 3
                    lineChart.invalidate()
                    chartHeight to 3
                } else {
                    maximizeButton.setImageResource(R.drawable.ic_fullscreen_exit)
                    tallChartHeight to 9
                }
                constrainHeight(lineChart.id, newHeight)
                binding.appbar.removeOnOffsetChangedListener(appBarLayoutListener)
                TransitionManager.beginDelayedTransition(binding.header, AutoTransition().apply {
                    addListener(object : ConstraintTransitionListener() {
                        override fun onTransitionEnd(transition: Transition) {
                            appBarLayoutListener = setupToolbarAnimation()
                            binding.appbar.addOnOffsetChangedListener(appBarLayoutListener)
                            lineChart.axisLeft.labelCount = labelCount
                            lineChart.invalidate()
                        }
                    })
                })
                applyTo(binding.header)
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