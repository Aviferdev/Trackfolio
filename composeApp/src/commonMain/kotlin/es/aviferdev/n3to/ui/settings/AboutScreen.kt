package es.aviferdev.n3to.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.core.premium.PremiumManager
import es.aviferdev.n3to.ui.common.InitialsAvatar
import es.aviferdev.n3to.ui.common.N3toLabel
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.coroutines.delay
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.about_app_name
import n3to.composeapp.generated.resources.about_contact_label
import n3to.composeapp.generated.resources.about_developer_label
import n3to.composeapp.generated.resources.about_developer_value
import n3to.composeapp.generated.resources.about_license_label
import n3to.composeapp.generated.resources.about_license_value
import n3to.composeapp.generated.resources.about_rate_app_label
import n3to.composeapp.generated.resources.about_revenuecat_id_label
import n3to.composeapp.generated.resources.about_section_app
import n3to.composeapp.generated.resources.about_section_dev
import n3to.composeapp.generated.resources.about_section_rate
import n3to.composeapp.generated.resources.about_share_app_label
import n3to.composeapp.generated.resources.about_tagline
import n3to.composeapp.generated.resources.about_title
import n3to.composeapp.generated.resources.about_version_label
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

// ─── WRAPPER ────────────────────────────────────────────────────────────────────
@Composable
fun AboutScreen(
    onBack: () -> Unit = {},
    onOpenStore: () -> Unit = koinInject(named("openStore")),
    onShareApp: () -> Unit = koinInject(named("shareApp")),
    appVersion: String = koinInject(named("appVersion")),
    premiumManager: PremiumManager = koinInject()
) {
    val premiumStatus by premiumManager.status.collectAsState()
    AboutContent(
        onBack = onBack,
        onOpenStore = onOpenStore,
        onShareApp = onShareApp,
        appVersion = appVersion,
        appUserId = premiumStatus.appUserId
    )
}

