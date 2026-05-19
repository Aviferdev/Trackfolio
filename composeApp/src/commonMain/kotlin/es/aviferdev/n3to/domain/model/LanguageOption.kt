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
        val FRENCH = LanguageOption(
            code = "fr",
            flag = "\uD83C\uDDEB\uD83C\uDDF7",
            displayNameKey = "language_french",
            isBeta = true
        )
        val GERMAN = LanguageOption(
            code = "de",
            flag = "\uD83C\uDDE9\uD83C\uDDEA",
            displayNameKey = "language_german",
            isBeta = true
        )
        val CHINESE = LanguageOption(
            code = "zh",
            flag = "\uD83C\uDDE8\uD83C\uDDF3",
            displayNameKey = "language_chinese",
            isBeta = true
        )
        val RUSSIAN = LanguageOption(
            code = "ru",
            flag = "\uD83C\uDDF7\uD83C\uDDFA",
            displayNameKey = "language_russian",
            isBeta = true
        )
        val JAPANESE = LanguageOption(
            code = "ja",
            flag = "\uD83C\uDDEF\uD83C\uDDF5",
            displayNameKey = "language_japanese",
            isBeta = true
        )
        val PORTUGUESE = LanguageOption(
            code = "pt",
            flag = "\uD83C\uDDF5\uD83C\uDDF9",
            displayNameKey = "language_portuguese",
            isBeta = true
        )
        val ITALIAN = LanguageOption(
            code = "it",
            flag = "\uD83C\uDDEE\uD83C\uDDF9",
            displayNameKey = "language_italian",
            isBeta = true
        )
        val KOREAN = LanguageOption(
            code = "ko",
            flag = "\uD83C\uDDF0\uD83C\uDDF7",
            displayNameKey = "language_korean",
            isBeta = true
        )

        val ALL = listOf(
            SYSTEM, SPANISH, ENGLISH,
            FRENCH, GERMAN, PORTUGUESE, ITALIAN,
            RUSSIAN, CHINESE, KOREAN, JAPANESE
        )
    }
}
