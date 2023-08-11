package com.taxapprf.data.remote.firebase

import com.taxapprf.data.remote.firebase.dao.FirebaseUserDao
import com.taxapprf.data.safeCall
import com.taxapprf.domain.user.SignInModel
import com.taxapprf.domain.user.SignUpModel
import com.taxapprf.domain.user.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class FirebaseUserDaoImpl @Inject constructor(
    private val fb: FirebaseAPI,
) : FirebaseUserDao {
    override suspend fun signInAnonymously() {
        safeCall {
            fb.auth.signInAnonymously().await()
        }
    }

    override suspend fun signInWithEmailAndPassword(signInModel: SignInModel) {
        safeCall {
            with(signInModel) {
                fb.auth.signInWithEmailAndPassword(email, password).await()
            }
        }
    }

    override suspend fun signUpWithEmailAndPassword(signUpModel: SignUpModel) {
        safeCall {
            with(signUpModel) {
                fb.auth.createUserWithEmailAndPassword(email, password).await()
            }
        }
    }

    override fun signOut() {
        safeCall {
            fb.auth.signOut()
        }
    }

    override fun isSignIn() = fb.auth.currentUser != null

    override suspend fun getProfile() =
        safeCall {
            UserModel(
                name = fb.auth.currentUser?.displayName,
                email = fb.auth.currentUser?.email,
                phone = fb.auth.currentUser?.phoneNumber,
            )
        }

    override suspend fun saveProfile(userModel: UserModel) {
        safeCall {

        }
    }
}