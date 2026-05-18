package es.aviferdev.n3to.core.ui

/**
 * Envoltorio simple para cadenas que se muestran en la UI.
 * Marca el string para que sea localizable en el futuro.
 *
 * ⚠️ **Los ViewModels no deben usar [UiText.Resource].**
 * En su lugar, deben emitir estados de dominio tipados (sealed classes)
 * y la capa UI resuelve usando `stringResource(Res.string.xxx)` directamente.
 *
 * @see Raw Texto literal sin localizar, para datos dinámicos que no requieren traducción.
 */
sealed class UiText {
    /** Texto literal sin localizar. */
    data class Raw(val text: String) : UiText()

    companion object {
        /** Crea un [UiText.Raw]. */
        fun raw(text: String) = Raw(text)
    }
}
