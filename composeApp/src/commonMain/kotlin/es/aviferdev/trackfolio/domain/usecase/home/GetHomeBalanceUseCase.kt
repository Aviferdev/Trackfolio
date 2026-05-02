package es.aviferdev.trackfolio.domain.usecase.home

import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.domain.model.HomeBalance
import es.aviferdev.trackfolio.domain.repository.AccountRepository
import es.aviferdev.trackfolio.domain.repository.DebtRepository
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeBalanceUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val debtRepository: DebtRepository
) {
    operator fun invoke(): Flow<HomeBalance> = combine(
        accountRepository.getTotalBalance(),
        debtRepository.getTotalByDirection(DebtDirection.I_OWE),
        debtRepository.getTotalByDirection(DebtDirection.THEY_OWE),
        transactionRepository.getAll()
    ) { totalCash, totalOwing, totalOwed, allTransactions ->
        HomeBalance(
            totalCash = totalCash,
            netBalance = totalCash - totalOwing + totalOwed,
            totalOwed = totalOwed,
            totalOwing = totalOwing,
            recentTransactions = allTransactions.take(3)
        )
    }
}
