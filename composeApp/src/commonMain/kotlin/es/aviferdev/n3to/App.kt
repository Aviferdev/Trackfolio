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
import androidx.compose.runtime.rememberCoroutineScope
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
import es.aviferdev.n3to.core.security.ThemeManager
import es.aviferdev.n3to.data.database.DatabaseInitializer
import es.aviferdev.n3to.domain.usecase.consent.GetConsentUseCase
import es.aviferdev.n3to.domain.usecase.consent.HasUserDecidedUseCase
import es.aviferdev.n3to.domain.usecase.onboarding.IsOnboardingCompletedUseCase
import es.aviferdev.n3to.domain.usecase.onboarding.ResetOnboardingUseCase
import es.aviferdev.n3to.platform.AnalyticsTracker
import es.aviferdev.n3to.platform.CrashlyticsTracker
import es.aviferdev.n3to.ui.consent.ConsentScreen
import es.aviferdev.n3to.ui.navigation.N3toNavHost
import es.aviferdev.n3to.ui.onboarding.OnboardingScreen
import es.aviferdev.n3to.ui.security.LockScreen
import es.aviferdev.n3to.ui.splash.SplashScreen
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.version.VersionBlockScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    val isOnboardingCompleted = koinInject<IsOnboardingCompletedUseCase>()
    val resetOnboarding = koinInject<ResetOnboardingUseCase>()

    var needsConsent by remember { mutableStateOf<Boolean?>(null) }
    var needsOnboarding by remember { mutableStateOf(true) }

    var splashFinished by remember { mutableStateOf(false) }

    var isLocked by remember {
        appLockManager.onAppStart()
        mutableStateOf(appLockManager.isLocked)
    }

    val balancesHidden by balanceVisibility.balancesHidden.collectAsState()
    val versionStatus by versionManager.status.collectAsState()
    val premiumStatus by premiumManager.status.collectAsState()

    val scope = rememberCoroutineScope()

    val themeManager = koinInject<ThemeManager>()
    val isDarkTheme by themeManager.isDark.collectAsState()

    // Inicializar base de datos, estado de consentimiento y estado de onboarding
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
        needsOnboarding = !isOnboardingCompleted()
        // Version check inmediato (no depende de consentimiento)
        versionManager.checkVersion()
    }

    // Lanzar inicialización de PremiumManager una vez que consent está resuelto
    val appReady = !(needsConsent ?: true)
    LaunchedEffect(appReady) {
        if (appReady) {
            premiumManager.initialize(AppConfig.revenueCatApiKey)
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

                // Paso 1: Chequeo de versión (antes de cualquier contenido)
                versionStatus is VersionManager.Status.Checking -> {
                    Box(
                        modifier = Modifier.fillMaxSize().background(BackgroundGray),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }

                // Paso 2: Bloqueo por versión obsoleta (hard block)
                versionStatus is VersionManager.Status.UpdateRequired -> {
                    val info = (versionStatus as VersionManager.Status.UpdateRequired).info
                    VersionBlockScreen(
                        title = info.blockTitle,
                        message = info.blockMessage,
                        buttonText = info.blockButtonText,
                        currentVersion = currentVersion,
                        minVersion = info.minVersion,
                        onOpenStore = openStore
                    )
                }

                // Paso 3: Bloqueo de seguridad biométrica
                isLocked -> {
                    LockScreen(
                        onUnlocked = {
                            appLockManager.onUnlocked()
                            isLocked = false
                        }
                    )
                }

                // Paso 4: Onboarding (primera vez o tras reset desde Ajustes)
                needsOnboarding -> {
                    OnboardingScreen(
                        onComplete = { needsOnboarding = false }
                    )
                }

                // Paso 5: Consentimiento GDPR (tras ver valor de la app)
                needsConsent == true -> {
                    ConsentScreen(
                        onConsentSaved = { needsConsent = false }
                    )
                }

                // Paso 6: App principal
                else -> {
                    N3toNavHost(
                        onResetOnboarding = {
                            scope.launch {
                                resetOnboarding()
                                needsOnboarding = true
                            }
                        }
                    )
                }
            }
        }
    }
}
