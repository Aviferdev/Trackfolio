package es.aviferdev.trackfolio.domain.usecase.networth

import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.model.NetWorthData
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.domain.repository.AccountRepository
import es.aviferdev.trackfolio.domain.repository.AssetRepository
import es.aviferdev.trackfolio.domain.repository.AssetTransactionRepository
import es.aviferdev.trackfolio.domain.repository.DebtRepository
import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository
import es.aviferdev.trackfolio.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetNetWorthDataUseCase(
    private val accountRepository: AccountRepository,
    private val assetRepository: AssetRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val fixedIncomeRepository: FixedIncomeRepository,
    private val loanRepository: LoanRepository,
    private val debtRepository: DebtRepository
) {
    operator fun invoke(accountId: String): Flow<NetWorthData> {
        // Combinar en grupos para evitar problemas de inferencia con 6 parámetros
        val accountFlow = accountRepository.getAccountById(accountId)
        val assetsFlow = assetRepository.getAssetsByAccount(accountId)
        val txsFlow = assetTransactionRepository.getByAccount(accountId)
        val fiFlow = fixedIncomeRepository.getOpenByAccount(accountId)
        val loansFlow = loanRepository.getActiveByAccount(accountId)
        val debtsFlow = debtRepository.getTotalByDirectionAndAccount(accountId, DebtDirection.I_OWE)

        // Combinar primeros 3 flujos
        val part1 = combine(accountFlow, assetsFlow, txsFlow) { account, assets, txs ->
            Triple(account, assets, txs)
        }

        // Combinar siguientes 3 flujos
        val part2 = combine(fiFlow, loansFlow, debtsFlow) { fi, loans, debts ->
            Triple(fi, loans, debts)
        }

        // Combinar los dos resultados
        return combine(part1, part2) { (account, assets, txs), (fiPositions, loans, debtsOwing) ->
            val accountBalance = account?.computedBalance ?: 0.0

            // Calcular valor del portfolio (activos con precio actual)
            val portfolioValue = assets
                .filter { !it.archived }
                .sumOf { asset ->
                    val txsForAsset = txs.filter { it.assetId == asset.id }
                    val position = PortfolioCalculator.calculate(txsForAsset, asset.currentPrice)
                    position.currentValue
                }

            // Valor de renta fija abierta
            val fixedIncomeValue = fiPositions
                .filter { it.isOpen }
                .sumOf { it.currentValue }

            // Total de préstamos pendientes
            val loansOutstanding = loans.sumOf { it.outstandingPrincipal }

            NetWorthData(
                totalAccountBalance   = accountBalance,
                totalPortfolioValue   = portfolioValue,
                totalFixedIncomeValue = fixedIncomeValue,
                totalLoansOutstanding = loansOutstanding,
                totalDebtsOwing       = debtsOwing,
                loans                 = loans
            )
        }
    }
}
