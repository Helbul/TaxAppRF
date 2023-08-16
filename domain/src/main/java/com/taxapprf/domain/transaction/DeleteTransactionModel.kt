package com.taxapprf.domain.transaction

data class DeleteTransactionModel(
    val accountKey: String,
    val yearKey: String,
    val transactionKey: String,

    val reportTax: Double,
    val transactionTax: Double,
)