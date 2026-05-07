package es.aviferdev.trackfolio.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

actual class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("trackfolio_prefs", Context.MODE_PRIVATE)

    actual fun getBool(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)

    actual fun putBool(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    actual fun getString(key: String, default: String): String =
        prefs.getString(key, default) ?: default

    actual fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    actual fun getInt(key: String, default: Int): Int =
        prefs.getInt(key, default)

    actual fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    actual fun getLong(key: String, default: Long): Long =
        prefs.getLong(key, default)

    actual fun putLong(key: String, value: Long) {
        prefs.edit { putLong(key, value) }
    }
}
