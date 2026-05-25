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
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_settings_category_count_many
import n3to.composeapp.generated.resources.portfolio_settings_category_count_one
import n3to.composeapp.generated.resources.portfolio_settings_category_section
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryListSection(
    categories: List<AssetCategory>,
    assetsByCategory: Map<String?, List<*>>,
    onCategoryClick: (String) -> Unit
) {
    Text(
        stringResource(Res.string.portfolio_settings_category_section),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.appColors.textSecondary
    )
    SettingsGroupCard {
        categories.forEachIndexed { index, category ->
            val count = assetsByCategory[category.id]?.size ?: 0
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onCategoryClick(category.id) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(category.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        fontSize = 15.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Text(
                        text = if (count == 1)
                            stringResource(Res.string.portfolio_settings_category_count_one, count)
                        else
                            stringResource(Res.string.portfolio_settings_category_count_many, count),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textSecondary
                    )
                }
                Text("›", fontSize = 18.sp, color = MaterialTheme.appColors.textSecondary)
            }
            if (index < categories.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.appColors.navyBorder,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 52.dp)
                )
            }
        }
    }
}
