package es.aviferdev.trackfolio.security

/**
 * Almacenamiento clave-valor simple multiplataforma.
 * Android: SharedPreferences. iOS: NSUserDefaults.
 */
expect class AppSettings {
    fun getBool(key: String, default: Boolean = false): Boolean
    fun putBool(key: String, value: Boolean)
    fun getString(key: String, default: String = ""): String
    fun putString(key: String, value: String)
}
