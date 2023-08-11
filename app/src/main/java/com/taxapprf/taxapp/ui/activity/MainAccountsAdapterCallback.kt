package com.taxapprf.taxapp.ui.activity

import com.taxapprf.domain.account.AccountModel

interface MainAccountsAdapterCallback {
    fun onClick(accountModel: AccountModel)
    fun onClickAdd()
}