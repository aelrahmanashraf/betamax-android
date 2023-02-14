package com.picassos.betamax.android.presentation.app.tvchannel.view_tvchannel

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.chip.Chip
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.fadeVisibility
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.utilities.Helper.requestedOrientationWithFullSensor
import com.picassos.betamax.android.core.utilities.Helper.toDips
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.databinding.ActivityViewTvchannelBinding
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.presentation.app.player.PlayerStatus
import com.picassos.betamax.android.presentation.app.player.PlayerViewModel
import com.picassos.betamax.android.presentation.app.tvchannel.related_tvchannels.RelatedTvChannelsAdapter
import com.picassos.betamax.android.presentation.app.video_quality.video_quality_chooser.VideoQualityChooserBottomSheetModal
import com.picassos.betamax.android.presentation.app.video_quality.video_quality_chooser.VideoQualityChooserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewTvChannelActivity : AppCompatActivity() {
    private lateinit var layout: ActivityViewTvchannelBinding
    private val viewTvChannelViewModel: ViewTvChannelViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private val videoQualityChooserViewModel: VideoQualityChooserViewModel by viewModels()

    private var exoPlayer: SimpleExoPlayer? = null

    private lateinit var tvChannel: TvChannels.TvChannel
    private var selectedGenre = 0
    private var selectedUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_view_tvchannel)

        window.apply {
            addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Config.BUILD_TYPE == "release") {
                setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@ViewTvChannelActivity)
            }
        }

        layout.goBack.setOnClickListener { finish() }

        getSerializable(this@ViewTvChannelActivity, "tv_channel", TvChannels.TvChannel::class.java).also { tvChannel ->
            viewTvChannelViewModel.apply {
                this@ViewTvChannelActivity.tvChannel = tvChannel
                requestTvChannel(tvChannel.tvChannelId)
            }
        }

        collectLatestOnLifecycleStarted(viewTvChannelViewModel.viewTvChannel) { state ->
            if (state.isLoading) {
                exoPlayer?.apply {
                    playWhenReady = false
                    pause()
                    clearMediaItems()
                }
                layout.apply {
                    recyclerRelatedTv.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                val tvChannelDetails = state.response.tvChannelDetails.tvChannels[0]
                when (state.response.videoQuality) {
                    1 -> initializePlayer(url = tvChannelDetails.sdUrl)
                    2 -> initializePlayer(url = tvChannelDetails.hdUrl)
                    3 -> initializePlayer(url = tvChannelDetails.fhdUrl)
                }
            }
            if (state.error != null) {
                layout.apply {
                    recyclerRelatedTv.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        viewTvChannelViewModel.apply {
                            requestTvChannel(tvChannel.tvChannelId)
                            requestTvGenres()
                            when (selectedGenre) {
                                0 -> requestTvChannels()
                                else -> requestTvChannelsByGenre(selectedGenre)
                            }
                        }
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        viewTvChannelViewModel.requestTvGenres()
        collectLatestOnLifecycleStarted(viewTvChannelViewModel.tvGenres) { state ->
            if (state.response != null) {
                state.response.genres.forEach { genre ->
                    layout.genresList.addView(createGenreChip(genre))
                }
            }
        }

        viewTvChannelViewModel.requestTvChannels()
        collectLatestOnLifecycleStarted(viewTvChannelViewModel.tvChannels) { state ->
            if (state.isLoading) {
                layout.progressbar.visibility = View.VISIBLE
            }
            if (state.response != null) {
                layout.progressbar.visibility = View.GONE

                val relatedTvChannelsAdapter = RelatedTvChannelsAdapter(tvChannel.tvChannelId, listener = object: OnTvChannelClickListener {
                    override fun onItemClick(tvChannel: TvChannels.TvChannel) {
                        viewTvChannelViewModel.apply {
                            this@ViewTvChannelActivity.tvChannel = tvChannel
                            requestTvChannel(tvChannel.tvChannelId)

                            when (selectedGenre) {
                                0 -> requestTvChannels()
                                else -> requestTvChannelsByGenre(selectedGenre)
                            }
                        }
                    }
                })
                layout.recyclerRelatedTv.apply {
                    layoutManager = LinearLayoutManager(this@ViewTvChannelActivity)
                    adapter = relatedTvChannelsAdapter
                }
                relatedTvChannelsAdapter.differ.submitList(state.response.tvChannels)

                if (state.response.tvChannels.isEmpty()) {
                    layout.noItems.visibility = View.VISIBLE
                } else {
                    layout.noItems.visibility = View.GONE
                }
            }
            if (state.error != null) {
                layout.progressbar.visibility = View.GONE
            }
        }

        collectLatestOnLifecycleStarted(videoQualityChooserViewModel.selectedVideoQuality) { isSafe ->
            isSafe?.let { quality ->
                exoPlayer?.apply {
                    playerViewModel.setPlayerStatus(PlayerStatus.RELEASE)
                    delay(500)
                    when (quality) {
                        1 -> initializePlayer(url = tvChannel.sdUrl)
                        2 -> initializePlayer(url = tvChannel.hdUrl)
                        3 -> initializePlayer(url = tvChannel.fhdUrl)
                    }
                }
                videoQualityChooserViewModel.setVideoQuality(null)
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            @SuppressLint("SwitchIntDef")
            override fun handleOnBackPressed() {
                when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        requestedOrientationWithFullSensor(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    }
                    Configuration.ORIENTATION_PORTRAIT -> {
                        finish()
                    }
                }
            }
        })
    }

    @SuppressLint("SwitchIntDef")
    private fun initializePlayer(url: String) {
        selectedUrl = url

        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(Config.MIN_BUFFER_DURATION, Config.MAX_BUFFER_DURATION, Config.MIN_PLAYBACK_START_BUFFER, Config.MIN_PLAYBACK_RESUME_BUFFER)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val trackSelector = DefaultTrackSelector(this@ViewTvChannelActivity, AdaptiveTrackSelection.Factory() as ExoTrackSelection.Factory)
        val renderersFactory = DefaultRenderersFactory(this@ViewTvChannelActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this@ViewTvChannelActivity, Util.getUserAgent(this@ViewTvChannelActivity, tvChannel.userAgent))
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(url)))

        exoPlayer = SimpleExoPlayer.Builder(this@ViewTvChannelActivity, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build().apply {
                addListener(playerListener)
                setMediaSource(mediaSource)
            }
        playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)

        layout.exoPlayer.apply {
            player = exoPlayer
            setControllerVisibilityListener { visibility ->
                layout.controllerContainer.apply {
                    when (visibility) {
                        View.VISIBLE -> fadeVisibility(View.VISIBLE, 400)
                        View.GONE -> fadeVisibility(View.GONE, 400)
                    }
                }
            }
        }

        collectLatestOnLifecycleStarted(playerViewModel.playerStatus) { status ->
            when (status) {
                PlayerStatus.INITIALIZE -> {

                }
                PlayerStatus.PREPARE -> {
                    exoPlayer?.prepare()
                    layout.playerAction.setOnClickListener {
                        when (exoPlayer?.isPlaying) {
                            true -> playerViewModel.setPlayerStatus(PlayerStatus.PAUSE)
                            else -> playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                        }
                    }
                    playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                }
                PlayerStatus.PLAY -> {
                    exoPlayer?.apply {
                        playWhenReady = true
                        play()
                    }
                }
                PlayerStatus.PAUSE -> {
                    exoPlayer?.apply {
                        playWhenReady = false
                        pause()
                    }
                }
                PlayerStatus.RETRY -> {
                    layout.playerAction.apply {
                        setImageResource(R.drawable.icon_retry)
                        setOnClickListener {
                            playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)
                        }
                    }
                }
                PlayerStatus.RELEASE -> {
                    exoPlayer?.apply {
                        removeListener(playerListener)
                        clearMediaItems()
                    }
                }
            }
        }

        layout.exoPlayer.findViewById<ImageView>(R.id.fullscreen_mode).setOnClickListener {
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> requestedOrientationWithFullSensor(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                Configuration.ORIENTATION_LANDSCAPE -> requestedOrientationWithFullSensor(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            }
        }

        layout.playerOptions.setOnClickListener {
            val videoQualityChooserBottomSheetModal = VideoQualityChooserBottomSheetModal()
            videoQualityChooserBottomSheetModal.show(supportFragmentManager, "TAG")
        }
    }

    private var playerListener = object: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            when (isPlaying) {
                true -> layout.playerAction.setImageResource(R.drawable.icon_pause_filled)
                else -> layout.playerAction.setImageResource(R.drawable.icon_play_filled)
            }
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_BUFFERING) {
                layout.apply {
                    playerProgressbar.visibility = View.VISIBLE
                    playerAction.visibility = View.GONE
                }
            } else {
                layout.apply {
                    playerProgressbar.visibility = View.GONE
                    playerAction.visibility = View.VISIBLE
                }
            }
        }
        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            if (error != null) {
                playerViewModel.setPlayerStatus(PlayerStatus.RETRY)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun createGenreChip(genre: Genres.Genre): Chip {
        val chip = this@ViewTvChannelActivity.layoutInflater.inflate(R.layout.item_genre_selectable, null, false) as Chip
        return chip.apply {
            id = ViewCompat.generateViewId()
            text = genre.title
            setOnCheckedChangeListener { _, _ ->
                selectedGenre = genre.genreId
                viewTvChannelViewModel.apply {
                    requestTvChannelsByGenre(selectedGenre)
                }
            }
        }
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout.apply {
                goBack.visibility = View.VISIBLE
                playerContainer.layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (200f).toDips(resources))
                exoPlayer.apply exoplayer@ {
                    this@exoplayer.findViewById<ImageView>(R.id.fullscreen_mode).setImageResource(R.drawable.icon_fullscreen_filled)
                }
                Helper.showSystemUI(window, root)
            }
        } else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout.apply {
                goBack.visibility = View.GONE
                playerContainer.layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                exoPlayer.apply exoplayer@ {
                    this@exoplayer.findViewById<ImageView>(R.id.fullscreen_mode).setImageResource(R.drawable.icon_fit_to_width_filled)
                }
                Helper.hideSystemUI(window, root)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
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
            clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Config.BUILD_TYPE == "release") {
                clearFlags(LayoutParams.FLAG_SECURE)
            }
        }
    }
}