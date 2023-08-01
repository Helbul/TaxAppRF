package com.taxapprf.taxapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.taxapprf.domain.user.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
) : ViewModel() {
    // TODO перенести сюда состояния, для подключения. Нет индикации загрузки при заходе уже авторизованным при медленном инете
    val user = getUserUseCase.execute()
        .asLiveData(viewModelScope.coroutineContext)
}