package es.aviferdev.trackfolio.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.annual.AnnualSummaryScreen
import es.aviferdev.trackfolio.ui.common.loading.GlobalLoadingManager
import es.aviferdev.trackfolio.ui.common.loading.GlobalLoadingOverlay
import es.aviferdev.trackfolio.ui.common.navigation.FloatingBottomNavBar
import es.aviferdev.trackfolio.ui.debt.DebtListScreen
import es.aviferdev.trackfolio.ui.fiscal.FiscalReportScreen
import es.aviferdev.trackfolio.ui.fixedincome.FixedIncomeDetailScreen
import es.aviferdev.trackfolio.ui.home.CategoryPickerScreen
import es.aviferdev.trackfolio.ui.home.HomeScreen
import es.aviferdev.trackfolio.ui.loan.LoanDetailScreen
import es.aviferdev.trackfolio.ui.networth.NetWorthScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetCategoryDetailScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetDetailScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetHistoryScreen
import es.aviferdev.trackfolio.ui.portfolio.PortfolioScreen
import es.aviferdev.trackfolio.ui.portfolio.PortfolioSettingsScreen
import es.aviferdev.trackfolio.ui.settings.ExpenseSettingsScreen
import es.aviferdev.trackfolio.ui.settings.IncomeSettingsScreen
import es.aviferdev.trackfolio.ui.settings.IncomeTypeDetailScreen
import es.aviferdev.trackfolio.ui.settings.SettingsScreen
import es.aviferdev.trackfolio.ui.transaction.TransactionDetailScreen
import es.aviferdev.trackfolio.ui.transaction.TransactionListScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun TrackfolioNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val loadingManager = koinInject<GlobalLoadingManager>()
    val isLoading by loadingManager.isLoading.collectAsState()
    val loadingMessage by loadingManager.loadingMessage.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                val hideRoutes = listOf(
                    Screen.TransactionDetail.route,
                    Screen.Transactions.route,
                    Screen.CategoryPicker.route,
                )
                val showBottomBar = currentDestination?.route?.let { route ->
                    hideRoutes.none { route.startsWith(it.substringBefore("{")) }
                } ?: true

                if (showBottomBar) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        FloatingBottomNavBar(
                            items = bottomNavItems(),
                            currentDestination = currentDestination,
                            onItemClick = { item ->
                                if (currentDestination?.route == item.screen.route) return@FloatingBottomNavBar
                                navController.popBackStack(Screen.Home.route, inclusive = false)
                                if (item.screen.route != Screen.Home.route) {
                                    navController.navigate(item.screen.route) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToTransactions = {
                            navController.navigate(Screen.Transactions.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCharts = {
                            navController.navigate(Screen.Charts.route) { launchSingleTop = true }
                        },
                        onNavigateToDebts = {
                            navController.navigate(Screen.Debts.route) { launchSingleTop = true }
                        },
                        onNavigateToFiscalReport = {
                            navController.navigate(Screen.FiscalReport.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        },
                        onNavigateToCategoryPicker = { type ->
                            navController.navigate(Screen.CategoryPicker.buildRoute(type.name)) {
                                launchSingleTop = true
                            }
                        }
                    )
                    // Reabrir sheet al volver del CategoryPicker
                    val catPickerCatId = navController.currentBackStackEntry
                        ?.savedStateHandle?.get<String>("selected_category_id")
                    val catPickerIncType = navController.currentBackStackEntry
                        ?.savedStateHandle?.get<String>("selected_income_type")

                    if (catPickerCatId != null || catPickerIncType != null) {
                        val homeVM: es.aviferdev.trackfolio.ui.home.AddTransactionViewModel = koinViewModel()
                        if (catPickerCatId != null) {
                            homeVM.onCategoryChange(catPickerCatId)
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.remove<String>("selected_category_id")
                        }
                        if (catPickerIncType != null) {
                            IncomeType.fromName(catPickerIncType)?.let { homeVM.onIncomeTypeChange(it) }
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.remove<String>("selected_income_type")
                        }
                    }
                    HomeScreen(
                        reopenFromPicker = catPickerCatId != null || catPickerIncType != null,
                        onConsumeReopen = {
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.remove<String>("selected_category_id")
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.remove<String>("selected_income_type")
                        },
                        onNavigateToTransactions = {
                            navController.navigate(Screen.Transactions.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCharts = {
                            navController.navigate(Screen.Charts.route) { launchSingleTop = true }
                        },
                        onNavigateToDebts = {
                            navController.navigate(Screen.Debts.route) { launchSingleTop = true }
                        },
                        onNavigateToFiscalReport = {
                            navController.navigate(Screen.FiscalReport.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        },
                        onNavigateToCategoryPicker = { type ->
                            navController.navigate(Screen.CategoryPicker.buildRoute(type.name)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = Screen.CategoryPicker.route,
                    arguments = listOf(
                        navArgument(Screen.CategoryPicker.ARG_INITIAL_TYPE) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val typeName = backStackEntry.arguments
                        ?.getString(Screen.CategoryPicker.ARG_INITIAL_TYPE).orEmpty()
                    val initialType = try {
                        TransactionType.valueOf(typeName)
                    } catch (_: Exception) {
                        TransactionType.EXPENSE
                    }
                    CategoryPickerScreen(
                        initialType = initialType,
                        onBack = { navController.popBackStack() },
                        onCreateCategory = {
                            navController.navigate(Screen.ExpenseSettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onCategorySelected = { categoryId ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_category_id", categoryId)
                            navController.popBackStack()
                        },
                        onIncomeTypeSelected = { incomeType ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_income_type", incomeType.name)
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.Transactions.route) {
                    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                    val editTxId = savedStateHandle?.get<String>("edit_transaction_id")
                    if (editTxId != null) {
                        savedStateHandle?.remove<String>("edit_transaction_id")
                    }

                    TransactionListScreen(
                        onBack = { navController.popBackStack() },
                        editTransactionId = editTxId,
                        onConsumeEdit = { },
                        onTransactionClick = { transaction ->
                            navController.navigate(
                                Screen.TransactionDetail.buildRoute(transaction.id)
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = Screen.TransactionDetail.route,
                    arguments = listOf(
                        navArgument(Screen.TransactionDetail.ARG_TRANSACTION_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val transactionId =
                        backStackEntry.arguments?.getString(Screen.TransactionDetail.ARG_TRANSACTION_ID)
                            .orEmpty()
                    TransactionDetailScreen(
                        transactionId = transactionId,
                        onBack = { navController.popBackStack() },
                        onEditTransaction = { tx ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("edit_transaction_id", tx.id)
                            navController.popBackStack()
                        }
                    )
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
                    route = Screen.IncomeTypeDetail.route,
                    arguments = listOf(
                        navArgument(Screen.IncomeTypeDetail.ARG_INCOME_TYPE) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val incomeTypeName = backStackEntry.arguments
                        ?.getString(Screen.IncomeTypeDetail.ARG_INCOME_TYPE).orEmpty()
                    val incomeType = IncomeType.fromName(incomeTypeName)
                    if (incomeType != null) {
                        IncomeTypeDetailScreen(
                            incomeType = incomeType,
                            onBack = { navController.popBackStack() }
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
                    route = Screen.AssetHistory.route,
                    arguments = listOf(
                        navArgument(Screen.AssetHistory.ARG_ASSET_ID) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val assetId =
                        backStackEntry.arguments?.getString(Screen.AssetHistory.ARG_ASSET_ID)
                            .orEmpty()
                    AssetHistoryScreen(
                        assetId = assetId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Screen.AssetCategoryDetail.route,
                    arguments = listOf(
                        navArgument(Screen.AssetCategoryDetail.ARG_CATEGORY_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val categoryId =
                        backStackEntry.arguments?.getString(Screen.AssetCategoryDetail.ARG_CATEGORY_ID)
                            .orEmpty()
                    AssetCategoryDetailScreen(
                        categoryId = categoryId,
                        onBack = { navController.popBackStack() },
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
                    route = Screen.AssetDetail.route,
                    arguments = listOf(
                        navArgument(Screen.AssetDetail.ARG_ASSET_ID) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val assetId =
                        backStackEntry.arguments?.getString(Screen.AssetDetail.ARG_ASSET_ID)
                            .orEmpty()
                    AssetDetailScreen(
                        assetId = assetId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Screen.FixedIncomeDetail.route,
                    arguments = listOf(
                        navArgument(Screen.FixedIncomeDetail.ARG_POSITION_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val positionId =
                        backStackEntry.arguments?.getString(Screen.FixedIncomeDetail.ARG_POSITION_ID)
                            .orEmpty()
                    FixedIncomeDetailScreen(
                        positionId = positionId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Screen.LoanDetail.route,
                    arguments = listOf(
                        navArgument(Screen.LoanDetail.ARG_LOAN_ID) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val loanId = backStackEntry.arguments
                        ?.getString(Screen.LoanDetail.ARG_LOAN_ID).orEmpty()
                    LoanDetailScreen(
                        loanId = loanId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

        }

        GlobalLoadingOverlay(
            isLoading = isLoading,
            message = loadingMessage
        )
    }
}


