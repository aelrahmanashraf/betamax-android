package com.picassos.betamax.android.presentation.app.movie.series

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import com.picassos.betamax.android.R
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleFragmentOwnerStarted
import com.picassos.betamax.android.databinding.FragmentSeriesBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.presentation.app.movie.movies.MoviesAdapter
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.presentation.app.movie.view_movie.ViewMovieActivity
import com.picassos.betamax.android.presentation.app.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class SeriesFragment : Fragment() {
    private lateinit var layout: FragmentSeriesBinding
    private val seriesViewModel: SeriesViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_series, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = SharedPreferences(requireContext())
        val entryPoint = EntryPointAccessors.fromApplication(requireContext(), AppEntryPoint::class.java)

        val requestDialog = RequestDialog(requireContext())

        collectLatestOnLifecycleFragmentOwnerStarted(entryPoint.getAccountUseCase().invoke()) { account ->
            layout.profileIcon.apply {
                text = Helper.characterIcon(account.username).uppercase()
                setOnClickListener {
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                }
            }
        }

        val seriesAdapter = MoviesAdapter(listener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie?) {
                Intent(requireContext(), ViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        })
        layout.recyclerSeries.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = seriesAdapter
        }

        seriesViewModel.requestSeries()
        collectLatestOnLifecycleFragmentOwnerStarted(seriesViewModel.series) { state ->
            if (state.isLoading) {
                requestDialog.show()
                layout.apply {
                    recyclerSeries.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                requestDialog.dismiss()

                val movies = state.response.movies
                seriesAdapter.differ.submitList(movies)
                if (movies.isEmpty()) {
                    layout.noItems.visibility = View.VISIBLE
                } else {
                    layout.noItems.visibility = View.GONE
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                layout.apply {
                    recyclerSeries.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        seriesViewModel.requestSeries()
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        layout.refreshLayout.apply {
            elevation = 0f
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.color_theme))
            when (sharedPreferences.loadDarkMode()) {
                1 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_white))
                2 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_darker))
                3 -> {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_darker))
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_white))
                    }
                }
            }
            setOnRefreshListener {
                if (isRefreshing) {
                    isRefreshing = false
                }
                seriesViewModel.requestSeries()
            }
        }
    }
}