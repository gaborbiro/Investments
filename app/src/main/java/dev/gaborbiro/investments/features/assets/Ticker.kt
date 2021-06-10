package dev.gaborbiro.investments.features.assets

sealed class Ticker(open val amount: Double) {
    data class FT(val symbol: String, override val amount: Double, val bookCost: Double) : Ticker(amount)
    data class Crypto(val symbol: String, override val amount: Double) : Ticker(amount)
}