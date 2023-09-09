package com.taxapprf.data.remote.firebase.dao

import com.taxapprf.data.remote.firebase.model.FirebaseTransactionModel
import kotlinx.coroutines.flow.Flow

interface RemoteTransactionDao {
    fun observeAll(
        accountKey: String,
        reportKey: String
    ): Flow<Result<List<FirebaseTransactionModel>>>

    suspend fun save(
        accountKey: String,
        reportKey: String,
        transactionKey: String?,
        firebaseTransactionModel: FirebaseTransactionModel
    )

    suspend fun delete(
        accountKey: String,
        reportKey: String,
        transactionKey: String
    )

    suspend fun deleteAll(
        accountKey: String,
        reportKey: String,
        transactionModels: List<FirebaseTransactionModel>
    )

    suspend fun pushAndGetKey(
        accountKey: String,
        reportKey: String,
    ): String?
}