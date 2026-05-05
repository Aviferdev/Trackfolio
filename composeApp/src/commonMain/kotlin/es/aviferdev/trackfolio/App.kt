package es.aviferdev.trackfolio

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.aviferdev.trackfolio.data.database.DatabaseInitializer
import es.aviferdev.trackfolio.security.AppLockManager
import es.aviferdev.trackfolio.ui.navigation.TrackfolioNavHost
import es.aviferdev.trackfolio.ui.security.LockScreen
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun App() {
    val databaseInitializer = koinInject<DatabaseInitializer>()
    val appLockManager      = koinInject<AppLockManager>()

    // Estado local del bloqueo, sincronizado con AppLockManager
    var isLocked by remember { mutableStateOf(appLockManager.isLocked) }

    // Inicializar BD al arrancar
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            databaseInitializer.initializeIfNeeded()
        }
    }

    // Observar ciclo de vida para bloquear al pasar a background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    appLockManager.onAppBackground()
                    isLocked = appLockManager.isLocked
                }
                Lifecycle.Event.ON_START -> {
                    isLocked = appLockManager.isLocked
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    TrackfolioTheme {
        if (isLocked) {
            LockScreen(
                onUnlocked = {
                    appLockManager.onUnlocked()
                    isLocked = false
                }
            )
        } else {
            TrackfolioNavHost()
        }
    }
}
