package com.taxapprf.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.taxapprf.data.local.room.LocalDatabase.Companion.CURRENCY_ORDINAL
import com.taxapprf.data.local.room.LocalDatabase.Companion.ID
import com.taxapprf.data.local.room.LocalDatabase.Companion.TRANSACTION_ID
import com.taxapprf.data.local.room.LocalDatabase.Companion.TYPE_ORDINAL
import com.taxapprf.data.local.room.entity.LocalAccountEntity
import com.taxapprf.data.local.room.entity.LocalCurrencyRateEntity.Companion.CURRENCY_RATE
import com.taxapprf.data.local.room.entity.LocalDeletedEntity
import com.taxapprf.data.local.room.entity.LocalReportEntity
import com.taxapprf.data.local.room.entity.LocalTransactionEntity
import com.taxapprf.data.local.room.entity.LocalTransactionEntity.Companion.DATE
import com.taxapprf.data.local.room.entity.LocalTransactionEntity.Companion.NAME
import com.taxapprf.data.local.room.entity.LocalTransactionEntity.Companion.SUM
import com.taxapprf.data.local.room.entity.LocalTransactionEntity.Companion.SUM_RUB
import com.taxapprf.data.local.room.entity.LocalTransactionEntity.Companion.TAX_RUB
import com.taxapprf.data.local.room.entity.LocalUserEntity
import com.taxapprf.data.local.room.model.sync.EmptyReportKeysDataModel
import com.taxapprf.data.local.room.model.sync.SyncResultAccountDataModel
import com.taxapprf.data.local.room.model.sync.SyncResultReportDataModel
import com.taxapprf.data.local.room.model.sync.SyncTransactionDataModel
import com.taxapprf.data.remote.firebase.Firebase.Companion.ACCOUNT_KEY
import com.taxapprf.data.remote.firebase.Firebase.Companion.REPORT_KEY
import com.taxapprf.data.sync.IS_SYNC
import com.taxapprf.data.sync.REMOTE_KEY
import com.taxapprf.data.sync.SYNC_AT

@Dao
interface LocalSyncDao {
    /* SYNC ALL */
    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): LocalUserEntity?

    /* SYNC DELETED */
    @Query("SELECT * FROM deleted ORDER BY account_key, report_key")
    fun getAllDeleted(): List<LocalDeletedEntity>

    @Query("DELETE FROM deleted")
    fun deleteAllDeleted()

    @Query("SELECT " +
            "r.id $ID, " +
            "r.remote_key $REPORT_KEY, " +
            "a.remote_key $ACCOUNT_KEY " +
            "FROM report r " +
            "LEFT JOIN account a ON a.id = r.account_id " +
            "WHERE r.size = 0")
    fun getEmptyReportKeysDataModels(): List<EmptyReportKeysDataModel>

    /* ACCOUNT SYNC */
    @Query("SELECT * FROM account WHERE user_id = :userId")
    fun getUserAccounts(userId: Int): List<LocalAccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUserAccounts(localAccountEntities: List<LocalAccountEntity>): List<Long>

    @Delete
    fun deleteUserAccounts(localAccountEntities: List<LocalAccountEntity>): Int

    @Query(
        "SELECT " +
                "id $ID, " +
                "remote_key $ACCOUNT_KEY " +
                "FROM account WHERE user_id = :userId"
    )
    fun getSyncResultUserAccounts(userId: Int): List<SyncResultAccountDataModel>

    /* REPORT SYNC */

    @Query("SELECT * FROM report WHERE account_id = :accountId")
    fun getAccountReports(accountId: Int): List<LocalReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAccountReports(reportEntities: List<LocalReportEntity>): List<Long>

    @Delete
    fun deleteAccountReports(reportEntities: List<LocalReportEntity>): Int

    @Query(
        "SELECT " +
                "r.id $ID, " +
                "a.remote_key $ACCOUNT_KEY, " +
                "r.remote_key $REPORT_KEY " +
                "FROM report r " +
                "LEFT JOIN account a ON a.id = r.account_id " +
                "WHERE r.account_id = :accountId"
    )
    fun getSyncResultAccountReports(accountId: Int): List<SyncResultReportDataModel>

    /* TRANSACTION SYNC */
    @Query(
        "SELECT " +
                "t.id $TRANSACTION_ID, " +
                "t.name $NAME, " +
                "t.date $DATE, " +
                "t.type_ordinal $TYPE_ORDINAL, " +
                "t.currency_ordinal $CURRENCY_ORDINAL, " +
                "r.currency_rate $CURRENCY_RATE, " +
                "t.sum $SUM, " +
                "t.sum_rub $SUM_RUB, " +
                "t.tax_rub $TAX_RUB, " +
                "t.remote_key $REMOTE_KEY, " +
                "t.is_sync $IS_SYNC, " +
                "t.sync_at $SYNC_AT " +
                "FROM `transaction` t " +
                "LEFT JOIN cbr_rate r ON r.currency_ordinal = t.currency_ordinal AND r.date = t.date " +
                "WHERE t.report_id = :reportId"
    )
    fun getTransactions(reportId: Int): List<SyncTransactionDataModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveLocalTransactionEntities(localTransactionEntities: List<LocalTransactionEntity>): List<Long>

    @Delete
    fun deleteLocalTransactionEntities(localTransactionEntities: List<LocalTransactionEntity>): Int

    @Query(
        "UPDATE report " +
                "SET " +
                "size = (SELECT COUNT(*) FROM `transaction` WHERE report_id = :reportId) " +
                "WHERE id = :reportId"
    )
    fun updateReportSize(reportId: Int)

    @Query("DELETE FROM report WHERE size = 0")
    fun deleteAllEmptyReport()

    @Transaction
    fun saveTransactionsWithUpdateReport(
        reportId: Int,
        localTransactionEntities: List<LocalTransactionEntity>
    ): List<Long> {
        println(localTransactionEntities)
        val result = saveLocalTransactionEntities(localTransactionEntities)
        updateReportSize(reportId)
        return result
    }

    @Transaction
    fun deleteTransactionsWithUpdateReport(
        reportId: Int,
        localTransactionEntities: List<LocalTransactionEntity>
    ) =
        if (localTransactionEntities.isNotEmpty()) {
            val result = deleteLocalTransactionEntities(localTransactionEntities)
            updateReportSize(reportId)
            deleteAllEmptyReport()
            result
        } else 0
}