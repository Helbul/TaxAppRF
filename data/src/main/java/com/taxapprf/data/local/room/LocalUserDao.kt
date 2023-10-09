package com.taxapprf.data.local.room

import androidx.room.Dao
import androidx.room.Query
import com.taxapprf.data.local.room.LocalDatabase.Companion.ACCOUNT_ID
import com.taxapprf.data.local.room.LocalDatabase.Companion.NAME
import com.taxapprf.data.local.room.LocalDatabase.Companion.USER_ID
import com.taxapprf.data.local.room.entity.LocalAccountEntity.Companion.IS_ACTIVE
import com.taxapprf.data.local.room.entity.LocalUserEntity
import com.taxapprf.data.local.room.entity.LocalUserEntity.Companion.AVATAR
import com.taxapprf.data.local.room.entity.LocalUserEntity.Companion.EMAIL
import com.taxapprf.data.local.room.entity.LocalUserEntity.Companion.PHONE
import com.taxapprf.data.local.room.model.GetUser
import com.taxapprf.data.local.room.model.GetUser.Companion.ACCOUNT_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalUserDao {
    @Query(
        "SELECT " +
                "u.email $EMAIL, " +
                "u.avatar $AVATAR, " +
                "u.name $NAME, " +
                "u.phone $PHONE, " +
                "a.id $ACCOUNT_ID, " +
                "a.user_id $USER_ID, " +
                "a.remote_key $ACCOUNT_NAME, " +
                "a.is_active $IS_ACTIVE " +
                "FROM user u " +
                "LEFT JOIN account a ON a.user_id = u.id " +
                "WHERE email = :email"
    )
    fun observeUsers(email: String): Flow<List<GetUser>>

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    fun getByEmail(email: String): LocalUserEntity?

    @Query("DELETE FROM user")
    fun deleteAll()
}