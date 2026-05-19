package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.portfolio_update_price_current
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AssetDetailScreen(
    assetId: String,
    onBack: () -> Unit,
    viewModel: AssetDetailViewModel = koinViewModel(parameters = { parametersOf(assetId) })
) {
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    val state by viewModel.uiState.collectAsState()

    AssetDetailContent(
        state = state,
        onBack = onBack,
        onLinkPlatform = { platformId -> viewModel.linkPlatform(platformId) },
        onUnlinkPlatform = { platformId -> viewModel.unlinkPlatform(platformId) }
    )

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
            containerColor = heroCardBg1,
            title = {
                Text(
                    "Error",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appCTextPrimary
                )
            },
            text = { Text(msg, fontSize = 14.sp, color = appCTextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(stringResource(Res.string.common_accept), color = appCCyanAccent, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AssetDetailContent(
    state: AssetDetailUiState,
    onBack: () -> Unit,
    onLinkPlatform: (String) -> Unit,
    onUnlinkPlatform: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    val appCNavyDeep = MaterialTheme.appColors.navyDeep
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appCNavyDeep)
    ) {
        TopBarApp(
            title = state.asset?.name ?: "Activo",
            subtitle = state.asset?.ticker,
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.asset?.let { asset ->
                item { AssetHeroCard(asset = asset) }
                item { Spacer(Modifier.height(4.dp)) }
            }

            item {
                NavySectionLabel("PLATAFORMAS")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Selecciona en qué plataformas (brokers, bancos, exchanges) tienes este activo.",
                    fontSize = 12.sp,
                    color = appCTextTertiary
                )
            }

            item {
                NavyCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        if (state.allPlatforms.isEmpty()) {
                            Text(
                                "No hay plataformas creadas. Crea la primera para vincularla a este activo.",
                                fontSize = 13.sp,
                                color = appCTextTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
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
                                            if (isLinked) onUnlinkPlatform(platform.id)
                                            else onLinkPlatform(platform.id)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Para vincular plataformas, ve a Ajustes › Portfolio › Plataformas.",
                            fontSize = 12.sp,
                            color = appCTextTertiary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            if (state.linkedPlatforms.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item { NavySectionLabel("PLATAFORMAS VINCULADAS") }
                item {
                    NavyCard {
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
                                        color = appCTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onUnlinkPlatform(platform.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Desvincular",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.appColors.expense
                                        )
                                    }
                                }
                                if (index < state.linkedPlatforms.lastIndex) {
                                    HorizontalDivider(
                                        color = appCNavyBorder,
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
}

// ── Componentes locales ──────────────────────────────────────────────────────

@Composable
private fun AssetHeroCard(asset: Asset, modifier: Modifier = Modifier) {
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    val appCCyanSubtle = MaterialTheme.appColors.cyanSubtle
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(heroCardBg1, heroCardBg2),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 90.dp.toPx()
                val cx = size.width - 40.dp.toPx()
                val cy = 40.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appCCyanGlow.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
            }
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Text(
            text = "ACTIVO",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.45f),
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = asset.ticker.ifBlank { asset.name },
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-1.5).sp,
            lineHeight = 36.sp
        )
        if (asset.ticker.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = asset.name,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.55f),
                fontWeight = FontWeight.Normal
            )
        }

        val price = asset.currentPrice
        if (price != null && price > 0.0) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.portfolio_update_price_current),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
                Text(
                    text = "${formatAmount(price)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appCCyanSubtle
                )
            }
        }
    }
}

@Composable
private fun NavySectionLabel(text: String) {
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = appCTextTertiary,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun NavyCard(content: @Composable () -> Unit) {
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(heroCardBg1)
            .border(0.5.dp, appCNavyBorder, RoundedCornerShape(14.dp))
    ) {
        content()
    }
}

@Composable
private fun PlatformToggleChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val appCNavySurfaceLight = MaterialTheme.appColors.navySurfaceLight
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    val bg     = if (isSelected) appCCyanAccent.copy(alpha = 0.12f) else appCNavySurfaceLight
    val border = if (isSelected) appCCyanAccent                      else appCNavyBorder
    val text   = if (isSelected) appCCyanAccent                      else appCTextSecondary

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
            Text("✓", fontSize = 12.sp, color = appCCyanAccent, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview
@Composable
private fun AssetDetailContentPreview() {
    N3toTheme {
        AssetDetailContent(
            state = AssetDetailUiState(
                asset = Asset(
                    id = "1",
                    accountId = "acc1",
                    ticker = "AAPL",
                    name = "Apple Inc.",
                    notes = null,
                    createdAt = 0L,
                    assetCategoryId = "cat1",
                    currentPrice = 150.0
                ),
                allPlatforms = listOf(
                    Platform(id = "p1", name = "Interactive Brokers", icon = "🏦", sortOrder = 0, createdAt = 0L),
                    Platform(id = "p2", name = "DeGiro", icon = "🏛️", sortOrder = 1, createdAt = 0L)
                ),
                linkedPlatformIds = setOf("p1"),
                linkedPlatforms = listOf(
                    Platform(id = "p1", name = "Interactive Brokers", icon = "🏦", sortOrder = 0, createdAt = 0L)
                )
            ),
            onBack = {},
            onLinkPlatform = {},
            onUnlinkPlatform = {}
        )
    }
}
