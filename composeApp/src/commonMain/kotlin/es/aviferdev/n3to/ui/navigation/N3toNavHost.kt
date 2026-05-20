package es.aviferdev.n3to.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.annual.AnnualSummaryScreen
import es.aviferdev.n3to.ui.common.loading.GlobalLoadingManager
import es.aviferdev.n3to.ui.common.loading.GlobalLoadingOverlay
import es.aviferdev.n3to.ui.common.navigation.FloatingBottomNavBar
import es.aviferdev.n3to.ui.debt.DebtListScreen
import es.aviferdev.n3to.ui.fiscal.FiscalReportScreen
import es.aviferdev.n3to.ui.fixedincome.FixedIncomeDetailScreen
import es.aviferdev.n3to.ui.home.CategoryPickerScreen
import es.aviferdev.n3to.ui.home.HomeScreen
import es.aviferdev.n3to.ui.home.viewmodel.AddTransactionViewModel
import es.aviferdev.n3to.ui.loan.LoanDetailScreen
import es.aviferdev.n3to.ui.navigation.Screen.Home
import es.aviferdev.n3to.ui.navigation.Screen.NetWorth
import es.aviferdev.n3to.ui.navigation.Screen.Portfolio
import es.aviferdev.n3to.ui.networth.NetWorthScreen
import es.aviferdev.n3to.ui.portfolio.AssetCategoryDetailScreen
import es.aviferdev.n3to.ui.portfolio.AssetDetailScreen
import es.aviferdev.n3to.ui.portfolio.AssetHistoryScreen
import es.aviferdev.n3to.ui.portfolio.PortfolioScreen
import es.aviferdev.n3to.ui.portfolio.PortfolioSettingsScreen
import es.aviferdev.n3to.ui.premium.PremiumScreen
import es.aviferdev.n3to.ui.realestate.RealEstateDetailScreen
import es.aviferdev.n3to.ui.savingsrates.SavingsRatesScreen
import es.aviferdev.n3to.ui.settings.AboutScreen
import es.aviferdev.n3to.ui.settings.AccountConfigScreen
import es.aviferdev.n3to.ui.settings.ExpenseSettingsScreen
import es.aviferdev.n3to.ui.settings.IncomeSettingsScreen
import es.aviferdev.n3to.ui.settings.IncomeTypeDetailScreen
import es.aviferdev.n3to.ui.settings.PrivacySettingsScreen
import es.aviferdev.n3to.ui.settings.SettingsScreen
import es.aviferdev.n3to.ui.settings.emergencyfund.EmergencyFundSettingsScreen
import es.aviferdev.n3to.ui.settings.feedback.FeedbackScreen
import es.aviferdev.n3to.ui.settings.goal.GoalSettingsScreen
import es.aviferdev.n3to.ui.settings.taxprofile.TaxProfileSettingsScreen
import es.aviferdev.n3to.ui.transaction.TransactionDetailScreen
import es.aviferdev.n3to.ui.transaction.TransactionListScreen
import es.aviferdev.n3to.ui.valuable.ValuableDetailScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun N3toNavHost() {
    val loadingManager = koinInject<GlobalLoadingManager>()
    val isLoading by loadingManager.isLoading.collectAsState()

    Box(Modifier.fillMaxSize()) {
        N3toContent()
        GlobalLoadingOverlay(
            isLoading = isLoading != null,
            message = isLoading
        )
    }
}

