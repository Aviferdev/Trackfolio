package es.aviferdev.n3to.ui.premium

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_retry
import n3to.composeapp.generated.resources.premium_ad_free_desc
import n3to.composeapp.generated.resources.premium_annual_label
import n3to.composeapp.generated.resources.premium_back_cd
import n3to.composeapp.generated.resources.premium_best_value
import n3to.composeapp.generated.resources.premium_lifetime_label
import n3to.composeapp.generated.resources.premium_monthly_label
import n3to.composeapp.generated.resources.premium_multiple_accounts_desc
import n3to.composeapp.generated.resources.premium_restore
import n3to.composeapp.generated.resources.premium_restore_cd
import n3to.composeapp.generated.resources.premium_themes_desc
import n3to.composeapp.generated.resources.premium_unlock_features
import n3to.composeapp.generated.resources.premium_welcome
import n3to.composeapp.generated.resources.premium_error_format
import n3to.composeapp.generated.resources.premium_restore_success
import n3to.composeapp.generated.resources.premium_restore_error_format
import n3to.composeapp.generated.resources.premium_already_premium_subtitle
import n3to.composeapp.generated.resources.premium_lifetime_value
import n3to.composeapp.generated.resources.premium_subscribe
import n3to.composeapp.generated.resources.premium_no_ads
import n3to.composeapp.generated.resources.premium_unlimited_accounts
import n3to.composeapp.generated.resources.premium_themes
import n3to.composeapp.generated.resources.premium_already_premium
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private fun getPackageLabel(identifier: String): String = when {
    identifier.contains("monthly") -> "Mensual"
    identifier.contains("annual") || identifier.contains("yearly") -> "Anual"
    identifier.contains("lifetime") -> "Vitalicio"
    else -> identifier
}

private fun isBestValue(identifier: String): Boolean =
    identifier.contains("annual") || identifier.contains("yearly")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = koinViewModel()
) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.premium_subscribe)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.premium_back_cd))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Icono premium
            Icon(
                Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(Res.string.premium_unlock_features),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Ventajas
            PremiumFeature(
                icon = Icons.Default.CheckCircle,
                title = stringResource(Res.string.premium_unlimited_accounts),
                description = stringResource(Res.string.premium_multiple_accounts_desc)
            )
            PremiumFeature(
                icon = Icons.Default.CheckCircle,
                title = stringResource(Res.string.premium_no_ads),
                description = stringResource(Res.string.premium_ad_free_desc)
            )
            PremiumFeature(
                icon = Icons.Default.CheckCircle,
                title = stringResource(Res.string.premium_themes),
                description = stringResource(Res.string.premium_themes_desc)
            )

            Spacer(Modifier.height(32.dp))

            // Tarjetas de precio
            if (uiState.isPremium) {
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
            } else if (uiState.productLoadError != null) {
                // Error loading products — mostrar mensaje y botón de reintentar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            uiState.productLoadError ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.retryLoadProducts() }) {
                            Text(stringResource(Res.string.common_retry))
                        }
                    }
                }
            } else {
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
                        onClick = { viewModel.purchase(product.identifier) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botón Restaurar compra
            Button(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                enabled = uiState.purchaseInProgress == null
            ) {
                if (uiState.purchaseInProgress == "restore") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.premium_restore_cd))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PremiumFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumProductCard(
    product: ProductInfo,
    isPurchasing: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (product.isBestValue) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val borderColor = if (product.isBestValue) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (product.isBestValue) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isPurchasing, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (product.isBestValue) 4.dp else 0.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        product.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (product.isBestValue) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalOffer,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(Res.string.premium_best_value),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        product.price,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        product.period,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isPurchasing) {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}
