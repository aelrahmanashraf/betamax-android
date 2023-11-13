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
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.utilities.Helper.requestedOrientationWithFullSensor
import com.picassos.betamax.android.core.utilities.Helper.toDips
import com.picassos.betamax.android.databinding.ActivityViewTvchannelBinding
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.model.SupportedVideoQualities
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.presentation.app.player.PlayerStatus
import com.picassos.betamax.android.presentation.app.player.PlayerViewModel
import com.picassos.betamax.android.presentation.app.tvchannel.related_tvchannels.RelatedTvChannelsAdapter
import com.picassos.betamax.android.presentation.app.quality.video_quality_chooser.VideoQualityChooserBottomSheetModal
import com.picassos.betamax.android.presentation.app.quality.video_quality_chooser.VideoQualityChooserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@DelicateCoroutinesApi
@AndroidEntryPoint
class ViewTvChannelActivity : AppCompatActivity() {
    private lateinit var layout: ActivityViewTvchannelBinding
    private val viewTvChannelViewModel: ViewTvChannelViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private val videoQualityChooserViewModel: VideoQualityChooserViewModel by viewModels()

    private var player: ExoPlayer? = null
    private lateinit var httpDataSource: DefaultHttpDataSource.Factory

    private lateinit var tvChannel: TvChannels.TvChannel
    private var selectedVideoQuality: Int = 0

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

        getSerializable(this@ViewTvChannelActivity, "tvchannel", TvChannels.TvChannel::class.java).also { tvChannel ->
            this@ViewTvChannelActivity.tvChannel = tvChannel
        }
        initializePlayer()

