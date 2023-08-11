package com.taxapprf.domain

import com.taxapprf.domain.account.AccountModel
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccounts(): Flow<Result<List<AccountModel>>>
    fun changeAccount(oldAccountModel: AccountModel, newAccountModel: AccountModel): Flow<Unit>
}