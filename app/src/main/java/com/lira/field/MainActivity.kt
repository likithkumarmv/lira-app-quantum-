package com.lira.field

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.lira.field.ui.navigation.LiraNavGraph
import com.lira.field.ui.theme.LiraTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sessionRepo = LiraApp.instance.sessionRepository
            val themeMode by sessionRepo.themeMode.collectAsState()
            val language by sessionRepo.language.collectAsState()
            val context = LocalContext.current

            val localizedContext = remember(language) {
                if (language == "system" || language.isBlank()) {
                    context
                } else {
                    val locale = Locale(language)
                    Locale.setDefault(locale)
                    val config = Configuration(context.resources.configuration)
                    config.setLocale(locale)
                    context.createConfigurationContext(config)
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                LiraTheme(themeMode = themeMode) {
                    LiraNavGraph()
                }
            }
        }
    }
}
