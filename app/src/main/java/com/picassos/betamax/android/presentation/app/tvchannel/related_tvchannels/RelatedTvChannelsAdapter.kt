package com.picassos.betamax.android.presentation.app.tvchannel.related_tvchannels

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener
import com.picassos.betamax.android.domain.model.TvChannels

class RelatedTvChannelsAdapter(private var selectedPosition: Int = RecyclerView.NO_POSITION, private val onClickListener: OnTvChannelClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class TvChannelsHolder(itemView: View, private val adapter: RelatedTvChannelsAdapter) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.tvchannel_container)
        val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.tvchannel_thumbnail)
        val title: TextView = itemView.findViewById(R.id.tvchannel_title)

        fun setData(data: TvChannels.TvChannel) {
            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(data.banner)
                .build()
            title.text = data.title
        }

        fun bind(item: TvChannels.TvChannel, onClickListener: OnTvChannelClickListener) {
            container.setBackgroundResource(
                if (absoluteAdapterPosition == adapter.selectedPosition) R.drawable.item_television_background_selected
                else R.drawable.item_television_background)

            itemView.setOnClickListener {
                val oldPosition = adapter.selectedPosition
                adapter.apply {
                    selectedPosition = absoluteAdapterPosition
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)
                }
                onClickListener.onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_related_tvchannel, parent, false)
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
            bind(tvChannels, onClickListener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}