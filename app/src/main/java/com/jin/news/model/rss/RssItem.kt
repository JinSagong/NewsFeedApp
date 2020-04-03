package com.jin.news.model.rss

import java.io.Serializable

data class RssItem(
    var title: String? = null,
    var link: String? = null,
    var link_url: String? = null,
    var link_image: String? = null,
    var link_description: String? = null,
    var pubDate: String? = null,
    var source: String? = null,
    var keyword1: String? = null,
    var keyword2: String? = null,
    var keyword3: String? = null,
    var loaded: Boolean = false
) : Serializable {

    override fun toString() = StringBuilder().run {
        title?.let { append("title: $it\n") }
        link?.let { append("link: $it\n") }
        link_url?.let { append("link_url: $it\n") }
        link_image?.let { append("link_image: $it\n") }
        link_description?.let { append("link_description: $it\n") }
        pubDate?.let { append("pubDate: $it\n") }
        source?.let { append("source: $it\n") }
        keyword1?.let { append("keyword1: $it\n") }
        keyword2?.let { append("keyword2: $it\n") }
        keyword3?.let { append("keyword3: $it\n") }
        append("loaded: $loaded")
        toString()
    }
}