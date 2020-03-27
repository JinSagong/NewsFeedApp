package com.jin.news.view

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
import com.jin.news.adapter.NewsAdapter
import com.jin.news.util.LocaleWrapper
import com.jin.news.viewmodel.NewsViewModel
import kotlinx.android.synthetic.main.activity_search.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {
    private val mAdapter: NewsAdapter by inject()
    private val mViewModel: NewsViewModel by viewModel()

    private var refreshFlag = false

    private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private var onSearch = true
    private var onEmptyQuery = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        overridePendingTransition(0, 0)

        initStartView()
        initDataBinding()
        initRefresh()
        setFab()

        searchBackImageView.setOnClickListener {
            if (onEmptyQuery) onBackPressed() else searchEditText.setText("")
        }
        searchEditText.addTextChangedListener { notifyQueryChanged(it) }
        searchEditText.setOnEditorActionListener { _, _, _ ->
            refresh()
            true
        }
        searchImageView.setOnClickListener { refresh() }
    }

    private fun initStartView() = with(searchRecyclerView) {
        adapter = mAdapter
        layoutManager = LinearLayoutManager(this@SearchActivity, RecyclerView.VERTICAL, false)
        setHasFixedSize(true)
    }

    private fun initDataBinding() = mViewModel.responseLiveData.observe(this, Observer {
        if (it.items?.size == 0) {
            // 검색 결과가 없음을 나타냅니다.
            searchSwipeRefreshLayout.visibility = View.GONE
            searchDescriptionTextView.text = getString(R.string.search_no_result)
            searchDescriptionTextView.visibility = View.VISIBLE
        } else {
            // 검색 결과를 표시합니다.
            searchSwipeRefreshLayout.visibility = View.VISIBLE
            searchDescriptionTextView.visibility = View.GONE
            mAdapter.updateItem(it)
        }
        if (refreshFlag) {
            refreshFlag = false
            searchSwipeRefreshLayout.isRefreshing = false
            searchRecyclerView.smoothScrollToPosition(0)
        }
    })

    private fun initRefresh() = with(searchSwipeRefreshLayout) {
        setColorSchemeColors(ContextCompat.getColor(context, R.color.colorTheme))
        setOnRefreshListener { refresh() }
    }

    private fun refresh() {
        if (!onEmptyQuery && !refreshFlag) {
            refreshFlag = true
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            if (!searchSwipeRefreshLayout.isRefreshing) searchSwipeRefreshLayout.isRefreshing = true
            mViewModel.getNews(searchEditText.text.toString())
        }
    }

    // 맨 위로 스크롤하기 기능 만들기
    private fun setFab() {
        searchTopFab.setOnClickListener { searchRecyclerView.smoothScrollToPosition(0) }
        searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val position =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                if (dy >= 0 || position == 0) searchTopFab.hide() else searchTopFab.show()
            }
        })
    }

    private fun notifyQueryChanged(query: Editable?) {
        if (query.isNullOrEmpty()) {
            onEmptyQuery = true
            searchBackImageView.setImageResource(R.drawable.ic_back_24dp)
        } else {
            onEmptyQuery = false
            searchBackImageView.setImageResource(R.drawable.ic_close_20dp)
        }
    }

    override fun onPause() {
        if (!onSearch) overridePendingTransition(0, 0)
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onSearch = false
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleWrapper.wrap(newBase))
    }
}