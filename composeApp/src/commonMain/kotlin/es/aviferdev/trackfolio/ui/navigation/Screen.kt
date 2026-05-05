package es.aviferdev.trackfolio.ui.navigation

sealed class Screen(val route: String) {
    data object Home        : Screen("home")
    data object Transactions: Screen("transactions")
    data object Portfolio   : Screen("portfolio")   // Sprint 11
    data object Debts       : Screen("debts")
    data object Settings    : Screen("settings")
    data object Charts      : Screen("charts")      // accesible desde Home, no en bottom bar
}
