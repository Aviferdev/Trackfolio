package es.aviferdev.n3to

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.aviferdev.n3to.core.VersionManager
import es.aviferdev.n3to.core.security.AppLockManager
import es.aviferdev.n3to.core.security.BalanceVisibilityManager
import es.aviferdev.n3to.core.security.LanguageManager
import es.aviferdev.n3to.core.security.ThemeManager
import es.aviferdev.n3to.core.security.setPlatformLanguage
import es.aviferdev.n3to.data.database.DatabaseInitializer
import es.aviferdev.n3to.domain.model.AppCurrency
import es.aviferdev.n3to.domain.model.toCurrencySymbol
import es.aviferdev.n3to.domain.usecase.account.GetAccountsUseCase
import es.aviferdev.n3to.domain.usecase.asset.AppStartupRefreshUseCase
import es.aviferdev.n3to.domain.usecase.asset.ShouldRefreshTodayUseCase
import es.aviferdev.n3to.domain.usecase.consent.GetConsentUseCase
import es.aviferdev.n3to.domain.usecase.consent.HasUserDecidedUseCase
import es.aviferdev.n3to.domain.usecase.onboarding.IsOnboardingCompletedUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.platform.AnalyticsTracker
import es.aviferdev.n3to.platform.CrashlyticsTracker
import es.aviferdev.n3to.ui.consent.ConsentScreen
import es.aviferdev.n3to.ui.navigation.N3toNavHost
import es.aviferdev.n3to.ui.onboarding.OnboardingScreen
import es.aviferdev.n3to.ui.security.LockScreen
import es.aviferdev.n3to.ui.splash.SplashScreen
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import es.aviferdev.n3to.ui.theme.LocalFiscalAmountsHidden
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.version.VersionBlockScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun App() {
    val koin = getKoin()
    val appLockManager = koinInject<AppLockManager>()
    val balanceVisibility = koinInject<BalanceVisibilityManager>()
    val analyticsTracker = koinInject<AnalyticsTracker>()
    val crashlyticsTracker = koinInject<CrashlyticsTracker>()
    val versionManager = koinInject<VersionManager>()
    val openStore: () -> Unit = koinInject(named("openStore"))
    val currentVersion: String = koinInject(named("appVersion"))

    val themeManager = koinInject<ThemeManager>()
    val isDarkTheme by themeManager.isDark.collectAsState()

    val languageManager = koinInject<LanguageManager>()
    val languageCode by languageManager.languageCode.collectAsState()

    // ─── Estado de inicialización ─────────────────────────────────────────────
    var dbReady by remember { mutableStateOf(false) }
    var needsConsent by remember { mutableStateOf<Boolean?>(null) }
    var needsOnboarding by remember { mutableStateOf(true) }
    var splashFinished by remember { mutableStateOf(false) }
    var isLocked by remember {
        appLockManager.onAppStart()
        mutableStateOf(appLockManager.isLocked)
    }
    val balancesHidden by balanceVisibility.balancesHidden.collectAsState()
    val versionStatus by versionManager.status.collectAsState()

    // ─── Inicialización pesada en background ──────────────────────────────────
    LaunchedEffect(Unit) {
        // 1. Crear BD y seed inicial (background)
        withContext(Dispatchers.Default) {
            koin.get<DatabaseInitializer>().initializeIfNeeded()
        }

        // 2. Consentimiento y onboarding (dependen de BD, ya disponible)
        val decided = koin.get<HasUserDecidedUseCase>().invoke()
        needsConsent = !decided
        if (decided) {
            val prefs = koin.get<GetConsentUseCase>().invoke()
            analyticsTracker.setEnabled(prefs.analytics)
            crashlyticsTracker.setCrashReportingEnabled(prefs.crashReporting)
        }
        needsOnboarding = !koin.get<IsOnboardingCompletedUseCase>().invoke()
        versionManager.checkVersion()

        // 3. Marcar BD como lista → se renderiza el resto de la UI
        dbReady = true
    }

    val appReady = dbReady && !(needsConsent ?: true)

    // ─── Refresco diario de precios (solo cuando appReady) ────────────────────
    var refreshMessage by remember { mutableStateOf<String?>(null) }
    var refreshDone by remember { mutableStateOf(false) }

    LaunchedEffect(appReady, refreshDone) {
        if (appReady && !refreshDone) {
            val accountId = koin.get<AccountSession>().selectedAccountId.value
            if (accountId == null) {
                println("[PriceRefresh] ⏭️ Sin cuenta seleccionada, se omite refresco")
            } else if (!koin.get<ShouldRefreshTodayUseCase>().invoke()) {
                println("[PriceRefresh] ⏭️ Ya se refrescó hoy, se omite")
            } else {
                println("[PriceRefresh] 🚀 Lanzando refresco diario...")
                withContext(Dispatchers.Default) {
                    val (_, priceResult) = koin.get<AppStartupRefreshUseCase>().invoke(accountId)
                    if (priceResult.hasUpdates || priceResult.hasNotFound || priceResult.hasFailures) {
                        refreshMessage = priceResult.summary
                    }
                }
            }
            refreshDone = true
        }
    }

    // ─── Lifecycle observer ──────────────────────────────────────────────────
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

    // ─── Sincronizar locale ──────────────────────────────────────────────────
    LaunchedEffect(languageCode) {
        setPlatformLanguage(languageCode)
    }

    // ─── Árbol de composición ─────────────────────────────────────────────────
    key(languageCode) {
        N3toTheme(darkTheme = isDarkTheme) {
            val currencySymbol = if (dbReady) {
                val selectedAccountId = koin.get<AccountSession>().selectedAccountId.value
                val allAccounts = koin.get<GetAccountsUseCase>().invoke().collectAsState(emptyList()).value
                allAccounts.find { it.id == selectedAccountId }?.currency?.toCurrencySymbol()
                    ?: AppCurrency.EUR.symbol
            } else {
                AppCurrency.EUR.symbol
            }

            CompositionLocalProvider(
                LocalBalanceHidden provides balancesHidden,
                LocalFiscalAmountsHidden provides balancesHidden,
                LocalCurrencySymbol provides currencySymbol
            ) {
                when {
                    // Paso 0: Splash screen
                    !splashFinished -> {
                        SplashScreen(
                            onSplashFinished = { splashFinished = true }
                        )
                    }

                    // Paso 1: Esperar a que la BD esté lista
                    !dbReady -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(MaterialTheme.appColors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryDark)
                        }
                    }

                    // Paso 2: Chequeo de versión
                    versionStatus is VersionManager.Status.Checking -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(MaterialTheme.appColors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryDark)
                        }
                    }

                    // Paso 3: Bloqueo por versión obsoleta
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

                    // Paso 4: Bloqueo de seguridad biométrica
                    isLocked -> {
                        LockScreen(
                            onUnlocked = {
                                appLockManager.onUnlocked()
                                isLocked = false
                            }
                        )
                    }

                    // Paso 5: Onboarding
                    needsOnboarding -> {
                        OnboardingScreen(
                            onComplete = { needsOnboarding = false }
                        )
                    }

                    // Paso 6: Consentimiento GDPR
                    needsConsent == true -> {
                        ConsentScreen(
                            onConsentSaved = { needsConsent = false }
                        )
                    }

                    // Paso 7: App principal
                    else -> {
                        val snackbarHostState = remember { SnackbarHostState() }

                        LaunchedEffect(refreshMessage) {
                            refreshMessage?.let { msg ->
                                snackbarHostState.showSnackbar(msg)
                                refreshMessage = null
                            }
                        }

                        Box {
                            N3toNavHost()
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .align(Alignment.TopCenter)
                            ) {
                                SnackbarHost(hostState = snackbarHostState) { data ->
                                    Snackbar(
                                        snackbarData = data,
                                        containerColor = MaterialTheme.appColors.navySurface,
                                        contentColor = MaterialTheme.appColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
