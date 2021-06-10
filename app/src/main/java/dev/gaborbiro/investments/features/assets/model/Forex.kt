package dev.gaborbiro.investments.features.assets.model

import com.google.gson.annotations.SerializedName

class Forex(val rates: Rates) {

    class Rates(@SerializedName("GBP") val gbp: Double)
}