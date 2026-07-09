@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
package com.tor

import androidx.compose.foundation.ExperimentalFoundationApi
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import android.os.Build
import java.util.*

// ==========================================================
// 1. الثيم - Design Tokens (القسم 1 من الوثيقة)
// ==========================================================

object PrayerTheme {
    val BACKGROUND = Color(0xFF0A0A0A)
    val SURFACE = Color(0xFF141414)
    val SURFACE_GRADIENT_END = Color(0xFF1C1C1E)
    val BORDER = Color(0xFF222222)
    val ACCENT = Color(0xFFD4AF37)
    val PRIMARY_TEXT = Color(0xFFFFFFFF)
    val SECONDARY_TEXT = Color(0xFF8E8E93)
    val INACTIVE = Color(0xFF48484A)
    val ROW_DIVIDER = Color(0xFF1A1A1A)
}

// ==========================================================
// موديلات البيانات (مشتركة بين PrayerActivity.kt و PrayerApp.kt)
// ==========================================================

data class CityEntry(val id: Int, val name: String, val lat: Double?, val lon: Double?)

data class DayPrayerTimes(
    val cityId: Int,
    val hijriMonthName: String,
    val hijriDay: String,
    val gregorianDay: String,
    val gregorianMonth: Int,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

data class PrayerItem(val key: String, val label: String, val icon: String, val time: String, val hasAlarmToggle: Boolean)

internal enum class PageIndex(val index: Int) {
    HOME(0), PRAYER_TIMES(1), TASBEEH(2), DUAS(3), SETTINGS(4)
}

// ==========================================================
// الموقع الجغرافي
// ==========================================================

internal fun fetchLocationBasic(
    context: Context,
    onSuccess: (Double, Double) -> Unit,
    onFailure: () -> Unit
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        onFailure()
        return
    }

    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                val lastLocation = locationManager.getLastKnownLocation(provider)
                if (lastLocation != null) {
                    onSuccess(lastLocation.latitude, lastLocation.longitude)
                    return
                }
            }
        }

        val activeProvider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (activeProvider == null) {
            onFailure()
            return
        }

        locationManager.requestSingleUpdate(activeProvider, object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                onSuccess(location.latitude, location.longitude)
            }
        }, Looper.getMainLooper())

    } catch (e: SecurityException) {
        onFailure()
    } catch (e: Exception) {
        onFailure()
    }
}

// ==========================================================
// تحميل قاعدة البيانات من GitHub Releases + الاستعلامات
// ==========================================================

object PrayerDbManager {
    private const val DB_NAME = "mawakit_maroc_final.db"
    private const val DB_URL =
        "https://github.com/darkdark49h/quran/releases/download/v7.1.1/mawakit_maroc_final.5.db"

    private var db: SQLiteDatabase? = null

    fun getDbFile(context: Context): File = context.getDatabasePath(DB_NAME)

    fun isDbAvailable(context: Context): Boolean = getDbFile(context).exists()

