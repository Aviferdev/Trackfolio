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
import es.aviferdev.n3to.core.VersionManager
import es.aviferdev.n3to.core.premium.PremiumManager
import es.aviferdev.n3to.core.security.AppLockManager
import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.core.security.BalanceVisibilityManager
import es.aviferdev.n3to.data.database.DatabaseInitializer
import es.aviferdev.n3to.domain.usecase.consent.GetConsentUseCase
import es.aviferdev.n3to.domain.usecase.consent.HasUserDecidedUseCase
import es.aviferdev.n3to.platform.AnalyticsTracker
import es.aviferdev.n3to.platform.CrashlyticsTracker
import es.aviferdev.n3to.ui.consent.ConsentScreen
import es.aviferdev.n3to.ui.navigation.N3toNavHost
import es.aviferdev.n3to.ui.onboarding.OnboardingScreen
import es.aviferdev.n3to.ui.security.LockScreen
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.version.VersionBlockScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

private const val KEY_ONBOARDING_DONE = "onboarding_done"

@Composable
fun App() {
    val databaseInitializer = koinInject<DatabaseInitializer>()
    val appLockManager = koinInject<AppLockManager>()
    val balanceVisibility = koinInject<BalanceVisibilityManager>()
    val settings = koinInject<AppSettings>()
    val hasUserDecided = koinInject<HasUserDecidedUseCase>()
    val getConsent = koinInject<GetConsentUseCase>()
    val analyticsTracker = koinInject<AnalyticsTracker>()
    val crashlyticsTracker = koinInject<CrashlyticsTracker>()
    val versionManager = koinInject<VersionManager>()
    val openStore: () -> Unit = koinInject(named("openStore"))
    val currentVersion: String = koinInject(named("appVersion"))
    koinInject<PremiumManager>()

    var onboardingDone by remember {
        mutableStateOf(settings.getBool(KEY_ONBOARDING_DONE, false))
    }

    var needsConsent by remember { mutableStateOf<Boolean?>(null) }

    var isLocked by remember {
        appLockManager.onAppStart()
        mutableStateOf(appLockManager.isLocked)
    }

    val balancesHidden by balanceVisibility.balancesHidden.collectAsState()
    val versionStatus by versionManager.status.collectAsState()

    // Inicializar base de datos y cargar estado de consentimiento
    LaunchedEffect(onboardingDone) {
        withContext(Dispatchers.Default) {
            databaseInitializer.initializeIfNeeded()
        }
        if (onboardingDone) {
            val decided = hasUserDecided()
            needsConsent = !decided
            if (decided) {
                val prefs = getConsent()
                analyticsTracker.setEnabled(prefs.analytics)
                crashlyticsTracker.setCrashReportingEnabled(prefs.crashReporting)
            }
        }
    }

    // Lanzar el chequeo de versión una vez que la app está lista (onboarding + consent resueltos)
    val appReady = onboardingDone && !(needsConsent ?: true)
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

    N3toTheme {
        CompositionLocalProvider(LocalBalanceHidden provides balancesHidden) {
            when {
                // Paso 1: Onboarding (slides de bienvenida)
                !onboardingDone -> {
                    OnboardingScreen(
                        onComplete = {
                            settings.putBool(KEY_ONBOARDING_DONE, true)
                            onboardingDone = true
                        }
                    )
                }

                // Paso 2: Consentimiento (GDPR)
                needsConsent == true -> {
                    ConsentScreen(
                        onConsentSaved = { needsConsent = false }
                    )
                }

                // Paso 3: Estado de carga mientras se chequea la versión
                versionStatus is VersionManager.Status.Checking -> {
                    Box(
                        modifier = Modifier.fillMaxSize().background(BackgroundGray),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }

                // Paso 4: Bloqueo por versión (hard block)
                versionStatus is VersionManager.Status.UpdateRequired -> {
                    val info = (versionStatus as VersionManager.Status.UpdateRequired).info
                    VersionBlockScreen(
                        currentVersion = currentVersion,
                        minVersion = info.minVersion,
                        onOpenStore = openStore
                    )
                }

                // Paso 5: Bloqueo de seguridad
                isLocked -> {
                    LockScreen(
                        onUnlocked = {
                            appLockManager.onUnlocked()
                            isLocked = false
                        }
                    )
                }

                // Paso 6: App principal
                else -> {
                    N3toNavHost()
                }
            }
        }
    }
}


