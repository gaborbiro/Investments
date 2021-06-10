package dev.gaborbiro.investments.features.assets.model

import androidx.annotation.ColorRes

class AssetUIModel(
    val label: String,
    val value: String,
    val details: String,
    val gain: String,
    @ColorRes val gainColor: Int,
)