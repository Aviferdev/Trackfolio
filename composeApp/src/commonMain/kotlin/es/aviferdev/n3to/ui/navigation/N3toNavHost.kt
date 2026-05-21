package es.aviferdev.n3to.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.ui.theme.LocalBottomNavPadding
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
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
import es.aviferdev.n3to.ui.networth.NetWorthScreen
import es.aviferdev.n3to.ui.portfolio.AssetCategoryDetailScreen
import es.aviferdev.n3to.ui.portfolio.AssetDetailScreen
import es.aviferdev.n3to.ui.portfolio.AssetHistoryScreen
import es.aviferdev.n3to.ui.portfolio.home.PortfolioScreen
import es.aviferdev.n3to.ui.portfolio.settings.PortfolioSettingsScreen
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
    val route: Any,
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

    val systemNavInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // 56dp (row) + 8dp (top padding) + 12dp (bottom padding) = 76dp fixed bar height
    val bottomNavPadding = if (currentDestination.needShowBottomBar()) 76.dp + systemNavInset else 0.dp

    CompositionLocalProvider(LocalBottomNavPadding provides bottomNavPadding) {
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
                startDestination = HomeRoute,
            ) {
                composable<HomeRoute> {
                    HomeScreen(
                        onOpenStore = openStore,
                        onNavigateToTransactions = {
                            navController.navigate(TransactionsRoute) { launchSingleTop = true }
                        },
                        onNavigateToCharts = {
                            navController.navigate(ChartsRoute) { launchSingleTop = true }
                        },
                        onNavigateToDebts = {
                            navController.navigate(DebtsRoute) { launchSingleTop = true }
                        },
                        onNavigateToFiscalReport = {
                            navController.navigate(FiscalReportRoute) { launchSingleTop = true }
                        },
                        onNavigateToSettings = {
                            navController.navigate(SettingsRoute) { launchSingleTop = true }
                        },
                        onNavigateToEmergencyFundSettings = {
                            navController.navigate(EmergencyFundSettingsRoute) {
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
                            navController.navigate(TransactionsRoute) { launchSingleTop = true }
                        },
                        onNavigateToCharts = {
                            navController.navigate(ChartsRoute) { launchSingleTop = true }
                        },
                        onNavigateToDebts = {
                            navController.navigate(DebtsRoute) { launchSingleTop = true }
                        },
                        onNavigateToFiscalReport = {
                            navController.navigate(FiscalReportRoute) { launchSingleTop = true }
                        },
                        onNavigateToSettings = {
                            navController.navigate(SettingsRoute) { launchSingleTop = true }
                        },
                        onNavigateToEmergencyFundSettings = {
                            navController.navigate(EmergencyFundSettingsRoute) {
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
                            navController.navigate(ExpenseSettingsRoute) { launchSingleTop = true }
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
                composable<TransactionsRoute> {
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
                composable<PortfolioRoute> {
                    PortfolioScreen(
                        onAssetClick = { assetId ->
                            navController.navigate(AssetHistoryRoute(assetId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(PortfolioSettingsRoute) {
                                launchSingleTop = true
                            }
                        },
                        onFixedIncomeClick = { positionId ->
                            navController.navigate(FixedIncomeDetailRoute(positionId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSavingsRates = {
                            navController.navigate(SavingsRatesRoute) { launchSingleTop = true }
                        }
                    )
                }
                composable<SavingsRatesRoute> {
                    SavingsRatesScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable<NetWorthRoute> {
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
                            navController.navigate(SettingsRoute) { launchSingleTop = true }
                        }
                    )
                }
                composable<DebtsRoute> {
                    DebtListScreen()
                }
                composable<SettingsRoute> {
                    SettingsScreen(
                        navigateBack = { navController.popBackStack() },
                        onNavigateToPrivacySettings = {
                            navController.navigate(PrivacySettingsRoute) { launchSingleTop = true }
                        },
                        onNavigateToPremium = {
                            navController.navigate(PremiumRoute) { launchSingleTop = true }
                        },
                        onNavigateToAccountConfig = { accountId ->
                            navController.navigate(AccountConfigRoute(accountId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToFeedback = {
                            navController.navigate(FeedbackRoute) { launchSingleTop = true }
                        },
                        onNavigateToAbout = {
                            navController.navigate(AboutRoute) { launchSingleTop = true }
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
                            navController.navigate(ExpenseSettingsRoute) { launchSingleTop = true }
                        },
                        onNavigateToIncomeSettings = {
                            navController.navigate(IncomeSettingsRoute) { launchSingleTop = true }
                        },
                        onNavigateToTaxProfile = {
                            navController.navigate(TaxProfileSettingsRoute) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToGoals = {
                            navController.navigate(GoalSettingsRoute) { launchSingleTop = true }
                        },
                        onNavigateToEmergencyFund = {
                            navController.navigate(EmergencyFundSettingsRoute) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<GoalSettingsRoute> {
                    GoalSettingsScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable<EmergencyFundSettingsRoute> {
                    EmergencyFundSettingsScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable<TaxProfileSettingsRoute> {
                    TaxProfileSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<FeedbackRoute> {
                    FeedbackScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable<AboutRoute> {
                    AboutScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<PrivacySettingsRoute> {
                    PrivacySettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToPremium = {
                            navController.navigate(PremiumRoute) { launchSingleTop = true }
                        }
                    )
                }
                composable<PremiumRoute> {
                    PremiumScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<ExpenseSettingsRoute> {
                    ExpenseSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<IncomeSettingsRoute> {
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
                composable<ChartsRoute> {
                    AnnualSummaryScreen(
                        navigateBack = { navController.popBackStack() },
                        onNavigateToExpenseSettings = {
                            navController.navigate(ExpenseSettingsRoute) { launchSingleTop = true }
                        }
                    )
                }
                composable<PortfolioSettingsRoute> {
                    PortfolioSettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToCategoryDetail = { categoryId ->
                            navController.navigate(AssetCategoryDetailRoute(categoryId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToPlatformDetail = { _ ->
                            // TODO: Implementar navegación a detalle de plataforma
                        }
                    )
                }
                composable<FiscalReportRoute> {
                    FiscalReportScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToPremium = {
                            navController.navigate(PremiumRoute) { launchSingleTop = true }
                        }
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
    } // CompositionLocalProvider
}

@Composable
private fun BottomBarN3toContent(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    if (currentDestination.needShowBottomBar()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            FloatingBottomNavBar(
                items = bottomNavItems(),
                currentDestination = currentDestination,
                onItemClick = { item ->
                    if (currentDestination?.hasRoute(item.route::class) != true) {
                        navController.popBackStack<HomeRoute>(inclusive = false)
                        if (item.route != HomeRoute) {
                            navController.navigate(item.route) { launchSingleTop = true }
                        }
                    }
                }
            )
        }
    }
}

private fun NavDestination?.needShowBottomBar() =
    this?.let {
        it.hasRoute(HomeRoute::class) ||
                it.hasRoute(PortfolioRoute::class) ||
                it.hasRoute(NetWorthRoute::class)
    } ?: false
