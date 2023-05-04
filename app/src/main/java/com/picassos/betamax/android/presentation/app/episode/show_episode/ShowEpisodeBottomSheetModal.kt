package com.picassos.betamax.android.presentation.app.episode.show_episode

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleSheetOwnerStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ShowEpisodeBottomSheetModalBinding
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.core.utilities.Helper.getBundleSerializable
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.domain.model.PlayerContent
import com.picassos.betamax.android.presentation.app.movie.movie_player.MoviePlayerActivity
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowEpisodeBottomSheetModal : BottomSheetDialogFragment() {
    interface OnEpisodeBottomSheetDismissedListener {
        fun onEpisodesBottomSheetDismissed()
    }

    private lateinit var layout: ShowEpisodeBottomSheetModalBinding
    private val showEpisodeViewModel: ShowEpisodeViewModel by activityViewModels()

    private lateinit var episode: Episodes.Episode

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.show_episode_bottom_sheet_modal, container, false)
        return layout.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val requestDialog = RequestDialog(requireContext())

        getBundleSerializable(requireArguments(), "movie", Movies.Movie::class.java).also { movie ->
            layout.apply {
                episodeReleaseDate.text = Helper.getFormattedDateString(movie.date, "yyyy")
            }
        }

        getBundleSerializable(requireArguments(), "episode", Episodes.Episode::class.java).also { episode ->
            this@ShowEpisodeBottomSheetModal.episode = episode

            layout.apply {
                episodeTitle.text = episode.title
                episodeDuration.text = "${episode.duration}min"
                episodeThumbnail.controller = Fresco.newDraweeControllerBuilder()
                    .setTapToRetryEnabled(true)
                    .setUri(episode.thumbnail)
                    .build()
            }
            layout.playEpisode.apply {
                setOnClickListener {
                    showEpisodeViewModel.requestCheckSubscription()
                }
            }
        }

        collectLatestOnLifecycleSheetOwnerStarted(showEpisodeViewModel.checkSubscription) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.response != null) {
                requestDialog.dismiss()

                val subscription = state.response
                when (subscription.daysLeft) {
                    0 -> startActivity(Intent(requireContext(), SubscribeActivity::class.java))
                    else -> {
                        Intent(requireContext(), MoviePlayerActivity::class.java).also { intent ->
                            intent.putExtra("playerContent", PlayerContent(
                                id = episode.episodeId,
                                title = episode.title,
                                url = episode.url,
                                meta = "${getString(R.string.season)} ${episode.seasonLevel}",
                                thumbnail = episode.thumbnail,
                                currentPosition = episode.currentPosition ?: 0))
                            startActivity(intent)
                        }
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(requireContext(), getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (activity is OnEpisodeBottomSheetDismissedListener) {
            (activity as OnEpisodeBottomSheetDismissedListener).onEpisodesBottomSheetDismissed()
        }
    }
}