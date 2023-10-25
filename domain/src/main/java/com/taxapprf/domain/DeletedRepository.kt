package com.taxapprf.domain

import com.taxapprf.domain.deleted.DeleteReportsModel
import com.taxapprf.domain.deleted.DeleteTransactionsModel

interface DeletedRepository {
    suspend fun deleteReports(deleteReportsModel: DeleteReportsModel)
    suspend fun deleteTransactions(deleteTransactionsModel: DeleteTransactionsModel)
}