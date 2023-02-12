package com.picassos.betamax.android.presentation.app.movie.movie_player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityMoviePlayerBinding
import com.picassos.betamax.android.domain.model.PlayerContent
import com.picassos.betamax.android.presentation.app.App
import com.picassos.betamax.android.presentation.app.continue_watching.ContinueWatchingViewModel
import com.picassos.betamax.android.presentation.app.player.PlayerStatus
import com.picassos.betamax.android.presentation.app.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
@AndroidEntryPoint
class MoviePlayerActivity : AppCompatActivity() {
    private lateinit var layout: ActivityMoviePlayerBinding
    private val moviePlayerViewModel: MoviePlayerViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private val continueWatchingViewModel: ContinueWatchingViewModel by viewModels()

    private lateinit var exoPlayer: SimpleExoPlayer
    private val cache: SimpleCache = App.cache

    private lateinit var playerContent: PlayerContent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.PlayerTheme)

        val requestDialog = RequestDialog(
            context = this@MoviePlayerActivity,
            isFullscreen = true)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_movie_player)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Config.BUILD_TYPE == "release") {
                setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@MoviePlayerActivity)
            }
        }

        layout.goBack.setOnClickListener {
            continueWatchingViewModel.requestUpdateContinueWatching(
                contentId = playerContent.id,
                url = playerContent.url,
                thumbnail = playerContent.thumbnail,
                currentPosition = exoPlayer.currentPosition.toInt())
        }

        getSerializable(this@MoviePlayerActivity, "playerContent", PlayerContent::class.java).also { playerContent ->
            this@MoviePlayerActivity.playerContent = playerContent
        }

        initializePlayer(url = playerContent.url)

        collectLatestOnLifecycleStarted(continueWatchingViewModel.updateContinueWatching) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode) {
                    200 -> finish()
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
            }
        }

        collectLatestOnLifecycleStarted(continueWatchingViewModel.deleteContinueWatching) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode) {
                    200 -> finish()
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                continueWatchingViewModel.requestUpdateContinueWatching(
                    contentId = playerContent.id,
                    url = playerContent.url,
                    thumbnail = playerContent.thumbnail,
                    currentPosition = exoPlayer.currentPosition.toInt())
            }
        })
    }

    @SuppressLint("SwitchIntDef")
    private fun initializePlayer(url: String) {
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(Config.MIN_BUFFER_DURATION, Config.MAX_BUFFER_DURATION, Config.MIN_PLAYBACK_START_BUFFER, Config.MIN_PLAYBACK_RESUME_BUFFER)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val trackSelector = DefaultTrackSelector(this@MoviePlayerActivity, AdaptiveTrackSelection.Factory() as ExoTrackSelection.Factory)
        val httpDataSource = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        val cacheDataSource = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSource)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSource).createMediaSource(MediaItem.fromUri(url))

        exoPlayer = SimpleExoPlayer.Builder(this@MoviePlayerActivity)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSource))
            .build().apply {
                addListener(playerListener)
                setMediaSource(mediaSource, true)
            }
        playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)

        layout.exoPlayer.apply {
            player = exoPlayer
            setControllerVisibilityListener { visibility ->
                layout.controllerContainer.apply {
                    when (visibility) {
                        View.VISIBLE -> animate().alpha(1F).duration = 400
                        View.GONE -> animate().alpha(0F).duration = 400
                    }
                }
            }
        }

        if (playerContent.currentPosition != 0) {
            exoPlayer.seekTo(playerContent.currentPosition.toLong())
        }

        collectLatestOnLifecycleStarted(playerViewModel.playerStatus) { status ->
            when (status) {
                PlayerStatus.INITIALIZE -> {

                }
                PlayerStatus.PREPARE -> {
                    exoPlayer.apply {
                        prepare()
                        layout.playerAction.setOnClickListener {
                            when (isPlaying) {
                                true -> playerViewModel.setPlayerStatus(PlayerStatus.PAUSE)
                                else -> playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                            }
                        }
                    }
                    playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                }
                PlayerStatus.PLAY -> {
                    exoPlayer.apply {
                        playWhenReady = true
                        play()
                    }
                }
                PlayerStatus.PAUSE -> {
                    exoPlayer.apply {
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
                    exoPlayer.apply {
                        removeListener(playerListener)
                        clearMediaItems()
                    }
                }
            }
        }

        layout.replay.setOnClickListener {
            exoPlayer.apply {
                if (currentPosition <= Config.PLAYER_REPLAY_DURATION) seekTo(0)
                else seekTo(currentPosition - Config.PLAYER_REPLAY_DURATION)
            }
        }

        layout.forward.setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition + Config.PLAYER_FORWARD_DURATION)
        }

        layout.exoPlayer.apply exoplayer@ {
            findViewById<ImageView>(R.id.fullscreen_mode).apply fullscreen@ {
                this@fullscreen.setOnClickListener {
                    when (this@exoplayer.resizeMode) {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> {
                            this@fullscreen.setImageDrawable(AppCompatResources.getDrawable(this@MoviePlayerActivity, R.drawable.icon_fit_to_width_filled))
                            this@exoplayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        }
                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> {
                            this@fullscreen.setImageDrawable(AppCompatResources.getDrawable(this@MoviePlayerActivity, R.drawable.icon_fullscreen_filled))
                            this@exoplayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    }
                }
            }
        }
    }

    private var playerListener = object: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                layout.playerAction.setImageResource(R.drawable.icon_pause_filled)
            } else {
                layout.playerAction.setImageResource(R.drawable.icon_play_filled)
            }
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_ENDED -> {
                    continueWatchingViewModel.requestDeleteContinueWatching(
                        contentId = playerContent.id)
                }
                Player.STATE_BUFFERING -> {
                    layout.apply {
                        playerProgressbar.visibility = View.VISIBLE
                        playerAction.visibility = View.INVISIBLE
                    }
                }
                else -> {
                    layout.apply {
                        playerProgressbar.visibility = View.INVISIBLE
                        playerAction.visibility = View.VISIBLE
                    }
                }
            }
        }
        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            playerViewModel.setPlayerStatus(PlayerStatus.RETRY)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, layout.root).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Config.BUILD_TYPE == "release") {
                clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}