package com.galaxys1.emulator

import androidx.compose.runtime.mutableStateListOf

/** The 24 applications that shipped on (or defined) the Galaxy S GT-I9000. */
val ALL_APPS: List<EmuApp> = listOf(
    EmuApp("phone", "Phone", "☎", 0xFF7ED321, 0xFF4E9A06),
    EmuApp("contacts", "Contacts", "👤", 0xFFFFB74D, 0xFFF57C00),
    EmuApp("messaging", "Messaging", "✉", 0xFFFFD54F, 0xFFFFA000),
    EmuApp("browser", "Internet", "🌐", 0xFF64B5F6, 0xFF1976D2),
    EmuApp("email", "Email", "@", 0xFF90A4AE, 0xFF546E7A),
    EmuApp("camera", "Camera", "📷", 0xFFB0BEC5, 0xFF607D8B),
    EmuApp("gallery", "Gallery", "🌻", 0xFFFFF176, 0xFFF9A825),
    EmuApp("music", "Music Player", "♪", 0xFFBA68C8, 0xFF7B1FA2),
    EmuApp("video", "Video Player", "▶", 0xFF4FC3F7, 0xFF0288D1),
    EmuApp("youtube", "YouTube", "▶", 0xFFE57373, 0xFFC62828),
    EmuApp("maps", "Maps", "📍", 0xFFAED581, 0xFF558B2F),
    EmuApp("market", "Market", "🛒", 0xFF81C784, 0xFF2E7D32),
    EmuApp("clock", "Clock", "🕐", 0xFF4DD0E1, 0xFF0097A7),
    EmuApp("calculator", "Calculator", "＋", 0xFFFF8A65, 0xFFE64A19),
    EmuApp("calendar", "Calendar", "31", 0xFFF06292, 0xFFC2185B),
    EmuApp("memo", "Memo", "🗒", 0xFFFFF59D, 0xFFFBC02D),
    EmuApp("myfiles", "My Files", "📁", 0xFFFFCC80, 0xFFEF6C00),
    EmuApp("settings", "Settings", "⚙", 0xFF90CAF9, 0xFF1565C0),
    EmuApp("taskmgr", "Task Manager", "▦", 0xFF80CBC4, 0xFF00796B),
    EmuApp("fmr", "FM Radio", "📻", 0xFFA1887F, 0xFF5D4037),
    EmuApp("voice", "Voice Recorder", "🎤", 0xFFEF9A9A, 0xFFD32F2F),
    EmuApp("allshare", "AllShare", "🔗", 0xFF9FA8DA, 0xFF3949AB),
    EmuApp("thinkfree", "ThinkFree Office", "📄", 0xFF80DEEA, 0xFF00ACC1),
    EmuApp("diary", "Mini Diary", "📖", 0xFFCE93D8, 0xFF8E24AA)
)

fun appById(id: String): EmuApp = ALL_APPS.firstOrNull { it.id == id } ?: ALL_APPS[0]

/** Home screen pages (4 across, dock excluded). Page 2 is the default page. */
val HOME_PAGES: List<List<String>> = listOf(
    listOf("market", "youtube", "maps", "email"),
    listOf("camera", "gallery", "music", "video", "browser", "calendar", "clock", "calculator", "settings", "taskmgr", "myfiles", "fmr"),
    listOf("voice", "allshare", "thinkfree", "diary")
)

val DOCK_APPS = listOf("phone", "contacts", "messaging")

val WALLPAPERS: List<Wallpaper> = listOf(
    Wallpaper("Galaxy Nebula", listOf(0xFF0B1E4B, 0xFF27408B, 0xFF7B2CBF, 0xFF0B1E4B), 0xFF7FD4FF),
    Wallpaper("Ocean Wave", listOf(0xFF00263A, 0xFF005B7A, 0xFF00A8A8, 0xFF00263A), 0xFF9FF5E0),
    Wallpaper("Sunset Field", listOf(0xFF2B1055, 0xFF7597DE, 0xFFFF9A56, 0xFF3B1D5E), 0xFFFFC46B),
    Wallpaper("Green Valley", listOf(0xFF0A2E1F, 0xFF1E6B45, 0xFF7BC96F, 0xFF0A2E1F), 0xFFC6F09A),
    Wallpaper("Midnight Seoul", listOf(0xFF050510, 0xFF141432, 0xFF3A3A7A, 0xFF050510), 0xFF8E9BFF)
)

