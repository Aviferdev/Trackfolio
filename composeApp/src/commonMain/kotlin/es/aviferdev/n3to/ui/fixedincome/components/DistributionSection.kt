package es.aviferdev.n3to.ui.fixedincome.components

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.fixedincome_change_region
import n3to.composeapp.generated.resources.fixedincome_change_sector
import n3to.composeapp.generated.resources.fixedincome_distribution_title
import n3to.composeapp.generated.resources.fixedincome_no_region
import n3to.composeapp.generated.resources.fixedincome_no_sector
import n3to.composeapp.generated.resources.fixedincome_region_label
import n3to.composeapp.generated.resources.fixedincome_sector_label
import n3to.composeapp.generated.resources.fixedincome_select_region
import n3to.composeapp.generated.resources.fixedincome_select_sector
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DistributionSection(
    position: FixedIncomePosition?,
    onUpdateRegionSector: (String?, String?) -> Unit,
    allRegions: List<AssetRegion>,
    allSectors: List<AssetSector>,
    modifier: Modifier = Modifier
) {
    var showRegionDialog by remember { mutableStateOf(false) }
    var showSectorDialog by remember { mutableStateOf(false) }

    // Look up display names from catalogs by ID
    val currentRegionName = position?.regionId?.let { regionId ->
        allRegions.firstOrNull { it.id == regionId }?.name
    }
    val currentSectorName = position?.sectorId?.let { sectorId ->
        allSectors.firstOrNull { it.id == sectorId }?.name
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_distribution_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(Res.string.fixedincome_region_label),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textSecondary
                    )
                    Text(
                        text = currentRegionName ?: stringResource(Res.string.fixedincome_no_region),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.regionId != null) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textTertiary
                    )
                }
                TextButton(onClick = { showRegionDialog = true }) {
                    Text(
                        stringResource(Res.string.fixedincome_change_region),
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.primary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(Res.string.fixedincome_sector_label),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textSecondary
                    )
                    Text(
                        text = currentSectorName ?: stringResource(Res.string.fixedincome_no_sector),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.sectorId != null) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textTertiary
                    )
                }
                TextButton(onClick = { showSectorDialog = true }) {
                    Text(
                        stringResource(Res.string.fixedincome_change_sector),
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.primary
                    )
                }
            }
        }
    }

    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.fixedincome_select_region),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    allRegions.forEach { region ->
                        TextButton(
                            onClick = {
                                onUpdateRegionSector(region.id, position?.sectorId)
                                showRegionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = region.name,
                                color = if (position?.regionId == region.id) MaterialTheme.appColors.primary else MaterialTheme.appColors.textPrimary,
                                fontWeight = if (position?.regionId == region.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRegionDialog = false }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.textSecondary
                    )
                }
            }
        )
    }

    if (showSectorDialog) {
        AlertDialog(
            onDismissRequest = { showSectorDialog = false },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.fixedincome_select_sector),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    allSectors.forEach { sector ->
                        TextButton(
                            onClick = {
                                onUpdateRegionSector(position?.regionId, sector.id)
                                showSectorDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sector.name,
                                color = if (position?.sectorId == sector.id) MaterialTheme.appColors.primary else MaterialTheme.appColors.textPrimary,
                                fontWeight = if (position?.sectorId == sector.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSectorDialog = false }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.textSecondary
                    )
                }
            }
        )
    }
}
