package es.aviferdev.trackfolio.domain.loan

import es.aviferdev.trackfolio.domain.model.AmortizationEntry
import es.aviferdev.trackfolio.domain.model.LoanRateChange
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Calculadora de amortización con sistema francés (cuota constante).
 *
 * Cuota = C × [r(1+r)^n] / [(1+r)^n − 1]
 *   C = capital pendiente
 *   r = tipo de interés mensual (anual / 12 / 100)
 *   n = número de cuotas restantes
 */
object FrenchAmortizationCalculator {

    private const val MILLIS_PER_MONTH = 30L * 24 * 60 * 60 * 1000

    /**
     * Calcula la cuota mensual según el modelo francés.
     *
     * @param principal   Capital pendiente.
     * @param annualRate  Tipo de interés anual en porcentaje (ej. 2.5 para 2.5%).
     * @param months      Número de cuotas restantes.
     * @return Cuota mensual redondeada a 2 decimales.
     */
    fun calculateMonthlyPayment(
        principal: Double,
        annualRate: Double,
        months: Int
    ): Double {
        if (months <= 0) return 0.0
        if (principal <= 0.0) return 0.0

        val r = annualRate / 12.0 / 100.0
        if (r <= 0.0) return principal / months // Sin interés → cuota lineal

        val factor = (1 + r).pow(months)
        val payment = principal * (r * factor) / (factor - 1)
        return (payment * 100).roundToLong() / 100.0
    }

    /**
     * Genera el cuadro de amortización completo, teniendo en cuenta
     * posibles cambios históricos de tipo de interés.
     *
     * @param totalAmount       Capital original del préstamo.
     * @param annualRate        Tipo de interés anual inicial (%).
     * @param totalInstallments Número total de cuotas del préstamo.
     * @param startDate         Fecha de inicio en epoch millis.
     * @param rateChanges       Historial de cambios de tipo, ordenados por
     *                          effectiveDate ASC. Cada cambio recalcula la
     *                          cuota desde ese punto.
     * @return Lista completa de [AmortizationEntry].
     */
    fun generateSchedule(
        totalAmount: Double,
        annualRate: Double,
        totalInstallments: Int,
        startDate: Long,
        rateChanges: List<LoanRateChange> = emptyList()
    ): List<AmortizationEntry> {
        if (totalInstallments <= 0 || totalAmount <= 0.0) return emptyList()

        val entries = mutableListOf<AmortizationEntry>()
        var outstanding = totalAmount
        var currentRate = annualRate
        var currentPayment = calculateMonthlyPayment(outstanding, currentRate, totalInstallments)

        // Ordenar cambios de tipo por fecha ascendente
        val sortedChanges = rateChanges.sortedBy { it.effectiveDate }

        for (i in 1..totalInstallments) {
            val date = startDate + i * MILLIS_PER_MONTH

            // Comprobar si hay un cambio de tipo efectivo en esta cuota
            val rateChange = sortedChanges.firstOrNull { change ->
                val changeInstallment = ((change.effectiveDate - startDate) / MILLIS_PER_MONTH).toInt()
                changeInstallment == i
            }
            if (rateChange != null) {
                currentRate = rateChange.newRate
                val remaining = totalInstallments - (i - 1)
                currentPayment = calculateMonthlyPayment(outstanding, currentRate, remaining)
            }

            val r = currentRate / 12.0 / 100.0
            val interestPortion = if (r > 0) outstanding * r else 0.0
            val principalPortion = currentPayment - interestPortion
            outstanding = (outstanding - principalPortion).coerceAtLeast(0.0)

            entries.add(
                AmortizationEntry(
                    installmentNumber = i,
                    date = date,
                    monthlyPayment = currentPayment,
                    principalPortion = (principalPortion * 100).roundToLong() / 100.0,
                    interestPortion = (interestPortion * 100).roundToLong() / 100.0,
                    outstandingBalance = (outstanding * 100).roundToLong() / 100.0
                )
            )
        }

        return entries
    }
}
