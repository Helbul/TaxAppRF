package com.taxapprf.taxapp.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.taxapprf.data.local.room.LocalDatabase.Companion.REPORT_ID
import com.taxapprf.domain.deleted.DeleteTransactionsModel
import com.taxapprf.domain.deleted.DeleteTransactionsUseCase
import com.taxapprf.domain.excel.ExportExcelModel
import com.taxapprf.domain.excel.ExportExcelUseCase
import com.taxapprf.domain.transactions.ObserveReportUseCase
import com.taxapprf.domain.transactions.ObserveTransactionsUseCase
import com.taxapprf.domain.transactions.TransactionModel
import com.taxapprf.taxapp.ui.BaseViewModel
import com.taxapprf.taxapp.ui.makeHot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TransactionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeReportUseCase: ObserveReportUseCase,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val exportExcelUseCase: ExportExcelUseCase,
    private val deleteTransactionsUseCase: DeleteTransactionsUseCase,
) : BaseViewModel() {
    val reportId = savedStateHandle.get<Int>(REPORT_ID)

    fun observeReport() =
        reportId?.let { reportId ->
            observeReportUseCase.execute(reportId)
                .makeHot(viewModelScope)
        }

    fun observeTransactions() =
        reportId?.let { reportId ->
            observeTransactionsUseCase.execute(reportId)
                .makeHot(viewModelScope)
        }

    fun deleteTransaction(transactionModel: TransactionModel) =
        reportId?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val deleteTransactionsModel = DeleteTransactionsModel(
                    reportId = reportId,
                    transactionIds = listOf(transactionModel.id)
                )
                deleteTransactionsUseCase.execute(deleteTransactionsModel)
            }
        }

    fun exportReport() =
        reportId?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val getExcelReportModel = ExportExcelModel(reportId)
                exportExcelUseCase.execute(getExcelReportModel)?.let {
                    successExport(it)
                }
            }
        }

    fun shareReport() {
        reportId?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val getExcelReportModel = ExportExcelModel(reportId)
                exportExcelUseCase.execute(getExcelReportModel)?.let {
                    successShare(it)
                }
            }
        }
    }
}