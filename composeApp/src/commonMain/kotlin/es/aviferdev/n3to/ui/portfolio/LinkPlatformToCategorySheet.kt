package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.ui.theme.*

import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_add_asset_no_platforms_hint
import n3to.composeapp.generated.resources.portfolio_add_asset_select_platform
import n3to.composeapp.generated.resources.portfolio_asset_detail_linked_platforms
import n3to.composeapp.generated.resources.portfolio_asset_detail_platform_desc
import n3to.composeapp.generated.resources.portfolio_asset_detail_platform_link_hint
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/**
 * Sheet para vincular plataformas a una categoría de activos.
 *
 * Muestra las plataformas existentes que aún no están vinculadas a esta
 * categoría, permitiendo vincularlas con un tap. También permite crear
 * una plataforma nueva que se vincula automáticamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkPlatformToCategorySheet(
    categoryName: String,
    linkedPlatforms: List<Platform>,
    allPlatforms: List<Platform>,
    onLink: (platformId: String) -> Unit,
    onUnlink: (platformId: String) -> Unit,
    onCreate: (name: String, icon: String, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val linkedIds = remember(linkedPlatforms) { linkedPlatforms.map { it.id }.toSet() }
    val unlinkedPlatforms = remember(allPlatforms, linkedIds) {
        allPlatforms.filter { it.id !in linkedIds }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Plataformas de $categoryName",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.portfolio_asset_detail_platform_desc),
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(16.dp))

            // Plataformas ya vinculadas
            if (linkedPlatforms.isNotEmpty()) {
                Text(
                    stringResource(Res.string.portfolio_asset_detail_linked_platforms),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(Modifier.height(8.dp))
                linkedPlatforms.forEach { p ->
                    PlatformLinkRow(
                        platform = p,
                        isLinked = true,
                        onToggle = { onUnlink(p.id) }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Plataformas disponibles para vincular
            if (unlinkedPlatforms.isNotEmpty()) {
                Text(
                    stringResource(Res.string.portfolio_add_asset_select_platform),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.portfolio_asset_detail_platform_link_hint),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textSecondary.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                unlinkedPlatforms.forEach { p ->
                    PlatformLinkRow(
                        platform = p,
                        isLinked = false,
                        onToggle = { onLink(p.id) }
                    )
                }
                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(Res.string.portfolio_add_asset_no_platforms_hint),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PlatformLinkRow(
    platform: Platform,
    isLinked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isLinked) PrimaryDark.copy(alpha = 0.06f) else MaterialTheme.appColors.surfaceElevated)
            .border(
                0.5.dp,
                if (isLinked) PrimaryDark.copy(alpha = 0.3f) else MaterialTheme.appColors.border,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(platform.icon, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            platform.name,
            fontSize = 14.sp,
            color = MaterialTheme.appColors.textPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (isLinked) {
            Text("✓", fontSize = 16.sp, color = PrimaryDark, fontWeight = FontWeight.Bold)
        } else {
            Text("+", fontSize = 18.sp, color = MaterialTheme.appColors.textSecondary, fontWeight = FontWeight.Light)
        }
    }
    Spacer(Modifier.height(6.dp))
}

private fun createMockPlatforms(): List<Platform> {
    val now = nowMillis()
    return listOf(
        Platform(id = "1", name = "Interactive Brokers", icon = "📊", sortOrder = 0, archived = false, createdAt = now),
        Platform(id = "2", name = "Degiro", icon = "📈", sortOrder = 1, archived = false, createdAt = now),
        Platform(id = "3", name = "Coinbase", icon = "🪙", sortOrder = 2, archived = false, createdAt = now),
        Platform(id = "4", name = "Sabadell", icon = "🏦", sortOrder = 3, archived = false, createdAt = now)
    )
}

@Preview
@Composable
private fun LinkPlatformToCategorySheetPreview() {
    N3toTheme {
        LinkPlatformToCategorySheet(
            categoryName = "Acciones",
            linkedPlatforms = listOf(createMockPlatforms()[0], createMockPlatforms()[1]),
            allPlatforms = createMockPlatforms(),
            onLink = {},
            onUnlink = {},
            onCreate = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
