package com.picassos.betamax.android.presentation.app.genre.genre_featured_movies

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityGenreFeaturedMoviesBinding
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.presentation.app.movie.movies.MoviesAdapter
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.presentation.app.movie.view_movie.ViewMovieActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenreFeaturedMoviesActivity : AppCompatActivity() {
    private lateinit var layout: ActivityGenreFeaturedMoviesBinding
    private val genreFeaturedMoviesViewModel: GenreFeaturedMoviesViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = SharedPreferences(this)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_genre_featured_movies)

        val requestDialog = RequestDialog(this)

        layout.goBack.setOnClickListener {
            if (intent.getStringExtra("type") == "mylist") {
                Intent().also { intent ->
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            } else {
                finish()
            }
        }

        intent.getStringExtra("type")?.let { type ->
            makeRequest(type)
        }

        val moviesAdapter = MoviesAdapter(onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(this@GenreFeaturedMoviesActivity, ViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivityForResult.launch(intent)
                }
            }
        })
        layout.recyclerMovies.apply {
            layoutManager = GridLayoutManager(this@GenreFeaturedMoviesActivity, 3)
            adapter = moviesAdapter
        }

        collectLatestOnLifecycleStarted(genreFeaturedMoviesViewModel.movies) { state ->
            if (state.isLoading) {
                requestDialog.show()
                layout.apply {
                    recyclerMovies.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                requestDialog.dismiss()

                val movies = state.response.movies
                moviesAdapter.differ.submitList(movies)
                if (movies.isEmpty()) {
                    layout.noItems.visibility = View.VISIBLE
                } else {
                    layout.noItems.visibility = View.GONE
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                layout.apply {
                    recyclerMovies.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        intent.getStringExtra("type")?.let { type ->
                            makeRequest(type)
                        }
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        layout.refreshLayout.apply {
            elevation = 0f
            setColorSchemeColors(ContextCompat.getColor(this@GenreFeaturedMoviesActivity, R.color.color_theme))
            when (sharedPreferences.loadDarkMode()) {
                1 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenreFeaturedMoviesActivity, R.color.color_white))
                2 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenreFeaturedMoviesActivity, R.color.color_darker))
                3 -> {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenreFeaturedMoviesActivity, R.color.color_darker))
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenreFeaturedMoviesActivity, R.color.color_white))
                    }
                }
            }
            setOnRefreshListener {
                if (isRefreshing) {
                    isRefreshing = false
                }
                intent.getStringExtra("type")?.let { type ->
                    makeRequest(type)
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (intent.getStringExtra("type") == "mylist") {
                    Intent().also { intent ->
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun makeRequest(requestType: String) {
        when (requestType) {
            "movies" -> {
                genreFeaturedMoviesViewModel.requestMovies()
                layout.toolbarTitle.text = getString(R.string.movies)
            }
            "series" -> {
                genreFeaturedMoviesViewModel.requestSeries()
                layout.toolbarTitle.text = getString(R.string.series)
            }
            "mylist" -> {
                genreFeaturedMoviesViewModel.requestSavedMovies()
                layout.toolbarTitle.text = getString(R.string.mylist)
            }
            "trending" -> {
                genreFeaturedMoviesViewModel.requestTrendingMovies()
                layout.toolbarTitle.text = getString(R.string.trending)
            }
        }
    }

    private var startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if (result != null && result.resultCode == RESULT_OK) {
            if (intent.getStringExtra("type") == "mylist") {
                genreFeaturedMoviesViewModel.requestSavedMovies()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@GenreFeaturedMoviesActivity)
    }
}