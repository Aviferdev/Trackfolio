package es.aviferdev.trackfolio.core.security

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
 *
 * Para mostrar los saldos cuando están ocultos, se requiere autenticación biométrica
 * si [BiometricAuthenticator] está configurado y disponible en el dispositivo.
 */
class BalanceVisibilityManager(
    private val settings: AppSettings,
    private val appLockManager: AppLockManager,
    private val authenticator: BiometricAuthenticator? = null
) {

    private val _balancesHidden = MutableStateFlow(
        settings.getBool(KEY_BALANCES_HIDDEN, false)
    )

    /** StateFlow observable desde Compose con `collectAsState()`. */
    val balancesHidden: StateFlow<Boolean> = _balancesHidden.asStateFlow()

    /** Lectura síncrona puntual (útil fuera de Compose). */
    val isHidden: Boolean get() = _balancesHidden.value

    private var pendingShowAuth: (() -> Unit)? = null

    /** Indica si hay una autenticación biométrica pendiente. */
    val isPendingBiometricAuth: Boolean get() = pendingShowAuth != null

    /**
     * Oculta los saldos directamente sin requerir biometría.
     * Este método se puede llamar siempre.
     */
    fun hide() {
        if (!_balancesHidden.value) {
            _balancesHidden.value = true
            settings.putBool(KEY_BALANCES_HIDDEN, true)
        }
    }

    /**
     * Intenta mostrar los saldos. Si [authenticator] está disponible y la biometría
     * está habilitada en el dispositivo, Solicita autenticación biométrica.
     * Si la biometría no está disponible o no está habilitada, muestra directamente.
     *
     * @param onBiometricRequiredCallback Callback invoked cuando se requiere biometría
     *        para que la UI muestre el prompt de autenticación.
     */
    fun requestShow(onBiometricRequiredCallback: () -> Unit) {
        if (!_balancesHidden.value) return

        val biometricIsEnabled = appLockManager.biometricEnabled
        val biometricAvailable = authenticator?.isAvailable() == true

        if (biometricIsEnabled && biometricAvailable) {
            pendingShowAuth = {
                _balancesHidden.value = false
                settings.putBool(KEY_BALANCES_HIDDEN, false)
                pendingShowAuth = null
            }
            onBiometricRequiredCallback()
        } else {
            _balancesHidden.value = false
            settings.putBool(KEY_BALANCES_HIDDEN, false)
        }
    }

    /**
     * Called cuando la autenticación biométrica es exitosa.
     */
    fun onBiometricSuccess() {
        pendingShowAuth?.invoke()
    }

    /**
     * Cancela la autenticación biométrica pendiente.
     */
    fun cancelPendingBiometric() {
        pendingShowAuth = null
    }

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
