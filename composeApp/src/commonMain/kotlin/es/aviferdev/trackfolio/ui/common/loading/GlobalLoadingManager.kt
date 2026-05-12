package es.aviferdev.trackfolio.ui.common.loading

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor centralizado del estado de carga global de la aplicación.
 *
 * Utiliza un contador interno para soportar activaciones concurrentes
 * desde múltiples ViewModels. El overlay permanece visible mientras
 * el contador sea > 0.
 *
 * Cada llamada a [show] debe tener su correspondiente [hide] (usar
 * try/finally en corrutinas).
 */
class GlobalLoadingManager {

    private var counter = 0

    private val _isLoading = MutableStateFlow(false)
    private val _loadingMessage = MutableStateFlow<String?>(null)

    /** Indica si el overlay de carga debe mostrarse. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Mensaje opcional que se muestra bajo el spinner. */
    val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

    /**
     * Activa el loading global.
     * @param message Texto opcional que aparecerá bajo el spinner.
     */
    fun show(message: String? = null) {
        counter++
        _isLoading.value = true
        if (message != null) {
            _loadingMessage.value = message
        }
    }

    /**
     * Desactiva el loading global.
     * Solo oculta el overlay cuando todas las activaciones concurrentes
     * han llamado a [hide].
     */
    fun hide() {
        counter = maxOf(0, counter - 1)
        if (counter == 0) {
            _isLoading.value = false
            _loadingMessage.value = null
        }
    }
}
