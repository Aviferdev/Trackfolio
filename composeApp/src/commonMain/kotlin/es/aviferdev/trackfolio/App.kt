package es.aviferdev.trackfolio

import androidx.compose.runtime.*
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

    // Inicializar con el estado real (persiste entre sesiones via AppSettings)
    var isLocked by remember {
        appLockManager.onAppStart()   // bloquea al arrancar si biometría activa
        mutableStateOf(appLockManager.isLocked)
    }

    // Inicializar BD
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            databaseInitializer.initializeIfNeeded()
        }
    }

    // Bloquear al pasar a background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    appLockManager.onAppBackground()
                    isLocked = appLockManager.isLocked
                }
                Lifecycle.Event.ON_START -> {
                    // Sincronizar por si cambió desde otro hilo
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
