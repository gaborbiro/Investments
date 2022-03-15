package dev.gaborbiro.investments

import java.time.ZonedDateTime

fun ft(lambda: Ticker.FT.Builder.() -> Unit): Ticker.FT {
    val builder = Ticker.FT.Builder()
    lambda(builder)
    return builder.build()
}

fun crypto(lambda: Ticker.Crypto.Builder.() -> Unit): Ticker.Crypto {
    val builder = Ticker.Crypto.Builder()
    lambda(builder)
    return builder.build()
}

sealed class Ticker(open val quantity: Double) {

    data class FT(
        val symbol: String,
        val currencyOverride: String? = null,
        val purchases: List<Purchase>
    ) : Ticker(purchases.sumOf { it.amount }) {

        class Builder {
            var symbol: String? = null
            var currencyOverride: String? = null

            private val purchases = mutableListOf<Purchase>()

            fun purchase(lambda: Purchase.Builder.() -> Unit) {
                val builder = Purchase.Builder()
                lambda(builder)
                purchases.add(builder.build())
            }

            fun build() = FT(symbol!!, currencyOverride, purchases)
        }
    }

    data class Crypto(val symbol: String, override val quantity: Double) : Ticker(quantity) {

        class Builder {
            var symbol: String? = null
            var amount: Double? = null

            fun build() = Crypto(symbol!!, amount!!)
        }
    }
}

data class Purchase(val amount: Double, val cost: Double, val date: ZonedDateTime) {

    class Builder {
        var date: ZonedDateTime? = null
        var amount: Double? = null
        var cost: Double? = null

        fun build() = Purchase(amount!!, cost!!, date!!)
    }
}