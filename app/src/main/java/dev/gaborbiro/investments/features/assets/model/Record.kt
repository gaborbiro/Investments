package dev.gaborbiro.investments.features.assets.model

class Record(val data: HashMap<String, Double>)

class RecordData : HashMap<String, Double>()

const val KEY_STOCKS_TOTAL = "stocksTotal"
const val KEY_CRYPTO_TOTAL = "cryptoTotal"
const val KEY_TOTAL = "total"
