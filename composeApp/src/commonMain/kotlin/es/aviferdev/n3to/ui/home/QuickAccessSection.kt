package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.N3toTheme
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.home_quick_debts
import n3to.composeapp.generated.resources.home_quick_fiscal
import n3to.composeapp.generated.resources.home_quick_resumen
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun QuickAccessSection(
    onNavigateToCharts: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToFiscalReport: () -> Unit,
    hasDebts: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickCard(
                icon = Icons.Outlined.BarChart,
                label = stringResource(Res.string.home_quick_resumen),
                onClick = onNavigateToCharts,
                modifier = Modifier.weight(1f)
            )
            QuickCard(
                icon = Icons.Outlined.Handshake,
                label = stringResource(Res.string.home_quick_debts),
                onClick = onNavigateToDebts,
                showBadge = hasDebts,
                modifier = Modifier.weight(1f)
            )
            QuickCard(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                label = stringResource(Res.string.home_quick_fiscal),
                onClick = onNavigateToFiscalReport,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    showBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BadgedBox(
                badge = {
                    if (showBadge) Badge(containerColor = ExpenseRed)
                }
            ) {
                Icon(icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
            }
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview
@Composable
private fun QuickAccessSectionPreview() {
    N3toTheme {
        QuickAccessSection(
            onNavigateToCharts = {},
            onNavigateToDebts = {},
            onNavigateToFiscalReport = {}
        )
    }
}
