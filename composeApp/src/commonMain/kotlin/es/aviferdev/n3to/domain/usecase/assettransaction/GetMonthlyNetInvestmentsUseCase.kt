package es.aviferdev.n3to.domain.usecase.assettransaction

import es.aviferdev.n3to.domain.model.MonthlyNetInvestment
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para obtener la inversión neta mensual (BUY - SELL).
 * A diferencia de [GetMonthlyInvestmentsUseCase], descuenta las ventas
 * para reflejar el flujo neto de caja hacia el portfolio.
 */
class GetMonthlyNetInvestmentsUseCase(
    private val repository: AssetTransactionRepository
) {
    /** Inversión neta de todos los meses de un año. */
    operator fun invoke(accountId: String, year: String): Flow<List<MonthlyNetInvestment>> =
        repository.getMonthlyNetInvestmentsByYear(accountId, year)

    /** Inversión neta de un mes concreto (formato yearMonth: "2026-01"). */
    fun getByMonth(accountId: String, yearMonth: String): Flow<MonthlyNetInvestment?> =
        repository.getMonthlyNetInvestmentByMonth(accountId, yearMonth)
}
