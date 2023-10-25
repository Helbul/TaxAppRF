package com.taxapprf.data

import com.taxapprf.data.local.room.LocalDeletedDao
import com.taxapprf.data.local.room.entity.LocalDeletedEntity
import com.taxapprf.data.local.room.model.transaction.DeletedReportDataModel
import com.taxapprf.data.local.room.model.transaction.DeletedTransactionDataModel
import com.taxapprf.domain.DeletedRepository
import com.taxapprf.domain.deleted.DeleteReportsModel
import com.taxapprf.domain.deleted.DeleteTransactionsModel
import javax.inject.Inject

class DeletedRepositoryImpl @Inject constructor(
    private val localDeletedDao: LocalDeletedDao,
) : DeletedRepository {
    override suspend fun deleteReports(deleteReportsModel: DeleteReportsModel) {
        deleteReportsModel.reportIds.forEach { reportId ->
            localDeletedDao.getGetDeletedReport(reportId)?.let { deletedReportDataModel ->
                val deletedTransactionDataModels =
                    localDeletedDao.getGetDeletedTransactionsByReportId(reportId)
                startLocalDeleteTransactions(deletedReportDataModel, deletedTransactionDataModels)
            }
        }
    }

    override suspend fun deleteTransactions(deleteTransactionsModel: DeleteTransactionsModel) {
        localDeletedDao.getGetDeletedReport(deleteTransactionsModel.reportId)
            ?.let { deletedReportDataModel ->
                val deletedTransactionDataModels =
                    localDeletedDao.getGetDeletedTransactionsByTransactionIds(
                        deleteTransactionsModel.transactionIds
                    )
                startLocalDeleteTransactions(deletedReportDataModel, deletedTransactionDataModels)
            }
    }

    private fun startLocalDeleteTransactions(
        deletedReportDataModel: DeletedReportDataModel,
        deletedTransactionDataModels: List<DeletedTransactionDataModel>
    ) {
        if (deletedTransactionDataModels.isNotEmpty()) {
            var newTax = deletedReportDataModel.taxRUB
            var newSize = deletedReportDataModel.size
            val localDeletedKeyEntities = mutableListOf<LocalDeletedEntity>()

            deletedTransactionDataModels
                .forEach { deletedTransactionDataModel ->
                    deletedTransactionDataModel.taxRUB?.let { newTax -= it }
                    newSize--

                    deletedTransactionDataModel.remoteKey?.let {
                        localDeletedKeyEntities.add(
                            LocalDeletedEntity(
                                accountKey = deletedReportDataModel.accountKey,
                                reportKey = deletedReportDataModel.reportKey,
                                transactionKey = deletedTransactionDataModel.remoteKey,
                                syncAt = deletedTransactionDataModel.syncAt
                            )
                        )
                    }
                }

            if (newSize > 0)
                localDeletedDao
                    .updateLocalReportEntity(deletedReportDataModel.id, newTax, newSize)
            else
                localDeletedDao.deleteLocalReportEntity(deletedReportDataModel.id)

            localDeletedDao.saveLocalDeletedEntity(localDeletedKeyEntities)
            localDeletedDao.deleteLocalTransactionEntities(deletedTransactionDataModels.map { it.id })
        }
    }
}