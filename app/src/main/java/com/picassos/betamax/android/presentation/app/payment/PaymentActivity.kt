package com.picassos.betamax.android.presentation.app.payment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getSerializable
import com.picassos.betamax.android.core.utilities.Let
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.core.utilities.Security
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.core.view.dialog.RequestDialog
import com.picassos.betamax.android.data.source.remote.PayPalService
import com.picassos.betamax.android.data.source.remote.body.paypal.PayPalCreateOrderBody
import com.picassos.betamax.android.databinding.ActivityPaymentBinding
import com.picassos.betamax.android.domain.model.SubscriptionPackage
import com.picassos.betamax.android.presentation.app.main.MainActivity
import com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.PayPalViewModel
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {
    private lateinit var layout: ActivityPaymentBinding
    private val paymentViewModel: PaymentViewModel by viewModels()
    private val paypalViewModel: PayPalViewModel by viewModels()
    private val subscribeViewModel: SubscribeViewModel by viewModels()

    private lateinit var requestDialog: RequestDialog

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_payment)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@PaymentActivity)
            }
        }

        requestDialog = RequestDialog(this)

        layout.goBack.setOnClickListener { finish() }

        getSerializable(this@PaymentActivity, "subscriptionPackage", SubscriptionPackage::class.java).also { subscriptionPackage ->
            paymentViewModel.setSubscriptionPackage(subscriptionPackage)
        }

        layout.apply {
            paypalPaymentMethod.setOnClickListener {
                paymentViewModel.setSelectedPaymentMethod(paymentMethod = "paypal")
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(paymentViewModel.subscriptionPackage, paymentViewModel.selectedPaymentMethod) { subscriptionPackage, isSafePaymentMethod ->
                    val total = subscriptionPackage.price.toDouble() + 0.99

                    layout.apply layout@ {
                        this@layout.subscriptionPackage.text =
                            subscriptionPackage.title
                        paymentAmount.text = "$${subscriptionPackage.price}"
                        paymentTax.text = "$0.99"
                        paymentTotal.text = "$$total"
                    }

                    isSafePaymentMethod?.let { paymentMethod ->
                        if (paymentMethod == "paypal") {
                            layout.paypalPaymentMethod.setBackgroundResource(R.drawable.payment_method_background_selected)
                        }
                        layout.checkout.apply {
                            isEnabled = true
                            setOnClickListener {
                                when (paymentMethod) {
                                    "paypal" -> createPayPalAuthorizedOrder(value = total)
                                }
                            }
                        }
                    }
                }.collect()
            }
        }

        collectLatestOnLifecycleStarted(subscribeViewModel.subscribe) { state ->
            if (state.isLoading) {
                requestDialog.show()
            }
            if (state.responseCode != null) {
                requestDialog.dismiss()

                Intent(this@PaymentActivity, MainActivity::class.java).also {intent ->
                    startActivity(intent)
                    finishAffinity()
                }
            }
            if (state.error != null) {
                requestDialog.dismiss()
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(this@PaymentActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(this@PaymentActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(this@PaymentActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }

    private fun createPayPalAuthorizedOrder(intent: String = "CAPTURE", currencyCode: String = "USD", value: Double) {
        paypalViewModel.apply {
            setRequestId(Security.generateRandomUUID())

            requestId.value?.let { requestId ->
                requestCreateAuthorizedOrder(
                    requestId = requestId,
                    order = PayPalCreateOrderBody(
                        intent = intent,
                        purchaseUnits = listOf(
                            PayPalCreateOrderBody.PurchaseUnit(
                                amount = PayPalCreateOrderBody.PurchaseUnit.Amount(
                                    currencyCode = currencyCode,
                                    value = value.toString())
                            )
                        ),
                        paymentSource = PayPalCreateOrderBody.PaymentSource(
                            PayPalCreateOrderBody.PaymentSource.PayPal(
                                PayPalCreateOrderBody.PaymentSource.PayPal.ExperienceContext(
                                    brandName = getString(R.string.app_name),
                                    locale = Locale.getDefault().language,
                                    landingPage = "LOGIN",
                                    userAction = "PAY_NOW",
                                    returnUrl = "${PayPalService.APP_PREFIX}/order?state=approved&paymentMethod=paypal&requestId=$requestId",
                                    cancelUrl = "${PayPalService.APP_PREFIX}/order?state=canceled&paymentMethod=paypal&requestId=$requestId")
                            )
                        )
                    )
                )
            }
        }

        lifecycleScope.launch {
            paypalViewModel.createAuthorizedOrder.collectLatest { state ->
                if (state.isLoading) {
                    requestDialog.show()
                }
                if (state.response != null) {
                    requestDialog.dismiss()

                    state.response.order.links.forEach { link ->
                        if (link.rel == "payer-action") {
                            CustomTabsIntent.Builder()
                                .build()
                                .launchUrl(this@PaymentActivity, Uri.parse(link.href))
                        }
                    }
                }
                if (state.error != null) {
                    requestDialog.dismiss()
                    when (state.error) {
                        Response.NETWORK_FAILURE_EXCEPTION -> {
                            showToast(this@PaymentActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                        }
                        Response.MALFORMED_REQUEST_EXCEPTION -> {
                            showToast(this@PaymentActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                            Firebase.crashlytics.log("Request returned a malformed request or response.")
                        }
                        else -> {
                            showToast(this@PaymentActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        }
                    }
                }
            }
        }
    }

    private fun capturePayPalAuthorizedOrder(requestId: String, orderId: String) {
        paypalViewModel.requestCaptureAuthorizedOrder(requestId, orderId)

        lifecycleScope.launch {
            paypalViewModel.captureAuthorizedOrder.collectLatest { state ->
                if (state.isLoading) {
                    requestDialog.show()
                }
                if (state.response != null) {
                    requestDialog.dismiss()

                    if (state.response.captureOrder.status == "COMPLETED") {
                        subscribeViewModel.requestUpdateSubscription(
                            subscriptionPackage = paymentViewModel.subscriptionPackage.value.id)
                    }
                }
                if (state.error != null) {
                    when (state.error) {
                        Response.NETWORK_FAILURE_EXCEPTION -> {
                            showToast(this@PaymentActivity, getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                        }
                        Response.MALFORMED_REQUEST_EXCEPTION -> {
                            showToast(this@PaymentActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                            Firebase.crashlytics.log("Request returned a malformed request or response.")
                        }
                        else -> {
                            showToast(this@PaymentActivity, getString(R.string.unknown_issue_occurred), 0, 1)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.apply {
            if (action == Intent.ACTION_VIEW) {
                data?.let { data ->
                    if (data.getQueryParameter("state") == "approved") {
                        Let.safeLet(data.getQueryParameter("requestId"), data.getQueryParameter("token")) { requestId, orderId ->
                            capturePayPalAuthorizedOrder(
                                requestId = requestId,
                                orderId = orderId)
                        }
                    }
                }
            }
        }
    }
}