val CONTACTS: List<Contact> = listOf(
    Contact("Anna Kim", "+82 10-2010-0604", "anna.kim@example.com", "Friends", 0xFFEF5350),
    Contact("Byung-Ho Lee", "+82 10-5550-1100", "byungho@example.com", "Work", 0xFF42A5F5),
    Contact("Chul-Soo Park", "+82 10-7788-2010", "chulsoo@example.com", "Work", 0xFF66BB6A),
    Contact("Dana Choi", "+1 415-555-0132", "dana@example.com", "Friends", 0xFFAB47BC),
    Contact("Eun-Ji Seo", "+82 10-9000-2011", "eunji@example.com", "Family", 0xFFFF7043),
    Contact("Galaxy Support", "1588-3366", "support@galaxy.example", "Services", 0xFF26A69A),
    Contact("Ji-Woo Han", "+82 10-1234-5678", "jiwoo@example.com", "Family", 0xFF5C6BC0),
    Contact("Min-Jun Kang", "+82 10-3141-5926", "minjun@example.com", "Friends", 0xFFFFCA28),
    Contact("Samsung Town", "+82 2-2255-0114", "town@example.com", "Work", 0xFF78909C),
    Contact("Soo-Ah Yoon", "+82 10-2718-2818", "sooah@example.com", "Friends", 0xFFEC407A)
)

fun seedThreads(): List<MessageThread> = listOf(
    MessageThread(
        0, "Ji-Woo Han", 0xFF5C6BC0, mutableStateListOf(
            ChatMessage(false, "Did you see the new Galaxy S? The Super AMOLED screen is unreal.", "09:41"),
            ChatMessage(true, "I know! blacks are actually BLACK. And it shoots 720p video.", "09:43"),
            ChatMessage(false, "Swype typing is so fast too. Come by the store in Seoul this weekend!", "09:44")
        )
    ),
    MessageThread(
        1, "Anna Kim", 0xFFEF5350, mutableStateListOf(
            ChatMessage(false, "Happy launch day! 🎉 Are you upgrading from your old phone?", "08:15"),
            ChatMessage(true, "Already did — 16 GB model. TouchWiz feels so smooth.", "08:20")
        )
    ),
    MessageThread(
        2, "Galaxy Support", 0xFF26A69A, mutableStateListOf(
            ChatMessage(false, "Welcome to Galaxy S! Your GT-I9000 is covered by a 2-year warranty in the United Kingdom.", "07:02")
        )
    )
)

val AUTO_REPLIES = listOf(
    "Nice! Sent from my Galaxy S 📱",
    "haha yes! This Super AMOLED though 😍",
    "OK, talk later — battery lasts ages on this thing.",
    "Call me? The speaker on this is great.",
    "👍 Got it!"
)

val TRACKS: List<Track> = listOf(
    Track("Galaxy Theme", "Samsung Orchestra", "Sounds of Galaxy", 214, 0xFF7B2CBF),
    Track("Over the Horizon", "Seoul Studio", "Galaxy S Ringtones", 187, 0xFF1976D2),
    Track("AMOLED Nights", "Neon Coast", "Synthwave 2010", 243, 0xFF00897B),
    Track("Hummingbird", "1 GHz Club", "TouchWiz Beats", 198, 0xFFF57C00),
    Track("Seoul Sunrise", "Han River Trio", "Morning in Seoul", 226, 0xFFC2185B)
)

