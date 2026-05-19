package es.aviferdev.n3to.ui.home

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ─── Icon mapping para categorías (Material icons) ───────────────────────────

private val CATEGORY_ICON_MAP: Map<String, ImageVector> = mapOf(
    "Hogar" to Icons.Filled.Home, "Alquiler" to Icons.Filled.Home,
    "Alimentación" to Icons.Filled.LocalGroceryStore, "Comida" to Icons.Filled.LocalGroceryStore,
    "Supermercado" to Icons.Filled.LocalGroceryStore, "Restaurante" to Icons.Filled.Favorite,
    "Transporte" to Icons.Filled.Flight, "Gasolina" to Icons.Filled.Flight,
    "Salud" to Icons.Filled.Favorite, "Farmacia" to Icons.Filled.Favorite, "Médico" to Icons.Filled.Favorite,
    "Formación" to Icons.Filled.Build, "Cursos" to Icons.Filled.Build, "Libros" to Icons.Filled.Build,
    "Ocio" to Icons.Filled.Favorite, "Cine" to Icons.Filled.Favorite, "Viajes" to Icons.Filled.Flight,
    "Suscripciones" to Icons.Filled.Build,
    "Compras" to Icons.Filled.AccountBalance, "Ropa" to Icons.Filled.AccountBalance,
    "Electrónica" to Icons.Filled.AccountBalance, "Regalos" to Icons.Filled.CardGiftcard,
    "Mascotas" to Icons.Filled.Pets,
    "Comisiones" to Icons.Filled.AccountBalance, "Bancarias" to Icons.Filled.AccountBalance,
    "Seguros" to Icons.Filled.Build, "Impuestos" to Icons.Filled.AccountBalance,
    "Otros" to Icons.Filled.Home, "Varios" to Icons.Filled.Home,
)

private val DEFAULT_CATEGORY_ICON: ImageVector get() = Icons.Filled.Home

private fun iconForCategory(name: String): ImageVector =
    CATEGORY_ICON_MAP.entries.firstOrNull { (key, _) ->
        name.contains(key, ignoreCase = true)
    }?.value ?: DEFAULT_CATEGORY_ICON

@Composable
private fun incomeTypeIcon(incomeType: IncomeType): ImageVector = when (incomeType) {
    IncomeType.SALARY        -> Icons.Filled.Work
    IncomeType.BANK_INTEREST -> Icons.Filled.AccountBalance
    IncomeType.BOND_DEPOSIT  -> Icons.AutoMirrored.Filled.ReceiptLong
    IncomeType.DIVIDEND      -> Icons.AutoMirrored.Filled.ShowChart
    IncomeType.BONUS_PRIZE   -> Icons.Filled.CardGiftcard
    IncomeType.PRIZE_LOTTERY -> Icons.Filled.EmojiEvents
    IncomeType.RENTAL_INCOME -> Icons.Filled.House
    IncomeType.FREELANCE     -> Icons.Filled.BusinessCenter
    IncomeType.EXEMPT_INCOME -> Icons.AutoMirrored.Filled.Assignment
}

