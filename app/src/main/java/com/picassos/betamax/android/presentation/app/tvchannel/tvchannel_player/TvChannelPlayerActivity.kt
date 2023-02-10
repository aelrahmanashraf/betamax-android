package com.picassos.betamax.android.presentation.app.tvchannel.tvchannel_player

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.databinding.ActivityTvchannelPlayerBinding
import com.picassos.betamax.android.domain.model.TvChannelPlayerContent
import com.picassos.betamax.android.presentation.app.player.PlayerStatus
import com.picassos.betamax.android.presentation.app.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TvChannelPlayerActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTvchannelPlayerBinding
    private val playerViewModel: PlayerViewModel by viewModels()

    private lateinit var exoPlayer: SimpleExoPlayer

    private lateinit var playerContent: TvChannelPlayerContent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.PlayerTheme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        layout = DataBindingUtil.setContentView(this, R.layout.activity_tvchannel_player)

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@TvChannelPlayerActivity)
            }
        }

        getSerializable(this@TvChannelPlayerActivity, "playerContent", TvChannelPlayerContent::class.java).also { playerContent ->
            this@TvChannelPlayerActivity.playerContent = playerContent
        }

        initializePlayer(
            url = playerContent.url,
            userAgent = playerContent.userAgent)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Intent().also { intent ->
                    intent.putExtra("currentPosition", exoPlayer.currentPosition.toInt())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    private fun initializePlayer(url: String, userAgent: String) {
        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(Config.MIN_BUFFER_DURATION, Config.MAX_BUFFER_DURATION, Config.MIN_PLAYBACK_START_BUFFER, Config.MIN_PLAYBACK_RESUME_BUFFER)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val trackSelector = DefaultTrackSelector(this@TvChannelPlayerActivity, AdaptiveTrackSelection.Factory() as ExoTrackSelection.Factory)
        val renderersFactory = DefaultRenderersFactory(this@TvChannelPlayerActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this@TvChannelPlayerActivity, Util.getUserAgent(this@TvChannelPlayerActivity, userAgent))
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(url)))

        exoPlayer = SimpleExoPlayer.Builder(this@TvChannelPlayerActivity, renderersFactory)
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
                layout.controllerContainer.visibility = when (visibility) {
                    View.VISIBLE -> View.VISIBLE
                    else -> View.GONE
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
                    exoPlayer.prepare()
                    layout.playerAction.setOnClickListener {
                        when (exoPlayer.isPlaying) {
                            true -> playerViewModel.setPlayerStatus(PlayerStatus.PAUSE)
                            else -> playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
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

        layout.exoPlayer.findViewById<ImageView>(R.id.minimized_mode).setOnClickListener {
            Intent().also { intent ->
                intent.putExtra("currentPosition", exoPlayer.currentPosition.toInt())
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
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
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    layout.apply {
                        playerProgressbar.visibility = View.VISIBLE
                        playerAction.visibility = View.GONE
                    }
                }
                else -> {
                    layout.apply {
                        playerProgressbar.visibility = View.GONE
                        playerAction.visibility = View.VISIBLE
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
            if (!Config.DEVELOPMENT_BUILD) {
                clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}