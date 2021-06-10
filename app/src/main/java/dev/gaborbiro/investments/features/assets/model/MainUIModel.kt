package dev.gaborbiro.investments.features.assets.model

sealed class MainUIModel {

    object Loading : MainUIModel()

    class Data(
        val stocksTotal: Int,
        val stocksGain: Int,
        val cryptoTotal: Int,
        val total: Int,
        val assets: List<AssetUIModel>
    ) : MainUIModel()

    class Error(val message: String) : MainUIModel()
}