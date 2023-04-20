package com.picassos.betamax.android.presentation.television.continue_watching

import androidx.recyclerview.widget.RecyclerView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.domain.listener.OnContinueWatchingClickListener
import com.picassos.betamax.android.domain.listener.OnContinueWatchingLongClickListener
import com.picassos.betamax.android.domain.model.ContinueWatching

class TelevisionContinueWatchingAdapter(private val onClickListener: OnContinueWatchingClickListener, private val onLongClickListener: OnContinueWatchingLongClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class ContinueWatchingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.continue_watching_thumbnail)
        val progress: ProgressBar = itemView.findViewById(R.id.continue_watching_progress)

        fun setData(continueWatching: ContinueWatching.ContinueWatching) {
            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(continueWatching.thumbnail)
                .build()
            progress.apply {
                max = continueWatching.duration
                progress = continueWatching.currentPosition
            }
        }

        fun bind(item: ContinueWatching.ContinueWatching, onClickListener: OnContinueWatchingClickListener, onLongClickListener: OnContinueWatchingLongClickListener) {
            itemView.apply {
                setOnClickListener {
                    onClickListener.onItemClick(item)
                }
                setOnLongClickListener {
                    onLongClickListener.onItemLongClick(item)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_television_continue_watching, parent, false)
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
        (holder as ContinueWatchingHolder).apply {
            setData(movies)
            bind(movies, onClickListener, onLongClickListener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}