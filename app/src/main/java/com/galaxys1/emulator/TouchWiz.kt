package com.galaxys1.emulator

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sin

// ---------------------------------------------------------------------------
// Time formatting helpers
// ---------------------------------------------------------------------------

fun formatTime(millis: Long): String = SimpleDateFormat("h:mm", Locale.US).format(Date(millis))
fun formatTimeAmPm(millis: Long): String = SimpleDateFormat("h:mm a", Locale.US).format(Date(millis))
fun formatDate(millis: Long): String = SimpleDateFormat("EEE, MMMM d, yyyy", Locale.US).format(Date(millis))
fun formatDateShort(millis: Long): String = SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(millis))

// ---------------------------------------------------------------------------
// The emulated screen: boot -> lock screen -> TouchWiz
// ---------------------------------------------------------------------------

@Composable
fun EmulatedScreen(state: EmulatorState) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            state.power == PowerState.OFF -> {
                Box(Modifier.fillMaxSize().background(Color.Black))
            }
            state.power == PowerState.BOOT -> BootScreen(state)
            !state.screenAwake -> {
                Box(Modifier.fillMaxSize().background(Color.Black))
            }
            state.power == PowerState.LOCK -> {
                Column(Modifier.fillMaxSize()) {
                    StatusBar(state)
                    LockScreen(state)
                }
            }
            else -> {
                Column(Modifier.fillMaxSize()) {
                    StatusBar(state)
                    Box(Modifier.fillMaxSize()) {
                        when {
                            state.currentApp != null -> EmulatedApp(state)
                            state.drawerOpen -> AppDrawer(state)
                            else -> HomeScreen(state)
                        }
                        if (state.shadeOpen) NotificationShade(state)
                        if (state.launcherMenuOpen) LauncherMenu(state)
                        if (state.recentsOpen) RecentsOverlay(state)
                        if (state.deviceOptionsOpen) DeviceOptionsDialog(state)
                        if (state.volumeHudVisible) VolumeHud(state)
                        state.toast?.let { ToastView(it) }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Boot sequence: SAMSUNG -> Galaxy S GT-I9000 swirl -> lock screen
// ---------------------------------------------------------------------------

@Composable
fun BootScreen(state: EmulatorState) {
    var phase by remember { mutableStateOf(0) }
    var tick by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        delay(1500); phase = 1
        delay(2600); phase = 2
        delay(450)
        state.bootFinished()
    }
    LaunchedEffect(Unit) {
        while (true) { tick += 0.08f; delay(50) }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when (phase) {
            0 -> Text(
                "S A M S U N G",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold
            )
            1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Samsung", color = Color(0xFFB9C4D6), fontSize = 13.sp)
                val pulse = 0.65f + 0.35f * sin(tick.toDouble()).toFloat()
                Text(
                    "Galaxy S",
                    color = Color(0xFF4FC3F7).copy(alpha = pulse.coerceIn(0.3f, 1f)),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("GT-I9000", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(16.dp))
                Canvas(Modifier.size(120.dp, 26.dp)) {
                    for (i in 0 until 7) {
                        val a = 0.25f + 0.75f * (0.5f + 0.5f * sin((tick - i * 0.55f).toDouble()).toFloat())
                        drawCircle(
                            color = Color(0xFF4FC3F7).copy(alpha = a.coerceIn(0.15f, 1f)),
                            radius = 4.5f + i * 0.4f,
                            center = Offset(12f + i * (size.width - 24f) / 6f, size.height / 2f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("SAMSUNG", color = Color(0xFF8A93A3), fontSize = 9.sp)
            }
            else -> Box(Modifier.fillMaxSize().background(Color.Black))
        }
    }
}

// ---------------------------------------------------------------------------
// Status bar (TouchWiz): drag down for the notification shade
// ---------------------------------------------------------------------------

@Composable
fun StatusBar(state: EmulatorState) {
    var dragTotal by remember { mutableStateOf(0f) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF4A4A4A), Color(0xFF181818))))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onVerticalDrag = { _, amount ->
                        dragTotal += amount
                        if (dragTotal > 42f && state.power == PowerState.ON) state.shadeOpen = true
                    }
                )
            }
            .clickable { if (state.power == PowerState.ON) state.shadeOpen = !state.shadeOpen }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✉", color = Color.White, fontSize = 9.sp)
        Spacer(Modifier.width(3.dp))
        Text("@", color = Color(0xFFBEE3FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (state.flightMode) {
            Text("✈", color = Color.White, fontSize = 10.sp)
            Spacer(Modifier.width(4.dp))
        } else {
            if (state.wifiOn) {
                Text("WiFi", color = Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
            }
            // signal bars
            Row(verticalAlignment = Alignment.Bottom) {
                listOf(3, 5, 7, 9).forEachIndexed { i, h ->
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(h.dp)
                            .background(if (state.flightMode) Color(0xFF555555) else Color.White)
                    )
                    if (i < 3) Spacer(Modifier.width(1.dp))
                }
            }
            Spacer(Modifier.width(4.dp))
            Text(if (state.flightMode) "" else "3G", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(5.dp))
        }
        // battery
        val level = if (state.hostBattery in 0..100) state.hostBattery else 76
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$level%", color = Color.White, fontSize = 8.sp)
            Spacer(Modifier.width(2.dp))
            Box(
                Modifier
                    .width(20.dp)
                    .height(9.dp)
                    .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(2.dp))
                    .padding(1.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(level / 100f)
                        .height(7.dp)
                        .background(if (level > 20) Color(0xFF7ED321) else Color(0xFFD0021B), RoundedCornerShape(1.dp))
                )
            }
        }
        Spacer(Modifier.width(5.dp))
        Text(formatTime(state.nowMillis), color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------------------
// Lock screen: swipe the glass bar to unlock (with puzzle notifications)
// ---------------------------------------------------------------------------

@Composable
fun LockScreen(state: EmulatorState) {
    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(state.wallpaperIndex)
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(26.dp))
            Text(
                formatTime(state.nowMillis),
                color = Color.White,
                fontSize = 52.sp,
                fontWeight = FontWeight.Light,
                style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(1f, 2f), 4f))
            )
            Text(
                formatDate(state.nowMillis),
                color = Color(0xFFE8F4FF),
                fontSize = 13.sp,
                style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(1f, 1f), 3f))
            )
            Spacer(Modifier.height(14.dp))
            // TouchWiz puzzle pieces for missed events
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PuzzleChip("☎", "1 missed call", Color(0xFF7ED321))
                PuzzleChip("✉", "2 messages", Color(0xFFFFC107))
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Battery ${if (state.hostBattery in 0..100) state.hostBattery else 76}%  ·  Galaxy S",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
            Spacer(Modifier.height(8.dp))
            SwipeToUnlock { state.unlock() }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PuzzleChip(glyph: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.38f))
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(glyph, color = color, fontSize = 13.sp)
        Spacer(Modifier.width(5.dp))
        Text(label, color = Color.White, fontSize = 10.5.sp)
    }
}

