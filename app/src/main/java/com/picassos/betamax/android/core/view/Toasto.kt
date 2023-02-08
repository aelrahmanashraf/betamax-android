package com.picassos.betamax.android.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.picassos.betamax.android.R
import android.widget.TextView
import android.widget.Toast
import java.util.*

object Toasto {
    /**
     * use @show_toast to show a custom toast with
     * text, duration and style. for example:
     * show_toast(context, title, duration, type);
     * we assigned null to the layout inflater instead
     * of assigning @custom_taast to root as it will always
     * return null. Toast design is designed to be clean.
     * @param context for context
     * @param title for toast title
     * @param duration for duration, you can whether
     * choose 0 for short period or 1 for long period
     * @param type for toast type. currently support designs
     * is (0) #primary, (1) #alert, (2) #warning
     */
    @JvmStatic
    @SuppressLint("InflateParams")
    fun showToast(context: Context, title: String?, duration: Int, type: Int) {
        // layout inflater
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var layout: View? = null
        when (type) {
            0 -> {
                layout = Objects.requireNonNull(inflater).inflate(R.layout.toast_layout_primary, null)
            }
            1 -> {
                layout = Objects.requireNonNull(inflater).inflate(R.layout.toast_layout_alert, null)
            }
            2 -> {
                layout = Objects.requireNonNull(inflater).inflate(R.layout.toast_layout_warning, null)
            }
        }

        // initialize toast text and set text from the method
        val toastTitle = layout?.findViewById<TextView>(R.id.toast_text)
        if (toastTitle != null) {
            toastTitle.text = title
        }

        // create a new toast and set gravity as @fill_horizontal
        // and @gravity.top to make the toast stick on the top.
        val toast = Toast(context)
        toast.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 0)
        toast.duration = duration
        toast.view = layout
        // show the toast
        toast.show()
    }
}