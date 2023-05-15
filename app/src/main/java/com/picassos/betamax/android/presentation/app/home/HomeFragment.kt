package com.picassos.betamax.android.presentation.app.home

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResult
import com.picassos.betamax.android.core.configuration.Config
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleFragmentOwnerStarted
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.FragmentHomeBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.presentation.app.movie.movies_slider.MoviesSliderAdapter
import com.picassos.betamax.android.presentation.app.genre.genres.GenresAdapter
import com.picassos.betamax.android.presentation.app.movie.movies.MoviesAdapter
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.domain.listener.*
import com.picassos.betamax.android.domain.model.ContinueWatching
import com.picassos.betamax.android.domain.model.PlayerContent
import com.picassos.betamax.android.presentation.app.continue_watching.ContinueWatchingAdapter
import com.picassos.betamax.android.presentation.app.continue_watching.ContinueWatchingViewModel
import com.picassos.betamax.android.presentation.app.continue_watching.continue_watching_options.ContinueWatchingOptionsBottomSheetModal
import com.picassos.betamax.android.presentation.app.genre.genres.GenresActivity
import com.picassos.betamax.android.presentation.app.genre.genre_featured_movies.GenreFeaturedMoviesActivity
import com.picassos.betamax.android.presentation.app.genre.genre_movies.GenreMoviesActivity
import com.picassos.betamax.android.presentation.app.movie.movie_player.MoviePlayerActivity
import com.picassos.betamax.android.presentation.app.movie.view_movie.ViewMovieActivity
import com.picassos.betamax.android.presentation.app.profile.ProfileActivity
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var layout: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private val continueWatchingViewModel: ContinueWatchingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
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

        layout.apply {
            brandingText.text = getString(R.string.app_name).lowercase()
            sideNavigation.setOnClickListener {
                showSideNavigation()
            }
        }

        val genresAdapter = GenresAdapter(isSpecial = true, listener = object: OnGenreClickListener {
            override fun onItemClick(genre: Genres.Genre) {
                Intent(requireContext(), GenreMoviesActivity::class.java).also { intent ->
                    intent.putExtra("genre", genre)
                    startActivity(intent)
                }
            }
        })
        layout.recyclerGenres.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = genresAdapter
        }

        val continueWatchingAdapter = ContinueWatchingAdapter(onClickListener = object: OnContinueWatchingClickListener {
            override fun onItemClick(continueWatching: ContinueWatching.ContinueWatching) {
                lifecycleScope.launch {
                    entryPoint.getSubscriptionUseCase().invoke().collect { subscription ->
                        if (subscription.daysLeft == 0) {
                            startActivity(Intent(requireContext(), SubscribeActivity::class.java))
                        } else {
                            Intent(requireContext(), MoviePlayerActivity::class.java).also { intent ->
                                intent.putExtra("playerContent", PlayerContent(
                                    id = continueWatching.contentId,
                                    title = continueWatching.title,
                                    url = continueWatching.url,
                                    thumbnail = continueWatching.thumbnail,
                                    currentPosition = continueWatching.currentPosition))
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }, optionsListener = object: OnContinueWatchingOptionsClickListener {
            override fun onOptionsClick(continueWatching: ContinueWatching.ContinueWatching) {
                val continueWatchingOptionsBottomSheetModal = ContinueWatchingOptionsBottomSheetModal()
                continueWatchingOptionsBottomSheetModal.arguments = Bundle().apply {
                    putInt("contentId", continueWatching.contentId)
                }
                continueWatchingOptionsBottomSheetModal.show(parentFragmentManager, "TAG")
            }
        })
        layout.recyclerContinueWatching.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = continueWatchingAdapter
        }

        val myListAdapter = MoviesAdapter(isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(requireContext(), ViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivityForResult.launch(intent)
                }
            }
        })
        layout.recyclerMylist.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = myListAdapter
        }

        val trendingAdapter = MoviesAdapter(isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(requireContext(), ViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivityForResult.launch(intent)
                }
            }
        })
        layout.recyclerTrending.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }

        val newlyReleaseAdapter = MoviesAdapter(isHorizontal = true, onClickListener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie) {
                Intent(requireContext(), ViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivityForResult.launch(intent)
                }
            }
        })
        layout.recyclerNewlyRelease.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newlyReleaseAdapter
        }

        homeViewModel.requestHomeContent()
        collectLatestOnLifecycleFragmentOwnerStarted(homeViewModel.home) { state ->
            if (state.isLoading) {
                layout.apply {
                    refreshLayout.isRefreshing = true
                    homeContainer.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                layout.refreshLayout.isRefreshing = false

                genresAdapter.differ.submitList(state.response.genres.genres)
                if (state.response.genres.genres.isEmpty()) {
                    layout.recyclerGenres.visibility = View.GONE
                } else {
                    layout.recyclerGenres.visibility = View.VISIBLE
                }

                state.response.featuredMovies.movies.let { movies ->
                    val featuredMoviesSliderAdapter = MoviesSliderAdapter(requireContext(), movies, listener = object: OnMovieClickListener {
                        override fun onItemClick(movie: Movies.Movie) {
                            Intent(requireContext(), ViewMovieActivity::class.java).also { intent ->
                                intent.putExtra("movie", movie)
                                startActivityForResult.launch(intent)
                            }
                        }
                    })
                    layout.viewpagerFeatured.adapter = featuredMoviesSliderAdapter
                    layout.indicatorFeatured.setViewPager(layout.viewpagerFeatured)

                    if (movies.isNotEmpty()) {
                        layout.featuredContainer.visibility = View.VISIBLE
                        layout.viewpagerFeatured.autoScroll(
                            lifecycleScope = viewLifecycleOwner.lifecycleScope,
                            interval = Config.SLIDER_INTERVAL)
                    } else {
                        layout.featuredContainer.visibility = View.GONE
                    }
                }

                myListAdapter.differ.submitList(state.response.myList.movies)
                if (state.response.myList.movies.isEmpty()) {
                    layout.myListContainer.visibility = View.GONE
                } else {
                    layout.myListContainer.visibility = View.VISIBLE
                    layout.viewAllMyList.setOnClickListener {
                        Intent(requireContext(), GenreFeaturedMoviesActivity::class.java).also { intent ->
                            intent.putExtra("type", "mylist")
                            startActivityForResult.launch(intent)
                        }
                    }
                }

                trendingAdapter.differ.submitList(state.response.trendingMovies.movies)
                if (state.response.trendingMovies.movies.isEmpty()) {
                    layout.trendingContainer.visibility = View.GONE
                } else {
                    layout.trendingContainer.visibility = View.VISIBLE
                    layout.viewAllTrending.setOnClickListener {
                        Intent(requireContext(), GenreFeaturedMoviesActivity::class.java).also { intent ->
                            intent.putExtra("type", "trending")
                            startActivityForResult.launch(intent)
                        }
                    }
                }

                newlyReleaseAdapter.differ.submitList(state.response.newlyRelease.movies)
                if (state.response.newlyRelease.movies.isEmpty()) {
                    layout.newlyReleaseContainer.visibility = View.GONE
                } else {
                    layout.newlyReleaseContainer.visibility = View.VISIBLE
                }
            }
            if (state.error != null) {
                layout.apply {
                    refreshLayout.isRefreshing = false
                    homeContainer.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        homeViewModel.requestHomeContent()
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                continueWatchingViewModel.requestContinueWatching()
            }
        }

        collectLatestOnLifecycleFragmentOwnerStarted(continueWatchingViewModel.continueWatching) { state ->
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

        collectLatestOnLifecycleFragmentOwnerStarted(continueWatchingViewModel.deleteContinueWatching) { state ->
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

        layout.refreshLayout.apply {
            elevation = 0f
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.color_theme))
            when (sharedPreferences.loadDarkMode()) {
                1 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_white))
                2 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_darker))
                3 -> {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {Configuration.UI_MODE_NIGHT_YES -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_darker))
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_white))
                    }
                }
            }
            setOnRefreshListener {
                homeViewModel.requestHomeContent()
            }
        }
    }

    private fun showSideNavigation() {
        val dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_side_navigation)
            setCancelable(true)
            setOnCancelListener {
                dismiss()
            }
        }

        dialog.findViewById<ImageView>(R.id.dialog_close).setOnClickListener {
            dialog.dismiss()
        }

        val genresAdapter = GenresAdapter(isSpecial = false, listener = object: OnGenreClickListener {
            override fun onItemClick(genre: Genres.Genre) {
                Intent(requireContext(), GenreMoviesActivity::class.java).also { intent ->
                    intent.putExtra("genre", genre)
                    startActivity(intent)
                }
            }
        })
        dialog.findViewById<RecyclerView>(R.id.recycler_genres).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = genresAdapter
        }

        dialog.findViewById<RelativeLayout>(R.id.view_all_genres).setOnClickListener {
            startActivity(Intent(requireContext(), GenresActivity::class.java))
        }

        homeViewModel.requestGenres()
        collectLatestOnLifecycleFragmentOwnerStarted(homeViewModel.genres) { state ->
            if (state.response != null) {
                genresAdapter.differ.submitList(state.response.genres)
            }
        }

        dialog.window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes.gravity = Gravity.START
                setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
            }
        }
        dialog.show()
    }

    private fun ViewPager.autoScroll(lifecycleScope: LifecycleCoroutineScope, interval: Long) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                scrollIndefinitely(interval)
            }
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

    private var startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if (result != null && result.resultCode == AppCompatActivity.RESULT_OK) {
            result.data?.let { data ->
                if (data.getBooleanExtra("refreshContent", false)) {
                    homeViewModel.requestHomeContent()
                }
            }
        }
    }
}