val VIDEOS: List<VideoItem> = listOf(
    VideoItem("Galaxy S (GT-I9000) Official Launch", "Samsung", "1:32", "2.4M", 0xFF1976D2),
    VideoItem("Super AMOLED vs LCD — Outdoor Test", "Mobile Review", "4:07", "890K", 0xFF00897B),
    VideoItem("720p HD Camera Sample — Seoul", "Ji-Woo Han", "0:58", "1.1M", 0xFFF57C00),
    VideoItem("TouchWiz 3.0 Walkthrough", "Gadget Lab", "6:21", "640K", 0xFF7B2CBF),
    VideoItem("Swype World Record Attempt", "TextFast", "2:44", "312K", 0xFFC2185B)
)

val EMAILS: List<EmailItem> = listOf(
    EmailItem("Samsung Mobile", "Welcome to your Galaxy S", "Dear customer, thank you for choosing Galaxy S…", "Dear customer,\n\nThank you for choosing the Samsung Galaxy S GT-I9000.\n\nYour 4.0-inch Super AMOLED display, 1 GHz Hummingbird processor and Android 2.3 with TouchWiz 3.0 are ready to go.\n\nTip: long-press the Home key to open the Task manager, and pull down from the top of the screen for quick Wi-Fi, Bluetooth and sound toggles.\n\n— Samsung Mobile", "09:12", true, 0xFF1976D2),
    EmailItem("Android Market", "Your order: Angry Birds", "Thanks for downloading from Android Market…", "Thanks for downloading from Android Market.\n\nAngry Birds has been installed on your GT-I9000.\n\nManage your apps any time from Market > My apps.\n\n— The Android Market team", "08:47", true, 0xFF2E7D32),
    EmailItem("Anna Kim", "Photos from launch day!", "Attached the shots from the Seoul store…", "Attached the shots from the Seoul store — the 5 MP camera did great indoors!\n\nLet's shoot a 720p video this weekend.\n\nAnna", "Yesterday", false, 0xFFEF5350),
    EmailItem("KakaoTalk Team", "Verify your number", "Welcome to KakaoTalk…", "Welcome to KakaoTalk!\n\nYour number has been verified. Message your friends for free, anywhere.\n\n— KakaoTalk", "Yesterday", false, 0xFFF9A825)
)

val MARKET_APPS: List<MarketApp> = listOf(
    MarketApp("Angry Birds", "Rovio", "Free", "★★★★½", "10M+", "🐦", 0xFFC62828),
    MarketApp("KakaoTalk", "Kakao", "Free", "★★★★½", "5M+", "💬", 0xFFF9A825),
    MarketApp("Fruit Ninja", "Halfbrick", "£0.63", "★★★★☆", "1M+", "🍉", 0xFF2E7D32),
    MarketApp("Swype Keyboard", "Swype Inc.", "Free", "★★★★☆", "500K+", "⌨", 0xFF1565C0),
    MarketApp("Paper Camera", "JFDP Labs", "£1.19", "★★★★½", "100K+", "🎨", 0xFF6A1B9A),
    MarketApp("Temple Run", "Imangi", "Free", "★★★★☆", "10M+", "🏃", 0xFFEF6C00)
)

val NOTIFICATIONS: List<EmuNotification> = listOf(
    EmuNotification("Messaging", "Ji-Woo Han", "Swype typing is so fast too. Come by…", "09:44", "✉"),
    EmuNotification("Email", "Samsung Mobile", "Welcome to your Galaxy S", "09:12", "@"),
    EmuNotification("Market", "Updates available", "3 applications have updates", "08:30", "🛒"),
    EmuNotification("Calendar", "Galaxy S launch party", "Today, 19:00 — Samsung Town, Seoul", "08:00", "31")
)

val FILE_FOLDERS: List<Pair<String, List<String>>> = listOf(
    "DCIM" to listOf("Camera/", "IMG_20100604_001.jpg", "IMG_20100604_002.jpg", "VID_20100605_001.mp4"),
    "Music" to listOf("Over_the_Horizon.mp3", "Galaxy_Theme.mp3", "AMOLED_Nights.mp3"),
    "Download" to listOf("TouchWiz_manual.pdf", "Kies_2.0_setup.exe"),
    "Pictures" to listOf("Wallpapers/", "Screenshot_01.png", "Screenshot_02.png"),
    "Android" to listOf("data/", "obb/")
)
