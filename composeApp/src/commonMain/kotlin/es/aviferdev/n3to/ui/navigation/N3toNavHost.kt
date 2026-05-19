package es.aviferdev.n3to.ui.navigation

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.core.security.AppSettings

import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import es.aviferdev.n3to.ui.loan.LoanDetailScreen
import es.aviferdev.n3to.ui.networth.NetWorthScreen
import es.aviferdev.n3to.ui.portfolio.AssetCategoryDetailScreen
import es.aviferdev.n3to.ui.portfolio.AssetDetailScreen
import es.aviferdev.n3to.ui.portfolio.AssetHistoryScreen
import es.aviferdev.n3to.ui.portfolio.PortfolioScreen
import es.aviferdev.n3to.ui.portfolio.PortfolioSettingsScreen
import es.aviferdev.n3to.ui.premium.PremiumScreen
import es.aviferdev.n3to.ui.realestate.RealEstateDetailScreen
import es.aviferdev.n3to.ui.valuable.ValuableDetailScreen
import es.aviferdev.n3to.ui.settings.AboutScreen
import es.aviferdev.n3to.ui.settings.ExpenseSettingsScreen
import es.aviferdev.n3to.ui.settings.feedback.FeedbackScreen
import es.aviferdev.n3to.ui.settings.IncomeSettingsScreen
import es.aviferdev.n3to.ui.settings.IncomeTypeDetailScreen
import es.aviferdev.n3to.ui.settings.PrivacySettingsScreen
import es.aviferdev.n3to.ui.settings.AccountConfigScreen
import es.aviferdev.n3to.ui.settings.SettingsScreen
import es.aviferdev.n3to.ui.settings.emergencyfund.EmergencyFundSettingsScreen
import es.aviferdev.n3to.ui.settings.goal.GoalSettingsScreen
import es.aviferdev.n3to.ui.settings.taxprofile.TaxProfileSettingsScreen
import es.aviferdev.n3to.ui.transaction.TransactionDetailScreen
import es.aviferdev.n3to.ui.transaction.TransactionListScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun N3toNavHost(
    onResetOnboarding: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val loadingManager = koinInject<GlobalLoadingManager>()
    val isLoading by loadingManager.isLoading.collectAsState()
    val loadingMessage by loadingManager.loadingMessage.collectAsState()

    val settings = koinInject<AppSettings>()
    var showTabsIntro by remember { mutableStateOf(!settings.getBool("has_seen_tabs_intro")) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                val hideRoutes = listOf(
                    Screen.TransactionDetail.route,
                    Screen.Transactions.route,
                    Screen.CategoryPicker.route,
                    Screen.FiscalReport.route,
                    Screen.Settings.route,
                    Screen.Debts.route,
                    Screen.Charts.route,
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
                        onNavigateToEmergencyFundSettings = {
                            navController.navigate(Screen.EmergencyFundSettings.route) { launchSingleTop = true }
                        },
                        onNavigateToFixedIncomeDetail = { positionId ->
                            navController.navigate(Screen.FixedIncomeDetail.buildRoute(positionId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCategoryPicker = { type ->
                            navController.navigate(Screen.CategoryPicker.buildRoute(type.name)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAccountConfig = { accountId ->
                            navController.navigate(Screen.AccountConfig.createRoute(accountId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToExpenseSettings = {
                            navController.navigate(Screen.ExpenseSettings.route) { launchSingleTop = true }
                        }
                    )
                    // Reabrir sheet al volver del CategoryPicker
                    val catPickerCatId = navController.currentBackStackEntry
                        ?.savedStateHandle?.get<String>("selected_category_id")
                    val catPickerIncType = navController.currentBackStackEntry
                        ?.savedStateHandle?.get<String>("selected_income_type")

                    if (catPickerCatId != null || catPickerIncType != null) {
                        val homeVM: es.aviferdev.n3to.ui.home.AddTransactionViewModel = koinViewModel()
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
                        onNavigateToEmergencyFundSettings = {
                            navController.navigate(Screen.EmergencyFundSettings.route) { launchSingleTop = true }
                        },
                        onNavigateToFixedIncomeDetail = { positionId ->
                            navController.navigate(Screen.FixedIncomeDetail.buildRoute(positionId)) {
                                launchSingleTop = true
                            }
                        },
                         onNavigateToCategoryPicker = { type ->
                            navController.navigate(Screen.CategoryPicker.buildRoute(type.name)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAccountConfig = { accountId ->
                            navController.navigate(Screen.AccountConfig.createRoute(accountId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToExpenseSettings = {
                            navController.navigate(Screen.ExpenseSettings.route) { launchSingleTop = true }
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
                        savedStateHandle.remove<String>("edit_transaction_id")
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
                        },
                        onPropertyClick = { propertyId ->
                            navController.navigate(Screen.RealEstateDetail.buildRoute(propertyId)) {
                                launchSingleTop = true
                            }
                        },
                        onValuableClick = { valuableId ->
                            navController.navigate(Screen.ValuableDetail.buildRoute(valuableId)) {
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
                            navController.navigate(Screen.AccountConfig.createRoute(accountId)) {
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
                        onResetOnboarding = onResetOnboarding
                    )
                }
                composable(
                    route = Screen.AccountConfig.route,
                    arguments = listOf(navArgument(Screen.AccountConfig.ARG_ACCOUNT_ID) { type = NavType.StringType })
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.getString(Screen.AccountConfig.ARG_ACCOUNT_ID) ?: return@composable
                    AccountConfigScreen(
                        accountId = accountId,
                        onBack = { navController.popBackStack() },
                        onNavigateToExpenseSettings = {
                            navController.navigate(Screen.ExpenseSettings.route) { launchSingleTop = true }
                        },
                        onNavigateToIncomeSettings = {
                            navController.navigate(Screen.IncomeSettings.route) { launchSingleTop = true }
                        },
                        onNavigateToTaxProfile = {
                            navController.navigate(Screen.TaxProfileSettings.route) { launchSingleTop = true }
                        },
                        onNavigateToGoals = {
                            navController.navigate(Screen.GoalSettings.route) { launchSingleTop = true }
                        },
                        onNavigateToEmergencyFund = {
                            navController.navigate(Screen.EmergencyFundSettings.route) { launchSingleTop = true }
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
                    AnnualSummaryScreen(
                        navigateBack = { navController.popBackStack() }
                    )
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
                    route = Screen.RealEstateDetail.route,
                    arguments = listOf(
                        navArgument(Screen.RealEstateDetail.ARG_PROPERTY_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val propertyId = backStackEntry.arguments
                        ?.getString(Screen.RealEstateDetail.ARG_PROPERTY_ID).orEmpty()
                    RealEstateDetailScreen(
                        propertyId = propertyId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLoan = { loanId ->
                            navController.navigate(Screen.LoanDetail.buildRoute(loanId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = Screen.ValuableDetail.route,
                    arguments = listOf(
                        navArgument(Screen.ValuableDetail.ARG_VALUABLE_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val valuableId = backStackEntry.arguments
                        ?.getString(Screen.ValuableDetail.ARG_VALUABLE_ID).orEmpty()
                    ValuableDetailScreen(
                        valuableId = valuableId,
                        onNavigateBack = { navController.popBackStack() }
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

        if (showTabsIntro) {
            WelcomeTabsDialog(
                onDismiss = {
                    settings.putBool("has_seen_tabs_intro", true)
                    showTabsIntro = false
                }
            )
        }
    }
}

@Composable
private fun WelcomeTabsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.appColors.navySurface,
        title = {
            Text(
                "Tus 3 secciones principales",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WelcomeTabRow(
                    label = "Home",
                    description = "Registra gastos e ingresos, consulta tu balance mensual y controla el fondo de emergencia."
                )
                WelcomeTabRow(
                    label = "Portfolio",
                    description = "Gestiona tus inversiones: acciones, ETFs, fondos, renta fija y otros activos."
                )
                WelcomeTabRow(
                    label = "Patrimonio Neto",
                    description = "Tu riqueza total: cuentas e inversiones más activos reales (vivienda, vehículos…) menos deudas."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = MaterialTheme.appColors.cyanAccent, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun WelcomeTabRow(label: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.cyanAccent)
        Text(description, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary, lineHeight = 18.sp)
    }
}

