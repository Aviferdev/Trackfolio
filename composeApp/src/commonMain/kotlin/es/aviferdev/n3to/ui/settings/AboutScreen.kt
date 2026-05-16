package es.aviferdev.n3to.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.N3toLabel
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

// ─── WRAPPER ────────────────────────────────────────────────────────────────────
@Composable
fun AboutScreen(
    onBack: () -> Unit = {},
    appVersion: String = koinInject(named("appVersion"))
) {
    AboutContent(
        onBack = onBack,
        appVersion = appVersion
    )
}

// ─── CONTENT ────────────────────────────────────────────────────────────────────
@Composable
fun AboutContent(
    onBack: () -> Unit = {},
    appVersion: String = "1.0.0",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(BackgroundGray)) {
        TopBarApp(title = "Acerca de", navigateBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Cabecera de la app ────────────────────────────────────────────
            item {
                AboutHeaderSection()
            }

            // ── Información de la aplicación ──────────────────────────────────
            item {
                N3toLabel(text = "Aplicación")
                Spacer(Modifier.height(8.dp))
                AboutGroupCard {
                    AboutInfoRow(label = "Versión", value = appVersion)
                }
            }

            // ── Información de desarrollo (placeholder) ──────────────────────
            item {
                N3toLabel(text = "Desarrollo")
                Spacer(Modifier.height(8.dp))
                AboutGroupCard {
                    AboutInfoRow(label = "Desarrollador", value = "—")
                    AboutRowDivider()
                    AboutInfoRow(label = "Licencia", value = "—")
                    AboutRowDivider()
                    AboutInfoRow(label = "Contacto", value = "—")
                }
            }

            // ── Espaciado inferior ────────────────────────────────────────────
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

// ─── HEADER ─────────────────────────────────────────────────────────────────────
@Composable
private fun AboutHeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "N3to",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Controla tus finanzas personales",
            fontSize = 13.sp,
            color = TextTertiary
        )
    }
}

// ─── GROUP CARD ─────────────────────────────────────────────────────────────────
@Composable
private fun AboutGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(content = content)
    }
}

// ─── ROW DIVIDER ────────────────────────────────────────────────────────────────
@Composable
private fun AboutRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = BorderGray,
        thickness = 0.5.dp
    )
}

// ─── INFO ROW (solo texto, sin interactividad) ─────────────────────────────────
@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextTertiary
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
            appVersion = "1.2.3"
        )
    }
}