    suspend fun downloadDb(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
            val request = Request.Builder().url(DB_URL).build()
            val response = client.newCall(request).execute()

            android.util.Log.d("PrayerDB", "Response code: ${response.code}")

            if (!response.isSuccessful) {
                android.util.Log.e("PrayerDB", "Failed with code: ${response.code}")
                return@withContext false
            }

            val dbFile = getDbFile(context)
            dbFile.parentFile?.mkdirs()

            val body = response.body ?: return@withContext false
            body.byteStream().use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            android.util.Log.d("PrayerDB", "Download success, size: ${dbFile.length()}")
            true
        } catch (e: Exception) {
            android.util.Log.e("PrayerDB", "Download error: ${e.message}", e)
            false
        }
    }

    fun getDatabase(context: Context): SQLiteDatabase {
        db?.let { return it }
        val dbFile = getDbFile(context)
        val opened = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        db = opened
        return opened
    }

    fun getAllCities(context: Context): List<CityEntry> {
        val database = getDatabase(context)
        val list = mutableListOf<CityEntry>()
        val cursor = database.rawQuery("SELECT id, name, lat, lon FROM cities ORDER BY name", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CityEntry(
                        it.getInt(0), it.getString(1),
                        if (it.isNull(2)) null else it.getDouble(2),
                        if (it.isNull(3)) null else it.getDouble(3)
                    )
                )
            }
        }
        return list
    }

    // حساب أقرب مدينة بدقة مطلقة عبر معادلة Haversine
    fun findNearestCity(context: Context, userLat: Double, userLon: Double): CityEntry? {
        val cities = getAllCities(context)
        var nearest: CityEntry? = null
        var minDist = Double.MAX_VALUE
        for (c in cities) {
            if (c.lat == null || c.lon == null) continue
            val dist = haversineKm(userLat, userLon, c.lat, c.lon)
            if (dist < minDist) {
                minDist = dist
                nearest = c
            }
        }
        return nearest
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0088
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).let { it * it }
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    // استعلام أوقات الصلاة لأي تاريخ ميلادي (يُستخدم من PrayerApp.kt لدعم التنقل بين الأيام)
    fun getPrayerTimesForDate(context: Context, cityId: Int, date: LocalDate): DayPrayerTimes? {
        val gMonth = date.monthValue
        val gDay = date.dayOfMonth.toString()

        val database = getDatabase(context)
        val cursor = database.rawQuery(
            """SELECT city_id, hijri_month_name, hijri_day, gregorian_day, gregorian_month, 
               fajr, sunrise, dhuhr, asr, maghrib, isha 
               FROM prayer_times WHERE city_id = ? AND gregorian_month = ? AND gregorian_day = ?""",
            arrayOf(cityId.toString(), gMonth.toString(), gDay)
        )

        var result: DayPrayerTimes? = null
        cursor.use {
            if (it.moveToFirst()) {
                result = DayPrayerTimes(
                    cityId = it.getInt(0), hijriMonthName = it.getString(1),
                    hijriDay = it.getString(2), gregorianDay = it.getString(3),
                    gregorianMonth = it.getInt(4), fajr = it.getString(5),
                    sunrise = it.getString(6), dhuhr = it.getString(7),
                    asr = it.getString(8), maghrib = it.getString(9), isha = it.getString(10)
                )
            }
        }
        return result
    }

    fun getTodayPrayerTimes(context: Context, cityId: Int): DayPrayerTimes? =
        getPrayerTimesForDate(context, cityId, LocalDate.now())
}

// ==========================================================
// أدوات مشتركة: بناء لائحة الصلوات + حساب القادمة + العداد
// (بدون private حتى يمكن استعمالها من PrayerApp.kt في نفس الـ package)
// ==========================================================

internal fun buildPrayerList(times: DayPrayerTimes): List<PrayerItem> {
    return listOf(
        PrayerItem("fajr", "الفجر", "🕌", times.fajr, hasAlarmToggle = true),
        PrayerItem("sunrise", "الشروق", "☀️", times.sunrise, hasAlarmToggle = false),
        PrayerItem("dhuhr", "الظهر", "🕋", times.dhuhr, hasAlarmToggle = true),
        PrayerItem("asr", "العصر", "📿", times.asr, hasAlarmToggle = true),
        PrayerItem("maghrib", "المغرب", "🌅", times.maghrib, hasAlarmToggle = true),
        PrayerItem("isha", "العشاء", "🌌", times.isha, hasAlarmToggle = true)
    )
}

internal fun parseTimeToday(time: String): Calendar? {
    val parts = time.trim().split(":")
    if (parts.size < 2) return null
    val h = parts[0].trim().toIntOrNull() ?: return null
    val m = parts[1].trim().toIntOrNull() ?: return null
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m); set(Calendar.SECOND, 0)
    }
}

internal fun findNextPrayer(items: List<PrayerItem>): Pair<PrayerItem, Calendar> {
    val now = Calendar.getInstance()
    for (item in items) {
        val cal = parseTimeToday(item.time) ?: continue
        if (cal.after(now)) return item to cal
    }
    val first = items.first()
    val cal = parseTimeToday(first.time) ?: Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, 1)
    return first to cal
}

