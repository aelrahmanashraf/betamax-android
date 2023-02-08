package com.picassos.betamax.android.presentation.app.auth.register

import android.content.Intent
import com.picassos.betamax.android.core.view.Toasto.showToast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.R
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.databinding.ActivityRegisterBinding
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.presentation.app.main.MainActivity
import com.picassos.betamax.android.core.utilities.Response
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.core.utilities.Helper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var layout: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView<ActivityRegisterBinding?>(this, R.layout.activity_register).apply {
            lifecycleOwner = this@RegisterActivity
            viewModel = registerViewModel
        }

        val requestDialog = RequestDialog(this)

        layout.agreementNotice.movementMethod = LinkMovementMethod.getInstance()

        collectLatestOnLifecycleStarted(registerViewModel.register) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.response != null) {
                requestDialog.dismiss()
                when (state.response.responseCode) {
                    200 -> {
                        layout.registerResponse.apply {
                            visibility = View.GONE
                            text = ""
                        }
                        Intent(this@RegisterActivity, MainActivity::class.java).also { intent ->
                            startActivity(intent)
                            finishAffinity()
                        }
                    }
                    403 -> {
                        layout.registerResponse.apply {
                            visibility = View.VISIBLE
                            text = getString(R.string.user_exists)
                        }
                    }
                    else -> {
                        layout.registerResponse.apply {
                            visibility = View.VISIBLE
                            text = getString(R.string.unknown_issue_occurred)
                        }
                    }
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@RegisterActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@RegisterActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@RegisterActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@RegisterActivity)
    }
}
