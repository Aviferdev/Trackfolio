package es.aviferdev.n3to.domain.model

/**
 * Detalles fiscales de una transacción de ingreso.
 * Relación 1:1 con [Transaction] (solo para tipo INCOME).
 *
 * Contiene los campos que antes estaban desnormalizados en Transaction:
 *   - incomeType: tipo de ingreso (salario, dividendo, etc.)
 *   - grossAmount: importe bruto antes de deducciones
 *   - commissionAmount: comisiones aplicadas (solo BOND_DEPOSIT)
 *   - issuerId: ID del emisor (FK a IssuerEntity)
 *
 * @property issuerName Nombre del emisor (transitorio, no persistido).
 *   Se resuelve vía lookup a IssuerEntity en el datasource.
 */
data class IncomeTaxDetails(
    val transactionId: String,
    val incomeType: IncomeType? = null,
    val grossAmount: Double? = null,
    val commissionAmount: Double? = null,
    val issuerId: String? = null,
    /** Nombre del emisor (transitorio, no persistido). */
    val issuerName: String? = null
)
