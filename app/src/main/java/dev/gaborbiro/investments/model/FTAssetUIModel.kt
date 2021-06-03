package dev.gaborbiro.investments.model

data class FTAssetUIModel(
    val symbol: String,
    val name: String,
    val value: Double,
    val currency: String,
)