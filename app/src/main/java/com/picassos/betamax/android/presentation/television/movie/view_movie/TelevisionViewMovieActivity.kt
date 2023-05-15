package com.picassos.betamax.android.presentation.television.movie.view_movie

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeController
import com.facebook.imagepipeline.postprocessors.BlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.core.view.dialog.TelevisionSubscriptionDialog
import com.picassos.betamax.android.databinding.ActivityTelevisionViewMovieBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.listener.OnEpisodeClickListener
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.listener.OnMovieFocusListener
import com.picassos.betamax.android.domain.listener.OnSeasonClickListener
import com.picassos.betamax.android.domain.model.*
import com.picassos.betamax.android.presentation.app.cast.CastAdapter
import com.picassos.betamax.android.presentation.television.episode.episode_player.TelevisionEpisodePlayerActivity
import com.picassos.betamax.android.presentation.television.episode.episodes.TelevisionEpisodesAdapter
import com.picassos.betamax.android.presentation.television.movie.movie_player.TelevisionMoviePlayerActivity
import com.picassos.betamax.android.presentation.television.movie.movies.TelevisionMoviesAdapter
import com.picassos.betamax.android.presentation.television.season.seasons.TelevisionSeasonsAdapter
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TelevisionViewMovieActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionViewMovieBinding
    private val televisionViewMovieViewModel: TelevisionViewMovieViewModel by viewModels()

    private lateinit var movie: Movies.Movie

    private var selectedSeason = Seasons.Season(level = 1)
    private var episodes: Episodes? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_view_movie)

        val requestDialog = RequestDialog(this)

        Helper.getSerializable(this@TelevisionViewMovieActivity, "movie", Movies.Movie::class.java).also { movie ->
            televisionViewMovieViewModel.apply {
                setMovie(movie)
                requestMovie(
                    movieId = movie.id,
                    seasonLevel = selectedSeason.level,
                    genreId = movie.genre)
            }
        }

        collectLatestOnLifecycleStarted(televisionViewMovieViewModel.movie) { isSafe ->
            isSafe?.let { movie ->
                this.movie = movie
            }
        }

        val castAdapter = CastAdapter()
        layout.recyclerCast.apply {
            layoutManager = LinearLayoutManager(this@TelevisionViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }

        val moviesAdapter = TelevisionMoviesAdapter(isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(this@TelevisionViewMovieActivity, TelevisionViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        }, onFocusListener = object: OnMovieFocusListener {
            override fun onItemFocus(movie: Movies.Movie, position: Int) {

            }
        })
        layout.recyclerRelatedMovies.apply {
            layoutManager = LinearLayoutManager(this@TelevisionViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }

        val episodesAdapter = TelevisionEpisodesAdapter(onClickListener = object: OnEpisodeClickListener {
            override fun onItemClick(episode: Episodes.Episode) {
                lifecycleScope.launch {
                    entryPoint.getSubscriptionUseCase().invoke().collect { subscription ->
                        if (subscription.daysLeft == 0) {
                            TelevisionSubscriptionDialog(this@TelevisionViewMovieActivity).show()
                        } else {
                            Intent(this@TelevisionViewMovieActivity, TelevisionEpisodePlayerActivity::class.java).also { intent ->
                                intent.putExtra("playerContent", EpisodePlayerContent(
                                    movie = movie,
                                    episode = episode,
                                    episodes = episodes,
                                    currentPosition = episode.currentPosition ?: 0))
                                startActivityForResult.launch(intent)
                            }
                        }
                    }
                }
            }
        })
        layout.recyclerEpisodes.apply {
            layoutManager = LinearLayoutManager(this@TelevisionViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = episodesAdapter
        }

        collectLatestOnLifecycleStarted(televisionViewMovieViewModel.viewMovie) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.response != null) {
                requestDialog.dismiss()

                val movieDetails = state.response.movieDetails.movies[0]
                layout.apply {
                    if (movieDetails.series == 0) {
                        movieDurationContainer.visibility = View.VISIBLE
                        relatedMoviesContainer.visibility = View.VISIBLE
                        seasonsContainer.visibility = View.GONE
                    } else {
                        movieDurationContainer.visibility = View.GONE
                        relatedMoviesContainer.visibility = View.GONE
                    }
                    movieTitle.text = movieDetails.title
                    movieDate.text = getString(R.string.released_in) + " " + Helper.getFormattedDateString(movieDetails.date, "yyyy")
                    movieDuration.text = Helper.convertMinutesToHoursAndMinutes(movieDetails.duration)
                    movieDescription.text = movieDetails.description
                    if (movieDetails.genre == 0) {
                        movieGenre.visibility = View.GONE
                    } else {
                        movieGenre.visibility = View.VISIBLE
                        movieGenre.text = state.response.movieGenre.title
                    }
                    movieRating.text = "${movieDetails.rating} / 10"
                    movieThumbnail.controller = Fresco.newDraweeControllerBuilder()
                        .setTapToRetryEnabled(true)
                        .setUri(movieDetails.thumbnail)
                        .build()
                }

                layout.movieThumbnailContainer.apply {
                    val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(movieDetails.banner))
                        .setPostprocessor(BlurPostProcessor(10, this@TelevisionViewMovieActivity, 1))
                        .build()

                    val controller = Fresco.newDraweeControllerBuilder().setImageRequest(request)
                        .setOldController(this.controller)
                        .build() as PipelineDraweeController

                    this.controller = controller
                }

                layout.playMovie.apply {
                    if (movieDetails.series == 0) {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            lifecycleScope.launch {
                                entryPoint.getSubscriptionUseCase().invoke().collect { subscription ->
                                    if (subscription.daysLeft == 0) {
                                        TelevisionSubscriptionDialog(this@TelevisionViewMovieActivity).show()
                                    } else {
                                        Intent(this@TelevisionViewMovieActivity, TelevisionMoviePlayerActivity::class.java).also { intent ->
                                            intent.putExtra("playerContent", MoviePlayerContent(movie = movie))
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                layout.apply {
                    saveMovie.setOnClickListener {
                        televisionViewMovieViewModel.requestSaveMovie(movieDetails.id)
                    }
                    saveMovieIcon.apply {
                        if (state.response.movieSaved == 1) {
                            setImageResource(R.drawable.icon_check)
                        } else {
                            setImageResource(R.drawable.icon_plus)
                        }
                    }
                }

                val cast = state.response.movieCast.cast
                castAdapter.differ.submitList(cast)
                if (cast.isEmpty()) {
                    layout.castContainer.visibility = View.GONE
                } else {
                    layout.castContainer.visibility = View.VISIBLE
                }

                if (movieDetails.series == 0) {
                    val movies = state.response.relatedMovies.movies
                    moviesAdapter.differ.submitList(movies)
                    if (movies.isEmpty()) {
                        layout.relatedMoviesContainer.visibility = View.GONE
                    } else {
                        layout.relatedMoviesContainer.visibility = View.VISIBLE
                    }
                } else {
                    val seasonsAdapter = TelevisionSeasonsAdapter(onClickListener = object: OnSeasonClickListener {
                        override fun onItemClick(season: Seasons.Season) {
                            televisionViewMovieViewModel.requestEpisodes(
                                movieId = movie.id,
                                seasonLevel = season.level)
                        }
                    })
                    layout.recyclerSeasons.apply {
                        layoutManager = LinearLayoutManager(this@TelevisionViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
                        adapter = seasonsAdapter
                    }
                    televisionViewMovieViewModel.requestSeasons(movieDetails.id)
                    collectLatestOnLifecycleStarted(televisionViewMovieViewModel.seasons) { seasonsState ->
                        if (seasonsState.response != null) {
                            val seasons = seasonsState.response.seasons
                            seasonsAdapter.differ.submitList(seasons)
                        }
                    }

                    val episodes = state.response.movieEpisodes
                    this@TelevisionViewMovieActivity.episodes = episodes
                    episodesAdapter.differ.submitList(episodes.rendered)
                    if (episodes.rendered.isEmpty()) {
                        layout.seasonsContainer.visibility = View.GONE
                    } else {
                        layout.seasonsContainer.visibility = View.VISIBLE
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
            }
        }

        collectLatestOnLifecycleStarted(televisionViewMovieViewModel.saveMovie) { state ->
            if (state.responseCode != null) {
                when (state.responseCode) {
                    "1" -> layout.saveMovieIcon.setImageResource(R.drawable.icon_check)
                    "0" -> layout.saveMovieIcon.setImageResource(R.drawable.icon_plus)
                    else -> showToast(this@TelevisionViewMovieActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                }
            }
            if (state.error != null) {
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@TelevisionViewMovieActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@TelevisionViewMovieActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@TelevisionViewMovieActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }

        collectLatestOnLifecycleStarted(televisionViewMovieViewModel.episodes) { state ->
            if (state.response != null) {
                val episodes = state.response.rendered
                episodesAdapter.differ.submitList(episodes)
            }
        }
    }

    private var startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if (result != null && result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                if (data.getBooleanExtra("refreshContent", false)) {
                    televisionViewMovieViewModel.requestEpisodes(
                        movieId = movie.id,
                        seasonLevel = selectedSeason.level)
                }
            }
        }
    }
}