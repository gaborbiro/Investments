package dev.gaborbiro.investments.model

data class Security(
    val symbol: String,
    val name: String,
    val value: Float,
    val currency: String,
)