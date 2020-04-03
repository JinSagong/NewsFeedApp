package com.jin.news.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jin.news.model.repository.Repository
import com.jin.news.model.og.OpenGraphCallback
import com.jin.news.model.og.OpenGraphManager
import com.jin.news.model.og.OpenGraphRunnable
import com.jin.news.model.rss.RssFeed
import com.jin.news.model.rss.RssItem
import com.jin.news.util.LOADING_TIME
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class NewsViewModel(private val repository: Repository) : ViewModel() {
    // 뷰모델 내부에서만 데이터 변경이 가능하고 뷰모델 외부에서는 Observing 만 가능하게 하기
    private val _responseLiveData = MutableLiveData<RssFeed>() // postValue 가능
    val responseLiveData: LiveData<RssFeed> get() = _responseLiveData // postValue 불가능

    // 한 번에 모든 Observable 을 해제하기 위해 compositeDisposable 을 이용함
    private val compositeDisposable = CompositeDisposable()

    private var ogManager: OpenGraphManager? = null

    fun getNews(query: String?) = compositeDisposable.add(
        repository.getData(query)
            .timeout(LOADING_TIME, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                Log.d(
                    this.javaClass.simpleName,
                    "[Rss Success] Last Build Date: ${data.lastBuildDate ?: ""}, Number of Feeds: ${data.items?.size ?: 0}"
                )
                _responseLiveData.postValue(data)
                getOpenGraph(data)
            }, {
                Log.d(this.javaClass.simpleName, "[Rss Failure] ${it.message}")
                _responseLiveData.postValue(RssFeed(null, null, null))
            })
    )

    private fun getOpenGraph(data: RssFeed) {
        val mCallback = object : OpenGraphCallback {
            // Open Graph Tag 가 없는 뉴스는 피드에서 삭제함
            override fun success(item: RssItem) {
                if (item.loaded && (item.link_description == null || item.link_url == null)) {
                    Log.d(
                        this@NewsViewModel.javaClass.simpleName,
                        "[Item Deleted - OG Null] ${item.title}"
                    )
                    data.items?.remove(item)
                }
                _responseLiveData.postValue(data)
            }

            // Open Graph Tag 를 불러오는 데 시간이 많이 소요되는 뉴스는 삭제함
            override fun failure(item: RssItem) {
                Log.d(
                    this@NewsViewModel.javaClass.simpleName,
                    "[Item Deleted - OG Timeout] ${item.title}"
                )
                data.items?.remove(item)
                _responseLiveData.postValue(data)
            }
        }

        // OpenGraphManager 를 이용해 처리중인 작업이 있으면 모두 종료하고 새로운 작업을 처리함
        ogManager?.resume()
        ogManager = OpenGraphManager()
        data.items?.forEach { ogManager?.start(OpenGraphRunnable(it, mCallback)) }
        ogManager?.close()
    }

    override fun onCleared() = compositeDisposable.clear().run { super.onCleared() }
}