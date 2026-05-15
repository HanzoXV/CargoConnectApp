package com.example.cargoconnectapp.ui.models.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Provides a bound [TruckAnimationService] instance as Compose state.
 *
 * Usage inside any composable:
 * ```
 * val service by rememberTruckAnimationService()
 * ```
 *
 * The returned value is `null` until the service is bound and non-null
 * once the connection is established. The binding is cleaned up automatically
 * when the composable leaves the composition.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun rememberTruckAnimationService(): State<TruckAnimationService?> {
    val context = LocalContext.current
    val serviceState = remember { mutableStateOf<TruckAnimationService?>(null) }

    DisposableEffect(Unit) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                serviceState.value =
                    (binder as TruckAnimationService.LocalBinder).getService()
            }
            override fun onServiceDisconnected(name: ComponentName) {
                serviceState.value = null
            }
        }

        // Bind to the service (it may already be running as a foreground service)
        val intent = Intent(context, TruckAnimationService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            // Un-register the UI callback so we don't leak a lambda into the service
            serviceState.value?.onPositionChanged  = null
            serviceState.value?.onAnimationComplete = null
            context.unbindService(connection)
            serviceState.value = null
        }
    }

    return serviceState
}

/**
 * Starts the [TruckAnimationService] as a foreground service.
 * Call this BEFORE trying to bind, so the service is promoted to foreground
 * immediately and Android won't kill it while we bind.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun Context.startTruckAnimationService() {
    val intent = Intent(this, TruckAnimationService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}