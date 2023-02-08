package com.picassos.betamax.android.presentation.app.season.seasons

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.picassos.betamax.android.domain.listener.OnSeasonClickListener
import com.picassos.betamax.android.domain.model.Seasons

class SeasonsAdapter(private val listener: OnSeasonClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class SeasonsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.season_title)

        fun setData(data: Seasons.Season) {
            title.text = data.title
        }

        fun bind(item: Seasons.Season, listener: OnSeasonClickListener) {
            itemView.setOnClickListener {  listener.onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_season_vertical, parent, false)
        return SeasonsHolder(view)
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Seasons.Season>() {
        override fun areItemsTheSame(oldItem: Seasons.Season, newItem: Seasons.Season): Boolean {
           return oldItem.id == newItem.id
               && oldItem.seasonId == newItem.seasonId
               && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Seasons.Season, newItem: Seasons.Season): Boolean {
            return oldItem.id == newItem.id
                && oldItem.seasonId == newItem.seasonId
                && oldItem.title == newItem.title
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val seasons = differ.currentList[position]
        val seasonsHolder = holder as SeasonsHolder
        seasonsHolder.setData(seasons)
        seasonsHolder.bind(seasons, listener)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}