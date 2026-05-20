package es.aviferdev.n3to.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data class CategoryPickerRoute(val initialType: String)
@Serializable data class TransactionDetailRoute(val transactionId: String)
@Serializable data class AccountConfigRoute(val accountId: String)
@Serializable data class IncomeTypeDetailRoute(val incomeTypeName: String)
@Serializable data class AssetHistoryRoute(val assetId: String)
@Serializable data class AssetCategoryDetailRoute(val categoryId: String)
@Serializable data class AssetDetailRoute(val assetId: String)
@Serializable data class FixedIncomeDetailRoute(val positionId: String)
@Serializable data class RealEstateDetailRoute(val propertyId: String)
@Serializable data class ValuableDetailRoute(val valuableId: String)
@Serializable data class LoanDetailRoute(val loanId: String)
