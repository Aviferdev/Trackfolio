package es.aviferdev.trackfolio

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import es.aviferdev.trackfolio.di.androidModule
import es.aviferdev.trackfolio.di.initKoin
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.DatabaseBackupManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class TrackfolioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(platformModule = androidModule) {
            androidContext(this@TrackfolioApp)
        }
    }
}

class MainActivity : FragmentActivity() {

    private val backupManager: DatabaseBackupManager by inject()
    private val biometricAuthenticator: BiometricAuthenticator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Registrar esta Activity en los managers que la requieren
        // (BiometricPrompt y el chooser/picker de backup necesitan una Activity).
        backupManager.bindActivity(this)
        biometricAuthenticator.bindActivity(this)
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        backupManager.unbindActivity()
        biometricAuthenticator.unbindActivity()
        super.onDestroy()
    }
}
