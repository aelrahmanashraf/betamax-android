package com.picassos.betamax.android.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.picassos.betamax.android.R
import android.widget.TextView
import android.widget.Toast

object Toasto {
    @SuppressLint("InflateParams")
    fun showToast(context: Context, title: String, duration: Int, type: Int) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View? = when (type) {
            0 -> {
                inflater.inflate(R.layout.toast_layout_primary, null)
            }
            1 -> {
                inflater.inflate(R.layout.toast_layout_alert, null)
            }
            2 -> {
                inflater.inflate(R.layout.toast_layout_warning, null)
            }
            else -> null
        }
        layout?.let { view ->
            view.findViewById<TextView>(R.id.toast_text)?.apply {
                text = title
            }

            Toast(context).apply {
                setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 0)
                this.duration = duration
                this.view = layout
                show()
            }
        }
    }
}