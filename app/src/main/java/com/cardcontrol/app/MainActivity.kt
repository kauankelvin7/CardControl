package com.cardcontrol.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cardcontrol.app.ui.MainScreen
import com.cardcontrol.app.ui.MainViewModel
import com.cardcontrol.app.ui.designsystem.AppearancePrefs
import com.cardcontrol.app.ui.designsystem.ThemeMode
import com.cardcontrol.app.ui.theme.CardControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }
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
