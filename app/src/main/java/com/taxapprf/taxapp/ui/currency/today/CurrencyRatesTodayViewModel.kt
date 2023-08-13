package com.taxapprf.taxapp.ui.currency.today

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.taxapprf.domain.currency.GetCurrencyRateTodayFromCBRUseCase
import com.taxapprf.taxapp.ui.BaseViewModel
import com.taxapprf.taxapp.ui.format
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CurrencyRatesTodayViewModel @Inject constructor(
    getTodayCBRRateUseCase: GetCurrencyRateTodayFromCBRUseCase,
) : BaseViewModel() {
    private val date = Calendar.getInstance().time
    val currencies = getTodayCBRRateUseCase.execute(date.format())
        .onStart { loading() }
        .catch { error(it) }
        .onEach { success() }
        .flowOn(Dispatchers.IO)
        .asLiveData(viewModelScope.coroutineContext)
}