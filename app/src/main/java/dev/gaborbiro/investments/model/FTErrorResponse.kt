package dev.gaborbiro.investments.model

class FTErrorResponse(
    val error: FTError,
)

class FTError(
    val code: Int,
    val message: String,
    val errors: List<FTErrorItem>,
)

class FTErrorItem(
    val reason: String,
    val message: String,
)