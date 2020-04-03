package com.jin.news

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jin.news.di.myDiModule
import com.jin.news.util.LanguagePreferences
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.jin.news.MyApplication.Companion.languagePreferences
import com.jin.news.util.SPLASH_TIME
import com.jin.news.viewmodel.NewsViewModel
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(MockitoJUnitRunner::class)
class NewsViewModelTest : KoinTest {
    private val viewModel: NewsViewModel by inject()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testRule = RxSchedulerRule()

    @Mock
    private lateinit var mContext: Context

    @Mock
    private lateinit var mPreferences: SharedPreferences

    @Before
    fun setUp() {
        startKoin(myDiModule)
        `when`(mContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mPreferences)
        languagePreferences = LanguagePreferences(mContext)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // Mock 을 활용한 SharedPreferences 테스트
    @Test
    fun languagePreferencesTest() {
        // given
        val value = R.id.languageRadioButton2

        // when
        `when`(languagePreferences.languageId).thenReturn(value)

        // then
        assertEquals(languagePreferences.getCountryAndLanguage(), "KR:ko")
    }

    // Koin 을 활용한 ViewModel 테스트
    @Test
    fun viewModelTest() {
        // given
        val query = null

        // when
        viewModel.getNews(query)

        // then
        val result = viewModel.responseLiveData.getOrAwaitValue()
        assertNotNull(result)
        println(println("value = ${result.toString()}"))
    }

    private fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = SPLASH_TIME,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T? {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data = o
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        this.observeForever(observer)
        if (!latch.await(time, timeUnit)) throw TimeoutException("LiveData value was never set.")
        return data
    }

    inner class RxSchedulerRule : TestRule {
        override fun apply(base: Statement?, description: Description?) =
            object : Statement() {
                @Throws(Throwable::class)
                override fun evaluate() {
                    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
                    RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
                    RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
                    RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
                    try {
                        base?.evaluate()
                    } finally {
                        RxJavaPlugins.reset()
                        RxAndroidPlugins.reset()
                    }
                }
            }
    }
}