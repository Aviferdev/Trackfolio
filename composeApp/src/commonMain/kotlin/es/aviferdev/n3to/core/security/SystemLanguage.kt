package es.aviferdev.n3to.core.security

/**
 * Devuelve el código ISO 639-1 del idioma preferido del sistema.
 *
 * Ejemplos: "es", "en", "fr", "de", etc.
 * Siempre devuelve un valor no vacío (fallback "es").
 */
expect fun getSystemLanguage(): String

/**
 * Establece el idioma activo a nivel de plataforma.
 *
 * En Android: cambia [java.util.Locale.setDefault].
 * En iOS: actualiza NSUserDefaults AppleLanguages.
 *
 * Necesario para que [org.jetbrains.compose.resources.stringResource]
 * seleccione el directorio de recursos cualificado correcto (values-en/).
 */
expect fun setPlatformLanguage(languageCode: String)
