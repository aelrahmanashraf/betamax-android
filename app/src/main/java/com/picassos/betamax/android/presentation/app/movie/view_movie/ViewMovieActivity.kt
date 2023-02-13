package com.picassos.betamax.android.presentation.app.movie.view_movie

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
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
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.picassos.betamax.android.core.utilities.Helper
import com.facebook.drawee.backends.pipeline.PipelineDraweeController
import com.facebook.imagepipeline.postprocessors.BlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityViewMovieBinding
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.presentation.app.cast.CastAdapter
import com.picassos.betamax.android.presentation.app.episode.episodes.EpisodesAdapter
import com.picassos.betamax.android.presentation.app.movie.movies.MoviesAdapter
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.domain.listener.OnEpisodeClickListener
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.model.PlayerContent
import com.picassos.betamax.android.domain.model.Seasons
import com.picassos.betamax.android.domain.worker.VideoPreloadWorker
import com.picassos.betamax.android.presentation.app.episode.show_episode.ShowEpisodeBottomSheetModal
import com.picassos.betamax.android.presentation.app.movie.movie_player.MoviePlayerActivity
import com.picassos.betamax.android.presentation.app.season.seasons.SeasonsBottomSheetModal
import com.picassos.betamax.android.presentation.app.season.seasons.SeasonsViewModel
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import org.json.JSONException

