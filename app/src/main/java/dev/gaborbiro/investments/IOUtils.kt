package dev.gaborbiro.investments

import com.google.gson.GsonBuilder
import dev.gaborbiro.investments.model.FTErrorResponse
import dev.gaborbiro.investments.model.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

suspend inline fun <reified T> fetch(url: String): Response<T> {
    val gson = GsonBuilder().create()
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val code = (connection as HttpsURLConnection).responseCode

            if (code == 200) {
                val stream = BufferedReader(InputStreamReader(connection.inputStream))
                Response.success(stream.use { gson.fromJson(it, T::class.java) })
            } else {
                val stream = BufferedReader(InputStreamReader(connection.errorStream))
                val errorResponse = stream.use {
                    gson.fromJson(it, FTErrorResponse::class.java)
                }
                Response.error(errorResponse.error.errors.map { it.message }.joinToString(", "))
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Response.error("Oops! Something went wrong. Check logs.")
        }
    }
}