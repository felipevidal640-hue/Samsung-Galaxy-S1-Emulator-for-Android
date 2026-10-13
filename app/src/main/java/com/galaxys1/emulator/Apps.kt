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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Dispatcher
// ---------------------------------------------------------------------------

@Composable
fun EmulatedApp(state: EmulatorState) {
    when (state.currentApp) {
        "phone" -> PhoneApp(state)
        "contacts" -> ContactsApp(state)
        "messaging" -> MessagingApp(state)
        "browser" -> BrowserApp(state)
        "camera" -> CameraApp(state)
        "gallery" -> GalleryApp(state)
        "music" -> MusicApp(state)
        "video" -> VideoApp(state)
        "settings" -> SettingsApp(state)
        else -> MoreApp(state)
    }
}

// ---------------------------------------------------------------------------
// Shared TouchWiz app chrome
// ---------------------------------------------------------------------------

@Composable
fun AppHeader(
    title: String,
    colorTop: Long = 0xFF5B7A99,
    colorBottom: Long = 0xFF33506E,
    onBack: (() -> Unit)? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(Brush.verticalGradient(listOf(Color(colorTop), Color(colorBottom))))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Text("‹", color = Color.White, fontSize = 24.sp, modifier = Modifier.clickable { onBack() }.padding(end = 8.dp))
        }
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable { onAction?.invoke() }
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
fun TwTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = Color(0xFF212121), fontSize = 13.sp),
        cursorBrush = SolidColor(Color(0xFF1976D2)),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFB0BEC5), RoundedCornerShape(6.dp))
            .padding(horizontal = 9.dp, vertical = 8.dp),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) Text(hint, color = Color(0xFF9E9E9E), fontSize = 13.sp)
                inner()
            }
        }
    )
}