@Composable
private fun SwipeToUnlock(onUnlock: () -> Unit) {
    val scope = rememberCoroutineScope()
    val anim = remember { Animatable(0f) }
    var trackWidth by remember { mutableStateOf(1f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0x66FFFFFF), Color(0x22FFFFFF), Color(0x11000000)))
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
    ) {
        trackWidth = constraints.maxWidth.toFloat()
        Text(
            "Swipe screen to unlock  ›››",
            color = Color.White,
            fontSize = 12.5.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(anim.value.roundToInt(), 0) }
                .size(58.dp)
                .padding(5.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFB74D), Color(0xFFF57C00))))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (anim.value > trackWidth * 0.42f) {
                                onUnlock()
                                scope.launch { anim.snapTo(0f) }
                            } else {
                                scope.launch { anim.animateTo(0f) }
                            }
                        },
                        onHorizontalDrag = { change, drag ->
                            change.consume()
                            scope.launch {
                                anim.snapTo((anim.value + drag).coerceIn(0f, trackWidth - 70f))
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text("🔓", fontSize = 20.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// Wallpaper
// ---------------------------------------------------------------------------

@Composable
fun WallpaperBackground(index: Int) {
    val wp = WALLPAPERS[index.coerceIn(WALLPAPERS.indices)]
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(wp.colors.map { Color(it) }))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val accent = Color(wp.accent)
            // distant glow
            drawCircle(accent.copy(alpha = 0.20f), radius = size.width * 0.55f, center = Offset(size.width * 0.78f, size.height * 0.20f))
            drawCircle(accent.copy(alpha = 0.12f), radius = size.width * 0.38f, center = Offset(size.width * 0.15f, size.height * 0.62f))
            // star field (deterministic)
            var seed = 1234567L
            fun nextF(): Float {
                seed = (seed * 1103515245L + 12345L) and 0x7fffffffL
                return (seed % 1000L) / 1000f
            }
            repeat(46) {
                drawCircle(
                    Color.White.copy(alpha = 0.16f + nextF() * 0.5f),
                    radius = 0.8f + nextF() * 1.7f,
                    center = Offset(nextF() * size.width, nextF() * size.height * 0.7f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Home screen + widgets + dock
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(state: EmulatorState) {
    var dragPx by remember { mutableStateOf(0f) }
    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(state.wallpaperIndex)
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val pageWidth = constraints.maxWidth.toFloat()
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    when {
                                        dragPx < -70f && state.homePage < HOME_PAGES.lastIndex -> state.homePage++
                                        dragPx > 70f && state.homePage > 0 -> state.homePage--
                                    }
                                    dragPx = 0f
                                },
                                onHorizontalDrag = { change, drag ->
                                    change.consume()
                                    dragPx += drag
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset((-state.homePage * pageWidth + dragPx).roundToInt(), 0) }
                    ) {
                        HOME_PAGES.forEachIndexed { page, ids ->
                            Box(
                                modifier = Modifier
                                    .width(maxWidth)
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                HomePageContent(state, page, ids)
                            }
                        }
                    }
                }

                // page dots
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 3.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    HOME_PAGES.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (i == state.homePage) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == state.homePage) Color(0xFFFF9800) else Color.White.copy(alpha = 0.55f))
                                .clickable { state.homePage = i }
                        )
                    }
                }

                Dock(state)
            }
        }
    }
}

