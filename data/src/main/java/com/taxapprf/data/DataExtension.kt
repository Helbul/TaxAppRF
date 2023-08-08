package com.taxapprf.data

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.taxapprf.data.error.AuthError
import com.taxapprf.data.error.SignInErrorWrongPassword
import com.taxapprf.data.error.SignUpErrorEmailAlreadyUse

inline fun <T> safeCall(call: () -> T): T {
    return try {
        call()
    } catch (e: Exception) {
        throw e.toAppError()
    }
}

fun Exception.toAppError() = when (this) {
    is FirebaseAuthUserCollisionException -> SignUpErrorEmailAlreadyUse()
    is FirebaseAuthInvalidCredentialsException -> SignInErrorWrongPassword()
    is FirebaseAuthInvalidUserException -> SignInErrorWrongPassword()
    is FirebaseException -> AuthError()
    else -> this
}