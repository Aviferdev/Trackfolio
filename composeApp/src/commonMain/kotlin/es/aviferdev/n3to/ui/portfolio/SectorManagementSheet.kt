package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteSectorUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveSectorUseCase
import es.aviferdev.n3to.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.portfolio_sector_name
import n3to.composeapp.generated.resources.portfolio_sector_new
import n3to.composeapp.generated.resources.portfolio_settings_sector_section
import n3to.composeapp.generated.resources.portfolio_settings_sector_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val SECTOR_ICONS = listOf(
    "💻", "🏥", "⚡", "🏦", "🛒", "🏭", "🏠", "📡", "💎", "💡",
    "🚀", "🎮", "📱", "🚗", "🍔", "💊", "📚", "🎬", "🔧", "🌾"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorManagementSheet(
    onDismiss: () -> Unit
) {
    val getSectorsUseCase: GetSectorsUseCase = koinInject()
    val saveSectorUseCase: SaveSectorUseCase = koinInject()
    val deleteSectorUseCase: DeleteSectorUseCase = koinInject()
    val scope = rememberCoroutineScope()

    var sectors by remember { mutableStateOf<List<AssetSector>>(emptyList()) }
    var newName by remember { mutableStateOf("") }
    var newIcon by remember { mutableStateOf("💻") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        getSectorsUseCase().collectLatest { list ->
            sectors = list
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                stringResource(Res.string.portfolio_settings_sector_section),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                stringResource(Res.string.portfolio_settings_sector_empty),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            Text(
                stringResource(Res.string.portfolio_sector_new),
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.appColors.surfaceElevated)
                        .border(1.dp, MaterialTheme.appColors.border, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        newIcon.toMaterialIcon(),
                        contentDescription = null,
                        tint = MaterialTheme.appColors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text(stringResource(Res.string.portfolio_sector_name), fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.appColors.primary,
                        unfocusedBorderColor = MaterialTheme.appColors.border
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newName.isBlank()) return@Button
                        scope.launch {
                            saveSectorUseCase(newName, newIcon)
                                .onSuccess { newName = "" }
                                .onFailure { errorMsg = it.message }
                        }
                    },
                    enabled = newName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.primary)
                ) {
                    Text("+", fontSize = 18.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SECTOR_ICONS.forEach { icon ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (newIcon == icon) MaterialTheme.appColors.primary.copy(alpha = 0.1f) else MaterialTheme.appColors.surfaceElevated)
                            .border(
                                if (newIcon == icon) 1.5.dp else 0.5.dp,
                                if (newIcon == icon) MaterialTheme.appColors.primary else MaterialTheme.appColors.border,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { newIcon = icon },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon.toMaterialIcon(),
                            contentDescription = null,
                            tint = if (newIcon == icon) MaterialTheme.appColors.primary else MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            errorMsg?.let { msg ->
                Text(msg, fontSize = 12.sp, color = MaterialTheme.appColors.expense, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sectors, key = { it.id }) { sector ->
                        SectorItem(
                            sector = sector,
                            onDelete = {
                                scope.launch { deleteSectorUseCase(sector.id) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectorItem(
    sector: AssetSector,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.appColors.surfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            sector.icon.toMaterialIcon(),
            contentDescription = null,
            tint = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            sector.name,
            fontSize = 14.sp,
            color = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                stringResource(Res.string.common_delete),
                tint = MaterialTheme.appColors.expense.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}