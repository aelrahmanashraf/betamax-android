package com.picassos.betamax.android.presentation.app.genre.tv_genres

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.chip.Chip
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.listener.OnGenreClickListener

class TvGenresAdapter(private val context: Context, private val listener: OnGenreClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal inner class TvGenresHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: Chip = itemView.findViewById(R.id.genre_container)

        fun setData(data: Genres.Genre) {
            container.text = data.title
        }

        fun bind(item: Genres.Genre, listener: OnGenreClickListener) {
            container.setOnCheckedChangeListener { _, _ ->
                listener.onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre_selectable, parent, false).apply {
            id = id
        }
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