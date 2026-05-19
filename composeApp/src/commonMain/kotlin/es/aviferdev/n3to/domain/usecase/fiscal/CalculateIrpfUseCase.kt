package es.aviferdev.n3to.domain.usecase.fiscal

class CalculateIrpfUseCase {
    fun resolveAmount(
        gross: Double,
        ssDeduction: Double,
        isPercentMode: Boolean,
        percent: String,
        fixedAmount: String
    ): Double = if (isPercentMode) {
        val pct = percent.replace(',', '.').toDoubleOrNull() ?: 0.0
        (gross - ssDeduction) * pct / 100.0
    } else {
        fixedAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
    }

    fun resolvePercent(
        gross: Double,
        ssDeduction: Double,
        isPercentMode: Boolean,
        percent: String,
        fixedAmount: String
    ): Double = if (isPercentMode) {
        percent.replace(',', '.').toDoubleOrNull() ?: 0.0
    } else {
        val base = gross - ssDeduction
        val fixed = fixedAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
        if (base > 0) (fixed / base) * 100.0 else 0.0
    }
}
