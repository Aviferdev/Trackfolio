package es.aviferdev.n3to.domain.usecase.networth

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.NetWorthHistoryPoint
import es.aviferdev.n3to.domain.model.PortfolioValuePoint
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.repository.AccountRepository
import es.aviferdev.n3to.domain.repository.DebtRepository
import es.aviferdev.n3to.domain.repository.LoanRepository
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfolioValueHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Calcula la evolución mensual del patrimonio neto.
 *
 * Para cada mes:
 *  - Activos = balance cuentas (initialBalance + transacciones acumuladas) + valor portfolio + valor inmuebles
 *  - Pasivos = préstamos pendientes (ajustados por propiedad vinculada) + deudas I_OWE pendientes
 *  - Patrimonio neto = activos − pasivos
 */
class GetNetWorthHistoryUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val loanRepository: LoanRepository,
    private val debtRepository: DebtRepository,
    private val getPortfolioValueHistory: GetPortfolioValueHistoryUseCase,
    private val propertyRepository: RealEstatePropertyRepository
) {
    operator fun invoke(accountId: String): Flow<List<NetWorthHistoryPoint>> {
        val accountsFlow = accountRepository.getAllAccounts()
        val loansFlow = loanRepository.getByAccount(accountId)
        val debtsFlow = debtRepository.getActiveByAccount(accountId)
        val propertiesFlow = propertyRepository.getPropertiesByAccount(accountId)

        val liabilitiesFlow = combine(loansFlow, debtsFlow) { loans, debts ->
            Pair(loans, debts)
        }

        val assetsFlow = combine(
            combine(accountsFlow, getPortfolioValueHistory(accountId)) { accounts, portfolioHistory ->
                Pair(accounts, portfolioHistory)
            },
            propertiesFlow
        ) { (accounts, portfolioHistory), properties ->
            Triple(accounts, portfolioHistory, properties)
        }

        return combine(assetsFlow, liabilitiesFlow) { (accounts, portfolioHistory, properties), (loans, debts) ->
            buildNetWorthHistory(accountId, accounts, portfolioHistory, loans, debts, properties)
        }
    }

    private suspend fun buildNetWorthHistory(
        accountId: String,
        accounts: List<Account>,
        portfolioHistory: List<PortfolioValuePoint>,
        loans: List<Loan>,
        debts: List<es.aviferdev.n3to.domain.model.Debt>,
        properties: List<RealEstateProperty>
    ): List<NetWorthHistoryPoint> {
        if (accounts.isEmpty()) return emptyList()

        val tz = TimeZone.currentSystemDefault()
        val now = nowMillis()
        val nowLocal = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz)

        // Determinar rango de meses
        val allDates = mutableListOf<Long>()
        allDates.addAll(portfolioHistory.map { it.date })
        allDates.addAll(loans.map { it.startDate })
        allDates.addAll(debts.map { it.date })
        accounts.forEach { allDates.add(it.createdAt) }

        if (allDates.isEmpty()) return emptyList()

        val firstLocal = Instant.fromEpochMilliseconds(allDates.min()).toLocalDateTime(tz)

        val months = generateMonthEnds(
            startYear = firstLocal.year,
            startMonth = firstLocal.monthNumber,
            endYear = nowLocal.year,
            endMonth = nowLocal.monthNumber
        )

        if (months.isEmpty()) return emptyList()

        // ── Preparar datos de balance de cuentas ──────────────────────────
        // Obtener breakdown mensual de transacciones para cada año del rango
        val monthlyNets = mutableMapOf<String, Double>() // "YYYY-MM" -> neto (income - expense)
        for (year in firstLocal.year..nowLocal.year) {
            try {
                val breakdowns = transactionRepository.getMonthlyBreakdown(
                    accountId, year.toString()
                ).first()
                for (mt in breakdowns) {
                    val key = "${year}-${mt.month.padStart(2, '0')}"
                    monthlyNets[key] = mt.balance
                }
            } catch (_: Exception) { }
        }

        // Balance total actual de todas las cuentas
        val totalCurrentBalance = accounts.sumOf { it.computedBalance }
        val totalInitialBalance = accounts.sumOf { it.initialBalance }

        // Construir mapa de balance acumulado por mes
        // Estrategia: empezar desde initialBalance y acumular monthlyNets
        // Para el mes actual usamos computedBalance como ancla
        val currentMonthKey = "${nowLocal.year}-${nowLocal.monthNumber.toString().padStart(2, '0')}"

        // Calcular el neto acumulado desde initialBalance hasta el mes actual
        // neto_acumulado = computedBalance - initialBalance
        // Luego distribuir: para cada mes, balance = initialBalance + suma de nets hasta ese mes
        val cumulativeNet = totalCurrentBalance - totalInitialBalance

        // Acumular nets mes a mes desde el inicio
        val balanceByMonth = mutableMapOf<String, Double>()
        var runningNet = 0.0
        for (month in months) {
            val monthLocal = Instant.fromEpochMilliseconds(month).toLocalDateTime(tz)
            val key = "${monthLocal.year}-${monthLocal.monthNumber.toString().padStart(2, '0')}"
            val monthNet = monthlyNets[key] ?: 0.0
            runningNet += monthNet
            balanceByMonth[key] = totalInitialBalance + runningNet
        }

        // Para el mes actual, forzar el computedBalance real
        balanceByMonth[currentMonthKey] = totalCurrentBalance

        // ── Pre-indexar valor del portfolio ───────────────────────────────
        val portfolioByMonth = mutableMapOf<String, Double>()
        for (point in portfolioHistory) {
            val pointLocal = Instant.fromEpochMilliseconds(point.date).toLocalDateTime(tz)
            val key = "${pointLocal.year}-${pointLocal.monthNumber.toString().padStart(2, '0')}"
            portfolioByMonth[key] = point.value
        }

        // ── Construir puntos ─────────────────────────────────────────────
        return months.map { monthEndMillis ->
            val monthLocal = Instant.fromEpochMilliseconds(monthEndMillis).toLocalDateTime(tz)
            val monthKey = "${monthLocal.year}-${monthLocal.monthNumber.toString().padStart(2, '0')}"

            val accountBalance = balanceByMonth[monthKey] ?: totalInitialBalance
            val portfolioValue = portfolioByMonth[monthKey] ?: 0.0

            val loansOutstanding = loans
                .filter { !it.archived && it.startDate <= monthEndMillis }
                .sumOf { it.outstandingPrincipal }

            val debtsOwing = debts
                .filter {
                    it.direction == DebtDirection.I_OWE &&
                    !it.isPaid &&
                    it.date <= monthEndMillis
                }
                .sumOf { it.amount }

            // ═══ Valor histórico de propiedades ═══
            // Simplificación: se usa currentEstimatedValue como constante desde acquisitionDate.
            val propertiesValue = properties
                .filter { !it.archived }
                .sumOf { property ->
                    val acquisitionYearMonth = epochToYearMonth(property.acquisitionDate)
                    if (monthKey >= acquisitionYearMonth) {
                        property.currentEstimatedValue * (property.ownershipPercentage / 100.0)
                    } else 0.0
                }

            val totalAssets = accountBalance + portfolioValue + propertiesValue
            val totalLiabilities = loansOutstanding + debtsOwing

            NetWorthHistoryPoint(
                yearMonth       = monthKey,
                netWorth        = totalAssets - totalLiabilities,
                totalAssets     = totalAssets,
                totalLiabilities = totalLiabilities
            )
        }
    }

    private fun epochToYearMonth(epochMillis: Long): String {
        val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
        return "${local.year}-${local.monthNumber.toString().padStart(2, '0')}"
    }

    private fun generateMonthEnds(
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ): List<Long> {
        val result = mutableListOf<Long>()
        var year = startYear
        var month = startMonth
        val tz = TimeZone.currentSystemDefault()

        while (year < endYear || (year == endYear && month <= endMonth)) {
            val lastDay = daysInMonth(year, month)
            val instant = LocalDateTime(year, month, lastDay, 23, 59, 59).toInstant(tz)
            result.add(instant.toEpochMilliseconds())
            month++
            if (month > 12) { month = 1; year++ }
        }
        return result
    }

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}
