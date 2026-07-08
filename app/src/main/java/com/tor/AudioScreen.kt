@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.tor

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.text.DecimalFormat

// ==================== تنسيق الحجم ====================
fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B","KB","MB","GB")
    val g = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#.##").format(bytes / Math.pow(1024.0, g.toDouble())) + " " + units[g]
}

class DownloadSpeedCalculator {
    private var lastBytes = 0L; private var lastTime = 0L; private var speed = 0.0
    fun update(b: Long): Double {
        val now = System.currentTimeMillis()
        if (lastTime == 0L) { lastTime = now; lastBytes = b; return 0.0 }
        val dt = (now - lastTime) / 1000.0
        if (dt >= 0.5) { speed = (b - lastBytes) / 1024.0 / dt; lastTime = now; lastBytes = b }
        return speed
    }
}

data class Reader(val id: String, val name: String, val imageRes: Int)

val readersList = listOf(
    Reader(id = "shuraim", name = "سعود الشريم", imageRes = R.drawable.reader_shuraim)
)

@Serializable
data class ShuraimVerseJson(
    val surah_number: Int,
    val ayah_number: Int,
    val audio_url: String,
    val duration: Int? = null,
    val segments: List<List<Long>>
)

object ShuraimData {
    private var allVerses: Map<String, ShuraimVerseJson>? = null

    fun load(ctx: Context): Map<String, ShuraimVerseJson> {
        allVerses?.let { return it }
        val jsonText = ctx.assets.open("shuraim.json").bufferedReader().use { it.readText() }
        val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<Map<String, ShuraimVerseJson>>(jsonText)
        allVerses = parsed
        return parsed
    }

    fun getSurahVerses(ctx: Context, surahNo: Int): List<ShuraimVerseJson> {
        return load(ctx).values.filter { it.surah_number == surahNo }.sortedBy { it.ayah_number }
    }

    fun extractAyahInSurah(audioUrl: String): Int? {
        val fileName = audioUrl.substringAfterLast("/").removeSuffix(".mp3")
        return fileName.takeLast(3).toIntOrNull()
    }
}

data class ShuraimAyahRef(val ayahInSurah: Int, val audioUrl: String)

fun getShuraimAyahRefs(ctx: Context, surahNo: Int): List<ShuraimAyahRef> {
    val verses = ShuraimData.getSurahVerses(ctx, surahNo)
    return verses.mapNotNull { v ->
        val ayahInSurah = ShuraimData.extractAyahInSurah(v.audio_url) ?: return@mapNotNull null
        ShuraimAyahRef(ayahInSurah, v.audio_url)
    }.sortedBy { it.ayahInSurah }
}

suspend fun downloadSurahShuraim(
    ctx: Context, surahNo: Int,
    onProgress: (Float, Double, Int, Int) -> Unit,
    onDone: () -> Unit, onError: () -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val ayahRefs = getShuraimAyahRefs(ctx, surahNo)
            if (ayahRefs.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(ctx, "لا توجد آيات لسورة $surahNo فملف JSON", Toast.LENGTH_LONG).show()
                    onError()
                }
                return@withContext
            }

            val total = ayahRefs.size
            val calc = DownloadSpeedCalculator()
            var totalDone = 0L

            for ((index, ref) in ayahRefs.withIndex()) {
                val file = PlayerManager.getAyahFile(ctx, surahNo, ref.ayahInSurah)
                if (!file.exists()) {
                    val response = OkHttpClient().newCall(Request.Builder().url(ref.audioUrl).build()).execute()
                    val body = response.body ?: continue
                    val tmp = File(file.parent, "${file.name}.tmp")
                    tmp.outputStream().use { out ->
                        body.byteStream().use { input ->
                            val buf = ByteArray(8192); var n: Int
                            while (input.read(buf).also { n = it } != -1) {
                                out.write(buf, 0, n); totalDone += n
                                val spd = calc.update(totalDone)
                                withContext(Dispatchers.Main) { onProgress((index + 1f) / total, spd, index + 1, total) }
                            }
                        }
                    }
                    tmp.renameTo(file)
                } else {
                    withContext(Dispatchers.Main) { onProgress((index + 1f) / total, 0.0, index + 1, total) }
                }
            }
            withContext(Dispatchers.Main) { onDone() }
        } catch (e: Exception) {
            Log.e("DOWNLOAD_SHURAIM", "Error: ${e.javaClass.simpleName} - ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(ctx, "خطأ: ${e.javaClass.simpleName} - ${e.message}", Toast.LENGTH_LONG).show()
                onError()
            }
        }
    }
}

