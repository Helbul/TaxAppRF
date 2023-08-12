package com.taxapprf.taxapp.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxapprf.domain.account.AccountModel
import com.taxapprf.domain.account.GetAccountsUseCase
import com.taxapprf.domain.account.SwitchAccountModel
import com.taxapprf.domain.account.SwitchAccountUseCase
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.domain.transaction.TransactionModel
import com.taxapprf.domain.user.IsSignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isSignInUseCase: IsSignInUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val switchAccountUseCase: SwitchAccountUseCase
) : ViewModel() {
    private val _state = ActivityStateLiveData()
    val state: LiveData<ActivityBaseState> = _state

    val isSignIn
        get() = isSignInUseCase.execute()

    private val _accounts = MutableLiveData<List<AccountModel>>()
    val accounts: LiveData<List<AccountModel>> = _accounts

    private val _account = MutableLiveData<AccountModel>()
    val account: LiveData<AccountModel> = _account

    var report: ReportModel? = null
    var transaction: TransactionModel? = null

    fun loading() = viewModelScope.launch(Dispatchers.IO) {
        getAccountsUseCase.execute()
            .onStart { _state.loading() }
            .catch { _state.error(it) }
            .collectLatest {
                _accounts.postValue(it.toAccountsModel())
                _state.success()
            }
    }

    init {
        loading()
    }

    fun switchAccount(newAccountModel: AccountModel) = viewModelScope.launch(Dispatchers.IO) {
        _account.value?.let { oldAccountModel ->
            val switchAccountModel = SwitchAccountModel(oldAccountModel, newAccountModel)
            switchAccountUseCase.execute(switchAccountModel)
                .onStart { _state.loading() }
                .catch { _state.error(it) }
                .collectLatest { _state.success() }
        }
    }

    private fun Result<List<AccountModel>>.toAccountsModel() =
        if (isFailure) listOf()
        else {
            val accounts = getOrNull()
            val account = accounts?.find { it.active }
                ?: accounts?.first()

            account?.let {
                _account.postValue(it)
            }

            accounts ?: listOf()
        }
}