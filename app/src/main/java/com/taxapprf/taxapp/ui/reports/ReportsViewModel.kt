package com.taxapprf.taxapp.ui.reports

import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.taxapprf.domain.deleted.DeleteReportsModel
import com.taxapprf.domain.deleted.DeleteReportsUseCase
import com.taxapprf.domain.excel.ImportExcelModel
import com.taxapprf.domain.excel.ImportExcelUseCase
import com.taxapprf.domain.reports.ObserveReportsUseCase
import com.taxapprf.domain.transactions.ReportModel
import com.taxapprf.taxapp.ui.BaseViewModel
import com.taxapprf.taxapp.ui.showLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val observeReportsUseCase: ObserveReportsUseCase,
    private val deleteReportsUseCase: DeleteReportsUseCase,
    private val importExcelUseCase: ImportExcelUseCase,
) : BaseViewModel() {
    private var accountId: Int = 0

    private val _reports: MutableStateFlow<List<ReportModel>?> = MutableStateFlow(null)
    val reports = _reports.asStateFlow()

    fun updateReports(reportsAccountId: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            accountId = reportsAccountId

            observeReportsUseCase.execute(accountId)
                .showLoading()
                .collectLatest { _reports.value = it }
        }

    fun deleteReport(reportModel: ReportModel) {
        deleteReports(listOf(reportModel.id))
    }

    private fun deleteReports(reportIds: List<Int>) =
        viewModelScope.launch(Dispatchers.IO) {
            val deleteReportsModel = DeleteReportsModel(reportIds)
            deleteReportsUseCase.execute(deleteReportsModel)
        }

    fun importExcel(intent: Intent?) =
        intent?.data?.let { uri ->
            viewModelScope.launch(Dispatchers.IO) {
                val importExcelModel = ImportExcelModel(accountId, uri)
                importExcelUseCase.execute(importExcelModel)
            }
        }
}