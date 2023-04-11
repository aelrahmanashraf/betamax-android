package com.picassos.betamax.android.presentation.television.movie.movies

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.listener.OnMovieFocusListener

class TelevisionMoviesAdapter(private val isPoster: Boolean = false, private val isHorizontal: Boolean = false, private val onClickListener: OnMovieClickListener, private val onFocusListener: OnMovieFocusListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class MoviesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.movie_title)
        val date: TextView = itemView.findViewById(R.id.movie_date)
        val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.movie_thumbnail)

        @SuppressLint("SetTextI18n")
        fun setData(data: Movies.Movie, isPoster: Boolean) {
            title.text = data.title
            date.text = itemView.context.getString(R.string.released_in) + " " + Helper.getFormattedDateString(data.date, "yyyy")
            if (isPoster) {
                itemView.findViewById<LinearLayout>(R.id.movie_meta).visibility = View.GONE
            }
            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(data.thumbnail)
                .build()
        }

        fun bind(item: Movies.Movie, onClickListener: OnMovieClickListener, onFocusListener: OnMovieFocusListener) {
            itemView.apply {
                setOnClickListener {
                    onClickListener.onItemClick(item)
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        onFocusListener.onItemFocus(item)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = when (isHorizontal) {
            false -> LayoutInflater.from(parent.context).inflate(R.layout.item_television_movie_vertical, parent, false)
            else -> LayoutInflater.from(parent.context).inflate(R.layout.item_television_movie_horizontal, parent, false)
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
        (holder as MoviesHolder).apply {
            setData(movies, isPoster)
            bind(movies, onClickListener, onFocusListener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}