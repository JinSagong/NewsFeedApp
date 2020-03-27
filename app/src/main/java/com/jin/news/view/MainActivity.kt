
package com.jin.news.view

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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jin.news.R
import com.jin.news.adapter.NewsAdapter
import com.jin.news.util.LocaleWrapper
import com.jin.news.util.Preferences
import com.jin.news.util.needSplash
import com.jin.news.viewmodel.NewsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.dialog_language.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import splitties.activities.start

class MainActivity : AppCompatActivity() {
    // private val layoutResourceId:Int get() = R.layout.activity_main

    private val mAdapter: NewsAdapter by inject()
    private val mViewModel: NewsViewModel by viewModel()
    private val languageDialog by lazy { BottomSheetDialog(this) }

    private val splashTime = 1300L
    private var splashFlag = true
    private var refreshFlag = false
    private var changeLanguageFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)

        // val viewDataBinding: ViewDataBinding = DataBindingUtil.setContentView(this, layoutResourceId)

        showSplashScreen()

        initStartView()
        initDataBinding()
        initRefresh()
        setFab()
        setLanguageDialog()

        refresh()
    }

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
                splashFlag = false
                needSplash = false
            }, splashTime)
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
        val lastUpdate = "${getString(R.string.last_update)}${it.lastBuildDate}"
        mainInfoTextView.text = lastUpdate
        mAdapter.updateItem(it)
        if (refreshFlag) {
            refreshFlag = false
            mainSwipeRefreshLayout.isRefreshing = false
            mainRecyclerView.smoothScrollToPosition(0)
        }
    })

    private fun initRefresh() = with(mainSwipeRefreshLayout) {
        setColorSchemeColors(ContextCompat.getColor(context, R.color.colorTheme))
        setOnRefreshListener { refresh() }
    }

    private fun refresh() {
        refreshFlag = true
        if (!mainSwipeRefreshLayout.isRefreshing) mainSwipeRefreshLayout.isRefreshing = true
        mViewModel.getNews(null)
    }

    // 맨 위로 스크롤하기 기능 만들기
    private fun setFab() {
        mainTopFab.setOnClickListener { mainRecyclerView.smoothScrollToPosition(0) }
        mainRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val position =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                if (dy >= 0 || position == 0) mainTopFab.hide() else mainTopFab.show()
            }
        })
    }

    // 언어 선택 기능 만들기
    private fun setLanguageDialog() {
        with(languageDialog) {
            setContentView(R.layout.dialog_language)
            behavior.skipCollapsed = true

            setOnShowListener {
                languageRadioGroup.check(Preferences.languageId)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            languageCancelTextView.setOnClickListener { languageDialog.dismiss() }

            languageDoneTextView.setOnClickListener {
                val checked = languageDialog.languageRadioGroup.checkedRadioButtonId
                if (checked != Preferences.languageId) {
                    changeLanguageFlag = true
                    Preferences.languageId = checked
                    start<MainActivity>()
                    finish()
                    overridePendingTransition(0, 0)
                }
                languageDialog.dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!changeLanguageFlag) needSplash = true
    }

    // 스플래시 화면이 표시될 동안 뒤로가기 기능 비활성화하기
    override fun onBackPressed() = if (splashFlag) Unit else super.onBackPressed()

    override fun onCreateOptionsMenu(menu: Menu) =
        menuInflater.inflate(R.menu.menu_main, menu).run { true }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_search -> start<SearchActivity>().run { true }
            R.id.action_language -> languageDialog.show().run { true }
            else -> super.onOptionsItemSelected(item)
        }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleWrapper.wrap(newBase))
    }
}