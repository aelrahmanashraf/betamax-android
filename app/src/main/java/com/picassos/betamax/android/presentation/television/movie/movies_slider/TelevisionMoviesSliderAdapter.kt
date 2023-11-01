package com.picassos.betamax.android.presentation.television.movie.movies_slider

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable
import android.net.Uri
import android.view.LayoutInflater
import com.picassos.betamax.android.R
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.model.Movies

class TelevisionMoviesSliderAdapter(private val context: Context, private val movies: List<Movies.Movie>, private val listener: OnMovieClickListener) : PagerAdapter() {
    override fun getCount(): Int {
        return movies.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View =  (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.item_television_movie_slider, null)
        val thumbnailContainer: SimpleDraweeView = view.findViewById(R.id.movie_thumbnail_container)
        val title: TextView = view.findViewById(R.id.movie_title)
        val date: TextView = view.findViewById(R.id.movie_date)
        val duration: TextView = view.findViewById(R.id.movie_duration)
        val rating: TextView = view.findViewById(R.id.movie_rating)
        val description: TextView = view.findViewById(R.id.movie_description)
        val play: LinearLayout = view.findViewById(R.id.play_movie)

        movies[position].let { movie ->
            title.text = movie.title
            date.text = Helper.getFormattedDateString(movie.date, "yyyy")
            movie.duration?.let { duration.text = Helper.formatDuration(it) }
            rating.text = "${context.getString(R.string.rating)}: ${movie.rating} / 10"
            description.text = movie.description

            val imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(movie.banner))
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .setProgressiveRenderingEnabled(true)
                .build()
            thumbnailContainer.controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(thumbnailContainer.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        imageRequest.sourceUri?.let { Fresco.getImagePipeline().evictFromMemoryCache(it) }
                    }
                })
                .build()
        }
        play.setOnClickListener {
            listener.onItemClick(movies[position])
        }

        (container as ViewPager).apply {
            addView(view, 0)
        }
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        (container as ViewPager).apply {
            removeView(view)
        }
    }
}