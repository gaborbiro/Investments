package dev.gaborbiro.investments.features.assets.model

import android.text.SpannableString
import androidx.annotation.ColorRes

class AssetUIModel(
    val label: String,
    val value: String,
    val details: SpannableString,
    val gain: String,
    @ColorRes val gainColor: Int,
    val yearlyGain: String,
)