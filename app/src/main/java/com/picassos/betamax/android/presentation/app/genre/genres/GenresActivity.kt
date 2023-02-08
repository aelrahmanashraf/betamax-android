package com.picassos.betamax.android.presentation.app.genre.genres

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityGenresBinding
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.domain.listener.OnGenreClickListener
import com.picassos.betamax.android.presentation.app.genre.genre_movies.GenreMoviesActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenresActivity : AppCompatActivity() {
    private lateinit var layout: ActivityGenresBinding
    private val genresViewModel: GenresViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = SharedPreferences(this)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_genres)

        val requestDialog = RequestDialog(this)

        layout.goBack.setOnClickListener { finish() }

        val genresAdapter = GenresAdapter (isSpecial = false, listener = object: OnGenreClickListener {
            override fun onItemClick(genre: Genres.Genre) {
                Intent(this@GenresActivity, GenreMoviesActivity::class.java).also { intent ->
                    intent.putExtra("genre", genre)
                    startActivity(intent)
                }
            }
        })
        layout.recyclerGenres.apply {
            layoutManager = LinearLayoutManager(this@GenresActivity)
            adapter = genresAdapter
        }

        genresViewModel.requestAllGenres()
        collectLatestOnLifecycleStarted(genresViewModel.genres) { state ->
            if (state.isLoading) {
                requestDialog.show()
                layout.apply {
                    recyclerGenres.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                requestDialog.dismiss()

                val categories = state.response.genres
                genresAdapter.differ.submitList(categories)
                if (categories.isEmpty()) {
                    layout.noItems.visibility = View.VISIBLE
                } else {
                    layout.noItems.visibility = View.GONE
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                layout.apply {
                    recyclerGenres.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        genresViewModel.requestAllGenres()
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        layout.refreshLayout.apply {
            elevation = 0f
            setColorSchemeColors(ContextCompat.getColor(this@GenresActivity, R.color.color_theme))
            when (sharedPreferences.loadDarkMode()) {
                1 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenresActivity, R.color.color_white))
                2 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenresActivity, R.color.color_darker))
                3 -> {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenresActivity, R.color.color_darker))
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this@GenresActivity, R.color.color_white))
                    }
                }
            }
            setOnRefreshListener {
                if (isRefreshing) {
                    isRefreshing = false
                }
                genresViewModel.requestAllGenres()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@GenresActivity)
    }
}