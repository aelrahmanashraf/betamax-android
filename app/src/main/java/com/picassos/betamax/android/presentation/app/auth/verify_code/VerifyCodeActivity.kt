package com.picassos.betamax.android.presentation.app.auth.verify_code

import android.annotation.SuppressLint
import com.picassos.betamax.android.core.view.Toasto.showToast
import androidx.appcompat.app.AppCompatActivity
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import android.os.Bundle
import com.picassos.betamax.android.R
import android.content.Intent
import android.os.CountDownTimer
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.databinding.ActivityVerifyCodeBinding
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.presentation.app.auth.reset_password.ResetPasswordActivity
import com.picassos.betamax.android.presentation.app.auth.send_reset_email.SendResetEmailViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyCodeActivity : AppCompatActivity() {
    private lateinit var layout: ActivityVerifyCodeBinding
    private val verificationCodeViewModel: VerifyCodeViewModel by viewModels()
    private val sendResetEmailViewModel: SendResetEmailViewModel by viewModels()

    private lateinit var account: Account
    private var request = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView<ActivityVerifyCodeBinding?>(this, R.layout.activity_verify_code).apply {
            lifecycleOwner = this@VerifyCodeActivity
            viewModel = verificationCodeViewModel
        }

        val requestDialog = RequestDialog(this)

        getSerializable(this, "account", Account::class.java).also { account ->
            verificationCodeViewModel.setAccount(account)
        }

        startTimer()

        collectLatestOnLifecycleStarted(verificationCodeViewModel.account) { isSafe ->
            isSafe?.let { account ->
                this.account = account
            }

            layout.emailVerification.text =
                getString(R.string.please_enter_the_6_digit_code_sent_to_you_at) + " ${account.emailAddress}"
        }

        intent.extras?.let { extras ->
            verificationCodeViewModel.apply {
                extras.getString("request")?.let { request ->
                    setRequest(request)
                }
            }
        }

        collectLatestOnLifecycleStarted(verificationCodeViewModel.request) { isSafe ->
            isSafe?.let { request ->
                this.request = request
            }
        }

        collectLatestOnLifecycleStarted(verificationCodeViewModel.verifyCode) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.response != null) {
                requestDialog.dismiss()
                when (state.response.responseCode) {
                    403 -> {
                        showToast(this, getString(R.string.verification_code_expired), 0, 1)
                    }
                    else -> {
                        when (request) {
                            "reset_password" -> {
                                Intent(this@VerifyCodeActivity, ResetPasswordActivity::class.java).also { intent ->
                                    intent.putExtra("account", state.response)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@VerifyCodeActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@VerifyCodeActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@VerifyCodeActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }

        collectLatestOnLifecycleStarted(sendResetEmailViewModel.sendResetEmail) { state ->
            if (state.isLoading) {
                startTimer()
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode.toInt()) {
                    200 -> {
                        showToast(this, getString(R.string.email_resent), 0, 2)
                        startTimer()
                    }
                    404 -> {
                        showToast(this, getString(R.string.no_users_found), 0, 1)
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
                        showToast(this, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    private fun startTimer() {
        layout.resendAgain.visibility = View.GONE
        object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                layout.resendAgainTimer.text = getString(R.string.resend_code_in) + millisUntilFinished / 1000
            }
            override fun onFinish() {
                layout.resendAgain.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        sendResetEmailViewModel.requestSendResetEmail(account.emailAddress)
                    }
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@VerifyCodeActivity)
    }
}
