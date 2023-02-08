package com.picassos.betamax.android.presentation.app.tvchannel.tvchannel_player

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.databinding.ActivityTvchannelPlayerBinding
import com.picassos.betamax.android.domain.model.TvChannelPlayerContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TvChannelPlayerActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTvchannelPlayerBinding

    private lateinit var exoPlayer: SimpleExoPlayer

    private lateinit var playerContent: TvChannelPlayerContent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.PlayerTheme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        layout = DataBindingUtil.setContentView(this, R.layout.activity_tvchannel_player)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

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

        getSerializable(this@TvChannelPlayerActivity, "playerContent", TvChannelPlayerContent::class.java).also { playerContent ->
            this@TvChannelPlayerActivity.playerContent = playerContent
        }

        val trackSelector = DefaultTrackSelector(this@TvChannelPlayerActivity, AdaptiveTrackSelection.Factory() as ExoTrackSelection.Factory)
        val renderersFactory = DefaultRenderersFactory(this@TvChannelPlayerActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this@TvChannelPlayerActivity, Util.getUserAgent(this@TvChannelPlayerActivity, playerContent.userAgent))
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(playerContent.url)))

        exoPlayer = SimpleExoPlayer.Builder(this@TvChannelPlayerActivity, renderersFactory)
            .setTrackSelector(trackSelector)
            .build().apply {
                addListener(playerListener)
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

        layout.play.setOnClickListener {
            exoPlayer.apply {
                when (isPlaying) {
                    true -> pause()
                    else -> play()
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

    override fun onConfigurationChanged(config: Configuration) {
        super.onConfigurationChanged(config)

        when (config.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                Toast.makeText(this@TvChannelPlayerActivity, "Portrait", Toast.LENGTH_LONG).show()
                Intent().also { intent ->
                    intent.putExtra("currentPosition", exoPlayer.currentPosition.toInt())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                Toast.makeText(this@TvChannelPlayerActivity, "Landscape", Toast.LENGTH_LONG).show()
            }
        }
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

        exoPlayer.apply {
            playWhenReady = true
            play()
        }
        Helper.restrictVpn(this@TvChannelPlayerActivity)
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