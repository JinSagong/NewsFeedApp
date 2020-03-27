package com.jin.news.rss

import com.jin.news.util.TimeFormat
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

class RssParser : DefaultHandler() {
    var lastBuildDate = ""
    val items = arrayListOf<RssItem>()

    private var parsingLastBuildDate = false
    private var parsingTitle = false
    private var parsingLink = false
    private var parsingPubDate = false

    private var title = ""
    private var link = ""
    private var pubDate = ""
    private var source = ""

    private var titleAndSource = ""

    @Throws(SAXException::class)
    override fun startElement(
        uri: String?, localName: String?, qName: String?, attributes: Attributes?
    ) {
        when (localName) {
            "lastBuildDate" -> {
                parsingLastBuildDate = true
                lastBuildDate = ""
            }
            "title" -> {
                parsingTitle = true
                titleAndSource = ""
            }
            "link" -> {
                parsingLink = true
                link = ""
            }
            "pubDate" -> {
                parsingPubDate = true
                pubDate = ""
            }
        }
        super.startElement(uri, localName, qName, attributes)
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String?, localName: String?, qName: String?) {
        when (localName) {
            "lastBuildDate" -> parsingLastBuildDate = false
            "item" -> {
                items.add(RssItem().apply {
                    this.title = this@RssParser.title
                    this.link = this@RssParser.link
                    this.pubDate = TimeFormat.getDuration(lastBuildDate, this@RssParser.pubDate)
                    this.source = this@RssParser.source
                })
            }
            "title" -> {
                parsingTitle = false
                titleAndSource.split(" - ").let {
                    title = it[0]
                    source = it[1]
                }
            }
            "link" -> parsingLink = false
            "pubDate" -> parsingPubDate = false
        }
        super.endElement(uri, localName, qName)
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray?, start: Int, length: Int) {
        val buff = ch?.let { String(ch, start, length) } ?: ""
        when {
            parsingLastBuildDate -> lastBuildDate += buff
            parsingLink -> link += buff
            parsingPubDate -> pubDate += buff
            parsingTitle -> titleAndSource += buff
        }
    }
}