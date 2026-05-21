package es.aviferdev.n3to.ui.security

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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.ErrorSoft
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.security_biometric_not_available
import n3to.composeapp.generated.resources.security_unlock_hint
import n3to.composeapp.generated.resources.security_unlock_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    val appCNavyDeep = MaterialTheme.appColors.navyDeep
    val appCNavySurface = MaterialTheme.appColors.navySurface
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    val viewModel: LockViewModel = koinViewModel()
    val lockState by viewModel.state.collectAsState()
    val unlockTitle = stringResource(Res.string.security_unlock_title)
    val unlockHint = stringResource(Res.string.security_unlock_hint)
    val bioNotAvailable = stringResource(Res.string.security_biometric_not_available)

    LaunchedEffect(Unit) {
        viewModel.authenticate(unlockTitle, unlockHint, bioNotAvailable)
    }
    LaunchedEffect(lockState) {
        if (lockState is LockUiState.Unlocked) onUnlocked()
    }

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
            // Icono de bloqueo
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
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = appCCyanAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "N3to",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = appCTextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "La app está bloqueada.\nAutentícate para continuar.",
                fontSize = 14.sp,
                color = appCTextSecondary,
                textAlign = TextAlign.Center
            )

            (lockState as? LockUiState.Error)?.message?.let { msg ->
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(appCNavySurface)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        msg,
                        fontSize = 13.sp,
                        color = ErrorSoft,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            val isAuthenticating = lockState is LockUiState.Authenticating
            Button(
                onClick = { viewModel.authenticate(unlockTitle, unlockHint, bioNotAvailable) },
                enabled = !isAuthenticating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appCCyanAccent,
                    disabledContainerColor = appCCyanAccent.copy(alpha = 0.38f)
                )
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = appCNavyDeep,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Desbloquear",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appCNavyDeep
                    )
                }
            }
        }
    }
}