@DelicateCoroutinesApi
@AndroidEntryPoint
class ViewMovieActivity : AppCompatActivity() {
    private lateinit var layout: ActivityViewMovieBinding
    private val viewMovieViewModel: ViewMovieViewModel by viewModels()
    private val seasonsViewModel: SeasonsViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var movie: Movies.Movie
    private var selectedSeason = Seasons.Season(level = 1)
    private var requireRefresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = SharedPreferences(this)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_view_movie)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@ViewMovieActivity)
            }
        }

        val requestDialog = RequestDialog(this)

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
                schedulePreloadWork(movie.url)
            }
        }

        val castAdapter = CastAdapter()
        layout.recyclerCast.apply {
            layoutManager = LinearLayoutManager(this@ViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }

        val moviesAdapter = MoviesAdapter(isHorizontal = true, listener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie?) {
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

        val episodesAdapter = EpisodesAdapter(listener = object: OnEpisodeClickListener {
            override fun onItemClick(episode: Episodes.Episode?) {
                val bundle = Bundle().apply {
                    putSerializable("movie", movie)
                    putSerializable("episode", episode)
                }
                val showEpisodeBottomSheetModal = ShowEpisodeBottomSheetModal()
                showEpisodeBottomSheetModal.arguments = bundle
                showEpisodeBottomSheetModal.show(supportFragmentManager, "TAG")
            }
        })
        layout.recyclerEpisodes.apply {
            layoutManager = LinearLayoutManager(this@ViewMovieActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = episodesAdapter
        }

        collectLatestOnLifecycleStarted(viewMovieViewModel.viewMovie) { state ->
            if (state.isLoading) {
                requestDialog.show()

                layout.apply {
                    movieContainer.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                requestDialog.dismiss()

                val movieDetails = state.response.movieDetails.movies[0]
                layout.apply {
                    movieTitle.text = movieDetails.title
                    movieDate.text = Helper.getFormattedDateString(movieDetails.date, "yyyy")
                    movieDuration.text = Helper.convertMinutesToHoursAndMinutes(movieDetails.duration)
                    movieDescription.text = movieDetails.description
                    if (movieDetails.genre == 0) {
                        movieGenre.visibility = View.GONE
                    } else {
                        movieGenre.visibility = View.VISIBLE
                        movieGenre.text = state.response.movieGenre.title
                    }
                    movieRating.text = movieDetails.rating.toString()
                    movieThumbnail.controller = Fresco.newDraweeControllerBuilder()
                        .setTapToRetryEnabled(true)
                        .setUri(movieDetails.thumbnail)
                        .build()
                }

                layout.movieThumbnailContainer.apply {
                    val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(movieDetails.banner))
                        .setPostprocessor(BlurPostProcessor(10, this@ViewMovieActivity, 1))
                        .build()

                    val controller = Fresco.newDraweeControllerBuilder().setImageRequest(request)
                        .setOldController(this.controller)
                        .build() as PipelineDraweeController

                    this.controller = controller
                }

                layout.playMovie.apply {
                    when (movieDetails.series) {
                        0 -> {
                            visibility = View.VISIBLE
                            setOnClickListener {
                                viewMovieViewModel.requestCheckSubscription()
                            }
                        }
                        1 -> visibility = View.GONE
                    }
                }

                layout.saveMovie.apply {
                    when (state.response.movieSaved) {
                        1 -> {
                            setImageResource(R.drawable.icon_check)
                        }
                        else -> {
                            setImageResource(R.drawable.icon_plus)
                        }
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

                when (movieDetails.series) {
                    0 -> {
                        layout.apply {
                            movieMetaContainer.visibility = View.VISIBLE
                            relatedMoviesContainer.visibility = View.VISIBLE
                            seasonsContainer.visibility = View.GONE
                        }
                        val movies = state.response.relatedMovies.movies
                        moviesAdapter.differ.submitList(movies)
                        if (movies.isEmpty()) {
                            layout.relatedMoviesContainer.visibility = View.GONE
                        } else {
                            layout.relatedMoviesContainer.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        layout.apply {
                            movieMetaContainer.visibility = View.GONE
                            relatedMoviesContainer.visibility = View.GONE
                            season.text = state.response.movieEpisodes.seasonTitle
                            seasonsContainer.apply {
                                visibility = View.VISIBLE
                                setOnClickListener {
                                    val bundle = Bundle().apply {
                                        putInt("movie_id", movie.id)
                                    }
                                    val seasonsBottomSheetModal = SeasonsBottomSheetModal()
                                    seasonsBottomSheetModal.arguments = bundle
                                    seasonsBottomSheetModal.show(supportFragmentManager, "TAG")
                                }
                            }
                        }
                        val episodes = state.response.movieEpisodes.rendered
                        episodesAdapter.differ.submitList(episodes)
                        if (episodes.isEmpty()) {
                            layout.seasonsContainer.visibility = View.GONE
                        } else {
                            layout.seasonsContainer.visibility = View.VISIBLE
                        }
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()

                layout.apply {
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

        collectLatestOnLifecycleStarted(viewMovieViewModel.checkSubscription) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.response != null) {
                requestDialog.dismiss()

                val subscription = state.response
                when (subscription.daysLeft) {
                    0 -> startActivity(Intent(this@ViewMovieActivity, SubscribeActivity::class.java))
                    else -> {
                        Intent(this@ViewMovieActivity, MoviePlayerActivity::class.java).also { intent ->
                            intent.putExtra("playerContent", PlayerContent(
                                id = movie.id,
                                url = movie.url,
                                thumbnail = movie.thumbnail))
                            startActivity(intent)
                        }
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
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
                viewMovieViewModel.requestEpisodes(
                    movieId = movie.id,
                    seasonLevel = season.level)
            }
        }

        collectLatestOnLifecycleStarted(viewMovieViewModel.episodes) { state ->
            if (state.response != null) {
                episodesAdapter.differ.submitList(state.response.rendered)
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
                if (isRefreshing) {
                    isRefreshing = false
                }
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

    private fun schedulePreloadWork(url: String) {
        val workManager = WorkManager.getInstance(applicationContext)
        val videoPreloadWorker = VideoPreloadWorker.buildWorkRequest(url)
        workManager.enqueueUniqueWork(
            "VideoPreloadWorker",
            ExistingWorkPolicy.KEEP,
            videoPreloadWorker)
    }
}