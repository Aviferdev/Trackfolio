package es.aviferdev.n3to.ui.portfolio.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.topbar.TopBarWithoutActionsApp
import es.aviferdev.n3to.ui.theme.LocalBottomNavPadding
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.networth_no_account_subtitle
import n3to.composeapp.generated.resources.networth_no_account_title
import n3to.composeapp.generated.resources.portfolio_no_portfolios_subtitle
import n3to.composeapp.generated.resources.common_go_to_settings
import n3to.composeapp.generated.resources.portfolio_no_portfolios_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PortfolioEmptyStateNoAccount(onNavigateToSettings: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep),
        contentAlignment = Alignment.Center
    ) {
        EmptyStateView(
            icon = "\uD83C\uDFE6",
            title = stringResource(Res.string.networth_no_account_title),
            subtitle = stringResource(Res.string.networth_no_account_subtitle),
            actionLabel = stringResource(Res.string.common_go_to_settings),
            onAction = onNavigateToSettings
        )
    }
}

@Composable
fun PortfolioEmptyStateNoPortfolio(onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = LocalBottomNavPadding.current)
    ) {
        TopBarWithoutActionsApp(
            onNavigateToSettings = onNavigateToSettings
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateView(
                icon = "\uD83D\uDCC2",
                title = stringResource(Res.string.portfolio_no_portfolios_title),
                subtitle = stringResource(Res.string.portfolio_no_portfolios_subtitle),
                actionLabel = stringResource(Res.string.common_go_to_settings),
                onAction = onNavigateToSettings
            )
        }
    }
}
