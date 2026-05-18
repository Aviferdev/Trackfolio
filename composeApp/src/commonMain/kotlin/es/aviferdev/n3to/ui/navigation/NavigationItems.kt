package es.aviferdev.n3to.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart

fun bottomNavItems() = listOf(
    BottomNavItem(
        screen = Screen.Home,
        label = "Inicio",
        icon = Icons.Outlined.AccountBalanceWallet,
        selectedIcon = Icons.Filled.AccountBalanceWallet
    ),
    BottomNavItem(
        screen = Screen.Portfolio,
        label = "Cartera",
        icon = Icons.AutoMirrored.Outlined.ShowChart,
        selectedIcon = Icons.AutoMirrored.Filled.ShowChart
    ),
    BottomNavItem(
        screen = Screen.NetWorth,
        label = "Patrimonio",
        icon = Icons.Outlined.PieChart,
        selectedIcon = Icons.Filled.PieChart
    ),
)