// ═══════════════════════════════════════════════════════════════════════════════
// Screen
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun CategoryPickerScreen(
    initialType: TransactionType,
    onCategorySelected: (categoryId: String) -> Unit = {},
    onIncomeTypeSelected: (incomeType: IncomeType) -> Unit = {},
    onCreateCategory: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: CategoryPickerViewModel = koinViewModel { parametersOf(initialType.name) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.background)) {
        val title = if (uiState.type == TransactionType.EXPENSE) "Selecciona categoría"
                    else "Selecciona tipo de ingreso"
        TopBarApp(
            title = title,
            navigateBack = onBack
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
            }
        } else {
            CategoryPickerContent(
                uiState               = uiState,
                onSearchQueryChange   = { viewModel.onSearchQueryChange(it) },
                onCategoryClick       = onCategorySelected,
                onIncomeTypeClick     = onIncomeTypeSelected,
                onCreateCategory      = onCreateCategory,
                modifier              = Modifier.fillMaxSize()
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Content — sin toggle, contenido según initialType
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryPickerContent(
    uiState: CategoryPickerUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onIncomeTypeClick: (IncomeType) -> Unit,
    onCreateCategory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))

        if (uiState.type == TransactionType.EXPENSE) {
            // ── GASTOS ─────────────────────────────────────────────────────
            // Frecuentes
            if (uiState.frequentCategories.isNotEmpty()) {
                SectionLabelC("Frecuentes")
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.frequentCategories.forEach { category ->
                        FrequentChip(
                            label   = category.name,
                            icon    = iconForCategory(category.name),
                            onClick = { onCategoryClick(category.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            // Search
            SearchField(
                query   = uiState.searchQuery,
                onChange = onSearchQueryChange,
                visible = uiState.allCategories.size > 6
            )

            // Todas las categorías
            SectionLabelC("Todas las categorías")
            Spacer(Modifier.height(8.dp))

            if (uiState.allCategories.isEmpty() && !uiState.isLoading) {
                // Sin categorías de gasto — mostrar hint + botón crear
                CategoryHintCard()
                Spacer(Modifier.weight(1f))
                CreateCategoryButton(onClick = onCreateCategory)
                Spacer(Modifier.height(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val filtered = uiState.filteredCategoryIndices.map { uiState.allCategories[it] }

                    itemsIndexed(filtered) { index, category ->
                    CategoryRow(
                        icon    = iconForCategory(category.name),
                        name   = category.name,
                        colorIndex = index,
                        onClick = { onCategoryClick(category.id) }
                    )
                    if (index < filtered.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(start = 48.dp),
                            color     = MaterialTheme.appColors.border,
                            thickness = 0.5.dp
                        )
                    }
                }

                if (filtered.isEmpty() && uiState.searchQuery.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin resultados para \"${uiState.searchQuery}\"",
                                fontSize = 13.sp,
                                color = MaterialTheme.appColors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Crear nueva categoría
                item {
                    Spacer(Modifier.height(8.dp))
                    CreateCategoryButton(onClick = onCreateCategory)
                    Spacer(Modifier.height(16.dp))
                }
            }
            } // cierra else del if (allCategories.isEmpty)
        } else {
            // ── INGRESOS — mostrar IncomeTypes ────────────────────────────
            SectionLabelC("Todos los tipos")
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                itemsIndexed(uiState.incomeTypes) { index, incomeType ->
                    IncomeTypeRow(
                        incomeType = incomeType,
                        colorIndex = index,
                        onClick    = { onIncomeTypeClick(incomeType) }
                    )
                    if (index < uiState.incomeTypes.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(start = 48.dp),
                            color     = MaterialTheme.appColors.border,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Subcomponentes
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun FrequentChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.appColors.surfaceElevated)
            .border(0.5.dp, MaterialTheme.appColors.border, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .height(60.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint    = MaterialTheme.appColors.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.appColors.textPrimary,
                textAlign  = TextAlign.Center,
                maxLines   = 1,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onChange: (String) -> Unit,
    visible: Boolean,
) {
    if (visible) {
        OutlinedTextField(
            value         = query,
            onValueChange = onChange,
            placeholder   = {
                Text(
                    "Buscar categoría…",
                    color    = MaterialTheme.appColors.textSecondary.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Buscar",
                    modifier           = Modifier.size(18.dp),
                    tint               = MaterialTheme.appColors.textSecondary
                )
            },
            singleLine    = true,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(48.dp),
            shape         = RoundedCornerShape(10.dp),
            textStyle     = LocalTextStyle.current.copy(fontSize = 13.sp, color = MaterialTheme.appColors.textPrimary),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.appColors.primary,
                unfocusedBorderColor = MaterialTheme.appColors.border,
                cursorColor          = MaterialTheme.appColors.primary
            )
        )
    }
}

@Composable
private fun CategoryRow(
    icon: ImageVector,
    name: String,
    colorIndex: Int,
    onClick: () -> Unit,
) {
    val bgColor = CategoryPalette[colorIndex % CategoryPalette.size].copy(alpha = 0.15f)
    val tintColor = CategoryPalette[colorIndex % CategoryPalette.size]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circle with colored background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint    = tintColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            name,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.appColors.textPrimary
        )
    }
}

@Composable
private fun IncomeTypeRow(
    incomeType: IncomeType,
    colorIndex: Int,
    onClick: () -> Unit,
) {
    val bgColor = CategoryPalette[colorIndex % CategoryPalette.size].copy(alpha = 0.15f)
    val tintColor = CategoryPalette[colorIndex % CategoryPalette.size]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circle with colored background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = incomeTypeIcon(incomeType),
                contentDescription = null,
                tint    = tintColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                incomeType.label,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.appColors.textPrimary
            )
            if (incomeType.hasWithholdingTax) {
                Text(
                    "Retención fiscal aplicable",
                    fontSize = 11.sp,
                    color    = MaterialTheme.appColors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun CreateCategoryButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.appColors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = "Crear categoría",
            tint    = MaterialTheme.appColors.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "Crear nueva categoría",
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.appColors.primary
        )
    }
}

@Composable
private fun CategoryHintCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.appColors.primary.copy(alpha = 0.07f))
            .border(0.5.dp, MaterialTheme.appColors.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.appColors.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "CÓMO ORGANIZAR CATEGORÍAS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.appColors.primary
            )
        }
        Text(
            text = "Agrupa gastos del mismo tipo. Ejemplos:",
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary
        )
        Text(
            text = "• Hogar → alquiler, hipoteca, suministros\n" +
                   "• Alimentación → supermercado, restaurantes\n" +
                   "• Transporte → gasolina, transporte público",
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SectionLabelC(text: String) {
    Text(
        text          = text.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Bold,
        color         = MaterialTheme.appColors.textTertiary,
        letterSpacing = 0.7.sp
    )
}
