@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
package com.tor

// ============== IMPORTS ==============
import com.icon.BrightnessEmpty

import android.app.Activity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

import androidx.compose.ui.platform.LocalDensity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.IconButton


import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween

import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.ui.res.painterResource
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.core.FastOutSlowInEasing

import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CheckCircle

// ==================== الألوان ====================
val BgScreen   = Color(0xFFFFFDF8)
val GreenMain  = Color(0xFF1B4D3E)
val GoldDark   = Color(0xFFB8860B)
val WordBlue   = Color(0xFF1565C0)
val TextMain   = Color(0xFF2C3E2D)
val ToolbarBg  = Color(0xFFF0EDE4)
val DividerCol = Color(0xFFD6CDB8)
val TabBg      = Color(0xFFE8F4F0)
val TabUnsel   = Color(0xFF7A9E8E)

// ==================== الموديلات الأساسية ====================
data class Ayah(
    val id: Int, val jozz: Int, val sura_no: Int,
    val sura_name_ar: String, val page: Int,
    val aya_no: Int, val aya_text: String
)
data class SurahWithAyahs(val surahName: String, val ayahs: List<Ayah>)
data class PageWithSurahs(val pageNumber: Int, val surahs: List<SurahWithAyahs>)
data class SurahInfo(val name: String, val soraNumber: Int, val pageListIndex: Int)
data class Bookmark(val id: Int, val ayahId: Int, val surahName: String, val ayahNumber: Int, val pageNumber: Int, val date: String, val time: String)

data class AyahTiming(val surahNumber: Int, val ayahNumber: Int, val timestampFrom: Long, val timestampTo: Long, val segments: List<Triple<Int, Long, Long>>)

// ==================== موديلات البيانات الإضافية ====================
data class JuzItem(val number: Int, val firstKey: String)
data class HizbItem(val number: Int, val firstKey: String)
data class RubItem(val number: Int, val firstKey: String)
data class SajdahItem(val number: Int, val surahNumber: Int, val ayahNumber: Int)
data class SurahDetailItem(
    val surahNo: Int,
    val name: String,
    val details: String,
    val page: Int,
    val firstAyahId: Int,
    val firstAyahText: String
)
data class PageItem(val pageNumber: Int, val firstAyahId: Int, val firstAyahText: String, val surahName: String, val jozzNumber: Int)
data class HizbWithRubs(val hizbNumber: Int, val hizbPage: Int, val rubs: List<RubCardItem>)
data class RubCardItem(val rubNumber: Int, val rubLabel: String, val firstAyahId: Int, val firstAyahText: String, val surahName: String, val page: Int)

data class JuzWithAyah(val juz: JuzItem, val ayah: Ayah?)
data class HizbWithAyah(val hizb: HizbItem, val ayah: Ayah?)
data class SajdahWithAyah(val sajdah: SajdahItem, val ayah: Ayah?)

data class PositionInfo(val surahName: String, val juz: Int, val hizb: Int, val rub: Int)

data class SurahDownloadState(val surahNumber: Int, val totalAyahs: Int = 0, val isDownloading: Boolean = false, val isDownloaded: Boolean = false, val downloadSpeedKBps: Double = 0.0, val bytesDownloaded: Long = 0L, val totalBytes: Long = 0L) {
    val progress: Float get() = if (totalBytes == 0L) 0f else bytesDownloaded.toFloat() / totalBytes
}
// ==================== دوال قراءة القواعد ====================
fun loadJuzList(ctx: Context): List<JuzItem> {
    val f = ctx.getDatabasePath("data.db"); if (!f.exists()) return emptyList()
    val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    val list = mutableListOf<JuzItem>()
    val c = db.rawQuery("SELECT juz_number, first_verse_key FROM juz_list ORDER BY juz_number", null)
    while (c.moveToNext()) list.add(JuzItem(c.getInt(0), c.getString(1)))
    c.close(); db.close(); return list
}

fun loadRubList(ctx: Context): List<RubItem> {
    val f = ctx.getDatabasePath("data.db"); if (!f.exists()) return emptyList()
    val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    val list = mutableListOf<RubItem>()
    val c = db.rawQuery("SELECT rub_number, first_verse_key FROM rub_list ORDER BY rub_number", null)
    while (c.moveToNext()) list.add(RubItem(c.getInt(0), c.getString(1)))
    c.close(); db.close(); return list
}

fun loadHizbList(ctx: Context): List<HizbItem> {
    val f = ctx.getDatabasePath("data.db"); if (!f.exists()) return emptyList()
    val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    val list = mutableListOf<HizbItem>()
    val c = db.rawQuery("SELECT hizb_number, first_verse_key FROM hizb_list ORDER BY hizb_number", null)
    while (c.moveToNext()) list.add(HizbItem(c.getInt(0), c.getString(1)))
    c.close(); db.close(); return list
}

fun loadSajdahList(ctx: Context): List<SajdahItem> {
    val f = ctx.getDatabasePath("data.db"); if (!f.exists()) return emptyList()
    val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    val list = mutableListOf<SajdahItem>()
    val c = db.rawQuery("SELECT sajdah_number, surah_number, ayah_number FROM sajdah_list ORDER BY sajdah_number", null)
    while (c.moveToNext()) list.add(SajdahItem(c.getInt(0), c.getInt(1), c.getInt(2)))
    c.close(); db.close(); return list
}

fun loadSurahDetails(ctx: Context, allAyahs: List<Ayah>): List<SurahDetailItem> {
    val surahFirstAyah = mutableMapOf<Int, Ayah>()
    for (ayah in allAyahs) {
        if (ayah.aya_no == 1 && !surahFirstAyah.containsKey(ayah.sura_no))
            surahFirstAyah[ayah.sura_no] = ayah
    }
    return try {
        val f = ctx.getDatabasePath("quran.db"); if (!f.exists()) return emptyList()
        val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val list = mutableListOf<SurahDetailItem>()
        val c = db.rawQuery("SELECT id, name_ar, details FROM surah_details ORDER BY id ASC", null)
        while (c.moveToNext()) {
            val no = c.getInt(0)
            val name = c.getString(1)?: ""
            val details = c.getString(2)?: ""
            val fa = surahFirstAyah[no]
            list.add(SurahDetailItem(no, name, details, fa?.page?: 0, fa?.id?: 0, fa?.aya_text?: ""))
        }
        c.close(); db.close(); list
    } catch (e: Exception) { Log.e("IDX", "loadSurahDetails: $e"); emptyList() }
}

fun loadPageItems(ctx: Context): List<PageItem> {
    val result = mutableListOf<PageItem>()
    try {
        val db = SQLiteDatabase.openDatabase(ctx.getDatabasePath("quran.db").absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val c = db.rawQuery("SELECT page, id, aya_text, sora_name_ar, jozz FROM quran WHERE id IN (SELECT MIN(id) FROM quran GROUP BY page) ORDER BY page ASC", null)
        while (c.moveToNext()) result.add(PageItem(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getInt(4)))
        c.close(); db.close()
    } catch (e: Exception) { Log.e("IDX", "loadPageItems: $e") }
    return result
}

// ==================== دوال الفهرس مع النصوص ====================
fun loadJuzWithAyahs(ctx: Context, allAyahs: List<Ayah>): List<JuzWithAyah> {
    val juzList = loadJuzList(ctx)
    return juzList.map { juz ->
        val ayah = parseVerseKey(juz.firstKey)?.let { (s, a) ->
            allAyahs.find { it.sura_no == s && it.aya_no == a }
        }
        JuzWithAyah(juz, ayah)
    }
}

fun loadHizbWithRubsAndAyahs(ctx: Context, allAyahs: List<Ayah>): List<HizbWithRubs> {
    val rubList = loadRubList(ctx)
    if (rubList.isEmpty()) return emptyList()
    val hizbList = loadHizbList(ctx)
    val hizbFirstKeys = hizbList.associateBy({ it.number }, { it.firstKey })

    return rubList.groupBy { (it.number - 1) / 4 + 1 }.map { (hizbNo, rubs) ->
        val rubCards = rubs.mapIndexed { index, rub ->
            val ayah = parseVerseKey(rub.firstKey)?.let { (s, a) ->
                allAyahs.find { it.sura_no == s && it.aya_no == a }
            }
            val hizbFirstKey = hizbFirstKeys[hizbNo]
            val isStartOfHizb = (index == 0) && (hizbFirstKey != null && rub.firstKey == hizbFirstKey)
            val label = when {
                isStartOfHizb -> "الحزب ${convertToArabicNumber(hizbNo)}"
                index == 1 -> "الربع"
                index == 2 -> "النصف"
                index == 3 -> "ثلاثة أرباع الحزب"
                else -> "الربع"
            }
            RubCardItem(rub.number, label, ayah?.id?: 0, ayah?.aya_text?: "", ayah?.sura_name_ar?: "", ayah?.page?: 0)
        }
        HizbWithRubs(hizbNo, rubCards.firstOrNull()?.page?: 0, rubCards)
    }
}

fun loadSajdahWithAyahs(ctx: Context, allAyahs: List<Ayah>): List<SajdahWithAyah> {
    val sajdahList = loadSajdahList(ctx)
    return sajdahList.map { sajdah ->
        val ayah = allAyahs.find { it.sura_no == sajdah.surahNumber && it.aya_no == sajdah.ayahNumber }
        SajdahWithAyah(sajdah, ayah)
    }
}

// ==================== بناء الصفحات ====================
fun buildPages(dbPath: String): List<PageWithSurahs> {
    val map = mutableMapOf<Int, MutableList<Ayah>>()
    val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
    val c = db.rawQuery("SELECT id, jozz, sora, sora_name_ar, page, aya_no, aya_text FROM quran ORDER BY page ASC, aya_no ASC", null)
    while (c.moveToNext()) {
        val page = c.getInt(c.getColumnIndexOrThrow("page"))
        map.getOrPut(page) { mutableListOf() }.add(Ayah(c.getInt(c.getColumnIndexOrThrow("id")), c.getInt(c.getColumnIndexOrThrow("jozz")), c.getInt(c.getColumnIndexOrThrow("sora")), c.getString(c.getColumnIndexOrThrow("sora_name_ar")), page, c.getInt(c.getColumnIndexOrThrow("aya_no")), c.getString(c.getColumnIndexOrThrow("aya_text"))))
    }
    c.close(); db.close()
    return map.map { (p, ayahs) -> PageWithSurahs(p, ayahs.groupBy { it.sura_name_ar }.map { (n,l) -> SurahWithAyahs(n,l) }) }.sortedBy { it.pageNumber }
}

fun buildSurahList(pages: List<PageWithSurahs>): List<SurahInfo> {
    val seen = mutableSetOf<String>()
    return buildList { pages.forEachIndexed { i, page -> page.surahs.forEach { s -> if (s.ayahs.first().aya_no == 1 && seen.add(s.surahName)) add(SurahInfo(s.surahName, s.ayahs.first().sura_no, i)) } } }
}

fun getTafsirMoyassar(ctx: Context, sura: Int, aya: Int): String? {
    return try {
        val f = ctx.getDatabasePath("moyssar.db"); if (!f.exists()) return null
        val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val c = db.rawQuery("SELECT text FROM tafsir WHERE ayah_key=?", arrayOf("$sura:$aya"))
        val r = if (c.moveToFirst()) c.getString(0) else null; c.close(); db.close(); r
    } catch (e: Exception) { null }
}

// ==================== إدارة العلامات ====================
private fun parseBookmarks(json: String): MutableList<Bookmark> {
    val list = mutableListOf<Bookmark>(); if (json == "[]") return list
    val clean = json.removePrefix("[").removeSuffix("]"); if (clean.isBlank()) return list
    clean.split("},").forEach { item ->
        val c = item.trim().removePrefix("{").removeSuffix("}"); if (c.isBlank()) return@forEach
        try { list.add(Bookmark(c.substringAfter("\"id\":").substringBefore(",").toInt(), c.substringAfter("\"ayahId\":").substringBefore(",").toInt(), c.substringAfter("\"surahName\":\"").substringBefore("\""), c.substringAfter("\"ayahNumber\":").substringBefore(",").toInt(), c.substringAfter("\"pageNumber\":").substringBefore(",").toInt(), c.substringAfter("\"date\":\"").substringBefore("\""), c.substringAfter("\"time\":\"").substringBefore("\"")))} catch (_: Exception) {}
    }
    return list
}

private fun serializeBookmarks(list: List<Bookmark>): String {
    val sb = StringBuilder("[")
    list.forEachIndexed { i, b -> sb.append("{\"id\":${b.id},\"ayahId\":${b.ayahId},\"surahName\":\"${b.surahName}\",\"ayahNumber\":${b.ayahNumber},\"pageNumber\":${b.pageNumber},\"date\":\"${b.date}\",\"time\":\"${b.time}\"}"); if (i < list.size-1) sb.append(",") }
    return sb.append("]").toString()
}

fun saveBookmark(ctx: Context, ayah: Ayah) {
    val prefs = ctx.getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
    val list = parseBookmarks(prefs.getString("list","[]") ?: "[]")
    if (list.none { it.ayahId == ayah.id }) {
        val now = Date(); val newId = (list.maxOfOrNull { it.id } ?: 0) + 1
        list.add(Bookmark(newId, ayah.id, ayah.sura_name_ar, ayah.aya_no, ayah.page, SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(now), SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)))
        prefs.edit().putString("list", serializeBookmarks(list)).apply()
    }
}

