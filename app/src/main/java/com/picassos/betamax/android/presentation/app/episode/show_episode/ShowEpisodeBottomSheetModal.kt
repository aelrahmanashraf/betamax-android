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
import androidx.lifecycle.lifecycleScope
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ShowEpisodeBottomSheetModalBinding
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.core.utilities.Helper.getBundleSerializable
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.model.EpisodePlayerContent
import com.picassos.betamax.android.presentation.app.episode.episode_player.EpisodePlayerActivity
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShowEpisodeBottomSheetModal : BottomSheetDialogFragment() {
    interface OnEpisodeBottomSheetDismissedListener {
        fun onEpisodesBottomSheetDismissed()
    }

    private lateinit var layout: ShowEpisodeBottomSheetModalBinding
    private val showEpisodeViewModel: ShowEpisodeViewModel by activityViewModels()

    private lateinit var movie: Movies.Movie
    private lateinit var episodes: Episodes
    private lateinit var episode: Episodes.Episode

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.show_episode_bottom_sheet_modal, container, false)
        return layout.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(requireContext(), AppEntryPoint::class.java)

        getBundleSerializable(requireArguments(), "movie", Movies.Movie::class.java).also { movie ->
            this@ShowEpisodeBottomSheetModal.movie = movie

            layout.apply {
                episodeReleaseDate.text = Helper.getFormattedDateString(movie.date, "yyyy")
            }
        }

        getBundleSerializable(requireArguments(), "episodes", Episodes::class.java).also { episodes ->
            this@ShowEpisodeBottomSheetModal.episodes = episodes
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
            layout.playEpisode.setOnClickListener {
                lifecycleScope.launch {
                    entryPoint.getSubscriptionUseCase().invoke().collect { subscription ->
                        if (subscription.daysLeft == 0) {
                            startActivity(Intent(requireContext(), SubscribeActivity::class.java))
                        } else {
                            Intent(requireContext(), EpisodePlayerActivity::class.java).also { intent ->
                                intent.putExtra("playerContent", EpisodePlayerContent(
                                    movie = movie,
                                    episode = episode,
                                    episodes = episodes,
                                    currentPosition = episode.currentPosition ?: 0))
                                startActivity(intent)
                            }
                        }
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