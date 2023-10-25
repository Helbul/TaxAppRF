package com.taxapprf.domain.excel

data class ExportExcelModel(
    val reportId: Int
) {
    var reportName: String? = null
    var reportTax: Double? = null
    var transactions: List<ExcelTransactionModel> = emptyList()

    var reportTitle: String? = null
    var reportInfo: String? = null
    var transactionTitles: List<String> = emptyList()
}