fun loadBookmarks(ctx: Context): List<Bookmark> = parseBookmarks(ctx.getSharedPreferences("bookmarks", Context.MODE_PRIVATE).getString("list","[]") ?: "[]")

fun deleteBookmark(ctx: Context, id: Int) {
    val prefs = ctx.getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
    val list = parseBookmarks(prefs.getString("list","[]") ?: "[]"); list.removeAll { it.id == id }
    prefs.edit().putString("list", serializeBookmarks(list)).apply()
}

// ==================== إدارة التفاسير ====================
data class TafsirSource(val id: String, val name: String, val url: String, val sizeBytes: Long)
data class TafsirDownloadState(val isDownloading: Boolean = false, val progress: Float = 0f, val isDownloaded: Boolean = false)

val tafsirSources = listOf(
    TafsirSource("ar-tafseer-al-saddi","تفسير السعدي","https://github.com/fiodourhalim-source/Al_forkan/releases/download/Mushaf/ar-tafseer-al-saddi.db",30_000_000),
    TafsirSource("ar-tafseer-al-qurtubi","تفسير القرطبي","https://github.com/fiodourhalim-source/Al_forkan/releases/download/Mushaf/ar-tafseer-al-qurtubi.db",40_000_000),
    TafsirSource("ar-tafsir-ibn-kathir","تفسير ابن كثير","https://github.com/fiodourhalim-source/Al_forkan/releases/download/Mushaf/ar-tafsir-ibn-kathir.db",35_000_000),
    TafsirSource("ar-tafsir-al-wasit","التفسير الوسيط","https://github.com/fiodourhalim-source/Al_forkan/releases/download/Mushaf/ar-tafsir-al-wasit.db",25_000_000)
)

fun downloadTafsir(context: Context, source: TafsirSource, onProgress: (Float) -> Unit, onComplete: (Boolean) -> Unit) {
    val dbFile = context.getDatabasePath(source.id + ".db"); val tempFile = File(dbFile.absolutePath + ".tmp"); dbFile.parentFile?.mkdirs()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).connectTimeout(30,TimeUnit.SECONDS).readTimeout(120,TimeUnit.SECONDS).build()
            val response = client.newCall(Request.Builder().url(source.url).build()).execute()
            if (!response.isSuccessful) { withContext(Dispatchers.Main) { onComplete(false) }; return@launch }
            val body = response.body ?: run { withContext(Dispatchers.Main) { onComplete(false) }; return@launch }
            val total = body.contentLength(); val input = body.byteStream(); val output = tempFile.outputStream()
            val buffer = ByteArray(8192); var read = 0L; var n: Int
            while (input.read(buffer).also { n = it } != -1) { output.write(buffer,0,n); read += n; withContext(Dispatchers.Main) { onProgress(if (total > 0) read.toFloat()/total else 0f) } }
            output.flush(); output.close(); input.close(); response.close()
            if (tempFile.exists()) { dbFile.delete(); if (!tempFile.renameTo(dbFile)) { tempFile.copyTo(dbFile,true); tempFile.delete() } }
            withContext(Dispatchers.Main) { saveDownloadedTafsir(context, source.id, source.name); onComplete(true) }
        } catch (e: Exception) { tempFile.delete(); withContext(Dispatchers.Main) { onComplete(false) } }
    }
}

fun saveDownloadedTafsir(context: Context, id: String, name: String) {
    val prefs = context.getSharedPreferences("tafsirs", Context.MODE_PRIVATE)
    val list = getDownloadedTafsirs(context).toMutableList(); if (list.none { it.first == id }) list.add(id to name)
    val json = buildString { append("["); list.forEachIndexed { i,(a,b) -> append("{\"id\":\"$a\",\"name\":\"$b\"}"); if (i < list.size-1) append(",") }; append("]") }
    prefs.edit().putString("downloaded", json).apply()
}

fun getDownloadedTafsirs(context: Context): List<Pair<String,String>> {
    val prefs = context.getSharedPreferences("tafsirs", Context.MODE_PRIVATE)
    val json = prefs.getString("downloaded","[]") ?: return emptyList()
    val result = mutableListOf<Pair<String,String>>(); if (json == "[]") return result
    val trimmed = json.removePrefix("[").removeSuffix("]"); if (trimmed.isBlank()) return result
    trimmed.split("},").forEach { item ->
        val c = item.trim().removePrefix("{").removeSuffix("}")
        val id = c.substringAfter("\"id\":\"").substringBefore("\""); val name = c.substringAfter("\"name\":\"").substringBefore("\"")
        if (id.isNotBlank()) result.add(id to name)
    }
    return result
}

fun deleteTafsir(context: Context, id: String) {
    val f = context.getDatabasePath("$id.db"); if (f.exists()) f.delete()
    val list = getDownloadedTafsirs(context).toMutableList(); list.removeAll { it.first == id }
    val json = buildString { append("["); list.forEachIndexed { i,(a,b) -> append("{\"id\":\"$a\",\"name\":\"$b\"}"); if (i < list.size-1) append(",") }; append("]") }
    context.getSharedPreferences("tafsirs", Context.MODE_PRIVATE).edit().putString("downloaded", json).apply()
}

fun getTafsirFromDatabase(context: Context, dbFileName: String, sura: Int, aya: Int): String? {
    val f = context.getDatabasePath(dbFileName); if (!f.exists()) return null
    return try {
        val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val c = db.rawQuery("SELECT text FROM tafsir WHERE ayah_key=?", arrayOf("$sura:$aya"))
        val r = if (c.moveToFirst()) c.getString(0) else null; c.close(); db.close(); r
    } catch (e: Exception) { null }
}

// ==================== أدوات مساعدة ====================
fun loadFont(ctx: Context, name: String): Typeface = Typeface.createFromAsset(ctx.assets, "font/$name")

fun convertToArabicNumber(n: Int): String {
    val d = arrayOf("٠","١","٢","٣","٤","٥","٦","٧","٨","٩")
    return n.toString().map { if (it.isDigit()) d[it.digitToInt()] else it.toString() }.joinToString("")
}

fun stripTashkeel(text: String): String =
    text.replace(Regex("[\u064B-\u065F\u0670]"), "")
        .replace("أ","ا").replace("إ","ا").replace("آ","ا")
        .replace("ة","ه").replace("ى","ي")

fun parseVerseKey(key: String): Pair<Int,Int>? {
    val p = key.split(":"); return if (p.size == 2) Pair(p[0].toIntOrNull() ?: return null, p[1].toIntOrNull() ?: return null) else null
}



object PlayerManager {
    var exoPlayer: ExoPlayer? = null
    val currentAyah      = mutableStateOf<Ayah?>(null)
    val currentWordIndex = mutableStateOf(-1)
    val isPlaying        = mutableStateOf(false)
    val ayahProgress      = mutableStateOf(0f)
    val isRepeatOne       = mutableStateOf(false)
    val playbackSpeed     = mutableStateOf(1.0f)
    val sleepTimerMinutesLeft = mutableStateOf<Int?>(null)
    val loopMode          = mutableStateOf("none") // "none" | "ayah" | "surah" | "custom"
    val loopCount         = mutableStateOf(1) // -1 = مستمر
    val loopRangeStart    = mutableStateOf<Int?>(null) // ayah_in_surah
    val loopRangeEnd      = mutableStateOf<Int?>(null)
    val playbackUiState   = mutableStateOf("idle")

    private var loopCounter = 0
    private var sleepTimerJob: Job? = null
    private var bufferingJob: Job? = null
    private val segmentsCache = mutableMapOf<Int, Map<Int, List<Triple<Int, Long, Long>>>>()
    private var positionJob: Job? = null
    private var allAyahsRef: List<Ayah> = emptyList()
    private var playlistAyahNumbers: List<Int> = emptyList()
    private var currentSurahNo: Int = -1
    private var playerListener: Player.Listener? = null
    private var isOnlineMode = false

    fun setAllAyahs(ayahs: List<Ayah>) { allAyahsRef = ayahs }
    fun init(ctx: Context) { if (exoPlayer == null) exoPlayer = ExoPlayer.Builder(ctx).build() }

    fun playNextAyah() {
        val player = exoPlayer ?: return
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    fun playPreviousAyah() {
        val player = exoPlayer ?: return
        if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
    }

    fun hasInternet(ctx: Context): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed.value = speed
        exoPlayer?.setPlaybackSpeed(speed)
    }

