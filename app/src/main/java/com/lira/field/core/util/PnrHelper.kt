package com.lira.field.core.util

import androidx.compose.ui.graphics.Color
import com.lira.field.core.model.PnrStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PnrHelper {

    /**
     * Formats act_by ISO string and current timestamp into:
     * "ACT BY 14:00 · 1h 35m" (or localized format in driver's language)
     */
    fun formatCountdown(
        actByIso: String?,
        nowMillis: Long = System.currentTimeMillis(),
        language: String = "en"
    ): String {
        if (actByIso.isNullOrBlank()) return ""
        val targetTime = parseTimeToMillis(actByIso, nowMillis) ?: return ""
        val timeStr = formatTimeToHHmm(targetTime)
        val diffMs = targetTime - nowMillis

        val prefix = when (language.lowercase(Locale.ROOT)) {
            "hi" -> "प्रस्थान समय"
            "mr" -> "निघण्याची वेळ"
            "ta" -> "புறப்படும் நேரம்"
            "kn" -> "ಹೊರಡುವ ಸಮಯ"
            "te" -> "బయలుదేరే సమయం"
            else -> "ACT BY"
        }

        if (diffMs <= 0) {
            val pastDiff = -diffMs
            val hours = pastDiff / (1000 * 60 * 60)
            val minutes = (pastDiff % (1000 * 60 * 60)) / (1000 * 60)
            val durationStr = if (hours > 0) "${hours}h ${minutes}m overdue" else "${minutes}m overdue"
            return "$prefix $timeStr · $durationStr"
        }

        val hours = diffMs / (1000 * 60 * 60)
        val minutes = (diffMs % (1000 * 60 * 60)) / (1000 * 60)
        val durationStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        return "$prefix $timeStr · $durationStr"
    }

    /**
     * Colour mapping by pnr_status:
     * ON_TRACK -> Green (#22C55E)
     * PNR_IMMINENT -> Amber (#F59E0B)
     * PAST_SAFE_PNR -> Red (#EF4444)
     * ALREADY_LOST -> Calm Grey (#94A3B8)
     */
    fun getStatusColor(status: PnrStatus?): Color {
        return when (status) {
            PnrStatus.ON_TRACK -> Color(0xFF22C55E)
            PnrStatus.PNR_IMMINENT -> Color(0xFFF59E0B)
            PnrStatus.PAST_SAFE_PNR -> Color(0xFFEF4444)
            PnrStatus.ALREADY_LOST -> Color(0xFF94A3B8)
            PnrStatus.UNKNOWN, null -> Color(0xFF94A3B8)
        }
    }

    /**
     * Determines whether driver dispatch ACK actions are allowed.
     * Lost jobs MUST NOT have ACK buttons.
     */
    fun isAckAllowed(status: PnrStatus?): Boolean {
        return status != PnrStatus.ALREADY_LOST
    }

    fun isAckAllowed(statusString: String?): Boolean {
        if (statusString == null) return true
        return !statusString.equals("ALREADY_LOST", ignoreCase = true)
    }

    fun parseTimeToMillis(timeString: String, baseNowMillis: Long = System.currentTimeMillis()): Long? {
        val trimmed = timeString.trim()
        val isoPatterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm"
        )
        for (p in isoPatterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.US)
                val d = sdf.parse(trimmed)
                if (d != null) return d.time
            } catch (_: Exception) {}
        }

        // Support simple "HH:mm" relative to current day
        try {
            val parts = trimmed.split(":")
            if (parts.size >= 2) {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = baseNowMillis
                    set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    set(Calendar.MINUTE, parts[1].toInt())
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                return cal.timeInMillis
            }
        } catch (_: Exception) {}

        return null
    }

    fun formatTimeToHHmm(epochMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date(epochMillis))
    }

    fun getPnrImminentTtsMessage(language: String): String {
        return when (language.lowercase(Locale.ROOT)) {
            "hi" -> "आपको 10 मिनट के भीतर निकलना होगा"
            "mr" -> "तुम्हाला 10 मिनिटांच्या आत निघावे लागेल"
            "ta" -> "நீங்கள் 10 நிமிடங்களுக்குள் புறப்பட வேண்டும்"
            "kn" -> "ನೀವು 10 ನಿಮಿಷಗಳ ಒಳಗೆ ಹೊರಡಬೇಕು"
            "te" -> "మీరు 10 నిమిషాల లోపు బయలుదేరాలి"
            else -> "You must leave within 10 minutes"
        }
    }
}

/**
 * Tracks PNR status changes and ensures TTS / audible alert triggers strictly once
 * per transition to PNR_IMMINENT.
 */
class PnrAlertTracker {
    private var lastStatus: PnrStatus? = null

    fun shouldTriggerImminentAlert(newStatus: PnrStatus?): Boolean {
        val previous = lastStatus
        lastStatus = newStatus
        return newStatus == PnrStatus.PNR_IMMINENT && previous != PnrStatus.PNR_IMMINENT
    }

    fun reset() {
        lastStatus = null
    }
}

