package com.picassos.betamax.android.presentation.television.tvchannel.tvchannels

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener
import com.picassos.betamax.android.domain.listener.OnTvChannelLongClickListener
import com.picassos.betamax.android.domain.model.TvChannels

class TelevisionTvChannelsAdapter(private var selectedPosition: Int = RecyclerView.NO_POSITION, private val onClickListener: OnTvChannelClickListener, private val onLongClickListener: OnTvChannelLongClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class TvChannelsHolder(itemView: View, private val adapter: TelevisionTvChannelsAdapter) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.tvchannel_container)
        val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.tvchannel_thumbnail)
        val title: TextView = itemView.findViewById(R.id.tvchannel_title)
        private val playing: CardView = itemView.findViewById(R.id.tvchannel_playing)

        fun setData(data: TvChannels.TvChannel) {
            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(data.banner)
                .build()
            title.text = data.title
        }

        fun bind(item: TvChannels.TvChannel, onClickListener: OnTvChannelClickListener, onLongClickListener: OnTvChannelLongClickListener) {
            playing.isVisible = absoluteAdapterPosition == adapter.selectedPosition

            itemView.apply {
                setOnClickListener {
                    val oldPosition = adapter.selectedPosition
                    adapter.apply {
                        selectedPosition = absoluteAdapterPosition
                        notifyItemChanged(oldPosition)
                        notifyItemChanged(selectedPosition)
                    }
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_television_tvchannel, parent, false)
        return TvChannelsHolder(view, this)
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<TvChannels.TvChannel>() {
        override fun areItemsTheSame(oldItem: TvChannels.TvChannel, newItem: TvChannels.TvChannel): Boolean {
           return oldItem.id == newItem.id
               && oldItem.title == newItem.title
               && oldItem.banner == newItem.banner
        }

        override fun areContentsTheSame(oldItem: TvChannels.TvChannel, newItem: TvChannels.TvChannel): Boolean {
            return oldItem.id == newItem.id
                && oldItem.title == newItem.title
                && oldItem.banner == newItem.banner
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tvChannels = differ.currentList[position]
        (holder as TvChannelsHolder).apply {
            setData(tvChannels)
            bind(tvChannels, onClickListener, onLongClickListener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}