package es.aviferdev.trackfolio.ui.navigation

sealed class Screen(val route: String) {
    data object Home        : Screen("home")
    data object Transactions: Screen("transactions")
    data object Portfolio   : Screen("portfolio")
    data object Debts       : Screen("debts")
    data object Settings    : Screen("settings")
    data object Charts      : Screen("charts")      // accesible desde Home, no en bottom bar
    data object FiscalReport: Screen("fiscal_report") // accesible desde Ajustes, no en bottom bar

    data object AssetHistory : Screen("asset_history/{assetId}") {
        const val ARG_ASSET_ID = "assetId"
        fun buildRoute(assetId: String): String = "asset_history/$assetId"
    }
}
