package com.picassos.betamax.android.presentation.app.info.about

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.databinding.ActivityAboutBinding
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Intents.openWebBrowser
import com.picassos.betamax.android.core.utilities.Intents.openWhatsapp
import com.picassos.betamax.android.di.AppEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@AboutActivity)
            }
        }

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
                    openWebBrowser(url = configuration.telegramURL)
                }
                whatsappLink.setOnClickListener {
                    openWhatsapp(number = configuration.whatsappURL)
                }
            }
        }
    }
}
