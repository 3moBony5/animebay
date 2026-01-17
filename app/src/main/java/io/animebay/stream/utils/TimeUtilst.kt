package io.animebay.stream.utils // تأكد من أن الباكج صحيح

import android.os.CountDownTimer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

/**
 * دالة لتحويل تاريخ بصيغة ISO إلى نص وصفي (مثال: "منذ 5 دقائق").
 * هذه الدالة موجودة لديك بالفعل.
 */
fun formatTimeAgo(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) {
        return "" // لا تعرض شيئاً إذا لم يكن هناك تاريخ
    }

    return try {
        // 1. تحليل التاريخ النصي (ISO 8601)
        val publishedInstant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(isoDate))
        val now = Instant.now()

        // 2. حساب الفارق الزمني
        val duration = Duration.between(publishedInstant, now)

        // 3. تحويل الفارق إلى نص مقروء
        when {
            duration.toMinutes() < 1 -> "الآن"
            duration.toMinutes() < 60 -> "منذ ${duration.toMinutes()} دقيقة"
            duration.toHours() < 24 -> "منذ ${duration.toHours()} ساعة"
            duration.toDays() < 2 -> "أمس"
            duration.toDays() < 7 -> "منذ ${duration.toDays()} أيام"
            else -> {
                // إذا كان أقدم من أسبوع، اعرض التاريخ العادي
                val zonedDateTime = publishedInstant.atZone(ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale("ar"))
                formatter.format(zonedDateTime)
            }
        }
    } catch (e: Exception) {
        // في حال كان صيغة التاريخ غير متوقعة
        e.printStackTrace()
        ""
    }
}

/**
 * ✅✅✅ --- الدالة الجديدة --- ✅✅✅
 * دالة Composable لإنشاء عداد تنازلي.
 * تحسب الوقت المتبقي حتى موعد الحلقة القادمة (بافتراض أنها بعد 7 أيام من الحلقة الأخيرة).
 *
 * @param lastEpisodeDateString تاريخ نشر الحلقة الأخيرة بصيغة ISO 8601.
 * @return نص يمثل الوقت المتبقي (مثال: "حلقة جديدة بعد: 6 يوم و 23 ساعة و 59 دقيقة").
 */
@Composable
fun rememberCountdown(lastEpisodePublishedAt: String?): String? {
    var countdownText by remember { mutableStateOf<String?>(null) }

    // DisposableEffect للتأكد من إلغاء الـ timer عند الخروج من الشاشة
    DisposableEffect(lastEpisodePublishedAt) {
        if (lastEpisodePublishedAt.isNullOrBlank()) {
            // إذا لم يكن هناك تاريخ، لا تفعل شيئاً
            onDispose { }
        }

        val timer = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
            val lastEpisodeTime = sdf.parse(lastEpisodePublishedAt)

            val nextEpisodeTime = Calendar.getInstance().apply {
                time = lastEpisodeTime
                add(Calendar.DAY_OF_YEAR, 7)
            }

            val now = Calendar.getInstance().timeInMillis
            val millisUntilFinished = nextEpisodeTime.timeInMillis - now

            if (millisUntilFinished <= 0) {
                countdownText = null // الحلقة صدرت بالفعل، لا تعرض العداد
                return@DisposableEffect onDispose { }
            }

            object : CountDownTimer(millisUntilFinished, 1000) { // التحديث كل 1000ms (ثانية)
                override fun onTick(millisLeft: Long) {
                    val days = TimeUnit.MILLISECONDS.toDays(millisLeft)
                    val hours = TimeUnit.MILLISECONDS.toHours(millisLeft) % 24
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60
                    // ✅ --- التعديل هنا --- ✅
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisLeft) % 60

                    // بناء النص مع الثواني
                    countdownText = "حلقة جديدة بعد: ${days} يوم ${hours} ساعة ${minutes} دقيقة ${seconds} ثانية"
                }

                override fun onFinish() {
                    countdownText = null // انتهى الوقت، قم بإخفاء العداد
                }
            }.start()

        } catch (e: Exception) {
            e.printStackTrace()
            countdownText = null
            null
        }

        onDispose {
            timer?.cancel()
        }
    }

    return countdownText
}
