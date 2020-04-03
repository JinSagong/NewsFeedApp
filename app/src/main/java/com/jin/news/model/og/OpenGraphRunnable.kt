package com.jin.news.model.og

import android.os.Process
import com.jin.news.util.KeywordGenerator
import com.jin.news.model.rss.RssItem
import com.jin.news.util.LOADING_TIME
import org.jsoup.Jsoup
import java.lang.Exception
import java.net.URL

class OpenGraphRunnable(private val item: RssItem, private val mCallback: OpenGraphCallback) :
    Runnable {
    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        try {
            val head = Jsoup.parse(URL(item.link), LOADING_TIME.toInt()).head().getElementsByTag("meta")
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
                            .replace("\n", " ")
                            .replace("\t", " ")
                        item.link_description = description
                        val keywords = KeywordGenerator.getKeywords(description)
                        item.keyword1 = keywords[0]
                        item.keyword2 = keywords[1]
                        item.keyword3 = keywords[2]
                    }
                }
            }
            item.loaded = true
            mCallback.success(item)
        } catch (e: Exception) {
            mCallback.failure(item)
        }
    }
}