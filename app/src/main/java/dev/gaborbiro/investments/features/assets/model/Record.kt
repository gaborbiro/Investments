package dev.gaborbiro.investments.features.assets.model

class Record(val data: RecordData)

class RecordData(
    val stocksTotal: Int,
    val cryptoTotal: Int,
    val total: Int,
)