    // ==================== مؤقت النوم - مصحح ومضمون ====================
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) return
        sleepTimerMinutesLeft.value = minutes
        sleepTimerJob = CoroutineScope(Dispatchers.Main).launch {
            var remaining = minutes
            while (remaining > 0 && isActive) {
                delay(60_000L)
                remaining -= 1
                sleepTimerMinutesLeft.value = remaining
            }
            if (isActive) {
                stop()
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        sleepTimerMinutesLeft.value = null
    }

    // ==================== ضبط التكرار - مصحح بالكامل ====================
    fun setLoopMode(mode: String, count: Int, rangeStart: Int? = null, rangeEnd: Int? = null) {
        loopMode.value = mode
        loopCount.value = count
        loopRangeStart.value = rangeStart
        loopRangeEnd.value = rangeEnd
        loopCounter = 0
        isRepeatOne.value = mode != "none"
    }

    fun clearLoop() {
        loopMode.value = "none"
        loopCount.value = 1
        loopRangeStart.value = null
        loopRangeEnd.value = null
        loopCounter = 0
        isRepeatOne.value = false
    }

    fun loadSurahSegments(ctx: Context, surahNo: Int) {
        if (segmentsCache.containsKey(surahNo)) return
        try {
            val verses = ShuraimData.getSurahVerses(ctx, surahNo)
            val map = mutableMapOf<Int, List<Triple<Int, Long, Long>>>()
            verses.forEach { v ->
                val ayahInSurah = ShuraimData.extractAyahInSurah(v.audio_url) ?: return@forEach
                val segs = v.segments.mapNotNull { seg ->
                    if (seg.size >= 3) Triple((seg[0] - 1).toInt(), seg[1], seg[2]) else null
                }
                map[ayahInSurah] = segs
            }
            segmentsCache[surahNo] = map
        } catch (e: Exception) { Log.e("PLAYER", "loadSurahSegments error", e) }
    }

    fun getAyahFile(ctx: Context, surahNo: Int, ayahInSurah: Int) = File(
        ctx.filesDir,
        "audio/shuraim/surah_${surahNo.toString().padStart(3, '0')}/${ayahInSurah.toString().padStart(3, '0')}.mp3"
    ).apply { parentFile?.mkdirs() }

    fun getSurahDir(ctx: Context, surahNo: Int) = File(
        ctx.filesDir, "audio/shuraim/surah_${surahNo.toString().padStart(3, '0')}"
    )

    fun isSurahReady(ctx: Context, surahNo: Int): Boolean {
        val dir = getSurahDir(ctx, surahNo)
        if (!dir.exists()) return false
        return File(dir, "001.mp3").exists()
    }

    private fun buildAvailableAyahList(ctx: Context, surahNo: Int): List<Int> {
        val dir = getSurahDir(ctx, surahNo)
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.extension == "mp3" }
            ?.mapNotNull { it.nameWithoutExtension.toIntOrNull() }
            ?.sorted() ?: emptyList()
    }

    fun playFromAyah(ctx: Context, ayah: Ayah): Boolean {
        val player = exoPlayer ?: return false
        loadSurahSegments(ctx, ayah.sura_no)

        val isLocal = isSurahReady(ctx, ayah.sura_no)
        if (isLocal) {
            isOnlineMode = false
            val availableAyahs = buildAvailableAyahList(ctx, ayah.sura_no)
            if (availableAyahs.isEmpty()) return false
            val startIndex = availableAyahs.indexOf(ayah.aya_no).let { if (it == -1) 0 else it }
            val ayahsToPlay = availableAyahs.subList(startIndex, availableAyahs.size)
            playPlaylist(ctx, ayah.sura_no, ayahsToPlay)
            return true
        } else {
            if (!hasInternet(ctx)) return false
            isOnlineMode = true
            val verses = ShuraimData.getSurahVerses(ctx, ayah.sura_no)
            if (verses.isEmpty()) return false
            val ayahsToPlay = verses.mapNotNull { ShuraimData.extractAyahInSurah(it.audio_url) }
                .filter { it >= ayah.aya_no }
            if (ayahsToPlay.isEmpty()) return false
            playPlaylistOnline(ctx, ayah.sura_no, ayahsToPlay, verses)
            return true
        }
    }

    private fun playPlaylist(ctx: Context, surahNo: Int, ayahNumbers: List<Int>) {
        val player = exoPlayer ?: return
        if (ayahNumbers.isEmpty()) return

        playerListener?.let { player.removeListener(it) }
        player.stop(); player.clearMediaItems()

        val mediaItems = ayahNumbers.map { ayahInSurah ->
            val file = getAyahFile(ctx, surahNo, ayahInSurah)
            MediaItem.fromUri(file.toURI().toString())
        }

        currentSurahNo = surahNo
        playlistAyahNumbers = ayahNumbers
        loopCounter = 0

        player.setMediaItems(mediaItems)
        player.setPlaybackSpeed(playbackSpeed.value)
        player.prepare()
        player.playWhenReady = true

        val firstAyahInSurah = ayahNumbers.first()
        currentAyah.value = allAyahsRef.find { it.sura_no == surahNo && it.aya_no == firstAyahInSurah }
        currentWordIndex.value = -1
        isPlaying.value = true
        playbackUiState.value = "playing"

        playerListener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val idx = player.currentMediaItemIndex
                val ayahInSurah = playlistAyahNumbers.getOrNull(idx) ?: return
                currentAyah.value = allAyahsRef.find { it.sura_no == currentSurahNo && it.aya_no == ayahInSurah }
                currentWordIndex.value = -1
            }
            override fun onPlaybackStateChanged(state: Int) {
                handlePlaybackState(ctx, state)
                if (state == Player.STATE_ENDED) {
                    handlePlaylistEnded(ctx, isOnline = false)
                }
            }
        }
        player.addListener(playerListener!!)
        startTracking(ctx)
    }

    private fun playPlaylistOnline(ctx: Context, surahNo: Int, ayahNumbers: List<Int>, verses: List<ShuraimVerseJson>) {
        val player = exoPlayer ?: return
        playerListener?.let { player.removeListener(it) }
        player.stop(); player.clearMediaItems()

        val mediaItems = ayahNumbers.mapNotNull { ayahInSurah ->
            val verse = verses.firstOrNull { ShuraimData.extractAyahInSurah(it.audio_url) == ayahInSurah }
            verse?.audio_url?.let { MediaItem.fromUri(it) }
        }
        if (mediaItems.isEmpty()) return

        currentSurahNo = surahNo
        playlistAyahNumbers = ayahNumbers
        loopCounter = 0

        player.setMediaItems(mediaItems)
        player.setPlaybackSpeed(playbackSpeed.value)
        player.prepare()
        player.playWhenReady = true

        val firstAyahInSurah = ayahNumbers.first()
        currentAyah.value = allAyahsRef.find { it.sura_no == surahNo && it.aya_no == firstAyahInSurah }
        currentWordIndex.value = -1
        isPlaying.value = true
        playbackUiState.value = "playing"

        playerListener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val idx = player.currentMediaItemIndex
                val ayahInSurah = playlistAyahNumbers.getOrNull(idx) ?: return
                currentAyah.value = allAyahsRef.find { it.sura_no == currentSurahNo && it.aya_no == ayahInSurah }
                currentWordIndex.value = -1
            }
            override fun onPlaybackStateChanged(state: Int) {
                handlePlaybackState(ctx, state)
                if (state == Player.STATE_ENDED) {
                    handlePlaylistEnded(ctx, isOnline = true, verses = verses)
                }
            }
        }
        player.addListener(playerListener!!)
        startTracking(ctx)
    }

    // ==================== معالجة نهاية الـ playlist (تكرار سورة أو الانتقال للسورة الموالية) ====================
    private fun handlePlaylistEnded(ctx: Context, isOnline: Boolean, verses: List<ShuraimVerseJson>? = null) {
        val player = exoPlayer ?: return

        if (loopMode.value == "surah") {
            if (loopCount.value == -1 || loopCounter < loopCount.value - 1) {
                loopCounter++
                player.seekTo(0, 0L)
                player.playWhenReady = true
                return
            } else {
                loopCounter = 0
                clearLoop()
            }
        }

        val nextSurahNo = currentSurahNo + 1
        if (nextSurahNo <= 114) {
            if (!isOnline && isSurahReady(ctx, nextSurahNo)) {
                loadSurahSegments(ctx, nextSurahNo)
                val nextAyahs = buildAvailableAyahList(ctx, nextSurahNo)
                if (nextAyahs.isNotEmpty()) { playPlaylist(ctx, nextSurahNo, nextAyahs); return }
            } else if (isOnline && hasInternet(ctx)) {
                loadSurahSegments(ctx, nextSurahNo)
                val nextVerses = ShuraimData.getSurahVerses(ctx, nextSurahNo)
                val nextAyahs = nextVerses.mapNotNull { ShuraimData.extractAyahInSurah(it.audio_url) }
                if (nextAyahs.isNotEmpty()) { playPlaylistOnline(ctx, nextSurahNo, nextAyahs, nextVerses); return }
            }
        }
        stop()
    }

    private fun handlePlaybackState(ctx: Context, state: Int) {
        when (state) {
            Player.STATE_BUFFERING -> {
                if (isOnlineMode) {
                    playbackUiState.value = "buffering"
                    bufferingJob?.cancel()
                    bufferingJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(15_000)
                        if (playbackUiState.value == "buffering") {
                            playbackUiState.value = "error"
                            exoPlayer?.pause()
                        }
                    }
                }
            }
            Player.STATE_READY -> {
                bufferingJob?.cancel()
                if (playbackUiState.value != "error") {
                    playbackUiState.value = if (exoPlayer?.isPlaying == true) "playing" else "paused"
                }
            }
        }
    }

    fun retryAfterError(ctx: Context) {
        if (hasInternet(ctx)) {
            playbackUiState.value = "buffering"
            exoPlayer?.prepare()
            exoPlayer?.play()
        }
    }

    fun switchReciterAndContinue(ctx: Context) {
        val ayah = currentAyah.value ?: return
        val wasPlaying = isPlaying.value
        stop()
        if (wasPlaying) playFromAyah(ctx, ayah)
    }

    // ==================== تتبع الموضع + منطق التكرار الكامل (آية/مخصص) ====================
    private fun startTracking(ctx: Context) {
        stopTracking()
        positionJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val player = exoPlayer ?: break
                if (!player.isPlaying) { delay(80); continue }

                val idx = player.currentMediaItemIndex
                val ayahInSurah = playlistAyahNumbers.getOrNull(idx)
                val pos = player.currentPosition
                val dur = player.duration

                ayahProgress.value = if (dur > 0) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f

                if (ayahInSurah != null) {
                    val segs = segmentsCache[currentSurahNo]?.get(ayahInSurah) ?: emptyList()
                    val segment = segs.lastOrNull { (_, from, _) -> pos >= from }
                    val wIdx = segment?.first ?: -1
                    if (wIdx != -1 && wIdx != currentWordIndex.value) currentWordIndex.value = wIdx
                }

                // ===== منطق التكرار: آية واحدة =====
                if (loopMode.value == "ayah" && dur > 0 && pos >= dur - 80) {
                    if (loopCount.value == -1 || loopCounter < loopCount.value - 1) {
                        loopCounter++
                        player.seekTo(0)
                        ayahProgress.value = 0f
                    } else {
                        loopCounter = 0
                        clearLoop()
                        // كيكمل للآية الموالية تلقائيا (ExoPlayer كيدير transition وحدو)
                    }
                }

                // ===== منطق التكرار: نطاق مخصص (من آية X لـ آية Y) =====
                if (loopMode.value == "custom" && ayahInSurah != null) {
                    val rStart = loopRangeStart.value
                    val rEnd = loopRangeEnd.value
                    if (rStart != null && rEnd != null && ayahInSurah == rEnd && dur > 0 && pos >= dur - 80) {
                        if (loopCount.value == -1 || loopCounter < loopCount.value - 1) {
                            loopCounter++
                            val startIdx = playlistAyahNumbers.indexOf(rStart)
                            if (startIdx != -1) {
                                player.seekTo(startIdx, 0L)
                                ayahProgress.value = 0f
                            }
                        } else {
                            loopCounter = 0
                            clearLoop()
                        }
                    }
                }

                delay(20)
            }
        }
    }

    private fun stopTracking() { positionJob?.cancel(); positionJob = null }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) { player.pause(); isPlaying.value = false; playbackUiState.value = "paused" }
        else { player.play(); isPlaying.value = true; playbackUiState.value = "playing" }
    }

    fun stop() {
        stopTracking()
        cancelSleepTimer()
        bufferingJob?.cancel()
        playerListener?.let { exoPlayer?.removeListener(it) }
        playerListener = null
        exoPlayer?.stop(); exoPlayer?.clearMediaItems()
        isPlaying.value = false; currentAyah.value = null; currentWordIndex.value = -1
        playlistAyahNumbers = emptyList(); currentSurahNo = -1
        ayahProgress.value = 0f
        loopCounter = 0
        clearLoop()
        playbackUiState.value = "idle"
    }

    fun release() { stop(); exoPlayer?.release(); exoPlayer = null }
}







