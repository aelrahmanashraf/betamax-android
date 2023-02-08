package com.picassos.betamax.android.presentation.app.auth.signin

import com.picassos.betamax.android.core.view.Toasto.showToast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.R
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.databinding.ActivitySigninBinding
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.presentation.app.main.MainActivity
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.presentation.app.auth.send_reset_email.SendResetEmailViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.validation.FormValidation
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.usecase.form_validation.ValidateEmail
import com.picassos.betamax.android.presentation.app.auth.register.RegisterActivity
import com.picassos.betamax.android.presentation.app.auth.verify_code.VerifyCodeActivity
import com.picassos.betamax.android.presentation.television.main.TelevisionMainActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlin.system.exitProcess

@AndroidEntryPoint
class SigninActivity : AppCompatActivity() {
    private lateinit var layout: ActivitySigninBinding
    private val signinViewModel: SigninViewModel by viewModels()
    private val sendResetEmailViewModel: SendResetEmailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView<ActivitySigninBinding?>(this, R.layout.activity_signin).apply {
            lifecycleOwner = this@SigninActivity
            viewModel = signinViewModel
        }

        val requestDialog = RequestDialog(this)

        collectLatestOnLifecycleStarted(entryPoint.getConfigurationUseCase().invoke()) { configuration ->
            if (!Helper.verifyLicense(configuration.developedBy)) {
                finishAffinity()
                exitProcess(0)
            }
        }

        collectLatestOnLifecycleStarted(signinViewModel.signin) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.response != null) {
                requestDialog.dismiss()
                when (state.response.responseCode) {
                    200 -> {
                        layout.signinResponse.apply {
                            visibility = View.GONE
                            text = ""
                        }
                        Intent().also { intent ->
                            when (Helper.isTelevision(this@SigninActivity)) {
                                true -> intent.setClass(this@SigninActivity, MainActivity::class.java)
                                false -> intent.setClass(this@SigninActivity, MainActivity::class.java)
                            }
                            startActivity(intent)
                            finishAffinity()
                        }
                    }
                    403 -> {
                        layout.signinResponse.apply {
                            visibility = View.VISIBLE
                            text = getString(R.string.wrong_password)
                        }
                    }
                    404 -> {
                        layout.signinResponse.apply {
                            visibility = View.VISIBLE
                            text = getString(R.string.no_users_found)
                        }
                    }
                    503 -> {
                        layout.signinResponse.apply {
                            visibility = View.VISIBLE
                            text = getString(R.string.you_have_exceeded_your_devices_limit)
                        }
                    }
                    else -> {
                        layout.signinResponse.apply {
                            visibility = View.GONE
                            text = ""
                        }
                        showToast(this@SigninActivity, getString(R.string.unknown_issue_occurred), 0,1)
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@SigninActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@SigninActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                }
            }
        }

        layout.resetPassword.setOnClickListener {
            val validateEmailResult = ValidateEmail().execute(layout.email.text.toString())
            val hasError = listOf(
                validateEmailResult).any { result -> !result.successful }

            if (hasError) {
                when (validateEmailResult.errorMessage) {
                    FormValidation.EMAIL_EMPTY -> showToast(this@SigninActivity, getString(R.string.enter_email_address), 0, 1)
                    FormValidation.EMAIL_INVALID -> showToast(this@SigninActivity, getString(R.string.invalid_email), 0, 1)
                }
            } else {
                sendResetEmailViewModel.requestSendResetEmail(
                    layout.email.text.toString())
            }
        }

        collectLatestOnLifecycleStarted(sendResetEmailViewModel.sendResetEmail) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()
                when (state.responseCode.toInt()) {
                    200 -> {
                        val accountDetails = Account(
                            emailAddress = layout.email.text.toString().trim())
                        Intent(this@SigninActivity, VerifyCodeActivity::class.java).also { intent ->
                            intent.putExtra("request", "reset_password")
                            intent.putExtra("account", accountDetails)
                            startActivity(intent)
                        }
                    }
                    404 -> {
                        showToast(this, getString(R.string.no_users_found), 0,1)
                    }
                    else -> {
                        showToast(this, getString(R.string.unknown_issue_occurred), 0,1)
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@SigninActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@SigninActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@SigninActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }

        layout.apply {
            register.setOnClickListener {
                Intent(this@SigninActivity, RegisterActivity::class.java).also { intent ->
                    startActivity(intent)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@SigninActivity)
    }
}
