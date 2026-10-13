package com.galaxys1.emulator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

@Composable
fun MoreApp(state: EmulatorState) {
    when (state.currentApp) {
        "email" -> EmailApp(state)
        "youtube" -> YouTubeApp(state)
        "maps" -> MapsApp(state)
        "market" -> MarketApp(state)
        "clock" -> ClockApp(state)
        "calculator" -> CalculatorApp(state)
        "calendar" -> CalendarApp(state)
        "memo" -> MemoApp(state)
        "myfiles" -> MyFilesApp(state)
        "taskmgr" -> TaskManagerApp(state)
        "fmr" -> FmRadioApp(state)
        "voice" -> VoiceRecorderApp(state)
        "allshare" -> AllShareApp(state)
        "thinkfree" -> ThinkFreeApp(state)
        "diary" -> DiaryApp(state)
        else -> {
            val app = appById(state.currentApp ?: "phone")
            Column(Modifier.fillMaxSize().background(Color(0xFFF4F6F8)), horizontalAlignment = Alignment.CenterHorizontally) {
                AppHeader(app.name, app.colorTop, app.colorBottom, onBack = { state.goHome() })
                Spacer(Modifier.height(40.dp))
                Text(app.glyph, fontSize = 54.sp)
                Text(app.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
                Text("Part of the original Galaxy S TouchWiz suite.", fontSize = 11.sp, color = Color(0xFF78909C))
            }
        }
    }
}

private fun clockText(millis: Long, withSeconds: Boolean = false): String {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    val h = c.get(Calendar.HOUR).let { if (it == 0) 12 else it }
    val m = c.get(Calendar.MINUTE)
    val s = c.get(Calendar.SECOND)
    val ampm = if (c.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
    return if (withSeconds) "%d:%02d:%02d %s".format(h, m, s, ampm) else "%d:%02d %s".format(h, m, ampm)
}

// ---------------------------------------------------------------------------
// Email
// ---------------------------------------------------------------------------

@Composable
fun EmailApp(state: EmulatorState) {
    val detail = state.appDetail
    if (detail != null && detail.startsWith("e:")) {
        val mail = EMAILS.getOrNull(detail.removePrefix("e:").toIntOrNull() ?: -1)
        if (mail != null) {
            Column(Modifier.fillMaxSize().background(Color.White)) {
                AppHeader("Email", 0xFF90A4AE, 0xFF546E7A, onBack = { state.appDetail = null }, action = "Reply", onAction = { state.showToast("Reply composer (demo)") })
                LazyColumn(Modifier.padding(16.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(mail.from, mail.color, 44)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(mail.from, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                                Text("To: me · ${mail.time}", fontSize = 10.sp, color = Color(0xFF78909C))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(mail.subject, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                        Spacer(Modifier.height(10.dp))
                        Text(mail.body, fontSize = 12.5.sp, color = Color(0xFF37474F))
                    }
                }
            }
            return
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("Email — Inbox", 0xFF90A4AE, 0xFF546E7A, action = "✎ Compose", onAction = { state.showToast("Composer (demo)") })
        LazyColumn {
            items(EMAILS.indices.toList()) { i ->
                val mail = EMAILS[i]
                Row(
                    Modifier.fillMaxWidth().clickable { state.appDetail = "e:$i" }.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(mail.from, mail.color, 38)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (mail.unread) Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF1976D2)))
                            if (mail.unread) Spacer(Modifier.width(5.dp))
                            Text(mail.from, fontSize = 13.sp, fontWeight = if (mail.unread) FontWeight.Bold else FontWeight.Normal, color = Color(0xFF212121))
                        }
                        Text(mail.subject, fontSize = 12.sp, fontWeight = if (mail.unread) FontWeight.Bold else FontWeight.Normal, color = Color(0xFF37474F), maxLines = 1)
                        Text(mail.preview, fontSize = 10.5.sp, color = Color(0xFF90A4AE), maxLines = 1)
                    }
                    Text(mail.time, fontSize = 9.sp, color = Color(0xFF90A4AE))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// YouTube
// ---------------------------------------------------------------------------

@Composable
fun YouTubeApp(state: EmulatorState) {
    val detail = state.appDetail
    if (detail != null && detail.startsWith("y:")) {
        val idx = detail.removePrefix("y:").toIntOrNull() ?: 0
        val v = VIDEOS.getOrNull(idx)
        if (v != null) {
            var playing by remember { mutableStateOf(true) }
            Column(Modifier.fillMaxSize().background(Color(0xFF141414))) {
                AppHeader("YouTube", 0xFFE57373, 0xFFB71C1C, onBack = { state.appDetail = null })
                Box(Modifier.fillMaxWidth().height(208.dp).clickable { playing = !playing }) {
                    PhotoScene(500 + idx * 91, Modifier.fillMaxSize())
                    if (!playing) Text("▶", color = Color.White, fontSize = 52.sp, modifier = Modifier.align(Alignment.Center))
                    Text("720p", color = Color.White, fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp))
                }
                Column(Modifier.padding(14.dp)) {
                    Text(v.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("${v.source} · ${v.views} views", color = Color(0xFFBDBDBD), fontSize = 10.5.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TwButton(if (playing) "⏸ Pause" else "▶ Play", 0xFFC62828, Modifier.weight(1f)) { playing = !playing }
                        TwButton("👍 Like", 0xFF37474F, Modifier.weight(1f)) { state.showToast("Liked") }
                        TwButton("⤴ Share", 0xFF37474F, Modifier.weight(1f)) { state.showToast("Share via Messaging / Email") }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Comments", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("galaxyfan2010: That AMOLED black level is unreal 🔥\n\nmobilefan_uk: Shot on my i9000, still holds up!", color = Color(0xFFBDBDBD), fontSize = 11.sp)
                }
            }
            return
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("YouTube", 0xFFE57373, 0xFFB71C1C, action = "🔍", onAction = { state.showToast("Search YouTube (demo)") })
        LazyColumn {
            items(VIDEOS.indices.toList()) { i ->
                val v = VIDEOS[i]
                Row(Modifier.fillMaxWidth().clickable { state.appDetail = "y:$i" }.padding(10.dp)) {
                    Box(Modifier.width(104.dp).height(62.dp).clip(RoundedCornerShape(5.dp))) {
                        PhotoScene(500 + i * 91, Modifier.fillMaxSize())
                        Text("▶", color = Color.White, fontSize = 17.sp, modifier = Modifier.align(Alignment.Center))
                        Text(v.duration, color = Color.White, fontSize = 8.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(v.title, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
                        Text("${v.source} · ${v.views} views", fontSize = 10.sp, color = Color(0xFF78909C))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Maps
// ---------------------------------------------------------------------------

@Composable
fun MapsApp(state: EmulatorState) {
    var query by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("Samsung Town, Seoul") }
    var zoom by remember { mutableStateOf(1f) }

    Column(Modifier.fillMaxSize().background(Color(0xFFE8F0E4))) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
            TwTextField(query, { query = it }, "Search Maps", Modifier.weight(1f))
            Spacer(Modifier.width(6.dp))
            TwButton("Search", 0xFF558B2F, Modifier.width(68.dp)) {
                if (query.isNotBlank()) { place = query; state.showToast("Showing: $query") }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color(0xFFEAF2E2))
                val gap = 46f * zoom
                var x = 0f
                while (x < size.width) { drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), strokeWidth = 9f * zoom); x += gap }
                var y = 0f
                while (y < size.height) { drawLine(Color.White, Offset(0f, y), Offset(size.width, y), strokeWidth = 9f * zoom); y += gap }
                drawLine(Color(0xFFFFD54F), Offset(0f, size.height * 0.42f), Offset(size.width, size.height * 0.58f), strokeWidth = 13f * zoom)
                drawLine(Color(0xFF90CAF9), Offset(size.width * 0.2f, size.height), Offset(size.width * 0.55f, 0f), strokeWidth = 20f * zoom)
                drawCircle(Color(0xFFD32F2F), radius = 11f, center = Offset(size.width / 2f, size.height / 2f - 13f))
                drawCircle(Color.White, radius = 4.5f, center = Offset(size.width / 2f, size.height / 2f - 13f))
            }
            Column(Modifier.align(Alignment.TopCenter).padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color.White).border(1.dp, Color(0xFFB0BEC5), RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("📍 $place", fontSize = 11.5.sp, color = Color(0xFF263238), fontWeight = FontWeight.SemiBold)
                }
            }
            Column(Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) {
                MapZoomButton("＋") { zoom = (zoom * 1.25f).coerceAtMost(2.6f) }
                Spacer(Modifier.height(6.dp))
                MapZoomButton("－") { zoom = (zoom / 1.25f).coerceAtLeast(0.55f) }
            }
            Row(Modifier.align(Alignment.BottomCenter).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TwButton("◎ My location", 0xFF37474F, Modifier.width(118.dp)) { place = "Samsung Town, Seoul"; state.showToast("Location found · GPS satellites") }
                TwButton("➤ Directions", 0xFF558B2F, Modifier.width(118.dp)) { state.showToast("Route: 12 min · 4.2 km by car") }
            }
        }
        Text("Google Maps · Data ©2010 · GPS ${if (state.gpsOn) "on" else "off"}", fontSize = 9.sp, color = Color(0xFF607D8B), modifier = Modifier.fillMaxWidth().background(Color.White).padding(6.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun MapZoomButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(38.dp).clip(RoundedCornerShape(6.dp)).background(Color.White)
            .border(1.dp, Color(0xFFB0BEC5), RoundedCornerShape(6.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(label, fontSize = 18.sp, color = Color(0xFF37474F), fontWeight = FontWeight.Bold) }
}

// ---------------------------------------------------------------------------
// Android Market (the original, green one)
// ---------------------------------------------------------------------------

@Composable
fun MarketApp(state: EmulatorState) {
    var installing by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(installing) {
        val name = installing
        if (name != null) {
            delay(1600)
            state.installedApps.add(name)
            state.showToast("$name installed")
            installing = null
        }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F4F4))) {
        Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF7CB342), Color(0xFF558B2F)))).padding(12.dp)) {
            Text("Android Market", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Apps · Games · My apps (${state.installedApps.size} installed)", color = Color(0xFFDCEDC8), fontSize = 10.5.sp)
        }
        LazyColumn {
            items(MARKET_APPS) { app ->
                val installed = state.installedApps.contains(app.name)
                Row(Modifier.fillMaxWidth().background(Color.White).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).clip(RoundedCornerShape(8.dp)).background(Color(app.color)), contentAlignment = Alignment.Center) {
                        Text(app.glyph, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        Text("${app.developer} · ${app.rating}", fontSize = 10.sp, color = Color(0xFF78909C))
                        Text("${app.downloads} downloads", fontSize = 9.5.sp, color = Color(0xFF90A4AE))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(app.price, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF33691E))
                        Spacer(Modifier.height(5.dp))
                        TwButton(
                            when { installed -> "Installed"; installing == app.name -> "Downloading…"; else -> "Install" },
                            if (installed) 0xFF9E9E9E else 0xFF558B2F,
                            Modifier.width(96.dp)
                        ) { if (!installed && installing == null) installing = app.name }
                    }
                }
            }
            item {
                Text(
                    "Market tip: apps you install are remembered in My apps for this session.",
                    fontSize = 10.sp, color = Color(0xFF78909C), modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Clock (clock / alarm / stopwatch / timer / world clock)
// ---------------------------------------------------------------------------

@Composable
fun ClockApp(state: EmulatorState) {
    var tab by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().background(Color(0xFF10151B))) {
        AppHeader("Clock", 0xFF4DD0E1, 0xFF006064)
        Row(Modifier.fillMaxWidth().background(Color(0xFF1B232C))) {
            listOf("Clock", "Alarm", "Stopwatch", "Timer", "World").forEachIndexed { i, label ->
                Box(
                    Modifier.weight(1f).background(if (tab == i) Color(0xFF26313C) else Color(0xFF1B232C))
                        .clickable { tab = i }.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) { Text(label, fontSize = 9.5.sp, color = if (tab == i) Color.White else Color(0xFF90A4AE), fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal) }
            }
        }
        when (tab) {
            0 -> Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(24.dp))
                Text(clockText(state.nowMillis, true), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
                Text(formatDate(state.nowMillis), color = Color(0xFF90CAF9), fontSize = 12.sp)
                Spacer(Modifier.height(18.dp))
                Text("Seoul ☀ 24° · AccuWeather", color = Color(0xFFB0BEC5), fontSize = 11.5.sp)
            }
            1 -> {
                val alarms = remember { mutableStateListOf(true, false, true) }
                val labels = listOf("07:00 — Wake up (Mon–Fri)", "08:30 — Gym", "19:00 — Galaxy S launch party")
                LazyColumn {
                    items(labels.indices.toList()) { i ->
                        SwitchRow(labels[i], if (alarms[i]) "Alarm on" else "Alarm off", alarms[i]) { alarms[i] = it }
                    }
                    item { Box(Modifier.padding(14.dp)) { TwButton("＋ Create alarm", 0xFF0097A7, Modifier.fillMaxWidth()) { state.showToast("Alarm created for 7:00 AM") } } }
                }
            }
            2 -> StopwatchTab(state)
            3 -> TimerTab(state)
            else -> LazyColumn {
                items(
                    listOf(
                        "Seoul" to "Asia/Seoul", "London" to "Europe/London",
                        "New York City" to "America/New_York", "Tokyo" to "Asia/Tokyo", "Sydney" to "Australia/Sydney"
                    )
                ) { (city, tz) ->
                    val cal = Calendar.getInstance(TimeZone.getTimeZone(tz))
                    cal.timeInMillis = state.nowMillis
                    val h = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
                    Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 11.dp)) {
                        Text(city, fontSize = 13.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                        Text("%d:%02d %s".format(h, cal.get(Calendar.MINUTE), if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"), fontSize = 13.sp, color = Color(0xFF37474F))
                    }
                }
            }
        }
    }
}

@Composable
private fun StopwatchTab(state: EmulatorState) {
    var running by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableStateOf(0L) }
    val laps = remember { mutableStateListOf<String>() }
    LaunchedEffect(running) {
        while (running) { delay(47); elapsedMs += 47 }
    }
    fun fmt(ms: Long): String = "%02d:%02d.%d".format(ms / 60000, (ms / 1000) % 60, (ms / 100) % 10)
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(20.dp))
        Text(fmt(elapsedMs), color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TwButton(if (running) "Pause" else "Start", 0xFF0097A7, Modifier.width(92.dp)) { running = !running }
            TwButton("Lap", 0xFF37474F, Modifier.width(76.dp)) { if (elapsedMs > 0) laps.add(0, "Lap ${laps.size + 1} · ${fmt(elapsedMs)}") }
            TwButton("Reset", 0xFF37474F, Modifier.width(76.dp)) { running = false; elapsedMs = 0; laps.clear() }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn { items(laps) { lap -> Text(lap, color = Color(0xFFB0BEC5), fontSize = 12.sp, modifier = Modifier.padding(vertical = 3.dp)) } }
    }
}

@Composable
private fun TimerTab(state: EmulatorState) {
    var remain by remember { mutableStateOf(300) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        while (running && remain > 0) { delay(1000); remain-- }
        if (remain == 0 && running) { running = false; state.showToast("⏰ Timer finished!") }
    }
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(20.dp))
        Text(formatDuration(remain), color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(60, 300, 600, 900).forEach { sec ->
                TwButton("${sec / 60} min", 0xFF37474F, Modifier.width(64.dp)) { remain = sec; running = false }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TwButton(if (running) "Pause" else "Start", 0xFF0097A7, Modifier.width(96.dp)) { if (remain > 0) running = !running }
            TwButton("Reset", 0xFF37474F, Modifier.width(76.dp)) { running = false; remain = 300 }
        }
    }
}

// ---------------------------------------------------------------------------
// Calculator
// ---------------------------------------------------------------------------

@Composable
fun CalculatorApp(state: EmulatorState) {
    var expr by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    fun evaluate(e: String): String {
        return try {
            val tokens = mutableListOf<Any>()
            var i = 0
            var expectNumber = true
            while (i < e.length) {
                val ch = e[i]
                when {
                    ch.isDigit() || ch == '.' -> {
                        var j = i
                        while (j < e.length && (e[j].isDigit() || e[j] == '.')) j++
                        var num = e.substring(i, j).toDouble()
                        if (j < e.length && e[j] == '%') { num /= 100.0; j++ }
                        tokens.add(num)
                        i = j
                        expectNumber = false
                    }
                    ch == '-' && expectNumber -> {
                        var j = i + 1
                        while (j < e.length && (e[j].isDigit() || e[j] == '.')) j++
                        if (j == i + 1) { tokens.add(ch); i++ } else {
                            var num = -e.substring(i + 1, j).toDouble()
                            if (j < e.length && e[j] == '%') { num /= 100.0; j++ }
                            tokens.add(num); i = j; expectNumber = false
                        }
                    }
                    ch in listOf('+', '-', '×', '÷') -> { tokens.add(ch); i++; expectNumber = true }
                    ch == '%' -> i++
                    else -> i++
                }
            }
            if (tokens.isEmpty()) return ""
            val pass1 = mutableListOf<Any>()
            var k = 0
            while (k < tokens.size) {
                val t = tokens[k]
                if ((t == '×' || t == '÷') && pass1.isNotEmpty() && k + 1 < tokens.size && tokens[k + 1] is Double && pass1.last() is Double) {
                    val a = pass1.removeAt(pass1.lastIndex) as Double
                    val b = tokens[k + 1] as Double
                    pass1.add(if (t == '×') a * b else a / b)
                    k += 2
                } else { pass1.add(t); k++ }
            }
            var acc = pass1.firstOrNull() as? Double ?: return "Error"
            var m = 1
            while (m + 1 < pass1.size + 1 && m < pass1.size) {
                val op = pass1[m]
                val next = pass1.getOrNull(m + 1) as? Double ?: break
                acc = when (op) { '+' -> acc + next; '-' -> acc - next; else -> acc }
                m += 2
            }
            if (acc == acc.toLong().toDouble()) acc.toLong().toString() else "%.6f".format(acc).trimEnd('0').trimEnd('.')
        } catch (ex: Exception) { "Error" }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF212121))) {
        AppHeader("Calculator", 0xFFFF8A65, 0xFFBF360C)
        Column(Modifier.fillMaxWidth().background(Color(0xFFFFF8E1)).padding(14.dp), horizontalAlignment = Alignment.End) {
            Text(expr.ifEmpty { "0" }, fontSize = 26.sp, color = Color(0xFF263238), maxLines = 1)
            Text(result.ifEmpty { " " }, fontSize = 15.sp, color = Color(0xFF00897B), fontWeight = FontWeight.Bold)
        }
        val keys = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )
        Column(Modifier.weight(1f).padding(7.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            keys.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { key ->
                        val isOp = key in listOf("÷", "×", "-", "+", "=")
                        Box(
                            Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(7.dp))
                                .background(
                                    when {
                                        key == "=" -> Brush.verticalGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)))
                                        isOp -> Brush.verticalGradient(listOf(Color(0xFF546E7A), Color(0xFF37474F)))
                                        key == "C" -> Brush.verticalGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828)))
                                        else -> Brush.verticalGradient(listOf(Color(0xFFFAFAFA), Color(0xFFE0E0E0)))
                                    }
                                )
                                .clickable {
                                    when (key) {
                                        "C" -> { expr = ""; result = "" }
                                        "⌫" -> expr = expr.dropLast(1)
                                        "±" -> expr = if (expr.startsWith("-")) expr.drop(1) else "-$expr"
                                        "=" -> { result = evaluate(expr); if (result != "Error" && result.isNotEmpty()) { expr = result; result = "" } }
                                        else -> { expr += key; result = evaluate(expr) }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(key, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = if (isOp || key == "C" || key == "=") Color.White else Color(0xFF263238))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Calendar
// ---------------------------------------------------------------------------

@Composable
fun CalendarApp(state: EmulatorState) {
    val cal = Calendar.getInstance()
    cal.timeInMillis = state.nowMillis
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val today = cal.get(Calendar.DAY_OF_MONTH)
    var selected by remember { mutableStateOf(today) }

    val first = Calendar.getInstance()
    first.set(year, month, 1)
    val startOffset = (first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY)
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.US).format(first.time)

    val events = mapOf(
        today to listOf("Galaxy S launch party · 19:00, Samsung Town"),
        4 to listOf("GT-I9000 release anniversary"),
        15 to listOf("Dentist · 10:30"),
        24 to listOf("Flight to London · KE907")
    )

    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("Calendar — $monthName", 0xFFF06292, 0xFFAD1457)
        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 10.sp, color = Color(0xFF78909C), fontWeight = FontWeight.Bold)
            }
        }
        val cells = List(startOffset) { 0 } + (1..daysInMonth).toList()
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        Modifier.weight(1f).height(42.dp).padding(2.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when {
                                    day == 0 -> Color.Transparent
                                    day == selected -> Color(0xFFC2185B)
                                    day == today -> Color(0xFFFCE4EC)
                                    else -> Color(0xFFFAFAFA)
                                }
                            )
                            .clickable { if (day > 0) selected = day },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day > 0) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day.toString(), fontSize = 12.5.sp, color = if (day == selected) Color.White else Color(0xFF263238), fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal)
                            if (events.containsKey(day)) Text("•", fontSize = 10.sp, color = if (day == selected) Color.White else Color(0xFFC2185B))
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("  $monthName · day $selected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        val dayEvents = events[selected]
        if (dayEvents == null) {
            Text("  No events. Tap ＋ in the menu to add one.", fontSize = 11.5.sp, color = Color(0xFF78909C), modifier = Modifier.padding(top = 6.dp))
        } else {
            dayEvents.forEach {
                Text("  📌 $it", fontSize = 11.5.sp, color = Color(0xFF263238), modifier = Modifier.padding(top = 6.dp))
            }
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.padding(12.dp)) {
            TwButton("＋ New event", 0xFFC2185B, Modifier.fillMaxWidth()) { state.showToast("Event editor (demo)") }
        }
    }
}

