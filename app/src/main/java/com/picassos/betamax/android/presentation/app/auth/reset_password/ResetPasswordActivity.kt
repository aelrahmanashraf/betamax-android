package com.picassos.betamax.android.presentation.app.auth.reset_password

import com.picassos.betamax.android.core.view.Toasto.showToast
import androidx.appcompat.app.AppCompatActivity
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import android.os.Bundle
import com.picassos.betamax.android.R
import android.content.Intent
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.databinding.ActivityResetPasswordBinding
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.presentation.app.auth.signin.SigninActivity
import com.google.firebase.crashlytics.ktx.crashlytics
import com.picassos.betamax.android.core.utilities.Helper
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var layout: ActivityResetPasswordBinding
    private val resetPasswordViewModel: ResetPasswordViewModel by viewModels()

    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView<ActivityResetPasswordBinding?>(this, R.layout.activity_reset_password).apply {
            lifecycleOwner = this@ResetPasswordActivity
            viewModel = resetPasswordViewModel
        }

        val requestDialog = RequestDialog(this)

        getSerializable(this, "account", Account::class.java).also { account ->
            resetPasswordViewModel.setAccount(account)
        }

        collectLatestOnLifecycleStarted(resetPasswordViewModel.account) { isSafe ->
            isSafe?.let { account ->
                this.account = account
            }
        }

        collectLatestOnLifecycleStarted(resetPasswordViewModel.resetPassword) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode) {
                    200 -> {
                        showToast(this, getString(R.string.password_updated_successfully), 0, 0)

                        Intent(this@ResetPasswordActivity, SigninActivity::class.java).also { intent ->
                            startActivity(intent)
                            finishAffinity()
                        }
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
                        showToast(this@ResetPasswordActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@ResetPasswordActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@ResetPasswordActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@ResetPasswordActivity)
    }
}
