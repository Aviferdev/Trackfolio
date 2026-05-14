package es.aviferdev.trackfolio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.aviferdev.trackfolio.data.database.DatabaseInitializer
import es.aviferdev.trackfolio.core.security.AppLockManager
import es.aviferdev.trackfolio.core.security.BalanceVisibilityManager
import es.aviferdev.trackfolio.ui.navigation.TrackfolioNavHost
import es.aviferdev.trackfolio.ui.security.LockScreen
import es.aviferdev.trackfolio.ui.theme.LocalBalanceHidden
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun App() {
    val databaseInitializer = koinInject<DatabaseInitializer>()
    val appLockManager = koinInject<AppLockManager>()
    val balanceVisibility = koinInject<BalanceVisibilityManager>()

    var isLocked by remember {
        appLockManager.onAppStart()
        mutableStateOf(appLockManager.isLocked)
    }

    val balancesHidden by balanceVisibility.balancesHidden.collectAsState()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            databaseInitializer.initializeIfNeeded()
        }
    }

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
        CompositionLocalProvider(LocalBalanceHidden provides balancesHidden) {
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
}
