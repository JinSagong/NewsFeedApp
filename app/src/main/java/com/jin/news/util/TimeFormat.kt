package com.jin.news.util

import com.jin.news.MyApplication.Companion.languagePreferences
import java.text.SimpleDateFormat
import java.util.*

object TimeFormat {
    fun toMillis(date: String) = SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US).parse(date)?.time

    fun currentMillis() = Calendar.getInstance().timeInMillis

    fun getDuration(lastBuildDate: String, pubDate: String): String {
        val date1 = toMillis(lastBuildDate)
        val date2 = toMillis(pubDate)

        return date1?.let { date2?.let { date1 - date2 } }?.let {
            when (languagePreferences.getLanguage()) {
                "ko" -> form(it, date1, date2, "방금 전", "1분", "분", "1시간", "시간")
                "ja" -> form(it, date1, date2, "ちょうど今", "1 分", " 分", "1 時間", " 時間")
                else -> form(it, date1, date2, "Just now", "1 min", " mins", "1 hr", " hrs")
            }
        } ?: ""
    }

    fun getTime(date1: Long?, date2: Long?): String {
        // 서버에 기록된 뉴스 작성 시간과 실제 뉴스 작성 시간이 달라서 24시간 전에 작성된 뉴스는 날짜만 표기함
        val flag = when {
            // 마지막 업데이트 시간
            date1 == null -> 0
            // 같은 년도에 작성된 뉴스
            SimpleDateFormat("yyyy", Locale.getDefault()).run { format(date1) == format(date2) } -> 1
            // 다른 년도에 작성된 뉴스
            else -> 2
        }
        return when (languagePreferences.getLanguage()) {
            "ko" -> when (flag) {
                1 -> SimpleDateFormat("M월 d일", Locale.KOREA).format(date2)
                2 -> SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(date2)
                else -> SimpleDateFormat("M월 d일 aa h:mm", Locale.KOREA).format(date2)
            }
            "ja" -> when (flag) {
                1 -> SimpleDateFormat("M月 d日", Locale.JAPAN).format(date2)
                2 -> SimpleDateFormat("yyyy年 M月 d日", Locale.JAPAN).format(date2)
                else -> SimpleDateFormat("M月 d日 aa h:mm", Locale.JAPAN).format(date2)
            }
            else -> when (flag) {
                1 -> SimpleDateFormat("d MMM", Locale.US).format(date2)
                2 -> SimpleDateFormat("d MMM yyyy", Locale.US).format(date2)
                else -> SimpleDateFormat("d MMM 'at' h:mm aa", Locale.US).format(date2)
            }
        }
    }

    private fun form(
        duration: Long, date1: Long?, date2: Long?,
        now: String, min: String, mins: String, hr: String, hrs: String
    ) = when (0L) {
        duration / (1000L * 60) -> now
        duration / (1000L * 60 * 2) -> min
        duration / (1000L * 60 * 60) -> "${duration / (1000L * 60)}$mins"
        duration / (1000L * 60 * 60 * 2) -> hr
        duration / (1000L * 60 * 60 * 24) -> "${duration / (1000L * 60 * 60)}$hrs"
        else -> getTime(date1, date2)
    }
}