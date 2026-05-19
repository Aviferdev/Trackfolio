package es.aviferdev.n3to.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_retry
import n3to.composeapp.generated.resources.premium_ad_free_desc
import n3to.composeapp.generated.resources.premium_annual_label
import n3to.composeapp.generated.resources.premium_best_value
import n3to.composeapp.generated.resources.premium_lifetime_label
import n3to.composeapp.generated.resources.premium_monthly_label
import n3to.composeapp.generated.resources.premium_multiple_accounts_desc
import n3to.composeapp.generated.resources.premium_restore
import n3to.composeapp.generated.resources.premium_restore_cd
import n3to.composeapp.generated.resources.premium_themes_desc
import n3to.composeapp.generated.resources.premium_fiscal_report
import n3to.composeapp.generated.resources.premium_fiscal_report_desc
import n3to.composeapp.generated.resources.premium_backup
import n3to.composeapp.generated.resources.premium_backup_desc
import n3to.composeapp.generated.resources.premium_unlock_features
import n3to.composeapp.generated.resources.premium_welcome
import n3to.composeapp.generated.resources.premium_error_format
import n3to.composeapp.generated.resources.premium_restore_success
import n3to.composeapp.generated.resources.premium_restore_error_format
import n3to.composeapp.generated.resources.premium_already_premium_subtitle
import n3to.composeapp.generated.resources.premium_lifetime_value
import n3to.composeapp.generated.resources.premium_subscribe
import n3to.composeapp.generated.resources.premium_unlimited_accounts
import n3to.composeapp.generated.resources.premium_themes
import n3to.composeapp.generated.resources.premium_already_premium
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.sp

import es.aviferdev.n3to.ui.theme.ExpenseRed

import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import org.koin.compose.viewmodel.koinViewModel

private fun getPackageLabel(identifier: String): String = when {
    identifier.contains("monthly") -> "Mensual"
    identifier.contains("annual") || identifier.contains("yearly") -> "Anual"
    identifier.contains("lifetime") -> "Vitalicio"
    else -> identifier
}

private fun isBestValue(identifier: String): Boolean =
    identifier.contains("annual") || identifier.contains("yearly")

@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = koinViewModel()
) {
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val welcomeText = stringResource(Res.string.premium_welcome)
    val restoreSuccessText = stringResource(Res.string.premium_restore_success)
    val errorFormatText = stringResource(Res.string.premium_error_format, "")
    val restoreErrorFormatText = stringResource(Res.string.premium_restore_error_format, "")
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PremiumEvent.PurchaseSuccess -> {
                    snackbarHostState.showSnackbar(welcomeText)
                    onBack()
                }

                is PremiumEvent.PurchaseError -> {
                    snackbarHostState.showSnackbar(errorFormatText.replace(": %1\$s", ": ${event.message}").replace(": ", ": ${event.message}"))
                }

                is PremiumEvent.RestoreSuccess -> {
                    snackbarHostState.showSnackbar(restoreSuccessText)
                }

                is PremiumEvent.RestoreError -> {
                    snackbarHostState.showSnackbar(restoreErrorFormatText.replace(": %1\$s", ": ${event.message}").replace(": ", ": ${event.message}"))
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBarApp(
                title = stringResource(Res.string.premium_subscribe),
                navigateBack = onBack
            )

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
                stringResource(Res.string.premium_unlock_features),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            PremiumFeatureRow(
                title = stringResource(Res.string.premium_unlimited_accounts),
                description = stringResource(Res.string.premium_multiple_accounts_desc)
            )
            PremiumFeatureRow(
                title = stringResource(Res.string.premium_themes),
                description = stringResource(Res.string.premium_themes_desc)
            )
            PremiumFeatureRow(
                title = stringResource(Res.string.premium_fiscal_report),
                description = stringResource(Res.string.premium_fiscal_report_desc)
            )
            PremiumFeatureRow(
                title = stringResource(Res.string.premium_backup),
                description = stringResource(Res.string.premium_backup_desc)
            )

            Spacer(Modifier.height(36.dp))

            // Tarjetas de precio
            when {
                uiState.isPremium -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(Res.string.premium_already_premium_subtitle),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.isLifetime) {
                            Text(stringResource(Res.string.premium_lifetime_value), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
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
                        color = appCCyanAccent
                    )
                } else {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.premium_restore_cd))
                }
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
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    Column(
        modifier = modifier
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
                val orbRadius = 110.dp.toPx()
                val cx = size.width - 30.dp.toPx()
                val cy = 30.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appCCyanGlow.copy(alpha = 0.18f), Color.Transparent),
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
                .background(appCCyanAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = appCCyanAccent,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            "Trackfolio Premium",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = appCTextPrimary,
            letterSpacing = (-0.8).sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Desbloquea todo el potencial\nde tus finanzas personales",
            fontSize = 13.sp,
            color = appCTextSecondary,
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
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
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
                .background(appCCyanAccent.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = appCCyanAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = appCTextPrimary
            )
            Text(
                description,
                fontSize = 12.sp,
                color = appCTextTertiary,
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
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    val borderColor = if (product.isBestValue) appCCyanAccent else appCNavyBorder
    val borderWidth = if (product.isBestValue) 1.5.dp else 0.5.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .background(heroCardBg1)
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
                    color = appCTextTertiary,
                    fontWeight = FontWeight.Normal
                )
                if (product.isBestValue) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(appCCyanAccent.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            stringResource(Res.string.premium_best_value),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = appCCyanAccent
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
                    color = appCTextPrimary,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    product.period,
                    fontSize = 13.sp,
                    color = appCTextSecondary,
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
                    color = appCCyanAccent,
                    trackColor = appCCyanAccent.copy(alpha = 0.15f)
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
                        color = appCCyanAccent
                    )
                    Text(
                        "→",
                        fontSize = 16.sp,
                        color = appCCyanAccent
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
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                val cx = size.width - 30.dp.toPx()
                val cy = 30.dp.toPx()
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
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(appCCyanAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = appCCyanAccent,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "Ya eres Premium",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = appCTextPrimary,
            letterSpacing = (-0.5).sp
        )

        if (isLifetime) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(appCCyanAccent.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "Acceso vitalicio",
                    fontSize = 12.sp,
                    color = appCCyanAccent,
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
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCNavyDeep = MaterialTheme.appColors.navyDeep
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
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
            color = appCTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appCCyanAccent,
                contentColor = appCNavyDeep
            )
        ) {
            Text("Reintentar", fontWeight = FontWeight.SemiBold)
        }
    }
}
