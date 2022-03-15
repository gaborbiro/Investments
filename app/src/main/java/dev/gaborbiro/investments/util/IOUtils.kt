package dev.gaborbiro.investments

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.gaborbiro.investments.model.Error
import dev.gaborbiro.investments.features.assets.model.FTErrorResponse
import dev.gaborbiro.investments.model.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger
import javax.net.ssl.HttpsURLConnection

val logger = Logger.getLogger("IO")

suspend inline fun <reified T> fetch(url: String): Response<T> {
    val gson = GsonBuilder().create()
    val type = object : TypeToken<T>() {}.type
    return withContext(Dispatchers.IO) {
        try {
            logger.log(Level.INFO, "Fetching $url")
            val connection = URL(url).openConnection()
            connection.connect()
            val code = (connection as HttpsURLConnection).responseCode

            if (code == 200) {
                val stream = BufferedReader(InputStreamReader(connection.inputStream))
                val ef: T = stream.use { gson.fromJson(it, type) }
                logger.log(Level.INFO, ef.toString())
                Response.success(ef)
            } else {
                val stream = BufferedReader(InputStreamReader(connection.errorStream))
                val errorResponse = stream.use {
                    gson.fromJson(it, FTErrorResponse::class.java)
                }
                val message = errorResponse.error.errors.joinToString("\n") { it.message }
                Response.error(Error.FTServerError(errorResponse.error.errors, message))
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            val error = when (e) {
                is IOException -> Error.NetworkError
                else -> Error.AppError("Oops! Something went wrong. Check logs.")
            }
            Response.error(error)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
suspend fun fetchNewAPIKey(): String? {
    return withContext(Dispatchers.IO) {
        val connection = URL(DOC_BASE_URL).openConnection()
        connection.connect()
        val code = (connection as HttpsURLConnection).responseCode

        if (code == 200) {
            val lines = InputStreamReader(connection.inputStream).readLines()
            val lineWithApiKey = lines.firstOrNull { it.contains("query=pson&amp;source=") }
            lineWithApiKey?.let {
                it.split("source=")[1].split("\"")[0]
            }
        } else null
    }
}