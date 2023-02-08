package com.picassos.betamax.android.core.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.picassos.betamax.android.BuildConfig
import com.picassos.betamax.android.R
import java.lang.Exception
import kotlin.math.min

class TelevisionSubscriptionDialog(context: Context, private val paymentToken: String) : Dialog(context, R.style.DialogStyle) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_television_subscription)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val point = Point()
        val manager = context.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getSize(point)
        val smallerDimension = (min(point.x, point.y)) * 3 / 4

        val qrgEncoder = QRGEncoder("${BuildConfig.BASE_URL}subscription/television_subscribe.php?paymentToken=${paymentToken}", null, QRGContents.Type.TEXT, smallerDimension).apply {
            colorBlack = ContextCompat.getColor(context, R.color.color_white)
            colorWhite = ContextCompat.getColor(context, R.color.reverse_dark)
        }
        try {
            val bitmap = qrgEncoder.bitmap
            findViewById<ImageView>(R.id.subscription_qrcode).setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        window?.let { window ->
            window.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
                attributes.gravity = Gravity.BOTTOM
                attributes.dimAmount = 0.0f
            }
        }
    }
}