package es.aviferdev.n3to.core.security

import java.util.Locale

actual fun getSystemLanguage(): String {
    val lang = Locale.getDefault().language
    return lang.ifEmpty { "es" }
}

actual fun setPlatformLanguage(languageCode: String) {
    Locale.setDefault(Locale(languageCode))
}
