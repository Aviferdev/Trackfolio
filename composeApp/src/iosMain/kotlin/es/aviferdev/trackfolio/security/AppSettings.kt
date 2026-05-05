package es.aviferdev.trackfolio.security

import platform.Foundation.NSUserDefaults

actual class AppSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getBool(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) != null) defaults.boolForKey(key)
        else default

    actual fun putBool(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
        defaults.synchronize()
    }

    actual fun getString(key: String, default: String): String =
        defaults.stringForKey(key) ?: default

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
        defaults.synchronize()
    }
}
