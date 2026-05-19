package es.aviferdev.n3to.ui.settings

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import es.aviferdev.n3to.ui.settings.components.*
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

/** Tipos de ingreso visibles en la Home (excluye los gestionados desde Portfolio). */
private val HOME_INCOME_TYPES = IncomeType.entries.filter {
    it != IncomeType.DIVIDEND &&
    it != IncomeType.BOND_DEPOSIT &&
    it != IncomeType.BONUS_PRIZE &&
    it != IncomeType.RENTAL_INCOME
}

@Composable
fun IncomeSettingsScreen(
    onBack: () -> Unit,
    onNavigateToIncomeTypeDetail: (IncomeType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)
    ) {
        TopBarApp(
            title = "Ingresos",
            navigateBack = onBack,
            containerColor = MaterialTheme.appColors.navySurface,
            dividerColor = MaterialTheme.appColors.navyBorder
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionHeader(label = "TIPOS DE INGRESO") }
            item {
                SettingsGroupCard {
                    HOME_INCOME_TYPES.forEachIndexed { index, incomeType ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onNavigateToIncomeTypeDetail(incomeType) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(incomeType.toMaterialIcon(), contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.appColors.cyanAccent)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = incomeType.label,
                                fontSize = 15.sp,
                                color = MaterialTheme.appColors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text("\u203A", fontSize = 18.sp, color = MaterialTheme.appColors.textSecondary)
                        }
                        if (index < HOME_INCOME_TYPES.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.appColors.navyBorder,
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

@Preview
@Composable
private fun IncomeSettingsScreenPreview() {
    N3toTheme {
        IncomeSettingsScreen(
            onBack = {},
            onNavigateToIncomeTypeDetail = {}
        )
    }
}
