package com.taxapprf.taxapp.ui.account.add

import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountAddViewModel @Inject constructor() : BaseViewModel() {
    var accountName = ""

    fun checkName(): Int? {
        return if (accountName.isErrorNameRange()) R.string.account_error_input_incorrect
        else null
    }

    private fun String.isErrorNameRange() = length < 3 || length > 16
}