internal fun formatRemaining(target: Calendar): String {
    val now = Calendar.getInstance()
    val diff = (target.timeInMillis - now.timeInMillis) / 1000
    if (diff < 0) return "00:00:00"
    val h = diff / 3600
    val m = (diff % 3600) / 60
    val s = diff % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}

// ==========================================================
// تخزين بسيط للمدينة المختارة (يُستخدم من طرف PrayerWidget.kt المستقل عن الـ Activity)
// ==========================================================

private const val PREFS_NAME = "prayer_prefs"
private const val KEY_CITY_ID = "city_id"

internal fun saveSelectedCityId(context: Context, cityId: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_CITY_ID, cityId)
        .apply()
}

internal fun getSavedCityId(context: Context): Int? {
    val id = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_CITY_ID, -1)
    return if (id == -1) null else id
}

// ==========================================================
// حالة تدفق الإقلاع (Splash -> DB -> Location -> Main)
// ==========================================================

private enum class Screen { SPLASH, DB_SYNC, DB_ERROR, LOCATION_CHOICE, CITY_PICKER, LOADING, MAIN }

@Composable
private fun DbErrorScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("تعذر الاتصال بالشبكة حالياً", color = PrayerTheme.PRIMARY_TEXT, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("تأكد من اتصالك بالإنترنت", color = PrayerTheme.SECONDARY_TEXT, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PrayerTheme.ACCENT)) {
                Text("إعادة المحاولة", color = Color.Black)
            }
        }
    }
}

// ==========================================================
// PrayerActivity - الواجهة الرئيسية
// ==========================================================

class PrayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PrayerAppRoot(
                fetchLocation = { onSuccess, onFailure ->
                    fetchLocationBasic(this, onSuccess, onFailure)
                },
                onOpenQuran = {
                    startActivity(Intent(this, MainActivity::class.java))
                },
                onOpenQibla = {
                    startActivity(Intent(this, QiblaActivity::class.java))
                }
            )
        }
    }
}

// ==========================================================
// جذر التطبيق - يدير تدفق الإقلاع ثم يعرض HorizontalPager
// ==========================================================

@Composable
private fun PrayerAppRoot(
    fetchLocation: (onSuccess: (Double, Double) -> Unit, onFailure: () -> Unit) -> Unit,
    onOpenQuran: () -> Unit,
    onOpenQibla: () -> Unit
) {
    val context = LocalContext.current
    var screen by remember { mutableStateOf(Screen.SPLASH) }
    var selectedCity by remember { mutableStateOf<CityEntry?>(null) }
    var todayTimes by remember { mutableStateOf<DayPrayerTimes?>(null) }
    var allCities by remember { mutableStateOf<List<CityEntry>>(emptyList()) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchLocation(
                { lat, lon ->
                    val nearest = PrayerDbManager.findNearestCity(context, lat, lon)
                    if (nearest != null) selectedCity = nearest
                    else screen = Screen.CITY_PICKER
                },
                { screen = Screen.CITY_PICKER }
            )
        } else {
            screen = Screen.CITY_PICKER
        }
    }

    LaunchedEffect(Unit) {
        delay(500)
        if (!PrayerDbManager.isDbAvailable(context)) {
            screen = Screen.DB_SYNC
            var success = PrayerDbManager.downloadDb(context)
            if (!success) {
                delay(1500)
                success = PrayerDbManager.downloadDb(context)
            }
            if (!success) {
                screen = Screen.DB_ERROR
                return@LaunchedEffect
            }
        }
        allCities = withContext(Dispatchers.IO) { PrayerDbManager.getAllCities(context) }
        screen = Screen.LOCATION_CHOICE
    }

    LaunchedEffect(selectedCity) {
        val city = selectedCity ?: return@LaunchedEffect
        screen = Screen.LOADING
        saveSelectedCityId(context, city.id)
        val times = withContext(Dispatchers.IO) { PrayerDbManager.getTodayPrayerTimes(context, city.id) }
        todayTimes = times
        screen = Screen.MAIN
    }

    when (screen) {
        Screen.DB_ERROR -> DbErrorScreen(onRetry = { screen = Screen.SPLASH })
        Screen.SPLASH -> SplashScreen()
        Screen.DB_SYNC -> DbSyncScreen()
        Screen.LOCATION_CHOICE -> LocationChoiceScreen(
            onGpsClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            onManualClick = { screen = Screen.CITY_PICKER }
        )
        Screen.CITY_PICKER -> CityPickerScreen(cities = allCities, onCitySelected = { selectedCity = it })
        Screen.LOADING -> LoadingScreen(selectedCity?.name ?: "")
        Screen.MAIN -> {
            val city = selectedCity
            val times = todayTimes
            if (city != null && times != null) {
                MainPagerScreen(
                    cityId = city.id,
                    cityName = city.name,
                    todayTimes = times,
                    onOpenQuran = onOpenQuran,
                    onOpenQibla = onOpenQibla
                )
            }
        }
    }
}

