package es.aviferdev.trackfolio.ui.common.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import es.aviferdev.trackfolio.ui.navigation.BottomNavItem
import es.aviferdev.trackfolio.ui.portfolio.AssetCategoryDetailScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetDetailScreen
import es.aviferdev.trackfolio.ui.portfolio.AssetHistoryScreen
import es.aviferdev.trackfolio.ui.portfolio.PortfolioScreen
import es.aviferdev.trackfolio.ui.portfolio.PortfolioSettingsScreen
import es.aviferdev.trackfolio.ui.settings.ExpenseSettingsScreen
import es.aviferdev.trackfolio.ui.settings.IncomeSettingsScreen
import es.aviferdev.trackfolio.ui.settings.IncomeTypeDetailScreen
import es.aviferdev.trackfolio.ui.settings.SettingsScreen
import es.aviferdev.trackfolio.ui.theme.BorderGray2
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceElevated
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.transaction.TransactionListScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun FloatingBottomNavBar(
    items: List<BottomNavItem>,
    currentDestination: androidx.navigation.NavDestination?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = remember(items, currentDestination) {
        items.indexOfFirst { item ->
            currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
        }.coerceAtLeast(0)
    }

    Surface(
        modifier = modifier
            .wrapContentWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
            .padding(bottom = 12.dp)
            .navigationBarsPadding()
            .border(1.dp, BorderGray2, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp,
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex

                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.1f else 1f,
                    animationSpec = tween(durationMillis = 200),
                    label = "scale"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (selected) TextPrimary else TextSecondary,
                    animationSpec = tween(durationMillis = 200),
                    label = "iconColor"
                )

                val textColor by animateColorAsState(
                    targetValue = if (selected) TextPrimary else TextSecondary,
                    animationSpec = tween(durationMillis = 200),
                    label = "textColor"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (selected) PrimaryDark else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.icon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(scale),
                            tint = iconColor
                        )

                        if (selected) {
                            Text(
                                text = item.label,
                                color = textColor,
                                fontSize = 14.sp,
                                modifier = Modifier.scale(scale)
                            )
                        }
                    }
                }
            }
        }
    }
}