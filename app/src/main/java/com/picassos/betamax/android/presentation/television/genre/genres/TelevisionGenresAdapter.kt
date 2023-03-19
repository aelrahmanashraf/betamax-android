package com.picassos.betamax.android.presentation.television.genre.genres

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.listener.OnGenreClickListener

class TelevisionGenresAdapter(private val listener: OnGenreClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal inner class GenresHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.genre_title)

        fun setData(data: Genres.Genre) {
            title.text = data.title
        }

        fun bind(item: Genres.Genre, listener: OnGenreClickListener) {
            itemView.setOnClickListener {  listener.onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_television_movie_genre, parent, false)
        return GenresHolder(view)
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
        val genresHolder = holder as GenresHolder
        genresHolder.setData(genres)
        genresHolder.bind(genres, listener)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}