package com.taxapprf.data.remote.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.taxapprf.data.remote.firebase.dao.RemoteAccountDao
import com.taxapprf.data.remote.firebase.model.FirebaseAccountModel
import com.taxapprf.data.safeCall
import com.taxapprf.domain.account.AccountModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class FirebaseAccountDaoImpl @Inject constructor(
    private val fb: FirebaseAPI,
) : RemoteAccountDao {
    override fun observeAll() = callbackFlow<Result<List<AccountModel>>> {
        safeCall {
            val callback = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val accounts =
                        snapshot.children.mapNotNull {
                            it.getValue(FirebaseAccountModel::class.java)
                                ?.toAccountModel(it.key)
                        }

                    if (accounts.isEmpty()) {
                        launch(Dispatchers.IO) { saveDefaultAccount() }
                    }

                    trySendBlocking(Result.success(accounts))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySendBlocking(Result.failure(error.toException()))
                }
            }

            if (fb.isUserAuth)
                fb.getAccountsPath().addValueEventListener(callback)

            awaitClose {
                if (!fb.isUserAuth)
                    fb.getAccountsPath().removeEventListener(callback)
            }
        }
    }

    override suspend fun save(firebaseAccountModel: FirebaseAccountModel) {
        safeCall {
            fb.getAccountsPath()
                .child(firebaseAccountModel.name!!)
                .setValue(firebaseAccountModel)
                .await()
        }
    }

    override suspend fun saveAll(accountModels: List<AccountModel>) {
        safeCall {
            val path = fb.getAccountsPath()

            accountModels.map {
                path
                    .child(it.key)
                    .setValue(it.toFirebaseAccountModel())
                    .await()
            }
        }
    }

    override suspend fun deleteAll(accountModels: List<AccountModel>) {
        safeCall {
            val path = fb.getAccountsPath()

            accountModels.map {
                path
                    .child(it.key)
                    .setValue(null)
                    .await()
            }
        }
    }

    override suspend fun saveDefaultAccount() {
        val firebaseAccountModel = FirebaseAccountModel("default", true)
        save(firebaseAccountModel)
    }

    private fun AccountModel.toFirebaseAccountModel() =
        FirebaseAccountModel(name = key, active = isActive, syncAt = syncAt)
}