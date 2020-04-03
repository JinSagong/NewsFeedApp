package com.jin.news.view.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jin.news.view.activity.DetailActivity
import com.jin.news.R
import com.jin.news.model.rss.RssFeed
import com.jin.news.model.rss.RssItem
import com.jin.news.util.LOADING_TIME
import kotlinx.android.synthetic.main.item_news.view.*
import splitties.activities.start

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ItemHolder>() {
    private val itemList = arrayListOf<RssItem>()
    private val loadeds = arrayListOf<Boolean>()
    private var millisCreated: Long? = null

    inner class ItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
    ) {
        fun onBind(item: RssItem) = with(itemView) {
            // 로딩이 완료될 때 까지 ProgressBar 를 표시함
            itemProgressBar.visibility = if (item.loaded) View.GONE else View.VISIBLE

            // 뉴스 제목, 뉴스 정보, 본문 내용 표시하기
            itemTitleTextView.text = item.title
            val info = "${item.source} · ${item.pubDate}"
            itemInfoTextView.text = info
            itemDescriptionTextView.text = item.link_description

            // 키워드 표시하기
            itemKeyword1TextView.run {
                visibility = if (item.keyword1 == null) View.INVISIBLE else View.VISIBLE
                text = item.keyword1
            }
            itemKeyword2TextView.run {
                visibility = if (item.keyword2 == null) View.INVISIBLE else View.VISIBLE
                text = item.keyword2
            }
            itemKeyword3TextView.run {
                visibility = if (item.keyword3 == null) View.INVISIBLE else View.VISIBLE
                text = item.keyword3
            }

            // 이미지 표시하기
            if (item.loaded) loadImage(item.link_image) else {
                itemView.itemImageView.visibility = View.VISIBLE
                itemImageView.setImageResource(R.drawable.ic_loading_96dp)
            }

            // DetailActivity 와 Intent 연결하기
            setOnClickListener {
                if (item.loaded) context.start<DetailActivity> {
                    putExtra("title", item.title)
                    putExtra("info", info)
                    putExtra("url", item.link_url)
                    putExtra("keyword1", item.keyword1)
                    putExtra("keyword2", item.keyword2)
                    putExtra("keyword3", item.keyword3)
                }
            }
        }

        private fun loadImage(url: String?) {
            Glide.with(itemView).load(url)
                .placeholder(R.drawable.ic_loading_96dp)
                .timeout(LOADING_TIME.toInt())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any?,
                        target: Target<Drawable>?, isFirstResource: Boolean
                    ): Boolean {
                        // 로드할 이미지가 없거나 로딩에 실패할 경우 이미지를 표시하지 않음
                        Log.d(
                            this@NewsAdapter.javaClass.simpleName, "[Glide Failure] ${e?.message}"
                        )
                        itemView.itemImageView.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?, model: Any?, target: Target<Drawable>?,
                        dataSource: DataSource?, isFirstResource: Boolean
                    ) = false
                })
                .transform(CenterCrop(), RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.dimens_half)))
                .into(itemView.itemImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemHolder(parent)

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ItemHolder, idx: Int) = holder.onBind(itemList[idx])

    fun updateItem(feed: RssFeed) = feed.items?.let {
        // 새로운 뉴스 피드를 목록을 불러왔을 때
        if (feed.millisCreated != millisCreated) {
            millisCreated = feed.millisCreated
            itemList.clear()
            itemList.addAll(it)
            loadeds.clear()
            it.forEach { item -> loadeds.add(item.loaded) }
            notifyDataSetChanged()

        } else {
            val loadedList = arrayListOf<Int>()
            it.forEachIndexed { idx, item -> if (item.loaded) loadedList.add(idx) }

            // 삭제해야 할 뉴스가 있을 때
            if (it.size != itemList.size) {
                var count = 0
                val removedList = arrayListOf<Int>()
                for (idx: Int in itemList.indices) {
                    if (idx - count >= it.size || itemList[idx].title != it[idx - count].title) {
                        count++
                        removedList.add(idx)
                    }
                }
                removedList.sortedDescending().forEach { idx ->
                    itemList.removeAt(idx)
                    loadeds.removeAt(idx)
                    notifyItemRemoved(idx)
                    notifyItemRangeChanged(idx, itemCount - idx)
                }
            }

            // 로딩이 완료된 뉴스만 업데이트 하기
            loadedList.forEach { idx -> if (!loadeds[idx]) notifyItemChanged(idx).run { loadeds[idx] = true } }
        }
    }
}