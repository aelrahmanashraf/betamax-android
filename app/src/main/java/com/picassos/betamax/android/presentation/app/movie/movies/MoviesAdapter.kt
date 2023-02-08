package com.picassos.betamax.android.presentation.app.movie.movies

import androidx.recyclerview.widget.RecyclerView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.listener.OnMovieClickListener

class MoviesAdapter(private val isHorizontal: Boolean = false, private val listener: OnMovieClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class MoviesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.movie_title)
        val date: TextView = itemView.findViewById(R.id.movie_date)
        val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.movie_thumbnail)

        fun setData(data: Movies.Movie) {
            title.text = data.title
            date.text = Helper.getFormattedDateString(data.date, "yyyy")
            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(data.thumbnail)
                .build()
        }

        fun bind(item: Movies.Movie?, listener: OnMovieClickListener) {
            itemView.setOnClickListener {  listener.onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = when (isHorizontal) {
            false -> LayoutInflater.from(parent.context).inflate(R.layout.item_movie_vertical, parent, false)
            else -> LayoutInflater.from(parent.context).inflate(R.layout.item_movie_horizontal, parent, false)
        }
        return MoviesHolder(view)
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Movies.Movie>() {
        override fun areItemsTheSame(oldItem: Movies.Movie, newItem: Movies.Movie): Boolean {
           return oldItem.id == newItem.id
               && oldItem.title == newItem.title
               && oldItem.description == newItem.description
        }

        override fun areContentsTheSame(oldItem: Movies.Movie, newItem: Movies.Movie): Boolean {
            return oldItem.id == newItem.id
                && oldItem.title == newItem.title
                && oldItem.description == newItem.description
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val movies = differ.currentList[position]
        val moviesHolder = holder as MoviesHolder
        moviesHolder.setData(movies)
        moviesHolder.bind(movies, listener)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}