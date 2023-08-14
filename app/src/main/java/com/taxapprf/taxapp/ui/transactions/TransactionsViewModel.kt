package com.taxapprf.taxapp.ui.transactions

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.taxapprf.domain.FirebasePathModel
import com.taxapprf.domain.account.AccountModel
import com.taxapprf.domain.excel.SendExcelReportUseCase
import com.taxapprf.domain.report.DeleteReportUseCase
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.domain.transaction.GetTransactionsUseCase
import com.taxapprf.domain.transaction.TransactionModel
import com.taxapprf.taxapp.ui.BaseState
import com.taxapprf.taxapp.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTaxUseCase: DeleteReportUseCase,
    private val sendExcelReportUseCase: SendExcelReportUseCase,
) : BaseViewModel() {
    lateinit var account: AccountModel
    lateinit var report: ReportModel

    private val _transactions = MutableLiveData<List<TransactionModel>>()
    val transactions: LiveData<List<TransactionModel>> = _transactions

    var emailUri: Uri? = null

    fun loadTransactions() = viewModelScope.launch(Dispatchers.IO) {
        val getTransactionsModel = FirebasePathModel(account.name, report.year)
        getTransactionsUseCase.execute(getTransactionsModel)
            .onStart { loading() }
            .catch { error(it) }
            .collectLatest {
                _transactions.postValue(it)
                success()
            }
    }

    fun deleteTax() = viewModelScope.launch(Dispatchers.IO) {
        val request = FirebasePathModel(account.name, report.year)
        deleteTaxUseCase.execute(request)
            .onStart { loading() }
            .catch { error(it) }
            .collectLatest {
                success(BaseState.SuccessDelete)
            }
    }

    fun sendExcelReport() = viewModelScope.launch(Dispatchers.IO) {
        transactions.value?.let { transactions ->
            sendExcelReportUseCase.execute(report, transactions)
                .onStart { loading() }
                .catch { error(it) }
                .collectLatest {
                    emailUri = it
                    success(BaseState.SuccessSendEmail)
                }
        }
    }

    fun share() {

    }

    fun export() {

    }

    fun import() {

    }
}