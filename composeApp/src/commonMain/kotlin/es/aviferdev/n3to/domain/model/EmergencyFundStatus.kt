package es.aviferdev.n3to.domain.model

/**
 * Estado del fondo de emergencia para mostrar en la Home.
 *
 * @property isConfigured        `true` si el usuario ha definido un fondo (targetMonths > 0).
 * @property targetAmount        Importe objetivo calculado (€).
 * @property currentBalance      Saldo de la cuenta (computedBalance).
 * @property coveragePercentage  Porcentaje de cobertura: 0.0..1.0.
 * @property isCovered           `true` si currentBalance >= targetAmount.
 * @property missingAmount       Cantidad que falta para cubrir el fondo (0 si está cubierto).
 * @property targetMonths        Meses configurados.
 * @property calculationMethod   Método de cálculo usado.
 * @property monthlyAverage      Media mensual de gastos detectada (solo en AUTO, null si no hay datos).
 * @property calculationMessage  Mensaje informativo (null = todo OK; texto si no hay datos o error).
 */
data class EmergencyFundStatus(
    val isConfigured: Boolean,
    val targetAmount: Double,
    val currentBalance: Double,
    val coveragePercentage: Float,
    val isCovered: Boolean,
    val missingAmount: Double,
    val targetMonths: Int,
    val calculationMethod: EmergencyFundMethod?,
    val monthlyAverage: Double?,
    val calculationMessage: String?
) {
    companion object {
        /** Estado por defecto cuando el fondo no está configurado. */
        val NOT_CONFIGURED = EmergencyFundStatus(
            isConfigured = false,
            targetAmount = 0.0,
            currentBalance = 0.0,
            coveragePercentage = 0f,
            isCovered = false,
            missingAmount = 0.0,
            targetMonths = 0,
            calculationMethod = null,
            monthlyAverage = null,
            calculationMessage = null
        )
    }
}
