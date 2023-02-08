package com.picassos.betamax.android.presentation.app.info.about

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.databinding.ActivityAboutBinding
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.di.AppEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlin.system.exitProcess

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {
    private lateinit var layout: ActivityAboutBinding
    private val aboutViewModel: AboutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this@AboutActivity, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_about)

        layout.goBack.setOnClickListener { finish() }

        collectLatestOnLifecycleStarted(entryPoint.getConfigurationUseCase().invoke()) { configuration ->
            if (!Helper.verifyLicense(configuration.developedBy)) {
                finishAffinity()
                exitProcess(0)
            }

            layout.apply {
                brandingText.text = getString(R.string.app_name).lowercase()
                aboutText.text = configuration.aboutText
                telegramLink.setOnClickListener {
                    Helper.handleWebIntent(
                        context = this@AboutActivity,
                        url = configuration.telegramURL)
                }
                whatsappLink.setOnClickListener {
                    Helper.handleWhatsappIntent(this@AboutActivity, configuration.whatsappURL)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@AboutActivity)
    }
}
