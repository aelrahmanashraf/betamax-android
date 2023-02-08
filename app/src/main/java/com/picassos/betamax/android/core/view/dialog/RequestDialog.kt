package com.picassos.betamax.android.core.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import com.picassos.betamax.android.R
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.Window
import android.view.WindowManager

class RequestDialog(context: Context, private val isFullscreen: Boolean = false) : Dialog(context, R.style.DialogStyle) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_request)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
                attributes.gravity = Gravity.CENTER
                attributes.dimAmount = 0.0f
                if (isFullscreen) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                       attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                    setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                }
            }
        }
    }
}