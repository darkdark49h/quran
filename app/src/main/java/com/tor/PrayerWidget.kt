package com.tor

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ==========================================================
// ثيم الويدجت
// ==========================================================
private object WidgetTheme {
    val ROOT_BACKGROUND = Color(0xD9121212)
    val ACTIVE_BACKGROUND = Color(0xFF222222)
    val ACCENT = Color(0xFFD4AF37)
    val PRIMARY = Color(0xFFFFFFFF)
    val SECONDARY = Color(0xFF9E9E9E)
}

private val WIDGET_PRAYER_KEYS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

// ==========================================================
// Receiver
// ==========================================================
class PrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerWidget()
}

// ==========================================================
// المنطق الرئيسي للويدجت
// ==========================================================
class PrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cityId = getSavedCityId(context)
        val times = withContext(Dispatchers.IO) {
            cityId?.let { PrayerDbManager.getTodayPrayerTimes(context, it) }
        }

        if (times == null) {
            provideContent { EmptyWidgetContent() }
            return
        }

        val allItems = buildPrayerList(times)
        val fiveItems = allItems.filter { it.key in WIDGET_PRAYER_KEYS }
            .sortedBy { WIDGET_PRAYER_KEYS.indexOf(it.key) }

        // إنشاء قائمة من الأزواج (PrayerItem, Calendar) لتجنب مشكلة calendar المفقودة
        val itemsWithCal = fiveItems.mapNotNull { item ->
            parseTimeToday(item.time)?.let { cal -> item to cal }
        }

        val now = Calendar.getInstance()
        val (nextItem, nextCal) = findNextPrayer(fiveItems)

        val lastItemWithCal = itemsWithCal.reversed().find { (_, cal) ->
            cal.timeInMillis <= now.timeInMillis
        }

        val headerText: String
        if (lastItemWithCal != null && isWithinTenMinutes(lastItemWithCal.second, now)) {
            val diffMinutes = getMinutesDifference(lastItemWithCal.second, now)
            headerText = "صلاة ${lastItemWithCal.first.label} منذ ${diffMinutes}د"
        } else {
            val diffMillis = nextCal.timeInMillis - now.timeInMillis
            val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
            headerText = String.format("- %02d:%02d", hours, minutes)
        }

        val activeIndex = fiveItems.indexOfFirst { it.key == nextItem.key }
        val hijriText = "${times.hijriDay} ${times.hijriMonthName} 1448"

        provideContent {
            PrayerWidgetContent(
                hijriText = hijriText,
                countdownText = headerText,
                items = fiveItems,
                activeIndex = activeIndex
            )
        }
    }

    private fun isWithinTenMinutes(prayerCal: Calendar, now: Calendar): Boolean {
        val diff = now.timeInMillis - prayerCal.timeInMillis
        return diff in 0..(10 * 60 * 1000)
    }

    private fun getMinutesDifference(prayerCal: Calendar, now: Calendar): Long {
        val diff = now.timeInMillis - prayerCal.timeInMillis
        return TimeUnit.MILLISECONDS.toMinutes(diff)
    }
}

// ==========================================================
// واجهة المستخدم (UI)
// ==========================================================
@Composable
private fun PrayerWidgetContent(
    hijriText: String,
    countdownText: String,
    items: List<PrayerItem>,
    activeIndex: Int
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .height(72.dp)
            .background(WidgetTheme.ROOT_BACKGROUND)
            .cornerRadius(16.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // الطبقة 1: الهيدر الموحد
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hijriText,
                style = TextStyle(color = ColorProvider(WidgetTheme.SECONDARY), fontSize = 10.sp)
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = countdownText,
                style = TextStyle(color = ColorProvider(WidgetTheme.ACCENT), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
        }

        // الطبقة 2: شبكة الصلوات الخمس
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                PrayerSlot(
                    item = item,
                    isActive = index == activeIndex,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }
}

@Composable
private fun PrayerSlot(item: PrayerItem, isActive: Boolean, modifier: GlanceModifier) {
    val nameColor = if (isActive) WidgetTheme.PRIMARY else WidgetTheme.SECONDARY
    val timeColor = if (isActive) WidgetTheme.ACCENT else WidgetTheme.SECONDARY
    val timeWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

    val slotContent: @Composable () -> Unit = {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = item.label,
                style = TextStyle(color = ColorProvider(nameColor), fontSize = 10.sp, textAlign = TextAlign.Center)
            )
            Text(
                text = item.time,
                style = TextStyle(color = ColorProvider(timeColor), fontSize = 11.sp, fontWeight = timeWeight, textAlign = TextAlign.Center)
            )
        }
    }

    if (isActive) {
        Box(
            modifier = modifier
                .background(WidgetTheme.ACTIVE_BACKGROUND)
                .cornerRadius(8.dp)
                .padding(vertical = 2.dp, horizontal = 1.dp),
            contentAlignment = Alignment.Center
        ) { slotContent() }
    } else {
        Box(
            modifier = modifier.padding(vertical = 2.dp, horizontal = 1.dp),
            contentAlignment = Alignment.Center
        ) { slotContent() }
    }
}

@Composable
private fun EmptyWidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .height(72.dp)
            .background(WidgetTheme.ROOT_BACKGROUND)
            .cornerRadius(16.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "افتح تطبيق الفرقان لعرض مواقيت الصلاة",
            style = TextStyle(color = ColorProvider(WidgetTheme.SECONDARY), fontSize = 12.sp, textAlign = TextAlign.Center)
        )
    }
}



