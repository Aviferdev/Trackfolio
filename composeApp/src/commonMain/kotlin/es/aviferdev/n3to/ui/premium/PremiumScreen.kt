package es.aviferdev.n3to.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.CyanGlow
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.NavySurfaceLight
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import org.koin.compose.viewmodel.koinViewModel

private val PACKAGE_LABELS = mapOf(
    "rc_monthly" to "Mensual",
    "rc_annual" to "Anual",
    "rc_lifetime" to "Vitalicio",
    "\$rc_monthly" to "Mensual",
    "\$rc_annual" to "Anual",
    "\$rc_lifetime" to "Vitalicio",
    "premium_monthly" to "Mensual",
    "premium_yearly" to "Anual",
    "premium_lifetime" to "Vitalicio"
)

private fun getPackageLabel(identifier: String): String =
    PACKAGE_LABELS[identifier] ?: identifier

private fun isBestValue(identifier: String): Boolean =
    identifier.contains("annual") || identifier.contains("yearly")

@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PremiumEvent.PurchaseSuccess -> {
                    snackbarHostState.showSnackbar("¡Bienvenido a Trackfolio Premium!")
                    onBack()
                }
                is PremiumEvent.PurchaseError -> snackbarHostState.showSnackbar("Error: ${event.message}")
                is PremiumEvent.RestoreSuccess -> snackbarHostState.showSnackbar("Compras restauradas correctamente")
                is PremiumEvent.RestoreError -> snackbarHostState.showSnackbar("Error al restaurar: ${event.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Navegación ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = CyanAccent
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Hero ──────────────────────────────────────────────────────
            PremiumHeroCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(36.dp))

            // ── Ventajas ──────────────────────────────────────────────────
            Text(
                "Incluye",
                fontSize = 11.sp,
                color = TextTertiary,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(14.dp))

            PremiumFeatureRow(
                title = "Cuentas ilimitadas",
                description = "Gestiona todas tus cuentas sin restricciones"
            )
            PremiumFeatureRow(
                title = "Temas exclusivos",
                description = "Personaliza la app con temas claro y oscuro"
            )

            Spacer(Modifier.height(36.dp))

            // ── Planes / estado ───────────────────────────────────────────
            when {
                uiState.isPremium -> {
                    PremiumActiveCard(isLifetime = uiState.isLifetime)
                }
                uiState.productLoadError != null -> {
                    PremiumErrorCard(
                        message = uiState.productLoadError ?: "",
                        onRetry = { viewModel.retryLoadProducts() }
                    )
                }
                else -> {
                    uiState.products.forEach { product ->
                        PremiumProductCard(
                            product = ProductInfo(
                                productId = product.identifier,
                                title = getPackageLabel(product.identifier),
                                price = product.price,
                                period = product.currencyCode,
                                isBestValue = isBestValue(product.identifier)
                            ),
                            isPurchasing = uiState.purchaseInProgress == product.identifier,
                            isAnyPurchasing = uiState.purchaseInProgress != null,
                            onClick = { viewModel.purchase(product.identifier) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Restaurar compra ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .clickable(
                        enabled = uiState.purchaseInProgress == null,
                        onClick = { viewModel.restorePurchases() }
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.purchaseInProgress == "restore") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = CyanAccent
                    )
                } else {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CyanAccent.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Restaurar compra",
                    fontSize = 13.sp,
                    color = CyanAccent.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Hero glassmorphism card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PremiumHeroCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(NavySurface, NavySurfaceLight),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 110.dp.toPx()
                val cx = size.width - 30.dp.toPx()
                val cy = 30.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
            }
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(CyanAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            "Trackfolio Premium",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-0.8).sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Desbloquea todo el potencial\nde tus finanzas personales",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Feature row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PremiumFeatureRow(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(CyanAccent.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                description,
                fontSize = 12.sp,
                color = TextTertiary,
                lineHeight = 16.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Product card — plan de suscripción
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PremiumProductCard(
    product: ProductInfo,
    isPurchasing: Boolean,
    isAnyPurchasing: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (product.isBestValue) CyanAccent else NavyBorder
    val borderWidth = if (product.isBestValue) 1.5.dp else 0.5.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .background(NavySurface)
            .clickable(enabled = !isAnyPurchasing, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    product.title,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    fontWeight = FontWeight.Normal
                )
                if (product.isBestValue) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanAccent.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Mejor valor",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    product.price,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    product.period,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            if (isPurchasing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = CyanAccent,
                    trackColor = CyanAccent.copy(alpha = 0.15f)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Comenzar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanAccent
                    )
                    Text(
                        "→",
                        fontSize = 16.sp,
                        color = CyanAccent
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Estado ya premium
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PremiumActiveCard(isLifetime: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(NavySurface, NavySurfaceLight),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 90.dp.toPx()
                val cx = size.width - 30.dp.toPx()
                val cy = 30.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
            }
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(CyanAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "Ya eres Premium",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )

        if (isLifetime) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanAccent.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "Acceso vitalicio",
                    fontSize = 12.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Error al cargar productos
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PremiumErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, ExpenseRed.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .background(ExpenseRed.copy(alpha = 0.05f))
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            message,
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanAccent,
                contentColor = NavyDeep
            )
        ) {
            Text("Reintentar", fontWeight = FontWeight.SemiBold)
        }
    }
}
