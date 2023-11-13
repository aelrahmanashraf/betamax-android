package com.picassos.betamax.android.presentation.app.episode.episode_player

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityEpisodePlayerBinding
import com.picassos.betamax.android.domain.model.EpisodePlayerContent
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.model.TracksGroup
import com.picassos.betamax.android.presentation.app.App
import com.picassos.betamax.android.presentation.app.continue_watching.ContinueWatchingViewModel
import com.picassos.betamax.android.presentation.app.player.PlayerStatus
import com.picassos.betamax.android.presentation.app.player.PlayerViewModel
import com.picassos.betamax.android.presentation.app.track.tracks.TracksAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@androidx.annotation.OptIn(UnstableApi::class)
@AndroidEntryPoint
class EpisodePlayerActivity : AppCompatActivity() {
    private lateinit var layout: ActivityEpisodePlayerBinding
    private val episodePlayerViewModel: EpisodePlayerViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private val continueWatchingViewModel: ContinueWatchingViewModel by viewModels()

    private lateinit var requestDialog: RequestDialog

    private lateinit var player: ExoPlayer
    private lateinit var httpDataSource: DefaultHttpDataSource.Factory
    private lateinit var cacheDataSource: CacheDataSource.Factory
    private val cache: SimpleCache = App.playerCache

    private lateinit var playerContent: EpisodePlayerContent

