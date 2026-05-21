package es.aviferdev.n3to.domain.usecase.emergencyfund

import es.aviferdev.n3to.domain.model.EmergencyFund
import es.aviferdev.n3to.domain.model.EmergencyFundMethod
import es.aviferdev.n3to.domain.model.EmergencyFundStatus
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.AccountRepository
import es.aviferdev.n3to.domain.repository.EmergencyFundRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.platform.nowLocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

private const val MONTHS_TO_AVERAGE = 12

/**
 * Calcula el estado del fondo de emergencia para la Home.
 *
 * Combina la configuración del fondo con el saldo de la cuenta
 * y (en modo AUTO) el historial de gastos de los últimos 12 meses.
 *
 * Es reactivo a cambios en la configuración del fondo y en el saldo de la cuenta.
 * Para el modo AUTO, los gastos mensuales se obtienen con firstOrNull() —
 * la reactividad se consigue pues la Home re-lanza el flujo al cambiar de cuenta
 * o al re-componerse la pantalla.
 */
class GetEmergencyFundStatusUseCase(
    private val emergencyFundRepository: EmergencyFundRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(accountId: String): Flow<EmergencyFundStatus> =
        emergencyFundRepository.getEmergencyFund(accountId)
            .flatMapLatest { fund ->
                if (fund == null || !fund.isConfigured) {
                    flow { emit(EmergencyFundStatus.NOT_CONFIGURED) }
                } else {
                    accountRepository.getAccountById(accountId).flatMapLatest { account ->
                        flow {
                            val balance = account?.computedBalance ?: 0.0
                            val calc = calculateTarget(fund)
                            emit(
                                computeStatus(
                                    fund = fund,
                                    targetAmount = calc.targetAmount,
                                    currentBalance = balance,
                                    monthlyAverage = calc.monthlyAverage,
                                    message = calc.message
                                )
                            )
                        }
                    }
                }
            }

    private suspend fun calculateTarget(fund: EmergencyFund): TargetResult {
        return when (fund.calculationMethod) {
            EmergencyFundMethod.MANUAL -> manualTarget(fund)
            EmergencyFundMethod.AUTO -> autoTarget(fund)
        }
    }

    private fun manualTarget(fund: EmergencyFund): TargetResult {
        val monthlyExpense = fund.manualMonthlyExpense
        if (monthlyExpense <= 0.0) {
            return TargetResult(
                targetAmount = 0.0,
                monthlyAverage = null,
                message = "Indica tu gasto mensual estimado."
            )
        }
        return TargetResult(
            targetAmount = fund.targetMonths * monthlyExpense,
            monthlyAverage = monthlyExpense,
            message = null
        )
    }

    private suspend fun autoTarget(fund: EmergencyFund): TargetResult {
        val now = nowLocalDateTime()
        val currentYear = now.year
        val currentMonth = now.monthNumber

        val monthlyExpenses = mutableListOf<Double>()

        for (i in 0 until MONTHS_TO_AVERAGE) {
            var month = currentMonth - i
            var year = currentYear
            while (month < 1) {
                month += 12
                year -= 1
            }

            val yearStr = year.toString()
            val monthStr = month.toString().padStart(2, '0')

            // Obtenemos las transacciones del mes (bloqueante sobre Flow,
            // pero aceptable porque se ejecuta en el contexto del ViewModel)
            val transactions = transactionRepository
                .getTransactionsByMonthAndAccount(fund.accountId, yearStr, monthStr)
                .firstOrNull()
                ?: emptyList()

            val expenseTotal = transactions
                .filter { tx ->
                    tx.type == TransactionType.EXPENSE &&
                            tx.linkedAssetTransactionId == null &&
                            tx.categoryId !in fund.excludedCategoryIds
                }
                .sumOf { it.amount }

            if (expenseTotal > 0.0) {
                monthlyExpenses.add(expenseTotal)
            }
        }

        if (monthlyExpenses.isEmpty()) {
            return TargetResult(
                targetAmount = 0.0,
                monthlyAverage = null,
                message = "No hay datos de gastos en los últimos $MONTHS_TO_AVERAGE meses. " +
                        "Registra gastos para usar el cálculo automático."
            )
        }

        val averageExpense = monthlyExpenses.average()
        return TargetResult(
            targetAmount = fund.targetMonths * averageExpense,
            monthlyAverage = averageExpense,
            message = null
        )
    }

    private fun computeStatus(
        fund: EmergencyFund,
        targetAmount: Double,
        currentBalance: Double,
        monthlyAverage: Double?,
        message: String?
    ): EmergencyFundStatus {
        if (targetAmount <= 0.0) {
            return EmergencyFundStatus(
                isConfigured = true,
                targetAmount = 0.0,
                currentBalance = currentBalance,
                coveragePercentage = 1f,
                isCovered = true,
                missingAmount = 0.0,
                targetMonths = fund.targetMonths,
                calculationMethod = fund.calculationMethod,
                monthlyAverage = monthlyAverage,
                calculationMessage = message
            )
        }

        val coverage = (currentBalance / targetAmount).toFloat().coerceIn(0f, 1f)
        val isCovered = currentBalance >= targetAmount
        val missing = maxOf(targetAmount - currentBalance, 0.0)

        return EmergencyFundStatus(
            isConfigured = true,
            targetAmount = targetAmount,
            currentBalance = currentBalance,
            coveragePercentage = coverage,
            isCovered = isCovered,
            missingAmount = missing,
            targetMonths = fund.targetMonths,
            calculationMethod = fund.calculationMethod,
            monthlyAverage = monthlyAverage,
            calculationMessage = message
        )
    }

    private data class TargetResult(
        val targetAmount: Double,
        val monthlyAverage: Double?,
        val message: String?
    )
}
