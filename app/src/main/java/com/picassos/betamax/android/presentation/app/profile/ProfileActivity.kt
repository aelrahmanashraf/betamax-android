package com.picassos.betamax.android.presentation.app.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.picassos.betamax.android.BuildConfig
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Intents.openEmailAddress
import com.picassos.betamax.android.core.utilities.Intents.openWebBrowser
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityProfileBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.model.Subscription
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.core.view.dialog.TelevisionSubscriptionDialog
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.presentation.app.auth.account_settings.AccountSettingsBottomSheetModal
import com.picassos.betamax.android.presentation.app.quality.manage_video_quality.ManageVideoQualityBottomSheetModal
import com.picassos.betamax.android.presentation.app.info.about.AboutActivity
import com.picassos.betamax.android.presentation.app.launch.LaunchActivity
import com.picassos.betamax.android.presentation.app.subscription.manage_subscription.ManageSubscriptionActivity
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import com.picassos.betamax.android.presentation.television.auth.account_settings.TelevisionAccountSettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private lateinit var layout: ActivityProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var subscription: Subscription

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this@ProfileActivity, AppEntryPoint::class.java)
        val sharedPreferences = SharedPreferences(this)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView<ActivityProfileBinding>(this, R.layout.activity_profile).apply {
            lifecycleOwner = this@ProfileActivity
            viewModel = profileViewModel
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@ProfileActivity)
            }
        }

        val requestDialog = RequestDialog(this)

        layout.goBack.setOnClickListener { finish() }

        profileViewModel.requestCheckSubscription()
        collectLatestOnLifecycleStarted(profileViewModel.checkSubscription) { state ->
            if (state.response != null) {
                subscription = state.response

                layout.apply {
                    premiumBadge.visibility = when (state.response.daysLeft) {
                        0 -> View.GONE
                        else -> View.VISIBLE
                    }
                    manageSubscription.isEnabled = true
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(entryPoint.getConfigurationUseCase().invoke(), entryPoint.getAccountUseCase().invoke()) { configuration, account ->
                    layout.apply {
                        accountSettings.setOnClickListener {
                            if (!Helper.isTelevision(this@ProfileActivity)) {
                                val accountSettingsBottomSheetModal = AccountSettingsBottomSheetModal()
                                accountSettingsBottomSheetModal.show(supportFragmentManager, "TAG")
                            } else {
                                startActivity(Intent(this@ProfileActivity, TelevisionAccountSettingsActivity::class.java))
                            }
                        }
                        manageSubscription.setOnClickListener {
                            if (subscription.daysLeft == 0) {
                                if (!Helper.isTelevision(this@ProfileActivity)) {
                                    startActivityForResult.launch(Intent(this@ProfileActivity, SubscribeActivity::class.java))
                                } else {
                                    val televisionSubscriptionDialog = TelevisionSubscriptionDialog(this@ProfileActivity)
                                    televisionSubscriptionDialog.show()
                                }
                            } else {
                                startActivityForResult.launch(Intent(this@ProfileActivity, ManageSubscriptionActivity::class.java))
                            }
                        }
                        manageVideoQuality.setOnClickListener {
                            val manageVideoQualityBottomSheetModal = ManageVideoQualityBottomSheetModal()
                            manageVideoQualityBottomSheetModal.show(supportFragmentManager, "manage_video_quality")
                        }
                        sendFeedback.setOnClickListener {
                            openEmailAddress(
                                email = configuration.email,
                                subject = getString(R.string.feedback_email_subject),
                                text = getString(R.string.feedback_email_body))
                        }
                        privacyPolicy.setOnClickListener {
                            openWebBrowser(url = configuration.privacyURL)
                        }
                        helpCentre.setOnClickListener {
                            openWebBrowser(url = configuration.helpURL)
                        }
                        about.setOnClickListener {
                            startActivity(Intent(this@ProfileActivity, AboutActivity::class.java))
                        }
                        signout.setOnClickListener {
                            profileViewModel.requestSignout()
                        }
                        developedByContainer.visibility = View.VISIBLE
                        developedByAuthor.apply {
                            visibility = View.VISIBLE
                            text = "${getString(R.string.by)} ${configuration.developedBy}"
                        }
                        copyright.text = Helper.copyright(this@ProfileActivity)
                        version.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
                    }
                }.collect()
            }
        }

        collectLatestOnLifecycleStarted(profileViewModel.profile) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.response != null) {
                requestDialog.dismiss()
            }
            if (state.error != null) {
                requestDialog.dismiss()
            }
        }

        collectLatestOnLifecycleStarted(profileViewModel.signout) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode) {
                    200 -> {
                        lifecycleScope.launch {
                            entryPoint.setAccountUseCase().invoke(Gson().toJson(Account()))

                            sharedPreferences.apply {
                                setSubscription(false)
                            }

                            Intent(this@ProfileActivity, LaunchActivity::class.java).also { intent ->
                                startActivity(intent)
                                finishAffinity()
                            }
                        }
                    }
                    else -> {
                        showToast(this@ProfileActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@ProfileActivity, "tes", 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@ProfileActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@ProfileActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    private var startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if (result != null && result.resultCode == RESULT_OK) {
            profileViewModel.requestCheckSubscription()
        }
    }
}