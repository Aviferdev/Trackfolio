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
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.ui.annual.AnnualSummaryScreen
import es.aviferdev.trackfolio.ui.debt.DebtListScreen
import es.aviferdev.trackfolio.ui.fiscal.FiscalReportScreen
import es.aviferdev.trackfolio.ui.fixedincome.FixedIncomeDetailScreen
import es.aviferdev.trackfolio.ui.loan.LoanDetailScreen
import es.aviferdev.trackfolio.ui.networth.NetWorthScreen
import es.aviferdev.trackfolio.ui.home.HomeScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetCategoryDetailScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetDetailScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetHistoryScreen
import es.aviferdev.trackfolio.ui.portfolio.PortfolioScreen
import es.aviferdev.trackfolio.ui.portfolio.PortfolioSettingsScreen
import es.aviferdev.trackfolio.ui.settings.ExpenseSettingsScreen
import es.aviferdev.trackfolio.ui.settings.IncomeSettingsScreen
import es.aviferdev.trackfolio.ui.settings.IncomeTypeDetailScreen
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
                    onNavigateToDebts = {
                        navController.navigate(Screen.Debts.route) { launchSingleTop = true }
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
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.PortfolioSettings.route) {
                            launchSingleTop = true
                        }
                    },
                    onFixedIncomeClick = { positionId ->
                        navController.navigate(Screen.FixedIncomeDetail.buildRoute(positionId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.NetWorth.route) {
                NetWorthScreen(
                    onLoanClick = { loanId ->
                        navController.navigate(Screen.LoanDetail.buildRoute(loanId)) {
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
                    },
                    onNavigateToExpenseSettings = {
                        navController.navigate(Screen.ExpenseSettings.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToIncomeSettings = {
                        navController.navigate(Screen.IncomeSettings.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.ExpenseSettings.route) {
                ExpenseSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.IncomeSettings.route) {
                IncomeSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToIncomeTypeDetail = { incomeType ->
                        navController.navigate(Screen.IncomeTypeDetail.buildRoute(incomeType.name)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route     = Screen.IncomeTypeDetail.route,
                arguments = listOf(
                    navArgument(Screen.IncomeTypeDetail.ARG_INCOME_TYPE) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val incomeTypeName = backStackEntry.arguments
                    ?.getString(Screen.IncomeTypeDetail.ARG_INCOME_TYPE).orEmpty()
                val incomeType = IncomeType.fromName(incomeTypeName)
                if (incomeType != null) {
                    IncomeTypeDetailScreen(
                        incomeType = incomeType,
                        onBack     = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Charts.route) {
                AnnualSummaryScreen()
            }
            composable(Screen.PortfolioSettings.route) {
                PortfolioSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToCategoryDetail = { categoryId ->
                        navController.navigate(Screen.AssetCategoryDetail.buildRoute(categoryId)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlatformDetail = { platform ->
                        // TODO: Implementar navegación a detalle de plataforma
                    }
                )
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
            composable(
                route     = Screen.AssetCategoryDetail.route,
                arguments = listOf(
                    navArgument(Screen.AssetCategoryDetail.ARG_CATEGORY_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString(Screen.AssetCategoryDetail.ARG_CATEGORY_ID).orEmpty()
                AssetCategoryDetailScreen(
                    categoryId   = categoryId,
                    onBack       = { navController.popBackStack() },
                    onAssetClick = { assetId ->
                        navController.navigate(Screen.AssetDetail.buildRoute(assetId)) {
                            launchSingleTop = true
                        }
                    },
                    onFixedIncomeClick = { positionId ->
                        navController.navigate(Screen.FixedIncomeDetail.buildRoute(positionId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route     = Screen.AssetDetail.route,
                arguments = listOf(
                    navArgument(Screen.AssetDetail.ARG_ASSET_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val assetId = backStackEntry.arguments?.getString(Screen.AssetDetail.ARG_ASSET_ID).orEmpty()
                AssetDetailScreen(
                    assetId = assetId,
                    onBack  = { navController.popBackStack() }
                )
            }
            composable(
                route     = Screen.FixedIncomeDetail.route,
                arguments = listOf(
                    navArgument(Screen.FixedIncomeDetail.ARG_POSITION_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val positionId = backStackEntry.arguments?.getString(Screen.FixedIncomeDetail.ARG_POSITION_ID).orEmpty()
                FixedIncomeDetailScreen(
                    positionId = positionId,
                    onBack     = { navController.popBackStack() }
                )
            }
            composable(
                route     = Screen.LoanDetail.route,
                arguments = listOf(
                    navArgument(Screen.LoanDetail.ARG_LOAN_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val loanId = backStackEntry.arguments?.getString(Screen.LoanDetail.ARG_LOAN_ID).orEmpty()
                LoanDetailScreen(
                    loanId = loanId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
