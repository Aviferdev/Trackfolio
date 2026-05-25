package es.aviferdev.n3to.ui.portfolio.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_settings_add
import n3to.composeapp.generated.resources.portfolio_settings_platform_empty
import n3to.composeapp.generated.resources.portfolio_settings_platform_section
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlatformListSection(
    platforms: List<Platform>,
    onAdd: () -> Unit,
    onEdit: (Platform) -> Unit
) {
    SectionHeader(
        label = stringResource(Res.string.portfolio_settings_platform_section),
        actionLabel = stringResource(Res.string.portfolio_settings_add),
        onAction = onAdd
    )
    SettingsGroupCard {
        if (platforms.isEmpty()) {
            Text(
                stringResource(Res.string.portfolio_settings_platform_empty),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
            )
        } else {
            platforms.forEachIndexed { index, platform ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onEdit(platform) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(platform.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = platform.name,
                            fontSize = 15.sp,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        if (!platform.notes.isNullOrBlank()) {
                            Text(
                                text = platform.notes,
                                fontSize = 11.sp,
                                color = MaterialTheme.appColors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                    Text("›", fontSize = 18.sp, color = MaterialTheme.appColors.textSecondary)
                }
                if (index < platforms.lastIndex) {
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