@Composable
fun N3toContent() {
    val openStore: () -> Unit = koinInject(named("openStore"))

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomBarN3toContent(
                    navController = navController,
                    currentDestination = currentDestination,
                )
            }
        ) { _ ->
            NavHost(
                navController = navController,
                startDestination = Home.route,
            ) {
                composable(Home.route) {
                    HomeScreen(
                        onOpenStore = openStore,
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
                        onNavigateToEmergencyFundSettings = {
                            navController.navigate(Screen.EmergencyFundSettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToFixedIncomeDetail = { positionId ->
                            navController.navigate(FixedIncomeDetailRoute(positionId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCategoryPicker = { type ->
                            navController.navigate(CategoryPickerRoute(type.name)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAccountConfig = { accountId ->
                            navController.navigate(AccountConfigRoute(accountId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToExpenseSettings = {
                            navController.navigate(Screen.ExpenseSettings.route) {
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
                        val homeVM: AddTransactionViewModel = koinViewModel()
                        if (catPickerCatId != null) {
                            homeVM.onCategoryChange(catPickerCatId)
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.remove<String>("selected_category_id")
                        }
                        if (catPickerIncType != null) {
                            IncomeType.fromName(catPickerIncType)
                                ?.let { homeVM.onIncomeTypeChange(it) }
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.remove<String>("selected_income_type")
                        }
                    }
                    HomeScreen(
                        onOpenStore = openStore,
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
                        onNavigateToEmergencyFundSettings = {
                            navController.navigate(Screen.EmergencyFundSettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToFixedIncomeDetail = { positionId ->
                            navController.navigate(FixedIncomeDetailRoute(positionId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCategoryPicker = { type ->
                            navController.navigate(CategoryPickerRoute(type.name)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAccountConfig = { accountId ->
                            navController.navigate(AccountConfigRoute(accountId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToExpenseSettings = {
                            navController.navigate(Screen.ExpenseSettings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<CategoryPickerRoute> { backStackEntry ->
                    val typeName = backStackEntry.toRoute<CategoryPickerRoute>().initialType
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
                        savedStateHandle.remove<String>("edit_transaction_id")
                    }

                    TransactionListScreen(
                        onBack = { navController.popBackStack() },
                        editTransactionId = editTxId,
                        onConsumeEdit = { },
                        onTransactionClick = { transaction ->
                            navController.navigate(TransactionDetailRoute(transaction.id)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<TransactionDetailRoute> { backStackEntry ->
                    val transactionId =
                        backStackEntry.toRoute<TransactionDetailRoute>().transactionId
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
                composable(Portfolio.route) {
                    PortfolioScreen(
                        onAssetClick = { assetId ->
                            navController.navigate(AssetHistoryRoute(assetId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.PortfolioSettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onFixedIncomeClick = { positionId ->
                            navController.navigate(FixedIncomeDetailRoute(positionId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSavingsRates = {
                            navController.navigate(Screen.SavingsRates.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.SavingsRates.route) {
                    SavingsRatesScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(NetWorth.route) {
                    NetWorthScreen(
                        onLoanClick = { loanId ->
                            navController.navigate(LoanDetailRoute(loanId)) {
                                launchSingleTop = true
                            }
                        },
                        onPropertyClick = { propertyId ->
                            navController.navigate(RealEstateDetailRoute(propertyId)) {
                                launchSingleTop = true
                            }
                        },
                        onValuableClick = { valuableId ->
                            navController.navigate(ValuableDetailRoute(valuableId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        }
                    )
                }
                composable(Screen.Debts.route) {
                    DebtListScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        navigateBack = { navController.popBackStack() },
                        onNavigateToPrivacySettings = {
                            navController.navigate(Screen.PrivacySettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToPremium = {
                            navController.navigate(Screen.Premium.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAccountConfig = { accountId ->
                            navController.navigate(AccountConfigRoute(accountId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToFeedback = {
                            navController.navigate(Screen.Feedback.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAbout = {
                            navController.navigate(Screen.About.route) {
                                launchSingleTop = true
                            }
                        },
                        onResetOnboarding = {
                            //TODO
                        }
                    )
                }
                composable<AccountConfigRoute> { backStackEntry ->
                    val accountId = backStackEntry.toRoute<AccountConfigRoute>().accountId
                    AccountConfigScreen(
                        accountId = accountId,
                        onBack = { navController.popBackStack() },
                        onNavigateToExpenseSettings = {
                            navController.navigate(Screen.ExpenseSettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToIncomeSettings = {
                            navController.navigate(Screen.IncomeSettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToTaxProfile = {
                            navController.navigate(Screen.TaxProfileSettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToGoals = {
                            navController.navigate(Screen.GoalSettings.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToEmergencyFund = {
                            navController.navigate(Screen.EmergencyFundSettings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.GoalSettings.route) {
                    GoalSettingsScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.EmergencyFundSettings.route) {
                    EmergencyFundSettingsScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.TaxProfileSettings.route) {
                    TaxProfileSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Feedback.route) {
                    FeedbackScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.About.route) {
                    AboutScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.PrivacySettings.route) {
                    PrivacySettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToPremium = {
                            navController.navigate(Screen.Premium.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.Premium.route) {
                    PremiumScreen(
                        onBack = { navController.popBackStack() }
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
                            navController.navigate(IncomeTypeDetailRoute(incomeType.name)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<IncomeTypeDetailRoute> { backStackEntry ->
                    val incomeTypeName =
                        backStackEntry.toRoute<IncomeTypeDetailRoute>().incomeTypeName
                    val incomeType = IncomeType.fromName(incomeTypeName)
                    if (incomeType != null) {
                        IncomeTypeDetailScreen(
                            incomeType = incomeType,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.Charts.route) {
                    AnnualSummaryScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.PortfolioSettings.route) {
                    PortfolioSettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToCategoryDetail = { categoryId ->
                            navController.navigate(AssetCategoryDetailRoute(categoryId)) {
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
                composable<AssetHistoryRoute> { backStackEntry ->
                    val assetId = backStackEntry.toRoute<AssetHistoryRoute>().assetId
                    AssetHistoryScreen(
                        assetId = assetId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<AssetCategoryDetailRoute> { backStackEntry ->
                    val categoryId = backStackEntry.toRoute<AssetCategoryDetailRoute>().categoryId
                    AssetCategoryDetailScreen(
                        categoryId = categoryId,
                        onBack = { navController.popBackStack() },
                        onAssetClick = { assetId ->
                            navController.navigate(AssetDetailRoute(assetId)) {
                                launchSingleTop = true
                            }
                        },
                        onFixedIncomeClick = { positionId ->
                            navController.navigate(FixedIncomeDetailRoute(positionId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<AssetDetailRoute> { backStackEntry ->
                    val assetId = backStackEntry.toRoute<AssetDetailRoute>().assetId
                    AssetDetailScreen(
                        assetId = assetId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<FixedIncomeDetailRoute> { backStackEntry ->
                    val positionId = backStackEntry.toRoute<FixedIncomeDetailRoute>().positionId
                    FixedIncomeDetailScreen(
                        positionId = positionId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<RealEstateDetailRoute> { backStackEntry ->
                    val propertyId = backStackEntry.toRoute<RealEstateDetailRoute>().propertyId
                    RealEstateDetailScreen(
                        propertyId = propertyId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLoan = { loanId ->
                            navController.navigate(LoanDetailRoute(loanId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<ValuableDetailRoute> { backStackEntry ->
                    val valuableId = backStackEntry.toRoute<ValuableDetailRoute>().valuableId
                    ValuableDetailScreen(
                        valuableId = valuableId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable<LoanDetailRoute> { backStackEntry ->
                    val loanId = backStackEntry.toRoute<LoanDetailRoute>().loanId
                    LoanDetailScreen(
                        loanId = loanId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarN3toContent(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    if (currentDestination?.route.needShowBottomBar()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            FloatingBottomNavBar(
                items = bottomNavItems(),
                currentDestination = currentDestination,
                onItemClick = { item ->
                    if (currentDestination?.route != item.screen.route) {
                        navController.popBackStack(Home.route, inclusive = false)
                        if (item.screen.route != Home.route) {
                            navController.navigate(item.screen.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    }
}

private fun String?.needShowBottomBar() =
    when (this) {
        Home.route,
        Portfolio.route,
        NetWorth.route -> true

        else -> false
    }