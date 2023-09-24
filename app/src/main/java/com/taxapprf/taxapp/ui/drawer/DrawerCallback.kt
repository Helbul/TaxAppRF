package com.taxapprf.taxapp.ui.drawer

import com.taxapprf.domain.user.AccountModel

interface DrawerCallback {
    fun navToReports()
    fun navToCurrencyConverter()
    fun navToCurrencyRatesToday()
    fun navToSign()
    fun navToSignOut()
    fun navToAccountAdd()
    fun switchAccount(accountModel: AccountModel)
}