// ---------------------------------------------------------------------------
// Memo
// ---------------------------------------------------------------------------

@Composable
fun MemoApp(state: EmulatorState) {
    val detail = state.appDetail
    if (detail != null && detail.startsWith("memo:")) {
        val key = detail.removePrefix("memo:")
        val idx = key.toIntOrNull()
        val existing = idx?.let { state.memos.getOrNull(it) }
        var title by remember(detail) { mutableStateOf(existing?.title ?: "") }
        var body by remember(detail) { mutableStateOf(existing?.body ?: "") }
        Column(Modifier.fillMaxSize().background(Color(0xFFFFFDE7))) {
            AppHeader(if (existing == null) "New memo" else "Edit memo", 0xFFFFF59D, 0xFFF9A825, onBack = { state.appDetail = null }, action = "Save", onAction = {
                val t = title.ifBlank { "Untitled" }
                if (existing == null) {
                    state.memos.add(0, Memo(t, body, formatDateShort(state.nowMillis)))
                    state.showToast("Memo saved")
                } else if (idx != null) {
                    state.memos[idx] = Memo(t, body, existing.date)
                    state.showToast("Memo updated")
                }
                state.appDetail = null
            })
            Column(Modifier.padding(14.dp)) {
                TwTextField(title, { title = it }, "Title", Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                TwTextField(body, { body = it }, "Write your memo…", Modifier.fillMaxWidth().height(150.dp))
                if (existing != null && idx != null) {
                    Spacer(Modifier.height(14.dp))
                    TwButton("Delete memo", 0xFFC62828, Modifier.fillMaxWidth()) {
                        state.memos.removeAt(idx)
                        state.appDetail = null
                        state.showToast("Memo deleted")
                    }
                }
            }
        }
        return
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFFFFDE7))) {
        AppHeader("Memo", 0xFFFFF59D, 0xFFF9A825, action = "＋ New", onAction = { state.appDetail = "memo:new" })
        if (state.memos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No memos yet — tap ＋ New", color = Color(0xFF8D6E00), fontSize = 12.sp)
            }
        }
        LazyColumn {
            items(state.memos.indices.toList()) { i ->
                val memo = state.memos[i]
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(6.dp)).background(Color(0xFFFFF9C4))
                        .border(1.dp, Color(0xFFF0E68C), RoundedCornerShape(6.dp))
                        .clickable { state.appDetail = "memo:$i" }
                        .padding(12.dp)
                ) {
                    Text(memo.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
                    Text(memo.body, fontSize = 11.5.sp, color = Color(0xFF5D4037), maxLines = 3, modifier = Modifier.padding(vertical = 4.dp))
                    Text(memo.date, fontSize = 9.sp, color = Color(0xFF9E8B5A))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// My Files / Task manager
// ---------------------------------------------------------------------------

@Composable
fun MyFilesApp(state: EmulatorState) {
    val detail = state.appDetail
    if (detail != null && detail.startsWith("f:")) {
        val folder = FILE_FOLDERS.getOrNull(detail.removePrefix("f:").toIntOrNull() ?: -1)
        if (folder != null) {
            Column(Modifier.fillMaxSize().background(Color.White)) {
                AppHeader("My Files / ${folder.first}", 0xFFFFCC80, 0xFFE65100, onBack = { state.appDetail = null })
                LazyColumn {
                    items(folder.second) { file ->
                        Row(Modifier.fillMaxWidth().clickable { state.showToast("Opening $file…") }.padding(horizontal = 14.dp, vertical = 11.dp)) {
                            Text(if (file.endsWith("/")) "📁" else "📄", fontSize = 16.sp, modifier = Modifier.width(32.dp))
                            Text(file, fontSize = 12.5.sp, color = Color(0xFF263238))
                        }
                    }
                }
            }
            return
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("My Files", 0xFFFFCC80, 0xFFE65100)
        Column(Modifier.padding(14.dp)) {
            Text("Internal storage · 11.2 GB free of 16 GB", fontSize = 11.sp, color = Color(0xFF37474F))
            Box(Modifier.fillMaxWidth().height(8.dp).padding(top = 3.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFECEFF1))) {
                Box(Modifier.fillMaxWidth(0.30f).height(8.dp).background(Color(0xFFFF9800), RoundedCornerShape(4.dp)))
            }
            Text("External SD · 7.4 GB free of 8 GB", fontSize = 11.sp, color = Color(0xFF37474F), modifier = Modifier.padding(top = 9.dp))
            Box(Modifier.fillMaxWidth().height(8.dp).padding(top = 3.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFECEFF1))) {
                Box(Modifier.fillMaxWidth(0.08f).height(8.dp).background(Color(0xFF43A047), RoundedCornerShape(4.dp)))
            }
        }
        LazyColumn {
            items(FILE_FOLDERS.indices.toList()) { i ->
                val (name, files) = FILE_FOLDERS[i]
                Row(Modifier.fillMaxWidth().clickable { state.appDetail = "f:$i" }.padding(horizontal = 14.dp, vertical = 11.dp)) {
                    Text("📁", fontSize = 18.sp, modifier = Modifier.width(36.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, fontSize = 13.sp, color = Color(0xFF212121))
                        Text("${files.size} items", fontSize = 10.sp, color = Color(0xFF78909C))
                    }
                    Text("›", fontSize = 17.sp, color = Color(0xFFB0BEC5))
                }
            }
        }
    }
}

@Composable
fun TaskManagerApp(state: EmulatorState) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F6F8))) {
        AppHeader("Task Manager", 0xFF80CBC4, 0xFF00695C)
        Column(Modifier.padding(14.dp)) {
            Text("Active applications: ${state.openedApps.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            Spacer(Modifier.height(8.dp))
            Text("RAM status", fontSize = 11.sp, color = Color(0xFF37474F))
            Box(Modifier.fillMaxWidth().height(10.dp).padding(top = 2.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFCFD8DC))) {
                Box(Modifier.fillMaxWidth(0.64f).height(10.dp).background(Color(0xFFFF9800), RoundedCornerShape(5.dp)))
            }
            Text("326 MB used / 512 MB total · CPU 18%", fontSize = 10.sp, color = Color(0xFF78909C), modifier = Modifier.padding(top = 3.dp))
            Spacer(Modifier.height(12.dp))
            if (state.openedApps.isEmpty()) {
                Text("No active applications.", fontSize = 11.5.sp, color = Color(0xFF78909C))
            } else {
                state.openedApps.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { id -> AppIcon(appById(id)) { state.openApp(id) } }
                        repeat(4 - row.size) { Spacer(Modifier.width(64.dp)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            TwButton("End all / Clear memory", 0xFFC62828, Modifier.fillMaxWidth()) {
                state.openedApps.clear()
                state.goHome()
                state.showToast("Memory cleared · 148 MB freed")
            }
            Spacer(Modifier.height(8.dp))
            TwButton("Package / Storage summary", 0xFF00695C, Modifier.fillMaxWidth()) {
                state.showToast("Apps: 312 MB · Media: 2.1 GB · Free: 11.2 GB")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FM Radio / Voice recorder
// ---------------------------------------------------------------------------

@Composable
fun FmRadioApp(state: EmulatorState) {
    var freq by remember { mutableStateOf(91.9f) }
    var on by remember { mutableStateOf(true) }
    val stations = listOf(88.1f to "KBS Classic FM", 91.9f to "MBC FM4U", 95.9f to "KBS Cool FM", 99.1f to "SBS Power FM", 103.5f to "CBS Music FM", 107.7f to "SBS Love FM")
    val station = stations.minByOrNull { kotlin.math.abs(it.first - freq) }
    Column(Modifier.fillMaxSize().background(Color(0xFF201612))) {
        AppHeader("FM Radio", 0xFFA1887F, 0xFF4E342E, action = if (on) "⏻ On" else "⏻ Off", onAction = { on = !on })
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎧 Headphones connected · antenna ready", color = Color(0xFFBCAAA4), fontSize = 10.sp)
            Spacer(Modifier.height(20.dp))
            Text("%.1f".format(freq), color = if (on) Color.White else Color(0xFF6D4C41), fontSize = 52.sp, fontWeight = FontWeight.Light)
            Text("MHz FM", color = Color(0xFFBCAAA4), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text(if (on) (station?.second ?: "— — —") else "Radio off", color = Color(0xFFFFCC80), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))
            Canvas(Modifier.fillMaxWidth().height(42.dp)) {
                val step = size.width / 24f
                for (i in 0..24) {
                    val h = if (i % 4 == 0) 30f else 16f
                    drawLine(Color(0xFF8D6E63), Offset(i * step, size.height), Offset(i * step, size.height - h), strokeWidth = 3f)
                }
                val pos = ((freq - 87.5f) / 20.5f) * size.width
                drawLine(Color(0xFFFF9800), Offset(pos, 0f), Offset(pos, size.height), strokeWidth = 4f)
            }
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TwButton("⏮ Seek", 0xFF5D4037, Modifier.width(84.dp)) { freq = (freq - 0.9f).coerceAtLeast(87.5f); state.showToast("Seeking down…") }
                TwButton("－", 0xFF37474F, Modifier.width(54.dp)) { freq = (freq - 0.1f).coerceAtLeast(87.5f) }
                TwButton("＋", 0xFF37474F, Modifier.width(54.dp)) { freq = (freq + 0.1f).coerceAtMost(108.0f) }
                TwButton("Seek ⏭", 0xFF5D4037, Modifier.width(84.dp)) { freq = (freq + 0.9f).coerceAtMost(108.0f); state.showToast("Seeking up…") }
            }
            Spacer(Modifier.height(14.dp))
            LazyColumn {
                items(stations) { (f, name) ->
                    Row(Modifier.fillMaxWidth().clickable { freq = f; on = true }.padding(vertical = 7.dp)) {
                        Text("%.1f".format(f), color = Color(0xFFFFCC80), fontSize = 12.5.sp, modifier = Modifier.width(52.dp))
                        Text(name, color = Color.White, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceRecorderApp(state: EmulatorState) {
    var recording by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }
    val recordings = remember { mutableStateListOf("Voice 001 · 0:42", "Voice 002 · 1:17") }
    LaunchedEffect(recording) {
        while (recording) { delay(1000); seconds++ }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF181818))) {
        AppHeader("Voice Recorder", 0xFFEF9A9A, 0xFFB71C1C)
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))
            Text(if (recording) "● Recording" else "Ready", color = if (recording) Color(0xFFEF5350) else Color(0xFFBDBDBD), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(formatDuration(seconds), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(14.dp))
            Canvas(Modifier.fillMaxWidth().height(56.dp)) {
                val bars = 26
                val step = size.width / bars
                for (i in 0 until bars) {
                    val level = if (recording) (kotlin.math.sin((seconds * 3 + i).toDouble()) * 0.5 + 0.55).toFloat() else 0.14f
                    val h = size.height * level.coerceIn(0.08f, 1f)
                    drawLine(Color(0xFFEF5350), Offset(i * step + step / 2, (size.height - h) / 2), Offset(i * step + step / 2, (size.height + h) / 2), strokeWidth = 5f)
                }
            }
            Spacer(Modifier.height(22.dp))
            Box(
                Modifier.size(66.dp).clip(CircleShape).background(if (recording) Color(0xFF616161) else Color(0xFFD32F2F))
                    .clickable {
                        if (recording) {
                            recording = false
                            recordings.add(0, "Voice ${recordings.size + 1} · ${formatDuration(seconds)}")
                            state.showToast("Recording saved to /Sounds")
                            seconds = 0
                        } else {
                            recording = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) { Text(if (recording) "⏹" else "⏺", color = Color.White, fontSize = 25.sp) }
            Text(if (recording) "Tap to stop & save" else "Tap to record", color = Color(0xFF9E9E9E), fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(18.dp))
            LazyColumn {
                items(recordings) { r ->
                    Row(Modifier.fillMaxWidth().clickable { state.showToast("Playing $r (demo)") }.padding(vertical = 8.dp)) {
                        Text("▶", color = Color(0xFFEF5350), fontSize = 14.sp, modifier = Modifier.width(30.dp))
                        Text(r, color = Color.White, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// AllShare / ThinkFree Office / Mini Diary
// ---------------------------------------------------------------------------

@Composable
fun AllShareApp(state: EmulatorState) {
    var scanning by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1400); scanning = false }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("AllShare — DLNA", 0xFF9FA8DA, 0xFF283593, action = "↻ Scan", onAction = { scanning = true })
        Column(Modifier.padding(16.dp)) {
            Text(if (scanning) "Searching for DLNA devices on GalaxyHome…" else "Devices found on GalaxyHome:", fontSize = 12.sp, color = Color(0xFF37474F))
            Spacer(Modifier.height(12.dp))
            if (!scanning) {
                listOf("📺  Living Room TV (Samsung LED)", "💻  DESKTOP-SEOUL", "📱  Galaxy Tab").forEach { device ->
                    Row(
                        Modifier.fillMaxWidth().clickable { state.showToast("Streaming to $device (demo)") }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(device, fontSize = 12.5.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                        Text("Play ›", fontSize = 11.sp, color = Color(0xFF3949AB), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Share photos, music and videos from this GT-I9000 over Wi-Fi — 2010 style.", fontSize = 10.5.sp, color = Color(0xFF78909C))
        }
    }
}

@Composable
fun ThinkFreeApp(state: EmulatorState) {
    val docs = listOf(
        Triple("Galaxy S Launch Brief.docx", "Word", "Product announcement and key specs for the GT-I9000 launch event."),
        Triple("Q3 Sales Forecast.xls", "Excel", "Galaxy S shipments: South Korea 1M, Europe 2.4M, US 3.1M (forecast)."),
        Triple("TouchWiz 3.0 Overview.ppt", "PowerPoint", "7 home screens · Social Hub · Swype · Daily Briefing widgets.")
    )
    val detail = state.appDetail
    if (detail != null && detail.startsWith("doc:")) {
        val doc = docs.getOrNull(detail.removePrefix("doc:").toIntOrNull() ?: -1)
        if (doc != null) {
            Column(Modifier.fillMaxSize().background(Color.White)) {
                AppHeader(doc.first, 0xFF80DEEA, 0xFF00838F, onBack = { state.appDetail = null })
                Column(Modifier.padding(16.dp)) {
                    Text(doc.second + " document", fontSize = 10.sp, color = Color(0xFF00838F), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(doc.first, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                    Spacer(Modifier.height(10.dp))
                    Text(doc.third, fontSize = 12.5.sp, color = Color(0xFF37474F))
                }
            }
            return
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("ThinkFree Office", 0xFF80DEEA, 0xFF00838F)
        LazyColumn {
            items(docs.indices.toList()) { i ->
                val doc = docs[i]
                Row(Modifier.fillMaxWidth().clickable { state.appDetail = "doc:$i" }.padding(horizontal = 14.dp, vertical = 11.dp)) {
                    Text("📄", fontSize = 18.sp, modifier = Modifier.width(36.dp))
                    Column {
                        Text(doc.first, fontSize = 12.5.sp, color = Color(0xFF212121))
                        Text(doc.second + " · My Documents", fontSize = 10.sp, color = Color(0xFF78909C))
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryApp(state: EmulatorState) {
    var entry by remember { mutableStateOf("First day with the Galaxy S. That Super AMOLED screen! Took 40 photos already.") }
    Column(Modifier.fillMaxSize().background(Color(0xFFFDF6EC))) {
        AppHeader("Mini Diary", 0xFFCE93D8, 0xFF6A1B9A)
        LazyColumn(Modifier.padding(16.dp)) {
            item {
                Text(formatDate(state.nowMillis), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                Text("Seoul · ☀ 24°C · photos from today", fontSize = 10.5.sp, color = Color(0xFF8E6E8E))
                Spacer(Modifier.height(10.dp))
                PhotoScene(state.photos.firstOrNull() ?: 101, Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(Modifier.height(12.dp))
                TwTextField(entry, { entry = it }, "Today's entry…", Modifier.fillMaxWidth().height(110.dp))
                Spacer(Modifier.height(12.dp))
                TwButton("Save diary", 0xFF8E24AA, Modifier.fillMaxWidth()) { state.showToast("Mini diary saved") }
            }
        }
    }
}