// ==================== MainActivity ====================
class MainActivity : ComponentActivity() {

    private fun copyDb(name: String): File {
        val out = getDatabasePath(name); out.parentFile?.mkdirs()
        if (out.exists() && out.length() > 0) return out
        assets.open(name).use { i -> FileOutputStream(out).use { o -> val buf = ByteArray(8192); var len: Int; while (i.read(buf).also { len = it } > 0) o.write(buf,0,len); o.flush() } }
        if (name == "moyssar.db") SQLiteDatabase.openDatabase(out.absolutePath, null, SQLiteDatabase.OPEN_READWRITE).use { db -> db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).close() }
        return out
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val quranDb = copyDb("quran.db")
        copyDb("moyssar.db")
        copyDb("data.db")

        PlayerManager.init(this)

        // تحميل بيانات الشريم مرة واحدة (تعبئة الكاش)
        ShuraimData.load(this)

        val pages = buildPages(quranDb.absolutePath)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = BgScreen) {
                    QuranScreen(pages)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayerManager.release()
    }
}




// ==================== PremiumProMaxPlusDrawer ====================
@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PremiumProMaxPlusDrawer(
    isHorizontalMode: Boolean, onHorizontalModeChange: (Boolean) -> Unit,
    fontSize: Float, onFontSizeChange: (Float) -> Unit,
    useMedinaFont: Boolean, onMedinaFontChange: (Boolean) -> Unit,
    unicodeEnabled: Boolean, onUnicodeEnabledChange: (Boolean) -> Unit,
    onClose: () -> Unit, onOpenAudioLibrary: () -> Unit
) {
    val meQuran = FontFamily(loadFont(LocalContext.current, "me_quran.ttf"))
    ModalDrawerSheet(modifier = Modifier.width(320.dp), drawerContainerColor = Color.White) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
                Box(Modifier.fillMaxWidth().height(60.dp).background(Color(0xFFF8F4E6)), contentAlignment = Alignment.Center) {
                    Text("الإعدادات", fontFamily = FontFamily.Default, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E2D), modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp))
                    IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = GreenMain, modifier = Modifier.size(28.dp))
                    }
                }
                Divider(color = DividerCol, thickness = 0.5.dp)
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onHorizontalModeChange(false) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (!isHorizontalMode) GreenMain else Color.LightGray), colors = ButtonDefaults.outlinedButtonColors(contentColor = if (!isHorizontalMode) GreenMain else Color.Gray)) { Text("عمودي", fontFamily = FontFamily.Default) }
                    OutlinedButton(onClick = { onHorizontalModeChange(true) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (isHorizontalMode) GreenMain else Color.LightGray), colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isHorizontalMode) GreenMain else Color.Gray)) { Text("أفقي", fontFamily = FontFamily.Default) }
                }
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF9F0)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Mic, contentDescription = null, tint = GoldDark, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("المكتبة الصوتية", fontFamily = FontFamily.Default, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { onOpenAudioLibrary() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                            Text("تحميل الاصوات", fontFamily = FontFamily.Default)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("حجم النص", fontFamily = FontFamily.Default, fontSize = 14.sp, color = TextMain)
                    Spacer(Modifier.height(4.dp))
                    Text("الرحمن الرحيم", fontFamily = meQuran, fontSize = fontSize.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Slider(value = fontSize, onValueChange = onFontSizeChange, valueRange = 16f..36f, modifier = Modifier.fillMaxWidth())
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                Text("جمالية النص القرآني", fontFamily = FontFamily.Default, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenMain, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                ListItem(
                    headlineContent = { Text("نوع الخط العثماني", fontFamily = FontFamily.Default) },
                    trailingContent = { TextButton(onClick = { onMedinaFontChange(!useMedinaFont) }) { Text(if (useMedinaFont) "خط المدينة المطور" else "خط حفص", color = GreenMain) } }
                )
                ListItem(
                    headlineContent = { Text("الزخرفة التلقائية للأرقام") },
                    supportingContent = { Text("تحويل أقواس الآيات إلى رموز يونيكود فخمة", fontSize = 12.sp, color = Color.Gray) },
                    trailingContent = { Switch(checked = unicodeEnabled, onCheckedChange = onUnicodeEnabledChange, colors = SwitchDefaults.colors(checkedThumbColor = GreenMain)) }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ==================== دوال حساب الحزب والربع - مقارنة رقمية صحيحة (ماشي نصية) ====================
// المشكلة الأصلية: SQL كان كيقارن first_verse_key كنص ("10:5" < "2:1" نصيا!) فهذا كان كيخلي
// الحزب/الربع يبقاو ثابتين على آخر قيمة غلط. هنا كنقارنو سورة:آية رقميا.

fun findHizbNumber(hizbList: List<HizbItem>, sura: Int, aya: Int): Int {
    var result = 1
    for (h in hizbList) {
        val parsed = parseVerseKey(h.firstKey) ?: continue
        val (s, a) = parsed
        if (s < sura || (s == sura && a <= aya)) result = h.number else break
    }
    return result
}

fun findRubNumber(rubList: List<RubItem>, sura: Int, aya: Int): Int {
    var result = 1
    for (r in rubList) {
        val parsed = parseVerseKey(r.firstKey) ?: continue
        val (s, a) = parsed
        if (s < sura || (s == sura && a <= aya)) result = r.number else break
    }
    return result
}

fun getCurrentPositionInfoFast(ayah: Ayah?, hizbList: List<HizbItem>, rubList: List<RubItem>): PositionInfo {
    if (ayah == null) return PositionInfo("", 1, 1, 1)
    val hizb = findHizbNumber(hizbList, ayah.sura_no, ayah.aya_no)
    val rub = findRubNumber(rubList, ayah.sura_no, ayah.aya_no)
    return PositionInfo(ayah.sura_name_ar, ayah.jozz, hizb, rub)
}


// ==================== FramedSurahName (مصحح: تمديد دقيق للإطار + توسيط الاسم) ====================
@Composable
fun FramedSurahName(
    surahNo: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val elgharibFont = remember {
        FontFamily(Typeface.createFromAsset(context.assets, "font/elgharib.ttf"))
    }
    val surahNameFont = remember {
        FontFamily(Typeface.createFromAsset(context.assets, "font/surah_name_v4.ttf"))
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp) // مسافة 2dp فقط من حواف الشاشة
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        val frameFontSize = with(density) { maxHeightPx.toSp() * 0.85f }
        val nameFontSize = with(density) { maxHeightPx.toSp() * 0.5f }

        // قياس العرض الطبيعي لنص الإطار، باش نحسبو نسبة التمديد الأفقي المطلوبة
        val frameStyle = remember(frameFontSize) {
            TextStyle(fontFamily = elgharibFont, fontSize = frameFontSize)
        }
        val frameLayoutResult = remember(frameStyle) {
            textMeasurer.measure(text = "10", style = frameStyle)
        }
        
        val frameNaturalWidth: Float = frameLayoutResult.size.width.toFloat()
val scaleX: Float = if (frameNaturalWidth > 0f) (maxWidthPx / frameNaturalWidth) else 1f
        
        
        // 1. الإطار - فونط elgharib، ممدود أفقيا باش يطابق العرض المتاح بالضبط
        Text(
            text = "10",
            fontFamily = elgharibFont,
            fontSize = frameFontSize,
            color = Color(0xFFB8860B),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center)
                .graphicsLayer {
                    this.scaleX = scaleX
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
        )

        // 2. اسم السورة - متوسط بالضبط داخل الإطار
        Text(
            text = "surah${surahNo.toString().padStart(3, '0')}" + "surah-icon",
            fontFamily = surahNameFont,
            fontSize = nameFontSize,
            color = Color(0xFFB8860B),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
    }
}

@Composable
fun QuranScreen(pages: List<PageWithSurahs>) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var showAudioPanel by remember { mutableStateOf(false) }
    var selectedAyah by remember { mutableStateOf<Ayah?>(null) }
    var showTafsirSheet by remember { mutableStateOf(false) }
    var tafsirAyahIndex by remember { mutableStateOf(0) }
    var showRecitersSheet by remember { mutableStateOf(false) }
    var showQuranIndex by remember { mutableStateOf(false) }
    var showBook by remember { mutableStateOf(false) }
    var showAudioLibraryScreen by remember { mutableStateOf(false) }
    var horiz by remember { mutableStateOf(false) }
    var marks by remember { mutableStateOf(loadBookmarks(ctx)) }
    var highlightedAyahId by remember { mutableStateOf<Int?>(null) }
    var lastSelectedAyahId by remember { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val quranFont = remember { FontFamily(loadFont(ctx, "hafs.ttf")) }
    val medinaFont = remember { FontFamily(loadFont(ctx, "me_quran.ttf")) }
    val meQuran = remember { FontFamily(loadFont(ctx, "me_quran.ttf")) }
    val surahNameFont = remember { FontFamily(loadFont(ctx, "surah_name_v4.ttf")) }

    val surahs = remember(pages) { buildSurahList(pages) }
    val allAyahs = remember(pages) { pages.flatMap { p -> p.surahs.flatMap { s -> s.ayahs } } }
    val ayahIndexMap = remember(allAyahs) { allAyahs.mapIndexed { i, a -> a.id to i }.toMap() }
    val savedIds = remember(marks) { marks.map { it.ayahId }.toSet() }
    val isPlaying by PlayerManager.isPlaying
    val ayahProgress by PlayerManager.ayahProgress

    val hizbList = remember { loadHizbList(ctx) }
    val rubList = remember { loadRubList(ctx) }

    var isHorizontalMode by remember { mutableStateOf(false) }
    var fontSize by remember { mutableFloatStateOf(24f) }
    var useMedinaFont by remember { mutableStateOf(true) }
    var unicodeEnabled by remember { mutableStateOf(true) }

    val firstVisibleAyah by remember {
        derivedStateOf {
            if (horiz) {
                pages.getOrNull(pagerState.currentPage)?.surahs?.firstOrNull()?.ayahs?.firstOrNull()
            } else {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) null
                else {
                    val viewportTop = listState.layoutInfo.viewportStartOffset
                    val firstVisible = visibleItems.firstOrNull { it.offset + it.size > viewportTop } ?: visibleItems.first()
                    pages.getOrNull(firstVisible.index)?.surahs?.firstOrNull()?.ayahs?.firstOrNull()
                }
            }
        }
    }

    val lastVisibleAyah by remember {
        derivedStateOf {
            if (horiz) {
                pages.getOrNull(pagerState.currentPage)?.surahs?.lastOrNull()?.ayahs?.lastOrNull()
            } else {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) null
                else {
                    val lastItem = visibleItems.last()
                    pages.getOrNull(lastItem.index)?.surahs?.lastOrNull()?.ayahs?.lastOrNull()
                }
            }
        }
    }

    val positionInfo = remember(firstVisibleAyah, hizbList, rubList) {
        getCurrentPositionInfoFast(firstVisibleAyah ?: lastVisibleAyah, hizbList, rubList)
    }

    // ===== Labels الجزء والحزب =====
    val posInHizb = if (positionInfo.rub > 0) (positionInfo.rub - 1) % 4 else 0
    val hizbLabel = when (posInHizb) {
        0 -> "الحزب ${convertToArabicNumber(positionInfo.hizb)}"
        1 -> "ربع الحزب ${convertToArabicNumber(positionInfo.hizb)}"
        2 -> "نصف الحزب ${convertToArabicNumber(positionInfo.hizb)}"
        3 -> "ثلاثة أرباع الحزب ${convertToArabicNumber(positionInfo.hizb)}"
        else -> "الحزب ${convertToArabicNumber(positionInfo.hizb)}"
    }
    val juzLabel = "الجزء ${convertToArabicNumber(positionInfo.juz)}"

    // ===== أنيميشن شريط المعلومات (overlay فوق المحتوى) =====
    var lastScrollOffset by remember { mutableIntStateOf(0) }
    var showInfoBar by remember { mutableStateOf(false) }

    LaunchedEffect(listState, horiz) {
        if (horiz) return@LaunchedEffect
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val current = (index * 10000) + offset
            val delta = current - lastScrollOffset
            when {
                delta > 4 -> showInfoBar = true
                delta < -4 -> showInfoBar = false
            }
            lastScrollOffset = current
        }
    }

    // شريط يطلع من تحت التولبار (offset سالب = مخفي تحته، 0 = ظاهر)
    val infoBarOffsetY by animateDpAsState(
        targetValue = if (showInfoBar) 0.dp else (-40.dp),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "InfoBarOffset"
    )
    val infoBarAlpha by animateFloatAsState(
        targetValue = if (showInfoBar) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "InfoBarAlpha"
    )

    DisposableEffect(Unit) {
        PlayerManager.init(ctx)
        PlayerManager.setAllAyahs(allAyahs)
        onDispose { PlayerManager.release() }
    }

    var targetPage by remember { mutableStateOf<Int?>(null) }
    var targetAyahId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(targetPage) {
        targetPage?.let {
            if (horiz) pagerState.animateScrollToPage(it) else listState.animateScrollToItem(it)
            targetPage = null
        }
    }
    LaunchedEffect(targetAyahId) {
        targetAyahId?.let { id ->
            val idx = pages.indexOfFirst { p -> p.surahs.any { it.ayahs.any { a -> a.id == id } } }
            if (idx != -1) { if (horiz) pagerState.scrollToPage(idx) else listState.scrollToItem(idx) }
            targetAyahId = null
        }
    }

    val playingAyahForScroll by PlayerManager.currentAyah
    LaunchedEffect(playingAyahForScroll?.id) {
        val ayah = playingAyahForScroll
        if (ayah != null && isPlaying) {
            val pageIdx = pages.indexOfFirst { p -> p.surahs.any { s -> s.ayahs.any { it.id == ayah.id } } }
            if (pageIdx != -1) {
                if (horiz) pagerState.animateScrollToPage(pageIdx)
                else listState.animateScrollToItem(pageIdx)
            }
        }
    }

    if (showBook) {
        BookmarksScreen(
            marks = marks, meQuran = meQuran,
            onClick = { p, id -> targetPage = p; targetAyahId = id; showBook = false },
            onBack = { showBook = false },
            onDelete = { id -> deleteBookmark(ctx, id); marks = loadBookmarks(ctx) }
        )
        return
    }
    if (showAudioLibraryScreen) {
        AudioLibraryScreen(meQuran = meQuran, onBack = { showAudioLibraryScreen = false })
        return
    }

    val currentPageIndex = if (horiz) pagerState.currentPage else listState.firstVisibleItemIndex
    val currentAyahPage = pages.getOrNull(currentPageIndex)?.surahs?.firstOrNull()?.ayahs?.firstOrNull()
    val isCurrentSaved = currentAyahPage?.let { savedIds.contains(it.id) } == true
    val currentSurahNo = firstVisibleAyah?.sura_no ?: currentAyahPage?.sura_no ?: 1

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val ctrl = WindowCompat.getInsetsController(window, view)
            ctrl.hide(WindowInsetsCompat.Type.navigationBars())
            ctrl.show(WindowInsetsCompat.Type.statusBars())
            ctrl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    val OliveGreenBg = Color(0xFF6B8E23).copy(alpha = 0.15f)

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    PremiumProMaxPlusDrawer(
                        isHorizontalMode = isHorizontalMode,
                        onHorizontalModeChange = { isHorizontalMode = it; horiz = it },
                        fontSize = fontSize, onFontSizeChange = { fontSize = it },
                        useMedinaFont = useMedinaFont, onMedinaFontChange = { useMedinaFont = it },
                        unicodeEnabled = unicodeEnabled, onUnicodeEnabledChange = { unicodeEnabled = it },
                        onClose = { scope.launch { drawerState.close() } },
                        onOpenAudioLibrary = {
                            showAudioLibraryScreen = true
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                Scaffold(
                    containerColor = BgScreen,
                    contentWindowInsets = WindowInsets(0),
                    topBar = {
                        // ===== التولبار الثابت فقط (بلا شريط المعلومات) =====
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ToolbarBg)
                                .statusBarsPadding()
                                .height(56.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.9f),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(40.dp).align(Alignment.CenterStart)
                            ) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, tint = GreenMain, modifier = Modifier.size(20.dp), contentDescription = null)
                                }
                            }

                            Text(
                                text = "surah${currentSurahNo.toString().padStart(3, '0')}",
                                fontFamily = surahNameFont,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenMain,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clickable { showQuranIndex = true }
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.9f),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(40.dp).align(Alignment.CenterEnd)
                            ) {
                                IconButton(onClick = { showBook = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_bookmark),
                                        contentDescription = null,
                                        tint = GreenMain,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = !showAudioPanel,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                color = ToolbarBg,
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    IconButton(onClick = { }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_search),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    IconButton(onClick = { }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_khatmah),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(GreenMain)
                                            .clickable {
                                                val ayah = selectedAyah
                                                if (ayah == null) {
                                                    Toast.makeText(ctx, "اضغط على آية أولاً", Toast.LENGTH_SHORT).show()
                                                    return@clickable
                                                }
                                                if (!PlayerManager.isSurahReady(ctx, ayah.sura_no) && !PlayerManager.hasInternet(ctx)) {
                                                    Toast.makeText(ctx, "يجب تحميل السورة أو الاتصال بالإنترنت", Toast.LENGTH_LONG).show()
                                                    return@clickable
                                                }
                                                showAudioPanel = true
                                                PlayerManager.playFromAyah(ctx, ayah)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    IconButton(onClick = { }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_scroll),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        currentAyahPage?.let { ayah ->
                                            if (isCurrentSaved) {
                                                val bookmarkId = marks.firstOrNull { it.ayahId == ayah.id }?.id
                                                bookmarkId?.let { deleteBookmark(ctx, it) }
                                            } else saveBookmark(ctx, ayah)
                                        }
                                        marks = loadBookmarks(ctx)
                                    }) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (isCurrentSaved) R.drawable.ic_close_bookmark else R.drawable.ic_save_bookmark
                                            ),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { padding ->
                    // ===== المحتوى كامل فـ Box واحد =====
                    Box(Modifier.fillMaxSize().padding(padding)) {

                        // ===== نص القرآن (LazyColumn / HorizontalPager) =====
                        if (horiz) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                key = { pages[it].pageNumber },
                                reverseLayout = true
                            ) { index ->
                                LazyColumn {
                                    item {
                                        PageContent(
                                            page = pages[index],
                                            quranFont = quranFont,
                                            medinaFont = medinaFont,
                                            meQuran = meQuran,
                                            selectedAyah = selectedAyah,
     highlightedSurah = null, // زيد هاد السطر
                                            highlightedAyahId = highlightedAyahId,
                                            useMedinaFont = useMedinaFont,
                                            unicodeEnabled = unicodeEnabled,
                                            fontSize = fontSize,
                                            OliveGreenBg = OliveGreenBg,
                                            isAudioActive = isPlaying,
                                            onAyahClick = { ayah ->
                                                if (isPlaying) PlayerManager.stop()
                                                selectedAyah = ayah
                                            },
                                            onAyahLongClick = { ayah ->
                                                tafsirAyahIndex = ayahIndexMap[ayah.id] ?: 0
                                                showTafsirSheet = true
                                            },
                                            onEmptyTap = { if (showAudioPanel) showAudioPanel = false }
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(state = listState) {
                                items(pages, key = { it.pageNumber }) { page ->
                                    PageContent(
                                        page = page,
                                        quranFont = quranFont,
                                        medinaFont = medinaFont,
                                        meQuran = meQuran,
                                        selectedAyah = selectedAyah,
                                        highlightedSurah = null,
                                        highlightedAyahId = highlightedAyahId,
                                        useMedinaFont = useMedinaFont,
                                        unicodeEnabled = unicodeEnabled,
                                        fontSize = fontSize,
                                        OliveGreenBg = OliveGreenBg,
                                        isAudioActive = isPlaying,
                                        onAyahClick = { ayah ->
                                            if (isPlaying) PlayerManager.stop()
                                            selectedAyah = ayah
                                        },
                                        onAyahLongClick = { ayah ->
                                            tafsirAyahIndex = ayahIndexMap[ayah.id] ?: 0
                                            showTafsirSheet = true
                                        },
                                        onEmptyTap = { if (showAudioPanel) showAudioPanel = false }
                                    )
                                }
                            }
                        }

                        // ===== شريط المعلومات (OVERLAY فوق النص، تحت التولبار مباشرة) =====
                        // كيستخدم offset + alpha بدون ما يحجز مساحة
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .offset(y = infoBarOffsetY)
                                .alpha(infoBarAlpha)
                                .background(ToolbarBg.copy(alpha = 0.97f))
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = juzLabel,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenMain
                                    )
                                    Text(
                                        text = hizbLabel,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenMain
                                    )
                                }
                            }
                        }

                        // ===== Audio Panel =====
                        AnimatedVisibility(
                            visible = showAudioPanel,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            AudioPanel(
                                meQuran = meQuran,
                                onClose = {
                                    PlayerManager.stop()
                                    showAudioPanel = false
                                    selectedAyah = null
                                },
                                onOpenReciters = { showRecitersSheet = true }
                            )
                        }

                        if (showRecitersSheet) RecitersBottomSheet(
                            meQuran = meQuran,
                            onDismiss = { showRecitersSheet = false },
                            onOpenLibrary = {
                                showRecitersSheet = false
                                showAudioLibraryScreen = true
                            }
                        )

                        if (showTafsirSheet && allAyahs.isNotEmpty()) AyahDetailsBottomSheet(
                            allAyahs = allAyahs,
                            initialIndex = tafsirAyahIndex,
                            quranFont = quranFont,
                            meQuran = meQuran,
                            savedIds = savedIds,
                            onDismiss = { showTafsirSheet = false },
                            onSave = { saveBookmark(ctx, it); marks = loadBookmarks(ctx) }
                        )
                    }
                }
            }
        }

        if (showQuranIndex) {
            QuranIndexScreen(
                surahs = surahs,
                allAyahs = allAyahs,
                meQuran = meQuran,
                quranFont = quranFont,
                lastSelectedAyahId = lastSelectedAyahId,
                onNavigateToAyah = { ayahId ->
                    lastSelectedAyahId = ayahId
                    targetAyahId = ayahId
                    highlightedAyahId = ayahId
                    showQuranIndex = false
                },
                onBack = { showQuranIndex = false }
            )
        }
    }
}



