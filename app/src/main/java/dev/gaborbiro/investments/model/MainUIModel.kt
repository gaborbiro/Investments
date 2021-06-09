package dev.gaborbiro.investments.model

sealed class MainUIModel {

    object Loading : MainUIModel()

    class Data(
        val stocksTotal: Int,
        val cryptoTotal: Int,
        val total: Int,
        val assets: List<AssetUIModel>
    ) : MainUIModel()

    class Error(val message: String) : MainUIModel()
}