@Composable
private fun HomePageContent(state: EmulatorState, page: Int, ids: List<String>) {
    Column(Modifier.fillMaxSize()) {
        when (page) {
            0 -> {
                DailyBriefingWidget(state)
                Spacer(Modifier.height(10.dp))
            }
            1 -> {
                ClockWeatherWidget(state)
                Spacer(Modifier.height(8.dp))
                GoogleSearchWidget(state)
                Spacer(Modifier.height(10.dp))
            }
            else -> {
                MiniDiaryWidget(state)
                Spacer(Modifier.height(10.dp))
            }
        }
        IconGrid(state, ids)
    }
}

@Composable
fun IconGrid(state: EmulatorState, ids: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ids.chunked(4).forEach { rowIds ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowIds.forEach { id ->
                    AppIcon(appById(id)) { state.openApp(id) }
                }
                repeat(4 - rowIds.size) { Spacer(Modifier.width(64.dp)) }
            }
        }
    }
}

@Composable
private fun Dock(state: EmulatorState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0x33000000), Color(0x99000000)))
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DOCK_APPS.forEach { id -> AppIcon(appById(id)) { state.openApp(id) } }
        // Applications key
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(64.dp)
                .clickable { state.drawerOpen = true }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(3.dp, RoundedCornerShape(11.dp))
                    .clip(RoundedCornerShape(11.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF64B5F6), Color(0xFF1565C0)))),
                contentAlignment = Alignment.Center
            ) { Text("▦", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            Text(
                "Applications",
                color = Color.White,
                fontSize = 9.sp,
                maxLines = 1,
                style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 3f)),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun AppIcon(app: EmuApp, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(3.dp, RoundedCornerShape(11.dp))
                .clip(RoundedCornerShape(11.dp))
                .background(Brush.verticalGradient(listOf(Color(app.colorTop), Color(app.colorBottom)))),
            contentAlignment = Alignment.Center
        ) {
            // glossy highlight
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)))
            )
            Text(app.glyph, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            app.name,
            color = Color.White,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 3f)),
            modifier = Modifier.padding(top = 2.dp).fillMaxWidth()
        )
    }
}

// ---- widgets ---------------------------------------------------------------

@Composable
private fun ClockWeatherWidget(state: EmulatorState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.30f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(formatTime(state.nowMillis), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
            Text(formatDateShort(state.nowMillis), color = Color(0xFFD6E9FF), fontSize = 10.5.sp)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("Seoul  ☀ 24°", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("AccuWeather.com · Sunny", color = Color(0xFFD6E9FF), fontSize = 9.5.sp)
        }
    }
}

@Composable
private fun GoogleSearchWidget(state: EmulatorState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .clickable { state.openApp("browser") }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Google", color = Color(0xFF3366CC), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text("Search…", color = Color(0xFF888888), fontSize = 11.5.sp)
        Spacer(Modifier.weight(1f))
        Text("🎤", fontSize = 13.sp)
    }
}