// ==================== PageContent ====================
@Composable
fun PageContent(
    page: PageWithSurahs, quranFont: FontFamily, medinaFont: FontFamily, meQuran: FontFamily,
    selectedAyah: Ayah?, highlightedSurah: String?, highlightedAyahId: Int? = null,
    useMedinaFont: Boolean = true, unicodeEnabled: Boolean = true, fontSize: Float = 24f,
    OliveGreenBg: Color,
    isAudioActive: Boolean = false,
    onAyahClick: (Ayah) -> Unit, onAyahLongClick: (Ayah) -> Unit,
    onEmptyTap: () -> Unit = {}
) {
    val currentPlayingAyah by PlayerManager.currentAyah
    val currentWordIdx by PlayerManager.currentWordIndex
    val isPlaying by PlayerManager.isPlaying
    val displayFont = if (useMedinaFont) medinaFont else quranFont

    val basmala = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
    val quranTextColor = Color(0xFF1C1C1C)

    Column(Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
        page.surahs.forEach { surah ->
            val firstAyahOfSurah = surah.ayahs.first()
            val isStartOfSurah = firstAyahOfSurah.aya_no == 1

            if (isStartOfSurah) {
                Spacer(Modifier.height(28.dp))
                FramedSurahName(surahNo = firstAyahOfSurah.sura_no, modifier = Modifier.padding(8.dp))
                Spacer(Modifier.height(20.dp))
                if (firstAyahOfSurah.sura_no != 9) {
                    Text(
                        text = basmala, fontFamily = displayFont, fontSize = (fontSize + 2).sp, color = quranTextColor,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }
            } else {
                Spacer(Modifier.height(16.dp))
            }

            val annotatedText = buildAnnotatedString {
                surah.ayahs.forEachIndexed { ayahIdx, ayah ->
                    val isSel = ayah.id == selectedAyah?.id
                    val isHighlighted = ayah.id == highlightedAyahId
                    val isThisAyahActive = isPlaying && currentPlayingAyah?.id == ayah.id

                    pushStringAnnotation("AYAH", ayah.id.toString())

                    val ayaText = ayah.aya_text.trim()
                    val isFirstAyahOfSurah = ayahIdx == 0 && ayah.aya_no == 1
                    val textWithoutBasmala = if (isFirstAyahOfSurah && ayaText.startsWith(basmala)) {
                        ayaText.removePrefix(basmala).trim()
                    } else ayaText

                    if (isThisAyahActive && textWithoutBasmala.isNotEmpty()) {
                        val words = textWithoutBasmala.split(" ")
                        words.forEachIndexed { wIdx, word ->
                            val isCurWord = wIdx == currentWordIdx
                            withStyle(SpanStyle(
                                color = if (isCurWord) GoldDark else quranTextColor,
                                background = OliveGreenBg,
                                fontFamily = displayFont, fontSize = fontSize.sp,
                                fontWeight = if (isCurWord) FontWeight.Bold else FontWeight.Normal
                            )) {
                                append(word)
                                if (wIdx < words.size - 1) append(" ")
                            }
                        }
                        append(" ")
                    } else if (textWithoutBasmala.isNotEmpty()) {
                        withStyle(SpanStyle(
                            color = if (isSel || isHighlighted) GoldDark else quranTextColor,
                            fontFamily = displayFont, fontSize = fontSize.sp,
                            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                        )) {
                            append(textWithoutBasmala)
                            append(" ")
                        }
                    }

                    val openBracket = if (unicodeEnabled) "\uFD3F" else "("
                    val closeBracket = if (unicodeEnabled) "\uFD3E" else ")"
                    withStyle(SpanStyle(
                        color = if (isSel || isThisAyahActive || isHighlighted) GoldDark else GreenMain,
                        fontFamily = meQuran, fontSize = (fontSize - 2).sp, fontWeight = FontWeight.Bold
                    )) {
                        append("$openBracket${convertToArabicNumber(ayah.aya_no)}$closeBracket ")
                    }
                    pop()
                }
            }

            val layoutResult = remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
            Text(
                text = annotatedText,
                style = TextStyle(textAlign = TextAlign.Center, textDirection = TextDirection.Rtl, lineHeight = (fontSize * 1.8).sp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp).pointerInput(isAudioActive) {
                    detectTapGestures(
                        onTap = { pos ->
                            val layout = layoutResult.value
                            if (layout == null) { onEmptyTap(); return@detectTapGestures }
                            val offset = layout.getOffsetForPosition(pos)
                            val ann = annotatedText.getStringAnnotations("AYAH", offset, offset).firstOrNull()
                            if (ann == null) {
                                // كليكة فمساحة خاوية
                                onEmptyTap()
                            } else if (isAudioActive) {
                                // الصوت شغال: التاب محرم على الآيات، كيتعامل كـ toggle UI
                                onEmptyTap()
                            } else {
                                surah.ayahs.find { it.id == ann.item.toInt() }?.let(onAyahClick)
                            }
                        },
                        
                        onLongPress = { pos ->
    layoutResult.value?.let { layout ->
        val offset = layout.getOffsetForPosition(pos)
        annotatedText.getStringAnnotations("AYAH", offset, offset).firstOrNull()?.let { ann ->
            surah.ayahs.find { it.id == ann.item.toInt() }?.let(onAyahLongClick)
        }
    }
}
                        
                    )
                },
                onTextLayout = { layoutResult.value = it }
            )
            Spacer(Modifier.height(24.dp))
        }
        Text(
            text = convertToArabicNumber(page.pageNumber), fontSize = 22.sp, color = GreenMain,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
        Spacer(Modifier.height(24.dp))
    }
}







// ==================== تبويب السور ====================
@Composable
fun SurahsTab(surahs: List<SurahDetailItem>, listState: LazyListState, lastSelectedId: Int?, onClick: (Int) -> Unit) {
    LazyColumn(state = listState, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(surahs, key = { it.surahNo }) { s ->
            SurahCard(s = s, isSelected = s.firstAyahId == lastSelectedId, onClick = { onClick(s.firstAyahId) })
        }
    }
}

@Composable
fun SurahCard(s: SurahDetailItem, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(if (isSelected) Color(0xFFFFF8DC) else Color.White),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                Icon(BrightnessEmpty, contentDescription = null,
                    tint = if (isSelected) GoldDark else Color(0xFFD4AF37),
                    modifier = Modifier.fillMaxSize())
                Text(convertToArabicNumber(s.surahNo), fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    color = if (isSelected) GreenMain else TextMain, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(s.name, fontFamily = FontFamily.Default, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = if (isSelected) GreenMain else TextMain)
                Text(s.details, fontFamily = FontFamily.Default, fontSize = 12.sp, color = Color(0xFF888888))
            }
            if (s.page > 0) {
                Box(Modifier.size(36.dp).background(
                    if (isSelected) GoldDark.copy(alpha = 0.15f) else Color(0xFFF0EDE4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(convertToArabicNumber(s.page), fontFamily = FontFamily.Default, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) GoldDark else GreenMain, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ==================== تبويب الأجزاء الجديد ====================
@Composable
fun JuzTabNew(juzList: List<JuzWithAyah>, listState: LazyListState, lastSelectedId: Int?, meQuran: FontFamily, quranFont: FontFamily, onClick: (Int) -> Unit) {
    LazyColumn(state = listState, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(juzList, key = { it.juz.number }) { item ->
            JuzCardNew(item, meQuran, quranFont, isSelected = item.ayah?.id == lastSelectedId) {
                item.ayah?.id?.let(onClick)
            }
        }
    }
}

@Composable
fun JuzCardNew(item: JuzWithAyah, meQuran: FontFamily, quranFont: FontFamily, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(if (isSelected) Color(0xFFFFF8DC) else Color.White),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(52.dp)) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(BrightnessEmpty, contentDescription = null,
                        tint = if (isSelected) GoldDark else Color(0xFFD4AF37),
                        modifier = Modifier.fillMaxSize())
                    Text(convertToArabicNumber(item.juz.number), fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        color = if (isSelected) GreenMain else TextMain, textAlign = TextAlign.Center)
                }
                Text("الجزء", fontFamily = FontFamily.Default, fontSize = 11.sp, color = Color(0xFF888888))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                if (!item.ayah?.aya_text.isNullOrBlank()) {
                    Text("﴿ ${item.ayah!!.aya_text} ﴾", fontFamily = meQuran, fontSize = 17.sp, color = TextMain,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = TextStyle(textDirection = TextDirection.Rtl))
                }
                item.ayah?.sura_name_ar?.let {
                    Text("$it - آية ${convertToArabicNumber(item.ayah!!.aya_no)}", fontFamily = FontFamily.Default, fontSize = 12.sp, color = Color(0xFF888888))
                }
            }
            Spacer(Modifier.width(8.dp))
            if ((item.ayah?.page?: 0) > 0) {
                Box(Modifier.size(36.dp).background(
                    if (isSelected) GoldDark.copy(alpha = 0.15f) else Color(0xFFF0EDE4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(convertToArabicNumber(item.ayah!!.page), fontFamily = FontFamily.Default,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = if (isSelected) GoldDark else GreenMain, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ==================== تبويب الأحزاب الجديد ====================
@Composable
fun HizbTabNew(hizbList: List<HizbWithRubs>, listState: LazyListState, meQuran: FontFamily, quranFont: FontFamily, lastSelectedId: Int?, onClick: (Int) -> Unit) {
    LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 16.dp)) {
        hizbList.forEach { hizb ->
            items(hizb.rubs, key = { "rub_${it.rubNumber}" }) { rub ->
                RubCardNew(rub, meQuran, quranFont, isSelected = rub.firstAyahId == lastSelectedId) {
                    if (rub.firstAyahId!= 0) onClick(rub.firstAyahId)
                }
            }
        }
    }
}

@Composable
fun RubCardNew(rub: RubCardItem, meQuran: FontFamily, quranFont: FontFamily, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(if (isSelected) Color(0xFFFFF8DC) else Color.White),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        shape = RoundedCornerShape(10.dp),
        border = if (rub.rubLabel.startsWith("الحزب")) BorderStroke(1.dp, if (isSelected) GoldDark else GreenMain.copy(alpha = 0.3f)) else null
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(110.dp)) {
                Text(rub.rubLabel, fontFamily = FontFamily.Default, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (isSelected) GoldDark else if (rub.rubLabel.startsWith("الحزب")) GreenMain else GoldDark.copy(alpha = 0.8f))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                if (rub.firstAyahText.isNotBlank()) {
                    Text("﴿ ${rub.firstAyahText} ﴾", fontFamily = meQuran, fontSize = 16.sp, color = TextMain,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = TextStyle(textDirection = TextDirection.Rtl))
                }
                if (rub.surahName.isNotBlank()) {
                    Text(rub.surahName, fontFamily = FontFamily.Default, fontSize = 12.sp, color = Color(0xFF888888),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
            Spacer(Modifier.width(8.dp))
            if (rub.page > 0) {
                Box(Modifier.size(34.dp).background(
                    if (isSelected) GoldDark.copy(alpha = 0.15f) else Color(0xFFF0EDE4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(convertToArabicNumber(rub.page), fontFamily = FontFamily.Default,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (isSelected) GoldDark else GreenMain, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ==================== تبويب الصفحات ====================
@Composable
fun PagesTab(pages: List<PageItem>, listState: LazyListState, meQuran: FontFamily, quranFont: FontFamily, lastSelectedId: Int?, onClick: (Int) -> Unit) {
    LazyColumn(state = listState, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(pages, key = { it.pageNumber }) { p ->
            PageCard(p, meQuran, quranFont, isSelected = p.firstAyahId == lastSelectedId) { onClick(p.firstAyahId) }
        }
    }
}

@Composable
fun PageCard(p: PageItem, meQuran: FontFamily, quranFont: FontFamily, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(if (isSelected) Color(0xFFFFF8DC) else Color.White),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                Icon(BrightnessEmpty, contentDescription = null,
                    tint = if (isSelected) GoldDark else Color(0xFFD4AF37),
                    modifier = Modifier.fillMaxSize())
                Text(convertToArabicNumber(p.pageNumber), fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    color = if (isSelected) GreenMain else TextMain, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(" ${p.firstAyahText} ", fontFamily = meQuran, fontSize = 17.sp, color = TextMain,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = TextStyle(textDirection = TextDirection.Rtl))
                Text("${p.surahName} • الجزء ${convertToArabicNumber(p.jozzNumber)}",
                    fontFamily = FontFamily.Default, fontSize = 12.sp, color = Color(0xFF888888),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

// ==================== تبويب السجدات الجديد ====================
@Composable
fun SajdahTabNew(sajdahs: List<SajdahWithAyah>, listState: LazyListState, meQuran: FontFamily, quranFont: FontFamily, lastSelectedId: Int?, onClick: (Int) -> Unit) {
    LazyColumn(state = listState, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sajdahs, key = { it.sajdah.number }) { item ->
            SajdahCardNew(item, meQuran, quranFont, isSelected = item.ayah?.id == lastSelectedId) {
                item.ayah?.id?.let(onClick)
            }
        }
    }
}

@Composable
fun SajdahCardNew(item: SajdahWithAyah, meQuran: FontFamily, quranFont: FontFamily, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(if (isSelected) Color(0xFFFFF8DC) else Color.White),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                Icon(BrightnessEmpty, contentDescription = null,
                    tint = if (isSelected) GoldDark else Color(0xFFD4AF37),
                    modifier = Modifier.fillMaxSize())
                Text(convertToArabicNumber(item.sajdah.number), fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    color = if (isSelected) GreenMain else TextMain, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                if (!item.ayah?.aya_text.isNullOrBlank()) {
                    Text("﴿ ${item.ayah!!.aya_text} ﴾", fontFamily = meQuran, fontSize = 17.sp, color = TextMain,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = TextStyle(textDirection = TextDirection.Rtl))
                }
                val surahAndJuz = buildString {
                    append(item.ayah?.sura_name_ar?: "")
                    if ((item.ayah?.jozz?: 0) > 0) append(" • الجزء ${convertToArabicNumber(item.ayah!!.jozz)}")
                }
                if (surahAndJuz.isNotBlank()) {
                    Text(surahAndJuz, fontFamily = FontFamily.Default, fontSize = 12.sp, color = Color(0xFF888888),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
    }
}

// ==================== QuranIndexScreen ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranIndexScreen(
    surahs: List<SurahInfo>,
    allAyahs: List<Ayah>,
    meQuran: FontFamily,
    quranFont: FontFamily,
    lastSelectedAyahId: Int?,
    onNavigateToAyah: (ayahId: Int) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("السور", "الأجزاء", "الأحزاب", "الصفحات", "السجدات")

    val surahListState = rememberLazyListState()
    val juzListState = rememberLazyListState()
    val hizbListState = rememberLazyListState()
    val pageListState = rememberLazyListState()
    val sajdahListState = rememberLazyListState()

    // السور: تُبنى من allAyahs + surah_details فورياً
    val surahDetails = remember(allAyahs) { loadSurahDetails(ctx, allAyahs) }

    // الموديلات الجديدة فيها النص مباشرة
    var juzList by remember { mutableStateOf<List<JuzWithAyah>>(emptyList()) }
    var hizbWithRubs by remember { mutableStateOf<List<HizbWithRubs>>(emptyList()) }
    var pageItems by remember { mutableStateOf<List<PageItem>>(emptyList()) }
    var sajdahList by remember { mutableStateOf<List<SajdahWithAyah>>(emptyList()) }

    LaunchedEffect(Unit) {
    withContext(Dispatchers.IO) {
        val juz = loadJuzWithAyahs(ctx, allAyahs) // زدت allAyahs
        val hizb = loadHizbWithRubsAndAyahs(ctx, allAyahs) // زدت allAyahs
        val pages = loadPageItems(ctx)
        val sajdah = loadSajdahWithAyahs(ctx, allAyahs) // زدت allAyahs
        withContext(Dispatchers.Main) {
            juzList = juz
            hizbWithRubs = hizb
            pageItems = pages
            sajdahList = sajdah
        }
    }
}
    
    
    
    

    var query by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    LaunchedEffect(query) { delay(200); debouncedQuery = query.trim() }
    val strippedQuery = remember(debouncedQuery) { stripTashkeel(debouncedQuery) }

    val filteredSurahs = remember(strippedQuery, surahDetails) {
        if (strippedQuery.isEmpty()) surahDetails
        else surahDetails.filter { s ->
            stripTashkeel(s.name).contains(strippedQuery, ignoreCase = true)
                || s.surahNo.toString() == strippedQuery
                || stripTashkeel(s.details).contains(strippedQuery, ignoreCase = true)
        }
    }
    val filteredJuz = remember(strippedQuery, juzList) {
        if (strippedQuery.isEmpty()) juzList
        else juzList.filter { it.juz.number.toString().contains(strippedQuery) }
    }
    val filteredHizb = remember(strippedQuery, hizbWithRubs) {
        if (strippedQuery.isEmpty()) hizbWithRubs
        else hizbWithRubs.filter { it.hizbNumber.toString().contains(strippedQuery) }
    }
    val filteredPages = remember(strippedQuery, pageItems) {
        if (strippedQuery.isEmpty()) pageItems
        else pageItems.filter { it.pageNumber.toString().contains(strippedQuery) }
    }
    val filteredSajdah = remember(strippedQuery, sajdahList) {
        if (strippedQuery.isEmpty()) sajdahList
        else sajdahList.filter { it.sajdah.number.toString().contains(strippedQuery) }
    }

    val isEmpty = when (selectedTab) {
        0 -> filteredSurahs.isEmpty(); 1 -> filteredJuz.isEmpty()
        2 -> filteredHizb.isEmpty(); 3 -> filteredPages.isEmpty()
        4 -> filteredSajdah.isEmpty(); else -> false
    } && debouncedQuery.isNotEmpty()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = Color(0xFFFFFDF8), contentWindowInsets = WindowInsets(0),
            topBar = {
                Surface(color = ToolbarBg, shadowElevation = 2.dp) {
                    Column {
                        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tint = GreenMain, contentDescription = null) }
                            OutlinedTextField(
                                value = query, onValueChange = { query = it },
                                modifier = Modifier.weight(1f).height(50.dp),
                                placeholder = { Text("ابحث...", fontFamily = FontFamily.Default, fontSize = 14.sp, color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GreenMain, modifier = Modifier.size(20.dp)) },
                                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) } },
                                singleLine = true, shape = CircleShape,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain, unfocusedBorderColor = DividerCol, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                                textStyle = TextStyle(fontFamily = FontFamily.Default, fontSize = 14.sp, textAlign = TextAlign.Right, textDirection = TextDirection.Rtl)
                            )
                        }
                        ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent, contentColor = GreenMain, edgePadding = 12.dp, divider = {},
                            indicator = { tabPositions -> Box(Modifier.tabIndicatorOffset(tabPositions[selectedTab]).height(3.dp).background(GoldDark, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))) }
                        ) {
                            tabs.forEachIndexed { i, title ->
                                Tab(selected = selectedTab == i, onClick = { selectedTab = i; query = "" },
                                    text = { Text(title, fontFamily = FontFamily.Default, fontSize = 15.sp, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == i) Color.Black else TabUnsel) })
                            }
                        }
                        HorizontalDivider(color = DividerCol)
                    }
                }
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                if (isEmpty) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.SearchOff, tint = Color.Gray, modifier = Modifier.size(48.dp), contentDescription = null)
                            Spacer(Modifier.height(8.dp))
                            Text("لا توجد نتائج", fontFamily = meQuran, fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                } else {
                    when (selectedTab) {
                        0 -> SurahsTab(filteredSurahs, surahListState, lastSelectedAyahId, onNavigateToAyah)
                        1 -> JuzTabNew(filteredJuz, juzListState, lastSelectedAyahId, meQuran, quranFont, onNavigateToAyah)
                        2 -> HizbTabNew(filteredHizb, hizbListState, meQuran, quranFont, lastSelectedAyahId, onNavigateToAyah)
                        3 -> PagesTab(filteredPages, pageListState, meQuran, quranFont, lastSelectedAyahId, onNavigateToAyah)
                        4 -> SajdahTabNew(filteredSajdah, sajdahListState, meQuran, quranFont, lastSelectedAyahId, onNavigateToAyah)
                    }
                }
            }
        }
    }
}

// ==================== AyahDetailsBottomSheet ====================
@Composable
fun AyahDetailsBottomSheet(allAyahs: List<Ayah>, initialIndex: Int, quranFont: FontFamily, meQuran: FontFamily, savedIds: Set<Int>, onDismiss: () -> Unit, onSave: (Ayah) -> Unit) {
    val ctx = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false, confirmValueChange = { it != SheetValue.Hidden })
    val safeInitial = initialIndex.coerceIn(0, allAyahs.size - 1)
    val pagerState = rememberPagerState(initialPage = safeInitial, pageCount = { allAyahs.size })
    val currentAyah = allAyahs[pagerState.currentPage]
    val isSaved = currentAyah.id in savedIds
    var showTafsirMgr by remember { mutableStateOf(false) }
    var ayahExpanded by remember { mutableStateOf(false) }
    var starAnim by remember { mutableStateOf(false) }
    val selectedTab = remember { mutableStateOf(0) }
    val saveScale by animateFloatAsState(targetValue = if (starAnim) 1.4f else 1f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), finishedListener = { starAnim = false })

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, dragHandle = { Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) { Box(Modifier.width(36.dp).height(4.dp).background(DividerCol, RoundedCornerShape(2.dp))) } }, containerColor = BgScreen, windowInsets = WindowInsets(0), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(color = ToolbarBg) {
                    Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, tint = GreenMain, modifier = Modifier.size(24.dp), contentDescription = null) }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = {}) { Icon(Icons.Filled.Share, tint = GreenMain, modifier = Modifier.size(22.dp), contentDescription = null) }
                        IconButton(onClick = { if (!isSaved) { starAnim = true; onSave(currentAyah) } }) {
                            Icon(imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, tint = if (isSaved) GoldDark else GreenMain, modifier = Modifier.size(28.dp).scale(saveScale), contentDescription = null)
                        }
                    }
                }
            }
            HorizontalDivider(color = DividerCol)
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth(), reverseLayout = true) { index ->
                val ayah = allAyahs[index]
                Column(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Surface(color = Color(0xFFF1EFE7), shape = RoundedCornerShape(12.dp), shadowElevation = 2.dp) {
                            Text("${ayah.sura_name_ar} ${convertToArabicNumber(ayah.aya_no)}", fontFamily = meQuran, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenMain, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                        }
                    }
                    Box(Modifier.fillMaxWidth().clickable { ayahExpanded = !ayahExpanded }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(ayah.aya_text, fontFamily = quranFont, fontSize = 24.sp, color = TextMain, textAlign = TextAlign.Center, maxLines = if (ayahExpanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                    }
                    HorizontalDivider(color = DividerCol)
                    AyahPageContentWithTabs(ayah, quranFont, meQuran, selectedTab) { showTafsirMgr = true }
                }
            }
        }
        if (showTafsirMgr) TafsirManagerFullScreen(onDismiss = { showTafsirMgr = false }, meQuran = meQuran)
    }
}

