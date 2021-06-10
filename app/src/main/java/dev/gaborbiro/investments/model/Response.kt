package dev.gaborbiro.investments.model

import dev.gaborbiro.investments.features.assets.model.FTErrorItem

class Response<T> {
    var result: T? = null
        private set

    operator fun component1(): T? {
        return this.result
    }

    var error: Error? = null
        private set

    operator fun component2(): Error? {
        return this.error
    }

    override fun toString(): String {
        return error?.let {
            "Response(error=$it)"
        } ?: run {
            "Response(result=$result)"
        }
    }

    companion object {

        fun <T> success(result: T) = Response<T>().apply {
            this.result = result
        }

        fun <T> error(error: Error) = Response<T>().apply {
            this.error = error
        }
    }


}

sealed class Error {
    data class FTServerError(val errors: List<FTErrorItem>, val uiMessage: String) : Error()
    data class AppError(val uiMessage: String) : Error()
    object NetworkError : Error()
}

suspend fun <T, R> Response<T>.flatMapSuspend(mapper: suspend (T) -> Response<R>): Response<R> {
    val (data, error) = this
    return data
        ?.let {
            try {
                mapper(it)
            } catch (t: Throwable) {
                t.printStackTrace()
                Response.error(Error.AppError("Oops! Something went wrong. Check logs."))
            }
        }
        ?: run { Response.error(error!!) }
}

fun <T, R> Response<T>.flatMap(mapper: (T) -> R): Response<R> {
    val (data, error) = this
    return data?.let {
        try {
            Response.success(mapper(it))
        } catch (t: Throwable) {
            Response.error(Error.AppError("Oops! Something went wrong. Check logs."))
        }
    } ?: run { Response.error(error!!) }
}