@Composable
private fun DailyBriefingWidget(state: EmulatorState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.30f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text("DAILY BRIEFING", color = Color(0xFF8EC9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("☀ Seoul 24° · Galaxy S launch party 19:00", color = Color.White, fontSize = 11.5.sp)
        Text("KOSPI 2,012.34 ▲ · 3 updates in Market", color = Color(0xFFD6E9FF), fontSize = 10.sp)
    }
}

@Composable
private fun MiniDiaryWidget(state: EmulatorState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF8E1).copy(alpha = 0.92f))
            .clickable { state.openApp("diary") }
            .padding(12.dp)
    ) {
        Text("MINI DIARY — TODAY", color = Color(0xFF8D6E00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("First day with the Galaxy S. That screen! 📸", color = Color(0xFF4E342E), fontSize = 11.5.sp)
    }
}

// ---------------------------------------------------------------------------
// Application drawer
// ---------------------------------------------------------------------------

@Composable
fun AppDrawer(state: EmulatorState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2111418))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF3C4656), Color(0xFF1E242E))))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Applications", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("${ALL_APPS.size} apps", color = Color(0xFFAFC3D9), fontSize = 10.sp)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(ALL_APPS) { app ->
                AppIcon(app) { state.openApp(app.id) }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(Color(0xFF0B0E13))
                .clickable { state.drawerOpen = false },
            contentAlignment = Alignment.Center
        ) {
            Text("⌂  Home", color = Color.White, fontSize = 13.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// Notification shade
// ---------------------------------------------------------------------------

@Composable
fun NotificationShade(state: EmulatorState) {
    var cleared by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF2E2E2E), Color(0xFF141414))))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, amount ->
                    if (amount < -14f) state.shadeOpen = false
                }
            }
    ) {
        Text(
            "Galaxy S · GT-I9000        ${formatDateShort(state.nowMillis)}",
            color = Color(0xFFBDBDBD),
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickToggle("Wi-Fi", "📶", state.wifiOn) { state.wifiOn = !state.wifiOn }
            QuickToggle("Bluetooth", "ᛒ", state.bluetoothOn) { state.bluetoothOn = !state.bluetoothOn }
            QuickToggle("GPS", "📍", state.gpsOn) { state.gpsOn = !state.gpsOn }
            QuickToggle("Sound", if (state.soundOn) "🔊" else "🔇", state.soundOn) { state.soundOn = !state.soundOn }
            QuickToggle("Rotation", "🔄", state.autoRotateOn) { state.autoRotateOn = !state.autoRotateOn }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1F1F1F)).padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notifications", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(
                "Clear",
                color = Color(0xFF8EC9FF),
                fontSize = 11.sp,
                modifier = Modifier.clickable { cleared = true }
            )
        }
        if (cleared) {
            Text(
                "No notifications",
                color = Color(0xFF9E9E9E),
                fontSize = 11.5.sp,
                modifier = Modifier.padding(14.dp)
            )
        } else {
            NOTIFICATIONS.forEach { n ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            state.shadeOpen = false
                            when (n.app) {
                                "Messaging" -> state.openApp("messaging")
                                "Email" -> state.openApp("email")
                                "Market" -> state.openApp("market")
                                "Calendar" -> state.openApp("calendar")
                                else -> Unit
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(n.glyph, fontSize = 18.sp, modifier = Modifier.width(30.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${n.app} · ${n.title}", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(n.text, color = Color(0xFFBDBDBD), fontSize = 10.5.sp, maxLines = 1)
                    }
                    Text(n.time, color = Color(0xFF8E8E8E), fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.fillMaxWidth().height(22.dp).background(Color(0xFF0A0A0A)),
            contentAlignment = Alignment.Center
        ) { Text("⠿", color = Color(0xFF777777), fontSize = 12.sp) }
    }
}

@Composable
private fun QuickToggle(label: String, glyph: String, on: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(58.dp).clickable { onClick() }
    ) {
        Text(glyph, fontSize = 19.sp, color = if (on) Color.White else Color(0xFF777777))
        Text(label, color = if (on) Color.White else Color(0xFF9E9E9E), fontSize = 9.sp)
        Box(
            Modifier
                .padding(top = 3.dp)
                .width(34.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (on) Color(0xFF7ED321) else Color(0xFF444444))
        )
    }
}

// ---------------------------------------------------------------------------
// Launcher menu (hardware Menu key), device options (hold Power), recents
// ---------------------------------------------------------------------------

@Composable
fun LauncherMenu(state: EmulatorState) {
    Box(Modifier.fillMaxSize().clickable { state.launcherMenuOpen = false }) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(Color(0xFFF2F2F2))
                .padding(10.dp)
        ) {
            val items = listOf(
                "＋" to "Add", "🖼" to "Wallpaper", "🔍" to "Search",
                "🔔" to "Notifications", "✎" to "Edit", "⚙" to "Settings"
            )
            items.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { (glyph, label) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(86.dp)
                                .clickable {
                                    state.launcherMenuOpen = false
                                    when (label) {
                                        "Wallpaper" -> {
                                            state.wallpaperIndex = (state.wallpaperIndex + 1) % WALLPAPERS.size
                                            state.showToast("Wallpaper: ${WALLPAPERS[state.wallpaperIndex].name}")
                                        }
                                        "Search" -> state.openApp("browser")
                                        "Notifications" -> state.shadeOpen = true
                                        "Settings" -> state.openApp("settings")
                                        else -> state.showToast("$label — TouchWiz 3.0")
                                    }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(glyph, fontSize = 21.sp, color = Color(0xFF37474F))
                            Text(label, color = Color(0xFF263238), fontSize = 10.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceOptionsDialog(state: EmulatorState) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f))
            .clickable { state.deviceOptionsOpen = false },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(252.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
                .clickable { }
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Phone options",
                color = Color(0xFF212121),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
            DeviceOptionRow(if (state.soundOn) "🔊" else "🔇", if (state.soundOn) "Silent mode (on)" else "Silent mode (off)") {
                state.soundOn = !state.soundOn
                state.deviceOptionsOpen = false
                state.showToast(if (state.soundOn) "Sound on" else "Silent mode activated")
            }
            DeviceOptionRow("✈", if (state.flightMode) "Flight mode (on)" else "Flight mode (off)") {
                state.flightMode = !state.flightMode
                state.deviceOptionsOpen = false
                state.showToast(if (state.flightMode) "Flight mode activated" else "Flight mode deactivated")
            }
            DeviceOptionRow("🔄", "Restart") {
                state.deviceOptionsOpen = false
                state.startBoot()
            }
            DeviceOptionRow("⏻", "Power off") { state.powerOff() }
        }
    }
}

@Composable
private fun DeviceOptionRow(glyph: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(glyph, fontSize = 17.sp, modifier = Modifier.width(32.dp))
        Text(label, color = Color(0xFF263238), fontSize = 12.5.sp)
    }
}

