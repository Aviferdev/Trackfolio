package es.aviferdev.n3to

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.aviferdev.n3to.core.AppConfig
import es.aviferdev.n3to.core.VersionManager
import es.aviferdev.n3to.core.premium.PremiumManager
import es.aviferdev.n3to.core.security.AppLockManager
import es.aviferdev.n3to.core.security.BalanceVisibilityManager
import es.aviferdev.n3to.data.database.DatabaseInitializer
import es.aviferdev.n3to.domain.usecase.consent.GetConsentUseCase
import es.aviferdev.n3to.domain.usecase.consent.HasUserDecidedUseCase
import es.aviferdev.n3to.platform.AnalyticsTracker
import es.aviferdev.n3to.platform.CrashlyticsTracker
import es.aviferdev.n3to.ui.consent.ConsentScreen
import es.aviferdev.n3to.ui.navigation.N3toNavHost
import es.aviferdev.n3to.ui.security.LockScreen
import es.aviferdev.n3to.ui.splash.SplashScreen
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.version.VersionBlockScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun App() {
    val databaseInitializer = koinInject<DatabaseInitializer>()
    val appLockManager = koinInject<AppLockManager>()
    val balanceVisibility = koinInject<BalanceVisibilityManager>()
    val hasUserDecided = koinInject<HasUserDecidedUseCase>()
    val getConsent = koinInject<GetConsentUseCase>()
    val analyticsTracker = koinInject<AnalyticsTracker>()
    val crashlyticsTracker = koinInject<CrashlyticsTracker>()
    val versionManager = koinInject<VersionManager>()
    val openStore: () -> Unit = koinInject(named("openStore"))
    val currentVersion: String = koinInject(named("appVersion"))
    val premiumManager = koinInject<PremiumManager>()

    var needsConsent by remember { mutableStateOf<Boolean?>(null) }

    var splashFinished by remember { mutableStateOf(false) }

    var isLocked by remember {
        appLockManager.onAppStart()
        mutableStateOf(appLockManager.isLocked)
    }

    val balancesHidden by balanceVisibility.balancesHidden.collectAsState()
    val versionStatus by versionManager.status.collectAsState()
    val premiumStatus by premiumManager.status.collectAsState()

    // Control de tema: solo usuarios Premium pueden usar tema claro.
    // Gratuito → siempre oscuro. Premium → puede alternar (por ahora siempre oscuro,
    // pendiente de implementar selector en Ajustes).
    val isDarkTheme = true
    premiumStatus.isPremium

    // Inicializar base de datos, cargar estado de consentimiento y PremiumManager
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            databaseInitializer.initializeIfNeeded()
        }
        val decided = hasUserDecided()
        needsConsent = !decided
        if (decided) {
            val prefs = getConsent()
            analyticsTracker.setEnabled(prefs.analytics)
            crashlyticsTracker.setCrashReportingEnabled(prefs.crashReporting)
        }
        // Inicializar PremiumManager (RevenueCat SDK se activará al actualizar Kotlin)
        premiumManager.initialize(AppConfig.revenueCatApiKey)
    }

    // Lanzar el chequeo de versión una vez que consent está resuelto
    val appReady = !(needsConsent ?: true)
    LaunchedEffect(appReady) {
        if (appReady) {
            versionManager.checkVersion()
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

    N3toTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalBalanceHidden provides balancesHidden) {
            when {
                // Paso 0: Splash screen con el icono de la app
                !splashFinished -> {
                    SplashScreen(
                        onSplashFinished = { splashFinished = true }
                    )
                }

                // Paso 1: Consentimiento (GDPR)
                needsConsent == true -> {
                    ConsentScreen(
                        onConsentSaved = { needsConsent = false }
                    )
                }

                // Paso 2: Estado de carga mientras se chequea la versión
                versionStatus is VersionManager.Status.Checking -> {
                    Box(
                        modifier = Modifier.fillMaxSize().background(BackgroundGray),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }

                // Paso 3: Bloqueo por versión (hard block)
                versionStatus is VersionManager.Status.UpdateRequired -> {
                    val info = (versionStatus as VersionManager.Status.UpdateRequired).info
                    VersionBlockScreen(
                        currentVersion = currentVersion,
                        minVersion = info.minVersion,
                        onOpenStore = openStore
                    )
                }

                // Paso 4: Bloqueo de seguridad
                isLocked -> {
                    LockScreen(
                        onUnlocked = {
                            appLockManager.onUnlocked()
                            isLocked = false
                        }
                    )
                }

                // Paso 5: App principal
                else -> {
                    N3toNavHost()
                }
            }
        }
    }
}
