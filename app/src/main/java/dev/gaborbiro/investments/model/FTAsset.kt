package dev.gaborbiro.investments.model

data class FTAsset(
    val symbol: String,
    val name: String,
    val value: Double,
    val currency: String,
)