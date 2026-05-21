package es.aviferdev.n3to.ui.common.topbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.component.IconActionButton
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.app_name
import n3to.composeapp.generated.resources.home_hide_balances
import n3to.composeapp.generated.resources.home_settings_cd
import n3to.composeapp.generated.resources.home_show_balances
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopBarWithoutActionsApp(
    onNavigateToSettings: (() -> Unit)? = null,
    onToggleBalances: (() -> Unit)? = null,
) {
    val balancesHidden = LocalBalanceHidden.current

    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(Res.string.app_name),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary,
                letterSpacing = (-0.3).sp
            )
        }
        if (onNavigateToSettings != null || onToggleBalances != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onToggleBalances?.let {
                    IconActionButton(
                        onClick = it,
                        icon = if (balancesHidden) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        iconTint = MaterialTheme.appColors.textSecondary,
                        label = if (balancesHidden) {
                            stringResource(Res.string.home_show_balances)
                        } else {
                            stringResource(Res.string.home_hide_balances)
                        }
                    )
                }
                onNavigateToSettings?.let {
                    IconActionButton(
                        onClick = it,
                        icon = Icons.Outlined.Settings,
                        iconTint = MaterialTheme.appColors.textSecondary,
                        label = stringResource(Res.string.home_settings_cd)
                    )
                }
            }
        }
    }
}