// ─── CONTENT ────────────────────────────────────────────────────────────────────
@Composable
fun AboutContent(
    onBack: () -> Unit = {},
    onOpenStore: () -> Unit = {},
    onShareApp: () -> Unit = {},
    appVersion: String = "1.0.0",
    appUserId: String = "",
    modifier: Modifier = Modifier
) {
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCNavyDeep = MaterialTheme.appColors.navyDeep
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    var tapCount by remember { mutableIntStateOf(0) }
    var showDevInfo by remember { mutableStateOf(false) }
    var copiedToClipboard by remember { mutableStateOf(false) }
    var copiedEmail by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            delay(1500)
            tapCount = 0
        }
    }
    LaunchedEffect(copiedToClipboard) {
        if (copiedToClipboard) {
            delay(1500)
            copiedToClipboard = false
        }
    }
    LaunchedEffect(copiedEmail) {
        if (copiedEmail) {
            delay(1500)
            copiedEmail = false
        }
    }

    Column(modifier = modifier.fillMaxSize().background(appCNavyDeep)) {
        TopBarWithActionsApp(title = stringResource(Res.string.about_title), navigateBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Cabecera hero ─────────────────────────────────────────────────
            item {
                AboutHeaderSection()
            }

            // ── Información de la aplicación ──────────────────────────────────
            item {
                N3toLabel(text = stringResource(Res.string.about_section_app))
                Spacer(Modifier.height(8.dp))
                AboutGroupCard {
                    AboutClickableInfoRow(
                        label = stringResource(Res.string.about_version_label),
                        value = appVersion,
                        onClick = {
                            tapCount++
                            if (tapCount >= 5 && !showDevInfo) {
                                showDevInfo = true
                                tapCount = 0
                            }
                        }
                    )
                    AnimatedVisibility(visible = showDevInfo && appUserId.isNotBlank()) {
                        Column {
                            AboutRowDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(appUserId))
                                        copiedToClipboard = true
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(Res.string.about_revenuecat_id_label),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = appCTextPrimary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (copiedToClipboard) "¡Copiado!" else appUserId,
                                        fontSize = 11.sp,
                                        color = if (copiedToClipboard) appCCyanAccent else appCTextTertiary,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Información de desarrollo ─────────────────────────────────────
            item {
                N3toLabel(text = stringResource(Res.string.about_section_dev))
                Spacer(Modifier.height(8.dp))
                AboutGroupCard {
                    AboutInfoRow(
                        label = stringResource(Res.string.about_developer_label),
                        value = stringResource(Res.string.about_developer_value)
                    )
                    AboutRowDivider()
                    AboutInfoRow(
                        label = stringResource(Res.string.about_license_label),
                        value = stringResource(Res.string.about_license_value)
                    )
                    AboutRowDivider()
                    AboutClickableInfoRow(
                        label = stringResource(Res.string.about_contact_label),
                        value = if (copiedEmail) "Copiado" else "apps@avifer.dev",
                        onClick = {
                            clipboardManager.setText(AnnotatedString("apps@avifer.dev"))
                            copiedEmail = true
                        }
                    )
                }
            }

            // ── Valorar y compartir la app ─────────────────────────────────────
            item {
                N3toLabel(text = stringResource(Res.string.about_section_rate))
                Spacer(Modifier.height(8.dp))
                AboutGroupCard {
                    AboutNavigableRow(
                        icon = Icons.Outlined.Star,
                        label = stringResource(Res.string.about_rate_app_label),
                        onClick = onOpenStore
                    )
                    AboutRowDivider()
                    AboutNavigableRow(
                        icon = Icons.Outlined.Share,
                        label = stringResource(Res.string.about_share_app_label),
                        onClick = onShareApp
                    )
                }
            }

            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

// ─── HEADER — glassmorphism lite hero ────────────────────────────────────────────
@Composable
private fun AboutHeaderSection() {
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    val appCNavySelected = MaterialTheme.appColors.navySelected
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            .padding(horizontal = 22.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InitialsAvatar(
            text = "N3",
            bgColor = appCNavySelected,
            size = 64.dp,
            textSize = 22
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.about_app_name),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = appCTextPrimary,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.about_tagline),
            fontSize = 13.sp,
            color = appCTextTertiary
        )
    }
}

// ─── GROUP CARD — flat + appCNavyBorder ─────────────────────────────────────────────
@Composable
private fun AboutGroupCard(content: @Composable ColumnScope.() -> Unit) {
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(0.5.dp, appCNavyBorder, RoundedCornerShape(11.dp)),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = heroCardBg1),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(content = content)
    }
}

// ─── ROW DIVIDER ────────────────────────────────────────────────────────────────
@Composable
private fun AboutRowDivider() {
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = appCNavyBorder,
        thickness = 0.5.dp
    )
}

// ─── INFO ROW (solo texto, sin interactividad) ─────────────────────────────────
@Composable
private fun AboutInfoRow(label: String, value: String) {
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val appCTextPrimary = MaterialTheme.appColors.textPrimary
        val appCTextTertiary = MaterialTheme.appColors.textTertiary
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = appCTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = appCTextTertiary
        )
    }
}

// ─── CLICKABLE INFO ROW ────────────────────────────────────────────────────────
@Composable
private fun AboutClickableInfoRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = appCTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = if (value == "Copiado") appCCyanAccent else appCTextTertiary
        )
    }
}

// ─── NAVIGABLE ROW (con icono appCCyanAccent y flecha) ──────────────────────────────
@Composable
private fun AboutNavigableRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = appCCyanAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = appCTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = appCTextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── PREVIEW ────────────────────────────────────────────────────────────────────
@Preview
@Composable
private fun AboutContentPreview() {
    N3toTheme {
        AboutContent(
            onBack = {},
            onOpenStore = {},
            onShareApp = {},
            appVersion = "1.2.3",
            appUserId = "ECBD1234ABCD5678"
        )
    }
}
