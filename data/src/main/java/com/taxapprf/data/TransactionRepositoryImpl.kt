package com.taxapprf.data

import com.taxapprf.data.error.CBRErrorRateIsEmpty
import com.taxapprf.data.remote.cbrapi.CBRAPI
import com.taxapprf.data.remote.firebase.FirebaseReportDaoImpl
import com.taxapprf.data.remote.firebase.FirebaseTransactionDaoImpl
import com.taxapprf.data.remote.firebase.model.FirebaseReportModel
import com.taxapprf.data.remote.firebase.model.FirebaseTransactionModel
import com.taxapprf.domain.FirebasePathModel
import com.taxapprf.domain.TransactionRepository
import com.taxapprf.domain.transaction.SaveTransactionModel
import com.taxapprf.domain.transaction.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.math.abs

class TransactionRepositoryImpl @Inject constructor(
    private val firebaseTransactionDao: FirebaseTransactionDaoImpl,
    private val firebaseReportDao: FirebaseReportDaoImpl,
    private val cbrapi: CBRAPI,
) : TransactionRepository {
    override fun getTransactions(firebasePathModel: FirebasePathModel) =
        firebaseTransactionDao.getTransactions(firebasePathModel)

    override fun saveTransactionModel(saveTransactionModel: SaveTransactionModel) = flow {
        with(saveTransactionModel) {
            val firebasePathModel = saveTransactionModel.toFirebasePathModel()

            transactionKey?.let {
                if (year != yearOld)
                    firebaseTransactionDao.deleteTransaction(firebasePathModel)
            }

            updateCBRRate()
            updateTax()

            val oldTax = firebaseReportDao.getReportTax(firebasePathModel)?.tax ?: 0.0
            val newTax = (oldTax + tax).roundUpToTwo()
            val firebaseReportModel = FirebaseReportModel(year, newTax)
            firebaseReportDao.saveReportTax(firebasePathModel, firebaseReportModel)

            firebaseTransactionDao.saveTransaction(
                firebasePathModel,
                saveTransactionModel.toFirebaseTransactionModel()
            )

            emit(Unit)
        }
    }


    private fun SaveTransactionModel.toFirebaseTransactionModel() =
        FirebaseTransactionModel(name, date, type, currency, rateCBR, sum, tax)

    private fun SaveTransactionModel.toFirebasePathModel() =
        FirebasePathModel(accountKey, year, transactionKey)

    override fun deleteTransaction(firebasePathModel: FirebasePathModel): Flow<Unit> =
        flow {
            emit(firebaseTransactionDao.deleteTransaction(firebasePathModel))
        }

    private fun SaveTransactionModel.updateCBRRate() {
        val rate = cbrapi.getCurrency(date).execute().body()
            ?.getCurrencyRate(currency)
            ?: throw CBRErrorRateIsEmpty()
        rateCBR = rate.roundUpToTwo()
    }

    private fun SaveTransactionModel.updateTax() {
        val k = when (TransactionType.valueOf(type)) {
            TransactionType.FUNDING_WITHDRAWAL -> 0
            TransactionType.COMMISSION -> {
                sum = abs(sum)
                -1
            }

            else -> 1
        }

        val taxBig = sum * rateCBR * 0.13 * k
        tax = taxBig.roundUpToTwo()
    }
}