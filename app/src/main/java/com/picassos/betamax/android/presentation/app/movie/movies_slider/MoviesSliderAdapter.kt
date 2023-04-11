package com.picassos.betamax.android.presentation.app.movie.movies_slider

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.picassos.betamax.android.R
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.listener.OnMovieClickListener

class MoviesSliderAdapter(private val context: Context, private val movies: List<Movies.Movie>, private val listener: OnMovieClickListener) : PagerAdapter() {
    override fun getCount(): Int {
        return movies.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View =  (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.item_movie_slider, null)
        val title: TextView = view.findViewById(R.id.movie_title)
        val description: TextView = view.findViewById(R.id.movie_description)
        val date: TextView = view.findViewById(R.id.movie_date)
        val duration: TextView = view.findViewById(R.id.movie_duration)
        val thumbnailContainer: SimpleDraweeView = view.findViewById(R.id.movie_thumbnail_container)
        movies[position].let { movie ->
            title.text = movie.title
            description.text = movie.description
            date.text = Helper.getFormattedDateString(movie.date, "yyyy")
            duration.text = Helper.convertMinutesToHoursAndMinutes(movie.duration)
            thumbnailContainer.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(movie.banner)
                .build()
        }
        view.setOnClickListener {
            listener.onItemClick(movies[position])
        }

        (container as ViewPager).apply {
            addView(view, 0)
        }
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val viewPager = container as ViewPager
        val view = `object` as View
        viewPager.removeView(view)
    }
}