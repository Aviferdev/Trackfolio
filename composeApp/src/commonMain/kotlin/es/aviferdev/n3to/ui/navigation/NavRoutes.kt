package es.aviferdev.n3to.ui.navigation

import kotlinx.serialization.Serializable

// ── Rutas simples (sin parámetros) ───────────────────────────────────────────
@Serializable
data object HomeRoute
@Serializable
data object TransactionsRoute
@Serializable
data object PortfolioRoute
@Serializable
data object DebtsRoute
@Serializable
data object SettingsRoute
@Serializable
data object ChartsRoute
@Serializable
data object NetWorthRoute
@Serializable
data object PortfolioSettingsRoute
@Serializable
data object FiscalReportRoute
@Serializable
data object ExpenseSettingsRoute
@Serializable
data object IncomeSettingsRoute
@Serializable
data object FeedbackRoute
@Serializable
data object AboutRoute
@Serializable
data object PrivacySettingsRoute
@Serializable
data object SavingsRatesRoute
@Serializable
data object GoalSettingsRoute
@Serializable
data object EmergencyFundSettingsRoute
@Serializable
data object TaxProfileSettingsRoute

// ── Rutas con parámetros ──────────────────────────────────────────────────────
@Serializable
data class CategoryPickerRoute(val initialType: String)
@Serializable
data class TransactionDetailRoute(val transactionId: String)
@Serializable
data class AccountConfigRoute(val accountId: String)
@Serializable
data class IncomeTypeDetailRoute(val incomeTypeName: String)
@Serializable
data class AssetHistoryRoute(val assetId: String)
@Serializable
data class AssetCategoryDetailRoute(val categoryId: String)
@Serializable
data class AssetDetailRoute(val assetId: String)
@Serializable
data class FixedIncomeDetailRoute(val positionId: String)
@Serializable
data class RealEstateDetailRoute(val propertyId: String)
@Serializable
data class ValuableDetailRoute(val valuableId: String)
@Serializable
data class LoanDetailRoute(val loanId: String)
@Serializable
data object PortfolioAssetTypesRoute
@Serializable
data object PortfolioPlatformsRoute
@Serializable
data object PortfolioSectorsRoute
@Serializable
data object PortfolioRegionsRoute
