package es.aviferdev.trackfolio.security

import android.content.Context
import android.content.SharedPreferences

actual class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("trackfolio_prefs", Context.MODE_PRIVATE)

    actual fun getBool(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)

    actual fun putBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual fun getString(key: String, default: String): String =
        prefs.getString(key, default) ?: default

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
