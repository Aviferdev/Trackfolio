package es.aviferdev.n3to.domain.model

/**
 * Representa una opción de idioma disponible en el selector.
 *
 * @param code Código ISO 639-1: "" para sistema, "es", "en", etc.
 * @param flag Emoji de bandera o globe para "Idioma del sistema".
 * @param displayNameKey Clave para el string resource del nombre visible.
 * @param isBeta Si es true, muestra una etiqueta "Beta" junto al nombre.
 */
data class LanguageOption(
    val code: String,
    val flag: String,
    val displayNameKey: String,
    val isBeta: Boolean = false
) {
    companion object {
        val SYSTEM = LanguageOption(
            code = "",
            flag = "\uD83C\uDF10",
            displayNameKey = "language_system"
        )
        val SPANISH = LanguageOption(
            code = "es",
            flag = "\uD83C\uDDEA\uD83C\uDDF8",
            displayNameKey = "language_spanish"
        )
        val ENGLISH = LanguageOption(
            code = "en",
            flag = "\uD83C\uDDEC\uD83C\uDDE7",
            displayNameKey = "language_english",
            isBeta = true
        )

        val ALL = listOf(SYSTEM, SPANISH, ENGLISH)
    }
}
