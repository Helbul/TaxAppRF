package com.taxapprf.taxapp.ui.sign.`in`

import androidx.lifecycle.viewModelScope
import com.taxapprf.domain.main.user.SignInModel
import com.taxapprf.domain.main.user.SignInUseCase
import com.taxapprf.domain.sync.SyncAllUseCase
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.ui.BaseViewModel
import com.taxapprf.taxapp.ui.isEmailIncorrect
import com.taxapprf.taxapp.ui.isErrorPasswordRange
import com.taxapprf.taxapp.ui.showLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val singInUseCase: SignInUseCase,
    private val syncAllUseCase: SyncAllUseCase,
) : BaseViewModel() {
    var email = ""
    var password = ""

    fun signIn() =
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                if (isUnlock) {
                    val signInModel = SignInModel(email, password)
                    singInUseCase.execute(signInModel)
                    syncAllUseCase.execute()
                    emit(Unit)
                }
            }
                .showLoading()
                .collect()
        }

    fun checkEmail(): Int? {
        return if (email.isEmpty()) R.string.sign_error_email_empty
        else if (email.isEmailIncorrect()) R.string.sign_error_email_incorrect
        else null
    }

    fun checkPassword(): Int? {
        return if (password.isErrorPasswordRange()) R.string.sign_error_password_length
        else null
    }
}