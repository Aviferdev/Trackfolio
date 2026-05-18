package es.aviferdev.n3to.domain.usecase.home

import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.domain.repository.AccountRepository
import es.aviferdev.n3to.domain.repository.DebtRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetHomeBalanceUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val debtRepository: DebtRepository
) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    operator fun invoke(selectedAccountId: String?): Flow<HomeBalance> =
        accountRepository.getAllAccounts().flatMapLatest { accounts ->
            if (accounts.isEmpty()) {
                flowOf(
                    HomeBalance(
                        selectedAccount        = null,
                        selectedAccountBalance = 0.0,
                        totalOwed              = 0.0,
                        totalOwing             = 0.0,
                        recentTransactions     = emptyList()
                    )
                )
            } else {
                val account = accounts.find { it.id == selectedAccountId } ?: accounts.first()
                combine(
                    transactionRepository.getRecentTransactionsByAccount(account.id, 5L),
                    debtRepository.getTotalByDirectionAndAccount(account.id, DebtDirection.THEY_OWE),
                    debtRepository.getTotalByDirectionAndAccount(account.id, DebtDirection.I_OWE)
                ) { recent, totalOwed, totalOwing ->
                    HomeBalance(
                        selectedAccount        = account,
                        selectedAccountBalance = account.computedBalance,
                        totalOwed              = totalOwed,
                        totalOwing             = totalOwing,
                        recentTransactions     = recent
                    )
                }
            }
        }
}
