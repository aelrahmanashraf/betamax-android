package com.picassos.betamax.android.presentation.television.season.seasons

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

class TelevisionSeasonsAdapter(private val onClickListener: OnSeasonClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class SeasonsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.season_title)

        fun setData(data: Seasons.Season) {
            title.text = data.title
        }

        fun bind(item: Seasons.Season, onClickListener: OnSeasonClickListener) {
            itemView.setOnClickListener {
                onClickListener.onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_television_season_horizontal, parent, false)
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
        (holder as SeasonsHolder).apply {
            setData(seasons)
            bind(seasons, onClickListener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}