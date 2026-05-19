package es.aviferdev.n3to.ui.fixedincome.components

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
    modifier: Modifier = Modifier
) {
    val regions = listOf("Europa", "EE.UU.", "España", "Emerging Markets", "Global")
    val sectors = listOf("Gobierno", "Corporativo", "Banca", "Energía", "Inmobiliario", "Otro")

    var showRegionDialog by remember { mutableStateOf(false) }
    var showSectorDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_distribution_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(Res.string.fixedincome_region_label), fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = position?.region ?: stringResource(Res.string.fixedincome_no_region),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.region != null) TextPrimary else TextTertiary
                    )
                }
                TextButton(onClick = { showRegionDialog = true }) {
                    Text(stringResource(Res.string.fixedincome_change_region), fontSize = 12.sp, color = PrimaryDark)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(Res.string.fixedincome_sector_label), fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = position?.sector ?: stringResource(Res.string.fixedincome_no_sector),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.sector != null) TextPrimary else TextTertiary
                    )
                }
                TextButton(onClick = { showSectorDialog = true }) {
                    Text(stringResource(Res.string.fixedincome_change_sector), fontSize = 12.sp, color = PrimaryDark)
                }
            }
        }
    }

    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            containerColor = SurfaceWhite,
            title = {
                Text(
                    stringResource(Res.string.fixedincome_select_region),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    regions.forEach { region ->
                        TextButton(
                            onClick = {
                                onUpdateRegionSector(region, position?.sector)
                                showRegionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = region,
                                color = if (position?.region == region) PrimaryDark else TextPrimary,
                                fontWeight = if (position?.region == region) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRegionDialog = false }) {
                    Text(stringResource(Res.string.common_cancel), color = TextSecondary)
                }
            }
        )
    }

    if (showSectorDialog) {
        AlertDialog(
            onDismissRequest = { showSectorDialog = false },
            containerColor = SurfaceWhite,
            title = {
                Text(
                    stringResource(Res.string.fixedincome_select_sector),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    sectors.forEach { sector ->
                        TextButton(
                            onClick = {
                                onUpdateRegionSector(position?.region, sector)
                                showSectorDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sector,
                                color = if (position?.sector == sector) PrimaryDark else TextPrimary,
                                fontWeight = if (position?.sector == sector) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSectorDialog = false }) {
                    Text(stringResource(Res.string.common_cancel), color = TextSecondary)
                }
            }
        )
    }
}