val surahNames = listOf(
    "الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال","التوبة","يونس",
    "هود","يوسف","الرعد","إبراهيم","الحجر","النحل","الإسراء","الكهف","مريم","طه",
    "الأنبياء","الحج","المؤمنون","النور","الفرقان","الشعراء","النمل","القصص","العنكبوت","الروم",
    "لقمان","السجدة","الأحزاب","سبأ","فاطر","يس","الصافات","ص","الزمر","غافر",
    "فصلت","الشورى","الزخرف","الدخان","الجاثية","الأحقاف","محمد","الفتح","الحجرات","ق",
    "الذاريات","الطور","النجم","القمر","الرحمن","الواقعة","الحديد","المجادلة","الحشر","الممتحنة",
    "الصف","الجمعة","المنافقون","التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج",
    "نوح","الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ","النازعات","عبس",
    "التكوير","الانفطار","المطففين","الانشقاق","البروج","الطارق","الأعلى","الغاشية","الفجر","البلد",
    "الشمس","الليل","الضحى","الشرح","التين","العلق","القدر","البينة","الزلزلة","العاديات",
    "القارعة","التكاثر","العصر","الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون","النصر",
    "المسد","الإخلاص","الفلق","الناس"
)

// ==================== Audio Panel الكامل ====================
@Composable
fun AudioPanel(
    meQuran: FontFamily,
    onClose: () -> Unit,
    onOpenReciters: () -> Unit
) {
    val ctx = LocalContext.current
    val ayahProgress by PlayerManager.ayahProgress
    val isRepeatOne by PlayerManager.isRepeatOne
    val playbackSpeed by PlayerManager.playbackSpeed
    val sleepMinutesLeft by PlayerManager.sleepTimerMinutesLeft
    val uiState by PlayerManager.playbackUiState

    var showSpeedMenu by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showLoopSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().offset(y = (-12).dp).height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(
                progress = { ayahProgress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = GreenMain,
                trackColor = DividerCol
            )
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, tint = Color.Gray, modifier = Modifier.size(24.dp), contentDescription = null)
                    }
                }

                Row(
                    modifier = Modifier.clickable { onOpenReciters() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(readersList.first().name, fontFamily = meQuran, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMain)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.KeyboardArrowDown, tint = TextMain, modifier = Modifier.size(16.dp), contentDescription = null)
                }

                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { showSleepDialog = true }) {
                        Icon(Icons.Filled.NightlightRound, tint = if (sleepMinutesLeft != null) GoldDark else GreenMain, modifier = Modifier.size(24.dp), contentDescription = null)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box {
                    TextButton(onClick = { showSpeedMenu = true }) {
                        Text("${playbackSpeed}x", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenMain)
                    }
                    DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = { PlayerManager.setPlaybackSpeed(speed); showSpeedMenu = false }
                            )
                        }
                    }
                }

                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { PlayerManager.playNextAyah() }) {
                        Icon(Icons.Filled.SkipPrevious, tint = GreenMain, modifier = Modifier.size(24.dp), contentDescription = null)
                    }
                }

                // الزر المركزي - حساس لحالة Buffering/Error
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (uiState == "error") Color.Red.copy(alpha = 0.12f) else GreenMain.copy(alpha = 0.12f))
                        .clickable {
                            when (uiState) {
                                "error" -> PlayerManager.retryAfterError(ctx)
                                else -> PlayerManager.togglePlayPause()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState) {
                        "buffering" -> CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp, color = GreenMain)
                        "error" -> Icon(Icons.Filled.WifiOff, tint = Color.Red, modifier = Modifier.size(28.dp), contentDescription = null)
                        "playing" -> Icon(Icons.Filled.Pause, tint = GreenMain, modifier = Modifier.size(32.dp), contentDescription = null)
                        else -> Icon(Icons.Filled.PlayArrow, tint = GreenMain, modifier = Modifier.size(32.dp), contentDescription = null)
                    }
                }

                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { PlayerManager.playPreviousAyah() }) {
                        Icon(Icons.Filled.SkipNext, tint = GreenMain, modifier = Modifier.size(24.dp), contentDescription = null)
                    }
                }

                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { showLoopSheet = true }) {
                        Icon(Icons.Filled.Repeat, tint = if (isRepeatOne) GoldDark else GreenMain, modifier = Modifier.size(24.dp), contentDescription = null)
                    }
                }
            }

            if (uiState == "error") {
                Text(
                    "انقطع الاتصال - اضغط لإعادة المحاولة",
                    fontSize = 12.sp, color = Color.Red, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }
        }
    }

    if (showSleepDialog) SleepTimerDialog(onDismiss = { showSleepDialog = false })
    if (showLoopSheet) AdvancedLoopSheet(onDismiss = { showLoopSheet = false })
}



