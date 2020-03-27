package com.jin.news.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jin.news.model.Repository
import com.jin.news.og.OpenGraphCallback
import com.jin.news.og.OpenGraphManager
import com.jin.news.og.OpenGraphRunnable
import com.jin.news.rss.RssFeed
import com.jin.news.util.Preferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class NewsViewModel(private val repository: Repository) : ViewModel() {
    // 뷰모델 내부에서만 데이터 변경이 가능하고 뷰모델 외부에서는 Observing 만 가능하게 하기
    private val _responseLiveData = MutableLiveData<RssFeed>() // postValue 가능
    val responseLiveData: LiveData<RssFeed> get() = _responseLiveData // postValue 불가능

    private val compositeDisposable = CompositeDisposable()

    private var ogManager: OpenGraphManager? = null

    private fun addDisposable(disposable: Disposable) = compositeDisposable.add(disposable)

    fun getNews(query: String?) = addDisposable(
        repository.getData(Preferences.getCountryAndLanguage(), query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                _responseLiveData.postValue(data)
                val mCallback = object : OpenGraphCallback {
                    override fun success() = _responseLiveData.postValue(data)
                    override fun failure() = Unit
                }
                ogManager?.resume()
                ogManager = OpenGraphManager()
                data.items?.forEach { ogManager?.start(OpenGraphRunnable(it, mCallback)) }
                ogManager?.close()

                Log.d(
                    this.javaClass.simpleName,
                    "[Response Success] Last Build Date: ${data.lastBuildDate ?: ""}" +
                            ", Number of Feeds: ${data.items?.size ?: 0}"
                )
            }, {
                Log.d(this.javaClass.simpleName, "[Response Failure] ${it.message}")
            })
    )

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}