package es.aviferdev.n3to.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star

fun bottomNavItems() = listOf(
    BottomNavItem(
        screen = Screen.Home,
        label = "Inicio",
        icon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Portfolio,
        label = "Portfolio",
        icon = Icons.Outlined.Star
    ),
    BottomNavItem(
        screen = Screen.NetWorth,
        label = "Patrimonio",
        icon = Icons.Outlined.AccountBalance
    ),
)