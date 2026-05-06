package es.aviferdev.trackfolio.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import es.aviferdev.trackfolio.ui.annual.AnnualSummaryScreen
import es.aviferdev.trackfolio.ui.debt.DebtListScreen
import es.aviferdev.trackfolio.ui.fiscal.FiscalReportScreen
import es.aviferdev.trackfolio.ui.home.HomeScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetHistoryScreen
import es.aviferdev.trackfolio.ui.portfolio.PortfolioScreen
import es.aviferdev.trackfolio.ui.settings.SettingsScreen
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceElevated
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.transaction.TransactionListScreen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun TrackfolioNavHost() {
    val navController      = rememberNavController()
    val navBackStackEntry  by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val items              = bottomNavItems()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = SurfaceWhite) {
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == item.screen.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentDestination?.route == item.screen.route) return@NavigationBarItem
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                            if (item.screen.route != Screen.Home.route) {
                                navController.navigate(item.screen.route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon  = { Icon(if (selected) item.selectedIcon else item.icon, item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = PrimaryDark,
                            selectedTextColor   = PrimaryDark,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor      = SurfaceElevated
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToTransactions = {
                        navController.navigate(Screen.Transactions.route) { launchSingleTop = true }
                    },
                    onNavigateToCharts = {
                        navController.navigate(Screen.Charts.route) { launchSingleTop = true }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                    }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionListScreen()
            }
            composable(Screen.Portfolio.route) {
                PortfolioScreen(
                    onAssetClick = { assetId ->
                        navController.navigate(Screen.AssetHistory.buildRoute(assetId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Debts.route) {
                DebtListScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToFiscalReport = {
                        navController.navigate(Screen.FiscalReport.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Charts.route) {
                AnnualSummaryScreen()
            }
            composable(Screen.FiscalReport.route) {
                FiscalReportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route     = Screen.AssetHistory.route,
                arguments = listOf(
                    navArgument(Screen.AssetHistory.ARG_ASSET_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val assetId = backStackEntry.arguments?.getString(Screen.AssetHistory.ARG_ASSET_ID).orEmpty()
                AssetHistoryScreen(
                    assetId = assetId,
                    onBack  = { navController.popBackStack() }
                )
            }
        }
    }
}
