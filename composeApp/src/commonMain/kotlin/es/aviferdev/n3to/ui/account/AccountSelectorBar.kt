package es.aviferdev.n3to.ui.account

import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.NavySelected
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary

import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AccountSelectorBar(
    accounts: List<Account>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    onNavigateToAccountConfig: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        accounts.forEach { account ->
            AccountChip(
                account = account,
                isSelected = account.id == selectedAccountId,
                onClick = { onAccountSelected(account.id) },
                onSettingsClick = { onNavigateToAccountConfig(account.id) }
            )
        }
    }
}

private val ChipShape = RoundedCornerShape(20.dp)

@Composable
private fun AccountChip(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(ChipShape)
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) CyanAccent.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.07f),
                shape = ChipShape
            )
            .background(
                if (isSelected) NavySelected else NavySurface,
                ChipShape
            )
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Left: tap to switch account ──────────────────────────────────────
        Row(
            modifier = Modifier
                .clickable(
                    enabled = !isSelected,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        if (isSelected) CyanAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f),
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

        // ── Divider ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(0.5.dp)
                .height(18.dp)
                .background(
                    if (isSelected) CyanAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.07f)
                )
        )

        // ── Right: settings icon ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onSettingsClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Configurar cuenta",
                tint = if (isSelected) TextSecondary else TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private fun createMockAccounts(): List<Account> {
    val now = nowMillis()
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
            onAccountSelected = {},
            onNavigateToAccountConfig = {}
        )
    }
}
