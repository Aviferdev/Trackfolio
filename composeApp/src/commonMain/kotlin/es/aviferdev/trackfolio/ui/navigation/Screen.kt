package es.aviferdev.trackfolio.ui.navigation

sealed class Screen(val route: String) {
    data object Home        : Screen("home")
    data object Transactions: Screen("transactions")
    data object Portfolio   : Screen("portfolio")   // Sprint 11
    data object Debts       : Screen("debts")
    data object Settings    : Screen("settings")
    data object Charts      : Screen("charts")      // accesible desde Home, no en bottom bar

    /**
     * Pantalla de historial de un activo concreto. Recibe `assetId` por
     * parámetro de ruta. No aparece en la bottom bar.
     */
    data object AssetHistory : Screen("asset_history/{assetId}") {
        const val ARG_ASSET_ID = "assetId"
        fun buildRoute(assetId: String): String = "asset_history/$assetId"
    }
}