    private var currentEpisode: Episodes.Episode? = null
    private var currentEpisodePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.PlayerTheme)

        requestDialog = RequestDialog(
            context = this@EpisodePlayerActivity,
            isFullscreen = true)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_episode_player)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Config.BUILD_TYPE == "release") {
                setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@EpisodePlayerActivity)
            }
        }

        layout.goBack.setOnClickListener {
            updateContinueWatching()
            collectLatestOnLifecycleStarted(continueWatchingViewModel.updateContinueWatching) { state ->
                if (state.isLoading) {
                    requestDialog.show()
                }
                if (state.responseCode != null) {
                    requestDialog.dismiss()
                    if (state.responseCode == 200) {
                        finish()
                    }
                }
                if (state.error != null) {
                    requestDialog.dismiss()
                }
            }

        }

        getSerializable(this@EpisodePlayerActivity, "playerContent", EpisodePlayerContent::class.java).also { playerContent ->
            this@EpisodePlayerActivity.playerContent = playerContent
            this@EpisodePlayerActivity.currentEpisode = playerContent.episode
            playerContent.episodes?.let { episodes ->
                episodes.rendered.indexOfFirst { it.episodeId == playerContent.episode.episodeId }.let { index ->
                    currentEpisodePosition = index
                }
            }
            initializePlayer(episode = playerContent.episode)
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                updateContinueWatching()
                collectLatestOnLifecycleStarted(continueWatchingViewModel.updateContinueWatching) { state ->
                    if (state.isLoading) {
                        requestDialog.show()
                    }
                    if (state.responseCode != null) {
                        requestDialog.dismiss()
                        if (state.responseCode == 200) {
                            finish()
                        }
                    }
                    if (state.error != null) {
                        requestDialog.dismiss()
                    }
                }
            }
        })
    }

    @SuppressLint("SwitchIntDef", "SetTextI18n")
    private fun initializePlayer(episode: Episodes.Episode) {
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                Config.MIN_BUFFER_DURATION,
                Config.MAX_BUFFER_DURATION,
                Config.MIN_PLAYBACK_START_BUFFER,
                Config.MIN_PLAYBACK_RESUME_BUFFER
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val trackSelector = DefaultTrackSelector(this@EpisodePlayerActivity)
        val renderersFactory = DefaultRenderersFactory(this@EpisodePlayerActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        }
        httpDataSource = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        cacheDataSource = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSource)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSource).createMediaSource(
            MediaItem.fromUri(Uri.parse(episode.url))
        )

        player = ExoPlayer.Builder(this@EpisodePlayerActivity)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSource))
            .setRenderersFactory(renderersFactory)
            .setSeekBackIncrementMs(Config.PLAYER_REPLAY_DURATION)
            .setSeekForwardIncrementMs(Config.PLAYER_FORWARD_DURATION)
            .build().apply {
                addListener(playerListener)
                setMediaSource(mediaSource, true)
            }

        playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)

        layout.apply {
            playerTitle.text = episode.title
            if (playerContent.movie.title.isNotEmpty() && playerContent.episodes?.seasonTitle != null) {
                playerMeta.text = "${playerContent.movie.title} | ${playerContent.episodes?.seasonTitle}"
            } else {
                playerMeta.visibility = View.GONE
            }
        }

        layout.playerView.apply {
            player = this@EpisodePlayerActivity.player
            setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                layout.controllerContainer.apply {
                    when (visibility) {
                        View.VISIBLE -> animate().alpha(1F).duration = 400
                        View.GONE -> animate().alpha(0F).duration = 400
                    }
                }
            })
            subtitleView?.apply {
                setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.2f)
            }
        }

        if (playerContent.currentPosition != 0) {
            player.seekTo(playerContent.currentPosition.toLong())
        }

        collectLatestOnLifecycleStarted(playerViewModel.playerStatus) { status ->
            when (status) {
                PlayerStatus.INITIALIZE -> {

                }
                PlayerStatus.PREPARE -> {
                    player.prepare()
                    layout.playerView.findViewById<ImageView>(R.id.player_action).setOnClickListener {
                        when (player.isPlaying) {
                            true -> playerViewModel.setPlayerStatus(PlayerStatus.PAUSE)
                            else -> playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                        }
                    }
                    playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                }
                PlayerStatus.PLAY -> {
                    player.apply {
                        playWhenReady = true
                        play()
                    }
                }
                PlayerStatus.PAUSE -> {
                    player.apply {
                        playWhenReady = false
                        pause()
                    }
                }
                PlayerStatus.RETRY -> {
                    layout.playerView.findViewById<ImageView>(R.id.player_action).apply {
                        setImageResource(R.drawable.icon_retry)
                        setOnClickListener {
                            playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)
                        }
                    }
                }
                PlayerStatus.RELEASE -> {
                    releasePlayer()
                }
            }
        }

        layout.playerView.findViewById<ImageView>(R.id.replay).setOnClickListener {
            player.seekBack()
        }

        layout.playerView.findViewById<ImageView>(R.id.forward).setOnClickListener {
            player.seekForward()
        }

        layout.playerView.findViewById<ImageView>(R.id.fullscreen_mode).apply {
            setOnClickListener {
                layout.playerView.resizeMode = when (layout.playerView.resizeMode) {
                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> {
                        setImageResource(R.drawable.icon_fit_to_width_filled)
                        AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                    AspectRatioFrameLayout.RESIZE_MODE_FILL -> {
                        setImageResource(R.drawable.icon_fullscreen_filled)
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                    else -> layout.playerView.resizeMode
                }
            }
        }

        layout.playerView.findViewById<ImageView>(R.id.tracks).setOnClickListener {
            showTracksDialog()
        }
    }

    private var playerListener = object: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                layout.playerView.findViewById<ImageView>(R.id.player_action).apply {
                    setImageResource(R.drawable.icon_pause_filled)
                }
            } else {
                layout.playerView.findViewById<ImageView>(R.id.player_action).apply {
                    setImageResource(R.drawable.icon_play_filled)
                }
            }
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_ENDED -> {
                    updateContinueWatching()
                    collectLatestOnLifecycleStarted(continueWatchingViewModel.updateContinueWatching) { state ->
                        if (state.isLoading) {
                            requestDialog.show()
                        }
                        if (state.responseCode != null) {
                            requestDialog.dismiss()

                            playerContent.episodes?.let { episodes ->
                                try {
                                    episodes.rendered.getOrNull(currentEpisodePosition + 1)?.let { episode ->
                                        currentEpisode = episode
                                        currentEpisodePosition += 1
                                        playNewUrl(episode = episode)
                                    } ?: run { finish() }
                                } catch (e: Exception) {
                                    finish()
                                }
                            } ?: run { finish() }
                        }
                        if (state.error != null) {
                            requestDialog.dismiss()
                        }
                    }
                }
                Player.STATE_BUFFERING -> {
                    layout.playerView.apply {
                        findViewById<ProgressBar>(R.id.player_progressbar).visibility = View.VISIBLE
                        findViewById<ImageView>(R.id.player_action).visibility = View.INVISIBLE
                    }
                }
                else -> {
                    layout.playerView.apply {
                        findViewById<ProgressBar>(R.id.player_progressbar).visibility = View.INVISIBLE
                        findViewById<ImageView>(R.id.player_action).visibility = View.VISIBLE
                    }
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

    private fun playNewUrl(episode: Episodes.Episode) {
        if (title.isNotEmpty()) {
            layout.playerTitle.text = episode.title
        }
        player.apply {
            stop()
            clearMediaItems()
        }
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSource).createMediaSource(
            MediaItem.fromUri(Uri.parse(episode.url))
        )
        player.apply {
            setMediaSource(mediaSource)
            playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)
        }
    }

    private fun releasePlayer() {
        player.apply {
            removeListener(playerListener)
            clearMediaItems()
            release()
        }
    }

    private fun updateContinueWatching() {
        currentEpisode?.let { episode ->
            continueWatchingViewModel.requestUpdateContinueWatching(
                contentId = episode.episodeId,
                title = episode.title,
                url = episode.url,
                thumbnail = episode.thumbnail,
                duration = player.duration.toInt(),
                currentPosition = player.currentPosition.toInt(),
                series = 1)
        }
    }

    private fun showTracksDialog() {
        val dialog = Dialog(this@EpisodePlayerActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_tracks)
            setCancelable(true)
            setOnCancelListener {
                dismiss()
            }
        }

        dialog.findViewById<ImageView>(R.id.dialog_close).setOnClickListener {
            dialog.dismiss()
        }

        val audioTracks = mutableListOf<String>()
        val subtitleTracks = mutableListOf<String>()
        val audioTracksGroup = mutableListOf<TracksGroup.Track>()
        val subtitleTracksGroup = mutableListOf<TracksGroup.Track>()

        for (i in 0 until player.currentTracks.groups.size) {
            val format = player.currentTracks.groups[i].getTrackFormat(0)
            when {
                format.sampleMimeType?.startsWith("audio/") == true && format.language != null -> {
                    audioTracks.add(format.language!!)
                }
                (format.sampleMimeType?.startsWith("text/") == true || format.sampleMimeType?.startsWith("application/") == true) && format.language != null -> {
                    subtitleTracks.add(format.language!!)
                }
            }
        }

        audioTracks.distinct().forEachIndexed { index, language ->
            val trackTitle = Locale(language).getDisplayLanguage(Locale.getDefault())
            val track = TracksGroup.Track(id = index + 1, code = language, title = trackTitle)
            audioTracksGroup.add(track)
        }

        subtitleTracksGroup.add(TracksGroup.Track(id = 0, code = "", title = getString(R.string.disable_subtitles)))
        subtitleTracks.distinct().forEachIndexed { index, language ->
            val trackTitle = Locale(language).getDisplayLanguage(Locale.getDefault())
            val track = TracksGroup.Track(id = index + 1, code = language, title = trackTitle)
            subtitleTracksGroup.add(track)
        }

        val audioTracksAdapter = TracksAdapter(listener = object : TracksAdapter.OnTrackClickListener {
            override fun onItemClick(track: TracksGroup.Track) {
                val parameters = player.trackSelectionParameters
                    .buildUpon()
                    .setPreferredAudioLanguage(track.code)
                    .build()
                player.trackSelectionParameters = parameters

                dialog.dismiss()
            }
        })
        dialog.findViewById<RecyclerView>(R.id.recycler_audio_tracks).apply {
            layoutManager = LinearLayoutManager(this@EpisodePlayerActivity)
            adapter = audioTracksAdapter
        }
        audioTracksAdapter.differ.submitList(audioTracksGroup)

        val subtitlesTracksAdapter = TracksAdapter(listener = object : TracksAdapter.OnTrackClickListener {
            override fun onItemClick(track: TracksGroup.Track) {
                if (track.code.isNotEmpty()) {
                    val parameters = player.trackSelectionParameters
                        .buildUpon()
                        .setPreferredTextLanguage(track.code)
                        .build()
                    player.trackSelectionParameters = parameters
                } else {
                    val parameters = player.trackSelectionParameters
                        .buildUpon()
                        .setPreferredTextLanguage(null)
                        .build()
                    player.trackSelectionParameters = parameters
                }

                dialog.dismiss()
            }
        })
        dialog.findViewById<RecyclerView>(R.id.recycler_subtitles_tracks).apply {
            layoutManager = LinearLayoutManager(this@EpisodePlayerActivity)
            adapter = subtitlesTracksAdapter
        }
        subtitlesTracksAdapter.differ.submitList(subtitleTracksGroup)

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            Helper.hideSystemUI(window, layout.root)
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
        releasePlayer()
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Config.BUILD_TYPE == "release") {
                clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}