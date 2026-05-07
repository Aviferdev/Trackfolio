package es.aviferdev.trackfolio.ui.navigation

sealed class Screen(val route: String) {
    data object Home        : Screen("home")
    data object Transactions: Screen("transactions")
    data object Portfolio   : Screen("portfolio")
    data object Debts       : Screen("debts")
    data object Settings    : Screen("settings")
    data object Charts      : Screen("charts")
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
}
