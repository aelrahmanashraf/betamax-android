package com.picassos.betamax.android.presentation.television.mylist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ActivityTelevisionMylistBinding
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.listener.OnMovieFocusListener
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.presentation.television.movie.movies.TelevisionMoviesAdapter
import com.picassos.betamax.android.presentation.television.movie.view_movie.TelevisionViewMovieActivity
import com.picassos.betamax.android.presentation.television.tvchannel.saved_tvchannels.TelevisionSavedTvChannelsAdapter
import com.picassos.betamax.android.presentation.television.tvchannel.tvchannels.TelevisionTvChannelsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TelevisionMyListActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionMylistBinding
    private val televisionMyListViewModel: TelevisionMyListViewModel by viewModels()

    private var filter = FILTER_MOVIES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_mylist)

        layout.apply {
            brandingText.text = getString(R.string.app_name).lowercase()
        }

        layout.apply {
            myMovies.setOnClickListener {
                filter = FILTER_MOVIES
                mylistTitle.text = getString(R.string.my_movies)
                televisionMyListViewModel.requestSavedMovies()
            }
            myTvchannels.setOnClickListener {
                filter = FILTER_TVCHANNELS
                mylistTitle.text = getString(R.string.my_tvchannels)
                televisionMyListViewModel.requestSavedTvChannels()
            }
        }

        val moviesAdapter = TelevisionMoviesAdapter(isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(this@TelevisionMyListActivity, TelevisionViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        }, onFocusListener = object: OnMovieFocusListener {
            override fun onItemFocus(movie: Movies.Movie, position: Int) {

            }
        })
        layout.recyclerMovies.apply {
            layoutManager = GridLayoutManager(this@TelevisionMyListActivity, 4)
            adapter = moviesAdapter
        }

        val tvChannelsAdapter = TelevisionSavedTvChannelsAdapter(object: OnTvChannelClickListener {
            override fun onItemClick(tvChannel: TvChannels.TvChannel) {
                Intent(this@TelevisionMyListActivity, TelevisionTvChannelsActivity::class.java).also { intent ->
                    intent.putExtra("tvChannel", tvChannel)
                    startActivity(intent)
                }
            }
        })
        layout.recyclerTv.apply {
            layoutManager = LinearLayoutManager(this@TelevisionMyListActivity)
            adapter = tvChannelsAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (filter) {
                    FILTER_MOVIES -> televisionMyListViewModel.requestSavedMovies()
                    FILTER_TVCHANNELS -> televisionMyListViewModel.requestSavedTvChannels()
                }
            }
        }

        collectLatestOnLifecycleStarted(televisionMyListViewModel.movies) { state ->
            if (state.isLoading) {
                layout.apply {
                    mylistProgressbar.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
                layout.apply {
                    recyclerMovies.visibility = View.GONE
                    recyclerTv.visibility = View.GONE
                }
            }
            if (state.response != null) {
                layout.apply {
                    mylistProgressbar.visibility = View.GONE
                    recyclerMovies.visibility = View.VISIBLE
                }

                moviesAdapter.differ.submitList(state.response.movies)
                if (state.response.movies.isEmpty()) {
                    layout.noItems.visibility = View.VISIBLE
                } else {
                    layout.noItems.visibility = View.GONE
                }
            }
            if (state.error != null) {
                layout.apply {
                    mylistProgressbar.visibility = View.GONE
                    recyclerMovies.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        televisionMyListViewModel.requestSavedMovies()
                    }
                }
            }
        }

        collectLatestOnLifecycleStarted(televisionMyListViewModel.tvChannels) { state ->
            if (state.isLoading) {
                layout.apply {
                    mylistProgressbar.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
                layout.apply {
                    recyclerMovies.visibility = View.GONE
                    recyclerTv.visibility = View.GONE
                }
            }
            if (state.response != null) {
                layout.apply {
                    mylistProgressbar.visibility = View.GONE
                    recyclerTv.visibility = View.VISIBLE
                }

                tvChannelsAdapter.differ.submitList(state.response.tvChannels)
                if (state.response.tvChannels.isEmpty()) {
                    layout.noItems.visibility = View.VISIBLE
                } else {
                    layout.noItems.visibility = View.GONE
                }
            }
            if (state.error != null) {
                layout.apply {
                    mylistProgressbar.visibility = View.GONE
                    recyclerTv.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        televisionMyListViewModel.requestSavedTvChannels()
                    }
                }
            }
        }
    }

    companion object {
        const val FILTER_MOVIES = "movies"
        const val FILTER_TVCHANNELS = "tv_channels"
    }
}