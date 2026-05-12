package es.aviferdev.trackfolio.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
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
import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.common.navigation.TopBarApp
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ─── Emoji mapping para categorías (dado que Category model no tiene emoji) ────

private val CATEGORY_EMOJI_MAP = mapOf(
    "Hogar" to "🏠",
    "Alquiler" to "🏠",
    "Alimentación" to "🍔",
    "Comida" to "🍔",
    "Supermercado" to "🛒",
    "Restaurante" to "🍽",
    "Transporte" to "🚗",
    "Gasolina" to "⛽",
    "Salud" to "💊",
    "Farmacia" to "💊",
    "Médico" to "🩺",
    "Formación" to "📚",
    "Cursos" to "📖",
    "Libros" to "📖",
    "Ocio" to "🎬",
    "Cine" to "🎬",
    "Viajes" to "✈️",
    "Suscripciones" to "📺",
    "Compras" to "🛍️",
    "Ropa" to "👕",
    "Electrónica" to "💻",
    "Regalos" to "🎁",
    "Mascotas" to "🐾",
    "Comisiones" to "🏛",
    "Bancarias" to "🏦",
    "Seguros" to "🛡",
    "Impuestos" to "📋",
    "Otros" to "📦",
    "Varios" to "📦",
)

private fun emojiForCategory(name: String): String =
    CATEGORY_EMOJI_MAP.entries.firstOrNull { (key, _) ->
        name.contains(key, ignoreCase = true)
    }?.value ?: "📌"

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

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
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
                CircularProgressIndicator(color = PrimaryDark)
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
                            emoji   = emojiForCategory(category.name),
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
                // Sin categorías de gasto — mostrar mensaje
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay categorías de gasto",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
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
                        emoji  = emojiForCategory(category.name),
                        name   = category.name,
                        onClick = { onCategoryClick(category.id) }
                    )
                    if (index < filtered.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(start = 48.dp),
                            color     = BorderGray,
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
                                color = TextSecondary,
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
                        onClick    = { onIncomeTypeClick(incomeType) }
                    )
                    if (index < uiState.incomeTypes.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(start = 48.dp),
                            color     = BorderGray,
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
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .border(0.5.dp, BorderGray, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .height(60.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary,
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
                    color    = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Buscar",
                    modifier           = Modifier.size(18.dp),
                    tint               = TextSecondary
                )
            },
            singleLine    = true,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(48.dp),
            shape         = RoundedCornerShape(10.dp),
            textStyle     = LocalTextStyle.current.copy(fontSize = 13.sp, color = TextPrimary),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = PrimaryDark,
                unfocusedBorderColor = BorderGray,
                cursorColor          = PrimaryDark
            )
        )
    }
}

@Composable
private fun CategoryRow(
    emoji: String,
    name: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryAlpha),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 16.sp)
        }

        Spacer(Modifier.width(12.dp))

        Text(
            name,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            color      = TextPrimary
        )
    }
}

@Composable
private fun IncomeTypeRow(
    incomeType: IncomeType,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryAlpha),
            contentAlignment = Alignment.Center
        ) {
            Text(incomeType.emoji, fontSize = 16.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                incomeType.label,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary
            )
            if (incomeType.hasIrpf) {
                Text(
                    "Retención IRPF aplicable",
                    fontSize = 11.sp,
                    color    = TextTertiary
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
            .border(1.dp, BorderGray.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = "Crear categoría",
            tint    = PrimaryDark,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "Crear nueva categoría",
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = PrimaryDark
        )
    }
}

@Composable
private fun SectionLabelC(text: String) {
    Text(
        text          = text.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Bold,
        color         = TextTertiary,
        letterSpacing = 0.7.sp
    )
}
