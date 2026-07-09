package com.tor

import android.content.Context
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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ==========================================================
// ثيم الويدجت (نفس منطق PrayerTheme لكن كـ Color مباشرة، Glance ما كيدعمش object composable resolution بنفس طريقة Compose العادي)
// ==========================================================

private object WidgetTheme {
    val ROOT_BACKGROUND = Color(0xD9121212)
    val ACTIVE_BACKGROUND = Color(0xFF222222)
    val ACCENT = Color(0xFFD4AF37)
    val PRIMARY = Color(0xFFFFFFFF)
    val SECONDARY = Color(0xFF9E9E9E)
}

// أسماء الصلوات الخمس فقط (بدون الشروق) كما ينص دفتر التحملات
private val WIDGET_PRAYER_KEYS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

// ==========================================================
// GlanceAppWidgetReceiver - نقطة الدخول التي يسجلها النظام
// ==========================================================

class PrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerWidget()
}

// ==========================================================
// PrayerWidget - المنطق الرئيسي (4x1)
// ==========================================================

class PrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 5. حساب الفهرس النشط يتم هنا، خارج الـ Composable، قبل أي رسم للواجهة (Zero Variable Re-instantiation)
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

        val (nextItem, nextCal) = findNextPrayer(fiveItems)
        val remainingText = formatRemaining(nextCal)
        val activeIndex = fiveItems.indexOfFirst { it.key == nextItem.key }
        val hijriText = "${times.hijriDay} ${times.hijriMonthName} 1448"

        provideContent {
            PrayerWidgetContent(
                hijriText = hijriText,
                countdownText = "- $remainingText",
                items = fiveItems,
                activeIndex = activeIndex
            )
        }
    }
}

// ==========================================================
// 1. الهيكل الجذري - Column يحتوي طبقتين (الهيدر + شبكة الصلوات)
// ==========================================================

@androidx.compose.runtime.Composable
private fun PrayerWidgetContent(
    hijriText: String,
    countdownText: String,
    items: List<PrayerItem>,
    activeIndex: Int
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .heightIn72()
            .background(WidgetTheme.ROOT_BACKGROUND)
            .cornerRadius(16.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // الطبقة 1: الهيدر الموحد
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = hijriText,
                style = TextStyle(
                    color = ColorProvider(WidgetTheme.SECONDARY),
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = countdownText,
                style = TextStyle(
                    color = ColorProvider(WidgetTheme.ACCENT),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // الطبقة 2: شبكة الصلوات الخمس الموحدة
        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp)) {
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

// GlanceModifier ما فيهوش height(72.dp) مباشر بسهولة قابلة لإعادة الاستخدام كـ extension نظيفة، هنا نلتزم بالحد الأقصى المطلوب
private fun GlanceModifier.heightIn72(): GlanceModifier = this.height(72.dp)

// ==========================================================
// 4. محرك التمييز المعزول - العنصر النشط فقط يُغلف بـ Box مع خلفية وزوايا دائرية
// ==========================================================

@androidx.compose.runtime.Composable
private fun PrayerSlot(item: PrayerItem, isActive: Boolean, modifier: GlanceModifier) {
    val nameColor = if (isActive) WidgetTheme.PRIMARY else WidgetTheme.SECONDARY
    val timeColor = if (isActive) WidgetTheme.ACCENT else WidgetTheme.SECONDARY
    val timeWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

    val slotContent: @androidx.compose.runtime.Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Text(
                text = item.label,
                style = TextStyle(color = ColorProvider(nameColor), fontSize = 11.sp, textAlign = TextAlign.Center)
            )
            Text(
                text = item.time,
                style = TextStyle(color = ColorProvider(timeColor), fontSize = 12.sp, fontWeight = timeWeight, textAlign = TextAlign.Center)
            )
        }
    }

    if (isActive) {
        Box(
            modifier = modifier
                .background(WidgetTheme.ACTIVE_BACKGROUND)
                .cornerRadius(8.dp)
                .padding(vertical = 4.dp, horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) { slotContent() }
    } else {
        Box(
            modifier = modifier.padding(vertical = 4.dp, horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) { slotContent() }
    }
}

// ==========================================================
// حالة فارغة - لا مدينة محفوظة بعد (المستخدم ما فتحش التطبيق أول مرة)
// ==========================================================

@androidx.compose.runtime.Composable
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
