package dev.gaborbiro.investments

import androidx.core.content.edit
import androidx.preference.PreferenceManager

object AppPreferences {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(App.appContext)

    fun setApiKey(apiKey: String) {
        prefs.edit {
            putString(PREFS_API_KEY, apiKey)
        }
    }

    fun getApiKey(): String = prefs.getString(PREFS_API_KEY, "069ac45d727cb260")!!
}

private const val PREFS_API_KEY = "PREFS_API_KEY"