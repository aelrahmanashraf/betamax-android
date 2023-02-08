package com.picassos.betamax.android.presentation.television.auth.account_settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.picassos.betamax.android.R
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ActivityTelevisionAccountSettingsBinding
import com.picassos.betamax.android.presentation.app.auth.change_password.ChangePasswordActivity
import com.picassos.betamax.android.presentation.app.auth.profile_info.ProfileInfoActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TelevisionAccountSettingsActivity : AppCompatActivity() {
    private lateinit var layout: ActivityTelevisionAccountSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_television_account_settings)

        layout.apply {
            editProfileInfo.setOnClickListener {
                startActivity(Intent(this@TelevisionAccountSettingsActivity, ProfileInfoActivity::class.java))
            }
            changePassword.setOnClickListener {
                startActivity(Intent(this@TelevisionAccountSettingsActivity, ChangePasswordActivity::class.java))
            }
        }
    }
}