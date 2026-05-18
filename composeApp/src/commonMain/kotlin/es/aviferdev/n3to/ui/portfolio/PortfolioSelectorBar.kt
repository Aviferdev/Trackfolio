package es.aviferdev.n3to.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.ui.common.component.NavyTab
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavySelected
import es.aviferdev.n3to.ui.theme.NavySurface

@Composable
fun PortfolioSelectorBar(
    portfolios: List<Portfolio>,
    selectedPortfolioId: String?,
    onSelectPortfolio: (String?) -> Unit,
    onAddPortfolio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavyTab(
            label = "Todas",
            selected = selectedPortfolioId == null,
            onClick = { onSelectPortfolio(null) }
        )
        portfolios.forEach { portfolio ->
            PortfolioTab(
                portfolio = portfolio,
                isSelected = portfolio.id == selectedPortfolioId,
                onClick = { onSelectPortfolio(portfolio.id) }
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
                .clickable { onAddPortfolio() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Nueva cartera",
                tint = CyanAccent,
                modifier = Modifier.width(18.dp)
            )
        }
    }
}

@Composable
private fun PortfolioTab(
    portfolio: Portfolio,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavyTab(
        label = portfolio.name,
        selected = isSelected,
        onClick = onClick,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        if (isSelected) NavySelected else NavySurface,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = portfolio.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) CyanAccent else Color.White.copy(alpha = 0.35f)
                )
            }
        }
    )
}
