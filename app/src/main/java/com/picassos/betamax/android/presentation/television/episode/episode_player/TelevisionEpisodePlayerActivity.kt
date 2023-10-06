package com.picassos.betamax.android.presentation.television.episode.episode_player

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityTelevisionEpisodePlayerBinding
import com.picassos.betamax.android.domain.model.EpisodePlayerContent
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.model.TracksGroup
import com.picassos.betamax.android.presentation.app.continue_watching.ContinueWatchingViewModel
import com.picassos.betamax.android.presentation.app.player.PlayerStatus
import com.picassos.betamax.android.presentation.app.player.PlayerViewModel
import com.picassos.betamax.android.presentation.app.track.tracks.TracksAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class TelevisionEpisodePlayerActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionEpisodePlayerBinding
    private val televisionEpisodePlayerViewModel: TelevisionEpisodePlayerViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private val continueWatchingViewModel: ContinueWatchingViewModel by viewModels()

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var httpDataSource: DefaultHttpDataSource.Factory

    private lateinit var playerContent: EpisodePlayerContent

    private var currentEpisode: Episodes.Episode? = null
    private var currentEpisodePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.PlayerTheme)

        val requestDialog = RequestDialog(
            context = this@TelevisionEpisodePlayerActivity,
            isFullscreen = true)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_episode_player)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        getSerializable(this@TelevisionEpisodePlayerActivity, "playerContent", EpisodePlayerContent::class.java).also { playerContent ->
            this@TelevisionEpisodePlayerActivity.playerContent = playerContent
            this@TelevisionEpisodePlayerActivity.currentEpisode = playerContent.episode
            playerContent.episodes?.let { episodes ->
                episodes.rendered.indexOfFirst { it.episodeId == playerContent.episode.episodeId }.let { index ->
                    currentEpisodePosition = index
                }
            }
            initializePlayer(episode = playerContent.episode)
        }

        collectLatestOnLifecycleStarted(continueWatchingViewModel.updateContinueWatching) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                if (state.responseCode == 200) {
                    finishActivityWithResult()
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
                if (state.responseCode == 200) {
                    playerContent.episodes?.let { episodes ->
                        try {
                            episodes.rendered.getOrNull(currentEpisodePosition + 1)?.let { episode ->
                                currentEpisode = episode
                                currentEpisodePosition += 1
                                playNewUrl(episode = episode)
                            } ?: run { finishActivityWithResult() }
                        } catch (e: Exception) {
                            finishActivityWithResult()
                        }
                    } ?: run { finishActivityWithResult() }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
            }
        }
    }

    @SuppressLint("SwitchIntDef", "SetTextI18n")
    private fun initializePlayer(episode: Episodes.Episode) {
        val trackSelector = DefaultTrackSelector(this@TelevisionEpisodePlayerActivity)
        val renderersFactory = DefaultRenderersFactory(this@TelevisionEpisodePlayerActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }
        val parameters = trackSelector.buildUponParameters()
            .setPreferredAudioLanguage("spa")
            .build()
        httpDataSource = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)

        val mediaSource = ProgressiveMediaSource.Factory(httpDataSource).createMediaSource(MediaItem.fromUri(Uri.parse(episode.url)))

        exoPlayer = ExoPlayer.Builder(this@TelevisionEpisodePlayerActivity)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSource))
            .setRenderersFactory(renderersFactory)
            .setSeekBackIncrementMs(Config.PLAYER_REPLAY_DURATION)
            .setSeekForwardIncrementMs(Config.PLAYER_FORWARD_DURATION)
            .build().apply {
                addListener(playerListener)
                setMediaSource(mediaSource, true)
            }
        exoPlayer.trackSelectionParameters = parameters

        playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)

        layout.apply {
            playerTitle.text = episode.title
            if (playerContent.movie.title.isNotEmpty() && playerContent.episodes?.seasonTitle != null) {
                playerMeta.text = "${playerContent.movie.title} | ${playerContent.episodes?.seasonTitle}"
            } else {
                playerMeta.visibility = View.GONE
            }
        }
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
                    exoPlayer.apply {
                        val lastPlayBackPosition = currentPosition
                        seekTo(lastPlayBackPosition)
                    }
                    playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)
                }
                PlayerStatus.RELEASE -> {
                    exoPlayer.apply {
                        stop()
                        removeListener(playerListener)
                        clearMediaItems()
                        release()
                    }
                }
            }
        }
    }

    private var playerListener = object: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                layout.exoPlayer.findViewById<ImageView>(R.id.player_action).apply {
                    setImageResource(R.drawable.icon_pause_filled)
                }
            } else {
                layout.exoPlayer.findViewById<ImageView>(R.id.player_action).apply {
                    setImageResource(R.drawable.icon_play_filled)
                }
            }
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_ENDED -> {
                    currentEpisode?.let { episode ->
                        continueWatchingViewModel.requestDeleteContinueWatching(
                            contentId = episode.episodeId)
                    }
                }
                Player.STATE_BUFFERING -> {
                    layout.exoPlayer.apply {
                        findViewById<ProgressBar>(R.id.player_progressbar).visibility = View.VISIBLE
                        findViewById<ImageView>(R.id.player_action).visibility = View.INVISIBLE
                    }
                }
                else -> {
                    layout.exoPlayer.apply {
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
        exoPlayer.apply {
            stop()
            clearMediaItems()
        }
        val mediaSource = ProgressiveMediaSource.Factory(httpDataSource).createMediaSource(MediaItem.fromUri(Uri.parse(episode.url)))
        exoPlayer.apply {
            setMediaSource(mediaSource)
            playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)
        }
    }

    private fun updateContinueWatching() {
        currentEpisode?.let { episode ->
            continueWatchingViewModel.requestUpdateContinueWatching(
                contentId = episode.episodeId,
                title = episode.title,
                url = episode.url,
                thumbnail = episode.thumbnail,
                duration = exoPlayer.duration.toInt(),
                currentPosition = exoPlayer.currentPosition.toInt(),
                series = 1)
        }
    }

    private fun showTracksDialog() {
        val dialog = Dialog(this@TelevisionEpisodePlayerActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_tracks)
            setCancelable(true)
            setOnCancelListener {
                dismiss()
            }
        }

        val audioTracks = mutableListOf<String>()
        val subtitleTracks = mutableListOf<String>()
        val audioTracksGroup = mutableListOf<TracksGroup.Track>()
        val subtitleTracksGroup = mutableListOf<TracksGroup.Track>()

        for (i in 0 until exoPlayer.currentTracks.groups.size) {
            val format = exoPlayer.currentTracks.groups[i].getTrackFormat(0)
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
                val parameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setPreferredAudioLanguage(track.code)
                    .build()
                exoPlayer.trackSelectionParameters = parameters

                dialog.dismiss()
            }
        })
        dialog.findViewById<RecyclerView>(R.id.recycler_audio_tracks).apply {
            layoutManager = LinearLayoutManager(this@TelevisionEpisodePlayerActivity)
            adapter = audioTracksAdapter
        }
        audioTracksAdapter.differ.submitList(audioTracksGroup)

        val subtitlesTracksAdapter = TracksAdapter(listener = object : TracksAdapter.OnTrackClickListener {
            override fun onItemClick(track: TracksGroup.Track) {
                if (track.code.isNotEmpty()) {
                    val parameters = exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .setPreferredTextLanguage(track.code)
                        .build()
                    exoPlayer.trackSelectionParameters = parameters
                } else {
                    val parameters = exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .setPreferredTextLanguage(null)
                        .build()
                    exoPlayer.trackSelectionParameters = parameters
                }

                dialog.dismiss()
            }
        })
        dialog.findViewById<RecyclerView>(R.id.recycler_subtitles_tracks).apply {
            layoutManager = LinearLayoutManager(this@TelevisionEpisodePlayerActivity)
            adapter = subtitlesTracksAdapter
        }
        subtitlesTracksAdapter.differ.submitList(subtitleTracksGroup)

        dialog.window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes.gravity = Gravity.END
                setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
            }
        }
        dialog.show()
    }

    private fun finishActivityWithResult() {
        Intent().also { intent ->
            intent.putExtra("refreshContent", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    showTracksDialog()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    if (!exoPlayer.isPlaying) {
                        playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                    } else {
                        playerViewModel.setPlayerStatus(PlayerStatus.PAUSE)
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    exoPlayer.seekForward()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    exoPlayer.seekBack()
                    return true
                }
                KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK -> {
                    updateContinueWatching()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
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
        playerViewModel.setPlayerStatus(PlayerStatus.RELEASE)
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}