package es.aviferdev.n3to.domain.usecase.goal

import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import es.aviferdev.n3to.domain.repository.GoalRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Obtiene el progreso de todos los meses de un año.
 * Usa el objetivo base + overrides para determinar la meta de cada mes.
 */
class GetYearlyGoalProgressUseCase(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val assetTransactionRepository: AssetTransactionRepository
) {
    operator fun invoke(accountId: String, year: Int): Flow<List<MonthlyGoalProgress>> = flow {
        val yearStr = year.toString()

        // Obtener base y overrides
        val baseGoal = goalRepository.getBaseGoal(accountId, year).first()
        val overrides = goalRepository.getOverrides(accountId, year).first()
        val overridesByMonth = overrides.associateBy { it.month }

        // Totales reales del año
        val breakdown = transactionRepository.getMonthlyBreakdown(accountId, yearStr).first()
        val savingsByMonth = breakdown.associateBy { it.month }

        // Inversión neta del año
        val netInvestments =
            assetTransactionRepository.getMonthlyNetInvestmentsByYear(accountId, yearStr).first()
        val netInvestmentByMonth = netInvestments.associateBy { it.month }

        // Construir progreso para los 12 meses
        val progress = (1..12).map { m ->
            val month = m.toString().padStart(2, '0')
            // Objetivo efectivo: override si existe, sino base
            val goal = overridesByMonth[m] ?: baseGoal?.copy(month = m)

            MonthlyGoalProgress.from(
                year = yearStr,
                month = month,
                goal = goal,
                savingsActual = savingsByMonth[month]?.balance ?: 0.0,
                investmentActual = netInvestmentByMonth[month]?.netAmount ?: 0.0
            )
        }
        emit(progress)
    }
}
