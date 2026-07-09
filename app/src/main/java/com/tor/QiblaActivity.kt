package com.tor

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

// ==========================================================
// ثوابت الكعبة + إعدادات الفلتر والمحاذاة
// ==========================================================

private const val KAABA_LAT = 21.422487
private const val KAABA_LON = 39.826206
private const val LOW_PASS_ALPHA = 0.15f
private const val ALIGNMENT_THRESHOLD_DEG = 1f
private const val MAP_ZOOM_LEVEL = 18.0

// ==========================================================
// أدوات جغرافية عامة (Bearing / Distance)
// ==========================================================

private fun calculateQiblaBearing(lat: Double, lon: Double): Double {
    val phi1 = Math.toRadians(lat)
    val phi2 = Math.toRadians(KAABA_LAT)
    val deltaLambda = Math.toRadians(KAABA_LON - lon)
    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    val theta = atan2(y, x)
    return (Math.toDegrees(theta) + 360.0) % 360.0
}

private fun calculateDistanceToKaabaKm(lat: Double, lon: Double): Double {
    val r = 6371.0088
    val phi1 = Math.toRadians(lat)
    val phi2 = Math.toRadians(KAABA_LAT)
    val dPhi = Math.toRadians(KAABA_LAT - lat)
    val dLambda = Math.toRadians(KAABA_LON - lon)
    val a = sin(dPhi / 2).let { it * it } + cos(phi1) * cos(phi2) * sin(dLambda / 2).let { it * it }
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

private fun normalizeAngle(angle: Float): Float {
    var a = angle % 360f
    if (a > 180f) a -= 360f
    if (a < -180f) a += 360f
    return a
}

// ==========================================================
// 4. محرك الحساب الفلكي المحلي (Pure Local NOAA Math)
// كائن Kotlin خالص - بلا أي مكتبة خارجية مثل SunCalc
// ==========================================================

object SunCalculator {

    data class SunPosition(
        val azimuthDeg: Double,   // اتجاه الشمس من الشمال الجغرافي
        val elevationDeg: Double, // زاوية الارتفاع فوق الأفق
        val isDaytime: Boolean
    )

    fun calculate(lat: Double, lon: Double, utcMillis: Long): SunPosition {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val hourUtc = cal.get(Calendar.HOUR_OF_DAY) +
                cal.get(Calendar.MINUTE) / 60.0 +
                cal.get(Calendar.SECOND) / 3600.0

        // 1. زاوية السنة الكسرية γ (بالراديان)
        val gamma = (2.0 * PI / 365.0) * (dayOfYear - 1 + (hourUtc - 12.0) / 24.0)

        // 2. معادلة الوقت الدقيقة Et (بالدقائق)
        val eqTimeMin = 229.18 * (
                0.000075 +
                        0.001868 * cos(gamma) -
                        0.032077 * sin(gamma) -
                        0.014615 * cos(2 * gamma) -
                        0.040849 * sin(2 * gamma)
                )

        // 3. الانحراف الشمسي δ (بالراديان)
        val declRad = 0.006918 -
                0.399912 * cos(gamma) +
                0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) +
                0.000907 * sin(2 * gamma) -
                0.002697 * cos(3 * gamma) +
                0.00148 * sin(3 * gamma)

        // الوقت الشمسي الحقيقي (بالدقائق) - نشتغل مباشرة بالتوقيت UTC (timezone offset = 0)
        val timeOffsetMin = eqTimeMin + 4.0 * lon
        val trueSolarTimeMin = hourUtc * 60.0 + timeOffsetMin

        var hourAngleDeg = (trueSolarTimeMin / 4.0) - 180.0
        if (hourAngleDeg < -180.0) hourAngleDeg += 360.0
        if (hourAngleDeg > 180.0) hourAngleDeg -= 360.0
        val hourAngleRad = Math.toRadians(hourAngleDeg)

        val latRad = Math.toRadians(lat)

        // 4. زاوية الارتفاع الشمسي (للتحقق من كون الشمس فوق الأفق = نهار)
        val cosZenith = (sin(latRad) * sin(declRad) + cos(latRad) * cos(declRad) * cos(hourAngleRad))
            .coerceIn(-1.0, 1.0)
        val zenithRad = acos(cosZenith)
        val elevationDeg = 90.0 - Math.toDegrees(zenithRad)

        // 5. اتجاه الشمس الحقيقي (Solar Azimuth) من الشمال الجغرافي
        val azDenom = cos(latRad) * sin(zenithRad)
        val azimuthDeg: Double = if (abs(azDenom) < 1e-6) {
            0.0
        } else {
            var cosAz = (sin(latRad) * cos(zenithRad) - sin(declRad)) / azDenom
            cosAz = cosAz.coerceIn(-1.0, 1.0)
            val acosAzDeg = Math.toDegrees(acos(cosAz))
            if (hourAngleDeg > 0.0) (acosAzDeg + 180.0).mod(360.0) else (540.0 - acosAzDeg).mod(360.0)
        }

        return SunPosition(azimuthDeg = azimuthDeg, elevationDeg = elevationDeg, isDaytime = elevationDeg > 0.0)
    }
}