@Composable
fun SleepTimerDialog(onDismiss: () -> Unit) {
    var customHours by remember { mutableStateOf("") }
    var customMinutes by remember { mutableStateOf("") }
    val activeMinutes by PlayerManager.sleepTimerMinutesLeft

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.width(280.dp),
        title = { Text("مؤقت النوم", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (activeMinutes != null) {
                    Text("المؤقت نشط: متبقي $activeMinutes دقيقة", color = GoldDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(15, 30, 60).forEach { mins ->
                        AssistChip(
                            onClick = { PlayerManager.startSleepTimer(mins); onDismiss() },
                            label = { Text("$mins د") }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customHours, onValueChange = { customHours = it.filter { c -> c.isDigit() } },
                        label = { Text("ساعات") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = customMinutes, onValueChange = { customMinutes = it.filter { c -> c.isDigit() } },
                        label = { Text("دقائق") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = customHours.toIntOrNull() ?: 0
                val m = customMinutes.toIntOrNull() ?: 0
                val total = h * 60 + m
                if (total > 0) PlayerManager.startSleepTimer(total)
                onDismiss()
            }) { Text("بدء المؤقت") }
        },
        dismissButton = {
            TextButton(onClick = { PlayerManager.cancelSleepTimer(); onDismiss() }) { Text("إلغاء المؤقت") }
        }
    )
}

@Composable
fun AdvancedLoopSheet(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(0) } // 0=آية 1=سورة 2=مخصص
    var count by remember { mutableStateOf(3) }
    var isInfinite by remember { mutableStateOf(false) }
    val tabs = listOf("آية", "سورة", "مخصص")

    val curAyah by PlayerManager.currentAyah
    var customFrom by remember { mutableStateOf(curAyah?.aya_no ?: 1) }
    var customTo by remember { mutableStateOf((curAyah?.aya_no ?: 1) + 2) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = BgScreen) {
        Column(Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 16.dp)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t) })
                }
            }
            Spacer(Modifier.height(16.dp))

            if (selectedTab == 2) {
                Text("من الآية إلى الآية (داخل نفس السورة)", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customFrom.toString(),
                        onValueChange = { customFrom = it.toIntOrNull() ?: customFrom },
                        label = { Text("من آية") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = customTo.toString(),
                        onValueChange = { customTo = it.toIntOrNull() ?: customTo },
                        label = { Text("إلى آية") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (count > 1) count-- }, enabled = !isInfinite) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }
                Text(if (isInfinite) "∞" else "$count", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                IconButton(onClick = { count++ }, enabled = !isInfinite) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clickable { isInfinite = !isInfinite },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isInfinite, onCheckedChange = { isInfinite = it })
                Text("تكرار مستمر ∞")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val mode = when (selectedTab) { 0 -> "ayah"; 1 -> "surah"; else -> "custom" }
                    val finalCount = if (isInfinite) -1 else count
                    if (mode == "custom") {
                        val from = minOf(customFrom, customTo)
                        val to = maxOf(customFrom, customTo)
                        PlayerManager.setLoopMode(mode, finalCount, from, to)
                    } else {
                        PlayerManager.setLoopMode(mode, finalCount)
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) { Text("تطبيق إعدادات التحفيظ", fontWeight = FontWeight.Bold) }
        }
    }
}




