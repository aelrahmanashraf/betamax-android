package com.picassos.betamax.android.presentation.app.season.seasons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleSheetOwnerStarted
import com.picassos.betamax.android.databinding.SeasonsBottomSheetModalBinding
import com.picassos.betamax.android.domain.listener.OnSeasonClickListener
import com.picassos.betamax.android.domain.model.Seasons
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeasonsBottomSheetModal : BottomSheetDialogFragment() {
    private lateinit var layout: SeasonsBottomSheetModalBinding
    private val seasonsViewModel: SeasonsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.seasons_bottom_sheet_modal, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val seasonsAdapter = SeasonsAdapter(listener = object: OnSeasonClickListener {
            override fun onItemClick(season: Seasons.Season) {
                seasonsViewModel.setSelectedSeason(season)
                dismiss()
            }
        })
        layout.recyclerSeasons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = seasonsAdapter
        }

        seasonsViewModel.requestSeasons(requireArguments().getInt("movie_id"))
        collectLatestOnLifecycleSheetOwnerStarted(seasonsViewModel.seasons) { state ->
            if (state.isLoading) {
                layout.apply {
                    progressbar.visibility = View.GONE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                layout.progressbar.visibility = View.GONE
                seasonsAdapter.differ.submitList(state.response.seasons)
            }
            if (state.error != null) {
                layout.apply {
                    progressbar.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        seasonsViewModel.requestSeasons(requireArguments().getInt("movie_id"))
                    }
                }
            }
        }
    }
}