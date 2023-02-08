package com.picassos.betamax.android.domain.usecase.account.profile_info

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.account.auth.SetLocalAccountUseCase
import javax.inject.Inject

data class ProfileInfoUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val setLocalAccountUseCase: SetLocalAccountUseCase,
    val updateProfileInfoUseCase: UpdateProfileInfoUseCase,
    val profileInfoValidation: ProfileInfoValidation)