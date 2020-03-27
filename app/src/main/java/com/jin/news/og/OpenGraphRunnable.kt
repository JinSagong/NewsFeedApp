package com.jin.news.og

import android.os.Process
import com.jin.news.rss.RssItem
import com.jin.news.util.specialCharSequence
import org.jsoup.Jsoup
import java.lang.Exception
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class OpenGraphRunnable(
    private val item: RssItem,
    private val mCallback: OpenGraphCallback
) : Runnable {
    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        try {
            val head =
                Jsoup.parse(URL(item.link), 10000).head().getElementsByTag("meta")
            head.forEach { element ->
                when (element.attr("property")) {
                    "og:url" -> item.link_url = element.attr("content")
                    "og:image" -> item.link_image = element.attr("content")
                    "og:description" -> {
                        val description = element.attr("content")
                            .replace("&nbsp;", " ")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&amp;", "&")
                            .replace("&quot;", "\"")
                            .replace("&#35;", "#")
                            .replace("&#39;", "'")
                            .replace("&#035;", "#")
                            .replace("&#039;", "'")
                        item.link_description = description
                        val keywords = getKeywords(description)
                        item.keyword1 = keywords[0]
                        item.keyword2 = keywords[1]
                        item.keyword3 = keywords[2]
                    }
                }
            }
            item.loaded = true
            mCallback.success()
        } catch (e: Exception) {
            mCallback.failure()
        }
    }

    private fun getKeywords(description: String): ArrayList<String?> {
        val result = arrayListOf<String?>()
        val st = StringTokenizer(description, specialCharSequence)
        val map = TreeMap<String, Int>()
        while (st.hasMoreTokens()) {
            st.nextToken()?.let { if (it.length >= 2) map[it] = (map[it] ?: 0) + 1 }
        }
        for (i: Int in 0..2) {
            map.maxBy { it.value }?.let {
                result.add(it.key)
                map.entries.remove(it)
            } ?: result.add(null)
        }
        return result
    }
}