// ==========================================================
// حالة دقة المستشعر المغناطيسي
// ==========================================================

enum class CalibrationState { GOOD, MEDIUM, POOR, UNKNOWN }

// ==========================================================
// إدارة الحالة - QiblaViewModel + StateFlow
// ==========================================================

data class QiblaUiState(
    val hasMagnetometer: Boolean = true,
    val lat: Double? = null,
    val lon: Double? = null,
    val qiblaBearing: Double = 0.0,
    val distanceKm: Double = 0.0,
    val geodesicPoints: List<Pair<Double, Double>> = emptyList(), // (lat, lon) من المستخدم إلى الكعبة
    val deviceAzimuth: Float = 0f, // الشمال المغناطيسي الخام من المستشعر
    val calibration: CalibrationState = CalibrationState.UNKNOWN,
    val sunAzimuthDeg: Double? = null,
    val sunElevationDeg: Double? = null
)

class QiblaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    fun setHasMagnetometer(has: Boolean) {
        _uiState.update { it.copy(hasMagnetometer = has) }
    }

    fun setLocation(lat: Double, lon: Double) {
        val bearing = calculateQiblaBearing(lat, lon)
        val distance = calculateDistanceToKaabaKm(lat, lon)
        val points = generateGeodesicPolyline(lat, lon, KAABA_LAT, KAABA_LON, segments = 64)
        _uiState.update {
            it.copy(lat = lat, lon = lon, qiblaBearing = bearing, distanceKm = distance, geodesicPoints = points)
        }
        refreshSunPosition()
    }

    fun updateDeviceAzimuth(azimuth: Float) {
        _uiState.update { it.copy(deviceAzimuth = azimuth) }
    }

    fun updateCalibration(state: CalibrationState) {
        _uiState.update { it.copy(calibration = state) }
    }

    fun refreshSunPosition() {
        val state = _uiState.value
        val lat = state.lat ?: return
        val lon = state.lon ?: return
        val sun = SunCalculator.calculate(lat, lon, System.currentTimeMillis())
        _uiState.update { it.copy(sunAzimuthDeg = sun.azimuthDeg, sunElevationDeg = sun.elevationDeg) }
    }

    // ==========================================================
    // 3. دالة الدائرة العظمى - Geodesic Interpolation (داخل الـ ViewModel كما ينص دفتر التحملات)
    // تولد مصفوفة إحداثيات بينية لتمثيل أقصر مسار كروي (Great-circle) - وليس خطاً مستقيماً (Rhumb Line)
    // ==========================================================
    private fun generateGeodesicPolyline(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
        segments: Int
    ): List<Pair<Double, Double>> {
        val phi1 = Math.toRadians(lat1); val lambda1 = Math.toRadians(lon1)
        val phi2 = Math.toRadians(lat2); val lambda2 = Math.toRadians(lon2)

        val angularDistance = 2 * asin(
            sqrt(
                sin((phi2 - phi1) / 2).pow(2) +
                        cos(phi1) * cos(phi2) * sin((lambda2 - lambda1) / 2).pow(2)
            )
        )

        if (angularDistance == 0.0) return listOf(lat1 to lon1)

        val result = mutableListOf<Pair<Double, Double>>()
        for (i in 0..segments) {
            val f = i.toDouble() / segments
            val a = sin((1 - f) * angularDistance) / sin(angularDistance)
            val b = sin(f * angularDistance) / sin(angularDistance)
            val x = a * cos(phi1) * cos(lambda1) + b * cos(phi2) * cos(lambda2)
            val y = a * cos(phi1) * sin(lambda1) + b * cos(phi2) * sin(lambda2)
            val z = a * sin(phi1) + b * sin(phi2)
            val phi = atan2(z, sqrt(x * x + y * y))
            val lambda = atan2(y, x)
            result.add(Math.toDegrees(phi) to Math.toDegrees(lambda))
        }
        return result
    }
}

// ==========================================================
// QiblaActivity
// ==========================================================

class QiblaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // إعداد osmdroid إلزامي قبل أي استخدام لـ MapView (تخزين الكاش داخل مجلد التطبيق - بلا صلاحية تخزين خارجية)
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = getExternalFilesDir(null) ?: filesDir
            osmdroidTileCache = java.io.File(osmdroidBasePath, "tiles")
        }

        setContent {
            QiblaScreen(onBack = { finish() })
        }
    }
}

// ==========================================================
// الشاشة الرئيسية للقبلة - تفحص العتاد وتوزع بين الوضعين
// ==========================================================

@Composable
private fun QiblaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: QiblaViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchLocationBasic(
                context,
                onSuccess = { lat, lon -> viewModel.setLocation(lat, lon) },
                onFailure = { permissionDenied = true }
            )
        } else {
            permissionDenied = true
        }
    }

    // فحص العتاد أولاً (Magnetometer) + طلب الموقع
    LaunchedEffect(Unit) {
        val hasMagnetometer = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS) &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
        viewModel.setHasMagnetometer(hasMagnetometer)

        fetchLocationBasic(
            context,
            onSuccess = { lat, lon -> viewModel.setLocation(lat, lon) },
            onFailure = { permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) }
        )
    }

    // تحديث زاوية الشمس كل 30 ثانية (فقط بعد توفر الموقع)
    LaunchedEffect(uiState.lat, uiState.lon) {
        if (uiState.lat == null) return@LaunchedEffect
        while (true) {
            delay(30_000)
            viewModel.refreshSunPosition()
        }
    }

    // تسجيل المستشعرات مرة واحدة فقط - فقط إذا كان العتاد متوفراً
    if (uiState.hasMagnetometer) {
        DisposableEffect(Unit) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            // فلتر التنعيم Low-Pass لمنع ارتعاش القراءات الخام
                            for (i in 0..2) gravity[i] = gravity[i] + LOW_PASS_ALPHA * (event.values[i] - gravity[i])
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            for (i in 0..2) geomagnetic[i] = geomagnetic[i] + LOW_PASS_ALPHA * (event.values[i] - geomagnetic[i])
                        }
                    }
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        viewModel.updateDeviceAzimuth((azimuthDeg + 360f) % 360f)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        viewModel.updateCalibration(
                            when (accuracy) {
                                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CalibrationState.GOOD
                                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CalibrationState.MEDIUM
                                else -> CalibrationState.POOR
                            }
                        )
                    }
                }
            }

            accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
            magnetometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }

            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(PrayerTheme.BACKGROUND)) {
        QiblaTopBar(
            onBack = onBack,
            calibration = if (uiState.hasMagnetometer) uiState.calibration else null
        )

        when {
            permissionDenied && uiState.lat == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تحتاج صلاحية الموقع لتحديد اتجاه القبلة", color = PrayerTheme.PRIMARY_TEXT, fontSize = 15.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrayerTheme.ACCENT)
                        ) { Text("منح الصلاحية", color = Color.Black) }
                    }
                }
            }
            uiState.lat == null || uiState.lon == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrayerTheme.ACCENT)
                }
            }
            uiState.hasMagnetometer -> {
                CompassModeContent(uiState = uiState)
            }
            else -> {
                HybridMapModeContent(uiState = uiState)
            }
        }
    }
}

// ==========================================================
// شريط علوي - 56.dp
// ==========================================================

@Composable
private fun QiblaTopBar(onBack: () -> Unit, calibration: CalibrationState?) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PrayerTheme.PRIMARY_TEXT)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("اتجاه القبلة", color = PrayerTheme.PRIMARY_TEXT, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))

        val dotColor = when (calibration) {
            CalibrationState.GOOD -> Color(0xFF34C759)
            CalibrationState.MEDIUM -> Color(0xFFFF9F0A)
            CalibrationState.POOR -> Color(0xFFFF3B30)
            else -> PrayerTheme.INACTIVE
        }
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
    }
}

// ==========================================================
// 5. محرك البوصلة الأساسي (Compass Mode)
// ==========================================================

