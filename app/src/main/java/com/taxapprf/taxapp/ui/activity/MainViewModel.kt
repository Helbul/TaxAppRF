package com.taxapprf.taxapp.ui.activity

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxapprf.data.NetworkManager
import com.taxapprf.domain.main.account.SwitchAccountModel
import com.taxapprf.domain.main.account.SwitchAccountUseCase
import com.taxapprf.domain.main.transaction.SaveTransactionModel
import com.taxapprf.domain.main.transaction.SaveTransactionUseCase
import com.taxapprf.domain.main.user.ObserveUserWithAccountsModel
import com.taxapprf.domain.main.user.ObserveUserWithAccountsUseCase
import com.taxapprf.domain.main.user.SignOutUseCase
import com.taxapprf.domain.main.user.UserWithAccountsModel
import com.taxapprf.domain.sync.SyncAllUseCase
import com.taxapprf.domain.tax.UpdateAllEmptySumRUBAndTaxRUBUseCase
import com.taxapprf.taxapp.ui.showLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val networkManager: NetworkManager,
    private val observeUserWithAccountsUseCase: ObserveUserWithAccountsUseCase,
    private val switchAccountUseCase: SwitchAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val syncAllUseCase: SyncAllUseCase,
    private val updateAllEmptySumRUBAndTaxRUBUseCase: UpdateAllEmptySumRUBAndTaxRUBUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
) : ViewModel() {
    var defaultUserName: String? = null
    var defaultAccountName: String? = null
    var accountId: Int? = null

    /*
    TODO() сделать загрузку в настройках курса за выбранный период
     viewModelScope.launch(Dispatchers.IO) {
                val date = LocalDate.now().toEpochDay()
                for (i in 0..900) {
                    getCBRRateUseCase.execute("USD", date - i)
                }
            }
            */

    private val _userWithAccounts = MutableStateFlow<UserWithAccountsModel?>(null)
    val userWithAccounts = _userWithAccounts.asStateFlow()

    private var isSynced = false
    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                viewModelScope.launch(Dispatchers.IO) {
                    if (!isSynced) syncAll()
                    isSynced = true
                    updateAllEmptySumRUBAndTaxRUBUseCase.execute()
                }
                networkManager.isConnection = true
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                networkManager.isConnection = false
            }
        }

    fun observeConnection() {
        networkManager.observeConnection(networkCallback)
    }

    suspend fun syncAll() =
        flow {
            syncAllUseCase.execute()
            emit(Unit)
        }
            .flowOn(Dispatchers.IO)
            .showLoading()

    fun updateUserWithAccounts() =
        defaultAccountName?.let { defaultAccountName ->
            viewModelScope.launch(Dispatchers.IO) {
                val observeUserWithAccountsModel =
                    ObserveUserWithAccountsModel(defaultAccountName)

                observeUserWithAccountsUseCase.execute(observeUserWithAccountsModel)
                    .collectLatest {
                        _userWithAccounts.value = it
                    }
            }
        }

    fun switchAccount(accountName: String) =
        userWithAccounts.value.let {
            it?.user?.id?.let { userId ->
                viewModelScope.launch(Dispatchers.IO) {
                    val switchAccountModel =
                        SwitchAccountModel(
                            userId = userId,
                            accountName = accountName
                        )
                    switchAccountUseCase.execute(switchAccountModel)
                }
            }
        }

    fun signOut() =
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                emit(signOutUseCase.execute())
            }
                .showLoading()
                .collectLatest {
                    updateUserWithAccounts()
                }
        }

    fun saveTransaction(saveTransactionModel: SaveTransactionModel) =
        viewModelScope.launch(Dispatchers.IO) {
            saveTransactionUseCase.execute(saveTransactionModel)
        }
}