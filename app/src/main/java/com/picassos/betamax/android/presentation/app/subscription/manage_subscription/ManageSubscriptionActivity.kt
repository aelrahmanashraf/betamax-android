package com.picassos.betamax.android.presentation.app.subscription.manage_subscription

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.R
import com.picassos.betamax.android.R.string.ends_at
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.databinding.ActivityManageSubscriptionBinding
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.core.utilities.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageSubscriptionActivity : AppCompatActivity() {
    private lateinit var layout: ActivityManageSubscriptionBinding
    private val manageSubscriptionViewModel: ManageSubscriptionViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = SharedPreferences(this)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_manage_subscription)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@ManageSubscriptionActivity)
            }
        }

        val requestDialog = RequestDialog(this)

        layout.goBack.setOnClickListener { finish() }

        manageSubscriptionViewModel.requestCheckSubscription()
        collectLatestOnLifecycleStarted(manageSubscriptionViewModel.checkSubscription) { state ->
            if (state.isLoading) {
                requestDialog.show()

                layout.apply {
                    subscriptionContainer.visibility = View.VISIBLE
                    internetConnection.root.visibility = View.GONE
                }
            }
            if (state.response != null) {
                requestDialog.dismiss()

                val subscription = state.response
                layout.apply {
                    brandingText.text = getString(R.string.app_name).lowercase()
                    subscriptionPackage.text = when (subscription.subscriptionPackage) {
                        1 -> getString(R.string.silver)
                        2 -> getString(R.string.gold)
                        3 -> getString(R.string.diamond)
                        else -> getString(R.string.regular)
                    }
                    daysLeft.text = "${subscription.daysLeft} ${getString(R.string.days)}"
                    subscriptionEnd.text = getString(ends_at) + " " + Helper.getFormattedDateString(subscription.subscriptionEnd, "dd MMM yy, hh:mm a")
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()

                layout.apply {
                    subscriptionContainer.visibility = View.GONE
                    internetConnection.root.visibility = View.VISIBLE
                    internetConnection.tryAgain.setOnClickListener {
                        manageSubscriptionViewModel.requestCheckSubscription()
                    }
                }
                if (state.error == Response.MALFORMED_REQUEST_EXCEPTION) {
                    Firebase.crashlytics.log("Request returned a malformed request or response.")
                }
            }
        }
    }
}