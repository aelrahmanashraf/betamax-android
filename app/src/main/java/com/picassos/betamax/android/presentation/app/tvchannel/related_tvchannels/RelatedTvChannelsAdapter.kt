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
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener

class RelatedTvChannelsAdapter(private val selectedChannel: Int, private val listener: OnTvChannelClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
     internal class TvChannelsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.tvchannel_container)
        val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.tvchannel_thumbnail)
        val title: TextView = itemView.findViewById(R.id.tvchannel_title)

        fun setData(activeChannel: Int, data: TvChannels.TvChannel) {
            if (activeChannel == data.tvChannelId) container.setBackgroundResource(R.drawable.item_tvchannel_backround_selected)
            else container.setBackgroundResource(R.drawable.item_tvchannel_background)

            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(data.banner)
                .build()
            title.text = data.title
        }

        fun bind(item: TvChannels.TvChannel, listener: OnTvChannelClickListener) {
            itemView.setOnClickListener {  listener.onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_related_tvchannel, parent, false)
        return TvChannelsHolder(view)
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
        val tvChannelsHolder = holder as TvChannelsHolder
        tvChannelsHolder.setData(selectedChannel, tvChannels)
        tvChannelsHolder.bind(tvChannels, listener)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}