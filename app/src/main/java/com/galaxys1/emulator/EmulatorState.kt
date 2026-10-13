package com.galaxys1.emulator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Power / boot phases of the emulated Galaxy S GT-I9000. */
enum class PowerState { OFF, BOOT, LOCK, ON }

/**
 * Central state holder for the emulated device.
 *
 * Everything the "firmware" (Android 2.3.6 + TouchWiz 3.0) does goes through
 * here: which app is open, whether the notification shade is pulled down,
 * the quick-settings toggles, the wallpaper, the brightness, the contents of
 * Messaging / Memo / Gallery, and so on.
 */
class EmulatorState {

    // ---- hardware / power -------------------------------------------------
    var power by mutableStateOf(PowerState.OFF)
    var screenAwake by mutableStateOf(false)

    /** true once the phone has finished its very first boot. */
    var bootedOnce by mutableStateOf(false)

    // ---- launcher / navigation -------------------------------------------
    var currentApp by mutableStateOf<String?>(null)

    /**
     * Detail screen inside the current app, encoded by the app itself,
     * e.g. "c:3" (contact 3), "t:1" (message thread 1), "p:0" (photo 0),
     * "set:display" (a Settings sub-screen). null = the app's top level.
     */
    var appDetail by mutableStateOf<String?>(null)

    var drawerOpen by mutableStateOf(false)
    var shadeOpen by mutableStateOf(false)
    var launcherMenuOpen by mutableStateOf(false)
    var deviceOptionsOpen by mutableStateOf(false)
    var recentsOpen by mutableStateOf(false)

    var homePage by mutableStateOf(1)

    // ---- status bar clock --------------------------------------------------
    var nowMillis by mutableStateOf(System.currentTimeMillis())

    // ---- system settings (emulated) ---------------------------------------
    var wifiOn by mutableStateOf(true)
    var bluetoothOn by mutableStateOf(false)
    var gpsOn by mutableStateOf(true)
    var soundOn by mutableStateOf(true)
    var autoRotateOn by mutableStateOf(true)
    var flightMode by mutableStateOf(false)

    /** Emulated screen brightness 0.25 .. 1.0 */
    var brightness by mutableStateOf(0.9f)

    /** Ringer / media volume 0 .. 15, like the Galaxy S volume rocker. */
    var volume by mutableStateOf(9)
    var volumeHudVisible by mutableStateOf(false)
    var volumeHudSeq by mutableStateOf(0)

    var wallpaperIndex by mutableStateOf(0)

    /** Host battery level 0..100 (Int), -1 if unknown. Shown in the status bar. */
    var hostBattery by mutableStateOf(-1)

    // ---- transient UI ------------------------------------------------------
    var toast by mutableStateOf<String?>(null)
    var toastSeq by mutableStateOf(0)

    // ---- user data ---------------------------------------------------------
    val openedApps = mutableStateListOf<String>()
    val installedApps = mutableStateListOf<String>()
    val photos = mutableStateListOf(101, 205, 312, 418, 527, 633, 740, 846, 952, 105, 219, 324)
    val threads = mutableStateListOf<MessageThread>()
    val memos = mutableStateListOf<Memo>()
    val downloads = mutableStateListOf<String>()

    init {
        threads.addAll(seedThreads())
        memos.addAll(
            listOf(
                Memo("Galaxy S launch memo", "4.0\" Super AMOLED, 480 x 800.\n1 GHz Hummingbird, 512 MB RAM.\nThe one that started it all.", "2010-06-04"),
                Memo("Shopping", "MicroSD card 16 GB\nScreen protector\nSpare battery EB575152VU", "2010-06-18")
            )
        )
    }

    // ---- actions -----------------------------------------------------------

    fun showToast(message: String) {
        toast = message
        toastSeq++
    }

    fun powerPress() {
        when (power) {
            PowerState.OFF -> startBoot()
            PowerState.BOOT -> Unit
            PowerState.LOCK, PowerState.ON -> {
                // short press of the power key: sleep / wake
                if (screenAwake) {
                    screenAwake = false
                    shadeOpen = false
                    launcherMenuOpen = false
                    deviceOptionsOpen = false
                    recentsOpen = false
                } else {
                    screenAwake = true
                }
            }
        }
    }

