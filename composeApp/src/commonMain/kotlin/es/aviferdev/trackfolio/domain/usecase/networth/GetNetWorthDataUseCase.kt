package es.aviferdev.trackfolio.domain.usecase.networth

import es.aviferdev.trackfolio.domain.model.*
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetNetWorthDataUseCase(
    private val accountRepository: AccountRepository,
    private val assetRepository: AssetRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val fixedIncomeRepository: FixedIncomeRepository,
    private val loanRepository: LoanRepository,
    private val debtRepository: DebtRepository,
    private val propertyRepository: RealEstatePropertyRepository
) {
    operator fun invoke(accountId: String): Flow<NetWorthData> {
        val accountFlow = accountRepository.getAccountById(accountId)
        val assetsFlow = assetRepository.getAssetsByAccount(accountId)
        val txsFlow = assetTransactionRepository.getByAccount(accountId)
        val fiFlow = fixedIncomeRepository.getOpenByAccount(accountId)
        val loansFlow = loanRepository.getActiveByAccount(accountId)
        val debtsFlow = debtRepository.getTotalByDirectionAndAccount(accountId, DebtDirection.I_OWE)
        val propertiesFlow = propertyRepository.getActiveByAccount(accountId)

        val assetsPart = combine(accountFlow, assetsFlow, txsFlow) { account, assets, txs ->
            Triple(account, assets, txs)
        }
        val liabilitiesPart = combine(fiFlow, loansFlow, debtsFlow) { fi, loans, debts ->
            Triple(fi, loans, debts)
        }
        return combine(assetsPart, liabilitiesPart, propertiesFlow) {
            (account, assets, txs), (fiPositions, loans, debtsOwing), properties ->

            val accountBalance = account?.computedBalance ?: 0.0
            val portfolioValue = assets.filter { !it.archived }
                .sumOf { asset ->
                    val txsForAsset = txs.filter { it.assetId == asset.id }
                    val position = PortfolioCalculator.calculate(txsForAsset, asset.currentPrice)
                    position.currentValue
                }
            val fixedIncomeValue = fiPositions.filter { it.isOpen }.sumOf { it.currentValue }

            // Valor inmobiliario
            val totalRealEstateValue = properties
                .filter { !it.archived }
                .sumOf { it.effectiveValue }

            // Ajuste de préstamos vinculados a propiedades
            val propertyByLoanId = properties
                .filter { it.linkedLoanId != null }
                .associateBy { it.linkedLoanId }

            val loansOutstanding = loans.sumOf { loan ->
                val linkedProperty = propertyByLoanId[loan.id]
                if (linkedProperty != null) {
                    loan.outstandingPrincipal * (linkedProperty.ownershipPercentage / 100.0)
                } else {
                    loan.outstandingPrincipal
                }
            }

            NetWorthData(
                totalAccountBalance   = accountBalance,
                totalPortfolioValue   = portfolioValue,
                totalFixedIncomeValue = fixedIncomeValue,
                totalRealEstateValue  = totalRealEstateValue,
                totalLoansOutstanding = loansOutstanding,
                totalDebtsOwing       = debtsOwing,
                loans                 = loans,
                properties            = properties
            )
        }
    }
}
