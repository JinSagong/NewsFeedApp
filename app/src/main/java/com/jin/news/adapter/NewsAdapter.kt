package com.jin.news.adapter

import android.graphics.drawable.Drawable
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
import com.jin.news.view.DetailActivity
import com.jin.news.R
import com.jin.news.rss.RssFeed
import com.jin.news.rss.RssItem
import kotlinx.android.synthetic.main.item_news.view.*
import splitties.activities.start

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ItemHolder>() {
    private val itemList = arrayListOf<RssItem>()

    inner class ItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
    ) {
        fun onBind(item: RssItem) = with(itemView) {
            itemProgressBar.visibility = if (item.loaded) View.GONE else View.VISIBLE
            itemKeywordScrollView.visibility = if (item.loaded) View.VISIBLE else View.INVISIBLE
            itemTitleTextView.text = item.title
            val info = "${item.source} · ${item.pubDate}"
            itemInfoTextView.text = info
            itemDescriptionTextView.text = item.link_description
            if (item.loaded) {
                itemKeyword1TextView.run {
                    visibility = if (item.keyword1 == null) View.GONE else View.VISIBLE
                    text = item.keyword1
                }
                itemKeyword2TextView.run {
                    visibility = if (item.keyword2 == null) View.GONE else View.VISIBLE
                    text = item.keyword2
                }
                itemKeyword3TextView.run {
                    visibility = if (item.keyword3 == null) View.GONE else View.VISIBLE
                    text = item.keyword3
                }
            }
            if (item.loaded) loadImage(item.link_image) else {
                itemView.itemImageView.visibility = View.VISIBLE
                itemImageView.setImageResource(R.drawable.ic_loading_96dp)
            }
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
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any?,
                        target: Target<Drawable>?, isFirstResource: Boolean
                    ): Boolean {
                        itemView.itemImageView.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?, model: Any?, target: Target<Drawable>?,
                        dataSource: DataSource?, isFirstResource: Boolean
                    ) = false
                })
                .transform(
                    CenterCrop(),
                    RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.dimens_half))
                )
                .into(itemView.itemImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemHolder(parent)

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ItemHolder, idx: Int) = holder.onBind(itemList[idx])

    fun updateItem(feed: RssFeed) = feed.items?.let {
        itemList.clear()
        itemList.addAll(it)
        notifyDataSetChanged()
    }
}