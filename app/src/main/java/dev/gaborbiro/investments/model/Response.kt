package dev.gaborbiro.investments.model

class Response<T> {
    var result: T? = null
        private set

    operator fun component1(): T? {
        return this.result
    }

    var error: String? = null
        private set

    operator fun component2(): String? {
        return this.error
    }

    companion object {

        fun <T> success(result: T) = Response<T>().apply {
            this.result = result
        }

        fun <T> error(error: String) = Response<T>().apply {
            this.error = error
        }
    }
}

suspend fun <T, R> Response<T>.flatMapSuspend(mapper: suspend (T) -> Response<R>): Response<R> {
    val (data, error) = this
    return data
        ?.let {
            try {
                mapper(it)
            } catch (t: Throwable) {
                t.printStackTrace()
                Response.error("Oops! Something went wrong. Check logs.")
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
            Response.error("Oops! Something went wrong. Check logs.")
        }
    }
        ?: run { Response.error(error!!) }
}