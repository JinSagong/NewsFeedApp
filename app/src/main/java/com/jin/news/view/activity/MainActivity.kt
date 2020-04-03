package com.jin.news.view.activity

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jin.news.R
import com.jin.news.view.adapter.NewsAdapter
import com.jin.news.view.dialog.DialogCallback
import com.jin.news.view.dialog.LanguageDialog
import com.jin.news.util.LocaleWrapper
import com.jin.news.util.SPLASH_TIME
import com.jin.news.util.needSplash
import com.jin.news.viewmodel.NewsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import splitties.activities.start

class MainActivity : AppCompatActivity() {
    private val mAdapter: NewsAdapter by inject()
    private val mViewModel: NewsViewModel by viewModel()

    private val languageDialog by lazy { LanguageDialog(this) }

    private var splashFlag = true
    private var changeLanguageFlag = false
    private var errorFlag = false

    private var millisCreated: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)

        showSplashScreen()

        initStartView()
        initDataBinding()
        initRefresh()
        setFab()
        setLanguageDialog()

        refresh()
    }

    // Activity 나 Fragment 를 활용한 Splash Screen 보다 더 자연스럽게 구현 가능함
    private fun showSplashScreen() {
        if (needSplash) {
            // 스플래시 화면을 위해 액션바 숨기기
            supportActionBar?.hide()

            // 스플래시 화면에 버전정보 표시하기
            try {
                val versionName = "v${packageManager.getPackageInfo(packageName, 0).versionName}"
                splashVersionTextView.text = versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            // 일정시간 이후 스플래시 화면 종료하기
            Handler().postDelayed({
                supportActionBar?.show()
                mainSwipeRefreshLayout.visibility = View.VISIBLE
                mainSplashLayout.visibility = View.GONE
                if (errorFlag) mainDescriptionTextView.visibility = View.VISIBLE
                splashFlag = false
                needSplash = false
            }, SPLASH_TIME)

        } else {
            mainSwipeRefreshLayout.visibility = View.VISIBLE
            mainSplashLayout.visibility = View.GONE
            splashFlag = false
        }
    }

    private fun initStartView() = with(mainRecyclerView) {
        adapter = mAdapter
        layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        setHasFixedSize(true)
    }

    private fun initDataBinding() = mViewModel.responseLiveData.observe(this, Observer {
        if (it.millisCreated == null) {
            // 뉴스 목록을 불러올 수 없음을 나타냄
            if (splashFlag) errorFlag = true else mainDescriptionTextView.visibility = View.VISIBLE
            mainRecyclerView.visibility = View.GONE
        } else {
            // 뉴스 목록을 표시함
            errorFlag = false
            val lastUpdate = "${getString(R.string.last_update)}${it.lastBuildDate}"
            mainInfoTextView.text = lastUpdate
            mainDescriptionTextView.visibility = View.GONE
            mainRecyclerView.visibility = View.VISIBLE
            mAdapter.updateItem(it)
        }

        if (it.millisCreated == null || millisCreated != it.millisCreated) {
            millisCreated = it.millisCreated
            mainSwipeRefreshLayout.isRefreshing = false
            mainRecyclerView.smoothScrollToPosition(0)
        }
    })

    private fun initRefresh() = with(mainSwipeRefreshLayout) {
        setColorSchemeColors(ContextCompat.getColor(context, R.color.colorTheme))
        setOnRefreshListener { refresh() }
    }

    private fun refresh() {
        if (!mainSwipeRefreshLayout.isRefreshing) mainSwipeRefreshLayout.isRefreshing = true
        mViewModel.getNews(null)
    }

    // 맨 위로 스크롤하기 기능 만들기
    private fun setFab() {
        mainTopFab.setOnClickListener { mainRecyclerView.smoothScrollToPosition(0) }
        mainRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                if (dy >= 0 || position == 0) mainTopFab.hide() else mainTopFab.show()
            }
        })
    }

    // 언어 선택 기능 만들기
    private fun setLanguageDialog() {
        languageDialog.setDialog(object : DialogCallback {
            override fun onDonePressed() {
                changeLanguageFlag = true
                start<MainActivity>()
                finish()
                overridePendingTransition(0, 0)
            }
        })
    }

    // 언어 변경으로 인해 초기화 할 때는 스플래시 화면을 표시하지 않도록 하기
    override fun onDestroy() = super.onDestroy().run { if (!changeLanguageFlag) needSplash = true }

    // 스플래시 화면이 표시될 동안 뒤로가기 기능 비활성화 하기
    override fun onBackPressed() = if (splashFlag) Unit else super.onBackPressed()

    override fun onCreateOptionsMenu(menu: Menu) =
        menuInflater.inflate(R.menu.menu_main, menu).run { true }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_search -> start<SearchActivity>().run { true }
            R.id.action_language -> languageDialog.show().run { true }
            else -> super.onOptionsItemSelected(item)
        }

    // 설정한 언어를 MainActivity 에 적용하기
    override fun attachBaseContext(newBase: Context?) = super.attachBaseContext(LocaleWrapper.wrap(newBase))
}