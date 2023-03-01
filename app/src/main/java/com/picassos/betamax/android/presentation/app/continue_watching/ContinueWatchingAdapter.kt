package com.picassos.betamax.android.presentation.app.continue_watching

import androidx.recyclerview.widget.RecyclerView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.domain.listener.OnContinueWatchingClickListener
import com.picassos.betamax.android.domain.listener.OnContinueWatchingOptionsClickListener
import com.picassos.betamax.android.domain.model.ContinueWatching

class ContinueWatchingAdapter(private val listener: OnContinueWatchingClickListener, private val optionsListener: OnContinueWatchingOptionsClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class ContinueWatchingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.continue_watching_thumbnail)
        private val moreOptions: ImageView = itemView.findViewById(R.id.continue_watching_more_options)

        fun setData(data: ContinueWatching.ContinueWatching) {
            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(data.thumbnail)
                .build()
        }

        fun bind(item: ContinueWatching.ContinueWatching, listener: OnContinueWatchingClickListener) {
            itemView.setOnClickListener {
                listener.onItemClick(item)
            }
        }

        fun bindOptions(item: ContinueWatching.ContinueWatching, listener: OnContinueWatchingOptionsClickListener) {
            itemView.setOnLongClickListener {
                listener.onOptionsClick(item)
                return@setOnLongClickListener true
            }
            moreOptions.setOnClickListener {
                listener.onOptionsClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_continue_watching, parent, false)
        return ContinueWatchingHolder(view)
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<ContinueWatching.ContinueWatching>() {
        override fun areItemsTheSame(oldItem: ContinueWatching.ContinueWatching, newItem: ContinueWatching.ContinueWatching): Boolean {
           return oldItem.id == newItem.id
               && oldItem.url == newItem.url
               && oldItem.thumbnail == newItem.thumbnail
               && oldItem.currentPosition == newItem.currentPosition
        }

        override fun areContentsTheSame(oldItem: ContinueWatching.ContinueWatching, newItem: ContinueWatching.ContinueWatching): Boolean {
            return oldItem.id == newItem.id
                && oldItem.url == newItem.url
                && oldItem.thumbnail == newItem.thumbnail
                && oldItem.currentPosition == newItem.currentPosition
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val movies = differ.currentList[position]
        val continueWatchingHolder = holder as ContinueWatchingHolder
        continueWatchingHolder.setData(movies)
        continueWatchingHolder.bind(movies, listener)
        continueWatchingHolder.bindOptions(movies, optionsListener)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}