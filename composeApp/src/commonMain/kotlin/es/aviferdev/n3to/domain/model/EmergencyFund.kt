package es.aviferdev.n3to.domain.model

/**
 * Configuración del fondo de emergencia para una cuenta.
 *
 * @property accountId            Cuenta a la que pertenece la configuración.
 * @property targetMonths         Meses que se quieren cubrir (0 = no configurado).
 * @property calculationMethod    Método de cálculo: MANUAL o AUTO.
 * @property manualMonthlyExpense Gasto mensual estimado (solo relevante en MANUAL).
 * @property excludedCategoryIds  IDs de categorías de gasto a excluir del cálculo en AUTO.
 */
data class EmergencyFund(
    val accountId: String,
    val targetMonths: Int,
    val calculationMethod: EmergencyFundMethod,
    val manualMonthlyExpense: Double,
    val excludedCategoryIds: List<String>
) {
    /** `true` si el fondo está configurado (meses > 0). */
    val isConfigured: Boolean get() = targetMonths > 0
}
