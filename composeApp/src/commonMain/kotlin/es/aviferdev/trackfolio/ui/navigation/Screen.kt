package es.aviferdev.trackfolio.ui.navigation

sealed class Screen(val route: String) {
    data object Home        : Screen("home")
    data object Transactions: Screen("transactions")
    data object Debts       : Screen("debts")
    data object Settings    : Screen("settings")
}
