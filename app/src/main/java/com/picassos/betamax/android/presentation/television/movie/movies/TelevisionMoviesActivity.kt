package com.picassos.betamax.android.presentation.television.movie.movies

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ActivityTelevisionMoviesBinding
import com.picassos.betamax.android.domain.listener.OnGenreClickListener
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.listener.OnMovieFocusListener
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.presentation.television.genre.genres.TelevisionGenresAdapter
import com.picassos.betamax.android.presentation.television.movie.view_movie.TelevisionViewMovieActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TelevisionMoviesActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionMoviesBinding
    private val televisionMoviesViewModel: TelevisionMoviesViewModel by viewModels()

    private var request = ""
    private var selectedGenre = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_movies)

        layout.apply {
            brandingText.text = getString(R.string.app_name).lowercase()
        }

        intent.getStringExtra("request")?.let { request ->
            this@TelevisionMoviesActivity.request = request
        }

        layout.apply {
            searchBar.setOnClickListener {
                showSearchDialog()
            }
            newlyRelease.setOnClickListener {
                selectedGenre = 1
                sectionTitle.text = getString(R.string.newly_release)
                televisionMoviesViewModel.requestNewlyReleaseMovies(filter = request)
            }
            trending.setOnClickListener {
                selectedGenre = 2
                sectionTitle.text = getString(R.string.trending)
                televisionMoviesViewModel.requestTrendingMovies(filter = request)
            }
        }

        val genresAdapter = TelevisionGenresAdapter(listener = object: OnGenreClickListener {
            override fun onItemClick(genre: Genres.Genre) {
                selectedGenre = genre.genreId
                layout.sectionTitle.text = genre.title
                televisionMoviesViewModel.requestMoviesByGenre(
                    genreId = genre.genreId,
                    filter = request)
            }
        })
        layout.recyclerGenres.apply {
            layoutManager = LinearLayoutManager(this@TelevisionMoviesActivity)
            adapter = genresAdapter
        }

        val moviesAdapter = TelevisionMoviesAdapter(isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(this@TelevisionMoviesActivity, TelevisionViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        }, onFocusListener = object: OnMovieFocusListener {
            override fun onItemFocus(movie: Movies.Movie) {

            }
        })
        layout.recyclerMovies.apply {
            layoutManager = GridLayoutManager(this@TelevisionMoviesActivity, 4)
            adapter = moviesAdapter
        }

        televisionMoviesViewModel.requestGenres()
        collectLatestOnLifecycleStarted(televisionMoviesViewModel.genres) { state ->
            if (state.isLoading) {
                layout.genresProgressbar.visibility = View.VISIBLE
            }
            if (state.response != null) {
                layout.genresProgressbar.visibility = View.GONE
                genresAdapter.differ.submitList(state.response.genres)
            }
        }

        televisionMoviesViewModel.requestNewlyReleaseMovies(filter = request)
        collectLatestOnLifecycleStarted(televisionMoviesViewModel.movies) { state ->
            if (state.isLoading) {
                layout.apply {
                    moviesProgressbar.visibility = View.VISIBLE
                    recyclerMovies.visibility = View.GONE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                layout.apply {
                    moviesProgressbar.visibility = View.GONE
                    recyclerMovies.visibility = View.VISIBLE
                }
                moviesAdapter.differ.submitList(state.response.movies)
            }
            if (state.error != null) {
                layout.apply {
                    moviesProgressbar.visibility = View.GONE
                    recyclerMovies.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        televisionMoviesViewModel.apply {
                            requestGenres()
                            when (selectedGenre) {
                                1 -> { requestNewlyReleaseMovies(filter = request) }
                                2 -> { requestTrendingMovies(filter = request) }
                                else -> requestMoviesByGenre(
                                    genreId = selectedGenre,
                                    filter = request)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showSearchDialog() {
        val dialog = Dialog(this@TelevisionMoviesActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setContentView(R.layout.dialog_television_search)
        }

        val searchBar = dialog.findViewById<EditText>(R.id.search_bar)
        searchBar.setOnEditorActionListener { _: TextView?, actionId: Int, event: KeyEvent? ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                if (searchBar.toString().isNotEmpty()) {
                    televisionMoviesViewModel.requestSearchMovies(
                        query = searchBar.text.toString(),
                        filter = request)
                    layout.searchBarText.text = searchBar.text.toString()
                    dialog.dismiss()
                }
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }
            false
        }

        dialog.window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
                attributes.gravity = Gravity.CENTER
                attributes.dimAmount = 0.0f
            }
        }

        dialog.show()
    }
}