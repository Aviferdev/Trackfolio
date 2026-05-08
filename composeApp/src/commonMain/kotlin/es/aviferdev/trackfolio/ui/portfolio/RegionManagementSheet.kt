package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.AssetRegion
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.DeleteRegionUseCase
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.SaveRegionUseCase
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionManagementSheet(
    onDismiss: () -> Unit
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderGray)
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
                "Gestión de Regiones",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                "Crea y elimina regiones geográficas para distribuir tus activos.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            Text(
                "Nueva región",
                fontSize = 12.sp,
                color = TextSecondary,
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
                    placeholder = { Text("Nombre (ej. Asia, Iberoamerica)", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDark,
                        unfocusedBorderColor = BorderGray
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
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("+", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            errorMsg?.let { msg ->
                Text(msg, fontSize = 12.sp, color = ExpenseRed, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryDark)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(regions, key = { it.id }) { region ->
                        RegionItem(
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
}

@Composable
private fun RegionItem(
    region: AssetRegion,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            region.name,
            fontSize = 14.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                "Eliminar",
                tint = ExpenseRed.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}