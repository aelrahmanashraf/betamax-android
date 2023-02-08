package com.picassos.betamax.android.presentation.app.genre.genre_movies

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityGenreMoviesBinding
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.presentation.app.movie.movies.MoviesAdapter
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.presentation.app.movie.view_movie.ViewMovieActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenreMoviesActivity : AppCompatActivity() {
    private lateinit var layout: ActivityGenreMoviesBinding
    private val genreMoviesViewModel: GenreMoviesViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var genre: Genres.Genre

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = SharedPreferences(this)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_genre_movies)

        val requestDialog = RequestDialog(this)

        layout.goBack.setOnClickListener { finish() }

        getSerializable(this@GenreMoviesActivity, "genre", Genres.Genre::class.java).also { genre ->
            genreMoviesViewModel.apply {
                setGenre(genre)
                requestGenreMovies(genre.genreId)
                layout.toolbarTitle.text = genre.title
            }
        }

        collectLatestOnLifecycleStarted(genreMoviesViewModel.genre) { isSafeGenre ->
            isSafeGenre?.let { genre ->
                this.genre = genre
            }
        }

        val moviesAdapter = MoviesAdapter(listener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie?) {
                Intent(this@GenreMoviesActivity, ViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        })
        layout.recyclerMovies.apply {
            layoutManager = GridLayoutManager(this@GenreMoviesActivity, 3)
            adapter = moviesAdapter
        }

        collectLatestOnLifecycleStarted(genreMoviesViewModel.movies) { state ->
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
                        genreMoviesViewModel.requestGenreMovies(genre.genreId)
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        layout.refreshLayout.apply {
            elevation = 0f
            setColorSchemeColors(ContextCompat.getColor(this@GenreMoviesActivity, R.color.color_theme))
            when (sharedPreferences.loadDarkMode()) {
                1 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenreMoviesActivity, R.color.color_white))
                2 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenreMoviesActivity, R.color.color_darker))
                3 -> {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenreMoviesActivity, R.color.color_darker))
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenreMoviesActivity, R.color.color_white))
                    }
                }
            }
            setOnRefreshListener {
                if (isRefreshing) {
                    isRefreshing = false
                }
                genreMoviesViewModel.requestGenreMovies(genre.genreId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@GenreMoviesActivity)
    }
}