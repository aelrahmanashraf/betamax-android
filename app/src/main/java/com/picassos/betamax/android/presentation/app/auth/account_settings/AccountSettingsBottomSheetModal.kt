package com.picassos.betamax.android.presentation.app.auth.account_settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.picassos.betamax.android.R
import com.picassos.betamax.android.databinding.AccountSettingsBottomSheetModalBinding
import com.picassos.betamax.android.presentation.app.auth.change_password.ChangePasswordActivity
import com.picassos.betamax.android.presentation.app.auth.profile_info.ProfileInfoActivity

class AccountSettingsBottomSheetModal : BottomSheetDialogFragment() {
    private lateinit var layout: AccountSettingsBottomSheetModalBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.account_settings_bottom_sheet_modal, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout.apply {
            editProfileInfo.setOnClickListener {
                startActivity(Intent(requireContext(), ProfileInfoActivity::class.java))
                dismiss()
            }
            changePassword.setOnClickListener {
                startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
                dismiss()
            }
        }
    }
}