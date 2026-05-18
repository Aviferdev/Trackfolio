package es.aviferdev.n3to.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.runtime.Composable
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.nav_home
import n3to.composeapp.generated.resources.nav_portfolio
import n3to.composeapp.generated.resources.nav_networth
import org.jetbrains.compose.resources.stringResource

@Composable
fun bottomNavItems() = listOf(
    BottomNavItem(
        screen = Screen.Home,
        label = stringResource(Res.string.nav_home),
        icon = Icons.Outlined.AccountBalanceWallet,
        selectedIcon = Icons.Filled.AccountBalanceWallet
    ),
    BottomNavItem(
        screen = Screen.Portfolio,
        label = stringResource(Res.string.nav_portfolio),
        icon = Icons.AutoMirrored.Outlined.ShowChart,
        selectedIcon = Icons.AutoMirrored.Filled.ShowChart
    ),
    BottomNavItem(
        screen = Screen.NetWorth,
        label = stringResource(Res.string.nav_networth),
        icon = Icons.Outlined.PieChart,
        selectedIcon = Icons.Filled.PieChart
    ),
)
