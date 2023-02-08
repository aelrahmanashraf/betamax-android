package com.picassos.betamax.android.presentation.app.restrict.vpn_restriction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.databinding.ActivityVpnRestrictedBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@AndroidEntryPoint
class VpnRestrictedActivity : AppCompatActivity() {
    private lateinit var layout: ActivityVpnRestrictedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView<ActivityVpnRestrictedBinding>(this, R.layout.activity_vpn_restricted).apply {
            confirm.setOnClickListener {
                finishAffinity()
                exitProcess(0)
            }
        }
    }
}