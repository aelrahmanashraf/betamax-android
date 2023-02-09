package com.picassos.betamax.android.presentation.app.subscription.subscribe

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ActivitySubscribeBinding
import com.picassos.betamax.android.di.AppEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@AndroidEntryPoint
class SubscribeActivity : AppCompatActivity() {
    private lateinit var layout: ActivitySubscribeBinding
    private val subscribeViewModel: SubscribeViewModel by viewModels()

    private var selectedPackage = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this@SubscribeActivity, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_subscribe)

        layout.goBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(entryPoint.getConfigurationUseCase().invoke(), entryPoint.getAccountUseCase().invoke()) { configuration, account ->
                    if (!Helper.verifyLicense(configuration.developedBy)) {
                        finishAffinity()
                        exitProcess(0)
                    }

                    layout.apply {
                        silverPackagePrice.text = "$${configuration.silverPackagePrice}"
                        silverPackage.setOnClickListener {
                            selectedPackage = 1
                            silverPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background_selected)
                            goldPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                            diamondPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                        }
                        goldPackagePrice.text = "$${configuration.goldPackagePrice}"
                        goldPackage.setOnClickListener {
                            selectedPackage = 2
                            silverPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                            goldPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background_selected)
                            diamondPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                        }
                        diamondPackagePrice.text = "$${configuration.diamondPackagePrice}"
                        diamondPackage.setOnClickListener {
                            selectedPackage = 3
                            silverPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                            goldPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                            diamondPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background_selected)
                        }
                        subscribe.setOnClickListener {

                        }
                    }
                }.collect()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Helper.restrictVpn(this@SubscribeActivity)
    }
}