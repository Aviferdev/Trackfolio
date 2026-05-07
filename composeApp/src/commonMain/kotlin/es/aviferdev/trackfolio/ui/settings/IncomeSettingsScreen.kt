package es.aviferdev.trackfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.ui.theme.*

/** Tipos de ingreso visibles en la Home (excluye los gestionados desde Portfolio). */
private val HOME_INCOME_TYPES = IncomeType.entries.filter {
    it != IncomeType.DIVIDEND && it != IncomeType.BOND_DEPOSIT
}

@Composable
fun IncomeSettingsScreen(
    onBack: () -> Unit,
    onNavigateToIncomeTypeDetail: (IncomeType) -> Unit
) {
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
                Text(
                    "Ingresos",
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
            item { SectionHeader(title = "TIPOS DE INGRESO") }
            item {
                SettingsGroupCard {
                    HOME_INCOME_TYPES.forEachIndexed { index, incomeType ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onNavigateToIncomeTypeDetail(incomeType) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(incomeType.emoji, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = incomeType.label,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text("\u203A", fontSize = 18.sp, color = TextSecondary)
                        }
                        if (index < HOME_INCOME_TYPES.lastIndex) {
                            HorizontalDivider(
                                color = BorderGray,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 52.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
