package com.jin.news.util

import java.text.SimpleDateFormat
import java.util.*

class TimeFormat {
    companion object {
        private val defaultDateFormat = SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US)

        fun getDuration(lastBuildDate: String, pubDate: String): String {
            val date1 = defaultDateFormat.parse(lastBuildDate)?.time
            val date2 = defaultDateFormat.parse(pubDate)?.time

            return date1?.let { date2?.let { date1 - date2 } }?.let {
                when (Preferences.getLanguage()) {
                    "en" -> form(it, pubDate, "Just now", "1 min", " mins", "1 hr", " hrs")
                    "ko" -> form(it, pubDate, "방금 전", "1분", "분", "1시간", "시간")
                    "ja" -> form(it, pubDate, "ちょうど今", "1 分", " 分", "1 時間", " 時間")
                    else -> ""
                }
            } ?: ""
        }

        fun getTime(date: String): String = when (Preferences.getLanguage()) {
            "en" -> SimpleDateFormat("d MMM 'at' h:mm aa", Locale.US)
                .format(defaultDateFormat.parse(date)?.time)
            "ko" -> SimpleDateFormat("M월 d일 aa h:mm", Locale.KOREA)
                .format(defaultDateFormat.parse(date)?.time)
            "ja" -> SimpleDateFormat("M月 d日 aa h:mm", Locale.JAPAN)
                .format(defaultDateFormat.parse(date)?.time)
            else -> ""
        }

        private fun form(
            duration: Long, date: String,
            now: String, min: String, mins: String, hr: String, hrs: String
        ) = when (0L) {
            duration / (1000L * 60) -> now
            duration / (1000L * 60 * 2) -> min
            duration / (1000L * 60 * 60) -> "${duration / (1000L * 60)}$mins"
            duration / (1000L * 60 * 60 * 2) -> hr
            duration / (1000L * 60 * 60 * 24) -> "${duration / (1000L * 60 * 60)}$hrs"
            else -> getTime(date)
        }
    }
}