// ==========================================================
// Splash Screen
// ==========================================================

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Text("الفرقان", color = PrayerTheme.ACCENT, fontSize = 36.sp, fontWeight = FontWeight.Bold)
    }
}

// ==========================================================
// شاشة مزامنة قاعدة البيانات (أول تشغيل فقط)
// ==========================================================

@Composable
private fun DbSyncScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrayerTheme.ACCENT)
            Spacer(modifier = Modifier.height(16.dp))
            Text("جاري تحميل بيانات المواقيت...", color = PrayerTheme.SECONDARY_TEXT, fontSize = 14.sp)
        }
    }
}

// ==========================================================
// Location Choice Screen
// ==========================================================

@Composable
private fun LocationChoiceScreen(onGpsClick: () -> Unit, onManualClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("حدد موقعك", color = PrayerTheme.PRIMARY_TEXT, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGpsClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrayerTheme.ACCENT)
        ) {
            Text("تحديد الموقع تلقائياً (GPS)", color = Color.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onManualClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, PrayerTheme.SECONDARY_TEXT)
        ) {
            Text("اختيار المدينة يدوياً", color = PrayerTheme.PRIMARY_TEXT, fontSize = 16.sp)
        }
    }
}

// ==========================================================
// City Picker Screen
// ==========================================================

@Composable
private fun CityPickerScreen(cities: List<CityEntry>, onCitySelected: (CityEntry) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, cities) {
        if (query.isBlank()) cities else cities.filter { it.name.contains(query) }
    }

    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND).padding(16.dp)) {
        Text("اختر مدينتك", color = PrayerTheme.PRIMARY_TEXT, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("ابحث", color = PrayerTheme.SECONDARY_TEXT) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PrayerTheme.PRIMARY_TEXT,
                unfocusedTextColor = PrayerTheme.PRIMARY_TEXT
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            items(filtered) { city ->
                Text(
                    text = city.name,
                    color = PrayerTheme.PRIMARY_TEXT,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCitySelected(city) }
                        .padding(vertical = 14.dp)
                )
                HorizontalDivider(color = PrayerTheme.SURFACE)
            }
        }
    }
}

// ==========================================================
// Loading Screen
// ==========================================================

@Composable
private fun LoadingScreen(cityName: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrayerTheme.ACCENT)
            Spacer(modifier = Modifier.height(16.dp))
            Text("جاري مزامنة أوقات الصلاة لمدينة $cityName...", color = PrayerTheme.SECONDARY_TEXT, fontSize = 14.sp)
        }
    }
}

// ==========================================================
// 2. الهيكل العام - HorizontalPager بـ 5 شاشات + BottomBar (القسم 2)
// ==========================================================

