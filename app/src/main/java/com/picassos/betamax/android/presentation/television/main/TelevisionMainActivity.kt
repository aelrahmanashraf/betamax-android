package com.picassos.betamax.android.presentation.television.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.core.view.dialog.TelevisionSubscriptionDialog
import com.picassos.betamax.android.databinding.ActivityTelevisionMainBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.listener.OnContinueWatchingClickListener
import com.picassos.betamax.android.domain.listener.OnContinueWatchingLongClickListener
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.listener.OnMovieFocusListener
import com.picassos.betamax.android.domain.model.ContinueWatching
import com.picassos.betamax.android.domain.model.EpisodePlayerContent
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.model.MoviePlayerContent
import com.picassos.betamax.android.presentation.app.continue_watching.ContinueWatchingViewModel
import com.picassos.betamax.android.presentation.app.profile.ProfileActivity
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import com.picassos.betamax.android.presentation.television.continue_watching.TelevisionContinueWatchingAdapter
import com.picassos.betamax.android.presentation.television.episode.episode_player.TelevisionEpisodePlayerActivity
import com.picassos.betamax.android.presentation.television.movie.movie_player.TelevisionMoviePlayerActivity
import com.picassos.betamax.android.presentation.television.movie.movies.TelevisionMoviesActivity
import com.picassos.betamax.android.presentation.television.movie.movies.TelevisionMoviesAdapter
import com.picassos.betamax.android.presentation.television.movie.view_movie.TelevisionViewMovieActivity
import com.picassos.betamax.android.presentation.television.mylist.TelevisionMyListActivity
import com.picassos.betamax.android.presentation.television.tvchannel.tvchannels.TelevisionTvChannelsActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@AndroidEntryPoint
class TelevisionMainActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionMainBinding
    private val televisionMainViewModel: TelevisionMainViewModel by viewModels()
    private val continueWatchingViewModel: ContinueWatchingViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_television_main)
        val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_main)

        val requestDialog = RequestDialog(this)

        televisionMainViewModel.apply {
            layout.navigationMovies.apply {
                setOnFocusChangeListener { _, isFocused ->
                    setNavigationMoviesFocusState(focused = isFocused)
                }
                setOnClickListener {
                    Intent(this@TelevisionMainActivity, TelevisionMoviesActivity::class.java).also { intent ->
                        intent.putExtra("request", "movies")
                        startActivity(intent)
                    }
                }
            }
            layout.navigationSeries.apply {
                setOnFocusChangeListener { _, isFocused ->
                    setNavigationSeriesFocusState(focused = isFocused)
                }
                setOnClickListener {
                    Intent(this@TelevisionMainActivity, TelevisionMoviesActivity::class.java).also { intent ->
                        intent.putExtra("request", "series")
                        startActivity(intent)
                    }
                }
            }
            layout.navigationLiveTv.apply {
                setOnFocusChangeListener { _, isFocused ->
                    setNavigationLiveTvFocusState(focused = isFocused)
                }
                setOnClickListener {
                    lifecycleScope.launch {
                        entryPoint.getSubscriptionUseCase().invoke().collect { subscription ->
                            if (subscription.daysLeft == 0) {
                                TelevisionSubscriptionDialog(this@TelevisionMainActivity).show()
                            } else {
                                startActivity(Intent(this@TelevisionMainActivity, TelevisionTvChannelsActivity::class.java))
                            }
                        }
                    }
                }
            }
            layout.navigationMylist.apply {
                setOnFocusChangeListener { _, isFocused ->
                    setNavigationMyListFocusState(focused = isFocused)
                }
                setOnClickListener {
                    startActivity(Intent(this@TelevisionMainActivity, TelevisionMyListActivity::class.java))
                }
            }
            layout.navigationProfile.apply {
                setOnFocusChangeListener { _, isFocused ->
                    setNavigationProfileFocusState(focused = isFocused)
                }
                setOnClickListener {
                    startActivity(Intent(this@TelevisionMainActivity, ProfileActivity::class.java))
                }
            }
        }

        collectLatestOnLifecycleStarted(televisionMainViewModel.navigation) { state ->
            if (state.isNavigationMoviesFocused || state.isNavigationSeriesFocused ||
                state.isNavigationLiveTvFocused || state.isNavigationMyListFocused || state.isNavigationProfileFocused) {
                toggleNavigationTitles(visibility = View.VISIBLE)
            }
            if (!state.isNavigationMoviesFocused && !state.isNavigationSeriesFocused &&
                !state.isNavigationLiveTvFocused && !state.isNavigationMyListFocused && !state.isNavigationProfileFocused) {
                toggleNavigationTitles(visibility = View.GONE)
            }
        }

        val continueWatchingAdapter = TelevisionContinueWatchingAdapter(onClickListener = object: OnContinueWatchingClickListener {
            override fun onItemClick(continueWatching: ContinueWatching.ContinueWatching) {
                lifecycleScope.launch {
                    entryPoint.getSubscriptionUseCase().invoke().collect { subscription ->
                        if (subscription.daysLeft == 0) {
                            TelevisionSubscriptionDialog(this@TelevisionMainActivity).show()
                        } else {
                            lifecycleScope.launch {
                                entryPoint.getSubscriptionUseCase().invoke().collect { subscription ->
                                    if (subscription.daysLeft == 0) {
                                        startActivity(Intent(this@TelevisionMainActivity, SubscribeActivity::class.java))
                                    } else {
                                        if (continueWatching.series == 0) {
                                            Intent(this@TelevisionMainActivity, TelevisionMoviePlayerActivity::class.java).also { intent ->
                                                intent.putExtra("playerContent", MoviePlayerContent(
                                                    movie = Movies.Movie(
                                                        id = continueWatching.contentId,
                                                        url = continueWatching.url,
                                                        title = continueWatching.title,
                                                        description = "",
                                                        thumbnail = continueWatching.thumbnail,
                                                        banner = "",
                                                        rating = 0.0,
                                                        duration = 0,
                                                        date = ""),
                                                    currentPosition = continueWatching.currentPosition))
                                                startActivity(intent)
                                            }
                                        } else {
                                            Intent(this@TelevisionMainActivity, TelevisionEpisodePlayerActivity::class.java).also { intent ->
                                                intent.putExtra("playerContent", EpisodePlayerContent(
                                                    movie = Movies.Movie(
                                                        id = 0,
                                                        url = "",
                                                        title = "",
                                                        description = "",
                                                        thumbnail = "",
                                                        banner = "",
                                                        rating = 0.0,
                                                        duration = 0,
                                                        date = ""),
                                                    episode = Episodes.Episode(
                                                        id = continueWatching.contentId,
                                                        episodeId = continueWatching.contentId,
                                                        movieId = 0,
                                                        seasonLevel = 0,
                                                        level = 0,
                                                        url = continueWatching.url,
                                                        title = continueWatching.title,
                                                        thumbnail = continueWatching.thumbnail,
                                                        duration = 0,
                                                        currentPosition = 0),
                                                    currentPosition = continueWatching.currentPosition)
                                                )
                                                startActivity(intent)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, onLongClickListener = object: OnContinueWatchingLongClickListener {
            override fun onItemLongClick(continueWatching: ContinueWatching.ContinueWatching) {
                showContinueWatchingOptions(continueWatching = continueWatching)
            }
        })
        layout.recyclerContinueWatching.apply {
            layoutManager = LinearLayoutManager(this@TelevisionMainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = continueWatchingAdapter
        }

        val newlyReleaseAdapter = TelevisionMoviesAdapter(isPoster = true, isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(this@TelevisionMainActivity, TelevisionViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        }, onFocusListener = object: OnMovieFocusListener {
            override fun onItemFocus(movie: Movies.Movie, position: Int) {
                setPreviewMovie(movie = movie)
            }
        })
        layout.recyclerNewlyRelease.apply {
            layoutManager = LinearLayoutManager(this@TelevisionMainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = newlyReleaseAdapter
        }

        val trendingAdapter = TelevisionMoviesAdapter(isPoster = true, isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(this@TelevisionMainActivity, TelevisionViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        }, onFocusListener = object: OnMovieFocusListener {
            override fun onItemFocus(movie: Movies.Movie, position: Int) {
                setPreviewMovie(movie = movie)
            }
        })
        layout.recyclerTrending.apply {
            layoutManager = LinearLayoutManager(this@TelevisionMainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                televisionMainViewModel.requestHomeContent()
            }
            televisionMainViewModel
        }
        collectLatestOnLifecycleStarted(televisionMainViewModel.home) { state ->
            if (state.isLoading) {
                layout.apply {
                    moviesProgressbar.visibility = View.VISIBLE
                    homeContainer.visibility = View.GONE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                layout.apply {
                    moviesProgressbar.visibility = View.GONE
                    homeContainer.visibility = View.VISIBLE
                }

                val continueWatching = state.response.continueWatching.continueWatching
                continueWatchingAdapter.differ.submitList(continueWatching)
                if (continueWatching.isEmpty()) {
                    layout.continueWatchingContainer.visibility = View.GONE
                } else {
                    layout.continueWatchingContainer.visibility = View.VISIBLE
                }

                val newlyRelease = state.response.newlyRelease.movies
                newlyReleaseAdapter.differ.submitList(newlyRelease)
                if (newlyRelease.isNotEmpty()) {
                    setPreviewMovie(newlyRelease[0])
                }
                val trending = state.response.trendingMovies.movies
                trendingAdapter.differ.submitList(trending)
            }
            if (state.error != null) {
                layout.apply {
                    moviesProgressbar.visibility = View.GONE
                    homeContainer.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        televisionMainViewModel.requestHomeContent()
                    }
                }
            }
        }

        collectLatestOnLifecycleStarted(continueWatchingViewModel.continueWatching) { state ->
            if (state.response != null) {
                val continueWatching = state.response.continueWatching
                continueWatchingAdapter.differ.submitList(continueWatching)
                if (continueWatching.isEmpty()) {
                    layout.continueWatchingContainer.visibility = View.GONE
                } else {
                    layout.continueWatchingContainer.visibility = View.VISIBLE
                }
            }
        }

        collectLatestOnLifecycleStarted(continueWatchingViewModel.deleteContinueWatching) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode) {
                    200 -> continueWatchingViewModel.requestContinueWatching()
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setPreviewMovie(movie: Movies.Movie) {
        layout.apply {
            movieTitle.text = movie.title
            movieDescription.text = movie.description
            movieDate.text = Helper.getFormattedDateString(movie.date, "yyyy")
            movieDuration.text = Helper.convertMinutesToHoursAndMinutes(movie.duration)
            movieRating.text = "${getString(R.string.rating)}: ${movie.rating} / 10"
            movieBanner.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(movie.banner)
                .build()
        }
    }

    private fun showContinueWatchingOptions(continueWatching: ContinueWatching.ContinueWatching) {
        val dialog = Dialog(this@TelevisionMainActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_television_continue_watching_options)
            setCancelable(true)
            setOnCancelListener {
                dismiss()
            }
        }

        dialog.findViewById<SimpleDraweeView>(R.id.continue_watching_thumbnail).controller = Fresco.newDraweeControllerBuilder()
            .setTapToRetryEnabled(true)
            .setUri(continueWatching.thumbnail)
            .build()

        dialog.findViewById<TextView>(R.id.continue_watching_title).apply {
            text = continueWatching.title
        }

        dialog.findViewById<LinearLayout>(R.id.remove_continue_watching).setOnClickListener {
            continueWatchingViewModel.requestDeleteContinueWatching(contentId = continueWatching.contentId)
            dialog.dismiss()
        }

        dialog.window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
            }
        }
        dialog.show()
    }

    private fun showExitConfirmation() {
        val dialog = Dialog(this@TelevisionMainActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_television_exit_confirmation)
            setCancelable(true)
            setOnCancelListener {
                dismiss()
            }
        }

        dialog.findViewById<Button>(R.id.cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.confirm).setOnClickListener {
            finishAffinity()
            exitProcess(0)
        }

        dialog.window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes.gravity = Gravity.START
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
            }
        }
        dialog.show()
    }

    private fun toggleNavigationTitles(visibility: Int) {
        layout.apply {
            navigationMoviesTitle.visibility = visibility
            navigationSeriesTitle.visibility = visibility
            navigationLiveTvTitle.visibility = visibility
            navigationMylistTitle.visibility = visibility
            navigationProfileTitle.visibility = visibility
        }
    }
}