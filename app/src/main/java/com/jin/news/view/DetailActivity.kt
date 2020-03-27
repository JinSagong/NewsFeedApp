package com.jin.news.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.jin.news.R
import com.jin.news.util.LocaleWrapper

import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    private val title by lazy { intent.getStringExtra("title") }
    private val info by lazy { intent.getStringExtra("info") }
    private val url by lazy { intent.getStringExtra("url") }
    private val keyword1 by lazy { intent.getStringExtra("keyword1") }
    private val keyword2 by lazy { intent.getStringExtra("keyword2") }
    private val keyword3 by lazy { intent.getStringExtra("keyword3") }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        detailBackImageView.setOnClickListener { finish() }
        detailTitleTextView.text = title
        detailInfoTextView.text = info
        detailKeyword1TextView.run {
            visibility = if (keyword1 == null) View.GONE else View.VISIBLE
            text = keyword1
        }
        detailKeyword2TextView.run {
            visibility = if (keyword2 == null) View.GONE else View.VISIBLE
            text = keyword2
        }
        detailKeyword3TextView.run {
            visibility = if (keyword3 == null) View.GONE else View.VISIBLE
            text = keyword3
        }

        with(webView) {
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            webViewClient = MyWebViewClient()
            webChromeClient = MyWebChromeClient()
            loadUrl(this@DetailActivity.url)
        }
    }

    override fun onBackPressed() =
        with(webView) { if (canGoBack()) goBack() else super.onBackPressed() }

    inner class MyWebViewClient: WebViewClient()

    inner class MyWebChromeClient: WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            webViewProgressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
            webViewProgressBar.progress = newProgress
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleWrapper.wrap(newBase))
    }
}