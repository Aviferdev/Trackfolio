package es.aviferdev.trackfolio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import es.aviferdev.trackfolio.data.database.DatabaseInitializer
import es.aviferdev.trackfolio.ui.navigation.TrackfolioNavHost
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun App() {
    val databaseInitializer = koinInject<DatabaseInitializer>()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            databaseInitializer.initializeIfNeeded()
        }
    }

    TrackfolioTheme {
        TrackfolioNavHost()
    }
}
