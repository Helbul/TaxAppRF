package com.taxapprf.domain.account

import com.taxapprf.domain.AccountRepository
import javax.inject.Inject

class SwitchAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    fun execute(switchAccountModel: SwitchAccountModel) =
        repository.switchAccount(switchAccountModel)
}