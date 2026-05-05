package es.aviferdev.trackfolio.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.BiometricResult
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.koinInject

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val authenticator: BiometricAuthenticator = koinInject()
    var errorMessage    by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    // Lanza autenticación automáticamente al aparecer la pantalla
    LaunchedEffect(Unit) {
        isAuthenticating = true
        authenticator.authenticate(
            title    = "Desbloquear Trackfolio",
            subtitle = "Usa tu huella, Face ID o PIN"
        ) { result ->
            isAuthenticating = false
            when (result) {
                is BiometricResult.Success      -> onUnlocked()
                is BiometricResult.UserCancelled -> errorMessage = null
                is BiometricResult.NotAvailable  -> errorMessage = "Biometría no disponible en este dispositivo"
                is BiometricResult.Error         -> errorMessage = result.message
            }
        }
    }

    Box(
        modifier         = Modifier.fillMaxSize().background(PrimaryDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 40.dp)
        ) {
            Box(
                modifier        = Modifier.size(80.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔒", fontSize = 36.sp)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Trackfolio",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "La app está bloqueada.\nAutentícate para continuar.",
                fontSize  = 14.sp,
                color     = Color.White.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )

            errorMessage?.let { msg ->
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(msg, fontSize = 13.sp, color = Color(0xFFEF9A9A), textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    isAuthenticating = true
                    errorMessage = null
                    authenticator.authenticate(
                        title    = "Desbloquear Trackfolio",
                        subtitle = "Usa tu huella, Face ID o PIN"
                    ) { result ->
                        isAuthenticating = false
                        when (result) {
                            is BiometricResult.Success       -> onUnlocked()
                            is BiometricResult.UserCancelled -> Unit
                            is BiometricResult.NotAvailable  -> errorMessage = "Biometría no disponible"
                            is BiometricResult.Error         -> errorMessage = result.message
                        }
                    }
                },
                enabled  = !isAuthenticating,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.38f)
                )
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryDark, strokeWidth = 2.dp)
                } else {
                    Text("Desbloquear", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryDark)
                }
            }
        }
    }
}