@Composable
private fun MainPagerScreen(
    cityId: Int,
    cityName: String,
    todayTimes: DayPrayerTimes,
    onOpenQuran: () -> Unit,
    onOpenQibla: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = PageIndex.HOME.index) { 5 }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                PageIndex.HOME.index -> HomeScreen(cityName = cityName, times = todayTimes, onOpenQuran = onOpenQuran, onOpenQibla = onOpenQibla)
                PageIndex.PRAYER_TIMES.index -> PrayerTimesScreen(cityId = cityId, cityName = cityName, initialTimes = todayTimes)
                PageIndex.TASBEEH.index -> TasbeehScreen()
                PageIndex.DUAS.index -> DuasScreen()
                PageIndex.SETTINGS.index -> SettingsScreen()
            }
        }

        BottomBar(
            currentPage = pagerState.currentPage,
            onPageSelected = { index ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch { pagerState.animateScrollToPage(index) }
            }
        )
    }
}

@Composable
private fun BottomBar(currentPage: Int, onPageSelected: (Int) -> Unit) {
    val items = listOf(
        Triple(PageIndex.HOME.index, Icons.Filled.Home, "الرئيسية"),
        Triple(PageIndex.PRAYER_TIMES.index, Icons.Filled.Schedule, "المواقيت"),
        Triple(PageIndex.TASBEEH.index, Icons.Filled.RadioButtonChecked, "التسبيح"),
        Triple(PageIndex.DUAS.index, Icons.Filled.ImportContacts, "الأدعية"),
        Triple(PageIndex.SETTINGS.index, Icons.Filled.Settings, "الإعدادات")
    )

    Column(modifier = Modifier.fillMaxWidth().height(80.dp).background(PrayerTheme.SURFACE)) {
        HorizontalDivider(color = PrayerTheme.BORDER, thickness = 0.5.dp)
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (index, icon, label) ->
                val active = currentPage == index
                Column(
                    modifier = Modifier.clickable { onPageSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        icon, contentDescription = label,
                        tint = if (active) PrayerTheme.ACCENT else PrayerTheme.INACTIVE,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(label, color = if (active) PrayerTheme.ACCENT else PrayerTheme.INACTIVE, fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================================
// 3. الشاشة الرئيسية (Home Screen - القسم 3 من الوثيقة)
// ==========================================================

@Composable
private fun HomeScreen(
    cityName: String,
    times: DayPrayerTimes,
    onOpenQuran: () -> Unit,
    onOpenQibla: () -> Unit // زر القبلة الملكي
) {
    val prayerItems = remember(times) { buildPrayerList(times) }

    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick = System.currentTimeMillis()
        }
    }

    val nextPrayerState by remember(tick, prayerItems) { derivedStateOf { findNextPrayer(prayerItems) } }
    val (nextItem, nextCal) = nextPrayerState
    val remainingText by remember(tick) { derivedStateOf { formatRemaining(nextCal) } }

    var quranOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND)) {

        // الهيدر العلوي الموزون بميزان الذهب - 48.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // اليمين: التاريخ الهجري
            Text(
                text = "${times.hijriDay} ${times.hijriMonthName} 1448",
                color = PrayerTheme.PRIMARY_TEXT,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            // اليسار: زر القبلة الملكي
            IconButton(onClick = onOpenQibla) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "القبلة",
                    tint = PrayerTheme.SECONDARY_TEXT,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // الطبقة المركزية الكبرى - Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
                .height(190.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrayerTheme.SURFACE, PrayerTheme.SURFACE_GRADIENT_END),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .border(BorderStroke(0.5.dp, PrayerTheme.BORDER), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "صلاة ${nextItem.label}",
                    color = PrayerTheme.ACCENT,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = nextItem.time,
                    color = PrayerTheme.PRIMARY_TEXT,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "باقي $remainingText",
                        color = PrayerTheme.SECONDARY_TEXT,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "  •  ",
                        color = PrayerTheme.BORDER,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = PrayerTheme.SECONDARY_TEXT,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = cityName,
                        color = PrayerTheme.SECONDARY_TEXT,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // بطاقة المصحف الشريف السريعة - 88.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .height(88.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PrayerTheme.SURFACE)
                .border(BorderStroke(0.5.dp, PrayerTheme.BORDER), RoundedCornerShape(20.dp))
                .clickable {
                    quranOpen = true
                    onOpenQuran()
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "دخول المصحف الشريف",
                color = PrayerTheme.PRIMARY_TEXT,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (quranOpen) Icons.Filled.MenuBook else Icons.Filled.Book,
                contentDescription = null,
                tint = PrayerTheme.ACCENT,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ==========================================================
// شاشات مؤقتة (التسبيح والأدعية) - غير مفصلة في الوثيقة بعد
// ==========================================================

@Composable
private fun TasbeehScreen() {
    Box(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND), contentAlignment = Alignment.Center) {
        Text("التسبيح الرقمي - قريباً", color = PrayerTheme.SECONDARY_TEXT, fontSize = 16.sp)
    }
}

@Composable
private fun DuasScreen() {
    Box(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND), contentAlignment = Alignment.Center) {
        Text("الأدعية المأثورة - قريباً", color = PrayerTheme.SECONDARY_TEXT, fontSize = 16.sp)
    }
}

// ==========================================================
// 5. مركز التحكم المعزول - شاشة الإعدادات (منيو رئيسي + شاشتين فرعيتين)
// ==========================================================

private enum class SettingsSubScreen { MENU, AZAN_ALERTS, WIDGET }

@Composable
private fun SettingsScreen() {
    var subScreen by remember { mutableStateOf(SettingsSubScreen.MENU) }

    when (subScreen) {
        SettingsSubScreen.MENU -> SettingsMenuScreen(
            onOpenAzanAlerts = { subScreen = SettingsSubScreen.AZAN_ALERTS },
            onOpenWidget = { subScreen = SettingsSubScreen.WIDGET }
        )
        SettingsSubScreen.AZAN_ALERTS -> AzanAlertsScreen(onBack = { subScreen = SettingsSubScreen.MENU })
        SettingsSubScreen.WIDGET -> WidgetSetupScreen(onBack = { subScreen = SettingsSubScreen.MENU })
    }
}

// ------------------------------------------------------------
// المنيو الرئيسي للإعدادات
// ------------------------------------------------------------

@Composable
private fun SettingsMenuScreen(onOpenAzanAlerts: () -> Unit, onOpenWidget: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND)) {

        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الإعدادات", color = PrayerTheme.PRIMARY_TEXT, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsMenuRow(
                    icon = Icons.Filled.Notifications,
                    title = "إدارة تنبيهات الصلوات والأذان",
                    subtitle = "الصوت، المؤذن، التنبيهات القبلية",
                    onClick = onOpenAzanAlerts
                )
            }
            item {
                SettingsMenuRow(
                    icon = Icons.Filled.Widgets,
                    title = "إضافة الويدجت",
                    subtitle = "أضف مواقيت الصلاة إلى الشاشة الرئيسية",
                    onClick = onOpenWidget
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SettingsMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PrayerTheme.SURFACE)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = PrayerTheme.ACCENT, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, color = PrayerTheme.PRIMARY_TEXT, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = PrayerTheme.SECONDARY_TEXT, fontSize = 12.sp)
            }
        }
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = PrayerTheme.SECONDARY_TEXT)
    }
}

