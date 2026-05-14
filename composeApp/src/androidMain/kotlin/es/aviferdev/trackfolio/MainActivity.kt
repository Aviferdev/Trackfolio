package es.aviferdev.trackfolio

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import es.aviferdev.trackfolio.di.androidModule
import es.aviferdev.trackfolio.di.initKoin
import es.aviferdev.trackfolio.domain.pdf.PdfReportGenerator
import es.aviferdev.trackfolio.core.security.BiometricAuthenticator
import es.aviferdev.trackfolio.core.security.DatabaseBackupManager
import es.aviferdev.trackfolio.core.security.setAppContextForPendingImport
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class TrackfolioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setAppContextForPendingImport(this)
        initKoin(platformModule = androidModule) {
            androidContext(this@TrackfolioApp)
        }
    }
}

class MainActivity : FragmentActivity() {

    private val backupManager: DatabaseBackupManager by inject()
    private val biometricAuthenticator: BiometricAuthenticator by inject()
    private val pdfReportGenerator: PdfReportGenerator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        backupManager.bindActivity(this)
        biometricAuthenticator.bindActivity(this)
        pdfReportGenerator.bindActivity(this)
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        backupManager.unbindActivity()
        biometricAuthenticator.unbindActivity()
        pdfReportGenerator.unbindActivity()
        super.onDestroy()
    }
}
