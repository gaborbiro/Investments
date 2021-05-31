package dev.gaborbiro.investments.model

class BinanceExchangeInfo(
    val symbols: List<BinanceSymbol>
)

class BinanceSymbol(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
)