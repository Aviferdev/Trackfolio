package es.aviferdev.n3to.ui.portfolio.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_settings_add
import n3to.composeapp.generated.resources.portfolio_settings_delete_cd
import n3to.composeapp.generated.resources.portfolio_settings_edit_cd
import n3to.composeapp.generated.resources.portfolio_settings_portfolio_section
import org.jetbrains.compose.resources.stringResource

@Composable
fun PortfolioListSection(
    portfolios: List<Portfolio>,
    onAdd: () -> Unit,
    onEdit: (Portfolio) -> Unit,
    onDelete: (Portfolio) -> Unit
) {
    SectionHeader(
        label = stringResource(Res.string.portfolio_settings_portfolio_section),
        actionLabel = stringResource(Res.string.portfolio_settings_add),
        onAction = onAdd
    )
    SettingsGroupCard {
        if (portfolios.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onAdd() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.portfolio_settings_add),
                    tint = MaterialTheme.appColors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(Res.string.portfolio_settings_add),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.appColors.primary
                )
            }
        } else {
            portfolios.forEachIndexed { index, portfolio ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            portfolio.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        if (portfolio.description != null) {
                            Text(
                                portfolio.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.appColors.textSecondary
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onEdit(portfolio) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.portfolio_settings_edit_cd),
                            tint = MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = { onDelete(portfolio) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.portfolio_settings_delete_cd),
                            tint = MaterialTheme.appColors.expense,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (index < portfolios.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.appColors.navyBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 52.dp)
                    )
                }
            }
        }
    }
}
