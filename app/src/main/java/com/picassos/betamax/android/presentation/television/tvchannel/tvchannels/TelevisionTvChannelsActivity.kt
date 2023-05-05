package com.picassos.betamax.android.presentation.television.tvchannel.tvchannels

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.databinding.ActivityTelevisionTvchannelsBinding
import com.picassos.betamax.android.domain.listener.OnGenreClickListener
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener
import com.picassos.betamax.android.domain.listener.OnTvChannelLongClickListener
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.model.QualityGroup
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.presentation.app.player.PlayerStatus
import com.picassos.betamax.android.presentation.app.player.PlayerViewModel
import com.picassos.betamax.android.presentation.television.genre.tvchannels_genres.TelevisionTvChannelsGenresAdapter
import com.picassos.betamax.android.presentation.app.quality.QualityAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TelevisionTvChannelsActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionTvchannelsBinding
    private val televisionTvChannelsViewModel: TelevisionTvChannelsViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    private var exoPlayer: ExoPlayer? = null
    private lateinit var httpDataSource: DefaultHttpDataSource.Factory

    private var tvChannel: TvChannels.TvChannel? = null
    private var selectedVideoQuality: Int = 0

    private var isFirstLaunch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_tvchannels)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        initializePlayer()

        layout.apply {
            tvchannelsFavorites.setOnClickListener {
                televisionTvChannelsViewModel.apply {
                    setSelectedNavigation(navigation = Navigations.FavoritesNavigation)
                    requestSavedTvChannels()
                }
            }
            allTvchannels.setOnClickListener {
                televisionTvChannelsViewModel.apply {
                    setSelectedNavigation(navigation = Navigations.HomeNavigation)
                    requestTvChannels()
                }
            }
        }

        val genresAdapter = TelevisionTvChannelsGenresAdapter(onClickListener = object: OnGenreClickListener {
            override fun onItemClick(genre: Genres.Genre) {
                televisionTvChannelsViewModel.apply {
                    setSelectedNavigation(navigation = Navigations.HomeNavigation)
                    requestTvChannelsByGenre(genre.genreId)
                }
            }
        })
        layout.recyclerGenres.apply {
            layoutManager = LinearLayoutManager(this@TelevisionTvChannelsActivity)
            adapter = genresAdapter
        }

        televisionTvChannelsViewModel.apply {
            requestTvGenres()
            collectLatestOnLifecycleStarted(tvGenres) { state ->
                if (state.isLoading) {
                    layout.genresProgressbar.visibility = View.VISIBLE
                }
                if (state.response != null) {
                    layout.genresProgressbar.visibility = View.GONE

                    val genres = state.response.genres
                    genresAdapter.differ.submitList(genres)
                }
            }
        }

        televisionTvChannelsViewModel.apply {
            setSelectedNavigation(navigation = Navigations.HomeNavigation)
            requestTvChannels()
        }
        collectLatestOnLifecycleStarted(televisionTvChannelsViewModel.tvChannels) { state ->
            if (state.isLoading) {
                layout.tvchannelsProgressbar.visibility = View.VISIBLE
            }
            if (state.response != null) {
                layout.tvchannelsProgressbar.visibility = View.GONE

                val tvChannels = state.response.tvChannels
                if (isFirstLaunch) {
                    playNewUrl(tvChannels[0].title,url = getTvChannelUrl(selectedVideoQuality, tvChannels[0]))
                    isFirstLaunch = false
                }
                val selectedPosition = tvChannel?.let { tvChannels.indexOfId(it.tvChannelId) } ?: run { 0 }

                val tvChannelsAdapter = TelevisionTvChannelsAdapter(selectedPosition = selectedPosition, onClickListener = object: OnTvChannelClickListener {
                    override fun onItemClick(tvChannel: TvChannels.TvChannel) {
                        televisionTvChannelsViewModel.apply {
                            this@TelevisionTvChannelsActivity.tvChannel = tvChannel
                            val tvChannelUrl = getTvChannelUrl(selectedVideoQuality, tvChannel)
                            playNewUrl(title = tvChannel.title, url = tvChannelUrl)
                        }
                        layout.tvchannelsNavContainer.visibility = View.GONE
                    }
                }, onLongClickListener = object: OnTvChannelLongClickListener {
                    override fun onItemLongClick(tvChannel: TvChannels.TvChannel) {
                        televisionTvChannelsViewModel.apply {
                            requestSaveTvChannel(tvChannelId = tvChannel.tvChannelId)
                        }
                    }
                })
                layout.recyclerTv.apply {
                    layoutManager = LinearLayoutManager(this@TelevisionTvChannelsActivity)
                    adapter = tvChannelsAdapter
                }
                tvChannelsAdapter.differ.submitList(tvChannels)
            }
        }

        televisionTvChannelsViewModel.requestPreferredVideoQuality()
        collectLatestOnLifecycleStarted(televisionTvChannelsViewModel.preferredVideoQuality) { state ->
            if (state.response != null) {
                selectedVideoQuality = state.response
            }
        }

        collectLatestOnLifecycleStarted(televisionTvChannelsViewModel.saveTvChannel) { state ->
            if (state.responseCode != null) {
                when (state.responseCode) {
                    "1" -> {
                        showToast(this@TelevisionTvChannelsActivity, getString(R.string.tvchannel_add_to_favorites), 0, 0)
                    }
                    "0" -> {
                        showToast(this@TelevisionTvChannelsActivity, getString(R.string.tvchannel_removed_from_favorites), 0, 2)
                    }
                }
                televisionTvChannelsViewModel.apply {
                    if (selectedNavigation.value == Navigations.FavoritesNavigation) {
                        requestSavedTvChannels()
                    }
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

    private fun initializePlayer() {
        exoPlayer?.apply {
            playWhenReady = false
            pause()
            clearMediaItems()
        }
        val trackSelector = DefaultTrackSelector(this@TelevisionTvChannelsActivity, AdaptiveTrackSelection.Factory() as ExoTrackSelection.Factory)
        val renderersFactory = DefaultRenderersFactory(this@TelevisionTvChannelsActivity).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }
        httpDataSource = DefaultHttpDataSource.Factory().setUserAgent(Util.getUserAgent(this@TelevisionTvChannelsActivity, tvChannel?.userAgent ?: ""))
        val mediaSource = HlsMediaSource.Factory(httpDataSource).createMediaSource(MediaItem.fromUri(Uri.EMPTY))

        exoPlayer = ExoPlayer.Builder(this@TelevisionTvChannelsActivity, renderersFactory)
            .setTrackSelector(trackSelector)
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
                    exoPlayer?.apply {
                        removeListener(playerListener)
                        clearMediaItems()
                    }
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

    private fun playNewUrl(title: String = "", url: String) {
        lifecycleScope.launch {
            if (title.isNotEmpty()) {
                layout.playerTitle.text = title
            }
            val mediaSource = HlsMediaSource.Factory(httpDataSource).createMediaSource(MediaItem.fromUri(Uri.parse(url)))
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                playerViewModel.setPlayerStatus(PlayerStatus.PREPARE)
            }
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

    private fun showChannelOptionsDialog(tvChannel: TvChannels.TvChannel) {
        val dialog = Dialog(this@TelevisionTvChannelsActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_television_tvchannel_options)
            setCancelable(true)
            setOnCancelListener {
                dismiss()
            }
        }

        val qualityGroup = mutableListOf<QualityGroup.Quality>().apply {
            if (tvChannel.sdUrl.isNotEmpty()) {
                add(QualityGroup.Quality(id = 1, prefix = "sd", title = getString(R.string.sd)))
            }
            if (tvChannel.hdUrl.isNotEmpty()) {
                add(QualityGroup.Quality(id = 1, prefix = "hd", title = getString(R.string.hd)))
            }
            if (tvChannel.fhdUrl.isNotEmpty()) {
                add(QualityGroup.Quality(id = 1, prefix = "fhd", title = getString(R.string.fhd)))
            }
        }

        val qualityAdapter = QualityAdapter(listener = object : QualityAdapter.OnQualityClickListener {
            override fun onItemClick(quality: QualityGroup.Quality) {
                televisionTvChannelsViewModel.apply {
                    selectedVideoQuality = quality.id
                    val tvChannelUrl = getTvChannelUrl(quality.id, tvChannel)
                    playNewUrl(title = tvChannel.title, url = tvChannelUrl)
                }
                dialog.dismiss()
            }
        })
        dialog.findViewById<RecyclerView>(R.id.recycler_quality).apply {
            layoutManager = LinearLayoutManager(this@TelevisionTvChannelsActivity)
            adapter = qualityAdapter
        }
        qualityAdapter.differ.submitList(qualityGroup)

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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (layout.tvchannelsNavContainer.visibility == View.GONE) {
                    tvChannel?.let { tvChannel ->
                        showChannelOptionsDialog(tvChannel)
                    }
                }
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                layout.tvchannelsNavContainer.apply {
                    if (visibility == View.GONE) visibility = View.VISIBLE
                }
            }
            KeyEvent.KEYCODE_ESCAPE,
            KeyEvent.KEYCODE_BACK -> {
                layout.tvchannelsNavContainer.apply {
                    if (visibility == View.VISIBLE) visibility = View.GONE else finish()
                }
            }
        }
        return false
    }

    private fun List<TvChannels.TvChannel>.indexOfId(id: Int): Int {
        for (i in indices) {
            if (this[i].tvChannelId == id) {
                return i
            }
        }
        return -1
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