@Composable
private fun CompassModeContent(uiState: QiblaUiState) {
    val haptic = LocalHapticFeedback.current
    val lat = uiState.lat ?: return
    val lon = uiState.lon ?: return

    // تصحيح الانحراف المغناطيسي عبر GeomagneticField
    val declination = remember(lat, lon) {
        GeomagneticField(lat.toFloat(), lon.toFloat(), 0f, System.currentTimeMillis()).declination
    }
    val trueAzimuth = (uiState.deviceAzimuth + declination + 360f) % 360f

    val dialRotation = -trueAzimuth
    val kaabaArrowAngle = normalizeAngle((uiState.qiblaBearing.toFloat() - trueAzimuth))
    val isAligned = abs(kaabaArrowAngle) < ALIGNMENT_THRESHOLD_DEG

    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        wasAligned = isAligned
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
            CompassDial(dialRotationDeg = dialRotation, kaabaAngleDeg = kaabaArrowAngle, isAligned = isAligned)
        }
        Spacer(Modifier.height(24.dp))
        InfoCard(angleDeg = uiState.qiblaBearing, distanceKm = uiState.distanceKm, hint = null)
    }
}

@Composable
private fun CompassDial(dialRotationDeg: Float, kaabaAngleDeg: Float, isAligned: Boolean) {
    val primaryColor = PrayerTheme.PRIMARY_TEXT
    val secondaryColor = PrayerTheme.SECONDARY_TEXT
    val surfaceColor = PrayerTheme.SURFACE
    val borderColor = PrayerTheme.BORDER
    val accentColor = if (isAligned) PrayerTheme.ACCENT else PrayerTheme.ACCENT.copy(alpha = 0.85f)

    Canvas(modifier = Modifier.size(280.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = surfaceColor, radius = radius, center = center)
        drawCircle(color = borderColor, radius = radius, center = center, style = Stroke(width = 2f))

        fun pointAt(angleDeg: Float, r: Float): Offset {
            val rad = Math.toRadians((angleDeg - 90.0)).toFloat()
            return Offset(center.x + r * cos(rad), center.y + r * sin(rad))
        }

        for (deg in 0 until 360 step 10) {
            val rotated = deg + dialRotationDeg
            val isMajor = deg % 90 == 0
            val outer = pointAt(rotated, radius - 4.dp.toPx())
            val inner = pointAt(rotated, radius - if (isMajor) 20.dp.toPx() else 12.dp.toPx())
            drawLine(
                color = if (isMajor) primaryColor else secondaryColor,
                start = inner, end = outer,
                strokeWidth = if (isMajor) 3f else 1.5f,
                cap = StrokeCap.Round
            )
        }

        val labels = listOf(0f to "ش", 90f to "ق", 180f to "ج", 270f to "غ")
        val textRadius = radius - 36.dp.toPx()
        labels.forEach { (baseAngle, label) ->
            val rotated = baseAngle + dialRotationDeg
            val pos = pointAt(rotated, textRadius)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = primaryColor.toArgbCompat()
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(label, pos.x, pos.y + paint.textSize / 3f, paint)
            }
        }

        val needleTip = pointAt(dialRotationDeg, radius - 40.dp.toPx())
        drawLine(color = secondaryColor, start = center, end = needleTip, strokeWidth = 2.5f, cap = StrokeCap.Round)

        val arrowTip = pointAt(kaabaAngleDeg, radius - 24.dp.toPx())
        drawLine(color = accentColor, start = center, end = arrowTip, strokeWidth = 6f, cap = StrokeCap.Round)

        val kaabaSize = 14.dp.toPx()
        val kaabaRad = Math.toRadians((kaabaAngleDeg - 90.0)).toFloat()
        val kaabaCenter = Offset(
            center.x + (radius - 24.dp.toPx()) * cos(kaabaRad),
            center.y + (radius - 24.dp.toPx()) * sin(kaabaRad)
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(kaabaCenter.x - kaabaSize / 2f, kaabaCenter.y - kaabaSize / 2f),
            size = androidx.compose.ui.geometry.Size(kaabaSize, kaabaSize)
        )
        drawRect(
            color = accentColor,
            topLeft = Offset(kaabaCenter.x - kaabaSize / 2f, kaabaCenter.y - kaabaSize / 2f),
            size = androidx.compose.ui.geometry.Size(kaabaSize, kaabaSize),
            style = Stroke(width = 2f)
        )

        drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = center)
    }
}

private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt()
)

// ==========================================================
// 3. محرك الخريطة بدون مستشعر - North-Up OSM Engine (osmdroid)
// ==========================================================

