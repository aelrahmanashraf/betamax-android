package com.picassos.betamax.android.presentation.television.main

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.databinding.ActivityTelevisionMainBinding
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.presentation.app.profile.ProfileActivity
import com.picassos.betamax.android.presentation.television.movie.movies.TelevisionMoviesActivity
import com.picassos.betamax.android.presentation.television.movie.movies_slider.TelevisionMoviesSliderAdapter
import com.picassos.betamax.android.presentation.television.movie.view_movie.TelevisionViewMovieActivity
import com.picassos.betamax.android.presentation.television.mylist.TelevisionMyListActivity
import com.picassos.betamax.android.presentation.television.tvchannel.tvchannels.TelevisionTvChannelsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.system.exitProcess

@AndroidEntryPoint
class TelevisionMainActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionMainBinding
    private val televisionMainViewModel: TelevisionMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_television_main)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_main)

        layout.apply {
            movies.setOnClickListener {
                Intent(this@TelevisionMainActivity, TelevisionMoviesActivity::class.java).also { intent ->
                    intent.putExtra("request", "movies")
                    startActivity(intent)
                }
            }
            series.setOnClickListener {
                Intent(this@TelevisionMainActivity, TelevisionMoviesActivity::class.java).also { intent ->
                    intent.putExtra("request", "series")
                    startActivity(intent)
                }
            }
            tv.setOnClickListener {
                startActivity(Intent(this@TelevisionMainActivity, TelevisionTvChannelsActivity::class.java))
            }
            mylist.setOnClickListener {
                startActivity(Intent(this@TelevisionMainActivity, TelevisionMyListActivity::class.java))
            }
            configuration.setOnClickListener {
                startActivity(Intent(this@TelevisionMainActivity, ProfileActivity::class.java))
            }
        }

        televisionMainViewModel.requestFeaturedMovies()
        collectLatestOnLifecycleStarted(televisionMainViewModel.moviesSlider) { state ->
            if (state.response != null) {
                val movies = state.response.movies

                val featuredMoviesSliderAdapter = TelevisionMoviesSliderAdapter(this@TelevisionMainActivity, movies, listener = object: OnMovieClickListener {
                    override fun onItemClick(movie: Movies.Movie?) {
                        Intent(this@TelevisionMainActivity, TelevisionViewMovieActivity::class.java).also { intent ->
                            intent.putExtra("movie", movie)
                            startActivity(intent)
                        }
                    }
                })
                layout.viewpagerFeatured.adapter = featuredMoviesSliderAdapter

                if (movies.isNotEmpty()) {
                    layout.viewpagerFeatured.autoScroll(
                        lifecycleScope = lifecycleScope,
                        interval = Config.TV_SLIDER_INTERVAL)
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        })
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

    private fun ViewPager.autoScroll(lifecycleScope: LifecycleCoroutineScope, interval: Long) {
        lifecycleScope.launchWhenResumed {
            scrollIndefinitely(interval)
        }
    }

    private suspend fun ViewPager.scrollIndefinitely(interval: Long) {
        delay(interval)
        val numberOfItems = adapter?.count ?: 0
        val lastIndex = if (numberOfItems > 0) numberOfItems - 1 else 0
        val nextItem = if (currentItem == lastIndex) 0 else currentItem + 1
        setCurrentItem(nextItem, true)

        scrollIndefinitely(interval)
    }
}