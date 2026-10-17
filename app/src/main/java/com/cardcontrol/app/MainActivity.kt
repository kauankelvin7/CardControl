package com.cardcontrol.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cardcontrol.app.ui.MainScreen
import com.cardcontrol.app.ui.MainViewModel
import com.cardcontrol.app.ui.designsystem.AppearancePrefs
import com.cardcontrol.app.ui.designsystem.ThemeMode
import com.cardcontrol.app.ui.designsystem.resolveDark
import com.cardcontrol.app.ui.theme.CardControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializa a janela no modo edge-to-edge sem qualquer faixa preta
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }
            val systemDark = isSystemInDarkTheme()
            val dark = resolveDark(themeMode, systemDark)

            // Sincroniza barras do sistema com o tema ativo (ícones claros no escuro, escuros no claro)
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                        detectDarkMode = { dark }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                        detectDarkMode = { dark }
                    )
                )
                onDispose {}
            }

            CardControlTheme(prefs = AppearancePrefs(mode = themeMode)) {
                val vm: MainViewModel = viewModel()
                MainScreen(
                    viewModel = vm,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it },
                    onOpenNotificationSettings = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                )
            }
        }
    }
}