// ------------------------------------------------------------
// شاشة تنبيهات الأذان (محتوى شاشة الإعدادات القديمة، منقولة هنا)
// ------------------------------------------------------------

@Composable
private fun AzanAlertsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var volume by remember { mutableStateOf(0.8f) }
    var fajrPreAlert by remember { mutableStateOf(true) }
    var otherPreAlert by remember { mutableStateOf(true) }
    val preAlertMinutes by remember { mutableStateOf(15) }

    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND)) {

        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PrayerTheme.PRIMARY_TEXT)
            }
            Spacer(Modifier.width(4.dp))
            Text("إعدادات الأذان والتنبيهات", color = PrayerTheme.PRIMARY_TEXT, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrayerTheme.SURFACE)
                        .clickable { /* فتح قائمة اختيار المؤذن */ }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("صوت الأذان الافتراضي", color = PrayerTheme.PRIMARY_TEXT, fontSize = 15.sp)
                        Text(
                            "الشيخ عبد الباسط عبد الصمد - أذان مكة",
                            color = PrayerTheme.SECONDARY_TEXT, fontSize = 12.sp
                        )
                    }
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = PrayerTheme.SECONDARY_TEXT)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().height(50.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.VolumeUp, contentDescription = null,
                        tint = PrayerTheme.SECONDARY_TEXT, modifier = Modifier.size(22.dp)
                    )
                    Slider(
                        value = volume,
                        onValueChange = { volume = it },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = PrayerTheme.ACCENT,
                            inactiveTrackColor = PrayerTheme.BORDER,
                            thumbColor = PrayerTheme.ACCENT
                        )
                    )
                    Text("${(volume * 100).toInt()}%", color = PrayerTheme.SECONDARY_TEXT, fontSize = 13.sp)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("التنبيه القبلي لصلاة الفجر ($preAlertMinutes د)", color = PrayerTheme.PRIMARY_TEXT, fontSize = 14.sp)
                    Switch(
                        checked = fajrPreAlert,
                        onCheckedChange = { fajrPreAlert = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = PrayerTheme.ACCENT)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("التنبيه القبلي لباقي الصلوات ($preAlertMinutes د)", color = PrayerTheme.PRIMARY_TEXT, fontSize = 14.sp)
                    Switch(
                        checked = otherPreAlert,
                        onCheckedChange = { otherPreAlert = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = PrayerTheme.ACCENT)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrayerTheme.SURFACE)
                        .clickable {
                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "تفعيل العمل المستدام في الخلفية",
                        color = PrayerTheme.ACCENT, fontSize = 15.sp, fontWeight = FontWeight.Medium
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ------------------------------------------------------------
// شاشة إضافة الويدجت - معاينة حية + تأكيد الإضافة إلى الشاشة الرئيسية
// ------------------------------------------------------------

@Composable
private fun WidgetSetupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND)) {

        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PrayerTheme.PRIMARY_TEXT)
            }
            Spacer(Modifier.width(4.dp))
            Text("إضافة الويدجت", color = PrayerTheme.PRIMARY_TEXT, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text("اضغط على الويدجت لإضافته إلى شاشتك الرئيسية", color = PrayerTheme.SECONDARY_TEXT, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))

            // معاينة الويدجت - نفس هندسة PrayerWidget.kt (4x1) لكن بـ Compose عادي للمعاينة داخل التطبيق
            WidgetPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clickable { showConfirmDialog = true }
            )

            Spacer(Modifier.height(20.dp))
            resultMessage?.let {
                Text(it, color = PrayerTheme.ACCENT, fontSize = 13.sp)
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = PrayerTheme.SURFACE,
            title = { Text("إضافة الويدجت", color = PrayerTheme.PRIMARY_TEXT) },
            text = { Text("هل تريد إضافة ويدجت مواقيت الصلاة إلى الشاشة الرئيسية؟", color = PrayerTheme.SECONDARY_TEXT) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    resultMessage = if (requestPinPrayerWidget(context)) {
                        "تم إرسال طلب الإضافة، أكّد من نافذة النظام"
                    } else {
                        "جهازك لا يدعم الإضافة التلقائية، أضفه يدوياً من قائمة الويدجتات"
                    }
                }) { Text("نعم", color = PrayerTheme.ACCENT) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("لا", color = PrayerTheme.SECONDARY_TEXT)
                }
            }
        )
    }
}

