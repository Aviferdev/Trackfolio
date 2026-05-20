package es.aviferdev.n3to.core.security

import platform.Foundation.NSUserDefaults

actual fun getSystemLanguage(): String {
    val languages = NSUserDefaults.standardUserDefaults
        .objectForKey("AppleLanguages") as? List<*>
    val preferred = languages?.firstOrNull() as? String ?: return "es"
    // "es-ES" -> "es", "en-US" -> "en"
    return preferred.substringBefore("-").ifEmpty { "es" }
}

actual fun setPlatformLanguage(languageCode: String) {
    val defaults = NSUserDefaults.standardUserDefaults
    val languages = listOf(languageCode)
    defaults.setObject(languages, forKey = "AppleLanguages")
    defaults.synchronize()
}
