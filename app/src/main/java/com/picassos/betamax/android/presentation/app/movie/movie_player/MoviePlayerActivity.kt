package com.picassos.betamax.android.presentation.app.movie.movie_player

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityMoviePlayerBinding
import com.picassos.betamax.android.domain.model.PlayerContent
import com.picassos.betamax.android.presentation.app.continue_watching.ContinueWatchingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoviePlayerActivity : AppCompatActivity() {
    private lateinit var layout: ActivityMoviePlayerBinding
    private val moviePlayerViewModel: MoviePlayerViewModel by viewModels()
    private val continueWatchingViewModel: ContinueWatchingViewModel by viewModels()

    private lateinit var exoPlayer: SimpleExoPlayer
    private var isFullscreen = false

    private lateinit var playerContent: PlayerContent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.PlayerTheme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val requestDialog = RequestDialog(
            context = this@MoviePlayerActivity,
            isFullscreen = true)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_movie_player)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (!Config.DEVELOPMENT_BUILD) {
                setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, layout.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this@MoviePlayerActivity, Util.getUserAgent(this@MoviePlayerActivity, applicationInfo.name))
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(Helper.parseUrl(playerContent.url))))

        exoPlayer = SimpleExoPlayer.Builder(this).build().apply {
            addListener(playerListener)
            seekTo(0)
            setMediaSource(mediaSource)
            prepare()
        }

        layout.exoPlayer.apply {
            player = exoPlayer
            setControllerVisibilityListener { visibility ->
                layout.controllerContainer.visibility = when (visibility) {
                    View.VISIBLE -> View.VISIBLE
                    else -> View.GONE
                }
            }
        }

        if (playerContent.currentPosition != 0) {
            exoPlayer.seekTo(playerContent.currentPosition.toLong())
        }

        layout.replay.setOnClickListener {
            exoPlayer.apply {
                if (currentPosition <= Config.PLAYER_REPLAY_DURATION)
                    seekTo(0)
                else
                    seekTo(currentPosition - Config.PLAYER_REPLAY_DURATION)
            }
        }

        layout.play.setOnClickListener {
            exoPlayer.apply {
                when (isPlaying) {
                    true -> pause()
                    else -> play()
                }
            }
        }

        layout.forward.setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition + Config.PLAYER_FORWARD_DURATION)
        }

        layout.fullscreenMode.setOnClickListener {
            if (!isFullscreen) {
                isFullscreen = true
                layout.fullscreenIcon.setImageResource(R.drawable.icon_fit_to_width_filled)
                layout.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            } else {
                isFullscreen = false
                layout.fullscreenIcon.setImageResource(R.drawable.icon_fullscreen_filled)
                layout.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }

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

    private var playerListener = object: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                exoPlayer.playWhenReady = true
                layout.playIcon.setImageResource(R.drawable.icon_pause_filled)
            } else {
                exoPlayer.playWhenReady
                layout.playIcon.setImageResource(R.drawable.icon_play_filled)
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
                        playIcon.visibility = View.GONE
                    }
                }
                else -> {
                    layout.apply {
                        playerProgressbar.visibility = View.GONE
                        playIcon.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun releasePlayer() {
        exoPlayer.apply {
            stop()
            clearMediaItems()
        }
    }

    override fun onResume() {
        super.onResume()

        Helper.restrictVpn(this@MoviePlayerActivity)

        exoPlayer.apply {
            playWhenReady = true
            play()
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.apply {
            removeListener(playerListener)
            releasePlayer()
        }
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (!Config.DEVELOPMENT_BUILD) {
                clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}