@Composable
fun RecentsOverlay(state: EmulatorState) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.72f))
            .clickable { state.recentsOpen = false },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF20242C))
                .clickable { }
                .padding(14.dp)
        ) {
            Text("Task manager", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("RAM  326 MB / 512 MB", color = Color(0xFF9FB3C8), fontSize = 10.sp)
            Box(
                Modifier.fillMaxWidth().height(7.dp).padding(top = 2.dp)
                    .clip(RoundedCornerShape(4.dp)).background(Color(0xFF11141A))
            ) {
                Box(Modifier.fillMaxWidth(0.64f).height(7.dp).background(Color(0xFFFF9800), RoundedCornerShape(4.dp)))
            }
            Spacer(Modifier.height(10.dp))
            if (state.openedApps.isEmpty()) {
                Text("No active applications", color = Color(0xFFB0BEC5), fontSize = 11.5.sp)
            } else {
                state.openedApps.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { id -> AppIcon(appById(id)) { state.openApp(id) } }
                        repeat(4 - row.size) { Spacer(Modifier.width(64.dp)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFC62828))
                    .clickable {
                        state.openedApps.clear()
                        state.recentsOpen = false
                        state.goHome()
                        state.showToast("All applications ended · RAM cleared")
                    }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) { Text("End all / Clear memory", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

// ---------------------------------------------------------------------------
// Volume HUD + toast
// ---------------------------------------------------------------------------

@Composable
fun VolumeHud(state: EmulatorState) {
    Box(Modifier.fillMaxSize().padding(top = 56.dp), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .width(230.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.82f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (state.volume == 0) "🔇  Silent" else "🔊  Volume", color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(15) { i ->
                    Box(
                        Modifier.width(10.dp).height(12.dp)
                            .background(if (i < state.volume) Color(0xFF7ED321) else Color(0xFF3A3A3A), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun ToastView(message: String) {
    Box(Modifier.fillMaxSize().padding(bottom = 92.dp), contentAlignment = Alignment.BottomCenter) {
        Text(
            message,
            color = Color.White,
            fontSize = 11.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
