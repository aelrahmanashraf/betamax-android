package com.picassos.betamax.android.presentation.television.launch

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Response.CREDENTIALS_NOT_SET
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.databinding.ActivityTelevisionLaunchBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.presentation.app.auth.signin.SigninActivity
import com.picassos.betamax.android.presentation.app.launch.LaunchViewModel
import com.picassos.betamax.android.presentation.television.main.TelevisionMainActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class TelevisionLaunchActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionLaunchBinding
    private val launchViewModel: LaunchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_launch)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        launchViewModel.requestLaunch()
        collectLatestOnLifecycleStarted(launchViewModel.launch) { state ->
            if (state.response != null) {
                when (state.response.configuration.responseCode) {
                    200 -> {
                        lifecycleScope.launch {
                            entryPoint.getAccountUseCase().invoke().collectLatest { account ->
                                delay(Config.TV_LAUNCH_TIMEOUT)
                                when (account.token) {
                                    CREDENTIALS_NOT_SET -> {
                                        startActivity(Intent(this@TelevisionLaunchActivity, SigninActivity::class.java))
                                    }
                                    else -> {
                                        startActivity(Intent(this@TelevisionLaunchActivity, TelevisionMainActivity::class.java))
                                    }
                                }
                                finishAffinity()
                            }
                        }
                    }
                    else -> showToast(this@TelevisionLaunchActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                }
            }
            if (state.error != null) {
                showInternetConnectionDialog()
                showToast(this@TelevisionLaunchActivity, state.error, 1, 2)
            }
        }
    }

    private fun showInternetConnectionDialog() {
        val dialog = Dialog(this@TelevisionLaunchActivity, R.style.DialogStyle).apply {
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