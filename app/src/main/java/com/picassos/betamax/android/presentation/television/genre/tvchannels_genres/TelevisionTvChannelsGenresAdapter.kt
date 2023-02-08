package com.picassos.betamax.android.presentation.television.genre.tvchannels_genres

import androidx.recyclerview.widget.RecyclerView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.listener.OnGenreClickListener

class TelevisionTvChannelsGenresAdapter(private val listener: OnGenreClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal inner class TvGenresHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.genre_title)

        fun setData(data: Genres.Genre) {
            title.text = data.title
        }

        fun bind(item: Genres.Genre, listener: OnGenreClickListener) {
            itemView.setOnClickListener {  listener.onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_television_genre, parent, false)
        return TvGenresHolder(view)
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Genres.Genre>() {
        override fun areItemsTheSame(oldItem: Genres.Genre, newItem: Genres.Genre): Boolean {
           return oldItem.id == newItem.id
               && oldItem.genreId == newItem.genreId
               && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Genres.Genre, newItem: Genres.Genre): Boolean {
            return oldItem.id == newItem.id
                && oldItem.genreId == newItem.genreId
                && oldItem.title == newItem.title
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val genres = differ.currentList[position]
        val tvGenresHolder = holder as TvGenresHolder
        tvGenresHolder.setData(genres)
        tvGenresHolder.bind(genres, listener)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}