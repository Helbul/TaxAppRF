package com.taxapprf.taxapp.ui.account.add

import androidx.lifecycle.viewModelScope
import com.taxapprf.data.error.InputErrorEmailEmpty
import com.taxapprf.domain.account.AccountModel
import com.taxapprf.domain.account.SwitchAccountModel
import com.taxapprf.domain.account.SwitchAccountUseCase
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
    var oldAccountModel: AccountModel? = null
    fun saveAccount(accountName: String) {
        oldAccountModel?.let { oldAccountModel ->
            if (accountName.isErrorInputAccountChecker()) return

            val switchAccountModel = SwitchAccountModel(oldAccountModel.name, accountName)

            viewModelScope.launch(Dispatchers.IO) {
                switchAccountUseCase.execute(switchAccountModel)
                    .onStart { loading() }
                    .catch { error(it) }
                    .collectLatest { success() }
            }
        }
    }

    private fun String.isErrorInputAccountChecker(): Boolean {
        if (isEmpty()) error(InputErrorEmailEmpty())
        else return false

        return true
    }
}