package es.aviferdev.n3to.core.security

import platform.Foundation.NSUserDefaults

actual class AppSettings {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getBool(key: String, default: Boolean): Boolean =
        defaults.objectForKey(key)?.let{
            defaults.boolForKey(key)
        } ?: default

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

    actual fun getInt(key: String, default: Int): Int =
        defaults.objectForKey(key)?.let {
            defaults.integerForKey(key).toInt()
        } ?: default

    actual fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
        defaults.synchronize()
    }

    actual fun getLong(key: String, default: Long): Long =
        defaults.objectForKey(key)?.let {
            defaults.integerForKey(key)
        } ?: default

    actual fun putLong(key: String, value: Long) {
        defaults.setInteger(value, forKey = key)
        defaults.synchronize()
    }
}
