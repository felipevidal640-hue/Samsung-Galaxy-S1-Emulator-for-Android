package com.galaxys1.emulator

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

/**
 * Hosts the emulated Samsung Galaxy S (GT-I9000) inside a modern Android phone.
 *
 * The real system stays out of the way — the whole experience, from the boot
 * logo to TouchWiz 3.0, runs inside the drawn handset on screen.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GalaxyS1EmulatorApp()
        }
    }
}

@Composable
fun GalaxyS1EmulatorApp() {
    val state = remember { EmulatorState() }
    val context = LocalContext.current

    // The hardware Back key of the host phone drives the emulated Back key.
    BackHandler { state.back() }

    // Emulated status-bar clock (1 s tick).
    LaunchedEffect(Unit) {
        while (true) {
            state.nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    // Show the host's real battery level in the emulated status bar.
    LaunchedEffect(Unit) {
        try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (intent != null) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) state.hostBattery = level * 100 / scale
            }
        } catch (e: Exception) {
            state.hostBattery = -1
        }
    }

    // Toast auto-dismiss.
    LaunchedEffect(state.toastSeq) {
        if (state.toast != null) {
            delay(2200)
            state.toast = null
        }
    }

    // Volume HUD auto-dismiss.
    LaunchedEffect(state.volumeHudSeq) {
        if (state.volumeHudVisible) {
            delay(1500)
            state.volumeHudVisible = false
        }
    }

    DeviceShell(
        state = state,
        onPowerClick = { state.powerPress() },
        onPowerLongClick = { state.powerLongPress() },
        onMenuClick = { state.menuPress() },
        onHomeClick = {
            if (state.power == PowerState.ON && state.screenAwake) state.goHome()
        },
        onHomeLongClick = { state.homeLongPress() },
        onBackClick = { state.back() },
        onVolumeUp = { if (state.screenAwake && state.power != PowerState.OFF) state.volumeUp() },
        onVolumeDown = { if (state.screenAwake && state.power != PowerState.OFF) state.volumeDown() }
    ) {
        EmulatedScreen(state)
    }
}
