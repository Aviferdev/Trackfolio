package es.aviferdev.n3to.core.security

import android.content.res.Resources
import java.util.Locale

/**
 * Lee el idioma REAL del dispositivo, ignorando cualquier sobreescritura
 * previa hecha con [setPlatformLanguage] mediante [Locale.setDefault].
 * Usamos [Resources.getSystem] porque refleja la configuración del sistema,
 * no la configuración forzada por la app.
 */
actual fun getSystemLanguage(): String {
    val config = Resources.getSystem().configuration
    val lang = config.locales.get(0).language
    return lang.ifEmpty { "es" }
}

actual fun setPlatformLanguage(languageCode: String) {
    Locale.setDefault(Locale(languageCode))
}