@Composable
private fun WidgetPreview(modifier: Modifier = Modifier) {
    val fajr = remember { PrayerItem("fajr", "فجر", "🕌", "04:12", false) }
    val dhuhr = remember { PrayerItem("dhuhr", "ظهر", "🕋", "13:05", false) }
    val asr = remember { PrayerItem("asr", "عصر", "📿", "16:42", false) }
    val maghrib = remember { PrayerItem("maghrib", "مغرب", "🌅", "19:58", false) }
    val isha = remember { PrayerItem("isha", "عشاء", "🌌", "21:20", false) }
    val items = remember { listOf(fajr, dhuhr, asr, maghrib, isha) }
    val activeIndex = 2 // معاينة ثابتة فقط لأغراض العرض

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xD9121212))
            .border(BorderStroke(0.5.dp, PrayerTheme.BORDER), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("21 محرم 1448", color = Color(0xFF9E9E9E), fontSize = 11.sp)
            Text("- 01:15:30", color = PrayerTheme.ACCENT, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                val isActive = index == activeIndex
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222222))
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(item.label, color = Color.White, fontSize = 11.sp)
                            Text(item.time, color = PrayerTheme.ACCENT, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(item.label, color = Color(0xFF9E9E9E), fontSize = 11.sp)
                            Text(item.time, color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun requestPinPrayerWidget(context: Context): Boolean {
    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
    val provider = android.content.ComponentName(context, PrayerWidgetReceiver::class.java)
    return if (appWidgetManager.isRequestPinAppWidgetSupported) {
        appWidgetManager.requestPinAppWidget(provider, null, null)
    } else {
        false
    }
}



@Composable
fun WidgetSetupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND)) {

        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PrayerTheme.PRIMARY_TEXT)
            }
            Spacer(Modifier.width(4.dp))
            Text("إضافة الويدجت", color = PrayerTheme.PRIMARY_TEXT, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text("اضغط على الويدجت لإضافته إلى شاشتك الرئيسية", color = PrayerTheme.SECONDARY_TEXT, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))

            // المعاينة دابا مطابقة للويدجت الحقيقي بالمليمتر
            WidgetPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clickable { showConfirmDialog = true }
            )

            Spacer(Modifier.height(20.dp))
            resultMessage?.let {
                Text(it, color = PrayerTheme.ACCENT, fontSize = 13.sp)
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = PrayerTheme.SURFACE,
            title = { Text("إضافة الويدجت", color = PrayerTheme.PRIMARY_TEXT) },
            text = { Text("هل تريد إضافة ويدجت مواقيت الصلاة إلى الشاشة الرئيسية؟", color = PrayerTheme.SECONDARY_TEXT) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    resultMessage = if (requestPinPrayerWidget(context)) {
                        "تم إرسال طلب الإضافة، أكّد من نافذة النظام"
                    } else {
                        "جهازك لا يدعم الإضافة التلقائية، أضفه يدوياً."
                    }
                }) { Text("نعم", color = PrayerTheme.ACCENT) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("لا", color = PrayerTheme.SECONDARY_TEXT)
                }
            }
        )
    }
}

