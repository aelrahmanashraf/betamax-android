package com.picassos.betamax.android.presentation.television.tvchannel.tvchannels

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.databinding.ActivityTelevisionTvchannelsBinding
import com.picassos.betamax.android.domain.listener.OnGenreClickListener
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.model.TelevisionPlayerContent
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.presentation.app.player.PlayerStatus
import com.picassos.betamax.android.presentation.app.player.PlayerViewModel
import com.picassos.betamax.android.presentation.television.genre.tvchannels_genres.TelevisionTvChannelsGenresAdapter
import com.picassos.betamax.android.presentation.television.tvchannel.tvchannel_player.TelevisionTvChannelPlayerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TelevisionTvChannelsActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionTvchannelsBinding
    private val televisionTvChannelsViewModel: TelevisionTvChannelsViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    private var exoPlayer: ExoPlayer? = null

    private lateinit var tvChannelsList: List<TvChannels.TvChannel>
    private var tvChannel: TvChannels.TvChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_tvchannels)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        layout.apply {
            brandingText.text = getString(R.string.app_name).lowercase()
        }

        layout.apply {
            tvchannelsGenres.setOnClickListener {
                showTvChannelsGenres()
            }
        }

        getSerializable(this@TelevisionTvChannelsActivity, "tvChannel", TvChannels.TvChannel::class.java).also { tvChannel ->
            if (tvChannel != null) {
                televisionTvChannelsViewModel.apply {
                    this@TelevisionTvChannelsActivity.tvChannel = tvChannel
                    requestTvChannel(tvChannel.tvChannelId)
                }
            }
        }

        val tvChannelsAdapter = TelevisionTvChannelsAdapter(listener = object: OnTvChannelClickListener {
            override fun onItemClick(tvChannel: TvChannels.TvChannel) {
                this@TelevisionTvChannelsActivity.tvChannel?.let { channel ->
                    if (channel.tvChannelId != tvChannel.tvChannelId) {
                        televisionTvChannelsViewModel.apply {
                            this@TelevisionTvChannelsActivity.tvChannel = tvChannel
                            requestTvChannel(tvChannel.tvChannelId)
                        }
                    } else {
                        exoPlayer?.apply {
                            if (isPlaying) {
                                Intent(this@TelevisionTvChannelsActivity, TelevisionTvChannelPlayerActivity::class.java).also { intent ->
                                    intent.putExtra("playerContent", TelevisionPlayerContent(
                                        url = tvChannel.hdUrl,
                                        userAgent = tvChannel.userAgent,
                                        currentTvChannelPosition = tvChannel.position,
                                        tvChannelsList = tvChannelsList))
                                    startActivity(intent)
                                }
                            } else {
                                play()
                            }
                        }
                    }
                } ?: run {
                    televisionTvChannelsViewModel.apply {
                        this@TelevisionTvChannelsActivity.tvChannel = tvChannel
                        requestTvChannel(tvChannel.tvChannelId)
                    }
                }
            }
        })
        layout.recyclerTv.apply {
            layoutManager = LinearLayoutManager(this@TelevisionTvChannelsActivity)
            adapter = tvChannelsAdapter
        }

        televisionTvChannelsViewModel.requestTvChannels()
        collectLatestOnLifecycleStarted(televisionTvChannelsViewModel.tvChannels) { state ->
            if (state.isLoading) {
                layout.tvchannelsProgressbar.visibility = View.VISIBLE
            }
            if (state.response != null) {
                tvChannelsList = state.response.tvChannels

                layout.tvchannelsProgressbar.visibility = View.GONE
                tvChannelsAdapter.differ.submitList(state.response.tvChannels)
            }
        }

        collectLatestOnLifecycleStarted(televisionTvChannelsViewModel.viewTvChannel) { state ->
            if (state.isLoading) {
                exoPlayer?.apply {
                    playWhenReady = false
                    pause()
                    clearMediaItems()
                }
            }
            if (state.response != null) {
                val tvChannelDetails = state.response.tvChannelDetails.tvChannels[0]

                initializePlayer(
                    url = tvChannelDetails.hdUrl,
                    userAgent = tvChannelDetails.userAgent)

                layout.tvchannelTitle.text = tvChannelDetails.title
                layout.tvchannelDetails.apply {
                    visibility = View.VISIBLE
                }

                layout.apply {
                    saveTvchannel.setOnClickListener {
                        televisionTvChannelsViewModel.requestSaveTvChannel(tvChannelDetails.tvChannelId)
                    }
                    saveTvchannelIcon.apply {
                        when (state.response.tvChannelSaved) {
                            1 -> {
                                setImageResource(R.drawable.icon_star_filled)
                            }
                            else -> {
                                setImageResource(R.drawable.icon_star_outline)
                            }
                        }
                    }
                }
            }
            if (state.error != null) {

            }
        }

        collectLatestOnLifecycleStarted(televisionTvChannelsViewModel.saveTvChannel) { state ->
            if (state.responseCode != null) {
                when (state.responseCode) {
                    "1" -> layout.saveTvchannelIcon.setImageResource(R.drawable.icon_star_filled)
                    "0" -> layout.saveTvchannelIcon.setImageResource(R.drawable.icon_star_outline)
                    else -> showToast(this@TelevisionTvChannelsActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                }
            }
            if (state.error != null) {
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@TelevisionTvChannelsActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@TelevisionTvChannelsActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@TelevisionTvChannelsActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    private fun showTvChannelsGenres() {
        val dialog = Dialog(this@TelevisionTvChannelsActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_television_tvchannels_genres)
            setCancelable(true)
            setOnCancelListener {
                dismiss()
            }
        }

        dialog.findViewById<LinearLayout>(R.id.all_tvchannels).setOnClickListener {
            televisionTvChannelsViewModel.requestTvChannels()
            dialog.dismiss()
        }

        val genresAdapter = TelevisionTvChannelsGenresAdapter(listener = object: OnGenreClickListener {
            override fun onItemClick(genre: Genres.Genre) {
                televisionTvChannelsViewModel.requestTvChannelsByGenre(genre.genreId)
                dialog.dismiss()
            }
        })
        dialog.findViewById<RecyclerView>(R.id.recycler_genres).apply {
            layoutManager = LinearLayoutManager(this@TelevisionTvChannelsActivity)
            adapter = genresAdapter
        }

        televisionTvChannelsViewModel.requestTvGenres()
        collectLatestOnLifecycleStarted(televisionTvChannelsViewModel.tvGenres) { state ->
            if (state.isLoading) {
                dialog.findViewById<ProgressBar>(R.id.genres_progressbar).visibility = View.VISIBLE
            }
            if (state.response != null) {
                dialog.findViewById<ProgressBar>(R.id.genres_progressbar).visibility = View.GONE
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

    private fun initializePlayer(url: String, userAgent: String, position: Long = 0) {
        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(Config.MIN_BUFFER_DURATION, Config.MAX_BUFFER_DURATION, Config.MIN_PLAYBACK_START_BUFFER, Config.MIN_PLAYBACK_RESUME_BUFFER)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val trackSelector = DefaultTrackSelector(this@TelevisionTvChannelsActivity, AdaptiveTrackSelection.Factory() as ExoTrackSelection.Factory)
        val renderersFactory = DefaultRenderersFactory(this@TelevisionTvChannelsActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }
        val httpDataSource = DefaultHttpDataSource.Factory().setUserAgent(Util.getUserAgent(this@TelevisionTvChannelsActivity, userAgent))
        val mediaSource = HlsMediaSource.Factory(httpDataSource).createMediaSource(MediaItem.fromUri(Uri.parse(url)))

        exoPlayer = ExoPlayer.Builder(this@TelevisionTvChannelsActivity, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build().apply {
                addListener(playerListener)
                setMediaSource(mediaSource)
            }
        playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)

        layout.exoPlayer.apply {
            player = exoPlayer
        }

        collectLatestOnLifecycleStarted(playerViewModel.playerStatus) { status ->
            when (status) {
                PlayerStatus.INITIALIZE -> {

                }
                PlayerStatus.PREPARE -> {
                    exoPlayer?.prepare()
                    playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                }
                PlayerStatus.PLAY -> {
                    exoPlayer?.apply {
                        playWhenReady = true
                        seekTo(position)
                        play()
                    }
                }
                PlayerStatus.PAUSE -> {
                    exoPlayer?.apply {
                        playWhenReady = false
                        pause()
                    }
                }
                PlayerStatus.RETRY -> { }
                PlayerStatus.RELEASE -> {
                    releasePlayer()
                }
            }
        }
    }

    private var playerListener = object: Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_BUFFERING) {
                layout.playerProgressbar.visibility = View.VISIBLE
            } else {
                layout.playerProgressbar.visibility = View.GONE
            }
        }
    }

    private fun releasePlayer() {
        exoPlayer?.apply {
            stop()
            removeListener(playerListener)
            clearMediaItems()
        }
    }

    override fun onPause() {
        super.onPause()
        playerViewModel.setPlayerStatus(PlayerStatus.PAUSE)
    }

    override fun onStop() {
        super.onStop()
        playerViewModel.setPlayerStatus(PlayerStatus.PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        playerViewModel.setPlayerStatus(PlayerStatus.RELEASE)
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}