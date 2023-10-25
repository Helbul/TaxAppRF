package com.taxapprf.domain.deleted

data class DeleteTransactionsModel(
    val reportId: Int,
    val transactionIds: List<Int>
)