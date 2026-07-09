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

private object WidgetTheme {
    val ROOT_BACKGROUND = Color(0xD9121212) // 85% شفافية فخمة
    val ACTIVE_BACKGROUND = Color(0xFF222222)
    val ACCENT = Color(0xFFD4AF37)
    val PRIMARY = Color(0xFFFFFFFF)
    val SECONDARY = Color(0xFF9E9E9E)
}

private val WIDGET_PRAYER_KEYS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

class PrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerWidget()
}

class PrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cityId = getSavedCityId(context) ?: return provideContent { EmptyWidgetContent() }
        val times = PrayerDbManager.getTodayPrayerTimes(context, cityId) ?: return provideContent { EmptyWidgetContent() }

        val allItems = buildPrayerList(times)
        val fiveItems = allItems.filter { it.key in WIDGET_PRAYER_KEYS }
            .sortedBy { WIDGET_PRAYER_KEYS.indexOf(it.key) }

        // --- محرك الحسابات الذكي (الـ 10 دقائق ومابعدها) ---
        val now = Calendar.getInstance()
        val (nextItem, nextCal) = findNextPrayer(fiveItems)
        val lastItem = findLastPrayer(fiveItems, now)

        val headerText: String
        if (lastItem != null && isWithinTenMinutes(lastItem.calendar, now)) {
            // ميزة: صلاة كذا منذ X دقائق
            val diffMinutes = getMinutesDifference(lastItem.calendar, now)
            headerText = "صلاة ${lastItem.label} منذ ${diffMinutes}د"
        } else {
            // الحساب التنازلي العادي للصلاة القادمة (ساعات ودقائق فقط للحفاظ على الباتري)
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

@Composable
private fun PrayerWidgetContent(
    hijriText: String,
    countdownText: String,
    items: List<PrayerItem>,
    activeIndex: Int
) {
    // تقليص الـ Padding العمودي لـ 6.dp باش نخليو المساحة للأرقام تبان
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

        // الطبقة 2: شبكة الصلوات الخمس (تقليص الـ padding الفوقاني لـ 2.dp)
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                PrayerSlot(
                    item = item,
                    isActive = index == activeIndex,
                    modifier = GlanceModifier.defaultWeight(1f) // فرض التساوي الرياضي فـ العرض
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

    // كبسولة المحتوى مقادة بـ مليمتر لـ الـ Spacing الداخلي
    val slotContent: @Composable () -> Unit = {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = item.label,
                style = TextStyle(color = ColorProvider(nameColor), fontSize = 10.sp, textAlign = TextAlign.Center)
            )
            // حذفنا الفراغات الزايدة، النص غيجي ديريكت تحت خوه
            Text(
                text = item.time,
                style = TextStyle(color = ColorProvider(timeColor), fontSize = 11.sp, fontWeight = timeWeight, textAlign = TextAlign.Center)
            )
        }
    }

    if (isActive) {
        // الـ Active Box واخد بادينغ خفيف بزاف (2.dp) باش ما يقجش النص لداخل
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





