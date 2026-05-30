package es.aviferdev.n3to.domain.usecase.goal

import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import es.aviferdev.n3to.domain.repository.GoalRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.platform.nowMonth
import es.aviferdev.n3to.platform.nowYear
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Obtiene el progreso de los objetivos del mes actual para la cuenta dada.
 * Usa el objetivo efectivo: si hay override para el mes lo usa; si no, usa el base.
 */
class GetCurrentMonthProgressUseCase(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val assetTransactionRepository: AssetTransactionRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(accountId: String): Flow<MonthlyGoalProgress> {
        val year = nowYear()
        val month = nowMonth()
        val yearStr = year.toString()
        val monthStr = month.toString().padStart(2, '0')
        val yearMonth = "$yearStr-$monthStr"

        return combine(
            goalRepository.getEffectiveMonthlyGoal(accountId, year, month),
            transactionRepository.getMonthlyTotalsByAccount(accountId, yearStr, monthStr),
            assetTransactionRepository.getMonthlyNetInvestmentByMonth(accountId, yearMonth)
        ) { goal, totals, netInvestment ->
            MonthlyGoalProgress.from(
                year = yearStr,
                month = monthStr,
                goal = goal,
                savingsActual = totals.balance,
                investmentActual = netInvestment?.netAmount ?: 0.0
            )
        }
    }
}
