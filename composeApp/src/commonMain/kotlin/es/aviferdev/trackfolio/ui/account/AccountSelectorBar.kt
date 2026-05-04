package es.aviferdev.trackfolio.ui.account

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Account

@Composable
fun AccountSelectorBar(
    accounts: List<Account>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    showBalance: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        accounts.forEach { account ->
            AccountChip(
                account     = account,
                isSelected  = account.id == selectedAccountId,
                showBalance = showBalance,
                onClick     = { onAccountSelected(account.id) }
            )
        }
    }
}

@Composable
private fun AccountChip(
    account: Account,
    isSelected: Boolean,
    showBalance: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick        = onClick,
        shape          = RoundedCornerShape(50),
        color          = containerColor,
        contentColor   = contentColor,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = account.name,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (account.needsInitialBalance) {
                Spacer(Modifier.width(4.dp))
                Text("⚠️", fontSize = 12.sp)
            }
        }
    }
}
