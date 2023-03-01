package com.picassos.betamax.android.presentation.app.launch

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Response.CREDENTIALS_NOT_SET
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.databinding.ActivityLaunchBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.presentation.app.auth.signin.SigninActivity
import com.picassos.betamax.android.presentation.app.main.MainActivity
import com.picassos.betamax.android.presentation.television.main.TelevisionMainActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class LaunchActivity : AppCompatActivity() {
    private lateinit var layout: ActivityLaunchBinding
    private val launchViewModel: LaunchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_launch)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@LaunchActivity)
            }
        }

        launchViewModel.requestLaunch()
        collectLatestOnLifecycleStarted(launchViewModel.launch) { state ->
            if (state.response != null) {
                when (state.response.configuration.responseCode) {
                    200 -> {
                        lifecycleScope.launch {
                            entryPoint.getAccountUseCase().invoke().collectLatest { account ->
                                delay(Config.LAUNCH_TIMEOUT)
                                when (account.token) {
                                    CREDENTIALS_NOT_SET -> {
                                        startActivity(Intent(this@LaunchActivity, SigninActivity::class.java))
                                    }
                                    else -> {
                                        when (Helper.isTelevision(this@LaunchActivity)) {
                                            true -> {
                                                if (Config.MOCK_TV) {
                                                    startActivity(Intent(this@LaunchActivity, MainActivity::class.java))
                                                } else {
                                                    startActivity(Intent(this@LaunchActivity, TelevisionMainActivity::class.java))
                                                }
                                            }
                                            false -> startActivity(Intent(this@LaunchActivity, MainActivity::class.java))
                                        }
                                    }
                                }
                                finishAffinity()
                            }
                        }
                    }
                    else -> showToast(this@LaunchActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                }
            }
            if (state.error != null) {
                showInternetConnectionDialog()
            }
        }
    }

    private fun showInternetConnectionDialog() {
        val dialog = Dialog(this@LaunchActivity, R.style.DialogStyle).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_internet_connection)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

        dialog.findViewById<Button>(R.id.try_again).setOnClickListener {
            launchViewModel.requestLaunch()
            dialog.dismiss()
        }

        dialog.window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
                attributes.gravity = Gravity.CENTER
                attributes.dimAmount = 0.0f
            }
        }
        dialog.show()
    }
}