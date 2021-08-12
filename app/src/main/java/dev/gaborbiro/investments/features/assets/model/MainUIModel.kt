package dev.gaborbiro.investments.features.assets.model

import dev.gaborbiro.investments.features.assets.SortType

sealed class MainUIModel {

    object Loading : MainUIModel()

    class Data(
        val stocksTotal: Int,
        val stocksGain: Int,
        val cryptoTotal: Int,
        val total: Int,
        val assets: List<AssetUIModel>,
        val stocksChart: ChartUIModel,
        val cryptoChart: ChartUIModel,
        val totalChart: ChartUIModel,
        val sortType: SortType,
    ) : MainUIModel()

    class Error(val message: String) : MainUIModel()
}

class ChartUIModel(val data: List<Pair<Float, Double>>)