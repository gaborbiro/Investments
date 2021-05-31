package dev.gaborbiro.investments.model

sealed class Asset(open val amount: Double) {
    data class FT(val symbol: String, override val amount: Double) : Asset(amount)
    data class Crypto(val symbol: String, override val amount: Double) : Asset(amount)
}