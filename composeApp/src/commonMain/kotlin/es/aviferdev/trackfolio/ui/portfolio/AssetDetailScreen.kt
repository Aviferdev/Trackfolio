package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Pantalla de configuración de un activo individual.
 * Permite gestionar las plataformas donde se encuentra el activo
 * (vincular/desvincular) y crear nuevas plataformas.
 */
@Composable
fun AssetDetailScreen(
    assetId: String,
    onBack: () -> Unit,
    viewModel: AssetDetailViewModel = koinViewModel(parameters = { parametersOf(assetId) })
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundGray)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary)
                }
                state.asset?.let { asset ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = asset.ticker.take(3),
                            fontSize = if (asset.ticker.length > 3) 9.sp else 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            asset.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(asset.ticker, fontSize = 12.sp, color = TextSecondary)
                    }
                } ?: Text(
                    "Activo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Plataformas vinculadas ────────────────────────────────────
            item {
                Text(
                    "PLATAFORMAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Selecciona en qué plataformas (brokers, bancos, exchanges) tienes este activo.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder(),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        if (state.allPlatforms.isEmpty()) {
                            Text(
                                "No hay plataformas creadas. Crea la primera para vincularla a este activo.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Chips de plataformas — las vinculadas aparecen seleccionadas
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.allPlatforms.forEach { platform ->
                                    val isLinked = platform.id in state.linkedPlatformIds
                                    PlatformToggleChip(
                                        icon = platform.icon,
                                        label = platform.name,
                                        isSelected = isLinked,
                                        onClick = {
                                            if (isLinked) viewModel.unlinkPlatform(platform.id)
                                            else viewModel.linkPlatform(platform.id)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Para vincular plataformas, ve a Ajustes › Portfolio › Plataformas.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // ── Resumen de plataformas vinculadas ─────────────────────────
            if (state.linkedPlatforms.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item {
                    Text(
                        "PLATAFORMAS VINCULADAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = CardDefaults.outlinedCardBorder(),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column {
                            state.linkedPlatforms.forEachIndexed { index, platform ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(platform.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = platform.name,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.unlinkPlatform(platform.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Desvincular", modifier = Modifier.size(14.dp), tint = ExpenseRed)
                                    }
                                }
                                if (index < state.linkedPlatforms.lastIndex) {
                                    HorizontalDivider(
                                        color = BorderGray,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    // ── Sheet crear plataforma ───────────────────────────────────────────────
    if (state.showAddPlatformSheet) {
        AddEditPlatformSheet(
            initial = null,
            onSave = { name, icon, notes -> viewModel.createAndLinkPlatform(name, icon, notes) },
            onDismiss = { viewModel.closeAddPlatformSheet() }
        )
    }

    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = SurfaceWhite,
            title = { Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ── Componentes locales ──────────────────────────────────────────────────────

@Composable
private fun PlatformToggleChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (isSelected) PrimaryDark.copy(alpha = 0.12f) else SurfaceElevated
    val border = if (isSelected) PrimaryDark                      else BorderGray
    val text   = if (isSelected) PrimaryDark                      else TextPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(Modifier.width(4.dp))
            Text("✓", fontSize = 12.sp, color = PrimaryDark, fontWeight = FontWeight.Bold)
        }
    }
}
