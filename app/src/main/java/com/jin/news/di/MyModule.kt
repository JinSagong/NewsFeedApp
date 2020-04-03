package com.jin.news.di

import com.jin.news.view.adapter.NewsAdapter
import com.jin.news.model.repository.Repository
import com.jin.news.model.repository.RepositoryImpl
import com.jin.news.model.rss.RssConverterFactory
import com.jin.news.model.rss.RssService
import com.jin.news.viewmodel.NewsViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.create

var retrofitPart = module {
    single<RssService> {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(RssConverterFactory.create())
            .baseUrl("https://news.google.com/")
            .build()
            .create()
    }
}

var modelPart = module { single<Repository> { RepositoryImpl(get()) } }

var viewModelPart = module { viewModel { NewsViewModel(get()) } }

var adapterPart = module { factory { NewsAdapter() } }

var myDiModule = listOf(retrofitPart, modelPart, viewModelPart, adapterPart)