        viewTvChannelViewModel.requestTvGenres()
        collectLatestOnLifecycleStarted(viewTvChannelViewModel.tvGenres) { state ->
            if (state.response != null) {
                layout.genresList.apply {
                    removeAllViews()
                    addView(createGenreChip(Genres.Genre(
                        id = 0,
                        genreId = 0,
                        title = getString(R.string.all),
                        special = 0), isChecked = true))
                    state.response.genres.forEach { genre ->
                        addView(createGenreChip(genre))
                    }
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

                val tvChannels = state.response.tvChannels
                val relatedTvChannelsAdapter = RelatedTvChannelsAdapter(selectedPosition = tvChannels.indexOfId(tvChannel.tvChannelId), onClickListener = object: OnTvChannelClickListener {
                    override fun onItemClick(tvChannel: TvChannels.TvChannel) {
                        viewTvChannelViewModel.apply {
                            this@ViewTvChannelActivity.tvChannel = tvChannel
                            val tvChannelUrl = getTvChannelUrl(selectedVideoQuality, tvChannel)
                            playNewUrl(title = tvChannel.title, url = tvChannelUrl)
                        }
                    }
                })
                layout.recyclerRelatedTv.apply {
                    layoutManager = LinearLayoutManager(this@ViewTvChannelActivity)
                    adapter = relatedTvChannelsAdapter
                }
                relatedTvChannelsAdapter.differ.submitList(tvChannels)

                if (tvChannels.isEmpty()) {
                    layout.noItems.visibility = View.VISIBLE
                } else {
                    layout.noItems.visibility = View.GONE
                }
            }
            if (state.error != null) {
                layout.progressbar.visibility = View.GONE
            }
        }

        viewTvChannelViewModel.requestPreferredVideoQuality()
        collectLatestOnLifecycleStarted(viewTvChannelViewModel.preferredVideoQuality) { state ->
            if (state.response != null) {
                val quality = state.response
                selectedVideoQuality = quality

                val tvChannelUrl = getTvChannelUrl(quality, tvChannel)
                playNewUrl(title = tvChannel.title, url = tvChannelUrl)
            }
        }

        collectLatestOnLifecycleStarted(videoQualityChooserViewModel.selectedVideoQuality) { isSafe ->
            isSafe?.let { quality ->
                selectedVideoQuality = quality
                when (quality) {
                    1 -> playNewUrl(url = tvChannel.sdUrl)
                    2 -> playNewUrl(url = tvChannel.hdUrl)
                    3 -> playNewUrl(url = tvChannel.fhdUrl)
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
    private fun initializePlayer() {
        player?.apply {
            playWhenReady = false
            pause()
            clearMediaItems()
        }

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
        val trackSelector = DefaultTrackSelector(this@ViewTvChannelActivity, AdaptiveTrackSelection.Factory() as ExoTrackSelection.Factory)
        val renderersFactory = DefaultRenderersFactory(this@ViewTvChannelActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        }
        httpDataSource = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(this@ViewTvChannelActivity, tvChannel.userAgent))
            .setAllowCrossProtocolRedirects(true)
        val mediaSource = HlsMediaSource.Factory(httpDataSource).createMediaSource(
            MediaItem.fromUri(Uri.EMPTY)
        )

        player = ExoPlayer.Builder(this@ViewTvChannelActivity, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build().apply {
                addListener(playerListener)
                setMediaSource(mediaSource)
            }
        playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)

        layout.playerView.apply {
            player = this@ViewTvChannelActivity.player
            setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                layout.controllerContainer.apply {
                    when (visibility) {
                        View.VISIBLE -> animate().alpha(1F).duration = 400
                        View.GONE -> animate().alpha(0F).duration = 400
                    }
                }
            })
        }

        collectLatestOnLifecycleStarted(playerViewModel.playerStatus) { status ->
            when (status) {
                PlayerStatus.INITIALIZE -> {

                }
                PlayerStatus.PREPARE -> {
                    player?.prepare()
                    layout.playerAction.setOnClickListener {
                        when (player?.isPlaying) {
                            true -> playerViewModel.setPlayerStatus(PlayerStatus.PAUSE)
                            else -> playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                        }
                    }
                    playerViewModel.setPlayerStatus(PlayerStatus.PLAY)
                }
                PlayerStatus.PLAY -> {
                    player?.apply {
                        playWhenReady = true
                        play()
                    }
                }
                PlayerStatus.PAUSE -> {
                    player?.apply {
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
                    player?.apply {
                        stop()
                        removeListener(playerListener)
                        clearMediaItems()
                        release()
                    }
                }
            }
        }

        layout.playerView.findViewById<ImageView>(R.id.fullscreen_mode).setOnClickListener {
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> requestedOrientationWithFullSensor(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                Configuration.ORIENTATION_LANDSCAPE -> requestedOrientationWithFullSensor(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            }
        }

        layout.playerOptions.setOnClickListener {
            val videoQualityChooserBottomSheetModal = VideoQualityChooserBottomSheetModal()
            videoQualityChooserBottomSheetModal.arguments = Bundle().apply {
                putSerializable("qualities", SupportedVideoQualities(
                    sdQuality = tvChannel.sdUrl.isNotEmpty(),
                    hdQuality = tvChannel.hdUrl.isNotEmpty(),
                    fhdQuality = tvChannel.fhdUrl.isNotEmpty()))
            }
            videoQualityChooserBottomSheetModal.show(supportFragmentManager, "video_quality_chooser")
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

    private fun playNewUrl(url: String, title: String = "") {
        if (title.isNotEmpty()) {
            layout.playerTitle.text = title
        }
        player?.apply {
            stop()
            clearMediaItems()
        }
        val mediaSource = HlsMediaSource.Factory(httpDataSource).createMediaSource(
            MediaItem.fromUri(Uri.parse(url))
        )
        player?.apply {
            setMediaSource(mediaSource)
            playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)
        }
    }

    fun getTvChannelUrl(selectedVideoQuality: Int, tvChannel: TvChannels.TvChannel): String {
        return when (selectedVideoQuality) {
            1 -> tvChannel.sdUrl.ifEmpty { tvChannel.hdUrl.takeIf { it.isNotEmpty() } ?: tvChannel.fhdUrl.takeIf { it.isNotEmpty() } } ?: ""
            2 -> tvChannel.hdUrl.takeIf { it.isNotEmpty() } ?: tvChannel.fhdUrl.takeIf { it.isNotEmpty() } ?: tvChannel.sdUrl
            3 -> tvChannel.fhdUrl.ifEmpty { tvChannel.hdUrl.takeIf { it.isNotEmpty() } ?: tvChannel.sdUrl }
            else -> ""
        }
    }

    @SuppressLint("InflateParams")
    private fun createGenreChip(genre: Genres.Genre, isChecked: Boolean = false): Chip {
        val chip = this@ViewTvChannelActivity.layoutInflater.inflate(R.layout.item_genre_selectable, null, false) as Chip
        return chip.apply {
            id = ViewCompat.generateViewId()
            text = genre.title
            setOnCheckedChangeListener { _, _ ->
                viewTvChannelViewModel.apply {
                    setSelectedGenre(genre.genreId)
                    when (genre.genreId) {
                        0 -> requestTvChannels()
                        else -> requestTvChannelsByGenre(selectedGenre.value)
                    }
                }
            }
            this.isChecked = isChecked || (viewTvChannelViewModel.selectedGenre.value == genre.genreId)
        }
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout.apply {
                goBack.visibility = View.VISIBLE
                playerContainer.layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (200f).toDips(resources))
                playerView.apply player@ {
                    this@player.findViewById<ImageView>(R.id.fullscreen_mode).setImageResource(R.drawable.icon_fullscreen_filled)
                }
                Helper.showSystemUI(window, root)
            }
        } else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout.apply {
                goBack.visibility = View.GONE
                playerContainer.layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                playerView.apply player@ {
                    this@player.findViewById<ImageView>(R.id.fullscreen_mode).setImageResource(R.drawable.icon_fit_to_width_filled)
                }
                Helper.hideSystemUI(window, root)
            }
        }
    }

    private fun List<TvChannels.TvChannel>.indexOfId(id: Int): Int {
        for (i in indices) {
            if (this[i].tvChannelId == id) {
                return i
            }
        }
        return -1
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