// ==================== نافذة القراء - Switch فوري بلا navigation ====================
@Composable
fun RecitersBottomSheet(
    meQuran: FontFamily,
    onDismiss: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val ctx = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedReader by remember { mutableStateOf(readersList.first()) }
    var searchQuery by remember { mutableStateOf("") }

    fun switchTo(reader: Reader) {
        selectedReader = reader
        PlayerManager.switchReciterAndContinue(ctx)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgScreen,
        windowInsets = WindowInsets(0)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                    placeholder = { Text("ابحث عن قارئ...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(100.dp)
                    ) {
                        items(readersList, key = { it.id }) { reader ->
                            val isSelected = reader.id == selectedReader.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { switchTo(reader) }
                            ) {
                                Box(
                                    Modifier.size(64.dp).clip(CircleShape)
                                        .border(width = if (isSelected) 2.dp else 0.dp, color = if (isSelected) GoldDark else Color.Transparent, shape = CircleShape)
                                ) {
                                    Image(
                                        painter = painterResource(id = reader.imageRes),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(reader.name, fontFamily = meQuran, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(readersList.filter { it.name.contains(searchQuery, ignoreCase = true) }, key = { it.id }) { reader ->
                        Row(
                            Modifier.fillMaxWidth().height(64.dp).clickable { switchTo(reader) }.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = reader.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(reader.name, fontFamily = meQuran, fontSize = 15.sp, color = TextMain)
                            }
                            Icon(Icons.Filled.StarBorder, tint = Color.Gray, contentDescription = null)
                        }
                    }
                }

                Button(
                    onClick = onOpenLibrary,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp)
                        .navigationBarsPadding().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenMain),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Folder, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("فتح المكتبة الصوتية لإدارة التحميلات", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== AudioLibraryScreen (114 سورة + فحص شبكة فوري) ====================
@Composable
fun AudioLibraryScreen(meQuran: FontFamily, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val downloadStates = remember { mutableStateMapOf<Int, SurahDownloadState>() }
    var currentTab by remember { mutableStateOf(0) }
    var showNoInternetSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            (1..114).forEach { n ->
                val isDl = PlayerManager.isSurahReady(ctx, n)
                downloadStates[n] = SurahDownloadState(surahNumber = n, isDownloaded = isDl)
            }
        }
    }

    LaunchedEffect(showNoInternetSnackbar) {
        if (showNoInternetSnackbar) {
            snackbarHostState.showSnackbar("لا يوجد اتصال بالإنترنت، تحقق من الشبكة")
            showNoInternetSnackbar = false
        }
    }

    Scaffold(
        containerColor = BgScreen,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) { data ->
            Snackbar(containerColor = Color.Red, contentColor = Color.White) { Text(data.visuals.message) }
        }},
        topBar = {
            Surface(color = ToolbarBg) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tint = GreenMain, contentDescription = null) }
                        Spacer(Modifier.width(8.dp))
                        Text("مكتبة سعود الشريم", fontFamily = meQuran, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenMain)
                    }
                    Image(
                        painter = painterResource(id = readersList.first().imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentScale = ContentScale.Crop
                    )
                    TabRow(currentTab, containerColor = TabBg, contentColor = GreenMain) {
                        Tab(currentTab == 0, { currentTab = 0 }, text = { Text("كل السور", fontFamily = meQuran, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp)) })
                        Tab(currentTab == 1, { currentTab = 1 }, text = { Text("المحملة",  fontFamily = meQuran, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp)) })
                    }
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            val list = if (currentTab == 0) (1..114).toList() else downloadStates.values.filter { it.isDownloaded }.map { it.surahNumber }.sorted()
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                if (list.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد سور محملة", fontFamily = meQuran, fontSize = 16.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(list, key = { it }) { n ->
                            val state = downloadStates[n] ?: SurahDownloadState(surahNumber = n)
                            val name = surahNames.getOrElse(n - 1) { "سورة $n" }

                            Row(
                                Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(24.dp).clip(CircleShape).background(TabBg), contentAlignment = Alignment.Center) {
                                        Text(convertToArabicNumber(n), fontSize = 11.sp, color = GreenMain, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(name, fontFamily = meQuran, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                                }

                                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                    Crossfade(targetState = when {
                                        state.isDownloaded -> "done"
                                        state.isDownloading -> "downloading"
                                        else -> "idle"
                                    }) { st ->
                                        when (st) {
                                            "idle" -> IconButton(onClick = {
                                                // فحص شبكة فوري قبل البدء
                                                if (!PlayerManager.hasInternet(ctx)) {
                                                    showNoInternetSnackbar = true
                                                    return@IconButton
                                                }
                                                scope.launch {
                                                    downloadStates[n] = state.copy(isDownloading = true)
                                                    downloadSurahShuraim(
                                                        ctx, n,
                                                        onProgress = { _, _, doneAyah, totalAyah ->
                                                            downloadStates[n] = downloadStates[n]!!.copy(bytesDownloaded = doneAyah.toLong(), totalBytes = totalAyah.toLong())
                                                        },
                                                        onDone = { downloadStates[n] = downloadStates[n]!!.copy(isDownloading = false, isDownloaded = true) },
                                                        onError = { downloadStates[n] = downloadStates[n]!!.copy(isDownloading = false) }
                                                    )
                                                }
                                            }) {
                                                Icon(Icons.Filled.Download, tint = Color.Gray, modifier = Modifier.size(22.dp), contentDescription = null)
                                            }
                                            "downloading" -> Box(contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(progress = { state.progress }, modifier = Modifier.size(24.dp), color = GreenMain, strokeWidth = 2.dp)
                                            }
                                            "done" -> Icon(Icons.Filled.CheckCircle, tint = GreenMain, modifier = Modifier.size(24.dp), contentDescription = null)
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = DividerCol.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}




