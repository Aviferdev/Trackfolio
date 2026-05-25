package es.aviferdev.n3to.ui.portfolio.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteRegionUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveRegionUseCase
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.portfolio_region_name
import n3to.composeapp.generated.resources.portfolio_region_new
import n3to.composeapp.generated.resources.portfolio_settings_region_empty
import n3to.composeapp.generated.resources.portfolio_settings_region_section
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun RegionManagementScreen(
    onBack: () -> Unit
) {
    val getRegionsUseCase: GetRegionsUseCase = koinInject()
    val saveRegionUseCase: SaveRegionUseCase = koinInject()
    val deleteRegionUseCase: DeleteRegionUseCase = koinInject()
    val scope = rememberCoroutineScope()

    var regions by remember { mutableStateOf<List<AssetRegion>>(emptyList()) }
    var newName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        getRegionsUseCase().collectLatest { list ->
            regions = list
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
            .imePadding()
    ) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.portfolio_settings_region_section),
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(Res.string.portfolio_settings_region_empty),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }

            item {
                Text(
                    stringResource(Res.string.portfolio_region_new),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = {
                            Text(
                                stringResource(Res.string.portfolio_region_name),
                                fontSize = 14.sp
                            )
                        },
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
                                saveRegionUseCase(newName)
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

                errorMsg?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        msg,
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.expense
                    )
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                    }
                }
            } else {
                items(regions, key = { it.id }) { region ->
                    RegionScreenItem(
                        region = region,
                        onDelete = {
                            scope.launch { deleteRegionUseCase(region.id) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegionScreenItem(
    region: AssetRegion,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.appColors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            region.name,
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
