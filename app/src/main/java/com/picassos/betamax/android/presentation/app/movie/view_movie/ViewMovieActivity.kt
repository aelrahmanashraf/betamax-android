package com.picassos.betamax.android.presentation.app.movie.view_movie

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.picassos.betamax.android.core.utilities.Helper
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.postprocessors.BlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.databinding.ActivityViewMovieBinding
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.presentation.app.cast.CastAdapter
import com.picassos.betamax.android.presentation.app.episode.episodes.EpisodesAdapter
import com.picassos.betamax.android.presentation.app.movie.movies.MoviesAdapter
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.listener.OnEpisodeClickListener
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.model.MoviePlayerContent
import com.picassos.betamax.android.domain.model.Seasons
import com.picassos.betamax.android.presentation.app.episode.episodes.EpisodesViewModel
import com.picassos.betamax.android.presentation.app.episode.show_episode.ShowEpisodeBottomSheetModal
import com.picassos.betamax.android.presentation.app.movie.movie_player.MoviePlayerActivity
import com.picassos.betamax.android.presentation.app.season.seasons.SeasonsBottomSheetModal
import com.picassos.betamax.android.presentation.app.season.seasons.SeasonsViewModel
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import org.json.JSONException

@AndroidEntryPoint
class ViewMovieActivity : AppCompatActivity(), ShowEpisodeBottomSheetModal.OnEpisodeBottomSheetDismissedListener {
    private lateinit var layout: ActivityViewMovieBinding
    private val viewMovieViewModel: ViewMovieViewModel by viewModels()
    private val seasonsViewModel: SeasonsViewModel by viewModels()
    private val episodesViewModel: EpisodesViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var movie: Movies.Movie
    private var selectedSeason = Seasons.Season(level = 1)
    private var episodes: Episodes? = null

