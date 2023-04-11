package com.picassos.betamax.android.presentation.app.track.tracks

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.picassos.betamax.android.domain.model.TracksGroup

class TracksAdapter(private val listener: OnTrackClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface OnTrackClickListener {
        fun onItemClick(track: TracksGroup.Track)
    }

    internal inner class TracksHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.track_title)

        fun setData(data: TracksGroup.Track) {
            title.text = data.title
        }

        fun bind(item: TracksGroup.Track, listener: OnTrackClickListener) {
            itemView.setOnClickListener {  listener.onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TracksHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false))
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<TracksGroup.Track>() {
        override fun areItemsTheSame(oldItem: TracksGroup.Track, newItem: TracksGroup.Track): Boolean {
           return oldItem.id == newItem.id
               && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: TracksGroup.Track, newItem: TracksGroup.Track): Boolean {
            return oldItem.id == newItem.id
                && oldItem.title == newItem.title
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tracks = differ.currentList[position]
        (holder as TracksHolder).apply {
            setData(tracks)
            bind(tracks, listener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}