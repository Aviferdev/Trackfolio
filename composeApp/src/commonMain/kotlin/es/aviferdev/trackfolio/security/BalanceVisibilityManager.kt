package es.aviferdev.trackfolio.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_BALANCES_HIDDEN = "balances_hidden"

/**
 * Gestiona la visibilidad global de los saldos de la app.
 *
 * Patrón consistente con [AppLockManager]:
 *  - Persistencia en [AppSettings] (SharedPreferences en Android, NSUserDefaults en iOS).
 *  - Estado reactivo expuesto como [StateFlow] para que la UI lo observe vía
 *    `collectAsState()` o a través del `CompositionLocal` `LocalBalanceHidden`.
 *
 * Cuando `balancesHidden == true`, las pantallas de saldos, deudas, portfolio,
 * movimientos y resumen anual sustituyen los importes por una máscara («•••••»),
 * preservando moneda, signo y prefijos (+ / −) para no romper la maquetación.
 *
 * El estado se persiste entre sesiones, así que si el usuario cierra la app con
 * los saldos ocultos los seguirá viendo ocultos al volver — útil para evitar
 * exposición accidental de información financiera en miradas casuales.
 */
class BalanceVisibilityManager(private val settings: AppSettings) {

    private val _balancesHidden = MutableStateFlow(
        settings.getBool(KEY_BALANCES_HIDDEN, false)
    )

    /** StateFlow observable desde Compose con `collectAsState()`. */
    val balancesHidden: StateFlow<Boolean> = _balancesHidden.asStateFlow()

    /** Lectura síncrona puntual (útil fuera de Compose). */
    val isHidden: Boolean get() = _balancesHidden.value

    /** Invierte el estado actual y lo persiste. */
    fun toggle() {
        val newValue = !_balancesHidden.value
        _balancesHidden.value = newValue
        settings.putBool(KEY_BALANCES_HIDDEN, newValue)
    }

    /** Fija explícitamente el estado y lo persiste. */
    fun setHidden(hidden: Boolean) {
        if (_balancesHidden.value == hidden) return
        _balancesHidden.value = hidden
        settings.putBool(KEY_BALANCES_HIDDEN, hidden)
    }
}
