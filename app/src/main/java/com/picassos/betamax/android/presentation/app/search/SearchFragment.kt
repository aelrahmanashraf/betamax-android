package com.picassos.betamax.android.presentation.app.search

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import com.picassos.betamax.android.R
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.picassos.betamax.android.core.utilities.Helper
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleFragmentOwnerStarted
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.databinding.FragmentSearchBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.presentation.app.movie.movies.MoviesAdapter
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.domain.listener.OnMovieClickListener
import com.picassos.betamax.android.presentation.app.movie.view_movie.ViewMovieActivity
import com.picassos.betamax.android.presentation.app.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.lang.Exception
import java.util.*

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private lateinit var layout: FragmentSearchBinding
    private val searchViewModel: SearchViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
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

        val moviesAdapter = MoviesAdapter(listener = object: OnMovieClickListener {
            override fun onItemClick(movie: Movies.Movie?) {
                Intent(requireContext(), ViewMovieActivity::class.java).also { intent ->
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
            }
        })
        layout.recyclerSearch.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = moviesAdapter
        }

        collectLatestOnLifecycleFragmentOwnerStarted(searchViewModel.movies) { state ->
            if (state.isLoading) {
                requestDialog.show()
                layout.apply {
                    recyclerSearch.visibility = View.VISIBLE
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
                    recyclerSearch.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        searchViewModel.requestSearchMovies(layout.searchBar.text.toString())
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        layout.searchBar.setOnEditorActionListener { _: TextView?, actionId: Int, event: KeyEvent? ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                if (layout.searchBar.text.toString().isNotEmpty()) {
                    searchViewModel.requestSearchMovies(layout.searchBar.text.toString())
                }
                val inputMethodManager = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
            }
            false
        }

        layout.voiceSearch.setOnClickListener {
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also { intent ->
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_help))
                try {
                    startActivityForResult.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
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
                searchViewModel.requestSearchMovies(layout.searchBar.text.toString())
            }
        }
    }

    private var startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if (result != null && result.resultCode == RESULT_OK) {
            val callback = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (callback != null) {
                layout.searchBar.setText(callback[0])
                searchViewModel.requestSearchMovies(layout.searchBar.text.toString())
            }
        }
    }
}