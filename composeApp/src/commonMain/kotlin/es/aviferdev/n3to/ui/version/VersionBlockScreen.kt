package es.aviferdev.n3to.ui.version

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.PrimaryDark
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pantalla de bloqueo forzoso (hard block).
 *
 * Se muestra a pantalla completa cuando la versión instalada de la app
 * es inferior a la versión mínima requerida. El usuario no puede salir
 * de esta pantalla hasta que actualice la aplicación desde la store.
 *
 * Diseño inspirado en [LockScreen]: fondo PrimaryDark, icono centrado,
 * título, subtítulo informativo y botón de acción para abrir la store.
 *
 * @param currentVersion Versión actual instalada (para mostrar al usuario).
 * @param minVersion     Versión mínima requerida (para mostrar al usuario).
 * @param onOpenStore    Lambda para abrir Play Store / App Store.
 */
@Composable
fun VersionBlockScreen(
    currentVersion: String,
    minVersion: String,
    onOpenStore: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SystemUpdate,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Título
            Text(
                text = "Actualización requerida",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            // Subtítulo
            Text(
                text = "Debes actualizar N3to para continuar utilizando la aplicación.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Versión mínima: $minVersion · Tu versión: $currentVersion",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.50f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Botón de actualizar
            Button(
                onClick = onOpenStore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Actualizar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryDark
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
            currentVersion = "1.1.0",
            minVersion = "1.2.0",
            onOpenStore = {}
        )
    }
}
