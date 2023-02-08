package com.picassos.betamax.android.presentation.television.tvchannel.tvchannel_player

import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ActivityTelevisionTvchannelPlayerBinding
import com.picassos.betamax.android.domain.model.TelevisionPlayerContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TelevisionTvChannelPlayerActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionTvchannelPlayerBinding
    private val televisionTvChannelPlayerViewModel: TelevisionTvChannelPlayerViewModel by viewModels()

    private lateinit var exoPlayer: SimpleExoPlayer

    private lateinit var playerContent: TelevisionPlayerContent
    private var currentTvChannelPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.PlayerTheme)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_tvchannel_player)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        Helper.getSerializable(this@TelevisionTvChannelPlayerActivity, "playerContent", TelevisionPlayerContent::class.java).also { playerContent ->
            this@TelevisionTvChannelPlayerActivity.playerContent = playerContent
            this@TelevisionTvChannelPlayerActivity.currentTvChannelPosition = playerContent.currentTvChannelPosition

            initializePlayer(
                url = playerContent.url,
                userAgent = playerContent.userAgent)
        }

        collectLatestOnLifecycleStarted(televisionTvChannelPlayerViewModel.tvChannel) { state ->
            if (state.isLoading) {
                exoPlayer.apply {
                    stop()
                    clearMediaItems()
                }
            }
            if (state.response != null) {
                val tvChannel = state.response.tvChannels[0]

                initializePlayer(
                    url = tvChannel.hdUrl,
                    userAgent = tvChannel.userAgent)
            }
        }
    }

    private fun initializePlayer(url: String, userAgent: String, position: Long = 0) {
        val trackSelector = DefaultTrackSelector(this@TelevisionTvChannelPlayerActivity, AdaptiveTrackSelection.Factory() as ExoTrackSelection.Factory)
        val renderersFactory = DefaultRenderersFactory(this@TelevisionTvChannelPlayerActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this@TelevisionTvChannelPlayerActivity, Util.getUserAgent(this@TelevisionTvChannelPlayerActivity, userAgent))
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(url)))

        exoPlayer = SimpleExoPlayer.Builder(this@TelevisionTvChannelPlayerActivity, renderersFactory)
            .setTrackSelector(trackSelector)
            .build().apply {
                addListener(playerListener)
                setMediaSource(mediaSource)
                prepare()
            }

        layout.exoPlayer.apply {
            player = exoPlayer
        }

        exoPlayer.apply {
            seekTo(position)
            play()
        }
    }

    private var playerListener = object: Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    layout.playerProgressbar.visibility = View.VISIBLE
                }
                else -> {
                    layout.playerProgressbar.visibility = View.GONE
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                currentTvChannelPosition += 1

                try {
                    televisionTvChannelPlayerViewModel.requestTvChannel(
                        tvChannelId = playerContent.tvChannelsList[currentTvChannelPosition].tvChannelId)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    currentTvChannelPosition -= 1
                    Toast.makeText(this@TelevisionTvChannelPlayerActivity, getString(R.string.cant_move_forward), Toast.LENGTH_LONG).show()
                }
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                exoPlayer.apply {
                    if (!isPlaying) play() else pause()
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                currentTvChannelPosition -= 1

                try {
                    televisionTvChannelPlayerViewModel.requestTvChannel(
                        tvChannelId = playerContent.tvChannelsList[currentTvChannelPosition].tvChannelId)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    currentTvChannelPosition += 1
                    Toast.makeText(this@TelevisionTvChannelPlayerActivity, getString(R.string.cant_move_back), Toast.LENGTH_LONG).show()
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
            }
        }
        return false
    }

    private fun releasePlayer() {
        exoPlayer.apply {
            stop()
            clearMediaItems()
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
        }
    }
}