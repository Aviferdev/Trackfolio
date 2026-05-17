package es.aviferdev.n3to.ui.account

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.NavySelected
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.N3toTheme
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AccountSelectorBar(
    accounts: List<Account>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        accounts.forEach { account ->
            AccountTab(
                account = account,
                isSelected = account.id == selectedAccountId,
                onClick = { onAccountSelected(account.id) }
            )
        }
    }
}

@Composable
private fun AccountTab(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorWidth"
    )

    Column(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
                    text = account.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) CyanAccent else Color.White.copy(alpha = 0.35f)
                )
            }

            Text(
                text = account.name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.40f)
            )

            if (account.needsInitialBalance) {
                Text("⚠️", fontSize = 10.sp)
            }
        }

        Box(
            modifier = Modifier
                .height(2.dp)
                .width(indicatorWidth)
                .background(CyanAccent, RoundedCornerShape(1.dp))
        )
    }
}

private fun createMockAccounts(): List<Account> {
    val now = Clock.System.now().toEpochMilliseconds()
    return listOf(
        Account(id = "1", name = "Principal", initialBalance = 5000.0, computedBalance = 5200.0, createdAt = now),
        Account(id = "2", name = "Efectivo", initialBalance = 0.0, computedBalance = 150.0, createdAt = now),
        Account(id = "3", name = "USD Savings", initialBalance = 1000.0, computedBalance = 1050.0, createdAt = now)
    )
}

@Preview
@Composable
private fun AccountSelectorBarPreview() {
    N3toTheme {
        AccountSelectorBar(
            accounts = createMockAccounts(),
            selectedAccountId = "1",
            onAccountSelected = {}
        )
    }
}
