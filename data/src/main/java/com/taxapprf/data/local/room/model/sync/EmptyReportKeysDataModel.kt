package com.taxapprf.data.local.room.model.sync

import androidx.room.ColumnInfo
import com.taxapprf.data.local.room.LocalDatabase
import com.taxapprf.data.remote.firebase.Firebase

data class EmptyReportKeysDataModel(
    @ColumnInfo(name = LocalDatabase.ID)
    val reportId: Int,
    @ColumnInfo(name = Firebase.ACCOUNT_KEY)
    val accountKey: String,
    @ColumnInfo(name = Firebase.REPORT_KEY)
    val reportKey: String,
)