package com.picassos.betamax.android.presentation.television.episode.episodes

import android.graphics.drawable.Animatable
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.listener.OnEpisodeClickListener

class TelevisionEpisodesAdapter(private val onClickListener: OnEpisodeClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class EpisodesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.episode_thumbnail)
        private val title: TextView = itemView.findViewById(R.id.episode_title)
        private val duration: TextView = itemView.findViewById(R.id.episode_duration)
        private val watchProgress: ProgressBar = itemView.findViewById(R.id.episode_progress)

        fun setData(episode: Episodes.Episode) {
            title.text = episode.title
            duration.text = Helper.convertMinutesToHoursAndMinutes(episode.duration)
            watchProgress.max = episode.duration * 60 * 1000
            watchProgress.progress = episode.currentPosition ?: 0

            val imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(episode.thumbnail))
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

        fun bind(item: Episodes.Episode?, onClickListener: OnEpisodeClickListener) {
            itemView.setOnClickListener {  onClickListener.onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_television_episode_horizontal, parent, false)
        return EpisodesHolder(view)
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Episodes.Episode>() {
        override fun areItemsTheSame(oldItem: Episodes.Episode, newItem: Episodes.Episode): Boolean {
           return oldItem.id == newItem.id
               && oldItem.episodeId == newItem.episodeId
               && oldItem.title == newItem.title
               && oldItem.duration == newItem.duration
               && oldItem.currentPosition == newItem.currentPosition
        }

        override fun areContentsTheSame(oldItem: Episodes.Episode, newItem: Episodes.Episode): Boolean {
            return oldItem.id == newItem.id
                && oldItem.episodeId == newItem.episodeId
                && oldItem.title == newItem.title
                && oldItem.duration == newItem.duration
                && oldItem.currentPosition == newItem.currentPosition
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val episodes = differ.currentList[position]
        (holder as EpisodesHolder).apply {
            setData(episodes)
            bind(episodes, onClickListener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}