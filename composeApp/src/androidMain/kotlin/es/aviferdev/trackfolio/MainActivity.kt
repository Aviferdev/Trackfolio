package es.aviferdev.trackfolio

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import es.aviferdev.trackfolio.di.androidModule
import es.aviferdev.trackfolio.di.initKoin
import org.koin.android.ext.koin.androidContext

class TrackfolioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(platformModule = androidModule) {
            androidContext(this@TrackfolioApp)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
