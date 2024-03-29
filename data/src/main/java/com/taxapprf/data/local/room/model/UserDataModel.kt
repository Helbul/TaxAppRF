package com.taxapprf.data.local.room.model

import androidx.room.ColumnInfo
import com.taxapprf.data.local.room.LocalDatabase.Companion.ACCOUNT_ID
import com.taxapprf.data.local.room.LocalDatabase.Companion.NAME
import com.taxapprf.data.local.room.LocalDatabase.Companion.USER_ID
import com.taxapprf.data.local.room.entity.LocalAccountEntity.Companion.IS_ACTIVE
import com.taxapprf.data.local.room.entity.LocalUserEntity.Companion.AVATAR
import com.taxapprf.data.local.room.entity.LocalUserEntity.Companion.EMAIL
import com.taxapprf.data.local.room.entity.LocalUserEntity.Companion.PHONE

data class UserDataModel(
    @ColumnInfo(name = USER_ID)
    val userId: Int,
    @ColumnInfo(name = EMAIL)
    val email: String?,
    @ColumnInfo(name = AVATAR)
    val avatar: String?,
    @ColumnInfo(name = NAME)
    val name: String?,
    @ColumnInfo(name = PHONE)
    val phone: String?,

    @ColumnInfo(name = ACCOUNT_ID)
    val accountId: Int,
    @ColumnInfo(name = ACCOUNT_NAME)
    val accountName: String,
    @ColumnInfo(name = IS_ACTIVE)
    val isAccountActive: Boolean,
) {
    companion object {
        const val ACCOUNT_NAME = "account_name"
    }
}