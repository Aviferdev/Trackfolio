package es.aviferdev.n3to.ui.debt.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.runtime.Composable
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.debt_empty_subtitle
import n3to.composeapp.generated.resources.debt_no_debts
import org.jetbrains.compose.resources.stringResource

@Composable
fun DebtEmptyState() {
    EmptyStateView(
        icon = Icons.Outlined.Handshake,
        title = stringResource(Res.string.debt_no_debts),
        subtitle = stringResource(Res.string.debt_empty_subtitle)
    )
}
