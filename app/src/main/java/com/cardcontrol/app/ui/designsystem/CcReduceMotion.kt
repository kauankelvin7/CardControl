package com.cardcontrol.app.ui.designsystem

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * CompositionLocal que indica se o usuário ativou "Remover animações" / Movimento Reduzido no Android (B2.6).
 */
val LocalCcReduceMotion = staticCompositionLocalOf { false }

/**
 * Lê em tempo real a escala de duração de animações do sistema (D2.2).
 * Quando igual a 0f, ativa o modo de movimento reduzido (ex: fade em vez de flip 3D).
 */
@Composable
fun rememberReduceMotion(): State<Boolean> {
    val context = LocalContext.current
    val resolver = context.contentResolver

    fun isReduced(): Boolean {
        return try {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        } catch (_: Exception) {
            false
        }
    }

    val state = remember { mutableStateOf(isReduced()) }

    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                state.value = isReduced()
            }
        }
        try {
            resolver.registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
                false,
                observer
            )
        } catch (_: Exception) {
            // Caso ocorra falha de registro por permissão restrita, mantém o valor inicial estável
        }

        onDispose {
            try {
                resolver.unregisterContentObserver(observer)
            } catch (_: Exception) {
            }
        }
    }

    return state
}
