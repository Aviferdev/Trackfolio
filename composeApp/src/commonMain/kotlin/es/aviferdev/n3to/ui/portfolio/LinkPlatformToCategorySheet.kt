package es.aviferdev.n3to.ui.portfolio

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

import org.jetbrains.compose.ui.tooling.preview.Preview

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
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Plataformas de $categoryName",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Selecciona las plataformas donde operas activos de esta categoría.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(16.dp))

            // Plataformas ya vinculadas
            if (linkedPlatforms.isNotEmpty()) {
                Text(
                    "VINCULADAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
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
                    "DISPONIBLES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Estas plataformas ya existen en otras categorías. Pulsa para vincularlas también aquí.",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.7f)
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
                    "Para crear plataformas, ve a Ajustes › Portfolio › Plataformas.",
                    fontSize = 12.sp,
                    color = TextSecondary,
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
            .background(if (isLinked) PrimaryDark.copy(alpha = 0.06f) else SurfaceElevated)
            .border(
                0.5.dp,
                if (isLinked) PrimaryDark.copy(alpha = 0.3f) else BorderGray,
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
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (isLinked) {
            Text("✓", fontSize = 16.sp, color = PrimaryDark, fontWeight = FontWeight.Bold)
        } else {
            Text("+", fontSize = 18.sp, color = TextSecondary, fontWeight = FontWeight.Light)
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
