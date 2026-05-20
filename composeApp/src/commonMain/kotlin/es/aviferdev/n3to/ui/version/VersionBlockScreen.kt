package es.aviferdev.n3to.ui.version

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

import es.aviferdev.n3to.ui.theme.N3toTheme

import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pantalla de bloqueo forzoso (hard block).
 *
 * Se muestra a pantalla completa cuando la versión instalada de la app
 * es inferior a la versión mínima requerida. El usuario no puede salir
 * de esta pantalla hasta que actualice la aplicación desde la store.
 *
 * Los textos (título, mensaje, botón) provienen de Firebase Remote Config
 * y se inyectan como parámetros, permitiendo su configuración remota sin
 * necesidad de actualizar la app.
 *
 * @param title          Título principal de la pantalla (desde Firebase RC).
 * @param message        Mensaje explicativo (desde Firebase RC).
 * @param buttonText     Texto del botón de acción (desde Firebase RC).
 * @param currentVersion Versión actual instalada (para mostrar al usuario).
 * @param minVersion     Versión mínima requerida (para mostrar al usuario).
 * @param onOpenStore    Lambda para abrir Play Store / App Store.
 */
@Composable
fun VersionBlockScreen(
    title: String,
    message: String,
    buttonText: String,
    currentVersion: String,
    minVersion: String,
    onOpenStore: () -> Unit
) {
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    val appCNavyDeep = MaterialTheme.appColors.navyDeep
    val appCNavySurface = MaterialTheme.appColors.navySurface
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appCNavyDeep),
        contentAlignment = Alignment.Center
    ) {
        // Orb decorativo cian (esquina superior derecha)
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(appCCyanGlow.copy(alpha = 0.10f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        val cornerRadius = size.width * 0.22f
                        drawRoundRect(
                            color = appCNavySurface,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                        )
                        drawRoundRect(
                            color = appCNavyBorder,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SystemUpdate,
                    contentDescription = null,
                    tint = appCCyanAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Título (configurable desde Firebase)
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = appCTextPrimary,
                letterSpacing = (-0.3).sp
            )

            Spacer(Modifier.height(12.dp))

            // Subtítulo (configurable desde Firebase)
            Text(
                text = message,
                fontSize = 14.sp,
                color = appCTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Versión mínima: $minVersion · Tu versión: $currentVersion",
                fontSize = 12.sp,
                color = appCTextTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Botón de acción (texto configurable desde Firebase)
            Button(
                onClick = onOpenStore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appCCyanAccent
                )
            ) {
                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appCNavyDeep
                )
            }
        }
    }
}

@Preview
@Composable
private fun VersionBlockScreenPreview() {
    N3toTheme {
        VersionBlockScreen(
            title = "Actualización requerida",
            message = "Debes actualizar N3to para continuar utilizando la aplicación.",
            buttonText = "Actualizar",
            currentVersion = "1.1.0",
            minVersion = "1.2.0",
            onOpenStore = {}
        )
    }
}
