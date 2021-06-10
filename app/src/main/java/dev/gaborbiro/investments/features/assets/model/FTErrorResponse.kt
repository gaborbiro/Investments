package dev.gaborbiro.investments.features.assets.model

data class FTErrorResponse(
    val error: FTError,
)

data class FTError(
    val code: Int,
    val message: String,
    val errors: List<FTErrorItem>,
)

data class FTErrorItem(
    val reason: String,
    val message: String,
)