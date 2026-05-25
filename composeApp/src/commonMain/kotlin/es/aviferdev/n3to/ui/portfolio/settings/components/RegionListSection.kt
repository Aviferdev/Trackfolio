package es.aviferdev.n3to.ui.portfolio.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_settings_manage
import n3to.composeapp.generated.resources.portfolio_settings_region_empty
import n3to.composeapp.generated.resources.portfolio_settings_region_section
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegionListSection(
    regions: List<AssetRegion>,
    onManage: () -> Unit
) {
    SectionHeader(
        label = stringResource(Res.string.portfolio_settings_region_section),
        actionLabel = stringResource(Res.string.portfolio_settings_manage),
        onAction = onManage
    )
    SettingsGroupCard {
        if (regions.isEmpty()) {
            Text(
                stringResource(Res.string.portfolio_settings_region_empty),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
            )
        } else {
            regions.forEachIndexed { index, region ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = region.name,
                        fontSize = 15.sp,
                        color = MaterialTheme.appColors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < regions.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.appColors.navyBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
