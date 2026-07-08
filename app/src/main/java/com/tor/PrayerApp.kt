@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.tor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// ==========================================================
// PrayerApp - شاشة أوقات الصلاة بالتفصيل (القسم 4 من الوثيقة)
// تستعمل الثيم والموديلات وقاعدة البيانات المعرفة في PrayerActivity.kt
// (نفس الـ package، لذا لا حاجة لاستيراد إضافي بينهما)
// ==========================================================

@Composable
internal fun PrayerTimesScreen(
    cityId: Int,
    cityName: String,
    initialTimes: DayPrayerTimes
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var times by remember { mutableStateOf<DayPrayerTimes?>(initialTimes) }
    var showDatePicker by remember { mutableStateOf(false) }

    // إعادة جلب الأوقات عند تغيير التاريخ (أسهم اليوم السابق/التالي أو DatePicker)
    LaunchedEffect(selectedDate) {
        times = withContext(Dispatchers.IO) {
            PrayerDbManager.getPrayerTimesForDate(context, cityId, selectedDate)
        }
    }

    val currentTimes = times
    if (currentTimes == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrayerTheme.ACCENT)
        }
        return
    }

    val prayerItems = remember(currentTimes) { buildPrayerList(currentTimes) }
    val isToday = selectedDate == LocalDate.now()

    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(isToday) {
        while (isToday) {
            delay(1000)
            tick = System.currentTimeMillis()
        }
    }

    val nextPrayerState by remember(tick, prayerItems) { derivedStateOf { findNextPrayer(prayerItems) } }
    val (nextItem, nextCal) = nextPrayerState
    val remainingText by remember(tick, isToday) {
        derivedStateOf { if (isToday) formatRemaining(nextCal) else "--:--:--" }
    }

    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND)) {

        // البطاقة العلوية المصغرة - 150.dp (نسخة مضغوطة من بطاقة الهيرو)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrayerTheme.SURFACE, PrayerTheme.SURFACE_GRADIENT_END),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isToday) "الصلاة القادمة: صلاة ${nextItem.label}" else cityName,
                    color = PrayerTheme.ACCENT,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    remainingText,
                    color = PrayerTheme.PRIMARY_TEXT,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // شريط التحكم في التاريخ (Date Ribbon) - 56.dp
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "اليوم السابق",
                tint = PrayerTheme.SECONDARY_TEXT,
                modifier = Modifier.size(20.dp).clickable { selectedDate = selectedDate.minusDays(1) }
            )
            Text(
                "${currentTimes.gregorianDay}/${currentTimes.gregorianMonth} - ${currentTimes.hijriDay} ${currentTimes.hijriMonthName}",
                color = PrayerTheme.ACCENT,
                fontSize = 15.sp,
                modifier = Modifier.clickable { showDatePicker = true }
            )
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = "اليوم التالي",
                tint = PrayerTheme.SECONDARY_TEXT,
                modifier = Modifier.size(20.dp).clickable { selectedDate = selectedDate.plusDays(1) }
            )
        }

        // جدول الصلوات الحية (Prayer Table)
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(prayerItems) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.label, color = PrayerTheme.PRIMARY_TEXT, fontSize = 16.sp)
                    Text(
                        item.time,
                        color = PrayerTheme.PRIMARY_TEXT,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (item.hasAlarmToggle) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = PrayerTheme.ACCENT,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
                HorizontalDivider(color = PrayerTheme.ROW_DIVIDER, thickness = 0.5.dp)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // واجهة اختيار التاريخ الفخمة (DatePicker Dialog) - Dark Theme مخصص
    if (showDatePicker) {
        val zone = ZoneId.systemDefault()
        val initialMillis = selectedDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("تم", color = PrayerTheme.ACCENT) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("إلغاء", color = PrayerTheme.SECONDARY_TEXT)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = PrayerTheme.SURFACE)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = PrayerTheme.SURFACE,
                    titleContentColor = PrayerTheme.PRIMARY_TEXT,
                    headlineContentColor = PrayerTheme.PRIMARY_TEXT,
                    weekdayContentColor = PrayerTheme.SECONDARY_TEXT,
                    dayContentColor = PrayerTheme.PRIMARY_TEXT,
                    selectedDayContainerColor = PrayerTheme.ACCENT,
                    selectedDayContentColor = Color.Black,
                    todayContentColor = PrayerTheme.ACCENT,
                    todayDateBorderColor = PrayerTheme.ACCENT
                )
            )
        }
    }
}
