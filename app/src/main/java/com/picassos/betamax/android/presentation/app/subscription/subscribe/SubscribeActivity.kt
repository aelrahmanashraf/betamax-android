package com.picassos.betamax.android.presentation.app.subscription.subscribe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ActivitySubscribeBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.model.SubscriptionPackage
import com.picassos.betamax.android.presentation.app.payment.PaymentActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@AndroidEntryPoint
class SubscribeActivity : AppCompatActivity() {
    private lateinit var layout: ActivitySubscribeBinding
    private val subscribeViewModel: SubscribeViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this@SubscribeActivity, AppEntryPoint::class.java)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_subscribe)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@SubscribeActivity)
            }
        }

        layout.goBack.setOnClickListener { finish() }

        collectLatestOnLifecycleStarted(entryPoint.getConfigurationUseCase().invoke()) { configuration ->
            if (!Helper.verifyLicense(configuration.developedBy)) {
                finishAffinity()
                exitProcess(0)
            }
            subscribeViewModel.setSelectedSubscriptionPackage(SubscriptionPackage(
                id = 1,
                title = getString(R.string.silver),
                price = configuration.silverPackagePrice))

            layout.apply {
                silverPackagePrice.text = "$${configuration.silverPackagePrice}"
                goldPackagePrice.text = "$${configuration.goldPackagePrice}"
                diamondPackagePrice.text = "$${configuration.diamondPackagePrice}"
            }

            layout.apply {
                silverPackage.setOnClickListener {
                    subscribeViewModel.setSelectedSubscriptionPackage(SubscriptionPackage(
                        id = 1,
                        title = getString(R.string.silver),
                        price = configuration.silverPackagePrice))
                    silverPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background_selected)
                    goldPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                    diamondPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                }
                goldPackage.setOnClickListener {
                    subscribeViewModel.setSelectedSubscriptionPackage(SubscriptionPackage(
                        id = 2,
                        title = getString(R.string.gold),
                        price = configuration.goldPackagePrice))
                    silverPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                    goldPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background_selected)
                    diamondPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                }
                diamondPackage.setOnClickListener {
                    subscribeViewModel.setSelectedSubscriptionPackage(SubscriptionPackage(
                        id = 3,
                        title = getString(R.string.diamond),
                        price = configuration.diamondPackagePrice))
                    silverPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                    goldPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background)
                    diamondPackage.background = AppCompatResources.getDrawable(this@SubscribeActivity, R.drawable.pricing_background_selected)
                }
            }
        }

        collectLatestOnLifecycleStarted(subscribeViewModel.selectedSubscriptionPackage) { subscriptionPackage ->
            if (subscriptionPackage != SubscriptionPackage()) {
                layout.payment.apply {
                    isEnabled = true
                    setOnClickListener {
                        Intent(this@SubscribeActivity, PaymentActivity::class.java).also { intent ->
                            intent.putExtra("subscriptionPackage", subscriptionPackage)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}