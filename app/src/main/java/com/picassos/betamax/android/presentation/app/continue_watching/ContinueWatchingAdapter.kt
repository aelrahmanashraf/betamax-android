package com.picassos.betamax.android.presentation.app.continue_watching

import android.graphics.drawable.Animatable
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.picassos.betamax.android.domain.listener.OnContinueWatchingClickListener
import com.picassos.betamax.android.domain.listener.OnContinueWatchingOptionsClickListener
import com.picassos.betamax.android.domain.model.ContinueWatching

class ContinueWatchingAdapter(private val onClickListener: OnContinueWatchingClickListener, private val optionsListener: OnContinueWatchingOptionsClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class ContinueWatchingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.continue_watching_thumbnail)
        private val moreOptions: ImageView = itemView.findViewById(R.id.continue_watching_more_options)
        private val progress: ProgressBar = itemView.findViewById(R.id.continue_watching_progress)

        fun setData(continueWatching: ContinueWatching.ContinueWatching) {
            progress.apply {
                max = continueWatching.duration
                progress = continueWatching.currentPosition
            }

            val imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(continueWatching.thumbnail))
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .setProgressiveRenderingEnabled(true)
                .build()
            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(thumbnail.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        imageRequest.sourceUri?.let { Fresco.getImagePipeline().evictFromMemoryCache(it) }
                    }
                })
                .build()
        }

        fun bind(item: ContinueWatching.ContinueWatching, onClickListener: OnContinueWatchingClickListener) {
            itemView.setOnClickListener {
                onClickListener.onItemClick(item)
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
        (holder as ContinueWatchingHolder).apply {
            setData(movies)
            bind(movies, onClickListener)
            bindOptions(movies, optionsListener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}