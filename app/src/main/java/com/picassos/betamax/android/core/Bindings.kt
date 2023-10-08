package com.picassos.betamax.android.core

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.validation.FormValidation

@BindingAdapter("text_error")
fun TextView.textError(error: String?) {
    if (error != null) {
        this.visibility = View.VISIBLE
        this.text = when (error) {
            FormValidation.FIELD_EMPTY -> resources.getString(R.string.this_field_is_required)
            FormValidation.USERNAME_EMPTY -> resources.getString(R.string.enter_username)
            FormValidation.USERNAME_INVALID -> resources.getString(R.string.invalid_username)
            FormValidation.EMAIL_EMPTY -> resources.getString(R.string.enter_email_address)
            FormValidation.EMAIL_INVALID -> resources.getString(R.string.invalid_email)
            FormValidation.PASSWORD_INVALID -> resources.getString(R.string.invalid_password)
            FormValidation.CONFIRM_PASSWORD_EMPTY -> resources.getString(R.string.reconfirm_your_password)
            FormValidation.PASSWORDS_NOT_MATCH -> resources.getString(R.string.password_doesnt_match)
            else -> ""
        }
    } else {
        this.visibility = View.GONE
    }
}

@BindingAdapter("layout_error")
fun EditText.layoutError(error: String?) {
    if (error != null) {
        this.setBackgroundResource(R.drawable.input_rectangle_background_error)
    } else {
        this.setBackgroundResource(R.drawable.input_rectangle_background)
    }
}

@BindingAdapter("layout_error_alt")
fun EditText.layoutErrorAlt(error: String?) {
    if (error != null) {
        this.setBackgroundResource(R.drawable.input_rectangle_background_alpha_error)
    } else {
        this.setBackgroundResource(R.drawable.input_rectangle_background_alpha)
    }
}

@BindingAdapter("visible")
fun Button.visible(error: String?) {
    this.visibility = if (error == null) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("enabled")
fun Button.enabled(error: String?) {
    this.isEnabled = error == null
}

@BindingAdapter("visible")
fun LinearLayout.visible(error: String?) {
    this.visibility = if (error == null) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("password_error", "confirm_password_error", requireAll = true)
fun Button.resetPasswordButton(passwordError: String?, confirmPasswordError: String?) {
    this.isEnabled = passwordError == null && confirmPasswordError == null
}

@BindingAdapter("current_password_error", "new_password_error", "confirm_password_error", requireAll = true)
fun Button.changePasswordButton(currentPasswordError: String?, newPasswordError: String?, confirmPasswordError: String?) {
    this.isEnabled = currentPasswordError == null && newPasswordError == null && confirmPasswordError == null
}