@Composable
private fun WidgetPreview(modifier: Modifier = Modifier) {
    // عوض ما نخدمو بـ PrayerItem ونجيبو خطأ، نديرو ليست ديال المعاينة خفيفة
    val items = listOf(
        "فجر" to "04:12",
        "ظهر" to "13:05",
        "عصر" to "16:42",
        "مغرب" to "19:58",
        "عشاء" to "21:20"
    )
    val activeIndex = 2 

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xD9121212))
            .border(BorderStroke(0.5.dp, PrayerTheme.BORDER), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp) // ردينا البادينغ 6.dp باش يتنفس
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("21 محرم 1448", color = Color(0xFF9E9E9E), fontSize = 10.sp)
            // حيدنا الثواني باش يكون مطابق للواقع
            Text("- 01:15", color = PrayerTheme.ACCENT, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            items.forEachIndexed { index, pair ->
                val isActive = index == activeIndex
                val (label, time) = pair
                
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222222))
                                .padding(vertical = 2.dp, horizontal = 1.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(label, color = Color.White, fontSize = 10.sp)
                            Text(time, color = PrayerTheme.ACCENT, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, color = Color(0xFF9E9E9E), fontSize = 10.sp)
                            Text(time, color = Color(0xFF9E9E9E), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// الدالة المصلحة ضد الـ Crash
private fun requestPinPrayerWidget(context: Context): Boolean {
    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
    val provider = android.content.ComponentName(context, PrayerWidgetReceiver::class.java)
    
    // الحل الإجباري باش التطبيق ما يطرطقش فـ الأجهزة القديمة
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(provider, null, null)
            true
        } else false
    } else {
        false
    }
}