@Composable
private fun HybridMapModeContent(uiState: QiblaUiState) {
    val lat = uiState.lat ?: return
    val lon = uiState.lon ?: return

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(320.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            OsmNorthUpMap(
                userLat = lat,
                userLon = lon,
                geodesicPoints = uiState.geodesicPoints
            )
        }

        Spacer(Modifier.height(16.dp))

        // محرك الشمس البصري: دائرة ثابتة + سهم القبلة + سهم الشمس
        SunEngineWidget(qiblaBearing = uiState.qiblaBearing, sunAzimuth = uiState.sunAzimuthDeg, sunElevation = uiState.sunElevationDeg)

        Spacer(Modifier.height(12.dp))

        Text(
            "قم بتدوير الهاتف يدوياً لمطابقة الخريطة أو استخدم زاوية الشمس الموضحة",
            color = PrayerTheme.SECONDARY_TEXT,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(16.dp))

        InfoCard(
            angleDeg = uiState.qiblaBearing,
            distanceKm = uiState.distanceKm,
            hint = null
        )
    }
}

@Composable
private fun OsmNorthUpMap(userLat: Double, userLon: Double, geodesicPoints: List<Pair<Double, Double>>) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true) // تكبير/تصغير بالأصابع مسموح، لكن بلا أي Overlay للدوران = Bearing يبقى 0 دوماً (North-Up Lock)
                controller.setZoom(MAP_ZOOM_LEVEL)
                controller.setCenter(GeoPoint(userLat, userLon))

                val userPoint = GeoPoint(userLat, userLon)
                val userMarker = Marker(this).apply {
                    position = userPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = "موقعك"
                }
                overlays.add(userMarker)

                // خط الدائرة العظمى (Geodesic Polyline) - وليس خطاً مستقيماً عادياً
                val polyline = Polyline(this).apply {
                    outlinePaint.color = android.graphics.Color.parseColor("#D4AF37")
                    outlinePaint.strokeWidth = 3.dp.value * resources.displayMetrics.density
                    setPoints(geodesicPoints.map { (plat, plon) -> GeoPoint(plat, plon) })
                }
                overlays.add(polyline)
            }
        }
    )
}

@Composable
private fun SunEngineWidget(qiblaBearing: Double, sunAzimuth: Double?, sunElevation: Double?) {
    val accentColor = PrayerTheme.ACCENT
    val primaryColor = PrayerTheme.PRIMARY_TEXT
    val secondaryColor = PrayerTheme.SECONDARY_TEXT

    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f - 8.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(color = PrayerTheme.SURFACE, radius = radius, center = center)
            drawCircle(color = PrayerTheme.BORDER, radius = radius, center = center, style = Stroke(width = 1.5f))

            fun pointAt(angleDeg: Double, r: Float): Offset {
                val rad = Math.toRadians(angleDeg - 90.0)
                return Offset(center.x + r * cos(rad).toFloat(), center.y + r * sin(rad).toFloat())
            }

            // سهم القبلة (ثابت دوماً في الأعلى كمرجع لأن الخريطة أصلاً North-Up)
            val qiblaTip = pointAt(qiblaBearing, radius - 10.dp.toPx())
            drawLine(color = accentColor, start = center, end = qiblaTip, strokeWidth = 5f, cap = StrokeCap.Round)

            // سهم الشمس - يظهر فقط نهاراً
            if (sunAzimuth != null && (sunElevation ?: -1.0) > 0.0) {
                val sunTip = pointAt(sunAzimuth, radius - 24.dp.toPx())
                drawLine(color = Color(0xFFFFC107), start = center, end = sunTip, strokeWidth = 4f, cap = StrokeCap.Round)
                drawCircle(color = Color(0xFFFFC107), radius = 6.dp.toPx(), center = sunTip)
            }

            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = center)
        }
    }

    if (sunAzimuth != null) {
        val delta = normalizeAngle((qiblaBearing - sunAzimuth).toFloat())
        Text(
            if ((sunElevation ?: -1.0) > 0.0)
                "الفرق بين اتجاه الشمس والقبلة: ${abs(delta).roundToInt()}°"
            else
                "الشمس تحت الأفق الآن - استخدم الخريطة فقط",
            color = secondaryColor,
            fontSize = 13.sp
        )
    }
}

// ==========================================================
// 2. البطاقة السفلية - 16.dp زوايا
// ==========================================================

@Composable
private fun InfoCard(angleDeg: Double, distanceKm: Double, hint: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PrayerTheme.SURFACE.copy(alpha = 0.9f))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "${"%.1f".format(angleDeg)}°",
            color = PrayerTheme.PRIMARY_TEXT,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text("انحراف القبلة من الشمال", color = PrayerTheme.SECONDARY_TEXT, fontSize = 12.sp)

        Text(
            "${"%,.0f".format(distanceKm)} كم إلى الكعبة (دائرة عظمى)",
            color = PrayerTheme.ACCENT,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        if (hint != null) {
            Text(hint, color = PrayerTheme.SECONDARY_TEXT, fontSize = 12.sp)
        }
    }
}






