package com.picassos.betamax.android.core.view.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.picassos.betamax.android.R
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import com.picassos.betamax.android.presentation.app.main.MainActivity

class SubscriptionSuccessDialog(private val activity: Activity) : Dialog(activity, R.style.ThemeDialogStyle) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_subscription_success)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        findViewById<ImageView>(R.id.dialog_close).setOnClickListener {
            navigateHome()
        }

        findViewById<Button>(R.id.kontinue).setOnClickListener {
            navigateHome()
        }

        window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
                attributes.gravity = Gravity.CENTER
                attributes.dimAmount = 0.0f
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            navigateHome()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun navigateHome() {
        Intent(activity, MainActivity::class.java).also { intent ->
            activity.startActivity(intent)
            activity.finishAffinity()
        }
    }
}