package es.aviferdev.n3to.ui.account

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.ui.common.component.NavyTabRow
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview
import es.aviferdev.n3to.platform.nowMillis

@Composable
fun AccountSelectorBar(
    accounts: List<Account>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) return

    val selected = accounts.find { it.id == selectedAccountId } ?: accounts.first()

    NavyTabRow(
        items = accounts,
        selected = selected,
        onSelect = { onAccountSelected(it.id) },
        label = { if (it.needsInitialBalance) "${it.name} ⚠️" else it.name },
        modifier = modifier
    )
}

@Preview
@Composable
private fun AccountSelectorBarPreview() {
    val now = nowMillis()
    N3toTheme {
        AccountSelectorBar(
            accounts = listOf(
                Account(id = "1", name = "Principal", initialBalance = 5000.0, computedBalance = 5200.0, createdAt = now),
                Account(id = "2", name = "Efectivo", initialBalance = 0.0, computedBalance = 150.0, createdAt = now),
                Account(id = "3", name = "USD Savings", initialBalance = 1000.0, computedBalance = 1050.0, createdAt = now)
            ),
            selectedAccountId = "1",
            onAccountSelected = {}
        )
    }
}
