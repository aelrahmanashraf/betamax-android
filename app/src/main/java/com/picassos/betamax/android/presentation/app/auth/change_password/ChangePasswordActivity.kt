package com.picassos.betamax.android.presentation.app.auth.change_password

import com.picassos.betamax.android.core.view.Toasto.showToast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.R
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.databinding.ActivityChangePasswordBinding
import com.picassos.betamax.android.core.utilities.Response
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.utilities.Helper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var layout: ActivityChangePasswordBinding
    private val changePasswordViewModel: ChangePasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView<ActivityChangePasswordBinding?>(this, R.layout.activity_change_password).apply {
            lifecycleOwner = this@ChangePasswordActivity
            viewModel = changePasswordViewModel
        }

        val requestDialog = RequestDialog(this)

        layout.goBack.setOnClickListener { finish() }

        collectLatestOnLifecycleStarted(changePasswordViewModel.changePassword) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode) {
                    200 -> {
                        showToast(this, getString(R.string.password_updated_successfully), 0, 0)
                        finish()
                    }
                    403 -> {
                        showToast(this, getString(R.string.current_password_wrong), 0, 1)
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
                        showToast(this@ChangePasswordActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@ChangePasswordActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@ChangePasswordActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@ChangePasswordActivity)
    }
}