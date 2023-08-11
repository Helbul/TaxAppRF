package com.taxapprf.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.taxapprf.data.error.DataErrorAuth
import com.taxapprf.domain.FirebasePathModel
import javax.inject.Inject


class FirebaseAPI @Inject constructor() {
    private val database =
        FirebaseDatabase.getInstance()
            .apply { setPersistenceEnabled(true) }
    private val reference = database.reference

    val auth = FirebaseAuth.getInstance()

    val isUserAuth
        get() = auth.currentUser != null

    private val uid: String
        get() = auth.currentUser?.uid ?: throw DataErrorAuth()

    fun getAccountsPath() = reference
        .child(ACCOUNTS)
        .child(uid)

    fun getReportsPath(accountKey: String) = reference
        .child(REPORTS)
        .child(uid)
        .child(accountKey)

    fun getReportPath(firebasePathModel: FirebasePathModel) = reference
        .child(REPORTS)
        .child(uid)
        .child(firebasePathModel.accountName)
        .child(firebasePathModel.year)

    fun getTransactionsPath(firebasePathModel: FirebasePathModel) = reference
        .child(TRANSACTIONS)
        .child(uid)
        .child(firebasePathModel.accountName)
        .child(firebasePathModel.year)

    companion object {
        const val ACCOUNTS = "accounts"
        const val REPORTS = "reports"
        const val TRANSACTIONS = "transactions"
    }
}