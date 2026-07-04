package com.englishfriendai.app.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Buckets used to group conversation history, per the History screen spec. */
enum class DateBucket {
    TODAY, YESTERDAY, LAST_WEEK, LAST_MONTH, OLDER
}

object DateUtils {

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun formatTime(epochMillis: Long): String = timeFormatter.format(Date(epochMillis))

    fun formatDate(epochMillis: Long): String = dateFormatter.format(Date(epochMillis))

    /** Buckets a timestamp relative to "now" for the History screen's grouped sections. */
    fun bucketFor(epochMillis: Long, now: Long = System.currentTimeMillis()): DateBucket {
        val today = startOfDay(now)
        val yesterday = today - DAY_MILLIS
        val weekAgo = today - 7 * DAY_MILLIS
        val monthAgo = today - 30 * DAY_MILLIS

        return when {
            epochMillis >= today -> DateBucket.TODAY
            epochMillis >= yesterday -> DateBucket.YESTERDAY
            epochMillis >= weekAgo -> DateBucket.LAST_WEEK
            epochMillis >= monthAgo -> DateBucket.LAST_MONTH
            else -> DateBucket.OLDER
        }
    }

    private fun startOfDay(epochMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = epochMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
}