// ==================== AyahPageContentWithTabs ====================
@Composable
fun AyahPageContentWithTabs(ayah: Ayah, quranFont: FontFamily, meQuran: FontFamily, selectedTabIndex: MutableState<Int>? = null, onTafsirManagerClick: () -> Unit) {
    val ctx = LocalContext.current
    val downloadedTafsirs = remember { getDownloadedTafsirs(ctx) }
    val allTafsirs = remember(downloadedTafsirs) { listOf("moyssar" to "التفسير الميسر") + downloadedTafsirs }
    val effectiveIndex = selectedTabIndex ?: remember { mutableStateOf(0) }
    val tafsirHtml = remember(ayah.id, effectiveIndex.value) {
        val id = allTafsirs.getOrNull(effectiveIndex.value)?.first ?: return@remember null
        if (id == "moyssar") getTafsirMoyassar(ctx, ayah.sura_no, ayah.aya_no) else getTafsirFromDatabase(ctx, "$id.db", ayah.sura_no, ayah.aya_no)
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).padding(start = 4.dp), contentAlignment = Alignment.Center) { IconButton(onClick = onTafsirManagerClick) { Icon(Icons.Filled.Tune, tint = GreenMain, contentDescription = null) } }
            ScrollableTabRow(effectiveIndex.value, Modifier.weight(1f).fillMaxHeight(), containerColor = Color.Transparent, contentColor = GreenMain, edgePadding = 0.dp, divider = {}, indicator = {}) {
                allTafsirs.forEachIndexed { i, (_, name) ->
                    val sel = effectiveIndex.value == i
                    Tab(selected = sel, onClick = { effectiveIndex.value = i }, text = { Text(name, fontFamily = meQuran, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) GreenMain else TabUnsel) }, modifier = Modifier.background(if (sel) GreenMain.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 4.dp))
                }
            }
        }
        if (!tafsirHtml.isNullOrBlank()) {
            LazyColumn(Modifier.fillMaxWidth().heightIn(min = 200.dp), contentPadding = PaddingValues(16.dp)) {
                item {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        AndroidView(factory = { c -> android.webkit.WebView(c).apply { settings.defaultFontSize = 18; setBackgroundColor(android.graphics.Color.TRANSPARENT) } }, update = { wv -> wv.loadDataWithBaseURL(null, "<html><body style='direction:rtl;text-align:justify;font-size:18px;color:#2C3E2D;line-height:1.8;'>$tafsirHtml</body></html>", "text/html", "UTF-8", null) })
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.HideSource, tint = Color.LightGray, modifier = Modifier.size(36.dp), contentDescription = null)
                    Spacer(Modifier.height(8.dp))
                    Text("لا يوجد تفسير لهذه الآية", fontFamily = meQuran, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

// ==================== TafsirManagerFullScreen ====================
@Composable
fun TafsirManagerFullScreen(onDismiss: () -> Unit, meQuran: FontFamily) {
    val ctx = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val downloadStates = remember { mutableStateMapOf<String, TafsirDownloadState>() }
    var downloadedList by remember { mutableStateOf(getDownloadedTafsirs(ctx)) }
    fun refresh() { downloadedList = getDownloadedTafsirs(ctx) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = false)) {
        Surface(Modifier.fillMaxSize(), color = BgScreen) {
            Scaffold(containerColor = BgScreen, contentWindowInsets = WindowInsets(0),
                topBar = {
                    Surface(color = ToolbarBg) {
                        Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tint = GreenMain, contentDescription = null) }
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Filled.LibraryBooks, tint = GreenMain, modifier = Modifier.size(22.dp), contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("إدارة التفاسير", fontFamily = meQuran, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenMain)
                        }
                    }
                }
            ) { pad ->
                Column(Modifier.fillMaxSize().padding(pad)) {
                    TabRow(selectedTab, containerColor = TabBg, contentColor = GreenMain) {
                        Tab(selectedTab == 0, { selectedTab = 0 }) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) { Icon(Icons.Filled.CloudDownload, tint = if (selectedTab == 0) GreenMain else TabUnsel, modifier = Modifier.size(18.dp), contentDescription = null); Spacer(Modifier.width(6.dp)); Text("التفاسير المتاحة", fontFamily = meQuran, fontSize = 14.sp, color = if (selectedTab == 0) GreenMain else TabUnsel) } }
                        Tab(selectedTab == 1, { selectedTab = 1 }) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) { Icon(Icons.Filled.OfflinePin, tint = if (selectedTab == 1) GreenMain else TabUnsel, modifier = Modifier.size(18.dp), contentDescription = null); Spacer(Modifier.width(6.dp)); Text("التفاسير المحملة", fontFamily = meQuran, fontSize = 14.sp, color = if (selectedTab == 1) GreenMain else TabUnsel) } }
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        when (selectedTab) {
                            0 -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                                items(tafsirSources) { src ->
                                    val state = downloadStates[src.id] ?: TafsirDownloadState()
                                    val isDl = downloadedList.any { it.first == src.id }
                                    val dispSize = remember(src.id, isDl) { if (isDl) { val f = ctx.getDatabasePath("${src.id}.db"); if (f.exists()) formatBytes(f.length()) else formatBytes(src.sizeBytes) } else formatBytes(src.sizeBytes) }
                                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(Modifier.weight(1f)) {
                                                Text(src.name, fontFamily = meQuran, fontSize = 16.sp, color = TextMain, fontWeight = FontWeight.Bold)
                                                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.DataUsage, tint = Color.Gray, modifier = Modifier.size(14.dp), contentDescription = null); Spacer(Modifier.width(4.dp)); Text(dispSize, fontFamily = meQuran, fontSize = 12.sp, color = Color.Gray) }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                                if (isDl) {
                                                    var showDel by remember { mutableStateOf(false) }
                                                    IconButton({ showDel = true }) { Icon(Icons.Filled.DeleteForever, tint = Color.Red.copy(.7f), contentDescription = null) }
                                                    if (showDel) AlertDialog({ showDel = false }, title = { Text("تأكيد الحذف", fontFamily = meQuran) }, text = { Text("هل تريد حذف ${src.name}؟", fontFamily = meQuran) }, confirmButton = { TextButton({ deleteTafsir(ctx, src.id); refresh(); downloadStates.remove(src.id); showDel = false }) { Text("حذف", fontFamily = meQuran, color = Color.Red) } }, dismissButton = { TextButton({ showDel = false }) { Text("إلغاء", fontFamily = meQuran) } })
                                                } else if (state.isDownloading) {
                                                    CircularProgressIndicator({ state.progress }, Modifier.size(36.dp), color = GreenMain, trackColor = DividerCol, strokeWidth = 3.dp)
                                                } else {
                                                    IconButton({ downloadStates[src.id] = TafsirDownloadState(isDownloading = true); downloadTafsir(ctx, src, { p -> downloadStates[src.id] = TafsirDownloadState(true, p) }, { ok -> if (ok) { downloadStates[src.id] = TafsirDownloadState(isDownloaded = true); refresh() } else downloadStates[src.id] = TafsirDownloadState() }) }) { Icon(Icons.Filled.DownloadForOffline, tint = GreenMain, contentDescription = null) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> if (downloadedList.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.CloudOff, tint = Color.LightGray, modifier = Modifier.size(48.dp), contentDescription = null); Spacer(Modifier.height(8.dp)); Text("لا توجد تفاسير محملة", fontFamily = meQuran, fontSize = 14.sp, color = Color.Gray) } }
                            } else {
                                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                                    items(downloadedList, key = { it.first }) { (id, name) ->
                                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.CheckCircle, tint = GreenMain, modifier = Modifier.size(20.dp), contentDescription = null); Spacer(Modifier.width(8.dp)); Text(name, fontFamily = meQuran, fontSize = 16.sp, color = TextMain, fontWeight = FontWeight.Bold) }
                                                var showDel by remember { mutableStateOf(false) }
                                                IconButton({ showDel = true }) { Icon(Icons.Filled.DeleteForever, tint = Color.Red.copy(.7f), contentDescription = null) }
                                                if (showDel) AlertDialog({ showDel = false }, title = { Text("تأكيد الحذف", fontFamily = meQuran) }, text = { Text("هل تريد حذف $name؟", fontFamily = meQuran) }, confirmButton = { TextButton({ deleteTafsir(ctx, id); refresh(); showDel = false }) { Text("حذف", fontFamily = meQuran, color = Color.Red) } }, dismissButton = { TextButton({ showDel = false }) { Text("إلغاء", fontFamily = meQuran) } })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== BookmarksScreen ====================
@Composable
fun BookmarksScreen(marks: List<Bookmark>, meQuran: FontFamily, onClick: (Int, Int) -> Unit, onBack: () -> Unit, onDelete: (Int) -> Unit) {
    Scaffold(containerColor = BgScreen, contentWindowInsets = WindowInsets(0),
        topBar = {
            Surface(color = ToolbarBg) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tint = GreenMain, contentDescription = null) }
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.CollectionsBookmark, tint = GreenMain, modifier = Modifier.size(22.dp), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("العلامات المحفوظة", fontFamily = meQuran, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenMain)
                    Spacer(Modifier.weight(1f))
                    if (marks.isNotEmpty()) { Surface(color = GreenMain, shape = CircleShape) { Text(convertToArabicNumber(marks.size), fontFamily = meQuran, fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) }; Spacer(Modifier.width(8.dp)) }
                }
            }
        }
    ) { pad ->
        
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (marks.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.BookmarkBorder, tint = Color.LightGray, modifier = Modifier.size(64.dp), contentDescription = null)
                        Spacer(Modifier.height(12.dp))
                        Text("لا توجد علامات محفوظة", fontFamily = meQuran, fontSize = 16.sp, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        Text("اضغط على أيقونة العلامة لحفظ موضعك", fontFamily = meQuran, fontSize = 13.sp, color = Color.LightGray)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(marks, key = { it.id }) { m ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onClick(m.pageNumber - 1, m.ayahId) }.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // أيقونة العلامة
                            Icon(Icons.Filled.Bookmark, tint = GoldDark, modifier = Modifier.size(28.dp), contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(m.surahName, fontFamily = meQuran, fontSize = 16.sp, color = GreenMain, fontWeight = FontWeight.Bold)
                                Text("الآية ${convertToArabicNumber(m.ayahNumber)}  •  الصفحة ${convertToArabicNumber(m.pageNumber)}", fontFamily = meQuran, fontSize = 13.sp, color = TextMain)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, tint = Color.LightGray, modifier = Modifier.size(12.dp), contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("${m.date}  ${m.time}", fontFamily = meQuran, fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            // زر حذف
                            IconButton({ onDelete(m.id) }) {
                                Icon(Icons.Filled.DeleteOutline, tint = Color.Red.copy(.6f), contentDescription = null)
                            }
                        }
                        HorizontalDivider(color = DividerCol, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}