    private var requireRefresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = SharedPreferences(this)
        val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_view_movie)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@ViewMovieActivity)
            }
        }

        layout.goBack.setOnClickListener {
            Intent().also { intent ->
                intent.putExtra("refreshContent", requireRefresh)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        getSerializable(this@ViewMovieActivity, "movie", Movies.Movie::class.java).also { movie ->
            viewMovieViewModel.apply {
                setMovie(movie)
                requestMovie(
                    movieId = movie.id,
                    seasonLevel = selectedSeason.level,
                    genreId = movie.genre)
            }
        }

        collectLatestOnLifecycleStarted(viewMovieViewModel.movie) { isSafe ->
            isSafe?.let { movie ->
                this.movie = movie
            }
        }

        val castAdapter = CastAdapter()
        layout.recyclerCast.apply {
            layoutManager = LinearLayoutManager(this@ViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }

        val moviesAdapter = MoviesAdapter(isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(this@ViewMovieActivity, ViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        })
        layout.recyclerRelatedMovies.apply {
            layoutManager = LinearLayoutManager(this@ViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }

        val episodesAdapter = EpisodesAdapter(onClickListener = object: OnEpisodeClickListener {
            override fun onItemClick(episode: Episodes.Episode) {
                val showEpisodeBottomSheetModal = ShowEpisodeBottomSheetModal()
                showEpisodeBottomSheetModal.arguments = Bundle().apply {
                    putSerializable("movie", movie)
                    putSerializable("episodes", episodes)
                    putSerializable("episode", episode)
                }
                showEpisodeBottomSheetModal.show(supportFragmentManager, "show_episode")
            }
        })
        layout.recyclerEpisodes.apply {
            layoutManager = LinearLayoutManager(this@ViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = episodesAdapter
        }

        collectLatestOnLifecycleStarted(viewMovieViewModel.viewMovie) { state ->
            if (state.isLoading) {
                layout.apply {
                    refreshLayout.isRefreshing = true
                    movieContainer.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                layout.refreshLayout.isRefreshing = false

                val movieDetails = state.response.movieDetails.movies[0]
                layout.apply {
                    if (movieDetails.series == 0) {
                        movieMetaContainer.visibility = View.VISIBLE
                        relatedMoviesContainer.visibility = View.VISIBLE
                        seasonsContainer.visibility = View.GONE
                    } else {
                        movieMetaContainer.visibility = View.GONE
                        relatedMoviesContainer.visibility = View.GONE
                    }
                    movieTitle.text = movieDetails.title
                    movieDate.text = Helper.getFormattedDateString(movieDetails.date, "yyyy")
                    movieDetails.duration?.let { movieDuration.text = Helper.convertMinutesToHoursAndMinutes(it) }
                    movieDescription.text = movieDetails.description
                    if (movieDetails.genre == 0) {
                        movieGenre.visibility = View.GONE
                    } else {
                        movieGenre.visibility = View.VISIBLE
                        movieGenre.text = state.response.movieGenre.title
                    }
                    movieRating.text = movieDetails.rating.toString()

                    val movieThumbnailImageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(movie.thumbnail))
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                        .setProgressiveRenderingEnabled(true)
                        .build()
                    movieThumbnail.controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(movieThumbnailImageRequest)
                        .setOldController(movieThumbnail.controller)
                        .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                            override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                                movieThumbnailImageRequest.sourceUri?.let { Fresco.getImagePipeline().evictFromMemoryCache(it) }
                            }
                        })
                        .build()

                    val movieThumbnailContainerImageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(movie.thumbnail))
                        .setPostprocessor(BlurPostProcessor(10, this@ViewMovieActivity, 1))
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                        .setProgressiveRenderingEnabled(true)
                        .build()
                    movieThumbnailContainer.controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(movieThumbnailContainerImageRequest)
                        .setOldController(movieThumbnailContainer.controller)
                        .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                            override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                                movieThumbnailContainerImageRequest.sourceUri?.let { Fresco.getImagePipeline().evictFromMemoryCache(it) }
                            }
                        })
                        .build()
                }

                layout.playMovie.apply {
                    when (movieDetails.series) {
                        0 -> visibility = View.VISIBLE
                        1 -> visibility = View.GONE
                    }
                    setOnClickListener {
                        lifecycleScope.launch {
                            entryPoint.getSubscriptionUseCase().invoke().collect { subscription ->
                                if (subscription.daysLeft == 0) {
                                    startActivity(Intent(this@ViewMovieActivity, SubscribeActivity::class.java))
                                } else {
                                    Intent(this@ViewMovieActivity, MoviePlayerActivity::class.java).also { intent ->
                                        intent.putExtra("playerContent", MoviePlayerContent(movie = movie))
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                    }
                }

                layout.saveMovie.apply {
                    if (state.response.movieSaved == 1) {
                        setImageResource(R.drawable.icon_check)
                    } else {
                        setImageResource(R.drawable.icon_plus)
                    }
                    setOnClickListener {
                        viewMovieViewModel.requestSaveMovie(movieDetails.id)
                    }
                }

                layout.shareMovie.setOnClickListener {
                    Intent(Intent.ACTION_SEND).also { intent ->
                        try {
                            intent.putExtra(Intent.EXTRA_TEXT, "share")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        intent.type = "text/plain"
                        startActivity(Intent.createChooser(intent, getString(R.string.share_movie)))
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
                    val episodes = state.response.movieEpisodes
                    this@ViewMovieActivity.episodes = episodes
                    layout.apply {
                        season.text = episodes.seasonTitle
                        seasonsContainer.apply {
                            visibility = View.VISIBLE
                            setOnClickListener {
                                val seasonsBottomSheetModal = SeasonsBottomSheetModal()
                                seasonsBottomSheetModal.arguments = Bundle().apply {
                                    putInt("movie_id", movie.id)
                                }
                                seasonsBottomSheetModal.show(supportFragmentManager, "seasons")
                            }
                        }
                    }
                    episodesAdapter.differ.submitList(episodes.rendered)
                    if (episodes.rendered.isEmpty()) {
                        layout.seasonsContainer.visibility = View.GONE
                    } else {
                        layout.seasonsContainer.visibility = View.VISIBLE
                    }
                }
            }
            if (state.error != null) {
                layout.apply {
                    refreshLayout.isRefreshing = false
                    movieContainer.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        viewMovieViewModel.requestMovie(
                            movieId = movie.id,
                            seasonLevel = selectedSeason.level,
                            genreId = movie.genre)
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        collectLatestOnLifecycleStarted(viewMovieViewModel.saveMovie) { state ->
            if (state.responseCode != null) {
                requireRefresh = true

                when (state.responseCode) {
                    "1" -> layout.saveMovie.setImageResource(R.drawable.icon_check)
                    "0" -> layout.saveMovie.setImageResource(R.drawable.icon_plus)
                    else -> showToast(this@ViewMovieActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                }
            }
            if (state.error != null) {
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@ViewMovieActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@ViewMovieActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@ViewMovieActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }

        collectLatestOnLifecycleStarted(seasonsViewModel.selectedSeason) { isSafe ->
            isSafe?.let { season ->
                selectedSeason = season
                layout.season.text = season.title
                episodesViewModel.requestEpisodes(
                    movieId = movie.id,
                    seasonLevel = season.level)
            }
        }

        collectLatestOnLifecycleStarted(episodesViewModel.episodes) { state ->
            if (state.response != null) {
                val episodes = state.response.rendered
                episodesAdapter.differ.submitList(episodes)
            }
        }

        layout.refreshLayout.apply {
            elevation = 0f
            setColorSchemeColors(ContextCompat.getColor(this@ViewMovieActivity, R.color.color_theme))
            when (sharedPreferences.loadDarkMode()) {
                1 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@ViewMovieActivity, R.color.color_white))
                2 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@ViewMovieActivity, R.color.color_darker))
                3 -> {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@ViewMovieActivity, R.color.color_darker))
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@ViewMovieActivity, R.color.color_white))
                    }
                }
            }
            setOnRefreshListener {
                viewMovieViewModel.requestMovie(
                    movieId = movie.id,
                    seasonLevel = selectedSeason.level,
                    genreId = movie.genre)
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Intent().also { intent ->
                    intent.putExtra("refreshContent", requireRefresh)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    override fun onEpisodesBottomSheetDismissed() {
        episodesViewModel.requestEpisodes(
            movieId = movie.id,
            seasonLevel = selectedSeason.level)
    }
}