package es.aviferdev.n3to.ui.navigation

sealed class Screen(val route: String) {
    data object Home        : Screen("home")
    data object Transactions: Screen("transactions")
    data object Portfolio   : Screen("portfolio")
    data object Debts       : Screen("debts")
    data object Settings    : Screen("settings")
    data object Charts      : Screen("charts")
    data object NetWorth    : Screen("net_worth")
    data object PortfolioSettings : Screen("portfolio_settings")
    data object FiscalReport: Screen("fiscal_report")

    data object ExpenseSettings : Screen("settings_expense")
    data object IncomeSettings  : Screen("settings_income")

    data object IncomeTypeDetail : Screen("settings_income/{incomeTypeName}") {
        const val ARG_INCOME_TYPE = "incomeTypeName"
        fun buildRoute(incomeTypeName: String): String = "settings_income/$incomeTypeName"
    }

    data object AssetHistory : Screen("asset_history/{assetId}") {
        const val ARG_ASSET_ID = "assetId"
        fun buildRoute(assetId: String): String = "asset_history/$assetId"
    }

    data object AssetCategoryDetail : Screen("portfolio_category/{categoryId}") {
        const val ARG_CATEGORY_ID = "categoryId"
        fun buildRoute(categoryId: String): String = "portfolio_category/$categoryId"
    }

    data object AssetDetail : Screen("portfolio_asset/{assetId}") {
        const val ARG_ASSET_ID = "assetId"
        fun buildRoute(assetId: String): String = "portfolio_asset/$assetId"
    }

    data object FixedIncomeDetail : Screen("fixed_income_detail/{positionId}") {
        const val ARG_POSITION_ID = "positionId"
        fun buildRoute(positionId: String): String = "fixed_income_detail/$positionId"
    }

    data object LoanDetail : Screen("loan_detail/{loanId}") {
        const val ARG_LOAN_ID = "loanId"
        fun buildRoute(loanId: String): String = "loan_detail/$loanId"
    }

    data object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun buildRoute(transactionId: String): String = "transaction_detail/$transactionId"
    }

    data object About : Screen("settings_about")

    data object PrivacySettings : Screen("settings_privacy")

    data object Premium : Screen("premium")

    data object CategoryPicker : Screen("category_picker/{initialType}") {
        const val ARG_INITIAL_TYPE = "initialType"
        fun buildRoute(initialType: String): String = "category_picker/$initialType"
    }

    data object RealEstateDetail : Screen("real_estate_detail/{propertyId}") {
        const val ARG_PROPERTY_ID = "propertyId"
        fun buildRoute(propertyId: String): String = "real_estate_detail/$propertyId"
    }

    data object GoalSettings : Screen("goals_settings")
}

/**
 * Helper para determinar si una Screen corresponde a una pestaña principal de la bottom bar.
 * Útil para lógica de navegación condicional (ej: deseleccionar bottom bar en pantallas secundarias).
 */
fun Screen.isMainTab(): Boolean =
    this == Screen.Home || this == Screen.Portfolio || this == Screen.NetWorth
