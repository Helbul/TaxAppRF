package com.taxapprf.taxapp.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.taxapprf.domain.account.AccountModel
import com.taxapprf.domain.account.SwitchAccountUseCase
import com.taxapprf.domain.account.GetAccountsUseCase
import com.taxapprf.domain.account.SwitchAccountModel
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.domain.transaction.TransactionModel
import com.taxapprf.domain.user.IsSignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isSignInUseCase: IsSignInUseCase,
    getAccountsUseCase: GetAccountsUseCase,
    private val switchAccountUseCase: SwitchAccountUseCase
) : ViewModel() {
    private val _state = ActivityStateLiveData()
    val state: LiveData<ActivityBaseState> = _state

    val isSignIn
        get() = isSignInUseCase.execute()

    val accounts = getAccountsUseCase.execute()
        .flowOn(Dispatchers.IO)
        .map { it.toAccountsModel() }
        .asLiveData(viewModelScope.coroutineContext)

    private val _account = MutableLiveData<AccountModel>()
    val account: LiveData<AccountModel> = _account

    var report: ReportModel? = null
    var transaction: TransactionModel? = null

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
            val result = getOrNull()
            result?.find { it.active }?.let {
                _account.postValue(it)
            }
            result ?: listOf()
        }
}