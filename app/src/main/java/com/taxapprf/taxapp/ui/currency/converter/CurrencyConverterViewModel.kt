package com.taxapprf.taxapp.ui.currency.converter

import com.taxapprf.data.error.internal.currency.converter.DataErrorInternalCurrencyConverterCalculate
import com.taxapprf.data.error.internal.currency.converter.DataErrorInternalCurrencyLoad
import com.taxapprf.data.getEpochDate
import com.taxapprf.domain.currency.CurrencyRateModel
import com.taxapprf.domain.currency.GetCurrencyRateModelsUseCase
import com.taxapprf.taxapp.ui.BaseViewModel
import com.taxapprf.taxapp.ui.showLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


@HiltViewModel
class CurrencyConverterViewModel @Inject constructor(
    private val getCurrencyRateModelsUseCase: GetCurrencyRateModelsUseCase,
) : BaseViewModel() {
    var currencyRates = emptyList<CurrencyRateModel>()
    var sum = DEFAULT_SUM
    var sumRUB = DEFAULT_SUM_RUB
    var currencyOrdinal: Int = DEFAULT_CURRENCY_ORDINAL
        set(value) {
            field = value
            recalculateSumRUB()
        }

    fun recalculateSumRUB(textSum: String? = null) {
        try {
            textSum?.let { sum = it.toDouble() }
            sumRUB = sum * currencyRates[currencyOrdinal].rate!!
        } catch (e: Exception) {
            error(DataErrorInternalCurrencyConverterCalculate())
        }
    }

    fun recalculateSum(textSumRUB: String? = null) {
        try {
            textSumRUB?.let { sumRUB = it.toDouble() }
            sum = sumRUB / currencyRates[currencyOrdinal].rate!!
        } catch (e: Exception) {
            error(DataErrorInternalCurrencyConverterCalculate())
        }
    }

    val updateCurrencyRates =
        flow {
            currencyRates = getCurrencyRateModelsUseCase.execute(getEpochDate())
            if (currencyRates.isEmpty()) throw DataErrorInternalCurrencyLoad()
            recalculateSumRUB()
            emit(Unit)
        }
            .flowOn(Dispatchers.IO)
            .showLoading()

    companion object {
        const val DEFAULT_SUM = 1.0
        const val DEFAULT_CURRENCY_ORDINAL = 13
        const val DEFAULT_SUM_RUB = 0.0
    }
}