package com.taxapprf.data

import android.net.Uri
import com.taxapprf.data.local.room.LocalAccountDao
import com.taxapprf.data.local.room.LocalMainDao
import com.taxapprf.data.local.room.LocalUserDao
import com.taxapprf.data.local.room.entity.LocalAccountEntity
import com.taxapprf.data.local.room.model.GetUser
import com.taxapprf.data.remote.firebase.dao.RemoteUserDao
import com.taxapprf.domain.UserRepository
import com.taxapprf.domain.user.AccountModel
import com.taxapprf.domain.user.ObserveUserWithAccountsModel
import com.taxapprf.domain.user.SignInModel
import com.taxapprf.domain.user.SignUpModel
import com.taxapprf.domain.user.SwitchAccountModel
import com.taxapprf.domain.user.UserModel
import com.taxapprf.domain.user.UserWithAccountsModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val localMainDao: LocalMainDao,
    private val userLocalDao: LocalUserDao,
    private val userRemoteDao: RemoteUserDao,
    private val accountLocalDao: LocalAccountDao,
) : UserRepository {
    override fun observeUserWithAccounts(
        observeUserWithAccountsModel: ObserveUserWithAccountsModel
    ) =
        findUserEmail(observeUserWithAccountsModel.defaultAccountName).let { email ->
            userLocalDao.observeUsers(email)
                .map { userWithAccounts ->
                    UserWithAccountsModel(
                        user = userWithAccounts.firstOrNull()?.toUserModel(),
                        activeAccount = userWithAccounts.find { it.isAccountActive }
                            ?.toAccountModel(),
                        otherAccounts = userWithAccounts.filter { !it.isAccountActive }
                            .map { it.toAccountModel() }
                    )
                }
        }

    override suspend fun saveUser(userModel: UserModel) {
        userModel.name?.let { userRemoteDao.updateUser(it) }
    }

    override suspend fun signIn(signInModel: SignInModel) {
        userRemoteDao.signInWithEmailAndPassword(signInModel)
        updateLocalUser()
    }

    override suspend fun signUp(signUpModel: SignUpModel) {
        userRemoteDao.signUpWithEmailAndPassword(signUpModel)
        updateLocalUser()
    }

    override suspend fun signOut() {
        userRemoteDao.signOut()
    }

    override suspend fun switchAccount(switchAccountModel: SwitchAccountModel) {
        accountLocalDao.resetActiveAccount()
        if (accountLocalDao.setActiveAccount(switchAccountModel.accountName) == 0)
            localMainDao.saveAccountEntity(
                newLocalAccountEntity(
                    switchAccountModel.userId,
                    switchAccountModel.accountName
                )
            )
    }

    override suspend fun deleteAll() {
        accountLocalDao.deleteAll()
        userLocalDao.deleteAll()
    }

    private fun findUserEmail(defaultAccountName: String) =
        userRemoteDao.getUser()?.email?.let { remoteUserEmail ->
            localMainDao.getUserByEmail(remoteUserEmail)?.email
                ?: run {
                    userRemoteDao.signOut()
                    getLocalUserEmail(defaultAccountName)
                }
        } ?: getLocalUserEmail(defaultAccountName)

    private fun getLocalUserEmail(defaultAccountName: String) =
        localMainDao.getUserByEmail(LOCAL_USER_EMAIL)?.let {
            LOCAL_USER_EMAIL
        } ?: run {
            localMainDao.saveDefaultUser(defaultAccountName)
            LOCAL_USER_EMAIL
        }

    private fun updateLocalUser() {
        userLocalDao.getByEmail(LOCAL_USER_EMAIL)?.let { localUser ->
            userRemoteDao.getUser()?.let { firebaseUser ->
                val updatedLocalUserEntity =
                    localUser.copy(
                        email = firebaseUser.email,
                        avatar = firebaseUser.photoUrl?.path,
                        name = firebaseUser.displayName,
                        phone = firebaseUser.phoneNumber
                    )
                localMainDao.saveUserEntity(updatedLocalUserEntity)
            }
        }
    }

    private fun GetUser.toUserModel() =
        UserModel(userId, email, avatar?.let { Uri.parse(it) }, name, phone)

    private fun GetUser.toAccountModel() =
        AccountModel(accountId, accountName)

    private fun newLocalAccountEntity(userId: Int, accountName: String) =
        LocalAccountEntity(
            userId = userId,
            isActive = true,
            remoteKey = accountName
        )

    companion object {
        const val LOCAL_USER_EMAIL = "local"
    }
}