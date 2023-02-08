package com.picassos.betamax.android.presentation.app.subscription.subscribe

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.picassos.betamax.android.BuildConfig
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.isPackageInstalled
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
                            silverPackage.background = getDrawable(R.drawable.pricing_background_selected)
                            goldPackage.background = getDrawable(R.drawable.pricing_background)
                            diamondPackage.background = getDrawable(R.drawable.pricing_background)
                        }
                        goldPackagePrice.text = "$${configuration.goldPackagePrice}"
                        goldPackage.setOnClickListener {
                            selectedPackage = 2
                            silverPackage.background = getDrawable(R.drawable.pricing_background)
                            goldPackage.background = getDrawable(R.drawable.pricing_background_selected)
                            diamondPackage.background = getDrawable(R.drawable.pricing_background)
                        }
                        diamondPackagePrice.text = "$${configuration.diamondPackagePrice}"
                        diamondPackage.setOnClickListener {
                            selectedPackage = 3
                            silverPackage.background = getDrawable(R.drawable.pricing_background)
                            goldPackage.background = getDrawable(R.drawable.pricing_background)
                            diamondPackage.background = getDrawable(R.drawable.pricing_background_selected)
                        }
                        subscribe.setOnClickListener {
                            val params = CustomTabColorSchemeParams.Builder().apply {
                                setToolbarColor(ContextCompat.getColor(this@SubscribeActivity, R.color.color_dark))
                            }
                            val builder = CustomTabsIntent.Builder().apply {
                                setDefaultColorSchemeParams(params.build())
                                setShowTitle(true)
                                setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                                setInstantAppsEnabled(false)
                                setUrlBarHidingEnabled(true)
                            }.build()

                            if (this@SubscribeActivity.isPackageInstalled("com.android.chrome")) {
                                builder.intent.setPackage("com.android.chrome")
                                builder.launchUrl(this@SubscribeActivity, Uri.parse(
                                    "${BuildConfig.BASE_URL}subscription/subscribe.php?paymentToken=${account.paymentToken}&package=${selectedPackage}"))
                            }
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