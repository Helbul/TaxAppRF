package com.taxapprf.taxapp.ui.account.add

import android.text.Editable
import androidx.lifecycle.viewModelScope
import com.taxapprf.domain.account.SwitchAccountModel
import com.taxapprf.domain.account.SwitchAccountUseCase
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountAddViewModel @Inject constructor(
    private val switchAccountUseCase: SwitchAccountUseCase
) : BaseViewModel() {
    private var accountName = ""
    fun saveAccount() {
        if (!isLock) {
            val switchAccountModel = SwitchAccountModel(account.name, accountName)

            viewModelScope.launch(Dispatchers.IO) {
                switchAccountUseCase.execute(switchAccountModel)
                    .onStart { start() }
                    .catch { error(it) }
                    .collectLatest { success() }
            }
        }
    }

    fun checkName(cAccountName: Editable?) = check {
        accountName = cAccountName.toString()
        if (accountName.isErrorNameRange()) R.string.error_input_account_incorrect
        else null
    }

    private fun String.isErrorNameRange() = isEmpty() || length > 16
}