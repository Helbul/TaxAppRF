package com.taxapprf.data

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.taxapprf.data.error.DataErrorUser
import com.taxapprf.data.error.DataErrorUserEmailAlreadyUse
import com.taxapprf.data.error.DataErrorUserWrongPassword
import kotlin.math.floor

inline fun <T> safeCall(call: () -> T): T {
    return try {
        call()
    } catch (e: Exception) {
        throw e.toAppError()
    }
}

fun Exception.toAppError() = when (this) {
    is FirebaseAuthUserCollisionException -> DataErrorUserEmailAlreadyUse()
    is FirebaseAuthInvalidCredentialsException -> DataErrorUserWrongPassword()
    is FirebaseAuthInvalidUserException -> DataErrorUserWrongPassword()
    is FirebaseException -> DataErrorUser()
    else -> this
}

fun Double.roundUpToTwo() = floor(this * 100.0) / 100.0