package es.aviferdev.n3to

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import es.aviferdev.n3to.core.security.BiometricAuthenticator
import es.aviferdev.n3to.core.security.DatabaseBackupManager
import es.aviferdev.n3to.core.security.setAppContextForPendingImport
import es.aviferdev.n3to.di.androidModule
import es.aviferdev.n3to.di.initKoin
import es.aviferdev.n3to.domain.pdf.PdfReportGenerator
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class N3toApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setAppContextForPendingImport(this)
        initKoin(platformModule = androidModule) {
            androidContext(this@N3toApp)
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
