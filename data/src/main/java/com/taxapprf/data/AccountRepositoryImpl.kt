package com.taxapprf.data

import com.taxapprf.data.remote.firebase.FirebaseAccountDaoImpl
import com.taxapprf.data.remote.firebase.model.FirebaseAccountModel
import com.taxapprf.domain.AccountRepository
import com.taxapprf.domain.account.AccountModel
import com.taxapprf.domain.account.SwitchAccountModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val firebaseAccountDao: FirebaseAccountDaoImpl,
) : AccountRepository {
    override fun getAccounts() = firebaseAccountDao.getAccounts()

    override fun switchAccount(switchAccountModel: SwitchAccountModel): Flow<Nothing> =
        flow {
            with(switchAccountModel) {
                println(switchAccountModel)
                firebaseAccountDao.saveAccounts(
                    mapOf(
                        oldAccountModel.name to oldAccountModel.toFirebaseAccountModel(),
                        newAccountModel.name to newAccountModel.toFirebaseAccountModel()
                    )
                )
            }
        }

    private fun AccountModel.toFirebaseAccountModel() =
        FirebaseAccountModel(name.trim(), !active)
}