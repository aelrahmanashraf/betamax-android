package com.picassos.betamax.android.presentation.app.auth.profile_info

import com.picassos.betamax.android.core.view.Toasto.showToast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.R
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Response
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ActivityProfileInfoBinding
import com.picassos.betamax.android.di.AppEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class ProfileInfoActivity : AppCompatActivity() {
    private lateinit var layout: ActivityProfileInfoBinding
    private val profileInfoViewModel: ProfileInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this@ProfileInfoActivity, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView<ActivityProfileInfoBinding?>(this, R.layout.activity_profile_info).apply {
            lifecycleOwner = this@ProfileInfoActivity
            viewModel = profileInfoViewModel
        }

        val requestDialog = RequestDialog(this)

        layout.goBack.setOnClickListener { finish() }

        collectLatestOnLifecycleStarted(entryPoint.getAccountUseCase().invoke()) { account ->
            layout.apply {
                email.setText(account.emailAddress)
                username.setText(account.username)
            }
        }

        collectLatestOnLifecycleStarted(profileInfoViewModel.updateProfileInfo) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode) {
                    200 -> {
                        showToast(this, getString(R.string.profile_info_saved_successfully), 0, 0)
                        finish()
                    }
                    else -> {
                        showToast(this, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@ProfileInfoActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@ProfileInfoActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@ProfileInfoActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@ProfileInfoActivity)
    }
}