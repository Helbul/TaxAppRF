package com.taxapprf.data

import com.taxapprf.data.local.room.dao.LocalReportDao
import com.taxapprf.data.local.room.entity.LocalReportEntity
import com.taxapprf.data.remote.firebase.FirebaseReportDaoImpl
import com.taxapprf.domain.ReportRepository
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.domain.transaction.DeleteTransactionModel
import com.taxapprf.domain.transaction.SaveTransactionModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val localDao: LocalReportDao,
    private val remoteDao: FirebaseReportDaoImpl,
) : ReportRepository {
    override fun observe(accountKey: String, reportKey: String) =
        localDao.observe(accountKey, reportKey).map { it?.toReportModel() }

    override fun observeAll(accountKey: String) =
        localDao.observeAll(accountKey).map { reports -> reports.map { it.toReportModel() } }

    override suspend fun delete(id: Int) {
        localDao.deleteDeferred(id)
    }

    override suspend fun deleteAll(ids: List<Int>) {
        localDao.deleteAllDeferred(ids)
    }

    override suspend fun updateAfterUpdateTransaction(
        saveTransactionModel: SaveTransactionModel
    ) {
        with(saveTransactionModel) {
            transactionKey?.let {
                reportKey?.let { reportKey ->
                    if (reportKey != newReportKey) {
                        deleteOrUpdateOldReport(accountKey, reportKey, tax)
                        updateReport(accountKey, reportKey, tax, isAdd = true)
                    } else updateReport(accountKey, reportKey, tax)
                }
            } ?: run {
                updateReport(accountKey, newReportKey, tax, isAdd = true)
            }
        }
    }

    override suspend fun updateAfterUpdateTransaction(
        deleteTransactionModel: DeleteTransactionModel
    ) {
        with(deleteTransactionModel) {
            updateReport(accountKey, reportKey, transactionTax, isDelete = true)
        }
    }

    private fun deleteOrUpdateOldReport(accountKey: String, reportKey: String, tax: Double?) {
        localDao.get(accountKey, reportKey)?.let { oldReport ->
            val newSize = oldReport.size - 1

            if (newSize == 0) {
                localDao.deleteDeferred(accountKey, reportKey)
            } else {
                val newTax = oldReport.tax - (tax ?: 0.0)
                localDao.save(
                    oldReport.copy(
                        size = newSize,
                        tax = newTax,
                        isSync = false,
                        syncAt = getTime()
                    )
                )
            }
        }
    }

    private fun updateReport(
        accountKey: String,
        reportKey: String,
        tax: Double?,
        isDelete: Boolean = false,
        isAdd: Boolean = false,
    ) {
        var id: Int? = null
        var newSize = 0
        var newTax = 0.0

        if (isDelete) {
            newSize--
            tax?.let { newTax -= it }
        } else if (isAdd) {
            newSize++
            tax?.let { newTax += it }
        }

        localDao.get(accountKey, reportKey)?.let {
            id = it.id
            newSize += it.size
            newTax += it.tax
        }

        localDao.save(
            LocalReportEntity(
                id = id ?: 0,
                accountKey = accountKey,
                key = reportKey,
                tax = newTax.roundUpToTwo(),
                size = newSize,
                isSync = false,
                isDelete = false,
                syncAt = getTime()
            )
        )
    }

    private fun LocalReportEntity.toReportModel() =
        ReportModel(id, key, tax, size)
}