    fun powerLongPress() {
        when (power) {
            PowerState.OFF -> startBoot()
            PowerState.BOOT -> Unit
            PowerState.LOCK, PowerState.ON -> {
                if (screenAwake) deviceOptionsOpen = true else screenAwake = true
            }
        }
    }

    fun startBoot() {
        power = PowerState.BOOT
        screenAwake = true
        currentApp = null
        appDetail = null
        drawerOpen = false
        shadeOpen = false
        launcherMenuOpen = false
        deviceOptionsOpen = false
        recentsOpen = false
    }

    fun bootFinished() {
        bootedOnce = true
        power = PowerState.LOCK
        screenAwake = true
    }

    fun unlock() {
        if (power == PowerState.LOCK) power = PowerState.ON
    }

    fun lock() {
        if (power == PowerState.ON) power = PowerState.LOCK
    }

    fun powerOff() {
        power = PowerState.OFF
        screenAwake = false
        deviceOptionsOpen = false
        currentApp = null
        appDetail = null
        drawerOpen = false
        shadeOpen = false
    }

    fun openApp(id: String) {
        currentApp = id
        appDetail = null
        drawerOpen = false
        launcherMenuOpen = false
        shadeOpen = false
        recentsOpen = false
        if (!openedApps.contains(id)) openedApps.add(id)
    }

    fun goHome() {
        currentApp = null
        appDetail = null
        drawerOpen = false
        launcherMenuOpen = false
        shadeOpen = false
        recentsOpen = false
    }

    /** Hardware Back key. Returns true when something consumed the press. */
    fun back(): Boolean {
        if (power != PowerState.ON || !screenAwake) return false
        return when {
            deviceOptionsOpen -> { deviceOptionsOpen = false; true }
            recentsOpen -> { recentsOpen = false; true }
            launcherMenuOpen -> { launcherMenuOpen = false; true }
            shadeOpen -> { shadeOpen = false; true }
            appDetail != null -> { appDetail = null; true }
            currentApp != null -> { currentApp = null; true }
            drawerOpen -> { drawerOpen = false; true }
            else -> false
        }
    }

    /** Hardware Menu key. */
    fun menuPress() {
        if (power != PowerState.ON || !screenAwake) return
        if (shadeOpen) { shadeOpen = false; return }
        launcherMenuOpen = !launcherMenuOpen
    }

    /** Hardware Home key (long press opens the original Task manager). */
    fun homeLongPress() {
        if (power == PowerState.ON && screenAwake) {
            recentsOpen = !recentsOpen
            launcherMenuOpen = false
            shadeOpen = false
        }
    }

    fun volumeUp() {
        if (volume < 15) volume++
        volumeHudVisible = true
        volumeHudSeq++
    }

    fun volumeDown() {
        if (volume > 0) volume--
        volumeHudVisible = true
        volumeHudSeq++
    }
}

// ---------------------------------------------------------------------------
// Data models shared by the firmware apps
// ---------------------------------------------------------------------------

/** An application installed on the emulated phone. */
data class EmuApp(
    val id: String,
    val name: String,
    /** Big friendly glyph drawn on the icon, like the original glossy tiles. */
    val glyph: String,
    val colorTop: Long,
    val colorBottom: Long
)

data class Contact(
    val name: String,
    val phone: String,
    val email: String,
    val group: String,
    val color: Long
)

data class ChatMessage(val fromMe: Boolean, val text: String, val time: String)

data class MessageThread(
    val id: Int,
    val contact: String,
    val color: Long,
    val messages: MutableList<ChatMessage>
)

data class Memo(val title: String, val body: String, val date: String)

data class Track(val title: String, val artist: String, val album: String, val durationSec: Int, val color: Long)

data class VideoItem(val title: String, val source: String, val duration: String, val views: String, val color: Long)

data class EmailItem(val from: String, val subject: String, val preview: String, val body: String, val time: String, val unread: Boolean, val color: Long)

data class MarketApp(val name: String, val developer: String, val price: String, val rating: String, val downloads: String, val glyph: String, val color: Long)

data class EmuNotification(val app: String, val title: String, val text: String, val time: String, val glyph: String)

data class Wallpaper(val name: String, val colors: List<Long>, val accent: Long)
