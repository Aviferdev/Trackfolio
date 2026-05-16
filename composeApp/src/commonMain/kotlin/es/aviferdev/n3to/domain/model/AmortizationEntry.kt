package es.aviferdev.n3to.domain.model

/**
 * Fila del cuadro de amortización francesa.
 * No se persiste — se calcula en memoria a partir de los datos del préstamo.
 */
data class AmortizationEntry(
    val installmentNumber: Int,
    val date: Long,
    val monthlyPayment: Double,
    val principalPortion: Double,
    val interestPortion: Double,
    val outstandingBalance: Double
)
