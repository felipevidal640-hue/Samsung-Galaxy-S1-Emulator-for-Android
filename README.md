# Samsung Galaxy S1 Emulator for Android

Relive the phone that started the Galaxy era. This app recreates the **Samsung
Galaxy S GT-I9000 (2010)** — the original Galaxy S — as a faithful, interactive
emulator that runs on any modern Android phone.

You don't get a picture of a phone: you get the phone. A glossy black GT-I9000
with chrome trim, earpiece, front camera, capacitive Menu/Back keys and the
physical Home key sits on screen. Press its side **power key** and it boots —
SAMSUNG logo, glowing *Galaxy S GT-I9000* animation and all — into
**Android 2.3.6 Gingerbread with TouchWiz 3.0**.

> This is a recreated experience (a simulation of the device and its software),
> not Samsung's original firmware or a hardware-level emulator. No original
> ROM, binary or copyrighted asset is included; every screen is redrawn in code.

## The hardware (emulated)

| Galaxy S GT-I9000 (2010) | In this emulator |
| --- | --- |
| 4.0″ Super AMOLED, 480 × 800 | The drawn handset's screen area |
| 1 GHz Hummingbird, 512 MB RAM | Quoted everywhere the original quoted it (About phone, Task manager) |
| Physical Home key, capacitive Menu / Back | All three work, including long-press Home → Task manager |
| Side power key & volume rocker | Tap power = sleep/wake, hold power = Phone options, rocker drives the volume HUD |
| 1500 mAh battery | Status bar shows **your real phone's** battery level |
| 5 MP camera, 720p video | Camera that "takes" photos into Gallery |

## The software (TouchWiz 3.0)

- **Boot sequence** — SAMSUNG → animated Galaxy S GT-I9000 swirl → lock screen.
- **Lock screen** — big clock, TouchWiz puzzle pieces for missed calls/messages,
  and the glass **"Swipe screen to unlock"** bar (actually swipe it).
- **Launcher** — 3 home-screen pages with the Clock/Weather (AccuWeather),
  Google Search, Daily Briefing and Mini Diary widgets, page dots, and the
  classic Phone / Contacts / Messaging / **Applications** dock.
- **Application drawer** — all 24 original-style apps in a 4-across grid.
- **Notification shade** — drag down from the status bar for the five TouchWiz
  quick toggles (Wi-Fi, Bluetooth, GPS, Sound, Rotation) and notifications.
- **Hardware keys** — Menu opens the TouchWiz launcher menu (Wallpaper really
  changes the wallpaper), Back walks back through apps, Home goes home,
  hold Home opens the Task manager ("End all / Clear memory").

## Working apps

Phone (dialer, call log, live in-call screen with timer) · Contacts ·
Messaging (send a text, get a reply) · Internet browser · Camera & Gallery ·
Music Player (plays through a playlist) · Video Player · YouTube · Maps ·
Email · Android Market (the green 2010 one — installs stick) · Clock
(alarm, stopwatch, timer, world clock) · Calculator (a real one) · Calendar ·
Memo (create/edit/delete notes) · My Files · Settings (brightness and wallpaper
actually change the phone; full About phone) · Task Manager · FM Radio ·
Voice Recorder · AllShare · ThinkFree Office · Mini Diary.

## Building

Requires JDK 17 and the Android SDK (API 34).

```bash
./gradlew assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/`.

You can also grab a ready-made build: every push runs
**Build Galaxy S1 Emulator APK** in GitHub Actions and uploads
`GalaxyS1-Emulator-debug-apk` as an artifact.

## Installing

1. Download the APK (from Actions artifacts or build it yourself).
2. Allow "Install unknown apps" for your browser/files app if Android asks.
3. Install, open **Galaxy S1 Emulator**, and press the power key on the
   right edge of the little Galaxy S.

## How to drive it

- **Power (right edge)** — tap: wake/sleep · hold: Phone options (Silent,
  Flight mode, Restart, Power off) · from off: boot.
- **Volume rocker (left edge)** — volume HUD, 0–15, just like the original.
- **Menu (≡)** — TouchWiz launcher menu. **Home** — home screen.
  **Hold Home** — Task manager. **Back (↩)** — back.
- **Pull down** from the emulated status bar — notification shade.
- Your host phone's system Back button also drives the emulated Back key.

## Project layout

```
app/src/main/java/com/galaxys1/emulator/
├── MainActivity.kt    Host activity, clocks, battery, toast/HUD plumbing
├── DeviceShell.kt     The GT-I9000 hardware: body, keys, rocker, screen
├── EmulatorState.kt   Firmware state machine + user data
├── Data.kt            Apps, contacts, messages, tracks, wallpapers…
├── TouchWiz.kt        Boot, lock screen, status bar, shade, launcher
├── Apps.kt            Phone, Contacts, Messaging, Browser, Camera,
│                      Gallery, Music, Video, Settings
└── AppsMore.kt        Email, YouTube, Maps, Market, Clock, Calculator,
                       Calendar, Memo, Files, Radio, Recorder, and more
```

Everything — icons, wallpapers, "photos", the map — is drawn at runtime with
Jetpack Compose, so the APK stays tiny and needs no permissions at all.

Made for everyone who remembers when 480 × 800 was stunning. 📱
