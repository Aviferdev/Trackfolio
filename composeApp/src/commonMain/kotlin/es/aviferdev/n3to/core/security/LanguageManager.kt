package es.aviferdev.n3to.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_APP_LANGUAGE = "app_language"

/**
 * Gestiona la preferencia de idioma del usuario.
 *
 * - Si el usuario nunca ha seleccionado un idioma, usa [getSystemLanguage].
 * - Al elegir "Idioma del sistema", borra la preferencia para que futuros
 *   inicios relean [getSystemLanguage] (capturando cambios del SO).
 * - Expone un [StateFlow] para que la UI reaccione en tiempo real.
 */
class LanguageManager(private val settings: AppSettings) {

    private val _languageCode = MutableStateFlow(resolveLanguage(settings))
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    /** `true` si el usuario nunca ha seleccionado un idioma manualmente. */
    val isSystemDefault: Boolean
        get() = settings.getString(KEY_APP_LANGUAGE, "").isEmpty()

    /**
     * Cambia el idioma activo.
     * @param code Código ISO 639-1. Pasa `""` para volver al idioma del sistema.
     */
    fun setLanguage(code: String) {
        val effective = code.ifEmpty { getSystemLanguage() }
        if (_languageCode.value == effective) return
        _languageCode.value = effective
        if (code.isEmpty()) {
            // "Idioma del sistema": borrar preferencia para que futuros inicios
            // vuelvan a leer el idioma del sistema (que puede haber cambiado).
            settings.putString(KEY_APP_LANGUAGE, "")
        } else {
            settings.putString(KEY_APP_LANGUAGE, code)
        }
    }

    private companion object {
        private fun resolveLanguage(settings: AppSettings): String {
            val saved = settings.getString(KEY_APP_LANGUAGE, "")
            return saved.ifEmpty { getSystemLanguage() }
        }
    }
}
