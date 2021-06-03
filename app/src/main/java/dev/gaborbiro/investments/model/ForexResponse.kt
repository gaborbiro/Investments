package dev.gaborbiro.investments.model

import com.google.gson.annotations.SerializedName

class ForexResponse(val rates: Rates) {

    class Rates(@SerializedName("GBP") val gbp: Double)
}