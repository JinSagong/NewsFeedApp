package com.jin.news.view.activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jin.news.R
import com.jin.news.view.adapter.NewsAdapter
import com.jin.news.util.LocaleWrapper
import com.jin.news.viewmodel.NewsViewModel
import kotlinx.android.synthetic.main.activity_search.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {
    private val mAdapter: NewsAdapter by inject()
    private val mViewModel: NewsViewModel by viewModel()

    private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private var onSearch = true
    private var onEmptyQuery = true

    private var millisCreated: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        overridePendingTransition(0, 0)

        initStartView()
        initDataBinding()
        initRefresh()
        setFab()
        setListeners()
    }

    private fun initStartView() = with(searchRecyclerView) {
        adapter = mAdapter
        layoutManager = LinearLayoutManager(this@SearchActivity, RecyclerView.VERTICAL, false)
        setHasFixedSize(true)
    }

    private fun initDataBinding() = mViewModel.responseLiveData.observe(this, Observer {
        when {
            it.millisCreated == null -> {
                // 검색 결과를 불러올 수 없음을 나타냄
                searchRecyclerView.visibility = View.GONE
                searchDescriptionTextView.text = getString(R.string.error)
                searchDescriptionTextView.visibility = View.VISIBLE
            }
            it.items?.size == 0 -> {
                // 검색 결과가 없음을 나타냄
                searchSwipeRefreshLayout.visibility = View.GONE
                searchDescriptionTextView.text = getString(R.string.search_no_result)
                searchDescriptionTextView.visibility = View.VISIBLE
            }
            else -> {
                // 검색 결과를 표시함
                searchRecyclerView.visibility = View.VISIBLE
                searchDescriptionTextView.visibility = View.GONE
                mAdapter.updateItem(it)
            }
        }

        if (it.millisCreated == null || millisCreated != it.millisCreated) {
            millisCreated = it.millisCreated
            searchSwipeRefreshLayout.isRefreshing = false
            searchRecyclerView.smoothScrollToPosition(0)
        }
    })

    private fun initRefresh() = with(searchSwipeRefreshLayout) {
        setColorSchemeColors(ContextCompat.getColor(context, R.color.colorTheme))
        setOnRefreshListener { refresh(false) }
    }

    private fun refresh(touched: Boolean) {
        if (!onEmptyQuery && ((touched && !searchSwipeRefreshLayout.isRefreshing) || !touched)) {
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            if (!searchSwipeRefreshLayout.isRefreshing) searchSwipeRefreshLayout.isRefreshing = true
            searchSwipeRefreshLayout.visibility = View.VISIBLE
            mViewModel.getNews(searchEditText.text.toString())
        }
    }

    // 맨 위로 스크롤하기 기능 만들기
    private fun setFab() {
        searchTopFab.setOnClickListener { searchRecyclerView.smoothScrollToPosition(0) }
        searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                if (dy >= 0 || position == 0) searchTopFab.hide() else searchTopFab.show()
            }
        })
    }

    private fun setListeners() {
        searchBackImageView.setOnClickListener { if (onEmptyQuery) onBackPressed() else searchEditText.setText("") }
        searchEditText.addTextChangedListener {
            if (it.isNullOrEmpty()) searchBackImageView.setImageResource(R.drawable.ic_back_24dp).run { onEmptyQuery = true }
            else searchBackImageView.setImageResource(R.drawable.ic_close_20dp).run { onEmptyQuery = false }
        }
        searchEditText.setOnEditorActionListener { _, _, _ -> refresh(true).run { true } }
        searchImageView.setOnClickListener { refresh(true) }
    }

    override fun onPause() {
        if (!onSearch) overridePendingTransition(0, 0)
        super.onPause()
    }

    override fun onBackPressed() = super.onBackPressed().run { onSearch = false }

    // 설정한 언어를 SearchActivity 에 적용하기
    override fun attachBaseContext(newBase: Context?) = super.attachBaseContext(LocaleWrapper.wrap(newBase))
}