@Composable
fun TwButton(text: String, color: Long, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(listOf(Color(color).copy(alpha = 0.85f), Color(color))))
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun Avatar(name: String, color: Long, size: Int = 42) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(color).copy(alpha = 0.8f), Color(color)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.trim().firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontSize = (size * 0.42f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Deterministic little "photo" drawn from a seed — used by Camera/Gallery/Video. */
@Composable
fun PhotoScene(seed: Int, modifier: Modifier = Modifier) {
    val skies = listOf(
        listOf(0xFF7EC8E3, 0xFFDFF6FF), listOf(0xFFFFB75E, 0xFFFFE3B3),
        listOf(0xFF5B7BC0, 0xFFB9C9F2), listOf(0xFF66BB6A, 0xFFE8F5E9)
    )
    val sky = skies[kotlin.math.abs(seed) % skies.size]
    val sunX = 0.2f + (kotlin.math.abs(seed * 37) % 60) / 100f
    val hill = 0.45f + (kotlin.math.abs(seed * 13) % 20) / 100f
    Canvas(modifier.background(Color(sky[1]))) {
        drawRect(Brush.verticalGradient(listOf(Color(sky[0]), Color(sky[1]))), size = size)
        drawCircle(Color(0xFFFFF59D), radius = size.width * 0.09f, center = Offset(size.width * sunX, size.height * 0.26f))
        drawCircle(Color.White.copy(alpha = 0.85f), radius = size.width * 0.07f, center = Offset(size.width * 0.72f, size.height * 0.18f))
        drawCircle(Color.White.copy(alpha = 0.7f), radius = size.width * 0.055f, center = Offset(size.width * 0.80f, size.height * 0.20f))
        val back = Path().apply {
            moveTo(0f, size.height * (hill + 0.12f))
            lineTo(size.width * 0.38f, size.height * hill)
            lineTo(size.width * 0.66f, size.height * (hill + 0.14f))
            lineTo(size.width, size.height * (hill + 0.02f))
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(back, Color(0xFF78909C))
        val front = Path().apply {
            moveTo(0f, size.height * 0.78f)
            quadraticBezierTo(size.width * 0.5f, size.height * 0.58f, size.width, size.height * 0.80f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(front, Color(0xFF558B2F))
    }
}

fun formatDuration(totalSec: Int): String = "%d:%02d".format(totalSec / 60, totalSec % 60)

// ---------------------------------------------------------------------------
// Phone — TouchWiz dialer with a live call screen
// ---------------------------------------------------------------------------

@Composable
fun PhoneApp(state: EmulatorState) {
    var tab by remember { mutableStateOf(0) }
    var number by remember { mutableStateOf("") }

    if (state.appDetail == "call") {
        CallScreen(state, number.ifEmpty { "Unknown" })
        return
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F6F8))) {
        AppHeader("Phone", 0xFF66BB6A, 0xFF2E7D32)
        Row(Modifier.fillMaxWidth().background(Color(0xFFE8ECEF))) {
            listOf("Dialer", "Call log", "Contacts", "Favourites").forEachIndexed { i, label ->
                Box(
                    Modifier.weight(1f)
                        .background(if (tab == i) Color.White else Color(0xFFE8ECEF))
                        .clickable { tab = i }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 10.5.sp, color = if (tab == i) Color(0xFF2E7D32) else Color(0xFF607D8B), fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        when (tab) {
            0 -> {
                Box(
                    Modifier.fillMaxWidth().height(58.dp).background(Color.White).padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(number.ifEmpty { "Enter number" }, fontSize = if (number.isEmpty()) 15.sp else 27.sp, color = if (number.isEmpty()) Color(0xFFB0BEC5) else Color(0xFF212121))
                }
                val keys = listOf(
                    Triple("1", "", "1"), Triple("2", "ABC", "2"), Triple("3", "DEF", "3"),
                    Triple("4", "GHI", "4"), Triple("5", "JKL", "5"), Triple("6", "MNO", "6"),
                    Triple("7", "PQRS", "7"), Triple("8", "TUV", "8"), Triple("9", "WXYZ", "9"),
                    Triple("*", "", "*"), Triple("0", "+", "0"), Triple("#", "", "#")
                )
                Column(Modifier.weight(1f).padding(6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    keys.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            row.forEach { (main, sub, value) ->
                                Column(
                                    Modifier.weight(1f).height(46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE3E8EC))))
                                        .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(6.dp))
                                        .clickable { number += value },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(main, fontSize = 19.sp, color = Color(0xFF263238), fontWeight = FontWeight.Medium)
                                    if (sub.isNotEmpty()) Text(sub, fontSize = 7.sp, color = Color(0xFF90A4AE))
                                }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TwButton("✆  Call", 0xFF43A047, Modifier.weight(1f)) {
                        if (number.isEmpty()) state.showToast("Enter a number first")
                        else state.appDetail = "call"
                    }
                    TwButton("⌫", 0xFF78909C, Modifier.width(58.dp)) { number = number.dropLast(1) }
                    TwButton("🎥 Video", 0xFF5E35B1, Modifier.weight(0.7f)) { state.showToast("Video calling is not available on this network") }
                }
            }
            1 -> LazyColumn(Modifier.weight(1f).background(Color.White)) {
                items(
                    listOf(
                        Triple("Ji-Woo Han", "Incoming · 09:32", "↙"),
                        Triple("+82 10-7788-2010", "Missed · Yesterday", "✖"),
                        Triple("Anna Kim", "Outgoing · Yesterday", "↗")
                    )
                ) { (name, sub, glyph) ->
                    Row(
                        Modifier.fillMaxWidth().clickable { number = CONTACTS.firstOrNull { it.name == name }?.phone ?: number }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(glyph, color = Color(0xFF2E7D32), fontSize = 15.sp, modifier = Modifier.width(28.dp))
                        Column {
                            Text(name, fontSize = 13.sp, color = Color(0xFF212121))
                            Text(sub, fontSize = 10.5.sp, color = Color(0xFF78909C))
                        }
                    }
                }
            }
            2 -> Box(Modifier.weight(1f).background(Color.White), contentAlignment = Alignment.Center) {
                TwButton("Open Contacts", 0xFF2E7D32, Modifier.padding(20.dp)) { state.openApp("contacts") }
            }
            else -> LazyColumn(Modifier.weight(1f).background(Color.White)) {
                items(CONTACTS.take(3)) { c ->
                    Row(
                        Modifier.fillMaxWidth().clickable { number = c.phone; tab = 0 }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(c.name, c.color, 38)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(c.name, fontSize = 13.sp, color = Color(0xFF212121))
                            Text("★ Favourite · ${c.phone}", fontSize = 10.5.sp, color = Color(0xFF78909C))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallScreen(state: EmulatorState, number: String) {
    var seconds by remember { mutableStateOf(0) }
    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) { delay(1000); seconds++ }
    }
    val contact = CONTACTS.firstOrNull { it.phone == number }
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF263238), Color(0xFF10181D)))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(26.dp))
        Avatar(contact?.name ?: number, contact?.color ?: 0xFF546E7A, 76)
        Spacer(Modifier.height(12.dp))
        Text(contact?.name ?: number, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
        Text(if (contact != null) number else "Mobile", color = Color(0xFFB0BEC5), fontSize = 11.5.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            if (seconds < 2) "Calling…" else "Connected · ${formatDuration(seconds)}",
            color = Color(0xFF9CCC65), fontSize = 13.sp
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            CallControl(if (muted) "🔇" else "🎤", "Mute", muted) { muted = !muted }
            CallControl("🔊", "Speaker", speaker) { speaker = !speaker }
            CallControl("⌨", "Keypad", false) { state.showToast("DTMF keypad") }
            CallControl("＋", "Add call", false) { state.showToast("Conference calling unavailable") }
        }
        Spacer(Modifier.height(22.dp))
        Box(
            Modifier.size(62.dp).clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828))))
                .clickable {
                    state.appDetail = null
                    state.showToast("Call ended · ${formatDuration(seconds)}")
                },
            contentAlignment = Alignment.Center
        ) { Text("☎", color = Color.White, fontSize = 26.sp) }
        Text("End call", color = Color(0xFFB0BEC5), fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CallControl(glyph: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(50.dp).clip(CircleShape)
                .background(if (active) Color(0xFF43A047) else Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) { Text(glyph, fontSize = 19.sp, color = Color.White) }
        Text(label, color = Color(0xFFCFD8DC), fontSize = 9.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

// ---------------------------------------------------------------------------
// Contacts
// ---------------------------------------------------------------------------

@Composable
fun ContactsApp(state: EmulatorState) {
    val detail = state.appDetail
    if (detail != null && detail.startsWith("c:")) {
        val c = CONTACTS.getOrNull(detail.removePrefix("c:").toIntOrNull() ?: -1)
        if (c != null) {
            Column(Modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
                AppHeader("Contact", 0xFFFFB74D, 0xFFF57C00, onBack = { state.appDetail = null })
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Avatar(c.name, c.color, 76)
                    Spacer(Modifier.height(10.dp))
                    Text(c.name, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                    Text(c.group, fontSize = 11.sp, color = Color(0xFF78909C))
                }
                DetailRow("📞", c.phone, "Mobile")
                DetailRow("✉", c.email, "Email")
                DetailRow("⭐", "Add to favourites", "")
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TwButton("✆ Call", 0xFF43A047, Modifier.weight(1f)) { state.openApp("phone") }
                    TwButton("✉ Message", 0xFFFFA000, Modifier.weight(1f)) {
                        val t = state.threads.firstOrNull { it.contact == c.name }
                        if (t != null) {
                            state.openApp("messaging")
                            state.appDetail = "t:${t.id}"
                        } else state.showToast("No conversation with ${c.name} yet")
                    }
                }
            }
            return
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("Contacts", 0xFFFFB74D, 0xFFF57C00, action = "＋ New", onAction = { state.showToast("New contacts are saved to the phone (demo)") })
        LazyColumn {
            items(CONTACTS.indices.toList()) { i ->
                val c = CONTACTS[i]
                Row(
                    Modifier.fillMaxWidth().clickable { state.appDetail = "c:$i" }.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(c.name, c.color)
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text(c.name, fontSize = 13.5.sp, color = Color(0xFF212121), fontWeight = FontWeight.Medium)
                        Text(c.phone, fontSize = 10.5.sp, color = Color(0xFF78909C))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(glyph: String, main: String, sub: String) {
    Row(
        Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(glyph, fontSize = 16.sp, modifier = Modifier.width(34.dp))
        Column {
            Text(main, fontSize = 13.sp, color = Color(0xFF212121))
            if (sub.isNotEmpty()) Text(sub, fontSize = 10.sp, color = Color(0xFF90A4AE))
        }
    }
}

// ---------------------------------------------------------------------------
// Messaging
// ---------------------------------------------------------------------------

@Composable
fun MessagingApp(state: EmulatorState) {
    val scope = rememberCoroutineScope()
    val detail = state.appDetail
    if (detail != null && detail.startsWith("t:")) {
        val thread = state.threads.firstOrNull { it.id == (detail.removePrefix("t:").toIntOrNull() ?: -1) }
        if (thread != null) {
            var input by remember { mutableStateOf("") }
            Column(Modifier.fillMaxSize().background(Color(0xFFF2F4F5))) {
                AppHeader(thread.contact, 0xFFFFD54F, 0xFFFF8F00, onBack = { state.appDetail = null }, action = "✆ Call", onAction = { state.openApp("phone") })
                LazyColumn(
                    Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    items(thread.messages) { m ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromMe) Arrangement.End else Arrangement.Start) {
                            Column(
                                Modifier.width(230.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (m.fromMe) Color(0xFFC5E1A5) else Color.White)
                                    .border(1.dp, Color(0xFFD5DBDB), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Text(m.text, fontSize = 12.5.sp, color = Color(0xFF263238))
                                Text(m.time, fontSize = 8.5.sp, color = Color(0xFF90A4AE), textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().background(Color.White).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TwTextField(input, { input = it }, "Type to compose", Modifier.weight(1f))
                    Spacer(Modifier.width(7.dp))
                    TwButton("Send", 0xFFFFA000, Modifier.width(64.dp)) {
                        val text = input.trim()
                        if (text.isNotEmpty()) {
                            thread.messages.add(ChatMessage(true, text, formatTime(state.nowMillis)))
                            input = ""
                            scope.launch {
                                delay(1300)
                                thread.messages.add(
                                    ChatMessage(false, AUTO_REPLIES[(thread.id + thread.messages.size) % AUTO_REPLIES.size], formatTime(state.nowMillis))
                                )
                            }
                        }
                    }
                }
            }
            return
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("Messaging", 0xFFFFD54F, 0xFFFF8F00, action = "✎ New", onAction = { state.showToast("Pick a contact to start a conversation") })
        LazyColumn {
            items(state.threads) { t ->
                val last = t.messages.lastOrNull()
                Row(
                    Modifier.fillMaxWidth().clickable { state.appDetail = "t:${t.id}" }.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(t.contact, t.color)
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.contact, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        Text(last?.text ?: "", fontSize = 11.sp, color = Color(0xFF78909C), maxLines = 1)
                    }
                    Text(last?.time ?: "", fontSize = 9.5.sp, color = Color(0xFF90A4AE))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Internet (browser)
// ---------------------------------------------------------------------------

@Composable
fun BrowserApp(state: EmulatorState) {
    var url by remember { mutableStateOf("www.google.com") }
    var page by remember { mutableStateOf("google") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun navigate(target: String) {
        page = target
        url = when (target) {
            "google" -> "www.google.com"
            "samsung" -> "www.samsung.com/galaxy"
            "news" -> "news.example.com/2010"
            else -> url
        }
        loading = true
        scope.launch { delay(750); loading = false }
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF37474F)).padding(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔒", fontSize = 12.sp)
            Spacer(Modifier.width(5.dp))
            TwTextField(url, { url = it }, "Enter URL", Modifier.weight(1f))
            Spacer(Modifier.width(6.dp))
            TwButton("Go", 0xFF1976D2, Modifier.width(46.dp)) {
                navigate(
                    when {
                        url.contains("samsung", true) -> "samsung"
                        url.contains("news", true) -> "news"
                        else -> "google"
                    }
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFE0E0E0))) {
            if (loading) Box(Modifier.fillMaxWidth(0.6f).height(3.dp).background(Color(0xFFFF9800)))
        }
        LazyColumn(Modifier.weight(1f).padding(16.dp)) {
            item {
                when (page) {
                    "samsung" -> BrowserPage(
                        "Samsung Galaxy S — GT-I9000",
                        listOf(
                            "4.0-inch Super AMOLED (480 × 800) with mDNIe image engine.",
                            "1 GHz Samsung Hummingbird (Cortex-A8) with PowerVR SGX540 graphics.",
                            "5 MP camera with 720p HD video at 30 fps. Android 2.3 Gingerbread, TouchWiz 3.0.",
                            "16 GB storage + microSD up to 32 GB · 512 MB RAM · 1500 mAh battery."
                        )
                    ) { navigate("google") }
                    "news" -> BrowserPage(
                        "Galaxy S tops 1 million sales",
                        listOf(
                            "Samsung's Galaxy S has become one of the fastest-selling Android phones of 2010.",
                            "Reviewers praise the Super AMOLED display: \"blacks are truly black, even in sunlight\".",
                            "Android Market passes 100,000 applications as Gingerbread rolls out."
                        )
                    ) { navigate("samsung") }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Google",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3366CC)
                            )
                            Spacer(Modifier.height(10.dp))
                            TwTextField("", {}, "Search Google", Modifier.fillMaxWidth())
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TwButton("Samsung Galaxy S", 0xFF1976D2, Modifier.weight(1f)) { navigate("samsung") }
                                TwButton("2010 Tech News", 0xFF00897B, Modifier.weight(1f)) { navigate("news") }
                            }
                            Spacer(Modifier.height(18.dp))
                            Text("Bookmarks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F), modifier = Modifier.fillMaxWidth())
                            listOf("www.samsung.com/galaxy" to "samsung", "news.example.com/2010" to "news").forEach { (label, target) ->
                                Text(
                                    "⭐  $label",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1565C0),
                                    modifier = Modifier.fillMaxWidth().clickable { navigate(target) }.padding(vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().background(Color(0xFFECEFF1)).padding(horizontal = 16.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("‹ Back", fontSize = 11.sp, color = Color(0xFF37474F), modifier = Modifier.clickable { navigate("google") })
            Text("▦ Windows", fontSize = 11.sp, color = Color(0xFF37474F), modifier = Modifier.clickable { state.showToast("1 window open") })
            Text("☆ Bookmark", fontSize = 11.sp, color = Color(0xFF37474F), modifier = Modifier.clickable { state.showToast("Bookmark added") })
        }
    }
}

@Composable
private fun BrowserPage(title: String, paragraphs: List<String>, onNext: () -> Unit) {
    Column {
        Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Spacer(Modifier.height(10.dp))
        paragraphs.forEach { p ->
            Text(p, fontSize = 12.5.sp, color = Color(0xFF37474F), modifier = Modifier.padding(bottom = 9.dp))
        }
        TwButton("Continue reading →", 0xFF1976D2, Modifier.fillMaxWidth(), onNext)
    }
}

// ---------------------------------------------------------------------------
// Camera + Gallery
// ---------------------------------------------------------------------------

@Composable
fun CameraApp(state: EmulatorState) {
    val scope = rememberCoroutineScope()
    var flashOn by remember { mutableStateOf(true) }
    var showFlash by remember { mutableStateOf(false) }
    var lastSeed by remember { mutableStateOf(state.photos.firstOrNull() ?: 101) }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            PhotoScene(lastSeed, Modifier.fillMaxSize())
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CameraChip(if (flashOn) "⚡ Flash on" else "⚡ Flash off") { flashOn = !flashOn }
                CameraChip("⇋ Front") { state.showToast("Switched to front camera (1.3 MP)") }
                CameraChip("HD 720p") { state.showToast("Video mode: 1280×720 @ 30fps") }
            }
            if (showFlash) Box(Modifier.fillMaxSize().background(Color.White))
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Seoul, South Korea  ·  GPS on", color = Color.White, fontSize = 9.5.sp)
            }
        }
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF14181D)).padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(6.dp)).clickable { state.openApp("gallery") }
            ) { PhotoScene(lastSeed, Modifier.fillMaxSize()) }
            Box(
                Modifier.size(64.dp).clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFFECEFF1), Color(0xFF90A4AE))))
                    .border(3.dp, Color(0xFF455A64), CircleShape)
                    .clickable {
                        scope.launch {
                            if (flashOn) { showFlash = true; delay(110); showFlash = false }
                            val seed = (System.currentTimeMillis() % 997).toInt() + 40
                            lastSeed = seed
                            state.photos.add(0, seed)
                            state.showToast("Saved to /DCIM/Camera · ${state.photos.size} photos")
                        }
                    },
                contentAlignment = Alignment.Center
            ) { Text("📸", fontSize = 25.sp) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Auto", color = Color.White, fontSize = 10.sp)
                Text("5.0 MP", color = Color(0xFF90A4AE), fontSize = 8.5.sp)
            }
        }
    }
}

@Composable
private fun CameraChip(text: String, onClick: () -> Unit) {
    Text(
        text,
        color = Color.White,
        fontSize = 9.5.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

@Composable
fun GalleryApp(state: EmulatorState) {
    val detail = state.appDetail
    if (detail != null && detail.startsWith("p:")) {
        val idx = detail.removePrefix("p:").toIntOrNull() ?: 0
        val seed = state.photos.getOrNull(idx)
        if (seed != null) {
            Column(Modifier.fillMaxSize().background(Color.Black)) {
                AppHeader("IMG_${1000 + idx}.jpg", 0xFF37474F, 0xFF14181D, onBack = { state.appDetail = null }, action = "🗑 Delete", onAction = {
                    state.photos.removeAt(idx)
                    state.appDetail = null
                    state.showToast("Photo deleted")
                })
                PhotoScene(seed, Modifier.weight(1f).fillMaxWidth())
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TwButton("‹ Prev", 0xFF37474F, Modifier.width(86.dp)) {
                        if (idx > 0) state.appDetail = "p:${idx - 1}"
                    }
                    Text("${idx + 1} / ${state.photos.size}", color = Color.White, fontSize = 12.sp)
                    TwButton("Next ›", 0xFF37474F, Modifier.width(86.dp)) {
                        if (idx < state.photos.lastIndex) state.appDetail = "p:${idx + 1}"
                    }
                }
            }
            return
        }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF101216))) {
        AppHeader("Gallery — Camera (${state.photos.size})", 0xFF37474F, 0xFF14181D)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.photos.indices.toList()) { i ->
                Box(Modifier.height(88.dp).clickable { state.appDetail = "p:$i" }) {
                    PhotoScene(state.photos[i], Modifier.fillMaxSize())
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Music + Video
// ---------------------------------------------------------------------------

@Composable
fun MusicApp(state: EmulatorState) {
    var trackIndex by remember { mutableStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(37f) }
    val track = TRACKS[trackIndex]

    LaunchedEffect(playing, trackIndex) {
        while (playing) {
            delay(1000)
            progress += 1f
            if (progress >= track.durationSec) {
                progress = 0f
                trackIndex = (trackIndex + 1) % TRACKS.size
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF17111F))) {
        AppHeader("Music Player", 0xFF8E24AA, 0xFF4A145E)
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(168.dp).clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(track.color).copy(alpha = 0.75f), Color(track.color)))),
                contentAlignment = Alignment.Center
            ) { Text("♪", color = Color.White, fontSize = 74.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(12.dp))
            Text(track.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("${track.artist} · ${track.album}", color = Color(0xFFCE93D8), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Slider(value = progress, onValueChange = { progress = it }, valueRange = 0f..track.durationSec.toFloat())
            Row(Modifier.fillMaxWidth()) {
                Text(formatDuration(progress.toInt()), color = Color(0xFFB39DDB), fontSize = 10.sp)
                Spacer(Modifier.weight(1f))
                Text(formatDuration(track.durationSec), color = Color(0xFFB39DDB), fontSize = 10.sp)
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏮", fontSize = 25.sp, color = Color.White, modifier = Modifier.clickable { trackIndex = (trackIndex - 1 + TRACKS.size) % TRACKS.size; progress = 0f })
                Box(
                    Modifier.size(58.dp).clip(CircleShape).background(Color(0xFFAB47BC)).clickable { playing = !playing },
                    contentAlignment = Alignment.Center
                ) { Text(if (playing) "⏸" else "▶", fontSize = 25.sp, color = Color.White) }
                Text("⏭", fontSize = 25.sp, color = Color.White, modifier = Modifier.clickable { trackIndex = (trackIndex + 1) % TRACKS.size; progress = 0f })
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            items(TRACKS.indices.toList()) { i ->
                val t = TRACKS[i]
                Row(
                    Modifier.fillMaxWidth()
                        .background(if (i == trackIndex) Color(0xFF2A1D38) else Color.Transparent)
                        .clickable { trackIndex = i; progress = 0f; playing = true }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(5.dp)).background(Color(t.color)), contentAlignment = Alignment.Center) {
                        Text("♪", color = Color.White, fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.title, color = Color.White, fontSize = 12.5.sp)
                        Text(t.artist, color = Color(0xFF9E9E9E), fontSize = 10.sp)
                    }
                    Text(formatDuration(t.durationSec), color = Color(0xFF9E9E9E), fontSize = 10.5.sp)
                }
            }
        }
    }
}

@Composable
fun VideoApp(state: EmulatorState) {
    val detail = state.appDetail
    if (detail != null && detail.startsWith("v:")) {
        val v = VIDEOS.getOrNull(detail.removePrefix("v:").toIntOrNull() ?: -1)
        if (v != null) {
            var playing by remember { mutableStateOf(true) }
            Column(Modifier.fillMaxSize().background(Color.Black)) {
                AppHeader("Video Player", 0xFF37474F, 0xFF14181D, onBack = { state.appDetail = null })
                Box(Modifier.fillMaxWidth().height(220.dp)) {
                    PhotoScene(300 + (detail.removePrefix("v:").toIntOrNull() ?: 0) * 71, Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().clickable { playing = !playing }, contentAlignment = Alignment.Center) {
                        if (!playing) Text("▶", color = Color.White, fontSize = 52.sp)
                    }
                    Text("HD 720p", color = Color.White, fontSize = 9.sp, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
                }
                Column(Modifier.padding(14.dp)) {
                    Text(v.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("${v.source} · ${v.views} views · ${v.duration}", color = Color(0xFF9E9E9E), fontSize = 10.5.sp)
                    Spacer(Modifier.height(14.dp))
                    TwButton(if (playing) "⏸ Pause" else "▶ Play", 0xFF0288D1, Modifier.fillMaxWidth()) { playing = !playing }
                }
            }
            return
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppHeader("Video Player", 0xFF4FC3F7, 0xFF0277BD)
        LazyColumn {
            items(VIDEOS.indices.toList()) { i ->
                val v = VIDEOS[i]
                Row(
                    Modifier.fillMaxWidth().clickable { state.appDetail = "v:$i" }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.width(96.dp).height(58.dp).clip(RoundedCornerShape(5.dp))) {
                        PhotoScene(300 + i * 71, Modifier.fillMaxSize())
                        Text("▶", color = Color.White, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
                        Text(v.duration, color = Color.White, fontSize = 8.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(v.title, fontSize = 12.5.sp, color = Color(0xFF212121), fontWeight = FontWeight.Medium)
                        Text("${v.source} · ${v.views} views", fontSize = 10.sp, color = Color(0xFF78909C))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Settings
// ---------------------------------------------------------------------------

@Composable
fun SettingsApp(state: EmulatorState) {
    when (state.appDetail) {
        "set:wireless" -> SettingsDetail(state, "Wireless and network") {
            SwitchRow("Wi-Fi", "Connected to GalaxyHome", state.wifiOn) { state.wifiOn = it }
            SwitchRow("Bluetooth", "Bluetooth 3.0 + HS", state.bluetoothOn) { state.bluetoothOn = it }
            SwitchRow("Flight mode", "Disable all wireless connections", state.flightMode) { state.flightMode = it }
            SwitchRow("GPS satellites", "Use GPS for location", state.gpsOn) { state.gpsOn = it }
            SwitchRow("Auto rotation", "Rotate screen automatically", state.autoRotateOn) { state.autoRotateOn = it }
        }
        "set:sound" -> SettingsDetail(state, "Sound and display — Sound") {
            Column(Modifier.padding(14.dp)) {
                Text("Phone volume  (${state.volume}/15)", fontSize = 12.5.sp, color = Color(0xFF37474F))
                Slider(
                    value = state.volume.toFloat(),
                    onValueChange = { state.volume = it.toInt(); state.volumeHudVisible = true; state.volumeHudSeq++ },
                    valueRange = 0f..15f,
                    steps = 14
                )
            }
            SwitchRow("Silent mode", "All sounds off except media", !state.soundOn) { state.soundOn = !it }
            SwitchRow("Phone vibration", "Vibrate on calls and alerts", true) { state.showToast("Vibration setting saved") }
            SwitchRow("Touch tones", "Audible touch feedback", state.soundOn) { state.soundOn = it }
        }
        "set:display" -> SettingsDetail(state, "Sound and display — Display") {
            Column(Modifier.padding(14.dp)) {
                Text("Brightness  ${(state.brightness * 100).toInt()}%", fontSize = 12.5.sp, color = Color(0xFF37474F))
                Slider(value = state.brightness, onValueChange = { state.brightness = it }, valueRange = 0.25f..1f)
                Spacer(Modifier.height(10.dp))
                Text("Wallpaper", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                Spacer(Modifier.height(6.dp))
                WALLPAPERS.forEachIndexed { i, wp ->
                    Row(
                        Modifier.fillMaxWidth()
                            .background(if (i == state.wallpaperIndex) Color(0xFFE3F2FD) else Color.White)
                            .clickable {
                                state.wallpaperIndex = i
                                state.showToast("Wallpaper set: ${wp.name}")
                            }
                            .padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp, 26.dp).clip(RoundedCornerShape(4.dp))
                                .background(Brush.verticalGradient(wp.colors.map { Color(it) }))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(wp.name, fontSize = 12.5.sp, color = Color(0xFF263238))
                        if (i == state.wallpaperIndex) {
                            Spacer(Modifier.weight(1f))
                            Text("✓ Applied", fontSize = 10.5.sp, color = Color(0xFF1976D2))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SwitchRow("Screen animations", "Window transition effects", true) { state.showToast("Animations setting saved") }
            }
        }
        "set:about" -> SettingsDetail(state, "About phone") {
            AboutRow("Model number", "GT-I9000")
            AboutRow("Firmware version", "2.3.6")
            AboutRow("Android version", "2.3.6 Gingerbread (TouchWiz 3.0)")
            AboutRow("Baseband version", "I9000XXJW4")
            AboutRow("Kernel version", "2.6.35.7-I9000XXJW4")
            AboutRow("Build number", "GINGERBREAD.XXJW4")
            AboutRow("Display", "4.0″ Super AMOLED · 480 × 800 · 233 ppi")
            AboutRow("Processor", "1 GHz ARM Cortex-A8 (Hummingbird)")
            AboutRow("Memory", "512 MB RAM · 16 GB storage")
            AboutRow("Camera", "5 MP AF · 720p@30fps · 1.3 MP front")
            AboutRow("Battery", "1500 mAh · currently ${if (state.hostBattery in 0..100) state.hostBattery else 76}%")
        }
        null -> {
            Column(Modifier.fillMaxSize().background(Color(0xFFF4F6F8))) {
                AppHeader("Settings", 0xFF90CAF9, 0xFF1565C0)
                LazyColumn {
                    item { SettingsSection("Wireless & sound") }
                    items(
                        listOf(
                            Triple("set:wireless", "📶", "Wireless and network"),
                            Triple("set:sound", "🔊", "Sound and display"),
                            Triple("set:display", "🖼", "Wallpaper & brightness")
                        )
                    ) { (id, glyph, label) -> SettingsMainRow(state, id, glyph, label) }
                    item { SettingsSection("Phone") }
                    items(
                        listOf(
                            Triple("taskmgr", "▦", "Applications / Task manager"),
                            Triple("set:about", "ℹ", "About phone"),
                            Triple("clock", "🕐", "Date and time")
                        )
                    ) { (id, glyph, label) ->
                        Row(
                            Modifier.fillMaxWidth().background(Color.White).clickable {
                                if (id.startsWith("set:")) state.appDetail = id else state.openApp(id)
                            }.padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(glyph, fontSize = 17.sp, modifier = Modifier.width(34.dp))
                            Text(label, fontSize = 13.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                            Text("›", fontSize = 17.sp, color = Color(0xFFB0BEC5))
                        }
                    }
                    item {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                "GT-I9000 · Android 2.3.6 · TouchWiz 3.0\nThis emulator runs entirely on your modern phone — no original firmware or ROM is included.",
                                fontSize = 10.sp, color = Color(0xFF78909C)
                            )
                        }
                    }
                }
            }
        }
        else -> SettingsDetail(state, "Settings") {
            Text("Coming soon in this firmware.", Modifier.padding(16.dp), fontSize = 12.sp, color = Color(0xFF607D8B))
        }
    }
}

@Composable
fun SettingsDetail(state: EmulatorState, title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF7F9FA))) {
        AppHeader(title, 0xFF90CAF9, 0xFF1565C0, onBack = { state.appDetail = null })
        LazyColumn { item { content() } }
    }
}

@Composable
fun SettingsSection(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2),
        modifier = Modifier.fillMaxWidth().background(Color(0xFFE8EFF5)).padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
fun SettingsMainRow(state: EmulatorState, id: String, glyph: String, label: String) {
    Row(
        Modifier.fillMaxWidth().background(Color.White).clickable { state.appDetail = id }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(glyph, fontSize = 17.sp, modifier = Modifier.width(34.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
        Text("›", fontSize = 17.sp, color = Color(0xFFB0BEC5))
    }
}

@Composable
fun SwitchRow(label: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, color = Color(0xFF212121))
            if (sub.isNotEmpty()) Text(sub, fontSize = 10.sp, color = Color(0xFF78909C))
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun AboutRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, fontSize = 10.sp, color = Color(0xFF78909C))
        Text(value, fontSize = 13.sp, color = Color(0xFF212121))
    }
}
