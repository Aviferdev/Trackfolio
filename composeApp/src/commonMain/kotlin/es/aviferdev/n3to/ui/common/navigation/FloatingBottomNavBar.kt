package es.aviferdev.n3to.ui.common.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import es.aviferdev.n3to.ui.navigation.BottomNavItem
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavySelected
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary

@Composable
fun FloatingBottomNavBar(
    items: List<BottomNavItem>,
    currentDestination: androidx.navigation.NavDestination?,
    onItemClick: (BottomNavItem) -> Unit,
    homeBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val selectedIndex = remember(items, currentDestination) {
        items.indexOfFirst { item ->
            currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
        }
    }

    Surface(
        modifier = modifier
            .wrapContentWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
            .padding(bottom = 12.dp)
            .navigationBarsPadding()
            .border(0.5.dp, NavyBorder, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 0.dp,
        color = NavySurface
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
                val isHomeTab = item.screen.route == "home"

                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.05f else 1f,
                    animationSpec = tween(durationMillis = 200),
                    label = "scale"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (selected) CyanAccent else TextSecondary,
                    animationSpec = tween(durationMillis = 200),
                    label = "iconColor"
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) CyanAccent else TextSecondary,
                    animationSpec = tween(durationMillis = 200),
                    label = "textColor"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) NavySelected else Color.Transparent)
                        .border(
                            width = if (selected) 0.5.dp else 0.dp,
                            color = if (selected) NavyBorder else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(scale),
                                tint = iconColor
                            )
                            // Badde de alerta en la pestaña Home
                            if (isHomeTab && homeBadgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (homeBadgeCount > 9) "9+" else homeBadgeCount.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        if (selected) {
                            Text(
                                text = item.label,
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.sp,
                                modifier = Modifier.scale(scale)
                            )
                        }
                    }
                }
            }
        }
    }
}
