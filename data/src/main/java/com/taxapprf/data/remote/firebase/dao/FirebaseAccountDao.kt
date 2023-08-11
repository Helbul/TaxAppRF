package com.taxapprf.data.remote.firebase.dao

import com.taxapprf.data.remote.firebase.model.FirebaseAccountModel
import com.taxapprf.domain.account.AccountModel
import kotlinx.coroutines.flow.Flow


interface FirebaseAccountDao {
    fun getAccounts(): Flow<Result<List<AccountModel>>>
    suspend fun saveAccount(firebaseAccountModel: FirebaseAccountModel)
    suspend fun saveAccounts(firebaseAccountModels: Map<String, FirebaseAccountModel>)
    suspend fun saveDefaultAccount()
}