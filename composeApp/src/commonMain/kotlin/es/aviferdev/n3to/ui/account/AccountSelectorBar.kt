package es.aviferdev.n3to.ui.account

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.AccountType
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.TextDisabled
import es.aviferdev.n3to.ui.theme.TextSecondary
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        accounts.forEach { account ->
            AccountChip(
                account    = account,
                isSelected = account.id == selectedAccountId,
                onClick    = { onAccountSelected(account.id) }
            )
        }
    }
}

@Composable
private fun AccountChip(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg      = if (isSelected) PrimaryAlpha      else SurfaceElevated
    val border  = if (isSelected) PrimaryDark       else BorderGray
    val txtColor = if (isSelected) PrimaryDark      else TextSecondary

    Surface(
        onClick      = onClick,
        shape        = RoundedCornerShape(20.dp),
        color        = bg,
        border       = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.dp else 0.5.dp,
            color = border
        ),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = account.name,
                fontSize   = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color      = txtColor
            )
            if (account.needsInitialBalance) {
                Spacer(Modifier.width(4.dp))
                Text("⚠️", fontSize = 11.sp)
            }
        }
    }
}

private fun createMockAccounts(): List<Account> {
    val now = Clock.System.now().toEpochMilliseconds()
    return listOf(
        Account(
            id = "1",
            name = "Cuenta Principal",
            initialBalance = 5000.0,
            computedBalance = 5200.0,
            createdAt = now,
            accountType = AccountType.GENERAL
        ),
        Account(
            id = "2",
            name = "Efectivo",
            initialBalance = 0.0,
            computedBalance = 0.0,
            createdAt = now,
            accountType = AccountType.CASH
        ),
        Account(
            id = "3",
            name = "USD Savings",
            initialBalance = 1000.0,
            computedBalance = 1050.0,
            createdAt = now,
            accountType = AccountType.GENERAL
        )
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
