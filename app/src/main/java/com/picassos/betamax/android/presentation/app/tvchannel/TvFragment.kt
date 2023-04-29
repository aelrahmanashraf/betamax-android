package com.picassos.betamax.android.presentation.app.tvchannel

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import com.picassos.betamax.android.R
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleFragmentOwnerStarted
import com.picassos.betamax.android.databinding.FragmentTvBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.presentation.app.tvchannel.tvchannels.TvChannelsAdapter
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.core.view.Toasto
import com.picassos.betamax.android.domain.listener.OnTvChannelClickListener
import com.picassos.betamax.android.presentation.app.profile.ProfileActivity
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import com.picassos.betamax.android.presentation.app.tvchannel.view_tvchannel.ViewTvChannelActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TvFragment : Fragment() {
    private lateinit var layout: FragmentTvBinding
    private val tvViewModel: TvViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_tv, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = SharedPreferences(requireContext())
        val entryPoint = EntryPointAccessors.fromApplication(requireContext(), AppEntryPoint::class.java)

        val requestDialog = RequestDialog(requireContext())

        collectLatestOnLifecycleFragmentOwnerStarted(entryPoint.getAccountUseCase().invoke()) { account ->
            layout.profileIcon.apply {
                text = Helper.characterIcon(account.username).uppercase()
                setOnClickListener {
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                }
            }
        }

        val tvAdapter = TvChannelsAdapter(onClickListener = object: OnTvChannelClickListener {
            override fun onItemClick(tvChannel: TvChannels.TvChannel) {
                tvViewModel.requestCheckSubscription()
                lifecycleScope.launch {
                    tvViewModel.checkSubscription.collectLatest { state ->
                        if (state.isLoading) {
                            requestDialog.show()
                        }
                        if (state.response != null) {
                            requestDialog.dismiss()

                            val subscription = state.response
                            when (subscription.daysLeft) {
                                0 -> startActivity(Intent(requireContext(), SubscribeActivity::class.java))
                                else -> {
                                    Intent(requireContext(), ViewTvChannelActivity::class.java).also { intent ->
                                        intent.putExtra("tvchannel", tvChannel)
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                        if (state.error != null) {
                            requestDialog.dismiss()
                            when (state.error) {
                                Response.NETWORK_FAILURE_EXCEPTION -> {
                                    Toasto.showToast(requireContext(), getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                                }
                                Response.MALFORMED_REQUEST_EXCEPTION -> {
                                    Toasto.showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                                }
                                else -> {
                                    Toasto.showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                                }
                            }
                        }
                    }
                }
            }
        })
        layout.recyclerTv.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = tvAdapter
        }

        tvViewModel.requestTvChannels()
        collectLatestOnLifecycleFragmentOwnerStarted(tvViewModel.tvChannels) { state ->
            if (state.isLoading) {
                layout.apply {
                    refreshLayout.isRefreshing = true
                    recyclerTv.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                layout.refreshLayout.isRefreshing = false

                val tvChannels = state.response.tvChannels
                tvAdapter.differ.submitList(tvChannels)
            }
            if (state.error != null) {
                layout.apply {
                    refreshLayout.isRefreshing = false
                    recyclerTv.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        tvViewModel.requestTvChannels()
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }

        layout.refreshLayout.apply {
            elevation = 0f
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.color_theme))
            when (sharedPreferences.loadDarkMode()) {
                1 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_white))
                2 -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_darker))
                3 -> {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_darker))
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.color_white))
                    }
                }
            }
            setOnRefreshListener {
                tvViewModel.requestTvChannels()
            }
        }
    }
}