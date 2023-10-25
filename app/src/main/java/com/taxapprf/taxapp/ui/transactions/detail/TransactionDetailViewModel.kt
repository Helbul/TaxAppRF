package com.taxapprf.taxapp.ui.transactions.detail

import androidx.lifecycle.SavedStateHandle
import com.taxapprf.data.getEpochDate
import com.taxapprf.data.local.room.LocalDatabase.Companion.ACCOUNT_ID
import com.taxapprf.data.local.room.LocalDatabase.Companion.REPORT_ID
import com.taxapprf.data.local.room.LocalDatabase.Companion.TRANSACTION_ID
import com.taxapprf.data.toLocalDate
import com.taxapprf.domain.currency.Currencies
import com.taxapprf.domain.main.transaction.SaveTransactionModel
import com.taxapprf.domain.toAppDate
import com.taxapprf.domain.transactions.TransactionTypes
import com.taxapprf.domain.transactions.detail.GetTransactionDetailModel
import com.taxapprf.domain.transactions.detail.GetTransactionDetailUseCase
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTransactionDetailUseCase: GetTransactionDetailUseCase,
) : BaseViewModel() {
    private val accountId = savedStateHandle.get<Int>(ACCOUNT_ID)
    private val reportId = savedStateHandle.get<Int>(REPORT_ID)
        ?.let { if (it == 0) null else it }
    private val transactionId = savedStateHandle.get<Int>(TRANSACTION_ID)
        ?.let { if (it == 0) null else it }


    var name: String = EMPTY_STRING
    var date: String = getEpochDate().toAppDate()
    var transactionTypeOrdinal: Int = TransactionTypes.TRADE.ordinal
    var currencyOrdinal: Int = Currencies.USD.ordinal
    var sum: String = EMPTY_STRING
    private var transactionTax: Double? = null

    val currencyCharCode: String
        get() = Currencies.values()[currencyOrdinal].name

    val transactionDetailModel =
        flow {
            val getTransactionDetailModel = GetTransactionDetailModel(reportId, transactionId)
            getTransactionDetailUseCase.execute(getTransactionDetailModel)
                ?.let { transactionDetailModel ->
                    transactionDetailModel.name?.let { name = it }
                    transactionDetailModel.date?.let { date = it.toAppDate() }
                    transactionDetailModel.transactionTypeOrdinal?.let {
                        transactionTypeOrdinal = it
                    }
                    transactionDetailModel.currencyOrdinal?.let { currencyOrdinal = it }
                    transactionDetailModel.sum?.let { sum = it.toString() }
                    transactionDetailModel.taxRUB?.let { transactionTax = it }
                }
            emit(Unit)
        }
            .flowOn(Dispatchers.IO)

    fun checkDate(year: Int, month: Int, dayOfMonth: Int): Int? {
        val dayFormatted = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
        val monthIncremented = month + 1
        val monthFormatted =
            if (monthIncremented < 10) "0$monthIncremented" else monthIncremented.toString()

        date = "$dayFormatted/$monthFormatted/$year"
        return checkDate()
    }

    fun checkName() =
        if (name.isNameRangeIncorrect()) R.string.transaction_detail_error_name_too_long
        else null

    fun checkDate() =
        date.toLocalDate()?.toEpochDay().let { cDate ->
            if (cDate == null) R.string.transaction_detail_error_date_format
            else if (cDate.isDateRangeIncorrect()) R.string.transaction_detail_error_date_range
            else null
        }

    fun checkSum() =
        try {
            val cSum = sum.toDouble()

            if (cSum > SUM_MAX_LENGTH) R.string.transaction_detail_error_sum_too_long
            else null
        } catch (_: java.lang.Exception) {
            R.string.transaction_detail_error_sum_empty
        }

    fun getSaveTransactionModel(): SaveTransactionModel? {
        val accountId = accountId ?: return null
        val date = date.toLocalDate()?.toEpochDay() ?: return null

        return SaveTransactionModel(
            accountId = accountId,
            reportId = reportId,
            transactionId = transactionId,
            transactionTypeOrdinal = transactionTypeOrdinal,
            currencyOrdinal = currencyOrdinal,
            name = name,
            date = date,
            sum = sumToDouble(),
            tax = transactionTax
        )
    }

    private fun sumToDouble() =
        try {
            sum.toDouble()
        } catch (_: Exception) {
            0.0
        }

    private fun Long.isDateRangeIncorrect() =
        this > LocalDate.now().toEpochDay()

    private fun String.isNameRangeIncorrect() = length > NAME_MAX_LENGTH

    companion object {
        const val NAME_MAX_LENGTH = 30
        const val SUM_MAX_LENGTH = 999999999999

        const val EMPTY_STRING = ""
    }
}