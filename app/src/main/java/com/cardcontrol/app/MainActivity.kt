package com.cardcontrol.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cardcontrol.app.ui.MainScreen
import com.cardcontrol.app.ui.MainViewModel
import com.cardcontrol.app.ui.theme.CardControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CardControlTheme {
                val vm: MainViewModel = viewModel()
                MainScreen(
                    viewModel = vm,
                    onOpenNotificationSettings = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                )
            }
        }
    }
}
