package es.aviferdev.trackfolio.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

fun bottomNavItems() = listOf(
    BottomNavItem(
        screen = Screen.Home,
        label  = "Inicio",
        icon   = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Transactions,
        label  = "Movimientos",
        icon   = Icons.Outlined.DateRange
    ),
    BottomNavItem(
        screen = Screen.Debts,
        label  = "Deudas",
        icon   = Icons.Outlined.Warning
    ),
    BottomNavItem(
        screen = Screen.Settings,
        label  = "Ajustes",
        icon   = Icons.Outlined.Settings
    )
)

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
